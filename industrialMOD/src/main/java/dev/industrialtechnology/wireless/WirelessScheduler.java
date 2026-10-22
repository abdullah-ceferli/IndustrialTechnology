package dev.industrialtechnology.wireless;
import java.util.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.world.WorldServer;
/** Bounded round-robin polling. One shared inventory summary serves all eight rules. */
public final class WirelessScheduler {
    private WirelessData current;private int revision=-1,cursor;private List<WirelessEndpoint> queue=Collections.emptyList();
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;WorldServer home=DimensionManager.getWorld(0);if(home==null)return;WirelessData data=WirelessData.get(home);
        if(data!=current||revision!=data.revision){current=data;revision=data.revision;queue=new ArrayList<WirelessEndpoint>(data.devices.values());cursor=0;}
        for(int i=0;i<Math.min(WirelessConfig.pollsPerTick,queue.size());i++){
            if(cursor>=queue.size())cursor=0;WirelessEndpoint endpoint=queue.get(cursor++);
            if(endpoint.kind!=WirelessEndpoint.Kind.AUTOMATION||!endpoint.enabled)continue;WorldServer w=DimensionManager.getWorld(endpoint.dimension);if(w==null)continue;
            long now=w.getTotalWorldTime();if(endpoint.lastPoll!=Long.MIN_VALUE&&now-endpoint.lastPoll<WirelessConfig.pollTicks)continue;
            endpoint.lastPoll=now;poll(data,endpoint);
        }
    }
    public static void poll(WirelessData data,WirelessEndpoint endpoint){
        ReactorAutomation.poll(data,endpoint);
        if(endpoint.codeMode){dev.industrialtechnology.automation.ScriptEngine.poll(data,endpoint);return;}
        MachineInspector.Reading reading=MachineInspector.inspect(endpoint,endpoint.dimension,false);
        if(reading.status!=WirelessAccess.Status.ONLINE||reading.inventory==null){endpoint.automationStatus=reading.status.toString();return;}
        int enabled=0,trueRules=0;boolean changed=false;boolean[] values=new boolean[8];
        try{for(int i=0;i<8;i++)if(endpoint.rules[i].enabled)values[i]=endpoint.rules[i].evaluate(reading.inventory);}
        catch(RuntimeException|LinkageError unavailable){endpoint.automationStatus="DATA UNAVAILABLE: inventory predicate";return;}
        for(int i=0;i<8;i++){AutomationRule rule=endpoint.rules[i];if(!rule.enabled)continue;enabled++;boolean value=values[i];if(value)trueRules++;
            if(value&&!rule.latched){WirelessEndpoint target=data.devices.get(rule.target);
                if(target!=null&&target.network.equals(endpoint.network)&&target.owner.equals(endpoint.owner)&&target.dimension==endpoint.dimension)WirelessAccess.activate(data,target);
            }
            if(rule.latched!=value){rule.latched=value;changed=true;}
        }
        if(changed)data.markDirty();endpoint.automationStatus=trueRules+" / "+enabled+" conditions true";
    }
}
