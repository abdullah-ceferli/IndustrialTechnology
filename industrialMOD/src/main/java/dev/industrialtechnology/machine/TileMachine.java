package dev.industrialtechnology.machine;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.core.ModConfig;
import dev.industrialtechnology.chunkloading.ChunkTickets;
import dev.industrialtechnology.quarry.ChunkArea;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

/** Shared identity, area selection, controls, persistence, and tickets. No RF/inventory interface. */
public abstract class TileMachine extends TileEntity {
    public final ChunkTickets tickets = new ChunkTickets(this);
    public int selected = ChunkArea.CENTER, loadedCount, loadedMask;
    public boolean enabled;
    public MachineStatus status = MachineStatus.IDLE;
    public String ticketError = "";
    public UUID machineId = UUID.randomUUID();
    protected UUID ownerId = new UUID(0, 0);
    protected String ownerName = "[IndustrialTech]";
    private GameProfile cachedOwner;
    private boolean initialized;
    public abstract String machineName();
    public abstract boolean wantsChunks();
    public boolean isQuarry() { return false; }
    public void setOwner(EntityPlayer player) {
        ownerId = player.getUniqueID(); ownerName = player.getCommandSenderName(); cachedOwner = null; markDirty();
    }
    public GameProfile ownerProfile() {
        if (cachedOwner == null) cachedOwner = new GameProfile(ownerId, ownerName);
        return cachedOwner;
    }
    @Override public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;
        if (!initialized) { initialized = true; tickets.dirty(); }
        tickets.tick(wantsChunks());
    }
    public void applySelection(int mask) {
        if (!ChunkArea.valid(mask, ModConfig.maxChunks) || mask == selected) return;
        selected = mask; onSelectionChanged(); tickets.dirty();
        if (worldObj != null && !worldObj.isRemote) tickets.tick(wantsChunks());
        markDirty(); syncVisual();
    }
    protected void onSelectionChanged() {}
    public void toggleEnabled() {
        enabled = !enabled; tickets.dirty();
        if (!enabled) { tickets.release(); status = isQuarry() ? MachineStatus.STOPPED : MachineStatus.DISABLED; }
        else { tickets.tick(wantsChunks()); status = ticketError.isEmpty() ? MachineStatus.IDLE : MachineStatus.CHUNK_LOADING_ERROR; }
        markDirty(); worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    public void destroyed() { enabled = false; tickets.release(); }
    public void syncVisual() { if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); }
    public void setLoadedMask(int mask) {
        if (loadedMask == mask) return;
        loadedMask = mask; loadedCount = Integer.bitCount(mask); syncVisual();
    }
    @Override public void invalidate() { tickets.release(); super.invalidate(); }
    @Override public void onChunkUnload() { tickets.release(); super.onChunkUnload(); }
    public boolean isUseableByPlayer(EntityPlayer player) {
        return !isInvalid() && player.worldObj == worldObj && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("DataVersion", 3); tag.setString("MachineId", machineId.toString());
        tag.setString("OwnerId", ownerId.toString()); tag.setString("OwnerName", ownerName);
        tag.setInteger("Selected", selected); tag.setBoolean("Enabled", enabled); tag.setInteger("Status", status.ordinal());
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        machineId = parseId(tag.getString("MachineId"), machineId);
        ownerId = parseId(tag.getString("OwnerId"), new UUID(0, 0)); cachedOwner = null;
        if (!tag.getString("OwnerName").isEmpty()) ownerName = tag.getString("OwnerName");
        selected = ChunkArea.sanitize(tag.getInteger("Selected"), ModConfig.maxChunks); enabled = tag.getBoolean("Enabled");
        status = MachineStatus.values()[Math.max(0, Math.min(MachineStatus.values().length - 1, tag.getInteger("Status")))];
        loadedMask = loadedCount = 0; tickets.dirty(); initialized = false;
    }
    private static UUID parseId(String value, UUID fallback) {
        try { return UUID.fromString(value); } catch (IllegalArgumentException e) { return fallback; }
    }
    public NBTTagCompound snapshot() {
        NBTTagCompound n = new NBTTagCompound();
        n.setInteger("Selected", selected); n.setInteger("Loaded", loadedCount);
        n.setInteger("Status", status.ordinal()); n.setBoolean("Enabled", enabled);
        n.setString("TicketError", ticketError); n.setInteger("MaxChunks", ModConfig.maxChunks);
        n.setBoolean("PreviewAllowed", ModConfig.preview); return n;
    }
    @Override public Packet getDescriptionPacket() {
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, visualState());
    }
    protected NBTTagCompound visualState() {
        NBTTagCompound n = new NBTTagCompound(); n.setBoolean("Active", enabled);
        n.setInteger("Selected", selected); n.setInteger("LoadedMask", loadedMask); return n;
    }
    @Override public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound n = packet.func_148857_g(); enabled = n.getBoolean("Active");
        selected = ChunkArea.sanitize(n.getInteger("Selected"), 25); loadedMask = n.getInteger("LoadedMask") & ChunkArea.ALL;
        loadedCount = Integer.bitCount(loadedMask);
    }
}
