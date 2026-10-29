package net.minecraft.world;
import java.util.*;
import net.minecraft.block.*;
public class World {
    public boolean isRemote=true, loaded=true, orphan;
    public int setCalls, lastFlags, invalidations, reads;
    public final BlockAir air=new BlockAir();
    private final Map<String,Block> blocks=new HashMap<String,Block>();
    private final Map<String,Integer> metadata=new HashMap<String,Integer>();
    private static String key(int x,int y,int z){return x+","+y+","+z;}
    public Block getBlock(int x,int y,int z){reads++;Block block=blocks.get(key(x,y,z));return block==null?air:block;}
    public int getBlockMetadata(int x,int y,int z){Integer value=metadata.get(key(x,y,z));return value==null?0:value;}
    public boolean blockExists(int x,int y,int z){return loaded && y>=0 && y<256;}
    public Object getTileEntity(int x,int y,int z){return orphan?this:null;}
    public boolean setBlock(int x,int y,int z,Block block,int meta,int flags){setCalls++;lastFlags=flags;blocks.put(key(x,y,z),block);metadata.put(key(x,y,z),meta);return true;}
}
