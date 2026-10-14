package dev.industrialtechnology.core;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class ModConfig {
    public static int maxChunks = 25, inputRate = 20000, speedTarget = 20000;
    public static int scansPerTick = 256, removalsPerTick = 5, globalScans = 4096, globalRemovals = 40;
    public static boolean autoLoad = true, fortune = true, silk = true, preview = true, debug = false;
    private static final int[] DEFAULT_CAPACITY = {400000, 1600000, 6400000};
    public static final int[] capacity = DEFAULT_CAPACITY.clone();
    public static final int[] energyCost = {4000, 4000, 4000};
    public static final int[] maxBlocksPerTick = {5, 5, 5};
    public static final double[] speedMultiplier = {1.0, 1.0, 1.0};
    private ModConfig() {}
    public static void load(File file) {
        Configuration c = new Configuration(file);
        c.load();
        migrateLegacyBalance(c);
        migrateHighPowerBalance(c);
        maxChunks = c.getInt("maximumSelectableChunks", "general", 25, 1, 25, "Hard upper bound: 25. Center is always selected.");
        inputRate = c.getInt("rfInputPerTick", "energy", 20000, 1, 1000000, "Aggregate input limit across all six sides.");
        speedTarget = c.getInt("maximumSpeedRFPerTick", "energy", 20000, 1, 1000000, "Average incoming RF/t required for full speed.");
        scansPerTick = c.getInt("scansPerMachinePerTick", "performance", 256, 1, 512, "Maximum cursor positions checked per machine per tick.");
        removalsPerTick = c.getInt("removalsPerMachinePerTick", "performance", 5, 1, 16, "Hard maximum real blocks removed per machine per tick.");
        globalScans = c.getInt("globalScansPerTick", "performance", 4096, 1, 8192, "Shared scan budget across all dimensions.");
        globalRemovals = c.getInt("globalRemovalsPerTick", "performance", 40, 1, 128, "Shared removal budget; round-robin scheduling.");
        for (int i = 0; i < 3; i++) {
            String category = "quarrymk" + (i + 1);
            capacity[i] = c.getInt("rfCapacity", category, DEFAULT_CAPACITY[i], 1000, 100000000, "Stored RF.");
            energyCost[i] = c.getInt("rfPerBlock", category, 4000, 1, capacity[i], "Charged only for successful harvesting.");
            maxBlocksPerTick[i] = c.getInt("maximumBlocksPerTick", category, 5, 1, 16, "Target maximum at maximumSpeedRFPerTick. Actual rate also requires RF per block.");
            speedMultiplier[i] = c.getFloat("miningSpeedMultiplier", category, 1.0f, 0.1f, 4.0f, "Multiplies target work rate; cannot bypass energy or operation budgets.");
        }
        autoLoad = c.getBoolean("automaticQuarryChunkLoading", "general", true, "Force exactly the selected chunks while running.");
        fortune = c.getBoolean("enableFortune", "general", true, "Enable tier Fortune levels.");
        silk = c.getBoolean("enableSilkTouch", "general", true, "Enable supported Silk Touch harvesting.");
        preview = c.getBoolean("enableAreaPreview", "client", true, "Render selected chunk outlines after clicking Preview.");
        debug = c.getBoolean("debugLogging", "general", false, "Log mining and ticket diagnostics.");
        if (c.hasChanged()) c.save();
    }
    private static void migrateHighPowerBalance(Configuration c) {
        if (c.hasKey("general", "balanceVersion") && c.get("general", "balanceVersion", 2).getInt() >= 2) return;
        migrateValue(c, "energy", "rfInputPerTick", 200, 20000);
        migrateValue(c, "energy", "maximumSpeedRFPerTick", 200, 20000);
        int[] oldCapacity = {40000, 160000, 640000};
        for (int i = 0; i < 3; i++) {
            String category = "quarrymk" + (i + 1);
            migrateValue(c, category, "rfPerBlock", 50, 4000);
            migrateValue(c, category, "maximumBlocksPerTick", 4, 5);
            migrateValue(c, category, "rfCapacity", oldCapacity[i], DEFAULT_CAPACITY[i]);
        }
        migrateValue(c, "performance", "removalsPerMachinePerTick", 4, 5);
        migrateValue(c, "performance", "globalRemovalsPerTick", 32, 40);
        c.get("general", "balanceVersion", 2, "Defaults migration version; custom values are preserved.").set(2);
    }
    private static void migrateValue(Configuration c, String category, String key, int oldValue, int newValue) {
        if (c.hasKey(category, key) && c.get(category, key, newValue).getInt() == oldValue) c.get(category, key, newValue).set(newValue);
    }
    private static void migrateLegacyBalance(Configuration c) {
        boolean legacy = c.hasKey("quarrymk1", "minimumTicksPerBlock");
        int[] oldCost = {1600, 1200, 1000};
        for (int i = 0; i < 3; i++) {
            String category = "quarrymk" + (i + 1);
            if (legacy && c.get(category, "rfPerBlock", 50).getInt() == oldCost[i]) c.get(category, "rfPerBlock", 50).set(50);
            c.getCategory(category).remove("minimumTicksPerBlock");
            String obsolete = "quarryMK" + (i + 1);
            if (c.hasCategory(obsolete) && c.getCategory(obsolete).isEmpty()) c.removeCategory(c.getCategory(obsolete));
        }
        if (legacy) {
            migrateDefault(c, "removalsPerMachinePerTick", 1, 4);
            migrateDefault(c, "scansPerMachinePerTick", 64, 256);
            migrateDefault(c, "globalScansPerTick", 1024, 4096);
            migrateDefault(c, "globalRemovalsPerTick", 16, 32);
        }
        c.getCategory("energy").remove("chunkLoaderRFPerTick");
    }
    private static void migrateDefault(Configuration c, String key, int oldValue, int newValue) {
        if (c.hasKey("performance", key) && c.get("performance", key, newValue).getInt() == oldValue)
            c.get("performance", key, newValue).set(newValue);
    }
}
