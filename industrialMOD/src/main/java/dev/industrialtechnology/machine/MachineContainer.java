package dev.industrialtechnology.machine;

import java.util.List;
import java.util.ArrayList;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.network.SnapshotPacket;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.quarry.TileQuarry;
import dev.industrialtechnology.quarry.VoidFilter;
import net.minecraftforge.common.ForgeHooks;

public final class MachineContainer extends Container {
    public final TileMachine machine;
    private final TilePoweredMachine inventory;
    public int tab, filterPage;
    private long lastActionTick = Long.MIN_VALUE;
    private int actionsThisTick;
    private NBTTagCompound lastSnapshot;
    private int syncTicks;
    public MachineContainer(InventoryPlayer player, TileMachine machine) {
        this.machine = machine;
        inventory = machine instanceof TilePoweredMachine ? (TilePoweredMachine) machine : null;
        if (inventory == null) return; // Utility loader: controls only, no machine/player inventory slots.
        for (int i = 0; i < inventory.getSizeInventory(); i++) addSlotToContainer(new OutputSlot(inventory, i, 78 + (i % 9) * 18, 33 + (i / 9) * 18, this));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(player, 9 + row * 9 + col, 78 + col * 18, 151 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player, col, 78 + col * 18, 209));
        if (machine instanceof TileQuarry) for (int i = 0; i < ((TileQuarry) machine).filter.getSizeInventory(); i++)
            addSlotToContainer(new GhostSlot(((TileQuarry) machine).filter, i, 78 + (i % VoidFilter.PAGE_SIZE) % 9 * 18, 73 + (i % VoidFilter.PAGE_SIZE) / 9 * 18));
        setTab(0);
    }
    public void setTab(int value) {
        tab = machine.isQuarry() && value >= 0 && value <= 2 ? value : 0;
        for (int i = 0; i < machineSlots(); i++) {
            Slot slot = (Slot) inventorySlots.get(i);
            slot.yDisplayPosition = tab == 1 ? 33 + (i / 9) * 18 : -10000;
        }
        for (int i = machineSlots() + 36; i < inventorySlots.size(); i++) {
            int index=i-machineSlots()-36;
            ((Slot) inventorySlots.get(i)).yDisplayPosition = tab == 2 && index / VoidFilter.PAGE_SIZE == filterPage
                ? 73 + (index % VoidFilter.PAGE_SIZE) / 9 * 18 : -10000;
        }
    }
    public int filterPages() { return machine instanceof TileQuarry ? (((TileQuarry) machine).filter.getSizeInventory()+24)/25 : 1; }
    public void setFilterPage(int page) { if(page>=0 && page<filterPages()){filterPage=page;setTab(tab);} }
    private int machineSlots() { return inventory == null ? 0 : inventory.getSizeInventory(); }
    public boolean allowAction() {
        long tick = machine.getWorldObj().getTotalWorldTime();
        if (tick != lastActionTick) { lastActionTick = tick; actionsThisTick = 0; }
        return ++actionsThisTick <= 8;
    }
    @Override public boolean canInteractWith(EntityPlayer player) { return machine.isUseableByPlayer(player); }
    @Override public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        if (crafter instanceof EntityPlayerMP) send((EntityPlayerMP) crafter);
    }
    public void send(EntityPlayerMP player) {
        IndustrialTechnology.network.sendTo(new SnapshotPacket(machine, windowId, tab, filterPage), player);
    }
    @Override public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (machine.getWorldObj().isRemote || ++syncTicks < 5) return;
        syncTicks = 0;
        NBTTagCompound state = machine.snapshot(); state.setInteger("Tab", tab); state.setInteger("FilterPage", filterPage);
        if (state.equals(lastSnapshot)) return;
        lastSnapshot = state;
        for (Object listener : crafters) if (listener instanceof EntityPlayerMP) send((EntityPlayerMP) listener);
    }
    @Override public ItemStack slotClick(int slot, int button, int mode, EntityPlayer player) {
        int ghosts = machineSlots() + 36;
        if (machine instanceof TileQuarry && slot >= ghosts && slot < ghosts + ((TileQuarry) machine).filter.getSizeInventory()) {
            if (tab != 2 || (slot-ghosts)/VoidFilter.PAGE_SIZE != filterPage || mode != 0 || button < 0 || button > 1 || !canInteractWith(player)
                || !ForgeHooks.canInteractWith(player, this) || !allowAction()) return null;
            VoidFilter filter = ((TileQuarry) machine).filter;
            // Use the authoritative carried stack. No item/NBT data is accepted in a custom packet.
            filter.setInventorySlotContents(slot - ghosts, button == 1 ? null : player.inventory.getItemStack());
            return null;
        }
        // Hidden output slots cannot be clicked through forged vanilla inventory packets.
        if (slot >= 0 && slot < machineSlots() && tab != 1) return null;
        return super.slotClick(slot, button, mode, player);
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= machineSlots() + 36 || index >= inventorySlots.size()) return null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (!slot.getHasStack() || !slot.canTakeStack(player)) return null;
        ItemStack stack = slot.getStack(), before = stack.copy();
        int machineSize = machineSlots();
        if (index < machineSize) {
            if (!mergeItemStack(stack, machineSize, machineSize + 36, true)) return null;
        } else if (index < machineSize + 27) {
            if (!mergeItemStack(stack, machineSize + 27, machineSize + 36, false)) return null;
        } else if (!mergeItemStack(stack, machineSize, machineSize + 27, false)) return null;
        if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
        if (before.stackSize == stack.stackSize) return null;
        slot.onPickupFromSlot(player, stack); return before;
    }
    private static final class OutputSlot extends Slot {
        private final MachineContainer container;
        OutputSlot(TilePoweredMachine machine, int slot, int x, int y, MachineContainer container) { super(machine, slot, x, y); this.container = container; }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
        @Override public boolean canTakeStack(EntityPlayer player) { return container.tab == 1; }
    }
    private static final class GhostSlot extends Slot {
        GhostSlot(VoidFilter filter, int slot, int x, int y) { super(filter, slot, x, y); }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
        @Override public boolean canTakeStack(EntityPlayer player) { return false; }
        @Override public int getSlotStackLimit() { return 1; }
    }
}
