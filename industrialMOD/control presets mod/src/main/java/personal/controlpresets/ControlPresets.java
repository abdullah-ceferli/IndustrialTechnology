package personal.controlpresets;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import java.io.File;

@Mod(modid = "controlpresets", name = "Control Presets", version = "1.0.0",
        acceptableRemoteVersions = "*")
public final class ControlPresets {
    private File directory;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        directory = new File(event.getModConfigurationDirectory(), "controlpresets");
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) ControlsClient.initialize(directory);
    }
}
