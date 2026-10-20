package dev.industrialtechnology.quarry;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Tier-sized configuration icons. Never stores or consumes real player items. */
public final class VoidFilter implements IInventory {
    public static final int SIZE = 50, PAGE_SIZE = 25, MAX_ENTRY_BYTES = 1024;
    public static int capacity(int tier) { return new int[]{18,36,50}[Math.max(0,Math.min(2,tier))]; }
    private final TileQuarry owner;
    private final ItemStack[] entries = new ItemStack[SIZE];
    public boolean enabled, ignoreMetadata;
    public VoidFilter(TileQuarry owner) { this.owner = owner; }
    public boolean matches(ItemStack stack) {
        if (!enabled || stack == null) return false;
        for (ItemStack entry : entries) if (entry != null && entry.getItem() == stack.getItem()
            && (ignoreMetadata || entry.getItemDamage() == stack.getItemDamage())
            && ItemStack.areItemStackTagsEqual(entry, stack)) return true;
        return false;
    }
    public int configured() { int n = 0; for (ItemStack entry : entries) if (entry != null) n++; return n; }
    public void clear() { for (int i = 0; i < getSizeInventory(); i++) entries[i] = null; markDirty(); }
    public static boolean valid(ItemStack stack) {
        if (stack == null) return true;
        if (stack.getItem() == null || Item.itemRegistry.getNameForObject(stack.getItem()) == null
            || stack.getItemDamage() < 0 || stack.getItemDamage() > 32767) return false;
        try {
            final int[] written = {0};
            DataOutputStream out = new DataOutputStream(new OutputStream() {
                @Override public void write(int value) throws IOException { if (++written[0] > MAX_ENTRY_BYTES) throw new IOException("Oversized filter icon"); }
                @Override public void write(byte[] data, int offset, int length) throws IOException {
                    written[0] += length; if (written[0] > MAX_ENTRY_BYTES) throw new IOException("Oversized filter icon");
                }
            });
            NBTTagCompound tag = new NBTTagCompound(); stack.writeToNBT(tag); CompressedStreamTools.write(tag, out);
            return true;
        } catch (IOException | RuntimeException error) { return false; }
    }
    public void write(NBTTagCompound tag) {
        tag.setBoolean("VoidEnabled", enabled); tag.setBoolean("VoidIgnoreMetadata", ignoreMetadata);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getSizeInventory(); i++) if (entries[i] != null) {
            NBTTagCompound item = new NBTTagCompound(); entries[i].writeToNBT(item); item.setInteger("FilterSlot", i); list.appendTag(item);
        }
        tag.setTag("VoidEntries", list);
    }
    public void read(NBTTagCompound tag) {
        enabled = tag.getBoolean("VoidEnabled"); ignoreMetadata = tag.getBoolean("VoidIgnoreMetadata");
        for (int i = 0; i < getSizeInventory(); i++) entries[i] = null;
        NBTTagList list = tag.getTagList("VoidEntries", 10);
        for (int i = 0; i < Math.min(SIZE, list.tagCount()); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i); int slot = item.getInteger("FilterSlot");
            if (slot < 0 || slot >= getSizeInventory()) continue;
            ItemStack stack = ItemStack.loadItemStackFromNBT(item);
            if (stack != null && valid(stack)) { stack.stackSize = 1; entries[slot] = stack; }
        }
    }
    @Override public int getSizeInventory() { return capacity(owner.tier()); }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < getSizeInventory() ? entries[slot] : null; }
    @Override public ItemStack decrStackSize(int slot, int count) { return null; }
    @Override public ItemStack getStackInSlotOnClosing(int slot) { return null; }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getSizeInventory() || !valid(stack)) return;
        entries[slot] = stack == null ? null : stack.copy(); if (entries[slot] != null) entries[slot].stackSize = 1; markDirty();
    }
    @Override public String getInventoryName() { return "Void Filter"; }
    @Override public boolean hasCustomInventoryName() { return true; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public void markDirty() { owner.markDirty(); }
    @Override public boolean isUseableByPlayer(EntityPlayer player) { return owner.isUseableByPlayer(player); }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }
}
