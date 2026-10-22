package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.world.World;import net.minecraft.block.Block;import net.minecraft.util.AxisAlignedBB;
/** Incremental six-neighbour flood fill. Unknown/unloaded space is never considered sealed. */
public final class RoomScanner {
    public final Set<Long> cells=new HashSet<Long>();private final ArrayDeque<Long> queue=new ArrayDeque<Long>();private final int ox,oy,oz;public boolean done,sealed=true;public String reason="Scanning";
    public RoomScanner(int x,int y,int z){ox=x;oy=y;oz=z;long key=key(x,y,z);cells.add(key);queue.add(key);}
    public static long key(int x,int y,int z){return ((long)x&0x3ffffffL)<<38|((long)z&0x3ffffffL)<<12|(y&4095);}
    public static int x(long k){return (int)(k>>38);}public static int z(long k){return (int)(k<<26>>38);}public static int y(long k){return (int)(k&4095);}
    public static boolean loaded(World w,int x,int y,int z){return y>=0&&y<256&&w.getChunkProvider().chunkExists(x>>4,z>>4);}
    public static boolean airtight(World w,int x,int y,int z){if(!loaded(w,x,y,z))return false;Block b=w.getBlock(x,y,z);if(b==SpaceRegistry.door)return (w.getBlockMetadata(x,y,z)&4)==0;if(b==SpaceRegistry.hull||b==SpaceRegistry.glass)return true;
        if(!b.getMaterial().blocksMovement()||b.getMaterial().isLiquid())return false;AxisAlignedBB box=b.getCollisionBoundingBoxFromPool(w,x,y,z);return box!=null&&box.minX<=x&&box.minY<=y&&box.minZ<=z&&box.maxX>=x+1&&box.maxY>=y+1&&box.maxZ>=z+1;}
    private void fail(String why){sealed=false;done=true;reason=why;queue.clear();}
    public int step(World w,int budget){int used=0;while(!done&&used<budget){Long k=queue.poll();if(k==null){done=true;reason="Sealed";break;}used++;int x=x(k),y=y(k),z=z(k);
            if(!loaded(w,x,y,z)){fail("Unloaded boundary");break;}if(Math.abs(x-ox)>SpaceConfig.roomRadius||Math.abs(y-oy)>SpaceConfig.roomRadius||Math.abs(z-oz)>SpaceConfig.roomRadius){fail("Open boundary / radius limit");break;}
            if(airtight(w,x,y,z)){fail("Probe above pressurizer is blocked");break;}
            for(int d=0;d<6;d++){int xx=x+(d==0?1:d==1?-1:0),yy=y+(d==2?1:d==3?-1:0),zz=z+(d==4?1:d==5?-1:0);if(!loaded(w,xx,yy,zz)){fail("Unloaded boundary");break;}if(airtight(w,xx,yy,zz))continue;long n=key(xx,yy,zz);if(cells.add(n)){if(cells.size()>SpaceConfig.maxRoom){fail("Open boundary / volume limit");break;}queue.add(n);}}
        }return used;}
}
