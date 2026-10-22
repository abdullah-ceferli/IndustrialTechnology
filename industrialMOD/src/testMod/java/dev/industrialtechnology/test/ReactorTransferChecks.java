package dev.industrialtechnology.test;
import java.util.function.BiConsumer;
import net.minecraft.inventory.*;import net.minecraft.item.*;import net.minecraft.init.Items;import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.wireless.ReactorAutomation;
/** Transfer failure contracts: real stack conservation, source extraction and destination restrictions. */
public final class ReactorTransferChecks {
    private static final class Sided extends InventoryBasic implements ISidedInventory {
        boolean extract=true,insert=true,reject;
        Sided(){super("Transfer contract",true,2);}
        public int[] getAccessibleSlotsFromSide(int side){return new int[]{0,1};}
        public boolean canInsertItem(int i,ItemStack s,int side){return insert;}
        public boolean canExtractItem(int i,ItemStack s,int side){return extract;}
        @Override public void setInventorySlotContents(int i,ItemStack stack){if(reject&&stack!=null)throw new IllegalStateException("Rejected insert");super.setInventorySlotContents(i,stack);}
    }
    private static void check(boolean yes){if(!yes)throw new AssertionError("Transfer violated inventory contract");}
    public static void run(BiConsumer<String,Runnable> test){
        test.accept("Reactor transfers preserve source extraction restrictions",()->{Sided s=new Sided(),d=new Sided();s.setInventorySlotContents(0,new ItemStack(Items.stick,3));s.extract=false;check(!ReactorAutomation.moveOne(s,0,0,d,0,0)&&s.getStackInSlot(0).stackSize==3&&d.getStackInSlot(0)==null);});
        test.accept("Reactor transfers preserve target insertion restrictions",()->{Sided s=new Sided(),d=new Sided();s.setInventorySlotContents(0,new ItemStack(Items.stick,3));d.insert=false;check(!ReactorAutomation.moveOne(s,0,0,d,0,0)&&s.getStackInSlot(0).stackSize==3&&d.getStackInSlot(0)==null);});
        test.accept("Reactor transfer rejects occupied targets and same inventories",()->{Sided s=new Sided(),d=new Sided();s.setInventorySlotContents(0,new ItemStack(Items.stick,3));d.setInventorySlotContents(0,new ItemStack(Items.coal));check(!ReactorAutomation.moveOne(s,0,0,d,0,0)&&!ReactorAutomation.moveOne(s,0,0,s,1,0)&&s.getStackInSlot(0).stackSize==3&&d.getStackInSlot(0).getItem()==Items.coal);});
        test.accept("Reactor one-item transfer conserves a stacked source and copies its complete NBT",()->{Sided s=new Sided(),d=new Sided();ItemStack stack=new ItemStack(Items.stick,3);stack.setTagCompound(new NBTTagCompound());stack.getTagCompound().setString("Marker","preserved");s.setInventorySlotContents(0,stack);check(ReactorAutomation.moveOne(s,0,0,d,0,0)&&s.getStackInSlot(0).stackSize==2&&d.getStackInSlot(0).stackSize==1&&d.getStackInSlot(0).getTagCompound().getString("Marker").equals("preserved"));});
        test.accept("Rejected target insertion restores the exact original source without duplication",()->{Sided s=new Sided(),d=new Sided();ItemStack before=new ItemStack(Items.stick,3);before.setStackDisplayName("Retained source");s.setInventorySlotContents(0,before.copy());d.reject=true;check(!ReactorAutomation.moveOne(s,0,0,d,0,0)&&ItemStack.areItemStacksEqual(before,s.getStackInSlot(0))&&d.getStackInSlot(0)==null);});
    }
    private ReactorTransferChecks(){}
}
