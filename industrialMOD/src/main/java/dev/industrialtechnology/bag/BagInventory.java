package dev.industrialtechnology.bag;

import java.util.UUID;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;

public final class BagInventory implements IInventory {
    public static final int SIZE=104;
    public final UUID owner;
    public final int color;
    private final BagData data;
    private final ItemStack[] items=new ItemStack[SIZE];
    public BagInventory(UUID owner,int color,BagData data){this.owner=owner;this.color=color;this.data=data;}
    @Override public int getSizeInventory(){return SIZE;}
    @Override public ItemStack getStackInSlot(int i){return i>=0&&i<SIZE?items[i]:null;}
    @Override public ItemStack decrStackSize(int i,int amount) {
        ItemStack stack=getStackInSlot(i);if(stack==null||amount<=0)return null;
        ItemStack result;
        if(amount>=stack.stackSize){result=stack;items[i]=null;}else result=stack.splitStack(amount);
        markDirty();return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int i){ItemStack s=getStackInSlot(i);return s==null?null:decrStackSize(i,s.stackSize);}
    @Override public void setInventorySlotContents(int i,ItemStack stack) {
        if(i<0||i>=SIZE||stack!=null&&!isItemValidForSlot(i,stack))return;
        if(stack!=null){if(stack.stackSize<=0)stack=null;else stack.stackSize=Math.min(stack.stackSize,Math.min(64,stack.getMaxStackSize()));}
        items[i]=stack;markDirty();
    }
    @Override public String getInventoryName(){return "container.industrialtech.storage_bag";}
    @Override public boolean hasCustomInventoryName(){return false;}
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public void markDirty(){if(data!=null)data.markDirty();}
    @Override public boolean isUseableByPlayer(EntityPlayer p){return p!=null&&owner.equals(p.getUniqueID());}
    @Override public void openInventory(){}
    @Override public void closeInventory(){markDirty();}
    @Override public boolean isItemValidForSlot(int i,ItemStack s){return i>=0&&i<SIZE&&s!=null&&s.getItem()!=BagRegistry.bag;}
    public NBTTagList write() {
        NBTTagList list=new NBTTagList();
        for(int i=0;i<SIZE;i++)if(items[i]!=null){NBTTagCompound n=new NBTTagCompound();n.setShort("Slot",(short)i);items[i].writeToNBT(n);list.appendTag(n);}
        return list;
    }
    public void read(NBTTagList list) {
        java.util.Arrays.fill(items,null);
        for(int i=0;i<list.tagCount();i++){NBTTagCompound n=list.getCompoundTagAt(i);int slot=n.getShort("Slot");ItemStack s=ItemStack.loadItemStackFromNBT(n);
            if(slot>=0&&slot<SIZE&&s!=null&&s.stackSize>0&&isItemValidForSlot(slot,s)){s.stackSize=Math.min(s.stackSize,Math.min(64,s.getMaxStackSize()));items[slot]=s;}}
    }
}
