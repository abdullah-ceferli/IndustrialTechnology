package dev.industrialtechnology.wireless;
import java.util.UUID;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
public final class TileRedstoneRelay extends TileEntity {
    public UUID endpoint,placer;
    private boolean lastSignal,clientSignal;
    public boolean signal(){
        if(worldObj==null||!(getBlockType() instanceof BlockRedstoneRelay)||((BlockRedstoneRelay)getBlockType()).kind!=WirelessEndpoint.Kind.CONTROL)return false;if(worldObj.isRemote)return clientSignal;
        WirelessEndpoint e=WirelessData.get(worldObj).devices.get(endpoint);
        return matches(e)&&WirelessAccess.status(e,e.dimension)==WirelessAccess.Status.ONLINE&&WirelessAccess.signal(e,worldObj.getTotalWorldTime());
    }
    private boolean matches(WirelessEndpoint e){
        if(e==null||!(getBlockType() instanceof BlockRedstoneRelay)||e.kind!=((BlockRedstoneRelay)getBlockType()).kind||e.dimension!=worldObj.provider.dimensionId||e.side!=getBlockMetadata())return false;
        ForgeDirection d=ForgeDirection.getOrientation(e.side);return e.x+d.offsetX==xCoord&&e.y+d.offsetY==yCoord&&e.z+d.offsetZ==zCoord;
    }
    @Override public void updateEntity(){
        if(worldObj==null||worldObj.isRemote)return;
        WirelessData data=WirelessData.get(worldObj);WirelessEndpoint e=data.devices.get(endpoint);
        if(endpoint!=null&&!matches(e)){getBlockType().dropBlockAsItem(worldObj,xCoord,yCoord,zCoord,0,0);worldObj.setBlockToAir(xCoord,yCoord,zCoord);return;}
        if(e!=null&&e.kind==WirelessEndpoint.Kind.CONTROL&&e.powered&&e.pulseEnd>0&&worldObj.getTotalWorldTime()>=e.pulseEnd){e.powered=false;e.pulseEnd=0;data.markDirty();}
        refresh();
    }
    public void refresh(){
        if(worldObj==null||worldObj.isRemote)return;boolean now=signal();if(now==lastSignal)return;
        lastSignal=now;markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);
        worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord,zCoord,getBlockType());
        ForgeDirection d=ForgeDirection.getOrientation(getBlockMetadata());worldObj.notifyBlocksOfNeighborChange(xCoord-d.offsetX,yCoord-d.offsetY,zCoord-d.offsetZ,getBlockType());
    }
    public void removed(){if(worldObj!=null&&!worldObj.isRemote){WirelessData data=WirelessData.get(worldObj);WirelessEndpoint e=data.devices.get(endpoint);if(matches(e)){e.enabled=false;WirelessAccess.off(data,e);}}}
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);if(endpoint!=null)n.setString("Endpoint",endpoint.toString());if(placer!=null)n.setString("Placer",placer.toString());n.setBoolean("LastSignal",lastSignal);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);endpoint=WirelessData.uuid(n.getString("Endpoint"));placer=WirelessData.uuid(n.getString("Placer"));lastSignal=n.getBoolean("LastSignal");}
    @Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();n.setBoolean("Signal",signal());return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,n);}
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity p){clientSignal=p.func_148857_g().getBoolean("Signal");}
}
