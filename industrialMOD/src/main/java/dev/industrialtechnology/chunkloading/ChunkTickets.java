package dev.industrialtechnology.chunkloading;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.quarry.ChunkArea;
import net.minecraft.world.World;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** One owner may use multiple tickets when the server permits fewer than 25 chunks per ticket. */
public final class ChunkTickets {
    private final TileMachine tile;
    private final List<Ticket> owned = new ArrayList<Ticket>();
    private boolean dirty = true, requested;
    private long retryAt;
    public ChunkTickets(TileMachine tile) { this.tile = tile; }
    public void dirty() { dirty = true; retryAt = 0; }
    public void tick(boolean active) {
        if (tile.getWorldObj() == null || tile.getWorldObj().isRemote) return;
        if (!active) { if (!owned.isEmpty()) release(); requested = false; tile.ticketError = ""; return; }
        if (active != requested) dirty = true;
        requested = active;
        long now = tile.getWorldObj().getTotalWorldTime();
        if (!dirty || now < retryAt) return;
        reconcile();
    }
    private Set<ChunkCoordIntPair> desired() {
        Set<ChunkCoordIntPair> set = new LinkedHashSet<ChunkCoordIntPair>();
        // Keep the machine chunk first, including under a restrictive ticket depth.
        set.add(new ChunkCoordIntPair(tile.xCoord >> 4, tile.zCoord >> 4));
        for (int cell = 0; cell < 25; cell++) if ((tile.selected & (1 << cell)) != 0)
            set.add(new ChunkCoordIntPair((tile.xCoord >> 4) + ChunkArea.dx(cell), (tile.zCoord >> 4) + ChunkArea.dz(cell)));
        return set;
    }
    private void reconcile() {
        Set<ChunkCoordIntPair> target = desired();
        int available = 0;
        for (Ticket ticket : owned) available += depth(ticket);
        while (available < target.size()) {
            Ticket ticket = ForgeChunkManager.requestTicket(IndustrialTechnology.instance, tile.getWorldObj(), ForgeChunkManager.Type.NORMAL);
            if (ticket == null) { fail("Forge ticket limit reached"); return; }
            owned.add(ticket); stamp(ticket);
            int depth = depth(ticket);
            if (depth < 1) { fail("Forge grants no chunk capacity"); return; }
            available += depth;
        }
        // Reassign only on configuration or restoration, never every tick.
        int needed = 0, sum = 0;
        while (sum < target.size()) sum += depth(owned.get(needed++));
        while (owned.size() > needed) ForgeChunkManager.releaseTicket(owned.remove(owned.size() - 1));
        List<ChunkCoordIntPair> chunks = new ArrayList<ChunkCoordIntPair>(target);
        int cursor = 0;
        for (Ticket ticket : owned) {
            Set<ChunkCoordIntPair> assigned = new HashSet<ChunkCoordIntPair>();
            for (int i = 0; i < depth(ticket) && cursor < chunks.size(); i++) assigned.add(chunks.get(cursor++));
            for (ChunkCoordIntPair chunk : new ArrayList<ChunkCoordIntPair>(ticket.getChunkList()))
                if (!assigned.contains(chunk)) ForgeChunkManager.unforceChunk(ticket, chunk);
            for (ChunkCoordIntPair chunk : assigned) if (!ticket.getChunkList().contains(chunk)) ForgeChunkManager.forceChunk(ticket, chunk);
        }
        Set<ChunkCoordIntPair> actual = new HashSet<ChunkCoordIntPair>();
        for (Ticket ticket : owned) actual.addAll(ticket.getChunkList());
        if (!actual.equals(target)) { fail("Forge rejected the selected region"); return; }
        tile.setLoadedMask(tile.selected); tile.ticketError = ""; dirty = false;
        if (dev.industrialtechnology.core.ModConfig.debug) IndustrialTechnology.log.info("Loaded {} chunks for {} at {},{},{}", actual.size(), tile.machineId, tile.xCoord, tile.yCoord, tile.zCoord);
    }
    private static int depth(Ticket ticket) {
        int limit = ticket.getChunkListDepth();
        // Forge's configuration documents zero as disabling chunk loading; never bypass that policy.
        return limit <= 0 || ticket.getMaxChunkListDepth() <= 0 ? 0 : Math.min(25, limit);
    }
    private void stamp(Ticket ticket) {
        NBTTagCompound n = ticket.getModData();
        n.setInteger("x", tile.xCoord); n.setInteger("y", tile.yCoord); n.setInteger("z", tile.zCoord);
        n.setString("MachineId", tile.machineId.toString());
    }
    private void fail(String message) {
        release(); tile.ticketError = message; dirty = true;
        retryAt = tile.getWorldObj().getTotalWorldTime() + 200;
    }
    public void release() {
        for (Ticket ticket : owned) ForgeChunkManager.releaseTicket(ticket);
        owned.clear(); tile.setLoadedMask(0); dirty = true;
    }
    private void adopt(Ticket ticket) { if (!owned.contains(ticket)) owned.add(ticket); dirty = true; }
    public static final class Callback implements ForgeChunkManager.LoadingCallback {
        @Override public void ticketsLoaded(List<Ticket> tickets, World world) {
            for (Ticket ticket : tickets) {
                NBTTagCompound n = ticket.getModData();
                // Loading the owner chunk is necessary to validate a persisted ticket, once per restored ticket.
                world.getChunkFromChunkCoords(n.getInteger("x") >> 4, n.getInteger("z") >> 4);
                TileEntity te = world.getTileEntity(n.getInteger("x"), n.getInteger("y"), n.getInteger("z"));
                if (te instanceof TileMachine) {
                    TileMachine tile = (TileMachine) te;
                    if (tile.machineId.toString().equals(n.getString("MachineId")) && tile.wantsChunks()) {
                        tile.tickets.adopt(ticket); continue;
                    }
                }
                ForgeChunkManager.releaseTicket(ticket);
            }
        }
    }
    public static final class WorldEvents {
        @SubscribeEvent public void unload(WorldEvent.Unload event) {
            // Forge persists its tickets before world teardown. Clear scheduler references to the old world.
            dev.industrialtechnology.quarry.MiningScheduler.removeWorld(event.world);
        }
    }
}
