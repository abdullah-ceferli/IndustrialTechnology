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
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import personal.offlineskins.OfflineSkins;
import personal.offlineskins.client.ClientSkins;
import personal.offlineskins.client.SkinScreen;

public class ClientProbe extends Probe {
    private final File root = new File(System.getProperty("skin.test.root"));
    private final boolean a = System.getProperty("skin.test.role").equals("A");
    private int stage, ticks;
    private AbstractClientPlayer oldPlayer;
    private long started = System.currentTimeMillis();
    private int previewFrames;
    private long reconnectAt;
    @Override public void init() {
        if(Boolean.getBoolean("skin.test.lan"))new LanProbe().init();
        else if(Boolean.getBoolean("skin.test.friends"))new FriendsProbe().init();
        else if (Boolean.getBoolean("skin.test.single")) new SoloProbe().init();
        else FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        try {
            if (System.currentTimeMillis() - started > 90000) throw new AssertionError("Client integration timeout at stage " + stage);
            if (stage == 71) {
                if (System.currentTimeMillis() >= reconnectAt) {
                    mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, "127.0.0.1", Integer.getInteger("skin.test.port")));
                    stage = 8;
                }
                return;
            }
            if (mc.thePlayer == null || mc.theWorld == null || ++ticks % 5 != 0) return;
            ClientSkins skins = (ClientSkins) OfflineSkins.proxy;
            AbstractClientPlayer other = null;
            for (Object value : mc.theWorld.playerEntities) {
                if (value instanceof AbstractClientPlayer && ((AbstractClientPlayer)value).getCommandSenderName().equals(a ? "SkinTestB" : "SkinTestA")) other = (AbstractClientPlayer) value;
            }
            if (ticks % 100 == 0) {
                System.out.println("SKIN_TEST_STATE stage=" + stage + " role=" + (a ? "A" : "B") + " sharing=" + skins.sharing()
                        + " local=" + mc.thePlayer.getLocationSkin() + " color=" + Integer.toHexString(color(mc.thePlayer))
                        + " other=" + (other == null ? "missing" : other.getLocationSkin() + ":" + Integer.toHexString(color(other)))
                        + " position=" + mc.thePlayer.posX + "," + mc.thePlayer.posY + "," + mc.thePlayer.posZ
                        + " players=" + mc.theWorld.playerEntities.size());
            }
            if (stage == 0 && other != null && skins.sharing()
                    && color(mc.thePlayer) == (a ? 0xffcc3333 : 0xff3366cc)
                    && color(other) == (a ? 0xff3366cc : 0xffcc3333)) {
                mark(a ? "a-initial" : "b-initial");
                stage = 1;
            }
            if (a) {
                if (stage == 1 && has("b-initial")) { skins.select("alternate.png"); mark("a-change"); stage = 2; }
                if (stage == 2 && has("b-change")) { skins.select(""); mark("a-clear"); stage = 3; }
                if (stage == 3 && has("b-clear")) { skins.select("skin.png"); mark("a-restore"); stage = 4; }
                if (stage == 4 && has("b-restore")) {
                    oldPlayer = mc.thePlayer;
                    mc.thePlayer.sendChatMessage("/kill");
                    stage = 5;
                }
                if (stage == 5 && mc.thePlayer.getHealth() <= 0) { mc.thePlayer.respawnPlayer(); mc.displayGuiScreen(null); stage = 6; }
                if (stage == 6 && mc.thePlayer != oldPlayer && color(mc.thePlayer) == 0xffcc3333) {
                    skins.select("alternate.png"); mark("a-respawn-change"); stage = 7;
                }
                if (stage == 7 && has("b-respawn-change")) {
                    mc.theWorld.sendQuittingDisconnectingPacket();
                    mc.loadWorld(null);
                    mc.displayGuiScreen(new GuiMainMenu());
                    reconnectAt = System.currentTimeMillis() + 1500;
                    stage = 71;
                    return;
                }
                if (stage == 8 && other != null && skins.sharing() && color(mc.thePlayer) == 0xff33cc66 && color(other) == 0xff3366cc) {
                    mark("a-rejoined"); stage = 9;
                }
                if (stage == 9 && has("b-rejoined")) { mc.displayGuiScreen(new SkinScreen(null)); stage = 10; }
            } else if (other != null) {
                if (stage == 1 && has("a-change") && color(other) == 0xff33cc66) { mark("b-change"); stage = 2; }
                if (stage == 2 && has("a-clear") && !other.getLocationSkin().getResourceDomain().equals(OfflineSkins.ID)) { mark("b-clear"); stage = 3; }
                if (stage == 3 && has("a-restore") && color(other) == 0xffcc3333) { mark("b-restore"); stage = 4; }
                if (stage == 4 && has("a-respawn-change") && color(other) == 0xff33cc66) { mark("b-respawn-change"); stage = 5; }
                if (stage == 5 && has("a-rejoined") && color(other) == 0xff33cc66) { mark("b-rejoined"); stage = 6; }
            }
            if (!a && stage == 6 && has("a-complete")) { mark("b-complete"); mc.shutdown(); }
        } catch (Throwable ex) {
            try { Files.write(new File(root, (a ? "a" : "b") + "-FAILED.txt").toPath(), ex.toString().getBytes(StandardCharsets.UTF_8)); }
            catch (Exception ignored) { }
            ex.printStackTrace();
            mc.shutdown();
        }
    }

    @SubscribeEvent public void render(TickEvent.RenderTickEvent event) {
        if (!a || stage != 10 || event.phase != TickEvent.Phase.END || ++previewFrames < 10) return;
        Minecraft mc = Minecraft.getMinecraft();
        try {
            net.minecraft.util.ScreenShotHelper.saveScreenshot(root, "skin-chooser.png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            mark("a-complete");
        } catch (Exception ex) { throw new RuntimeException(ex); }
        mc.shutdown();
    }

    private int color(AbstractClientPlayer player) {
        ResourceLocation location = player.getLocationSkin();
        if (!location.getResourceDomain().equals(OfflineSkins.ID)) return 0;
        ITextureObject object = Minecraft.getMinecraft().getTextureManager().getTexture(location);
        if (!(object instanceof DynamicTexture)) return 0;
        return ((DynamicTexture) object).getTextureData()[8 + 8 * 64];
    }
    private boolean has(String name) { return new File(root, name + ".txt").exists(); }
    private void mark(String name) throws Exception {
        Files.write(new File(root, name + ".txt").toPath(), ("PASS " + name).getBytes(StandardCharsets.UTF_8));
        System.out.println("OFFLINE_SKINS_TEST PASS " + name);
    }
}
