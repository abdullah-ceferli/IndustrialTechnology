package dev.industrialtechnology.wireless;
import java.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;
/** World-owned state. Runtime caches never replace the serialized registry. */
public final class WirelessData extends WorldSavedData {
    private static final String KEY="industrialtech_wireless";
    public final Map<UUID,UUID> networks=new LinkedHashMap<UUID,UUID>();
    public final Map<UUID,WirelessEndpoint> devices=new LinkedHashMap<UUID,WirelessEndpoint>();
    public int revision;
    public WirelessData(){super(KEY);}
    public WirelessData(String name){super(name);}
    public static WirelessData get(World world){
        if(world==null||world.isRemote)throw new IllegalStateException("Server registry only");
        WorldServer home=DimensionManager.getWorld(0);if(home==null)throw new IllegalStateException("Overworld unavailable");
        WirelessData data=(WirelessData)home.mapStorage.loadData(WirelessData.class,KEY);
        if(data==null){data=new WirelessData();home.mapStorage.setData(KEY,data);data.markDirty();}return data;
    }
    public boolean owns(UUID network,UUID player){return player!=null&&player.equals(networks.get(network));}
    public List<WirelessEndpoint> linked(UUID network){List<WirelessEndpoint> result=new ArrayList<WirelessEndpoint>();for(WirelessEndpoint e:devices.values())if(network.equals(e.network))result.add(e);return result;}
    public UUID create(UUID owner){int count=0;for(UUID o:networks.values())if(owner.equals(o))count++;if(count>=WirelessConfig.terminals)return null;UUID id=UUID.randomUUID();networks.put(id,owner);markDirty();return id;}
    public boolean add(WirelessEndpoint e){if(!owns(e.network,e.owner)||devices.size()>=WirelessConfig.globalEndpoints||linked(e.network).size()>=WirelessConfig.endpoints)return false;devices.put(e.id,e);revision++;markDirty();return true;}
    public void remove(WirelessEndpoint e){devices.remove(e.id);revision++;markDirty();}
    @Override public void writeToNBT(NBTTagCompound n){
        NBTTagList nets=new NBTTagList();for(Map.Entry<UUID,UUID> e:networks.entrySet()){NBTTagCompound t=new NBTTagCompound();t.setString("Id",e.getKey().toString());t.setString("Owner",e.getValue().toString());nets.appendTag(t);}n.setTag("Networks",nets);
        NBTTagList list=new NBTTagList();for(WirelessEndpoint e:devices.values())list.appendTag(e.write());n.setTag("Endpoints",list);n.setInteger("Version",1);
    }
    @Override public void readFromNBT(NBTTagCompound n){
        networks.clear();devices.clear();NBTTagList nets=n.getTagList("Networks",10);
        for(int i=0;i<Math.min(65536,nets.tagCount());i++){NBTTagCompound t=nets.getCompoundTagAt(i);UUID id=uuid(t.getString("Id")),owner=uuid(t.getString("Owner"));if(id!=null&&owner!=null)networks.put(id,owner);}
        NBTTagList list=n.getTagList("Endpoints",10);for(int i=0;i<Math.min(32768,list.tagCount());i++){WirelessEndpoint e=WirelessEndpoint.read(list.getCompoundTagAt(i));if(e!=null&&owns(e.network,e.owner))devices.put(e.id,e);}revision++;
    }
    public static UUID uuid(String value){try{return UUID.fromString(value);}catch(IllegalArgumentException e){return null;}}
    public static String clean(String text,int max){if(text==null)return "";String value=text.replaceAll("[\\p{Cntrl}\u00a7]","").trim();return value.substring(0,Math.min(max,value.length()));}
}
