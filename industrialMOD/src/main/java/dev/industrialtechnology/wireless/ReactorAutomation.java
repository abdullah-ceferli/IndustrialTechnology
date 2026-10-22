package dev.industrialtechnology.wireless;

import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

/** Local, bounded ZIRNOX feeder. No remote inventory sourcing and no fuel removal from the core. */
public final class ReactorAutomation {
    private static long budgetTick=Long.MIN_VALUE;private static int used;
    private static final int MAX_SOURCE_SLOTS=256;
    private static final class Supply {TileEntity tile,controller;IInventory inventory;int side;String message="NO ADJACENT SUPPLY";}
    public static String positionStatus(WirelessEndpoint e,HbmReactors.Access r,int slot,ReactorSettings draft){
        if(draft.items[slot].isEmpty())return "UNASSIGNED";
        if(!draft.enabled[slot])return "DISABLED";
        ItemStack actual=r.inventory.getStackInSlot(slot);
        if(actual!=null)return draft.matches(slot,actual)?"FILLED":"WRONG FUEL";
        if(!e.reactor.running)return "READY";
        return e.reactorPositions[slot];
    }
    private static Supply supply(WirelessEndpoint e,HbmReactors.Access access){
        Supply result=new Supply(),fallback=null;WorldServer world=(WorldServer)access.core.getWorldObj();
        int first=e.reactor.supply<0?0:e.reactor.supply,last=e.reactor.supply<0?5:e.reactor.supply;
        for(int side=first;side<=last;side++){
            ForgeDirection d=ForgeDirection.getOrientation(side);int x=e.x+d.offsetX,y=e.y+d.offsetY,z=e.z+d.offsetZ;
            if(!WirelessAccess.loaded(world,x,y,z))continue;TileEntity tile=world.getTileEntity(x,y,z);if(tile==null||tile==access.core||tile instanceof TileRedstoneRelay)continue;
            try{Object resolved=HbmMonitoring.resolve(tile);if(!(resolved instanceof IInventory)||resolved==access.core||!HbmMonitoring.allows(tile,"inventory"))continue;
                Supply candidate=new Supply();candidate.tile=tile;candidate.controller=resolved instanceof TileEntity?(TileEntity)resolved:tile;candidate.inventory=(IInventory)resolved;candidate.side=d.getOpposite().ordinal();candidate.message="READY";
                if(!ReactorPermissions.allow(e.owner,world,x,y,z,candidate.side)||candidate.controller!=tile&&!ReactorPermissions.allow(e.owner,world,candidate.controller.xCoord,candidate.controller.yCoord,candidate.controller.zCoord,candidate.side)){result.message="NO SUPPLY PERMISSION";continue;}
                if(e.reactor.supply>=0||hasStock(e,access,candidate))return candidate;
                if(fallback==null)fallback=candidate;
            }catch(ReflectiveOperationException|RuntimeException|LinkageError unavailable){result.message="UNSUPPORTED SUPPLY";}
        }return fallback==null?result:fallback;
    }
    private static boolean hasStock(WirelessEndpoint e,HbmReactors.Access access,Supply source){
        int[] slots=accessible(source.inventory,source.side);if(slots==null)return false;
        for(int index:slots){ItemStack stack=source.inventory.getStackInSlot(index);if(!HbmReactors.validFuel(stack)||source.inventory instanceof ISidedInventory&&!((ISidedInventory)source.inventory).canExtractItem(index,stack,source.side))continue;
            for(int slot=0;slot<24;slot++)if(e.reactor.enabled[slot]&&access.inventory.getStackInSlot(slot)==null&&e.reactor.matches(slot,stack))return true;
        }return false;
    }
    /** Read-only current source diagnostics, independent of transient command feedback. */
    public static void describeSupply(WirelessEndpoint e,HbmReactors.Access access,net.minecraft.nbt.NBTTagCompound n){
        Supply source=supply(e,access);n.setString("SupplyMessage",source.message);
        n.setInteger("AccessX",e.x);n.setInteger("AccessY",e.y);n.setInteger("AccessZ",e.z);
        if(source.inventory==null)return;
        n.setString("SupplyName",WirelessData.clean(source.inventory.getInventoryName(),64));
        n.setInteger("SupplyX",source.tile.xCoord);n.setInteger("SupplyY",source.tile.yCoord);n.setInteger("SupplyZ",source.tile.zCoord);
        boolean empty=false;for(int slot=0;slot<24;slot++)if(e.reactor.enabled[slot]&&!e.reactor.items[slot].isEmpty()&&access.inventory.getStackInSlot(slot)==null){empty=true;break;}
        n.setString("SupplyMessage",accessible(source.inventory,source.side)==null?"UNSUPPORTED SUPPLY":!empty?"NO EMPTY ASSIGNMENTS":hasStock(e,access,source)?"READY":"NO MATCHING FUEL");
    }
    public static void poll(WirelessData data,WirelessEndpoint e){
        if(!e.reactor.running)return;WorldServer world=DimensionManager.getWorld(e.dimension);if(world==null)return;
        long now=world.getTotalWorldTime();if(e.reactorNextPoll!=Long.MIN_VALUE&&now<e.reactorNextPoll)return;e.reactorNextPoll=now+WirelessConfig.reactorPollTicks;
        if(budgetTick!=DimensionManager.getWorld(0).getTotalWorldTime()){budgetTick=DimensionManager.getWorld(0).getTotalWorldTime();used=0;}
        try{
            HbmReactors.Access r=HbmReactors.access(e);
            if(!r.online()||r.type!=HbmReactors.ZIRNOX){e.reactorMessage=r.message;for(int i=0;i<24;i++)e.reactorPositions[i]=r.status;return;}
            if(!ReactorPermissions.allow(e.owner,world,e.x,e.y,e.z,e.side)||!ReactorPermissions.allow(e.owner,world,r.core.xCoord,r.core.yCoord,r.core.zCoord,e.side)){blocked(e,"NO PERMISSION");return;}
            Supply source=supply(e,r);e.reactorMessage=source.inventory==null?source.message:hasStock(e,r,source)?"Reading adjacent supply":"NO MATCHING FUEL in adjacent supply";
            if(source.inventory!=null&&(!ReactorPermissions.allow(e.owner,world,source.tile.xCoord,source.tile.yCoord,source.tile.zCoord,source.side)||source.controller!=source.tile&&!ReactorPermissions.allow(e.owner,world,source.controller.xCoord,source.controller.yCoord,source.controller.zCoord,source.side))){blocked(e,"NO SUPPLY PERMISSION");return;}
            int[] available=source.inventory==null?null:accessible(source.inventory,source.side);
            java.util.Map<String,java.util.List<Integer>> stock=new java.util.HashMap<String,java.util.List<Integer>>();
            if(available!=null)for(int index:available){ItemStack stack=source.inventory.getStackInSlot(index);if(stack==null)continue;String key=String.valueOf(net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem()))+"#"+stack.getItemDamage();java.util.List<Integer> indices=stock.get(key);if(indices==null){indices=new java.util.ArrayList<Integer>();stock.put(key,indices);}indices.add(index);}
            int count=0;
            for(int offset=0;offset<24;offset++){
                int slot=(e.reactorCursor+offset)%24;ReactorSettings settings=e.reactor;
                if(!settings.enabled[slot]||settings.items[slot].isEmpty())continue;
                ItemStack current=r.inventory.getStackInSlot(slot);if(current!=null){e.reactorPositions[slot]=settings.matches(slot,current)?"FILLED":"WRONG FUEL";continue;}
                if(!HbmReactors.validFuel(settings.icon(slot))){e.reactorPositions[slot]="UNSUPPORTED";continue;}
                if(source.inventory==null){e.reactorPositions[slot]="WAITING FOR FUEL";continue;}
                if(count>=WirelessConfig.reactorTransfers||used>=WirelessConfig.reactorWorkBudget){e.reactorPositions[slot]="READY";continue;}
                if(!r.inventory.isItemValidForSlot(slot,settings.icon(slot))||r.inventory instanceof ISidedInventory&&!((ISidedInventory)r.inventory).canInsertItem(slot,settings.icon(slot),e.side)){e.reactorPositions[slot]="BLOCKED";continue;}
                if(available==null){e.reactorPositions[slot]="UNSUPPORTED";continue;}
                java.util.List<Integer> slots=stock.get(settings.items[slot]+"#"+settings.metadata[slot]);boolean found=false;
                if(slots!=null)for(int index:slots){ItemStack stack=source.inventory.getStackInSlot(index);if(!settings.matches(slot,stack)||!HbmReactors.validFuel(stack))continue;found=true;used++;
                    if(moveOne(source.inventory,index,source.side,r.inventory,slot,e.side)){e.reactorPositions[slot]="FILLED";count++;e.reactorMessage="Inserted "+count+" real rod(s) from adjacent supply";data.markDirty();}
                    else e.reactorPositions[slot]="BLOCKED";break;
                }if(!found)e.reactorPositions[slot]="WAITING FOR FUEL";
            }e.reactorCursor=(e.reactorCursor+1)%24;
        }catch(RuntimeException|LinkageError unavailable){e.reactorMessage="DATA UNAVAILABLE: feeder paused";e.reactorNextPoll=now+200;}
    }
    private static void blocked(WirelessEndpoint e,String message){e.reactorMessage=message;java.util.Arrays.fill(e.reactorPositions,"BLOCKED");}
    private static int[] accessible(IInventory inventory,int face){
        int size=inventory.getSizeInventory();if(size<0||size>MAX_SOURCE_SLOTS)return null;
        int[] result;if(inventory instanceof ISidedInventory)result=((ISidedInventory)inventory).getAccessibleSlotsFromSide(face);else{result=new int[size];for(int i=0;i<size;i++)result[i]=i;}
        if(result==null||result.length>MAX_SOURCE_SLOTS)return null;for(int slot:result)if(slot<0||slot>=size)return null;return result;
    }
    /** Verified HBM destination setter is a normal slot assignment. Preserve the complete real fuel NBT. */
    public static boolean moveOne(IInventory source,int sourceSlot,int sourceFace,IInventory target,int targetSlot,int targetFace){
        if(source==target||sourceSlot<0||sourceSlot>=source.getSizeInventory()||targetSlot<0||targetSlot>=target.getSizeInventory())return false;
        ItemStack original=source.getStackInSlot(sourceSlot);if(original==null||original.stackSize<1||target.getStackInSlot(targetSlot)!=null)return false;
        if(source instanceof ISidedInventory&&!((ISidedInventory)source).canExtractItem(sourceSlot,original,sourceFace))return false;
        if(!target.isItemValidForSlot(targetSlot,original)||target instanceof ISidedInventory&&!((ISidedInventory)target).canInsertItem(targetSlot,original,targetFace))return false;
        if(target.getInventoryStackLimit()<1)return false;
        ItemStack before=original.copy(),one=original.copy();one.stackSize=1;
        ItemStack remainder=original.stackSize==1?null:original.copy();if(remainder!=null)remainder.stackSize--;
        // No callbacks/notifications between the two writes; work runs on the server thread.
        // Roll back contract-compliant inventories if a setter fails; never overwrite occupied fuel.
        try{
            if(!ItemStack.areItemStacksEqual(before,source.getStackInSlot(sourceSlot))||target.getStackInSlot(targetSlot)!=null)return false;
            source.setInventorySlotContents(sourceSlot,remainder);
            if(!ItemStack.areItemStacksEqual(remainder,source.getStackInSlot(sourceSlot)))throw new IllegalStateException("Supply did not accept extraction");
            target.setInventorySlotContents(targetSlot,one);
            if(!ItemStack.areItemStacksEqual(one,target.getStackInSlot(targetSlot)))throw new IllegalStateException("Target rejected the rod");
        }catch(RuntimeException|LinkageError failed){
            if(target.getStackInSlot(targetSlot)==one)target.setInventorySlotContents(targetSlot,null);
            if(ItemStack.areItemStacksEqual(remainder,source.getStackInSlot(sourceSlot)))source.setInventorySlotContents(sourceSlot,before);
            source.markDirty();target.markDirty();return false;
        }source.markDirty();target.markDirty();return true;
    }
    private ReactorAutomation(){}
}
