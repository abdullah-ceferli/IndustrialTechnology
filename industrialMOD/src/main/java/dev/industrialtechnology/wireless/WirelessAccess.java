package dev.industrialtechnology.wireless;
import java.util.UUID;
import dev.industrialtechnology.core.ModRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
public final class WirelessAccess {
    public enum Status { ONLINE, OFFLINE, OTHER_DIMENSION, DISCONNECTED, DISABLED, UNSUPPORTED, DATA_UNAVAILABLE }
    public static UUID terminal(EntityPlayer player,ItemStack stack,boolean create){
        if(stack==null||stack.getItem()!=ModRegistry.terminal||player.worldObj.isRemote)return null;
        WirelessData data=WirelessData.get(player.worldObj);NBTTagCompound n=stack.getTagCompound();UUID id=n==null?null:WirelessData.uuid(n.getString("WirelessNetwork"));
        if(id!=null)return data.owns(id,player.getUniqueID())?id:null;
        if(!create || n!=null&&n.hasKey("WirelessNetwork"))return null;
        id=data.create(player.getUniqueID());if(id==null)return null;if(n==null){n=new NBTTagCompound();stack.setTagCompound(n);}n.setString("WirelessNetwork",id.toString());n.setString("WirelessOwner",player.getUniqueID().toString());player.inventory.markDirty();return id;
    }
    public static UUID active(EntityPlayer player){
        UUID selected=WirelessData.uuid(player.getEntityData().getString("IndustrialTerminal")),first=null;
        for(ItemStack stack:player.inventory.mainInventory){if(stack==null||stack.getItem()!=ModRegistry.terminal)continue;UUID id=terminal(player,stack,true);if(id==null)continue;if(id.equals(selected))return id;if(first==null)first=id;}
        return first;
    }
    public static boolean loaded(net.minecraft.world.World world,int x,int y,int z){return world!=null&&y>=0&&y<world.getHeight()&&world.getChunkProvider().chunkExists(x>>4,z>>4);}
    public static TileEntity tile(WirelessEndpoint e){WorldServer w=DimensionManager.getWorld(e.dimension);return loaded(w,e.x,e.y,e.z)?w.getTileEntity(e.x,e.y,e.z):null;}
    public static TileRedstoneRelay relay(WirelessEndpoint e){
        WorldServer w=DimensionManager.getWorld(e.dimension);ForgeDirection d=ForgeDirection.getOrientation(e.side);int x=e.x+d.offsetX,y=e.y+d.offsetY,z=e.z+d.offsetZ;
        if(!loaded(w,x,y,z))return null;TileEntity tile=w.getTileEntity(x,y,z);
        return tile instanceof TileRedstoneRelay&&e.id.equals(((TileRedstoneRelay)tile).endpoint)?(TileRedstoneRelay)tile:null;
    }
    public static Status status(WirelessEndpoint e,int dimension){
        if(e.dimension!=dimension)return Status.OTHER_DIMENSION;WorldServer w=DimensionManager.getWorld(e.dimension);
        if(!loaded(w,e.x,e.y,e.z))return Status.OFFLINE;
        if(e.kind!=WirelessEndpoint.Kind.CONTROL&&!e.block.equals(net.minecraft.block.Block.blockRegistry.getNameForObject(w.getBlock(e.x,e.y,e.z))))return Status.DISCONNECTED;
        if(e.kind==WirelessEndpoint.Kind.CONTROL&&w.isAirBlock(e.x,e.y,e.z))return Status.DISCONNECTED;
        if(e.hasPhysicalStick()){ForgeDirection d=ForgeDirection.getOrientation(e.side);if(!loaded(w,e.x+d.offsetX,e.y+d.offsetY,e.z+d.offsetZ))return Status.OFFLINE;if(relay(e)==null)return Status.DISCONNECTED;}
        if(e.kind!=WirelessEndpoint.Kind.CONTROL&&e.tileToken!=null){TileEntity t=w.getTileEntity(e.x,e.y,e.z);if(!TargetAnchors.matches(t,e.tileToken))return Status.DISCONNECTED;}
        return e.enabled?Status.ONLINE:Status.DISABLED;
    }
    public static boolean signal(WirelessEndpoint e,long time){return e.enabled&&e.powered&&(e.pulseEnd==0||time<e.pulseEnd);}
    public static boolean activate(WirelessData data,WirelessEndpoint e){
        if(e.kind!=WirelessEndpoint.Kind.CONTROL||status(e,e.dimension)!=Status.ONLINE)return false;
        WorldServer w=DimensionManager.getWorld(e.dimension);long now=w.getTotalWorldTime();
        if(e.pulseMode&&signal(e,now))return false; // Repeated active pulses are ignored, never extended.
        e.powered=true;e.pulseEnd=e.pulseMode?now+20L*e.pulseSeconds:0;data.markDirty();TileRedstoneRelay relay=relay(e);if(relay!=null)relay.refresh();return true;
    }
    public static void off(WirelessData data,WirelessEndpoint e){e.powered=false;e.pulseEnd=0;data.markDirty();TileRedstoneRelay relay=relay(e);if(relay!=null)relay.refresh();}
    public static boolean command(WirelessData data,WirelessEndpoint e,String action,int seconds){
        if(e==null||e.kind!=WirelessEndpoint.Kind.CONTROL||status(e,e.dimension)!=Status.ONLINE)return false;
        if("OFF".equals(action)){off(data,e);return true;}
        long now=DimensionManager.getWorld(e.dimension).getTotalWorldTime();
        if("PULSE".equals(action)){int duration=seconds==0?e.pulseSeconds:seconds;if(duration<5||duration>10)return false;
            if(e.powered&&e.pulseEnd>now)return false;e.powered=true;e.pulseEnd=now+20L*duration;
        }else if("ON".equals(action)){e.powered=true;e.pulseEnd=0;}else return false;
        data.markDirty();TileRedstoneRelay relay=relay(e);if(relay!=null)relay.refresh();return true;
    }
    private WirelessAccess(){}
}
