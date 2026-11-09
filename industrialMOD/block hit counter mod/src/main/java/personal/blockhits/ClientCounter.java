package personal.blockhits;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class ClientCounter {
    public static ClientCounter instance;
    private final HitCounter counter = new HitCounter();
    private final KeyBinding toggle = new KeyBinding("key.blockhitcounter.toggle", Keyboard.KEY_G,
            "key.categories.blockhitcounter");
    private String label = "0";

    public static void initialize() {
        instance = new ClientCounter();
        ClientRegistry.registerKeyBinding(instance.toggle);
        MinecraftForge.EVENT_BUS.register(instance);
        FMLCommonHandler.instance().bus().register(instance);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void mouse(MouseEvent event) {
        if (event.button >= 0 && event.buttonstate) press(event.button - 100, false);
    }

    @SubscribeEvent public void keyboard(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            int code = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
            press(code, Keyboard.isRepeatEvent());
        }
    }

    // Shared by the mouse and keyboard paths so rebound Attack/Toggle keys work too.
    void press(int code, boolean repeat) {
        Minecraft mc = Minecraft.getMinecraft();
        if (repeat || code == 0 || mc.theWorld == null || mc.thePlayer == null
                || mc.currentScreen != null || !mc.inGameHasFocus) return;
        if (code == toggle.getKeyCode()) {
            counter.toggle();
            label = "0";
        } else if (counter.enabled() && code == mc.gameSettings.keyBindAttack.getKeyCode()) {
            MovingObjectPosition target = mc.objectMouseOver;
            if (target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && !mc.theWorld.isAirBlock(target.blockX, target.blockY, target.blockZ)) {
                counter.hit();
                label = Long.toString(counter.hits());
            }
        }
    }

    @SubscribeEvent public void overlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !counter.enabled()
                || mc.theWorld == null || mc.thePlayer == null || mc.currentScreen != null
                || mc.gameSettings.hideGUI) return;
        mc.fontRenderer.drawStringWithShadow(label,
                event.resolution.getScaledWidth() - mc.fontRenderer.getStringWidth(label) - 8, 8, 0xffffff);
    }

    @SubscribeEvent public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        counter.reset();
        label = "0";
    }

    public HitCounter state() { return counter; }
}
