package personal.offlineskins.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import personal.offlineskins.OfflineSkins;
import personal.offlineskins.client.ClientSkins;
import personal.offlineskins.client.SkinScreen;

/** Two actual clients, with one hosting a disposable integrated LAN world. */
public final class LanProbe extends Probe {
    private final File root = new File(System.getProperty("skin.test.root"));
    private final boolean host = "A".equals(System.getProperty("skin.test.role"));
    private final long started = System.currentTimeMillis();
    private int stage, ticks, frames;
    private long reconnectAt;
    private AbstractClientPlayer beforeRespawn;

    @Override public void init() { FMLCommonHandler.instance().bus().register(this); }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        try {
            if (System.currentTimeMillis() - started > 210000) throw new AssertionError("LAN timeout at stage " + stage);
            if (host && stage == 0 && mc.currentScreen instanceof GuiMainMenu) {
                stage = 1;
                mc.launchIntegratedServer("lan-skin-test", "LAN Skin Test",
                        new WorldSettings(17L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.FLAT));
            }
            if (!host && stage == 71) {
                if (System.currentTimeMillis() >= reconnectAt) {
                    int port = Integer.parseInt(new String(Files.readAllBytes(new File(root,"lan-port.txt").toPath()), StandardCharsets.UTF_8).trim());
                    mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, "127.0.0.1", port));
                    stage = 8;
                }
                return;
            }
            if (host && stage == 92 && mc.currentScreen instanceof GuiMainMenu) {
                stage = 93;
                mc.launchIntegratedServer("followup-solo", "Followup Solo",
                        new WorldSettings(18L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.FLAT));
            }
            if (mc.thePlayer == null || mc.theWorld == null || ++ticks % 5 != 0) return;
            ClientSkins skins = (ClientSkins) OfflineSkins.proxy;
            AbstractClientPlayer other = other();
            if (ticks % 100 == 0) System.out.println("LAN_TEST_STATE role="+(host?"host":"guest")+" stage="+stage
                    +" lan="+skins.lanSharing()+" friends="+skins.friendsActive()+" own="+Integer.toHexString(color(mc.thePlayer))
                    +" other="+(other==null?"missing":Integer.toHexString(color(other))));
            if (skins.lanSharing() && skins.friendsActive()) throw new AssertionError("External service active during LAN sharing");
            if (!skins.friendsOnly()) throw new AssertionError("Saved friends-only preference was changed");
            if (host) {
                if (stage == 1 && skins.sharing() && color(mc.thePlayer) == 0xffcc3333) {
                    if (skins.lanSharing() || skins.friendsActive()) throw new AssertionError("Private single-player shared externally");
                    mark("host-singleplayer-local"); skins.select("alternate.png"); stage=2;
                } else if (stage == 2 && color(mc.thePlayer) == 0xff33cc66) {
                    mark("host-singleplayer-change"); skins.select("skin.png");
                    mc.getIntegratedServer().setOnlineMode(false);
                    String port=mc.getIntegratedServer().shareToLAN(WorldSettings.GameType.SURVIVAL,true);
                    if(port==null)throw new AssertionError("Open to LAN failed");
                    Files.write(new File(root,"lan-port.txt").toPath(),port.getBytes(StandardCharsets.UTF_8)); stage=3;
                } else if (stage == 3 && skins.lanSharing()) {
                    mark("host-lan-auto"); stage=4;
                } else if (stage == 4 && other!=null && color(other)==0xff3366cc && color(mc.thePlayer)==0xffcc3333) {
                    mark("host-sees-guest"); stage=5;
                } else if (stage == 5 && has("guest-sees-host")) {
                    skins.select("alternate.png");mark("host-change");stage=6;
                } else if (stage == 6 && has("guest-change") && other!=null && color(other)==0xffeecc33) {
                    mark("host-sees-guest-change");skins.select("");mark("host-clear");stage=7;
                } else if (stage == 7 && has("guest-sees-host-clear")) {
                    skins.select("skin.png");mark("host-restore");stage=8;
                } else if (stage == 8 && has("guest-clear") && other!=null && !custom(other)) {
                    mark("host-sees-guest-clear");stage=9;
                } else if (stage == 9 && has("guest-restore") && other!=null && color(other)==0xff3366cc) {
                    mark("host-sees-guest-restore");stage=10;
                } else if (stage == 10 && has("guest-rejoined") && other!=null && color(other)==0xff3366cc) {
                    mark("host-sees-guest-rejoined");beforeRespawn=mc.thePlayer;mc.thePlayer.sendChatMessage("/kill");stage=11;
                } else if (stage == 11 && mc.thePlayer.getHealth()<=0) {
                    mc.thePlayer.respawnPlayer();mc.displayGuiScreen(null);stage=12;
                } else if (stage == 12 && mc.thePlayer!=beforeRespawn && color(mc.thePlayer)==0xffcc3333) {
                    skins.select("alternate.png");mark("host-respawn-change");stage=13;
                } else if (stage == 13 && has("guest-sees-host-respawn")) {
                    skins.select("skin.png");mark("host-final-restore");stage=14;
                } else if (stage == 14 && has("guest-complete")) {
                    mc.displayGuiScreen(new SkinScreen(null));stage=90;
                } else if (stage == 93 && skins.sharing() && color(mc.thePlayer)==0xffcc3333) {
                    if(skins.lanSharing() || skins.friendsActive())throw new AssertionError("LAN sharing leaked into the next private world");
                    mark("host-private-again");mark("host-complete");stage=94;
                    mc.theWorld.sendQuittingDisconnectingPacket();mc.loadWorld(null);mc.displayGuiScreen(new GuiMainMenu());mc.shutdown();
                }
            } else {
                if (stage == 0 && skins.lanSharing() && other!=null && color(other)==0xffcc3333 && color(mc.thePlayer)==0xff3366cc) {
                    mark("guest-sees-host");stage=1;
                } else if (stage == 1 && has("host-change") && other!=null && color(other)==0xff33cc66) {
                    mark("guest-sees-host-change");skins.select("alternate.png");mark("guest-change");stage=2;
                } else if (stage == 2 && has("host-clear") && other!=null && !custom(other)) {
                    mark("guest-sees-host-clear");stage=3;
                } else if (stage == 3 && has("host-restore") && other!=null && color(other)==0xffcc3333) {
                    skins.select("");mark("guest-clear");stage=4;
                } else if (stage == 4 && has("host-sees-guest-clear")) {
                    skins.select("skin.png");mark("guest-restore");stage=5;
                } else if (stage == 5 && has("host-sees-guest-restore")) {
                    mark("guest-left");mc.theWorld.sendQuittingDisconnectingPacket();mc.loadWorld(null);
                    mc.displayGuiScreen(new GuiMainMenu());reconnectAt=System.currentTimeMillis()+1500;stage=71;
                } else if (stage == 8 && skins.lanSharing() && other!=null && color(other)==0xffcc3333 && color(mc.thePlayer)==0xff3366cc) {
                    mark("guest-rejoined");stage=9;
                } else if (stage == 9 && has("host-respawn-change") && other!=null && color(other)==0xff33cc66) {
                    mark("guest-sees-host-respawn");stage=10;
                } else if (stage == 10 && has("host-final-restore") && other!=null && color(other)==0xffcc3333) {
                    mark("guest-complete");stage=11;mc.theWorld.sendQuittingDisconnectingPacket();mc.loadWorld(null);mc.displayGuiScreen(new GuiMainMenu());mc.shutdown();
                }
            }
        } catch(Throwable ex) { fail(ex); }
    }

    @SubscribeEvent public void render(TickEvent.RenderTickEvent event) {
        if(!host || stage!=90 || event.phase!=TickEvent.Phase.END || ++frames<10)return;
        Minecraft mc=Minecraft.getMinecraft();
        try {
            net.minecraft.util.ScreenShotHelper.saveScreenshot(root,"lan-skin-chooser.png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
            stage=92;mc.theWorld.sendQuittingDisconnectingPacket();mc.loadWorld(null);mc.displayGuiScreen(new GuiMainMenu());
        }catch(Throwable ex){fail(ex);}
    }
    private AbstractClientPlayer other(){
        for(Object value:Minecraft.getMinecraft().theWorld.playerEntities)if(value instanceof AbstractClientPlayer
                && ((AbstractClientPlayer)value).getCommandSenderName().equals(host?"SkinTestB":"SkinTestA"))return (AbstractClientPlayer)value;
        return null;
    }
    private boolean custom(AbstractClientPlayer player){return player.getLocationSkin().getResourceDomain().equals(OfflineSkins.ID);}
    private int color(AbstractClientPlayer player){
        if(!custom(player))return 0;
        ResourceLocation location=player.getLocationSkin();
        Object texture=Minecraft.getMinecraft().getTextureManager().getTexture(location);
        return texture instanceof DynamicTexture?((DynamicTexture)texture).getTextureData()[8+8*64]:0;
    }
    private boolean has(String name){return new File(root,name+".txt").exists();}
    private void mark(String name)throws Exception{
        Files.write(new File(root,name+".txt").toPath(),("PASS "+name).getBytes(StandardCharsets.UTF_8));
        System.out.println("OFFLINE_SKINS_TEST PASS "+name);
    }
    private void fail(Throwable ex){
        try{Files.write(new File(root,(host?"host":"guest")+"-FAILED.txt").toPath(),ex.toString().getBytes(StandardCharsets.UTF_8));}catch(Exception ignored){}
        ex.printStackTrace();Minecraft.getMinecraft().shutdown();
    }
}
