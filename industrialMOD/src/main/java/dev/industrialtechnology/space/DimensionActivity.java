package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.world.*;import net.minecraft.nbt.*;import net.minecraft.tileentity.TileEntity;import dev.industrialtechnology.machine.TileMachine;
/** Only this mod's machine index and tickets are touched. Vanilla spawn chunks are left to vanilla. */
public final class DimensionActivity {
    private static final Map<World,State> states=new WeakHashMap<World,State>();private static final class State {boolean occupied;long emptySince,exempt;int cursor;List<Entry> resume=Collections.emptyList();}
    public static final class Entry {int x,y,z;String id;Entry(int x,int y,int z,String id){this.x=x;this.y=y;this.z=z;this.id=id;}}
    public static final class Index extends WorldSavedData {
        private final Map<String,Entry> entries=new LinkedHashMap<String,Entry>();
        public Index(){super("industrialtech_machine_resume");}public Index(String name){super(name);}
        public static Index get(World w){Index d=(Index)w.perWorldStorage.loadData(Index.class,"industrialtech_machine_resume");if(d==null){d=new Index();w.perWorldStorage.setData("industrialtech_machine_resume",d);}return d;}
        void add(TileMachine t){String id=t.machineId.toString();Entry old=entries.get(id);if(old!=null&&old.x==t.xCoord&&old.y==t.yCoord&&old.z==t.zCoord)return;if(entries.size()>=100000)return;entries.put(id,new Entry(t.xCoord,t.yCoord,t.zCoord,id));markDirty();}
        @Override public void readFromNBT(NBTTagCompound n){entries.clear();NBTTagList l=n.getTagList("Machines",10);for(int i=0;i<Math.min(100000,l.tagCount());i++){NBTTagCompound e=l.getCompoundTagAt(i);String id=e.getString("Id");entries.put(id,new Entry(e.getInteger("X"),e.getInteger("Y"),e.getInteger("Z"),id));}}
        @Override public void writeToNBT(NBTTagCompound n){NBTTagList l=new NBTTagList();for(Entry e:entries.values()){NBTTagCompound v=new NBTTagCompound();v.setInteger("X",e.x);v.setInteger("Y",e.y);v.setInteger("Z",e.z);v.setString("Id",e.id);l.appendTag(v);}n.setTag("Machines",l);}
    }
    private static State state(World w){State s=states.get(w);if(s==null){s=new State();s.emptySince=w.getTotalWorldTime();s.occupied=!w.playerEntities.isEmpty();states.put(w,s);if(s.occupied)s.resume=new ArrayList<Entry>(Index.get(w).entries.values());}return s;}
    public static boolean active(World w){if(w==null||w.isRemote||!SpaceConfig.pauseOwnedChunkLoadingInUnoccupiedDimensions)return true;int dim=w.provider.dimensionId;if(dim!=0&&(!SpaceConfig.pausePlanetMachines||SpaceDimensions.planet(dim)<0))return true;State s=state(w);return !w.playerEntities.isEmpty()||w.getTotalWorldTime()<s.exempt||w.getTotalWorldTime()-s.emptySince<SpaceConfig.idleGrace;}
    public static void register(TileMachine t){if(t.getWorldObj()!=null&&!t.getWorldObj().isRemote)Index.get(t.getWorldObj()).add(t);}
    public static void unregister(TileMachine t){if(t.getWorldObj()==null||t.getWorldObj().isRemote)return;Index d=Index.get(t.getWorldObj());if(d.entries.remove(t.machineId.toString())!=null)d.markDirty();}
    public static void exempt(World w,int ticks){State s=state(w);s.exempt=Math.max(s.exempt,w.getTotalWorldTime()+Math.min(10000,Math.max(0,ticks)));}
    public static void tick(World w){if(w.isRemote)return;State s=state(w);boolean occupied=!w.playerEntities.isEmpty();if(occupied!=s.occupied){s.occupied=occupied;if(occupied){s.resume=new ArrayList<Entry>(Index.get(w).entries.values());s.cursor=0;}else s.emptySince=w.getTotalWorldTime();}
        if(!occupied||!active(w))return;Index index=Index.get(w);for(int count=0;count<2&&s.cursor<s.resume.size();count++){
            Entry e=s.resume.get(s.cursor++);w.getChunkFromChunkCoords(e.x>>4,e.z>>4);TileEntity te=w.getTileEntity(e.x,e.y,e.z);if(te instanceof TileMachine&&((TileMachine)te).machineId.toString().equals(e.id)){TileMachine t=(TileMachine)te;t.tickets.dirty();t.tickets.tick(t.wantsChunks());}else{index.entries.remove(e.id);index.markDirty();}
        }if(s.cursor>=s.resume.size())s.resume=Collections.emptyList();
    }
    public static void clear(){states.clear();}public static void unload(World w){states.remove(w);}
}
