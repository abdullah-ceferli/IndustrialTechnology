package dev.industrialtechnology.console;
import java.util.*;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.industrialtechnology.wireless.*;

/** Eight console visits per server tick globally, with no chunk tickets or world searches. */
public final class ConsoleService {
    private static final Set<TileConsole> loaded=Collections.newSetFromMap(new IdentityHashMap<TileConsole,Boolean>());
    private static final Map<UUID,Claim> claims=new HashMap<UUID,Claim>();
    private static List<TileConsole> queue=new ArrayList<TileConsole>();private static boolean dirty;private static int cursor;
    private static final class Claim{final TileConsole tile;final int row;Claim(TileConsole t,int r){tile=t;row=r;}}
    public static void register(TileConsole t){if(loaded.add(t))dirty=true;}
    public static void remove(TileConsole t){t.stopAll();if(loaded.remove(t))dirty=true;}
    public static boolean claim(TileConsole t,int row,UUID target){Claim c=claims.get(target);if(c!=null&&(c.tile!=t||c.row!=row))return false;claims.put(target,new Claim(t,row));return true;}
    public static boolean owns(TileConsole t,int row,UUID target){Claim c=claims.get(target);return c!=null&&c.tile==t&&c.row==row;}
    public static void release(TileConsole t,int row,boolean off){
        ConsoleChannel channel=t.channels[row];Claim c=claims.get(channel.target);if(c==null||c.tile!=t||c.row!=row)return;
        claims.remove(channel.target);if(off)t.setOutput(channel,false);
    }
    public static void clear(){loaded.clear();claims.clear();queue.clear();dirty=false;cursor=0;}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){if(event.phase!=TickEvent.Phase.END)return;if(dirty){queue=new ArrayList<TileConsole>(loaded);dirty=false;cursor=0;}for(int i=0;i<Math.min(8,queue.size());i++){if(cursor>=queue.size())cursor=0;TileConsole t=queue.get(cursor++);if(t.getWorldObj()!=null&&!t.isInvalid()&&WirelessAccess.loaded(t.getWorldObj(),t.xCoord,t.yCoord,t.zCoord))t.service();}}
    public static boolean blueControls(WirelessData data,UUID network,UUID target){
        if(target==null)return false;for(WirelessEndpoint e:data.linked(network))if(e.kind==WirelessEndpoint.Kind.AUTOMATION&&e.enabled){
            if(e.codeMode&&e.scriptEnabled){for(dev.industrialtechnology.automation.ScriptRule r:e.scriptRules)if(target.equals(r.target))return true;}
            else if(!e.codeMode)for(AutomationRule r:e.rules)if(r.enabled&&target.equals(r.target))return true;
        }return false;
    }
}
