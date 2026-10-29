package dev.industrialtechnology.steel;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
public final class SteelContainer extends Container {
    public static final int MACHINE_SLOTS=5,PLAYER_START=5,HOTBAR_START=32,END=41;
    public static final int[] INVENTORY={0,2,4,8,9};
    public static final int[][] POS={{44,54},{44,18},{99,36},{174,76},{174,96}};
    public final TileSteelFactory factory;
    public int shownPower,shownProgress,shownStatus,shownTicks=100,shownHE=500,shownCoal=4;
    private final int[] previous={-1,-1,-1,-1,-1,-1,-1};
    public SteelContainer(InventoryPlayer inv,TileSteelFactory t){factory=t;for(int i=0;i<MACHINE_SLOTS;i++){final int index=INVENTORY[i];addSlotToContainer(new Slot(t,index,POS[i][0],POS[i][1]){
        @Override public boolean isItemValid(ItemStack s){return factory.isItemValidForSlot(index,s);}
        @Override public int getSlotStackLimit(){return index>=8?1:64;}
    });}for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlotToContainer(new Slot(inv,c+r*9+9,21+c*18,115+r*18));for(int c=0;c<9;c++)addSlotToContainer(new Slot(inv,c,21+c*18,173));}
    public static final String[] statuses={"Producing steel","Need 16 iron ingots","Need coal dust","Steel output full","Need HBM HE power","HBM required"};
    private int status(){String s=factory.status();for(int i=0;i<statuses.length;i++)if(statuses[i].equals(s))return i;return 5;}
    private int[] values(){int p=(int)factory.getPower();SteelUpgradeProfile u=factory.recipeProfile();return new int[]{p&65535,(p>>>16)&65535,factory.progress,status(),u.ticks,u.hePerTick,u.coal};}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();int[] values=values();for(Object o:crafters){ICrafting c=(ICrafting)o;for(int i=0;i<values.length;i++)if(values[i]!=previous[i])c.sendProgressBarUpdate(this,i,values[i]);}System.arraycopy(values,0,previous,0,values.length);}
    @Override public void addCraftingToCrafters(ICrafting c){super.addCraftingToCrafters(c);int[] values=values();for(int i=0;i<values.length;i++)c.sendProgressBarUpdate(this,i,values[i]);}
    @Override public void updateProgressBar(int id,int value){int v=value&65535;if(id==0)shownPower=(shownPower&0xffff0000)|v;else if(id==1)shownPower=(shownPower&65535)|(v<<16);else if(id==2)shownProgress=v;else if(id==3)shownStatus=Math.min(5,v);else if(id==4)shownTicks=Math.max(5,Math.min(130,v));else if(id==5)shownHE=Math.max(50,Math.min(14000,v));else if(id==6)shownCoal=Math.max(1,Math.min(4,v));}
    @Override public boolean canInteractWith(EntityPlayer p){return factory.isUseableByPlayer(p);}
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int i){if(i<0||i>=inventorySlots.size())return null;Slot slot=(Slot)inventorySlots.get(i);if(!slot.getHasStack())return null;ItemStack s=slot.getStack(),copy=s.copy();boolean moved;if(i<MACHINE_SLOTS)moved=mergeItemStack(s,PLAYER_START,END,true);else if(SteelRegistry.upgrade(s)!=0)moved=mergeItemStack(s,3,5,false);else if(TileSteelFactory.iron(s))moved=mergeItemStack(s,0,1,false);else if(TileSteelFactory.coal(s))moved=mergeItemStack(s,1,2,false);else moved=i<HOTBAR_START?mergeItemStack(s,HOTBAR_START,END,false):mergeItemStack(s,PLAYER_START,HOTBAR_START,false);if(!moved)return null;if(s.stackSize<=0)slot.putStack(null);else slot.onSlotChanged();slot.onPickupFromSlot(p,s);return copy;}
}
