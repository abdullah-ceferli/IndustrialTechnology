package dev.industrialtechnology.space;

import java.util.*;import com.mojang.authlib.GameProfile;import net.minecraft.entity.player.*;import net.minecraft.item.ItemStack;import net.minecraft.nbt.*;import net.minecraft.util.*;import net.minecraft.world.WorldServer;import net.minecraftforge.common.util.*;import net.minecraftforge.event.ForgeEventFactory;
/** Incremental checked deployment. Occupied cells are never cleared to fit a pad. */
public final class TileLaunchFacility extends TileSpaceMachine {
    public String facilityId=UUID.randomUUID().toString();public int phase,cursor,preparedChunks;public long armStart=-1; // scan/build/ready/cleanup
    public TileLaunchFacility(){super(SpaceRegistry.LAUNCH);}
    public int facing(){return BlockSpaceMachine.front(getBlockMetadata());}
    public int worldX(int x,int z){return xCoord+LaunchFacilityPlan.dx(x,z,facing());}public int worldZ(int x,int z){return zCoord+LaunchFacilityPlan.dz(x,z,facing());}
    public int shipX(){return worldX(LaunchFacilityPlan.SHIP_X,LaunchFacilityPlan.SHIP_Z);}public int shipZ(){return worldZ(LaunchFacilityPlan.SHIP_X,LaunchFacilityPlan.SHIP_Z);}public int shipY(){return yCoord+LaunchFacilityPlan.DECK;}
    public NBTTagCompound controllerPayload(){NBTTagCompound n=new NBTTagCompound();super.writeToNBT(n);n.removeTag("x");n.removeTag("y");n.removeTag("z");n.removeTag("id");return n;}
    private EntityPlayerMP builder(){try{return net.minecraftforge.common.util.FakePlayerFactory.get((WorldServer)worldObj,new GameProfile(UUID.fromString(owner),"[LC39 construction]"));}catch(RuntimeException e){return null;}}
    private boolean ownerOnline(){for(Object p:worldObj.playerEntities)if(((EntityPlayer)p).getUniqueID().toString().equals(owner))return true;return false;}
    @Override public void updateEntity(){if(worldObj==null||worldObj.isRemote)return;FacilityIndex.Entry entry=FacilityIndex.get(worldObj).reserve(this);if(entry==null){dismantle("Facility overlaps another pad or index is full");return;}if(phase==3||entry.cleanup){phase=3;return;}
        if(phase!=2){if(!ownerOnline()){status="Deployment paused: owner is offline";return;}advance();return;}if(worldObj.getTotalWorldTime()%20==0)super.updateEntity();
    }
    private boolean prepareChunks(){int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minZ=minX,maxZ=maxX;for(int x:new int[]{LaunchFacilityPlan.MIN_X,LaunchFacilityPlan.MAX_X})for(int z:new int[]{LaunchFacilityPlan.MIN_Z,LaunchFacilityPlan.MAX_Z}){minX=Math.min(minX,worldX(x,z));maxX=Math.max(maxX,worldX(x,z));minZ=Math.min(minZ,worldZ(x,z));maxZ=Math.max(maxZ,worldZ(x,z));}int cx=(minX>>4)-1,cz=(minZ>>4)-1,nx=(maxX>>4)-cx+2,nz=(maxZ>>4)-cz+2,total=nx*nz;
        // Populate a one-chunk border before placing parts, so later neighbour generation
        // cannot run a structure generator over an already built edge of this facility.
        int loaded=0;while(preparedChunks<total&&loaded++<2&&FacilityIndex.budget>=64){FacilityIndex.budget-=64;worldObj.getChunkFromChunkCoords(cx+preparedChunks%nx,cz+preparedChunks/nx);preparedChunks++;if(!isLoaded())return false;}markDirty();if(preparedChunks<total){status="Preparing terrain chunks "+preparedChunks+" / "+total;return false;}return true;}
    public void advance(){if(phase==0&&!prepareChunks())return;EntityPlayerMP p=builder();if(p==null){dismantle("Invalid builder ownership");return;}int total=LaunchFacilityPlan.CELLS.size(),vacuum=27*30*57;int work=0;
        while(work++<256&&FacilityIndex.budget-->0&&phase<2){
            if(phase==0&&cursor>=total+vacuum){phase=1;cursor=0;continue;}
            if(phase==1&&cursor>=total){phase=2;cursor=0;status="LC-39 facility ready";markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);break;}
            int x,y,z,kind=-1;if(cursor<total){LaunchFacilityPlan.Cell c=LaunchFacilityPlan.CELLS.get(cursor);x=worldX(c.x,c.z);y=yCoord+c.y;z=worldZ(c.x,c.z);kind=c.kind;}else{int n=cursor-total,dx=n%27-13,dz=n/27%30-6,dy=n/(27*30);x=worldX(LaunchFacilityPlan.SHIP_X+dx,LaunchFacilityPlan.SHIP_Z+dz);z=worldZ(LaunchFacilityPlan.SHIP_X+dx,LaunchFacilityPlan.SHIP_Z+dz);y=shipY()+dy;}
            worldObj.getChunkFromChunkCoords(x>>4,z>>4);if(!worldObj.isAirBlock(x,y,z)){dismantle("Blocked at "+x+" "+y+" "+z+"; returning kit");break;}
            p.worldObj=worldObj;p.dimension=worldObj.provider.dimensionId;p.setPosition(x+.5,y+.5,z+.5);if(!p.canPlayerEdit(x,y,z,1,new ItemStack(LaunchFacilityRegistry.facility))||!SpaceProtection.interact(this,x,y,z)){dismantle("Protected position; returning kit");break;}
            if(phase==1){BlockSnapshot before=BlockSnapshot.getBlockSnapshot(worldObj,x,y,z);worldObj.setBlock(x,y,z,LaunchFacilityRegistry.part,kind,2);if(ForgeEventFactory.onPlayerBlockPlace(p,before,ForgeDirection.UP).isCanceled()){before.restore(true,false);dismantle("Placement denied; returning kit");break;}}
            cursor++;
        }
        if(phase<2)status=(phase==0?"Checking clear space ":"Deploying LC-39 ")+cursor*100/Math.max(1,phase==0?total+vacuum:total)+"%";
        markDirty();if(worldObj.getTotalWorldTime()%20==0)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);
    }
    public void dismantle(String message){if(phase==3)return;int built=phase==2?LaunchFacilityPlan.CELLS.size():phase==1?cursor:0;status=message;phase=3;FacilityIndex d=FacilityIndex.get(worldObj);FacilityIndex.Entry e=d.entries.get(facilityId);if(e==null){e=new FacilityIndex.Entry();e.id=facilityId;e.owner=owner;e.dim=worldObj.provider.dimensionId;e.x=xCoord;e.y=yCoord;e.z=zCoord;e.facing=facing();d.entries.put(e.id,e);}e.cleanup=true;e.cursor=0;e.limit=built;e.payload=controllerPayload();d.markDirty();markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
    @Override public boolean isUseableByPlayer(EntityPlayer p){return worldObj!=null&&p.worldObj==worldObj&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&p.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=20000&&authorized(p);}
    public boolean intact(){if(phase!=2)return false;for(int x:new int[]{-16,16})for(int z:new int[]{-42,-25,-8}){int xx=worldX(x,z),zz=worldZ(x,z);if(!loaded(xx,yCoord+7,zz)||worldObj.getBlock(xx,yCoord+7,zz)!=LaunchFacilityRegistry.part)return false;}return true;}
    @Override public NBTTagCompound snapshot(){NBTTagCompound n=super.snapshot();n.setBoolean("Facility",true);n.setInteger("FacilityPhase",phase);n.setInteger("FacilityProgress",cursor);n.setString("FacilityId",facilityId);n.setLong("ArmStart",armStart);return n;}
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setString("FacilityId",facilityId);n.setInteger("FacilityPhase",phase);n.setInteger("FacilityCursor",cursor);n.setInteger("PreparedChunks",preparedChunks);n.setLong("ArmStart",armStart);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);facilityId=n.hasKey("FacilityId")?n.getString("FacilityId"):UUID.randomUUID().toString();phase=Math.max(0,Math.min(3,n.getInteger("FacilityPhase")));cursor=Math.max(0,n.getInteger("FacilityCursor"));preparedChunks=Math.max(0,Math.min(144,n.getInteger("PreparedChunks")));armStart=n.hasKey("ArmStart")?n.getLong("ArmStart"):-1;}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){NBTTagCompound n=snapshot();n.setBoolean("Rocket",items[0]!=null);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,n);}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager m,net.minecraft.network.play.server.S35PacketUpdateTileEntity p){super.onDataPacket(m,p);NBTTagCompound n=p.func_148857_g();phase=n.getInteger("FacilityPhase");cursor=n.getInteger("FacilityProgress");armStart=n.getLong("ArmStart");clientShuttle=n.getBoolean("Shuttle");}
    @Override public AxisAlignedBB getRenderBoundingBox(){return AxisAlignedBB.getBoundingBox(xCoord-80,yCoord-1,zCoord-80,xCoord+80,yCoord+110,zCoord+80);}
    @Override public double getMaxRenderDistanceSquared(){return 512*512;}
}
