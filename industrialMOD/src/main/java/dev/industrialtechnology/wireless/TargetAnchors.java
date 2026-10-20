package dev.industrialtechnology.wireless;
import java.util.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.nbt.*;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
/** 1.7.10 has no TileEntity#getTileData. Persist identity in our own chunk-data namespace. */
public final class TargetAnchors {
    private static final Map<TileEntity,UUID> TOKENS=Collections.synchronizedMap(new WeakHashMap<TileEntity,UUID>());
    public static UUID bind(TileEntity tile){UUID id=TOKENS.get(tile);if(id==null){id=UUID.randomUUID();TOKENS.put(tile,id);tile.markDirty();}return id;}
    public static boolean matches(TileEntity tile,UUID id){return tile!=null&&id!=null&&id.equals(TOKENS.get(tile));}
    @SubscribeEvent public void save(ChunkDataEvent.Save event){
        NBTTagList list=new NBTTagList();for(Object value:event.getChunk().chunkTileEntityMap.values()){
            TileEntity t=(TileEntity)value;UUID token=TOKENS.get(t);if(token==null||t.isInvalid())continue;
            NBTTagCompound n=new NBTTagCompound();n.setInteger("X",t.xCoord&15);n.setInteger("Y",t.yCoord);n.setInteger("Z",t.zCoord&15);n.setString("Id",token.toString());n.setString("Class",t.getClass().getName());list.appendTag(n);
        }event.getData().setTag("IndustrialWirelessAnchors",list);
    }
    @SubscribeEvent public void load(ChunkDataEvent.Load event){
        NBTTagList list=event.getData().getTagList("IndustrialWirelessAnchors",10);for(int i=0;i<Math.min(32768,list.tagCount());i++){
            NBTTagCompound n=list.getCompoundTagAt(i);int x=n.getInteger("X"),y=n.getInteger("Y"),z=n.getInteger("Z");if(x<0||x>15||z<0||z>15||y<0||y>255)continue;
            TileEntity t=(TileEntity)event.getChunk().chunkTileEntityMap.get(new ChunkPosition(x,y,z));UUID token=WirelessData.uuid(n.getString("Id"));
            if(t!=null&&token!=null&&t.getClass().getName().equals(n.getString("Class")))TOKENS.put(t,token);
        }
    }
    @SubscribeEvent public void unload(WorldEvent.Unload event){synchronized(TOKENS){Iterator<TileEntity> iterator=TOKENS.keySet().iterator();while(iterator.hasNext())if(iterator.next().getWorldObj()==event.world)iterator.remove();}}
}
