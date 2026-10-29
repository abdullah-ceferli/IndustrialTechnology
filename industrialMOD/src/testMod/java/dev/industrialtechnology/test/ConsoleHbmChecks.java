package dev.industrialtechnology.test;
import java.util.UUID;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.storage.*;
import com.hbm.inventory.fluid.Fluids;
import dev.industrialtechnology.console.*;
import dev.industrialtechnology.wireless.*;
import net.minecraft.world.WorldServer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
public final class ConsoleHbmChecks {
    public static void run(WorldServer w,FakePlayer p,UUID network)throws Exception{
        w.setBlock(330,70,90,ModBlocks.machine_bigasstank,12,3);TileEntityMachineBigAssTank tank=(TileEntityMachineBigAssTank)w.getTileEntity(330,70,90);tank.tank.setTankType(Fluids.NAPHTHA);tank.tank.setFill(2000000);
        WirelessEndpoint green=endpoint(w,p,network,330,70,90,WirelessEndpoint.Kind.MONITOR);SensorReading fluid=SensorReading.fluid(green,0,0);if(!fluid.available||fluid.amount!=2000000||!"mB".equals(fluid.unit))throw new AssertionError("Actual HBM tank: "+fluid.status+" "+fluid.amount);
        w.setBlock(334,70,90,ModBlocks.machine_battery,0,3);Object battery=w.getTileEntity(334,70,90);battery.getClass().getMethod("setPower",long.class).invoke(battery,123456L);WirelessEndpoint yellow=endpoint(w,p,network,334,70,90,WirelessEndpoint.Kind.CONTROL);SensorReading he=SensorReading.energy(yellow,0);if(!he.available||he.amount!=123456||!"HE".equals(he.unit)||((Number)battery.getClass().getMethod("getPower").invoke(battery)).longValue()!=123456)throw new AssertionError("Actual HBM HE: "+he.status+" "+he.amount);
        WirelessAccess.command(WirelessData.get(w),yellow,"ON",0);if(!yellow.powered)throw new AssertionError("Yellow lost redstone capability");WirelessAccess.off(WirelessData.get(w),yellow);
    }
    private static WirelessEndpoint endpoint(WorldServer w,FakePlayer p,UUID network,int x,int y,int z,WirelessEndpoint.Kind kind){
        p.setPosition(x+.5,y,z+2);ItemStack item=new ItemStack(ItemWirelessStick.item(kind));((ItemWirelessStick)item.getItem()).onItemUseFirst(item,p,w,x,y,z,3,.5F,.5F,1);net.minecraft.tileentity.TileEntity te=w.getTileEntity(x,y,z+1);if(!(te instanceof TileRedstoneRelay))throw new AssertionError("HBM sensor torch failed to place");return WirelessData.get(w).devices.get(((TileRedstoneRelay)te).endpoint);
    }
}
