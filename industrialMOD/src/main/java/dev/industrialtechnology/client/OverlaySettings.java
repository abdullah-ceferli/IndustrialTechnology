package dev.industrialtechnology.client;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

/** Per-client visibility preferences. Never changes server mining or tickets. */
public final class OverlaySettings {
    public static boolean quarries = true, loaders = true;
    private static Configuration config;
    private OverlaySettings() {}
    public static void load() {
        config = new Configuration(new File(Minecraft.getMinecraft().mcDataDir, "config/industrialtech-client.cfg"));
        config.load();
        quarries = config.getBoolean("showMiningAreas", "overlays", true, "Show nearby active quarry boundaries.");
        loaders = config.getBoolean("showLoadedChunks", "overlays", true, "Show actual chunks forced by nearby enabled loaders.");
        if (config.hasChanged()) config.save();
    }
    public static void toggle(boolean quarry) {
        if (quarry) quarries = !quarries; else loaders = !loaders;
        if (config != null) {
            config.get("overlays", "showMiningAreas", true).set(quarries);
            config.get("overlays", "showLoadedChunks", true).set(loaders); config.save();
        }
    }
}
