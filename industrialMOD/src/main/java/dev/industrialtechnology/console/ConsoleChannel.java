package dev.industrialtechnology.console;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.wireless.WirelessData;
public final class ConsoleChannel {
    public UUID source,target;
    public boolean energy,autoMetric=true,enabled,refill,latched;
    public int tank,manual=-1;
    public long low=1800000,high=2000000;
    public String expected="",seriesKey="",message="Choose a sensor";
    public final TrendSeries history=new TrendSeries();
    public transient SensorReading reading=new SensorReading();
    public transient long lastPoll=Long.MIN_VALUE;
    public void reset(){latched=false;expected="";seriesKey="";history.clear();lastPoll=Long.MIN_VALUE;reading=new SensorReading();}
    public NBTTagCompound write(){NBTTagCompound n=new NBTTagCompound();if(source!=null)n.setString("Source",source.toString());if(target!=null)n.setString("Target",target.toString());n.setBoolean("Energy",energy);n.setBoolean("Auto",autoMetric);n.setBoolean("Enabled",enabled);n.setBoolean("Refill",refill);n.setBoolean("Latched",latched);n.setInteger("Tank",tank);n.setInteger("Manual",manual);n.setLong("Low",low);n.setLong("High",high);n.setString("Expected",expected);n.setString("SeriesKey",seriesKey);
        long[] times=history.times(),values=history.values(); // NBT 1.7.10 has no long-array tag.
        int[] th=new int[times.length*2],vh=new int[values.length*2];for(int i=0;i<times.length;i++){th[i*2]=(int)(times[i]>>>32);th[i*2+1]=(int)times[i];vh[i*2]=(int)(values[i]>>>32);vh[i*2+1]=(int)values[i];}n.setIntArray("Times",th);n.setIntArray("Values",vh);return n;}
    public static ConsoleChannel read(NBTTagCompound n){ConsoleChannel c=new ConsoleChannel();c.source=WirelessData.uuid(n.getString("Source"));c.target=WirelessData.uuid(n.getString("Target"));c.energy=n.getBoolean("Energy");c.autoMetric=!n.hasKey("Auto")||n.getBoolean("Auto");c.enabled=n.getBoolean("Enabled");c.refill=n.getBoolean("Refill");c.latched=n.getBoolean("Latched");c.manual=n.hasKey("Manual")?Math.max(-1,Math.min(1,n.getInteger("Manual"))):-1;c.tank=Math.max(0,Math.min(63,n.getInteger("Tank")));if(TankBand.valid(n.getLong("Low"),n.getLong("High"))){c.low=n.getLong("Low");c.high=n.getLong("High");}else c.enabled=false;c.expected=WirelessData.clean(n.getString("Expected"),128);c.seriesKey=WirelessData.clean(n.getString("SeriesKey"),160);
        int[] th=n.getIntArray("Times"),vh=n.getIntArray("Values");int size=Math.min(TrendSeries.LIMIT,Math.min(th.length,vh.length)/2);for(int i=0;i<size;i++)c.history.add(((long)th[i*2]<<32)|(th[i*2+1]&0xffffffffL),((long)vh[i*2]<<32)|(vh[i*2+1]&0xffffffffL));return c;}
}
