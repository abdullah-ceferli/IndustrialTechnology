package dev.industrialtechnology.energy;
import java.util.WeakHashMap;
import net.minecraft.tileentity.TileEntity;
/** Weak, expiring watches exist only while a terminal requests this battery's readings. */
public final class BatteryFlowMeter {
    private static final WeakHashMap<Object,Watch> watches=new WeakHashMap<Object,Watch>();
    private static final class Watch {final FlowWindow window;long refreshed;Watch(long tick){window=new FlowWindow(tick);refreshed=tick;}}
    public static synchronized long[] watch(Object object){
        if(!(object instanceof MeteredBattery)||!(object instanceof TileEntity))return null;TileEntity tile=(TileEntity)object;
        if(tile.getWorldObj()==null||tile.getWorldObj().isRemote||tile.isInvalid())return null;long now=tile.getWorldObj().getTotalWorldTime();
        Watch w=watches.get(object);if(w==null||now<w.refreshed||now-w.refreshed>40){if(watches.size()>=4096)return null;w=new Watch(now);watches.put(object,w);}w.refreshed=now;return w.window.read(now);
    }
    public static synchronized void changed(Object object,long before,long after){
        Watch w=watches.get(object);if(w==null||!(object instanceof TileEntity))return;TileEntity tile=(TileEntity)object;
        if(tile.getWorldObj()==null||tile.getWorldObj().isRemote||tile.isInvalid())return;long now=tile.getWorldObj().getTotalWorldTime();
        if(now<w.refreshed||now-w.refreshed>40){watches.remove(object);return;}w.window.add(now,before,after);
    }
    private BatteryFlowMeter(){}
}
