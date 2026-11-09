package personal.offlineskins.client;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetHandler;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.input.Keyboard;
import personal.offlineskins.CommonProxy;
import personal.offlineskins.OfflineSkins;
import personal.offlineskins.Packets;
import personal.offlineskins.SkinFiles;
import personal.offlineskins.SkinImage;
import personal.offlineskins.SharingPolicy;

public final class ClientSkins extends CommonProxy {
    private static final ResourceLocation LOCAL = new ResourceLocation(OfflineSkins.ID, "local");
    private static final UUID NO_PLAYER = new UUID(0, 0);
    private final Map<UUID, ResourceLocation> remote = new HashMap<UUID, ResourceLocation>();
    private final WeakHashMap<AbstractClientPlayer, ResourceLocation> originals = new WeakHashMap<AbstractClientPlayer, ResourceLocation>();
    private final ArrayBlockingQueue<Incoming> incoming = new ArrayBlockingQueue<Incoming>(2048);
    private final KeyBinding key = new KeyBinding("key.offlineskins.choose", Keyboard.KEY_F8, "key.categories.offlineskins");
    private INetHandler activeHandler;
    private Configuration configuration;
    private SkinFiles files;
    private SkinImage local;
    private String selected;
    private boolean sharing;
    private boolean lanSharing;
    private String status = "";
    private boolean initialLoad = true;
    private File directory;
    private FriendsLink friendsLink;
    private FriendsLink.State friendsState;
    private String friendsEndpoint = "";
    private boolean friendsOnly;
    private boolean friendsActive;
    private final Map<String, FriendTexture> friendTextures = new HashMap<String, FriendTexture>();
    private final Map<String, FriendsLink.Image> friendImages = new HashMap<String, FriendsLink.Image>();
    private static final class FriendTexture {
        final String hash; final ResourceLocation location;
        FriendTexture(String hash,ResourceLocation location){this.hash=hash;this.location=location;}
    }

    private static final class Incoming {
        final INetHandler handler;
        final UUID player;
        final byte[] png;
        final int protocol;
        Incoming(INetHandler handler, UUID player, byte[] png, int protocol) {
            this.handler = handler; this.player = player; this.png = png; this.protocol = protocol;
        }
    }

