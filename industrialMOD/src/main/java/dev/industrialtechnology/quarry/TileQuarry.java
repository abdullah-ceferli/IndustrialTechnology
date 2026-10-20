package dev.industrialtechnology.quarry;

import dev.industrialtechnology.core.ModConfig;
import dev.industrialtechnology.machine.TilePoweredMachine;
import dev.industrialtechnology.machine.MachineStatus;
import net.minecraft.nbt.NBTTagCompound;

public class TileQuarry extends TilePoweredMachine {
    public final MiningCursor cursor = new MiningCursor();
    public final MiningMetrics metrics = new MiningMetrics();
    public final VoidFilter filter = new VoidFilter(this);
    public int miningCeiling = -1;
    private boolean heightValidated;
    HarvestPlan waitingHarvest;
    protected int tier;
    public boolean silkMode, everStarted;
    public double work;
    public long mined, skipped;
    public String issue = "";
    public TileQuarry() { this(0); }
    protected TileQuarry(int tier) { super(54); this.tier = tier; }
    public int tier() { return tier; }
    public int cost() { return ModConfig.energyCost[tier]; }
    @Override protected int workingReserve() { return Math.min(capacity(), cost() * ModConfig.removalsPerTick); }
    public int fortuneLevel() { return !silkMode && ModConfig.fortune ? tier + 1 : 0; }
    public boolean usesSilk() { return silkMode && ModConfig.silk; }
    @Override public boolean isQuarry() { return true; }
    @Override public int capacity() { return ModConfig.capacity[tier]; }
    @Override public String machineName() { return "Quarry MK" + (tier + 1); }
    public void ensureMiningHeight() {
        if (heightValidated && miningCeiling <= Math.max(0, yCoord)) return;
        miningCeiling = Math.max(0, Math.min(255, miningCeiling < 0 ? yCoord : Math.min(yCoord, miningCeiling)));
        cursor.clampBelow(miningCeiling); heightValidated = true;
    }
    @Override public boolean wantsChunks() { ensureMiningHeight(); return ModConfig.autoLoad && enabled && cursor.choose(selected); }
    @Override public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        ensureMiningHeight();
        metrics.advance(worldObj.getTotalWorldTime());
        if (!cursor.choose(selected)) {
            status = MachineStatus.COMPLETED;
            if (enabled) { enabled = false; tickets.release(); markDirty(); worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); }
        } else if (!enabled) status = everStarted ? MachineStatus.STOPPED : MachineStatus.IDLE;
        else if (!ticketError.isEmpty()) status = MachineStatus.CHUNK_LOADING_ERROR;
        else if (!worldObj.getChunkProvider().chunkExists(cursor.x(xCoord) >> 4, cursor.z(zCoord) >> 4)) {
            status = MachineStatus.CHUNK_LOADING_ERROR; issue = "Target chunk is not loaded";
        }
        else if (!output.pending.isEmpty() || !filter.enabled && !output.hasSpace()) status = MachineStatus.OUTPUT_FULL;
        else {
            if (issue.equals("Target chunk is not loaded")) issue = "";
            int rate = operatingInput();
            double previous = work;
            work = MiningRate.advance(work, rate, ModConfig.speedTarget, ModConfig.maxBlocksPerTick[tier], ModConfig.speedMultiplier[tier], ModConfig.removalsPerTick);
            if (work != previous) markDirty();
            if (rate <= 0 || energy < cost()) status = MachineStatus.NO_POWER;
            else { status = metrics.blocksPerSecond() > 0 ? MachineStatus.MINING : MachineStatus.IDLE; MiningScheduler.submit(this); }
        }
    }
    @Override public void toggleEnabled() {
        if (!cursor.choose(selected)) return;
        super.toggleEnabled();
        if (enabled) { everStarted = true; issue = ""; }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    public void setMode(boolean silk) {
        if (silk && !ModConfig.silk || !silk && !ModConfig.fortune) return;
        silkMode = silk; waitingHarvest = null; markDirty();
    }
    @Override protected void onSelectionChanged() {
        ensureMiningHeight(); cursor.choose(selected); issue = ""; waitingHarvest = null;
        // Existing per-chunk positions survive deselection and later reselection.
    }
    public boolean ready() { return work + 1.0e-9 >= 1.0 && energy >= cost(); }
    public void harvested() {
        consume(cost()); work = Math.max(0, work - 1.0); mined++;
        metrics.record(worldObj.getTotalWorldTime()); if (enabled) status = MachineStatus.MINING; markDirty();
    }
    public void miningFailure(String reason) { issue = reason; enabled = false; status = MachineStatus.STOPPED; tickets.release(); markDirty(); syncVisual(); }
    @Override public void writeToNBT(NBTTagCompound tag) {
        ensureMiningHeight(); super.writeToNBT(tag); tag.setInteger("Tier", tier); tag.setBoolean("Silk", silkMode);
        tag.setInteger("MiningCeiling", miningCeiling); filter.write(tag);
        tag.setBoolean("EverStarted", everStarted); tag.setIntArray("Cursor", cursor.copy());
        tag.setInteger("CursorCell", cursor.cell); tag.setDouble("Work", work);
        tag.setLong("Mined", mined); tag.setLong("Skipped", skipped); tag.setString("Issue", issue);
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        // Tier is determined by the registered TileEntity class, never by untrusted saved data.
        super.readFromNBT(tag); silkMode = tag.getBoolean("Silk"); everStarted = tag.getBoolean("EverStarted");
        cursor.read(tag.getIntArray("Cursor")); cursor.cell = Math.max(0, Math.min(24, tag.getInteger("CursorCell")));
        miningCeiling = tag.hasKey("MiningCeiling") ? tag.getInteger("MiningCeiling") : yCoord;
        heightValidated = false; ensureMiningHeight(); waitingHarvest = null; filter.read(tag);
        cursor.choose(selected); double value = tag.getDouble("Work");
        if (tag.getInteger("DataVersion") < 2) value /= new int[]{10, 7, 5}[tier];
        work = Double.isNaN(value) || Double.isInfinite(value) ? 0 : Math.max(0, Math.min(ModConfig.removalsPerTick, value));
        mined = Math.max(0, tag.getLong("Mined")); skipped = Math.max(0, tag.getLong("Skipped")); issue = tag.getString("Issue");
    }
    @Override public NBTTagCompound snapshot() {
        NBTTagCompound n = super.snapshot();
        n.setInteger("MiningCeiling", Math.max(0, miningCeiling));
        // Icons synchronize through individual vanilla ghost slots, avoiding the legacy 32-KiB NBT packet limit.
        n.setBoolean("VoidEnabled",filter.enabled); n.setBoolean("VoidIgnoreMetadata",filter.ignoreMetadata);
        n.setInteger("VoidCapacity",filter.getSizeInventory()); n.setInteger("VoidConfigured",filter.configured());
        n.setInteger("Tier", tier); n.setBoolean("Silk", silkMode); n.setInteger("Fortune", fortuneLevel());
        n.setInteger("Cost", cost()); n.setInteger("MaxBlocksPerTick", ModConfig.maxBlocksPerTick[tier]);
        n.setDouble("SpeedMultiplier", ModConfig.speedMultiplier[tier]); n.setInteger("OperationLimit", ModConfig.removalsPerTick);
        n.setInteger("MeasuredSpeed", metrics.blocksPerSecond());
        n.setDouble("PotentialSpeed", Math.min(20.0 * operatingInput() / Math.max(1, cost()),
            20.0 * Math.min(ModConfig.removalsPerTick, ModConfig.maxBlocksPerTick[tier] * ModConfig.speedMultiplier[tier])
                * Math.min(operatingInput(), ModConfig.speedTarget) / Math.max(1.0, ModConfig.speedTarget)));
        n.setBoolean("FortuneAllowed", ModConfig.fortune); n.setBoolean("SilkAllowed", ModConfig.silk); n.setBoolean("AutoLoad", ModConfig.autoLoad);
        n.setInteger("SpeedTarget", ModConfig.speedTarget); n.setInteger("Completed", cursor.completedMask());
        n.setInteger("Cell", cursor.cell); n.setInteger("Y", cursor.y()); n.setLong("Scanned", cursor.scannedBelow(selected, miningCeiling));
        n.setDouble("Work", work); n.setLong("Mined", mined); n.setLong("Skipped", skipped);
        n.setInteger("Pending", output.pending.size()); n.setString("Issue", issue);
        return n;
    }
    @Override protected NBTTagCompound visualState() {
        ensureMiningHeight(); NBTTagCompound n = super.visualState(); n.setInteger("MiningCeiling", miningCeiling); return n;
    }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager, net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
        super.onDataPacket(manager, packet); miningCeiling = Math.max(0, Math.min(yCoord, packet.func_148857_g().getInteger("MiningCeiling")));
    }
    public static final class MK1 extends TileQuarry { public MK1() { super(0); } }
    public static final class MK2 extends TileQuarry { public MK2() { super(1); } }
    public static final class MK3 extends TileQuarry { public MK3() { super(2); } }
}
