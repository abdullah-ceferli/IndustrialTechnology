package dev.industrialtechnology.railway;
import java.util.UUID;import net.minecraft.tileentity.TileEntity;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.network.*;import net.minecraft.network.play.server.S35PacketUpdateTileEntity;import net.minecraft.util.AxisAlignedBB;
public final class TileTrack extends TileEntity {
 public UUID id=UUID.randomUUID();public int shape,facing;public boolean switched,station;
 @Override public boolean canUpdate(){return false;}public RailPath path(int branch){return new RailPath(xCoord+.5,yCoord+.125,zCoord+.5,facing,shape,branch);}public int active(){return shape==4&&switched?1:0;}
 @Override public void validate(){super.validate();TrackWorld.add(this);}@Override public void invalidate(){TrackWorld.remove(this);super.invalidate();}@Override public void onChunkUnload(){TrackWorld.remove(this);super.onChunkUnload();}
 public void toggle(){if(worldObj!=null&&!worldObj.isRemote)for(Object o:worldObj.getEntitiesWithinAABB(EntityRollingStock.class,getRenderBoundingBox().expand(24,0,24))){EntityRollingStock e=(EntityRollingStock)o;if(e.rail!=null&&e.rail.track.equals(id))return;}switched=!switched;markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
 @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setString("RailId",id.toString());n.setInteger("Shape",shape);n.setInteger("Facing",facing);n.setBoolean("Switched",switched);n.setBoolean("Station",station);}
 @Override public void readFromNBT(NBTTagCompound n){TrackWorld.remove(this);super.readFromNBT(n);try{id=UUID.fromString(n.getString("RailId"));}catch(Exception ignored){}shape=Math.max(0,Math.min(4,n.getInteger("Shape")));facing=Math.floorMod(n.getInteger("Facing"),4);switched=n.getBoolean("Switched");station=n.getBoolean("Station");TrackWorld.add(this);}
 @Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,n);}@Override public void onDataPacket(NetworkManager m,S35PacketUpdateTileEntity p){readFromNBT(p.func_148857_g());}
 @Override public AxisAlignedBB getRenderBoundingBox(){return AxisAlignedBB.getBoundingBox(xCoord-18,yCoord,zCoord-18,xCoord+19,yCoord+6,zCoord+19);}@Override public double getMaxRenderDistanceSquared(){return 65536;}
}
