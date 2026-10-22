package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;import net.minecraft.entity.Entity;import net.minecraft.util.AxisAlignedBB;
/** Fixed six-block chamber: floor controller, Z-axis doors at +/-2; base lies at -Z. */
public final class Airlock {
    private static final Map<World,Set<TileSpaceMachine>> locks=new WeakHashMap<World,Set<TileSpaceMachine>>();
    public static void clear(){locks.clear();}
    public static void unload(World w){locks.remove(w);}
    private static boolean door(TileSpaceMachine t,int dz){return t.loaded(t.xCoord,t.yCoord+1,t.zCoord+dz)&&t.getWorldObj().getBlock(t.xCoord,t.yCoord+1,t.zCoord+dz)==SpaceRegistry.door&&t.getWorldObj().getBlock(t.xCoord,t.yCoord+2,t.zCoord+dz)==SpaceRegistry.door;}
    private static boolean open(TileSpaceMachine t,int dz){return door(t,dz)&&(t.getWorldObj().getBlockMetadata(t.xCoord,t.yCoord+1,t.zCoord+dz)&4)!=0;}
    private static boolean setDoor(TileSpaceMachine t,int dz,boolean open){if(!door(t,dz))return false;World w=t.getWorldObj();int x=t.xCoord,y=t.yCoord+1,z=t.zCoord+dz;if(!open&&!w.getEntitiesWithinAABB(Entity.class,AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+2,z+1)).isEmpty()){t.lockStatus="Door blocked by an entity";return false;}int bits=open?4:0;w.setBlockMetadataWithNotify(x,y,z,bits,3);w.setBlockMetadataWithNotify(x,y+1,z,bits|8,3);Atmosphere.changed(w);return true;}
    public static boolean structure(TileSpaceMachine t){World w=t.getWorldObj();if(!door(t,-2)||!door(t,2))return false;for(int z=-2;z<=2;z++)for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++){
        int xx=t.xCoord+x,yy=t.yCoord+y,zz=t.zCoord+z;if(!t.loaded(xx,yy,zz))return false;
        if(x==0&&(z==-2||z==2)&&(y==1||y==2))continue;
        boolean shell=x==-1||x==1||y==0||y==3||z==-2||z==2;
        if(shell&&!RoomScanner.airtight(w,xx,yy,zz))return false;if(!shell&&RoomScanner.airtight(w,xx,yy,zz))return false;
    }return true;}
    private static int base(TileSpaceMachine t){return Atmosphere.pressure(t.getWorldObj(),t.xCoord,t.yCoord+1,t.zCoord-3);}
    private static int outside(TileSpaceMachine t){TileSpaceMachine r=Atmosphere.roomAt(t.getWorldObj(),t.xCoord,t.yCoord+1,t.zCoord+3);return SpaceDimensions.planet(t.getWorldObj().provider.dimensionId)<0?100:r==null?0:r.pressure;}
    public static void snapshot(TileSpaceMachine t,net.minecraft.nbt.NBTTagCompound n){if(t.getWorldObj()==null)return;n.setInteger("BasePressure",base(t));n.setInteger("OutsidePressure",outside(t));n.setBoolean("InnerOpen",open(t,-2));n.setBoolean("OuterOpen",open(t,2));}
    public static void action(TileSpaceMachine t,int a){if(!structure(t)){t.lockStatus="Build 1x3x2 chamber, Z doors +/-2, sealed walls/floor/roof";return;}
        if(a==0){setDoor(t,-2,false);setDoor(t,2,false);t.lockCycle=0;t.lockStatus="Doors closed";}
        else if(a==1){if(open(t,2)||Math.abs(t.lockPressure-base(t))>5){t.lockStatus="Close outer door and match base pressure first";return;}t.lockCycle=0;setDoor(t,-2,!open(t,-2));}
        else if(a==2){if(open(t,-2)||Math.abs(t.lockPressure-outside(t))>5){t.lockStatus="Close inner door and match outside pressure first";return;}t.lockCycle=0;setDoor(t,2,!open(t,2));}
        else if(a==3||a==4){if(!setDoor(t,-2,false)||!setDoor(t,2,false)){t.lockCycle=0;return;}t.lockCycle=a==3?1:2;t.lockStatus=a==3?"Pressurizing to base; open inner when matched":"Recovering air; open outer when matched";}
        t.markDirty();
    }
    public static void tick(TileSpaceMachine t){Set<TileSpaceMachine> set=locks.get(t.getWorldObj());if(set==null){set=new HashSet<TileSpaceMachine>();locks.put(t.getWorldObj(),set);}set.add(t);if(t.getWorldObj().getTotalWorldTime()%20!=0)return;
        if(!structure(t)){setDoor(t,-2,false);setDoor(t,2,false);t.lockCycle=0;t.lockPressure=Math.max(0,t.lockPressure-20);t.lockStatus="Chamber incomplete / leaking";return;}
        int occupants=0;for(Object o:t.getWorldObj().playerEntities){net.minecraft.entity.player.EntityPlayer p=(net.minecraft.entity.player.EntityPlayer)o;if(!p.capabilities.isCreativeMode&&Math.floor(p.posX)==t.xCoord&&p.posY>=t.yCoord+1&&p.posY<t.yCoord+3&&Math.abs(Math.floor(p.posZ)-t.zCoord)<=1)occupants++;}if(occupants>0){t.lockPressure=Math.max(0,t.lockPressure-occupants);t.markDirty();}
        if(open(t,-2)&&open(t,2)){setDoor(t,-2,false);setDoor(t,2,false);t.lockCycle=0;t.lockStatus="Interlock closed inconsistent doors";return;}
        if(open(t,-2)){int goal=base(t);if(goal>t.lockPressure){TileSpaceMachine r=Atmosphere.roomAt(t.getWorldObj(),t.xCoord,t.yCoord+1,t.zCoord-3);int units=(int)Math.ceil(6.0*SpaceConfig.airPerBlock*(goal-t.lockPressure)/100);if(r!=null&&r.gas>=units){r.gas-=units;r.markDirty();t.lockPressure=goal;}}else t.lockPressure=goal;t.lockStatus="Inner open; outer locked";return;}if(open(t,2)){int goal=outside(t);if(goal<t.lockPressure)t.lockPressure=goal;t.lockStatus="Outer open; inner locked";return;}
        if(!t.enabled||t.energy<SpaceConfig.machineRF){t.lockStatus="Doors closed; cycle needs power";return;}
        if(t.lockCycle==0)return;int goal=t.lockCycle==1?base(t):outside(t);if(t.lockPressure==goal){t.lockCycle=0;t.lockStatus="Pressure matched; selected door may open";return;}
        int delta=Math.min(10,Math.abs(goal-t.lockPressure));int units=(int)Math.ceil(6.0*SpaceConfig.airPerBlock*delta/100);if(goal>t.lockPressure){if(t.tanks[0].getFluidAmount()<units){t.lockStatus="Waiting for breathable air";return;}t.tanks[0].drain(units,true);t.lockPressure+=delta;}else{int accepted=t.tanks[0].fill(new net.minecraftforge.fluids.FluidStack(SpaceRegistry.air,units*80/100),false);if(accepted>0)t.tanks[0].fill(new net.minecraftforge.fluids.FluidStack(SpaceRegistry.air,accepted),true);t.lockPressure-=delta;}
        t.energy-=SpaceConfig.machineRF;t.markDirty();
    }
    public static int pressureAt(World w,int x,int y,int z){Set<TileSpaceMachine> s=locks.get(w);if(s==null)return 0;int p=0;for(TileSpaceMachine t:s)if(!t.isInvalid()&&x==t.xCoord&&y>=t.yCoord+1&&y<=t.yCoord+2&&Math.abs(z-t.zCoord)<=1)p=Math.max(p,t.lockPressure);return p;}
    public static TileSpaceMachine controller(World w,int x,int y,int z){for(int dz=-2;dz<=2;dz++)for(int dy=-2;dy<=-1;dy++){int yy=y+dy,zz=z+dz;if(!RoomScanner.loaded(w,x,yy,zz))continue;TileEntity te=w.getTileEntity(x,yy,zz);if(te instanceof TileSpaceMachine&&((TileSpaceMachine)te).kind==SpaceRegistry.AIRLOCK)return (TileSpaceMachine)te;}return null;}
}