    @Override public void initialize(File directory) {
        this.directory=directory;
        files = new SkinFiles(new File(directory, "skins"));
        try {
            Files.createDirectories(files.directory().toPath());
            File help = new File(files.directory(), "PUT-SKINS-HERE.txt");
            if (!help.exists()) Files.write(help.toPath(), (
                    "Put your skin PNGs here. Name your first skin skin.png.\r\n"
                    + "In Minecraft press F8 or type /offlineskin to choose a skin.\r\n"
                    + "64x32 and 64x64 PNGs up to 24 KiB are supported. Classic player model only.\r\n"
                    + "Install this same mod on the server and all clients to share skins.\r\n"
                    + "No website, online account, or internet connection is needed.\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) { OfflineSkins.log.warn("Could not create skin folder", ex); }
        configuration = new Configuration(new File(directory, "client.cfg"));
        configuration.load();
        selected = configuration.get("skin", "selected", "skin.png", "Filename in the skins folder. Empty disables the custom skin.").getString();
        friendsOnly=configuration.get("sharing","friendsOnly",false,"Friends mode never uploads your skin to the Minecraft server.").getBoolean(false);
        friendsEndpoint=configuration.get("sharing","service","","Friends service address, including its #certificate fingerprint when supplied.").getString();
        ClientRegistry.registerKeyBinding(key);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new SkinCommand(this));
    }

    @Override public void hello(INetHandler handler, int protocol) {
        incoming.offer(new Incoming(handler, NO_PLAYER, null, protocol));
    }

    @Override public void receive(INetHandler handler, UUID player, byte[] png) {
        incoming.offer(new Incoming(handler, player, png, 0));
    }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        // Forge preInit runs before Minecraft creates its texture manager.
        if (initialLoad) {
            initialLoad = false;
            reload(false);
            if(friendsOnly && !friendsEndpoint.isEmpty()) {
                try { startFriends(friendsEndpoint); }
                catch(IOException ex){status=ex.getMessage();}
            }
        }
        INetHandler current = mc.getNetHandler();
        if (current != activeHandler) {
            lanSharing = false;
            clearRemote();
            activeHandler = current;
            sharing = false;
        }
        if (current == null) { tickFriends(); return; }
        for (int count = 0; count < 64; count++) {
            Incoming message = incoming.poll();
            if (message == null) break;
            // Packets from a previous server can never overwrite the new server's skins.
            if (message.handler != current) continue;
            if (message.png == null) {
                if (Packets.Hello.compatible(message.protocol)) {
                    net.minecraft.network.NetworkManager connection = mc.getNetHandler().getNetworkManager();
                    lanSharing = SharingPolicy.acceptLan(Packets.Hello.isLan(message.protocol),
                            connection.isLocalChannel(), isLanOpen(), connection.getSocketAddress());
                    sharing = true;
                    upload();
                }
                continue;
            }
            if (mc.thePlayer != null && message.player.equals(mc.thePlayer.getUniqueID())) continue;
            if(lanSharing || !friendsOnly) updateRemote(message.player, message.png);
        }
        tickFriends();
        applyAll();
    }

    @SubscribeEvent public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) applyAll();
    }

    @SubscribeEvent public void renderPlayer(RenderPlayerEvent.Pre event) {
        if (event.entityPlayer instanceof AbstractClientPlayer) apply((AbstractClientPlayer) event.entityPlayer);
    }

    @SubscribeEvent public void key(InputEvent.KeyInputEvent event) {
        if (key.isPressed()) Minecraft.getMinecraft().displayGuiScreen(new SkinScreen(null));
    }

    private void applyAll() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) apply(mc.thePlayer);
        if (mc.theWorld != null) {
            for (Object player : mc.theWorld.playerEntities) {
                if (player instanceof AbstractClientPlayer && player != mc.thePlayer) apply((AbstractClientPlayer) player);
            }
        }
    }

    private ResourceLocation overrideFor(AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (player == mc.thePlayer) return local == null ? null : LOCAL;
        if(friendsOnly && !lanSharing) {
            if(!friendsActive)return null;
            FriendsLink.Image image=friendImages.get(player.getCommandSenderName().toLowerCase(java.util.Locale.ROOT));
            if(image==null)return null;
            FriendTexture texture=friendTextures.get(image.code);
            if(texture==null || !texture.hash.equals(image.hash)) {
                if(texture!=null)release(texture.location);
                ResourceLocation location=new ResourceLocation(OfflineSkins.ID,"friends/"+image.code);
                mc.getTextureManager().loadTexture(location,new DynamicTexture(image.skin.image));
                texture=new FriendTexture(image.hash,location);friendTextures.put(image.code,texture);
            }
            return texture.location;
        }
        return remote.get(player.getUniqueID());
    }

    private void apply(AbstractClientPlayer player) {
        ResourceLocation override = overrideFor(player);
        ResourceLocation current = player.getLocationSkin();
        if (override != null) {
            if (!override.equals(current)) {
                // Preserve a real Mojang skin if its asynchronous lookup completes later.
                if(!current.getResourceDomain().equals(OfflineSkins.ID))originals.put(player, current);
                player.func_152121_a(MinecraftProfileTexture.Type.SKIN, override);
            }
        } else {
            ResourceLocation original = originals.remove(player);
            if (original != null && current.getResourceDomain().equals(OfflineSkins.ID)) {
                player.func_152121_a(MinecraftProfileTexture.Type.SKIN,
                        original.equals(AbstractClientPlayer.locationStevePng) ? null : original);
            }
        }
    }

    private void updateRemote(UUID player, byte[] png) {
        ResourceLocation previous = remote.get(player);
        if (png.length == 0) {
            remote.remove(player);
            applyAll();
            release(previous);
            return;
        }
        try {
            SkinImage image = SkinImage.decode(png);
            ResourceLocation texture = new ResourceLocation(OfflineSkins.ID, "players/" + player);
            release(previous);
            Minecraft.getMinecraft().getTextureManager().loadTexture(texture, new DynamicTexture(image.image));
            remote.put(player, texture);
        } catch (IOException ex) { OfflineSkins.log.warn("Ignored invalid skin from server: {}", ex.getMessage()); }
    }

    private void clearRemote() {
        Map<UUID, ResourceLocation> previous = new HashMap<UUID, ResourceLocation>(remote);
        remote.clear();
        // Also restore players in the previous world before releasing their textures.
        for (AbstractClientPlayer player : new java.util.ArrayList<AbstractClientPlayer>(originals.keySet())) {
            if (player != null) apply(player);
        }
        for (ResourceLocation texture : previous.values()) release(texture);
    }

    public void select(String filename) throws IOException {
        if (!filename.isEmpty()) {
            // Validate before changing the selection or its working texture.
            SkinImage.read(files.resolve(filename));
        }
        selected = filename;
        configuration.get("skin", "selected", "skin.png").set(selected);
        configuration.save();
        reload(true);
    }

    public void reload(boolean announce) {
        try {
            SkinImage next = selected.isEmpty() ? null : SkinImage.read(files.resolve(selected));
            local = next;
            if (next == null) {
                applyAll();
                release(LOCAL);
            } else {
                release(LOCAL);
                Minecraft.getMinecraft().getTextureManager().loadTexture(LOCAL, new DynamicTexture(next.image));
            }
            applyAll();
            upload();
            if(friendsLink!=null)friendsLink.skin(playerName(),local==null?new byte[0]:local.png);
            status = next == null ? "Custom skin disabled." : "Using " + selected;
        } catch (IOException ex) {
            // A broken replacement file never removes a previously working texture.
            status = ex.getMessage();
        }
        if (announce) chat(status);
    }

    private void upload() {
        if (sharing && Minecraft.getMinecraft().getNetHandler() == activeHandler) {
            boolean mayShare=SharingPolicy.mayUpload(friendsOnly,
                    Minecraft.getMinecraft().isIntegratedServerRunning(), lanSharing);
            OfflineSkins.network.sendToServer(new Packets.Upload(!mayShare || local == null ? new byte[0] : local.png));
        }
    }

    private String playerName() {
        Minecraft mc=Minecraft.getMinecraft();
        return mc.thePlayer!=null?mc.thePlayer.getCommandSenderName():mc.getSession().getUsername();
    }

    private void tickFriends() {
        Minecraft mc=Minecraft.getMinecraft();
        boolean active=friendsOnly && friendsLink!=null && !lanSharing && !mc.isIntegratedServerRunning()
                && (mc.theWorld!=null || mc.currentScreen instanceof FriendsScreen);
        if(active!=friendsActive) {
            friendsActive=active;
            if(!active) {friendImages.clear();applyAll();clearFriendTextures();}
        }
        if(friendsLink==null)return;
        friendsLink.skin(playerName(),local==null?new byte[0]:local.png);
        friendsLink.active(active);
        FriendsLink.State next=friendsLink.poll();
        if(next==null)return;
        friendsState=next;
        friendImages.clear();
        if(active) {
            java.util.Set<String> ambiguous=new java.util.HashSet<String>();
            for(FriendsLink.Image image:next.images.values()) {
                String name=image.name.toLowerCase(java.util.Locale.ROOT);
                if(friendImages.put(name,image)!=null)ambiguous.add(name);
            }
            for(String name:ambiguous)friendImages.remove(name);
        }
        applyAll();
        java.util.Set<String> codes=new java.util.HashSet<String>();for(FriendsLink.Image image:friendImages.values())codes.add(image.code);
        java.util.Iterator<Map.Entry<String,FriendTexture>> it=friendTextures.entrySet().iterator();
        while(it.hasNext()){Map.Entry<String,FriendTexture> entry=it.next();if(!codes.contains(entry.getKey())){release(entry.getValue().location);it.remove();}}
    }
    private void clearFriendTextures(){for(FriendTexture texture:friendTextures.values())release(texture.location);friendTextures.clear();}
    private void startFriends(String address) throws IOException {
        FriendsLink next=new FriendsLink(directory,address,playerName(),local==null?new byte[0]:local.png);
        if(friendsLink!=null)friendsLink.close();friendsLink=next;friendsState=null;friendsEndpoint=next.endpoint();
    }
    public void connectFriends(String address) throws IOException {
        startFriends(address);friendsOnly=true;
        configuration.get("sharing","service","").set(friendsEndpoint);
        configuration.get("sharing","friendsOnly",false).set(true);configuration.save();
        if(!lanSharing)clearRemote();upload();tickFriends();
    }
    public void disconnectFriends() {
        if(friendsLink!=null)friendsLink.close();friendsLink=null;friendsActive=false;friendsState=null;
        friendImages.clear();applyAll();clearFriendTextures();
        // Remain in friends-only mode: disconnecting never silently enables public server sharing.
        configuration.get("sharing","service","").set("");configuration.save();upload();
    }
    public void useServerSharing() {
        disconnectFriends();friendsOnly=false;
        configuration.get("sharing","friendsOnly",false).set(false);configuration.save();upload();
    }
    public void friendAction(String action,String code) {
        if(friendsLink==null || !friendsLink.action(action,code))chat("Friends sharing is paused. Join a multiplayer server or open Friends from the main menu.");
    }
    public FriendsLink.State friendsState(){return friendsState;}
    public String friendsEndpoint(){return friendsEndpoint;}
    public boolean friendsOnly(){return friendsOnly;}
    public boolean friendsActive(){return friendsActive;}
    public boolean lanSharing(){return lanSharing;}

    @Override public boolean isLanOpen() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.isIntegratedServerRunning() && mc.getIntegratedServer().getPublic();
    }

    private static void release(ResourceLocation texture) {
        if (texture == null) return;
        ITextureObject object = Minecraft.getMinecraft().getTextureManager().getTexture(texture);
        // Unlike TextureManager.deleteTexture in 1.7.10, this resets the stored
        // GL identifier, so disabling and re-enabling cannot delete a reused ID.
        if (object instanceof AbstractTexture) ((AbstractTexture) object).deleteGlTexture();
    }

    public void openFolder() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(files.directory());
                return;
            }
        } catch (IOException | UnsupportedOperationException | SecurityException ex) {
            OfflineSkins.log.debug("Could not open skin folder", ex);
        }
        chat(files.directory().getAbsolutePath());
    }

    public void chat(String text) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Offline Skins] " + text));
        }
    }

    public SkinFiles files() { return files; }
    public String selected() { return selected; }
    public String status() { return status; }
    public boolean sharing() { return sharing; }
    public ResourceLocation preview() { return local == null ? null : LOCAL; }
}
