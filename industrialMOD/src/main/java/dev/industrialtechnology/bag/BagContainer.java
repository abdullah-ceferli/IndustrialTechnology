package dev.industrialtechnology.bag;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

/** Vanilla inventory transactions with the opening hotbar slot protected on the server. */
public final class BagContainer extends Container {
    public static final int STORAGE=104,PLAYER_START=104,HOTBAR_START=131,END=140;
    public final BagInventory bag;
    public final int heldSlot,lockedSlot,color;
    private final EntityPlayer owner;
    public BagContainer(EntityPlayer player,BagInventory inventory) {
        owner=player;bag=inventory;color=inventory.color;heldSlot=player.inventory.currentItem;lockedSlot=HOTBAR_START+heldSlot;
        for(int row=0;row<8;row++)for(int col=0;col<13;col++)addSlotToContainer(new Slot(bag,col+row*13,12+col*18,5+row*18){@Override public boolean isItemValid(ItemStack s){return bag.isItemValidForSlot(getSlotIndex(),s);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player.inventory,col+row*9+9,48+col*18,152+row*18));
        for(int col=0;col<9;col++){
            if(col==heldSlot)addSlotToContainer(new Slot(player.inventory,col,48+col*18,210){@Override public boolean canTakeStack(EntityPlayer p){return false;}@Override public boolean isItemValid(ItemStack s){return false;}});
            else addSlotToContainer(new Slot(player.inventory,col,48+col*18,210));
        }
        bag.openInventory();
    }
    public static BagContainer open(EntityPlayer p,boolean client) {
        ItemStack s=p.getHeldItem();if(!BagRegistry.valid(s))return null;int color=BagRegistry.color(s);
        return new BagContainer(p,client?new BagInventory(p.getUniqueID(),color,null):BagData.get(p.worldObj).inventory(p.getUniqueID(),color));
    }
    @Override public boolean canInteractWith(EntityPlayer p){return p==owner&&!p.isDead&&p.inventory.currentItem==heldSlot&&BagRegistry.valid(p.getHeldItem())&&BagRegistry.color(p.getHeldItem())==color&&bag.isUseableByPlayer(p);}
    @Override public ItemStack slotClick(int slot,int button,int mode,EntityPlayer p){if(!canInteractWith(p)||slot==lockedSlot||mode==2&&button==heldSlot)return null;return super.slotClick(slot,button,mode,p);}
    @Override public boolean canDragIntoSlot(Slot s){return s.slotNumber!=lockedSlot&&super.canDragIntoSlot(s);}
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int index) {
        if(!canInteractWith(p)||index<0||index>=END||index==lockedSlot)return null;
        Slot slot=(Slot)inventorySlots.get(index);if(!slot.getHasStack()||!slot.canTakeStack(p))return null;
        ItemStack s=slot.getStack(),copy=s.copy();
        if(index<STORAGE){if(!mergeItemStack(s,PLAYER_START,END,true))return null;}
        else {if(s.getItem()==BagRegistry.bag||!mergeItemStack(s,0,STORAGE,false))return null;}
        if(s.stackSize<=0)slot.putStack(null);else slot.onSlotChanged();slot.onPickupFromSlot(p,s);return copy;
    }
    @Override public void onContainerClosed(EntityPlayer p){super.onContainerClosed(p);bag.closeInventory();}
}
