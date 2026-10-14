package dev.industrialtechnology.quarry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.World;
import dev.industrialtechnology.core.ModConfig;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Fair bounded work across all worlds: one cursor step per round, rotating the first machine. */
public final class MiningScheduler {
    private static final Set<TileQuarry> PENDING = new LinkedHashSet<TileQuarry>();
    private static int first;
    public static void submit(TileQuarry tile) { PENDING.add(tile); }
    public static void clear() { PENDING.clear(); first = 0; }
    public static void removeWorld(World world) { PENDING.removeIf(q -> q.getWorldObj() == world); }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        List<TileQuarry> machines = new ArrayList<TileQuarry>(PENDING); PENDING.clear();
        int size = machines.size(), scanned = 0, removed = 0;
        int[] scans = new int[size], removals = new int[size];
        boolean[] stopped = new boolean[size];
        boolean progress = true;
        while (progress && scanned < ModConfig.globalScans && removed < ModConfig.globalRemovals) {
            progress = false;
            for (int j = 0; j < size && scanned < ModConfig.globalScans && removed < ModConfig.globalRemovals; j++) {
                int i = (first + j) % size;
                TileQuarry tile = machines.get(i);
                if (stopped[i] || scans[i] >= ModConfig.scansPerTick || removals[i] >= ModConfig.removalsPerTick || tile.isInvalid() || !tile.enabled) continue;
                MiningEngine.Result result;
                try { result = MiningEngine.step(tile); }
                catch (RuntimeException e) {
                    tile.miningFailure("Block handler failed during scanning");
                    dev.industrialtechnology.core.IndustrialTechnology.log.error("Quarry scan failed", e);
                    result = MiningEngine.Result.WAIT;
                }
                scans[i]++; scanned++;
                if (result == MiningEngine.Result.WAIT) stopped[i] = true;
                else { progress = true; if (result == MiningEngine.Result.MINED) { removals[i]++; removed++; } }
            }
        }
        first = (first + 1) % size;
    }
}
