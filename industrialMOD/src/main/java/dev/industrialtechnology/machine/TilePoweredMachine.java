package dev.industrialtechnology.machine;

import java.util.Arrays;
import cofh.api.energy.IEnergyHandler;
import dev.industrialtechnology.core.ModConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/** Powered machines opt into RF and output storage. The Chunk Loader has neither capability. */
public abstract class TilePoweredMachine extends TileMachine implements ISidedInventory, IEnergyHandler {
    public final OutputInventory output;
    public int energy, inputAverage;
    private int saturatedInput;
    private final int[] accessible, inputSamples = new int[20];
    private int sampleIndex, sampleSum, receivedThisTick;
    private long receiveTick = Long.MIN_VALUE, lastRateTick = Long.MIN_VALUE;
    protected TilePoweredMachine(int slots) {
        output = new OutputInventory(slots); accessible = new int[slots];
        for (int i = 0; i < slots; i++) accessible[i] = i;
    }
    public abstract int capacity();
    @Override public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        if (!dev.industrialtechnology.space.DimensionActivity.active(worldObj)) return;
        updateInputAverage();
        int previous = saturatedInput;
        if (energy < capacity() - workingReserve()) saturatedInput = 0;
        else if (energy == capacity() && inputAverage > 0) saturatedInput = Math.max(saturatedInput, inputAverage);
        boolean flushed = output.flush();
        if (previous != saturatedInput || flushed) markDirty();
    }
    private void updateInputAverage() {
        long tick = worldObj.getTotalWorldTime();
        if (lastRateTick == tick) return;
        lastRateTick = tick;
        int sample = receiveTick == tick - 1 ? receivedThisTick : 0;
        // Reception already happened this tick: the previous tick was finalized by receiveEnergy.
        if (receiveTick != tick) addInputSample(sample);
    }
    private void addInputSample(int sample) {
        sampleSum -= inputSamples[sampleIndex]; inputSamples[sampleIndex] = sample;
        sampleSum += sample; sampleIndex = (sampleIndex + 1) % 20;
        inputAverage = sampleSum / 20;
    }
    @Override public int receiveEnergy(ForgeDirection from, int amount, boolean simulate) {
        if (worldObj == null || worldObj.isRemote || amount <= 0 || !canConnectEnergy(from) || !dev.industrialtechnology.space.DimensionActivity.active(worldObj)) return 0;
        long tick = worldObj.getTotalWorldTime();
        int used = receiveTick == tick ? receivedThisTick : 0;
        int accepted = Math.max(0, Math.min(amount, Math.min(capacity() - energy, ModConfig.inputRate - used)));
        if (!simulate && accepted > 0) {
            if (receiveTick != tick) {
                // If the energy network ticks before us, finalize the old sample once here.
                if (lastRateTick != tick) {
                    addInputSample(receiveTick == tick - 1 ? receivedThisTick : 0);
                    lastRateTick = tick;
                }
                receiveTick = tick; receivedThisTick = 0;
            }
            receivedThisTick += accepted; energy += accepted; markDirty();
            // A full buffer stops accepting RF before the 20-tick meter can finish sampling startup.
            // Preserve the actual fill packet/aggregate rate, bounded by the input ceiling and working reserve.
            if (energy == capacity()) saturatedInput = Math.max(saturatedInput, Math.min(ModConfig.inputRate, Math.max(amount, receivedThisTick)));
        }
        return accepted;
    }
    public boolean consume(int amount) {
        if (amount < 0 || energy < amount) return false;
        energy -= amount; markDirty(); return true;
    }
    protected int workingReserve() { return 0; }
    /** A saturated receiver cannot measure unused supply. Retain the measured rate within one operation of full. */
    public int operatingInput() { return energy >= capacity() - workingReserve() ? Math.max(inputAverage, saturatedInput) : inputAverage; }
    @Override public int extractEnergy(ForgeDirection from, int amount, boolean simulate) { return 0; }
    @Override public boolean canConnectEnergy(ForgeDirection from) { return from != null && from != ForgeDirection.UNKNOWN; }
    @Override public int getEnergyStored(ForgeDirection from) { return energy; }
    @Override public int getMaxEnergyStored(ForgeDirection from) { return capacity(); }
    @Override public int getSizeInventory() { return output.slots.length; }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < output.slots.length ? output.slots[slot] : null; }
    @Override public ItemStack decrStackSize(int slot, int count) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null || count <= 0) return null;
        ItemStack result;
        if (count >= stack.stackSize) { result = stack; output.slots[slot] = null; }
        else result = stack.splitStack(count);
        markDirty(); return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) { return decrStackSize(slot, Integer.MAX_VALUE); }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= output.slots.length) return;
        if (stack != null && stack.stackSize > Math.min(getInventoryStackLimit(), stack.getMaxStackSize())) {
            ItemStack remainder = stack.copy();
            remainder.stackSize -= Math.min(getInventoryStackLimit(), stack.getMaxStackSize());
            stack = stack.copy(); stack.stackSize -= remainder.stackSize; output.capture(remainder);
        }
        output.slots[slot] = stack == null || stack.stackSize <= 0 ? null : stack; markDirty();
    }
    @Override public String getInventoryName() { return machineName(); }
    @Override public boolean hasCustomInventoryName() { return true; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return !isInvalid() && player.worldObj == worldObj && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }
    @Override public int[] getAccessibleSlotsFromSide(int side) { return accessible; }
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return false; }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) { return getStackInSlot(slot) != null; }


    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setInteger("Energy", energy); tag.setInteger("SaturatedInput", saturatedInput); output.write(tag);
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy = Math.max(0, Math.min(capacity(), tag.getInteger("Energy")));
        saturatedInput = Math.max(0, Math.min(ModConfig.inputRate, tag.getInteger("SaturatedInput")));
        output.read(tag); Arrays.fill(inputSamples, 0);
        inputAverage = sampleSum = receivedThisTick = sampleIndex = 0;
        receiveTick = lastRateTick = Long.MIN_VALUE;
    }
    @Override public NBTTagCompound snapshot() {
        NBTTagCompound n = super.snapshot();
        n.setInteger("Energy", energy); n.setInteger("Capacity", capacity());
        n.setInteger("Input", inputAverage); n.setInteger("OperatingInput", operatingInput()); return n;
    }
}
