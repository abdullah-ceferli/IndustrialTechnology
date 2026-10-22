package dev.industrialtechnology.space;
import java.lang.reflect.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import dev.industrialtechnology.core.IndustrialTechnology;
/** Optional verified native tank bridge. No HBM classes are linked or bundled. */
public final class HbmSpaceBridge {
    private static boolean checked;private static Class<?> tankTile;private static Object air,kerosene,oil,oxygen;private static Method tanks,getType,getFill,setFill;private static Field nativeTank;
    public static Item material(String field){try{return (Item)Class.forName("com.hbm.items.ModItems").getField(field).get(null);}catch(ReflectiveOperationException|LinkageError e){return null;}}
    private static void check(){if(checked)return;checked=true;try{
        Class<?> f=Class.forName("com.hbm.inventory.fluid.Fluids"),t=Class.forName("com.hbm.inventory.fluid.tank.FluidTank");
        air=f.getField("AIR").get(null);oil=f.getField("OIL").get(null);kerosene=f.getField("KEROSENE").get(null);oxygen=f.getField("OXYGEN").get(null);
        tankTile=Class.forName("com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank");nativeTank=tankTile.getField("tank");tanks=tankTile.getMethod("getSendingTanks");getType=t.getMethod("getTankType");getFill=t.getMethod("getFill");setFill=t.getMethod("setFill",int.class);
        IndustrialTechnology.log.info("Space bridge: native HBM AIR/OIL/KEROSENE/OXYGEN storage tank extraction available; native pipes use the optional MK2 launch receiver.");
    }catch(ReflectiveOperationException|LinkageError e){tankTile=null;IndustrialTechnology.log.info("Space bridge: using standalone fluids and air manufacturing (native HBM storage adapter unavailable).");}}
    /** Caller supplies an owned, loaded, adjacent tank. Respect the HBM tank's native sending mode. */
    public static int extract(TileEntity tile,int fluid,int max,boolean simulate){check();if(tankTile==null||tile==null||tile.getClass()!=tankTile||max<=0)return 0;try{
        Object expected=fluid==0?air:fluid==1?oil:fluid==2?kerosene:fluid==3||fluid==4?oxygen:null;if(expected==null)return 0;
        Object tank=nativeTank.get(tile);Object[] sending=(Object[])tanks.invoke(tile);boolean allowed=false;for(Object s:sending)if(s==tank)allowed=true;
        if(!allowed||getType.invoke(tank)!=expected)return 0;int amount=Math.min(max,(Integer)getFill.invoke(tank));
        if(!simulate&&amount>0){setFill.invoke(tank,(Integer)getFill.invoke(tank)-amount);tile.markDirty();}return amount;
    }catch(ReflectiveOperationException|RuntimeException e){return 0;}}
}
