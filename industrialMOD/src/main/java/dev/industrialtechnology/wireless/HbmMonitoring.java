package dev.industrialtechnology.wireless;
import java.lang.reflect.*;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;
/** Optional, reflection-isolated adapter verified against HBM 1.0.27_X5808 public APIs. */
public final class HbmMonitoring {
    public static Object resolve(TileEntity tile)throws ReflectiveOperationException{
        if(!tile.getClass().getName().startsWith("com.hbm."))return tile;
        Class<?> proxy;
        try{proxy=Class.forName("com.hbm.tileentity.TileEntityProxyBase");}catch(ClassNotFoundException absent){return tile;}
        if(!proxy.isInstance(tile))return tile;
        Object position=proxy.getField("cachedPosition").get(tile);int[] xyz=null;
        if(position!=null)xyz=new int[]{coordinate(position,"getX"),coordinate(position,"getY"),coordinate(position,"getZ")};
        if(xyz==null){
            Block block=tile.getBlockType();
            try{xyz=(int[])block.getClass().getMethod("findCore",IBlockAccess.class,int.class,int.class,int.class)
                .invoke(block,new LoadedAccess(tile.getWorldObj(),tile.xCoord,tile.zCoord),tile.xCoord,tile.yCoord,tile.zCoord);}
            catch(NoSuchMethodException unsupported){return null;}
        }
        if(xyz==null||xyz.length!=3||!WirelessAccess.loaded(tile.getWorldObj(),xyz[0],xyz[1],xyz[2]))return null;
        TileEntity controller=tile.getWorldObj().getTileEntity(xyz[0],xyz[1],xyz[2]);if(controller==null||proxy.isInstance(controller))return null;
        // Dynamic chemical-factory ports expose a delegated inventory/tank view per access position.
        try{Class<?> provider=Class.forName("com.hbm.tileentity.TileEntityProxyDyn$IProxyDelegateProvider");
            if(provider.isInstance(controller))return provider.getMethod("getDelegateForPosition",int.class,int.class,int.class).invoke(controller,tile.xCoord,tile.yCoord,tile.zCoord);
        }catch(ClassNotFoundException absent){}
        return controller;
    }
    static boolean allows(TileEntity tile,String capability)throws ReflectiveOperationException{
        if(!tile.getClass().getName().startsWith("com.hbm."))return true;
        Class<?> base=Class.forName("com.hbm.tileentity.TileEntityProxyBase");if(!base.isInstance(tile))return true;
        Class<?> combo=Class.forName("com.hbm.tileentity.TileEntityProxyCombo");if(!combo.isInstance(tile))return false;
        // X5808's public proxy methods gate on these inspected package-private flags.
        // Reading them avoids calling proxy lookup code that could load chunks before our guard.
        Field flag=combo.getDeclaredField(capability);flag.setAccessible(true);return flag.getBoolean(tile);
    }
    private static int coordinate(Object position,String method)throws ReflectiveOperationException{return((Number)position.getClass().getMethod(method).invoke(position)).intValue();}
    static boolean readTanks(Object source,List<MachineInspector.Tank> output)throws ReflectiveOperationException{
        if(!source.getClass().getName().startsWith("com.hbm."))return false;
        Method all;try{all=source.getClass().getMethod("getAllTanks");}catch(NoSuchMethodException unavailable){return false;}
        Object tanks=all.invoke(source);if(tanks==null||!tanks.getClass().isArray())return false;
        if(Array.getLength(tanks)>MachineInspector.MAX_TANKS)throw new IllegalStateException("Too many tanks");
        for(int i=0;i<Array.getLength(tanks);i++){
            Object tank=Array.get(tanks,i);if(tank==null||!tank.getClass().getName().equals("com.hbm.inventory.fluid.tank.FluidTank"))throw new IllegalStateException("Unsupported tank in exposed set");
            Class<?> c=tank.getClass();long fill=((Number)c.getMethod("getFill").invoke(tank)).longValue(),max=((Number)c.getMethod("getMaxFill").invoke(tank)).longValue();
            Object type=c.getMethod("getTankType").invoke(tank);String name="Empty";int color=0x71838a;
            if(type!=null){name=String.valueOf(type.getClass().getMethod("getName").invoke(type));color=((Number)type.getClass().getMethod("getColor").invoke(type)).intValue();}
            // The inspected HBM tank GUI labels getFill/getMaxFill as mB. No unit conversion is performed.
            MachineInspector.Tank value=new MachineInspector.Tank(name,fill,max,"mB",color);
            // getLocalizedName() is @SideOnly(CLIENT) in X5808. Send the verified common translation key.
            if(type!=null)value.localizationKey=String.valueOf(type.getClass().getMethod("getUnlocalizedName").invoke(type));
            value.identifier=type==null||name.equalsIgnoreCase("NONE")?"":"hbm:"+name;output.add(value);
        }return !output.isEmpty();
    }
    /** HBM findCore receives a view that never loads chunks, with a finite lookup radius. */
    private static final class LoadedAccess implements IBlockAccess {
        private final World world;private final int x,z;
        LoadedAccess(World world,int x,int z){this.world=world;this.x=x;this.z=z;}
        private boolean loaded(int a,int b,int c){return Math.abs((long)a-x)<=128&&Math.abs((long)c-z)<=128&&WirelessAccess.loaded(world,a,b,c);}
        public Block getBlock(int a,int b,int c){return loaded(a,b,c)?world.getBlock(a,b,c):Blocks.air;}
        public TileEntity getTileEntity(int a,int b,int c){return loaded(a,b,c)?world.getTileEntity(a,b,c):null;}
        public int getBlockMetadata(int a,int b,int c){return loaded(a,b,c)?world.getBlockMetadata(a,b,c):0;}
        public boolean isAirBlock(int a,int b,int c){return getBlock(a,b,c)==Blocks.air;}
        public int isBlockProvidingPowerTo(int a,int b,int c,int side){return 0;}
        public boolean isSideSolid(int a,int b,int c,ForgeDirection side,boolean fallback){return loaded(a,b,c)&&world.isSideSolid(a,b,c,side,fallback);}
        public int getLightBrightnessForSkyBlocks(int a,int b,int c,int light){return 0;}
        public BiomeGenBase getBiomeGenForCoords(int a,int c){return BiomeGenBase.plains;}
        public int getHeight(){return 256;}
        public boolean extendedLevelsInChunkCache(){return false;}
    }
    private HbmMonitoring(){}
}
