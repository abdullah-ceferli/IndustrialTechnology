package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.world.*;import net.minecraft.nbt.*;
/** Persistent vehicle identity prevents copied payload NBT from creating two live rockets. */
public final class FlightLedger extends WorldSavedData {
    private final Map<String,String> vehicles=new HashMap<String,String>();
    public FlightLedger(){super("industrialtech_space_flights");}public FlightLedger(String s){super(s);}
    public static FlightLedger get(World w){World root=net.minecraftforge.common.DimensionManager.getWorld(0);if(root==null)root=w;FlightLedger d=(FlightLedger)root.mapStorage.loadData(FlightLedger.class,"industrialtech_space_flights");if(d==null){d=new FlightLedger();root.mapStorage.setData("industrialtech_space_flights",d);}return d;}
    public boolean claim(String rocket,String entity){String v=vehicles.get(rocket);if(v==null&&vehicles.size()>=100000)return false;if(v!=null&&!v.equals(entity))return false;vehicles.put(rocket,entity);markDirty();return true;}
    public void recover(String rocket,String entity){if(entity.equals(vehicles.get(rocket))){vehicles.remove(rocket);markDirty();}}
    @Override public void readFromNBT(NBTTagCompound n){vehicles.clear();NBTTagList l=n.getTagList("Vehicles",10);for(int i=0;i<Math.min(100000,l.tagCount());i++){NBTTagCompound v=l.getCompoundTagAt(i);vehicles.put(v.getString("Rocket"),v.getString("Entity"));}}
    @Override public void writeToNBT(NBTTagCompound n){NBTTagList l=new NBTTagList();for(Map.Entry<String,String> e:vehicles.entrySet()){NBTTagCompound v=new NBTTagCompound();v.setString("Rocket",e.getKey());v.setString("Entity",e.getValue());l.appendTag(v);}n.setTag("Vehicles",l);}
}
