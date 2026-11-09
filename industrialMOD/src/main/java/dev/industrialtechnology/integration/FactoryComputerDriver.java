package dev.industrialtechnology.integration;
import java.util.*;
import dev.industrialtechnology.capacitor.TileCapacitorBank;
import dev.industrialtechnology.console.*;
import dev.industrialtechnology.wireless.*;
import li.cil.oc.api.Driver;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.SidedBlock;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.*;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
/** Passive OC adapter. Reading a component never transfers energy/items or commands a Yellow. */
public final class FactoryComputerDriver implements SidedBlock {
    public static void register(){Driver.add(new FactoryComputerDriver());}
    public boolean worksWith(World w,int x,int y,int z,ForgeDirection side){
        if(!WirelessAccess.loaded(w,x,y,z))return false;TileEntity t=w.getTileEntity(x,y,z);
        return t instanceof TileCapacitorBank||t instanceof TileConsole||t instanceof TileRedstoneRelay&&green((TileRedstoneRelay)t)!=null;
    }
    public li.cil.oc.api.network.ManagedEnvironment createEnvironment(World w,int x,int y,int z,ForgeDirection side){return worksWith(w,x,y,z,side)?new Environment(w,w.getTileEntity(x,y,z)):null;}
    private static WirelessEndpoint green(TileRedstoneRelay torch){
        if(torch.getWorldObj()==null||torch.getWorldObj().isRemote)return null;
        WirelessData data=WirelessData.get(torch.getWorldObj());WirelessEndpoint e=data.devices.get(torch.endpoint);
        return e!=null&&e.kind==WirelessEndpoint.Kind.MONITOR&&e.owner.equals(torch.placer)&&data.owns(e.network,e.owner)?e:null;
    }
    public static final class Environment extends li.cil.oc.api.prefab.ManagedEnvironment implements NamedBlock {
        private final World world;private final TileEntity target;private final int x,y,z;private final BatteryMonitor batteries=new BatteryMonitor();
        public Environment(World w,TileEntity t){world=w;target=t;x=t.xCoord;y=t.yCoord;z=t.zCoord;setNode(Network.newNode(this,Visibility.Network).withComponent("industrialtech",Visibility.Network).create());}
        public String preferredName(){return "industrialtech";}public int priority(){return 10;}
        private boolean loaded(){return !target.isInvalid()&&WirelessAccess.loaded(world,x,y,z)&&world.getTileEntity(x,y,z)==target;}
        private WirelessEndpoint endpoint(){return loaded()&&target instanceof TileRedstoneRelay?green((TileRedstoneRelay)target):null;}
        @Callback(doc="function():table -- Component kind and loaded status. Calls run on the server thread.")
        public Object[] getInfo(Context context,Arguments args){Map<String,Object> out=new LinkedHashMap<String,Object>();out.put("available",loaded());out.put("kind",target instanceof TileCapacitorBank?"capacitor_network":target instanceof TileConsole?((TileConsole)target).controller?"refinery_controller":"history_monitor":"green_monitor");return new Object[]{out};}
        @Callback(doc="function():table -- Actual HE/RF storage and independent supported transfer readings; exact values are also strings.")
        public Object[] getEnergy(Context context,Arguments args){
            Map<String,Object> out=new LinkedHashMap<String,Object>();out.put("available",false);if(!loaded())return new Object[]{out};NBTTagCompound n;
            if(target instanceof TileCapacitorBank){NBTTagCompound bank=((TileCapacitorBank)target).snapshot();n=new NBTTagCompound();n.setBoolean("Available",bank.getLong("Capacity")>0);n.setString("Status","ONLINE");n.setLong("Amount",bank.getLong("Stored"));n.setLong("Capacity",bank.getLong("Capacity"));n.setInteger("Count",bank.getInteger("Banks"));n.setString("Unit","RF");n.setLong("Input",bank.getLong("In"));n.setLong("Output",bank.getLong("Out"));n.setBoolean("RatesAvailable",true);}
            else{WirelessEndpoint e=endpoint();if(e==null)return new Object[]{out};n=batteries.snapshot(e,world.provider.dimensionId);}
            out.put("available",n.getBoolean("Available"));out.put("status",n.getString("Status"));out.put("unit",n.getString("Unit"));
            for(String key:new String[]{"Amount","Capacity","Input","Output"}){String name=key.equals("Amount")?"stored":key.toLowerCase(Locale.ROOT);long value=n.getLong(key);out.put(name,value);out.put(name+"Exact",Long.toString(value));}
            out.put("ratesAvailable",n.getBoolean("RatesAvailable"));out.put("batteries",n.getInteger("Count"));out.put("row",n.getBoolean("Row"));out.put("partial",n.getBoolean("Partial"));return new Object[]{out};
        }
        @Callback(doc="function():table -- Supported fluid tanks exposed to this linked Green stick; amounts remain mB.")
        public Object[] getTanks(Context context,Arguments args){
            Map<Integer,Object> out=new LinkedHashMap<Integer,Object>();WirelessEndpoint e=endpoint();if(e==null)return new Object[]{out};MachineInspector.Reading reading=MachineInspector.inspect(e,world.provider.dimensionId,true);
            if(reading.status!=WirelessAccess.Status.ONLINE||!reading.fluidAvailable)return new Object[]{out};int index=0;
            for(MachineInspector.Tank tank:reading.tanks){Map<String,Object> t=new LinkedHashMap<String,Object>();t.put("name",tank.name);t.put("id",tank.identifier);t.put("amount",tank.amount);t.put("capacity",tank.capacity);t.put("unit",tank.unit);out.put(++index,t);}return new Object[]{out};
        }
        @Callback(doc="function():table -- The monitor/controller's sixteen cached channels, including availability and status.")
        public Object[] getChannels(Context context,Arguments args){
            Map<Integer,Object> out=new LinkedHashMap<Integer,Object>();if(!loaded()||!(target instanceof TileConsole))return new Object[]{out};TileConsole console=(TileConsole)target;
            for(int i=0;i<TileConsole.CHANNELS;i++){ConsoleChannel channel=console.channels[i];SensorReading r=channel.reading;Map<String,Object> row=new LinkedHashMap<String,Object>();WirelessEndpoint source=console.endpoint(channel.source);row.put("name",source==null?"Unlinked":source.name);row.put("status",channel.message);row.put("enabled",channel.enabled);row.put("output",channel.latched);row.put("available",r!=null&&r.available);if(r!=null){row.put("amount",r.amount);row.put("capacity",r.capacity);row.put("unit",r.unit);}out.put(i+1,row);}return new Object[]{out};
        }
    }
}
