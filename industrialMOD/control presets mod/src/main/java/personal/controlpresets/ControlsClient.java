package personal.controlpresets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public final class ControlsClient {
    public static ControlsClient instance;
    private final PresetStore store;
    private boolean loaded;
    private ControlsClient(File directory) { store = new PresetStore(directory); }
    public static void initialize(File directory) {
        instance = new ControlsClient(directory);
        MinecraftForge.EVENT_BUS.register(instance);
    }

    private static final class PresetsButton extends GuiButton {
        PresetsButton(int x, int y) { super(23174, x, y, 102, 20, "Saved Controls"); }
    }
    @SubscribeEvent public void controls(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.gui instanceof GuiControls)) return;
        // Preserve the key list and existing settings; share vanilla's bottom row.
        for (Object value : event.buttonList) {
            GuiButton button = (GuiButton)value;
            if (button.id == 200) { button.width = 100; button.xPosition = event.gui.width / 2 - 155; }
            if (button.id == 201) { button.width = 100; button.xPosition = event.gui.width / 2 - 51; }
            if (button instanceof PresetsButton) return;
        }
        event.buttonList.add(new PresetsButton(event.gui.width / 2 + 53, event.gui.height - 29));
    }
    @SubscribeEvent public void clicked(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiControls && event.button instanceof PresetsButton) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(new PresetsScreen(event.gui, this));
        }
    }
    public PresetStore store() throws IOException {
        if (!loaded) {
            store.read();
            if (store.names().isEmpty()) store.save("Initial controls", capture());
            loaded = true;
        }
        return store;
    }
    public PresetStore.Snapshot capture() {
        GameSettings options = Minecraft.getMinecraft().gameSettings;
        PresetStore.Snapshot result = new PresetStore.Snapshot();
        result.sensitivity = options.mouseSensitivity;
        result.invertMouse = options.invertMouse;
        result.touchscreen = options.touchscreen;
        Map<String, Integer> occurrences = new HashMap<String, Integer>();
        for (KeyBinding key : options.keyBindings) {
            PresetStore.Binding entry = new PresetStore.Binding(key.getKeyCategory(), key.getKeyDescription(), 0, key.getKeyCode());
            String base = entry.id();
            int occurrence = occurrences.containsKey(base) ? occurrences.get(base) : 0;
            occurrences.put(base, occurrence + 1);
            entry.occurrence = occurrence;
            result.bindings.add(entry);
        }
        return result;
    }
    public String load(String name) throws IOException {
        PresetStore presets = store();
        PresetStore.Snapshot requested = presets.get(name);
        // Persist the undo snapshot before changing any live bindings.
        presets.saveBeforeLoad(capture());
        Map<String, Integer> saved = new HashMap<String, Integer>();
        for (PresetStore.Binding key : requested.bindings) saved.put(key.id(), key.keyCode);
        PresetStore.Snapshot current = capture();
        GameSettings options = Minecraft.getMinecraft().gameSettings;
        int applied = 0;
        for (int i = 0; i < current.bindings.size(); i++) {
            Integer code = saved.get(current.bindings.get(i).id());
            if (code != null) { options.keyBindings[i].setKeyCode(code); applied++; }
        }
        options.mouseSensitivity = requested.sensitivity;
        options.invertMouse = requested.invertMouse;
        options.touchscreen = requested.touchscreen;
        KeyBinding.unPressAllKeys();
        KeyBinding.resetKeyBindingArrayAndHash();
        options.saveOptions();
        return "Loaded " + applied + " keys; " + (requested.bindings.size() - applied) + " unavailable. Backup saved.";
    }
}
