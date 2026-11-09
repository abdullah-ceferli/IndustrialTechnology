package dev.industrialtechnology.wireless;
import java.util.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import dev.industrialtechnology.energy.BatteryFlowMeter;
import dev.industrialtechnology.capacitor.TileCapacitorBank;
/** Bounded, read-only row discovery. No cables followed, no chunks requested, no energy conversion. */
public final class BatteryMonitor {
    private static final int RADIUS=32;
    private String previousKey="";private long previousAmount,previousTick;
    private static final class Battery {
        Object source;TileEntity tile;long amount,capacity,in,out;int count=1;String unit;boolean bank,groupable;
    }
    private static Battery read(TileEntity tile,int side)throws ReflectiveOperationException{
        if(tile==null||tile.isInvalid())return null;Object source=HbmMonitoring.resolve(tile);if(source==null)return null;Battery b=new Battery();b.source=source;b.tile=source instanceof TileEntity?(TileEntity)source:tile;
        if(source instanceof TileCapacitorBank){NBTTagCompound n=((TileCapacitorBank)source).snapshot();b.amount=n.getLong("Stored");b.capacity=n.getLong("Capacity");b.count=n.getInteger("Banks");b.in=n.getLong("In");b.out=n.getLong("Out");b.unit="RF";b.bank=true;}
        else if(isHbmEnergy(source)){Class<?> api=Class.forName("api.hbm.energymk2.IEnergyHandlerMK2");b.amount=((Number)api.getMethod("getPower").invoke(source)).longValue();b.capacity=((Number)api.getMethod("getMaxPower").invoke(source)).longValue();b.unit="HE";}
        else if(source instanceof cofh.api.energy.IEnergyReceiver){cofh.api.energy.IEnergyReceiver rf=(cofh.api.energy.IEnergyReceiver)source;ForgeDirection face=ForgeDirection.getOrientation(side);b.amount=rf.getEnergyStored(face);b.capacity=rf.getMaxEnergyStored(face);b.unit="RF";}
        else if(source instanceof cofh.api.energy.IEnergyProvider){cofh.api.energy.IEnergyProvider rf=(cofh.api.energy.IEnergyProvider)source;ForgeDirection face=ForgeDirection.getOrientation(side);b.amount=rf.getEnergyStored(face);b.capacity=rf.getMaxEnergyStored(face);b.unit="RF";}
        else return null;
        String type=source.getClass().getName().toLowerCase(Locale.ROOT);b.groupable=!b.bank&&(type.contains("battery")||type.contains("capacitor")||type.contains(".cell."));
        return b.amount<0||b.capacity<=0||b.amount>b.capacity?null:b;
    }
    private static boolean isHbmEnergy(Object object){try{return Class.forName("api.hbm.energymk2.IEnergyHandlerMK2").isInstance(object);}catch(ClassNotFoundException absent){return false;}}
    private static final class Row {final List<Battery> batteries=new ArrayList<Battery>();boolean partial;int axis;}
    private static Row row(Battery center,int side,int axis)throws ReflectiveOperationException{
        Row row=new Row();row.batteries.add(center);row.axis=axis;World world=center.tile.getWorldObj();Set<Object> seen=Collections.newSetFromMap(new IdentityHashMap<Object,Boolean>());seen.add(center.source);
        for(int sign:new int[]{-1,1})for(int distance=1;distance<=RADIUS+1;distance++){
            int x=center.tile.xCoord+(axis==0?sign*distance:0),y=center.tile.yCoord+(axis==2?sign*distance:0),z=center.tile.zCoord+(axis==1?sign*distance:0);
            if(y<0||y>=world.getHeight())break;
            if(!WirelessAccess.loaded(world,x,y,z)){row.partial=true;break;}
            TileEntity tile=world.getTileEntity(x,y,z);if(tile==null)break;Battery b=read(tile,side);
            if(b==null){if(tile.getClass()==center.tile.getClass())row.partial=true;break;}
            if(!b.groupable||!b.unit.equals(center.unit)||b.source.getClass()!=center.source.getClass()||!seen.add(b.source))break;
            if(distance>RADIUS){row.partial=true;break;}row.batteries.add(b);
        }return row;
    }
    public NBTTagCompound snapshot(WirelessEndpoint e,int dimension){
        NBTTagCompound n=new NBTTagCompound();n.setBoolean("Row",e.batteryRow);String status=WirelessAccess.status(e,dimension).toString();n.setString("Status",status);
        if(!"ONLINE".equals(status)){previousKey="";return n;}
        try{
            Battery center=read(WirelessAccess.tile(e),e.side);if(center==null){n.setString("Status","NO ENERGY DATA");previousKey="";return n;}
            Row selected=new Row();selected.batteries.add(center);
            if(e.batteryRow&&center.groupable){for(int axis=0;axis<3;axis++){Row candidate=row(center,e.side,axis);if(axis==0||candidate.batteries.size()>selected.batteries.size())selected=candidate;}}
            n.setBoolean("Groupable",center.groupable);n.setString("Axis",new String[]{"X","Z","Y"}[selected.axis]);n.setBoolean("Partial",selected.partial);
            long amount=0,capacity=0,input=0,output=0;int count=0;boolean exact=true,ready=true;StringBuilder key=new StringBuilder(e.id.toString()).append(':').append(e.batteryRow);
            for(Battery b:selected.batteries){amount=Math.addExact(amount,b.amount);capacity=Math.addExact(capacity,b.capacity);count+=b.count;
                key.append(':').append(b.tile.xCoord).append(',').append(b.tile.yCoord).append(',').append(b.tile.zCoord).append('@').append(System.identityHashCode(b.source)).append('/').append(b.capacity);
                if(b.bank){input=Math.addExact(input,b.in);output=Math.addExact(output,b.out);continue;}
                long[] flow=BatteryFlowMeter.watch(b.source);if(flow==null){exact=false;ready=false;}else{input=Math.addExact(input,flow[0]);output=Math.addExact(output,flow[1]);ready&=flow[2]>0;}
            }
            long now=center.tile.getWorldObj().getTotalWorldTime();String series=key.toString();boolean net=series.equals(previousKey)&&now>previousTick;
            long change=net?(amount-previousAmount)/(now-previousTick):0;
            n.setBoolean("Available",true);n.setString("Status","ONLINE");n.setString("Unit",center.unit);n.setLong("Amount",amount);n.setLong("Capacity",capacity);n.setInteger("Count",count);
            n.setString("Name",center.bank?"Capacitor network":e.batteryRow?"Battery row":"Battery");n.setBoolean("Network",center.bank);
            n.setBoolean("Exact",exact);n.setBoolean("RatesAvailable",exact&&ready);n.setLong("Input",input);n.setLong("Output",output);n.setBoolean("NetAvailable",net);n.setLong("Net",change);
            previousKey=series;previousAmount=amount;previousTick=now;
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){n.setBoolean("Available",false);n.setString("Status","ENERGY UNAVAILABLE");previousKey="";}
        return n;
    }
}
