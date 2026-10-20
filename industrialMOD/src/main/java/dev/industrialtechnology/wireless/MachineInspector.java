package dev.industrialtechnology.wireless;
import java.util.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.*;
import net.minecraftforge.common.util.ForgeDirection;
/** Reads real server inventory/tank APIs. No insertion, extraction, fill, or drain calls. */
public final class MachineInspector {
    public static final int MAX_SLOTS=4096,MAX_TANKS=64;
    public static final class Tank {
        public final String name,unit;public String identifier="";public String localizationKey="";public final long amount,capacity;public final int color;
        public Tank(String name,long amount,long capacity,String unit,int color){this.name=WirelessData.clean(name,64);this.amount=Math.max(0,amount);this.capacity=Math.max(0,capacity);this.unit=unit;this.color=color;}
    }
    public static final class InventoryView {
        public final IInventory inventory;public final int side;public final int[] slots;public final ItemStack[] contents;
        public final boolean complete;public int occupied;
        InventoryView(IInventory inventory,int side){
            this.inventory=inventory;this.side=side;int size=Math.max(0,inventory.getSizeInventory());
            int[] available;if(inventory instanceof ISidedInventory)available=((ISidedInventory)inventory).getAccessibleSlotsFromSide(side);
            else {available=new int[Math.min(MAX_SLOTS,size)];for(int i=0;i<available.length;i++)available[i]=i;}
            if(available==null)available=new int[0];LinkedHashSet<Integer> ids=new LinkedHashSet<Integer>();
            for(int slot:available){if(slot>=0&&slot<size&&ids.size()<MAX_SLOTS)ids.add(slot);}
            complete=available.length<=MAX_SLOTS && size<=MAX_SLOTS;slots=new int[ids.size()];contents=new ItemStack[ids.size()];int i=0;
            for(int slot:ids){slots[i]=slot;contents[i]=inventory.getStackInSlot(slot);if(contents[i]!=null&&contents[i].stackSize>0)occupied++;i++;}
        }
        public long amount(ItemStack item){long n=0;for(ItemStack stack:contents)if(same(stack,item))n+=Math.max(0,stack.stackSize);return n;}
        public boolean insertable(int visible,ItemStack item){int slot=slots[visible];return inventory.isItemValidForSlot(slot,item)&&(!(inventory instanceof ISidedInventory)||((ISidedInventory)inventory).canInsertItem(slot,item,side));}
        public boolean extractable(int visible,ItemStack item){return !(inventory instanceof ISidedInventory)||((ISidedInventory)inventory).canExtractItem(slots[visible],item,side);}
        public boolean full(ItemStack filter){
            if(slots.length==0)return false;
            for(int i=0;i<slots.length;i++){
                ItemStack stack=contents[i],probe=filter==null?stack:filter;
                // With no item filter, an empty opaque slot is conservatively considered available.
                if(probe==null)return false;
                if(insertable(i,probe)&&(stack==null||same(stack,probe)&&stack.stackSize<Math.min(inventory.getInventoryStackLimit(),probe.getMaxStackSize())))return false;
            }return true;
        }
    }
    public static final class Reading {
        public WirelessAccess.Status status=WirelessAccess.Status.ONLINE;
        public boolean fluidAvailable;public String fluidMessage="FLUID DATA UNAVAILABLE";
        public InventoryView inventory;public final List<Tank> tanks=new ArrayList<Tank>();public String message="",adapter="Forge inventory/fluid";
    }
    public static boolean same(ItemStack a,ItemStack b){return a!=null&&b!=null&&a.getItem()==b.getItem()&&a.getItemDamage()==b.getItemDamage()&&ItemStack.areItemStackTagsEqual(a,b);}
    public static Reading inspect(WirelessEndpoint e,int dimension){return inspect(e,dimension,true);}
    public static Reading inspect(WirelessEndpoint e,int dimension,boolean fluids){
        Reading result=new Reading();result.status=WirelessAccess.status(e,dimension);if(result.status!=WirelessAccess.Status.ONLINE)return result;
        try{
            TileEntity tile=WirelessAccess.tile(e);if(tile==null){result.status=WirelessAccess.Status.UNSUPPORTED;return result;}
            Object source=HbmMonitoring.resolve(tile);
            if(source==null){result.status=WirelessAccess.Status.DATA_UNAVAILABLE;result.message="Controller unavailable or not loaded";return result;}
            if(source instanceof IInventory&&HbmMonitoring.allows(tile,"inventory")){InventoryView view=new InventoryView((IInventory)source,e.side);if(view.slots.length>0||!view.complete)result.inventory=view;}
            try{if(fluids&&HbmMonitoring.allows(tile,"fluid")&&source instanceof IFluidHandler){
                FluidTankInfo[] tanks=((IFluidHandler)source).getTankInfo(ForgeDirection.getOrientation(e.side));
                if(tanks!=null&&tanks.length>MAX_TANKS)throw new IllegalStateException("Too many tanks");
                if(tanks!=null)for(int i=0;i<tanks.length;i++){
                    FluidTankInfo tank=tanks[i];if(tank==null||tank.capacity<0)throw new IllegalStateException("Tank data missing");FluidStack fluid=tank.fluid;
                    result.tanks.add(new Tank(fluid==null?"Empty":fluid.getLocalizedName(),fluid==null?0:fluid.amount,tank.capacity,"mB",fluid==null?0x71838a:fluid.getFluid().getColor(fluid)));
                    result.tanks.get(result.tanks.size()-1).identifier=fluid==null?"":FluidRegistry.getFluidName(fluid.getFluid());
                }
            }
            if(fluids&&HbmMonitoring.allows(tile,"fluid")&&result.tanks.isEmpty()&&HbmMonitoring.readTanks(source,result.tanks))result.adapter="HBM X5808 / Forge inventory";
            result.fluidAvailable=fluids&&!result.tanks.isEmpty();
            for(Tank tank:result.tanks)if(tank.amount>0&&(tank.identifier==null||tank.identifier.isEmpty()))result.fluidAvailable=false;
            }catch(ReflectiveOperationException|RuntimeException|LinkageError badFluidAPI){result.tanks.clear();result.fluidAvailable=false;}
            if(source!=tile)result.adapter="HBM X5808 multiblock port";
            if(result.inventory==null&&result.tanks.isEmpty())result.status=WirelessAccess.Status.UNSUPPORTED;
            if(result.inventory!=null&&!result.inventory.complete){result.status=WirelessAccess.Status.DATA_UNAVAILABLE;result.message="Inventory exceeds safe scan limit";}
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){result.status=WirelessAccess.Status.DATA_UNAVAILABLE;result.message="Machine API unavailable for this version";result.inventory=null;result.tanks.clear();}
        return result;
    }
    private MachineInspector(){}
}
