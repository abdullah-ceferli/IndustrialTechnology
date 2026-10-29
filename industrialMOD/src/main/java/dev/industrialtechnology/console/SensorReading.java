package dev.industrialtechnology.console;
import java.lang.reflect.Method;
import dev.industrialtechnology.wireless.*;
import dev.industrialtechnology.capacitor.TileCapacitorBank;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/** Read-only sensor adapter. Energy is never converted between RF and HE. */
public final class SensorReading {
    public boolean available;
    public String status="UNAVAILABLE",name="",identifier="",unit="mB";
    public long amount,capacity;
    public int tanks,color=0x69b9b5;
    public static SensorReading fluid(WirelessEndpoint endpoint,int dimension,int index){
        SensorReading r=new SensorReading();if(endpoint==null)return r;
        MachineInspector.Reading reading=MachineInspector.inspect(endpoint,dimension,true);r.status=reading.status.toString();
        if(reading.status!=WirelessAccess.Status.ONLINE||!reading.fluidAvailable){if(reading.status==WirelessAccess.Status.ONLINE)r.status="NO TANK DATA";return r;}
        r.tanks=reading.tanks.size();if(index<0||index>=r.tanks){r.status="TANK NOT FOUND";return r;}
        MachineInspector.Tank tank=reading.tanks.get(index);r.amount=tank.amount;r.capacity=tank.capacity;r.name=tank.name;r.identifier=tank.identifier;r.color=tank.color;r.unit=tank.unit;
        r.available=r.capacity>0&&"mB".equals(r.unit);r.status=r.available?"ONLINE":"UNSUPPORTED UNIT";return r;
    }
    public static SensorReading energy(WirelessEndpoint endpoint,int dimension){
        SensorReading r=new SensorReading();if(endpoint==null)return r;r.status=WirelessAccess.status(endpoint,dimension).toString();if(!"ONLINE".equals(r.status))return r;
        try{
            TileEntity tile=WirelessAccess.tile(endpoint);if(tile==null){r.status="NO ENERGY DATA";return r;}
            if(tile instanceof TileCapacitorBank){NBTTagCompound n=((TileCapacitorBank)tile).snapshot();r.amount=n.getLong("Stored");r.capacity=n.getLong("Capacity");r.unit="RF";r.name="Capacitor network ("+n.getInteger("Banks")+")";}
            else{
                Object source=HbmMonitoring.resolve(tile);if(source==null){r.status="OFFLINE";return r;}
                Class<?> hbm=null;try{hbm=Class.forName("api.hbm.energymk2.IEnergyHandlerMK2");}catch(ClassNotFoundException ignored){}
                if(hbm!=null&&hbm.isInstance(source)){r.amount=((Number)hbm.getMethod("getPower").invoke(source)).longValue();r.capacity=((Number)hbm.getMethod("getMaxPower").invoke(source)).longValue();r.unit="HE";r.name="HBM energy";}
                else if(source instanceof cofh.api.energy.IEnergyReceiver){cofh.api.energy.IEnergyReceiver rf=(cofh.api.energy.IEnergyReceiver)source;ForgeDirection side=ForgeDirection.getOrientation(endpoint.side);r.amount=rf.getEnergyStored(side);r.capacity=rf.getMaxEnergyStored(side);r.unit="RF";r.name="RF energy";}
                else{r.status="NO ENERGY DATA";return r;}
            }
            r.available=r.amount>=0&&r.capacity>0&&r.amount<=r.capacity;r.identifier="energy:"+r.unit;r.status=r.available?"ONLINE":"ENERGY UNAVAILABLE";
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){r.status="ENERGY UNAVAILABLE";}
        return r;
    }
    public NBTTagCompound write(){NBTTagCompound n=new NBTTagCompound();n.setBoolean("Available",available);n.setString("Status",status);n.setString("Name",name);n.setString("Id",identifier==null?"":identifier);n.setString("Unit",unit);n.setLong("Amount",amount);n.setLong("Capacity",capacity);n.setInteger("Tanks",tanks);n.setInteger("Color",color);return n;}
}
