package personal.offlineskins.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import personal.offlineskins.OfflineSkins;
import personal.offlineskins.client.ClientSkins;
import personal.offlineskins.client.SkinScreen;
import personal.offlineskins.client.FriendsScreen;

public final class SoloProbe extends Probe {
    private final File root = new File(System.getProperty("skin.test.root"));
    private int stage, ticks, frames;
    @Override public void init() { FMLCommonHandler.instance().bus().register(this); }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        try {
            if (stage == 0 && mc.currentScreen instanceof GuiMainMenu) {
                stage = 1;
                mc.launchIntegratedServer("offline-skin-test", "Offline Skin Test", new WorldSettings(7L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.FLAT));
            }
            if (mc.thePlayer == null || ++ticks % 10 != 0) return;
            ClientSkins skins = (ClientSkins) OfflineSkins.proxy;
            if (stage == 1 && skins.sharing() && color() == 0xffcc3333) {
                mark("singleplayer-initial"); skins.select("alternate.png"); stage = 2;
            } else if (stage == 2 && color() == 0xff33cc66) {
                mark("singleplayer-change"); skins.select(""); stage = 3;
            } else if (stage == 3 && !mc.thePlayer.getLocationSkin().getResourceDomain().equals(OfflineSkins.ID)) {
                mark("singleplayer-clear"); skins.select("skin.png"); stage = 4;
            } else if (stage == 4 && color() == 0xffcc3333) {
                mark("singleplayer-restore");
                skins.connectFriends("http://127.0.0.1:1");stage=40;
            } else if(stage==40) {
                if(skins.friendsActive())throw new AssertionError("Friends networking active in single-player");
                if(color()!=0xffcc3333)throw new AssertionError("Local skin lost while friends networking is paused");
                mark("singleplayer-friends-paused");
                String port=mc.getIntegratedServer().shareToLAN(WorldSettings.GameType.SURVIVAL,false);
                if(port==null)throw new AssertionError("Temporary Open to LAN test failed");
                stage=41;
            } else if(stage==41) {
                if(skins.friendsActive())throw new AssertionError("Friends networking active after Open to LAN");
                if(color()!=0xffcc3333)throw new AssertionError("Local skin lost after Open to LAN");
                if(skins.friendsState()!=null && !skins.friendsState().code.isEmpty())throw new AssertionError("Unexpected service registration from single-player/LAN");
                mark("lan-friends-paused");mc.displayGuiScreen(new FriendsScreen(null));stage=5;
            }
        } catch (Throwable ex) { fail(ex); }
    }

    @SubscribeEvent public void render(TickEvent.RenderTickEvent event) {
        if (stage != 5 || event.phase != TickEvent.Phase.END || ++frames < 10) return;
        Minecraft mc = Minecraft.getMinecraft();
        try {
            net.minecraft.util.ScreenShotHelper.saveScreenshot(root, "full-pack-friends-screen.png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            mark("singleplayer-complete");
            stage = 6;
            mc.theWorld.sendQuittingDisconnectingPacket();
            mc.loadWorld(null);
            mc.shutdown();
        } catch (Throwable ex) { fail(ex); }
    }

    private int color() {
        Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation resource = mc.thePlayer.getLocationSkin();
        if (!resource.getResourceDomain().equals(OfflineSkins.ID)) return 0;
        Object texture = mc.getTextureManager().getTexture(resource);
        return texture instanceof DynamicTexture ? ((DynamicTexture) texture).getTextureData()[8 + 8 * 64] : 0;
    }
    private void mark(String value) throws Exception {
        Files.write(new File(root, value + ".txt").toPath(), ("PASS " + value).getBytes(StandardCharsets.UTF_8));
        System.out.println("OFFLINE_SKINS_TEST PASS " + value);
    }
    private void fail(Throwable ex) {
        try { Files.write(new File(root, "singleplayer-FAILED.txt").toPath(), ex.toString().getBytes(StandardCharsets.UTF_8)); }
        catch (Exception ignored) { }
        ex.printStackTrace(); Minecraft.getMinecraft().shutdown();
    }
}
