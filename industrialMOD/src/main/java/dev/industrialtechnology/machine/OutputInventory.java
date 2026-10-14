package dev.industrialtechnology.machine;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Merge operations own their stacks. Overflow from ONE harvest is durable and never spawned. */
public final class OutputInventory {
    public final ItemStack[] slots;
    public final List<ItemStack> pending = new ArrayList<ItemStack>();
    public OutputInventory(int size) { slots = new ItemStack[size]; }
    public boolean hasSpace() {
        for (ItemStack stack : slots) if (stack == null || stack.stackSize < Math.min(64, stack.getMaxStackSize())) return true;
        return false;
    }
    /** Simulates the whole uncommitted harvest, including tags and partial compatible stacks. */
    public boolean canFit(List<ItemStack> drops) {
        if (drops.isEmpty()) return true;
        // Reuse immutable item/tag identities and simulate counts only; never clone 54 NBT trees per block.
        ItemStack[] types = slots.clone(); int[] counts = new int[slots.length];
        for (int i = 0; i < slots.length; i++) counts[i] = slots[i] == null ? 0 : slots[i].stackSize;
        for (ItemStack drop : drops) {
            int remaining = drop.stackSize;
            for (int pass = 0; pass < 2 && remaining > 0; pass++) for (int i = 0; i < types.length && remaining > 0; i++) {
                if (pass == 0 && types[i] != null && types[i].isItemEqual(drop) && ItemStack.areItemStackTagsEqual(types[i], drop)) {
                    int move = Math.min(remaining, Math.max(0, Math.min(64, types[i].getMaxStackSize()) - counts[i]));
                    counts[i] += move; remaining -= move;
                } else if (pass == 1 && types[i] == null) {
                    int move = Math.min(remaining, Math.min(64, drop.getMaxStackSize()));
                    types[i] = drop; counts[i] = move; remaining -= move;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }
    public boolean flush() {
        boolean changed = false;
        for (int i = 0; i < pending.size();) {
            ItemStack stack = pending.get(i);
            int before = stack.stackSize;
            insert(stack);
            changed |= stack.stackSize != before;
            if (stack.stackSize <= 0) pending.remove(i); else i++;
        }
        return changed;
    }
    private void insert(ItemStack stack) {
        for (int pass = 0; pass < 2 && stack.stackSize > 0; pass++) {
            for (int i = 0; i < slots.length && stack.stackSize > 0; i++) {
                ItemStack existing = slots[i];
                if (pass == 0 && existing != null && existing.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(existing, stack)) {
                    int move = Math.min(stack.stackSize, Math.min(64, existing.getMaxStackSize()) - existing.stackSize);
                    if (move > 0) { existing.stackSize += move; stack.stackSize -= move; }
                } else if (pass == 1 && existing == null) {
                    int move = Math.min(stack.stackSize, Math.min(64, stack.getMaxStackSize()));
                    slots[i] = stack.copy(); slots[i].stackSize = move; stack.stackSize -= move;
                }
            }
        }
    }
    public void capture(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return;
        int count = stack.stackSize;
        while (count > 0) {
            ItemStack copy = stack.copy(); copy.stackSize = Math.min(count, Math.max(1, Math.min(64, stack.getMaxStackSize())));
            pending.add(copy); count -= copy.stackSize;
        }
    }
    public void write(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < slots.length; i++) if (slots[i] != null) {
            NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("Slot", i);
            slots[i].writeToNBT(tag); list.appendTag(tag);
        }
        nbt.setTag("Items", list);
        list = new NBTTagList();
        for (ItemStack stack : pending) { NBTTagCompound tag = new NBTTagCompound(); stack.writeToNBT(tag); list.appendTag(tag); }
        nbt.setTag("PendingDrops", list);
    }
    public void read(NBTTagCompound nbt) {
        for (int i = 0; i < slots.length; i++) slots[i] = null;
        pending.clear();
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getInteger("Slot");
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
            if (slot >= 0 && slot < slots.length && stack != null && stack.stackSize > 0) {
                if (slots[slot] == null) slots[slot] = stack; else capture(stack);
            }
        }
        list = nbt.getTagList("PendingDrops", 10);
        for (int i = 0; i < list.tagCount(); i++) capture(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
    }
}
