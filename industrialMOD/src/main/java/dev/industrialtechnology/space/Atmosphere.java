package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import net.minecraftforge.fluids.*;
public final class Atmosphere {
    private static final Map<World,Set<TileSpaceMachine>> rooms=new WeakHashMap<World,Set<TileSpaceMachine>>();private static final Map<World,Long> revisions=new WeakHashMap<World,Long>();private static final Map<TileSpaceMachine,Long> seen=new WeakHashMap<TileSpaceMachine,Long>();
    public static int budget=1024;public static void clear(){rooms.clear();revisions.clear();seen.clear();}
    public static void changed(World w){if(w!=null&&!w.isRemote)revisions.put(w,revision(w)+1);}
    private static long revision(World w){Long n=revisions.get(w);return n==null?0:n;}
    public static void unload(World w){rooms.remove(w);revisions.remove(w);seen.keySet().removeIf(t->t.getWorldObj()==w);}
    public static void remove(TileSpaceMachine t){Set<TileSpaceMachine> s=rooms.get(t.getWorldObj());if(s!=null)s.remove(t);seen.remove(t);}
    public static void tick(TileSpaceMachine t){World w=t.getWorldObj();Set<TileSpaceMachine> list=rooms.get(w);if(list==null){list=new HashSet<TileSpaceMachine>();rooms.put(w,list);}list.add(t);
        long rev=revision(w);Long old=seen.get(t);if(t.scan==null&&(old==null||old!=rev||t.rescanDue())){t.scan=new RoomScanner(t.xCoord,t.yCoord+1,t.zCoord);if(old==null||old!=rev)t.sealed=false;seen.put(t,rev);}
        if(t.scan!=null&&budget>0){int n=t.scan.step(w,Math.min(budget,SpaceConfig.roomBudget));budget-=n;if(t.scan.done){t.sealed=t.scan.sealed;t.roomMessage=t.scan.reason;if(t.sealed){t.room=t.scan.cells;t.roomVolume=t.room.size();for(TileSpaceMachine other:list)if(other!=t&&!other.isInvalid()&&other.sealed&&other.room.contains(RoomScanner.key(t.xCoord,t.yCoord+1,t.zCoord))&&compare(other,t)<0){t.sealed=false;t.roomMessage="Another pressurizer owns this room";break;}}t.scan=null;t.scanned();}}
        if(w.getTotalWorldTime()%20!=0)return;if(t.scan!=null&&t.room.isEmpty()){t.status="Validating saved room";t.pressure=0;return;}int need=Math.max(1,t.roomVolume*SpaceConfig.airPerBlock);
        if(t.enabled&&t.sealed&&t.energy>=SpaceConfig.machineRF){int use=Math.min(500,Math.min(Math.max(0,need-t.gas),t.tanks[0].getFluidAmount()));if(use>0){t.tanks[0].drain(use,true);t.gas+=use;t.energy-=SpaceConfig.machineRF;}}
        if(t.sealed){int occupants=0;for(Object o:w.playerEntities){EntityPlayer p=(EntityPlayer)o;if(!p.capabilities.isCreativeMode&&t.room.contains(RoomScanner.key((int)Math.floor(p.posX),(int)Math.floor(p.posY+p.getEyeHeight()),(int)Math.floor(p.posZ))))occupants++;}t.gas=Math.max(0,t.gas-Math.max(1,t.roomVolume/50)-occupants);}
        else t.gas=Math.max(0,t.gas-Math.max(1,need/5));
        t.gas=Math.min(t.gas,need);t.pressure=Math.max(0,Math.min(100,(int)(100L*t.gas/need)));t.status=t.scan!=null?"Validating room":t.sealed?(t.pressure>=80?"Breathable":"Pressurizing"):(t.gas>0?"LEAK: losing pressure":t.roomMessage);t.markDirty();
    }
    private static int compare(TileSpaceMachine a,TileSpaceMachine b){if(a.xCoord!=b.xCoord)return Integer.compare(a.xCoord,b.xCoord);if(a.yCoord!=b.yCoord)return Integer.compare(a.yCoord,b.yCoord);return Integer.compare(a.zCoord,b.zCoord);}
    public static TileSpaceMachine roomAt(World w,int x,int y,int z){Set<TileSpaceMachine> s=rooms.get(w);if(s==null)return null;TileSpaceMachine best=null;long k=RoomScanner.key(x,y,z);for(TileSpaceMachine t:s)if(!t.isInvalid()&&t.room.contains(k)&&(best==null||t.pressure>best.pressure))best=t;return best;}
    public static int pressure(World w,int x,int y,int z){if(SpaceDimensions.planet(w.provider.dimensionId)<0)return 100;TileSpaceMachine room=roomAt(w,x,y,z);int p=room==null?0:room.pressure;return Math.max(p,Airlock.pressureAt(w,x,y,z));}
    public static boolean breathable(EntityPlayer p){return pressure(p.worldObj,(int)Math.floor(p.posX),(int)Math.floor(p.posY+p.getEyeHeight()),(int)Math.floor(p.posZ))>=80;}
}
