package dev.industrialtechnology.automation;
import java.util.*;
import net.minecraft.item.Item;
import dev.industrialtechnology.wireless.*;
/** One bounded read per endpoint poll; the last rising action for a target wins within a script. */
public final class ScriptEngine {
    public static ScriptCompiler.Resolver resolver(final WirelessData data,final WirelessEndpoint source){return new ScriptCompiler.Resolver(){
        public String item(String id){if(id.length()>128||id.indexOf(':')<1||!Item.itemRegistry.containsKey(id))throw new IllegalArgumentException("Unknown item \""+id+"\".");return id;}
        public String fluid(String id){return FluidNames.resolve(id);}
        public UUID control(String name){
            WirelessEndpoint result=null;UUID explicit=name.startsWith("id:")?WirelessData.uuid(name.substring(3)):null;
            for(WirelessEndpoint e:data.linked(source.network))if(e.kind==WirelessEndpoint.Kind.CONTROL&&e.owner.equals(source.owner)&&(explicit!=null?explicit.equals(e.id):name.equals(e.name))){if(result!=null)throw new IllegalArgumentException("More than one Yellow Stick has that name. Rename it or use id:UUID.");result=e;}
            if(result==null)throw new IllegalArgumentException("Unknown control: "+name);
            if(result.dimension!=source.dimension)throw new IllegalArgumentException("Yellow Stick must be in the same dimension.");return result.id;
        }
    };}
    public static boolean save(WirelessData data,WirelessEndpoint e,String text){
        try{List<ScriptRule> parsed=ScriptCompiler.compile(text,WirelessConfig.scriptLength,resolver(data,e));
            for(int i=0;i<parsed.size();i++)if(i<e.scriptRules.size()&&parsed.get(i).same(e.scriptRules.get(i)))parsed.get(i).latched=e.scriptRules.get(i).latched;
            e.script=text.replace("\r\n","\n").replace('\r','\n');e.scriptRules=parsed;e.scriptChecked=true;e.scriptRevision++;e.scriptError="";e.scriptErrorLine=0;e.lastPoll=Long.MIN_VALUE;data.markDirty();return true;
        }catch(ScriptCompiler.Error error){e.scriptError=error.display();e.scriptErrorLine=error.line;return false;}
    }
    public static void poll(WirelessData data,WirelessEndpoint e){
        if(!e.scriptEnabled){e.automationStatus="Script disabled";return;}
        if(e.scriptRules.isEmpty()){e.automationStatus="Save a valid script first";return;}
        if(!e.scriptChecked){
            ScriptCompiler.Resolver resolver=resolver(data,e);e.scriptChecked=true;
            for(ScriptRule r:e.scriptRules)try{if(r.kind==ScriptRule.Kind.ITEM)resolver.item(r.identifier);if(r.kind==ScriptRule.Kind.FLUID)resolver.fluid(r.identifier);}
            catch(IllegalArgumentException unavailable){e.scriptEnabled=false;e.scriptError="Line "+r.line+": "+unavailable.getMessage();e.scriptErrorLine=r.line;e.automationStatus="INVALID SAVED PROGRAM";data.markDirty();return;}
        }
        boolean needsFluid=false;for(ScriptRule r:e.scriptRules)if(r.kind==ScriptRule.Kind.FLUID||r.kind==ScriptRule.Kind.TANK_EMPTY||r.kind==ScriptRule.Kind.TANK_FULL)needsFluid=true;
        MachineInspector.Reading reading=MachineInspector.inspect(e,e.dimension,needsFluid);
        if(reading.status!=WirelessAccess.Status.ONLINE){e.automationStatus=needsFluid&&(reading.status==WirelessAccess.Status.UNSUPPORTED||reading.status==WirelessAccess.Status.DATA_UNAVAILABLE)?"FLUID DATA UNAVAILABLE":reading.status.toString();return;}
        Boolean[] values=new Boolean[e.scriptRules.size()];LinkedHashMap<UUID,ScriptRule> actions=new LinkedHashMap<UUID,ScriptRule>();
        try{for(int i=0;i<values.length;i++)values[i]=e.scriptRules.get(i).evaluate(reading);}catch(RuntimeException|LinkageError badAPI){e.automationStatus="DATA UNAVAILABLE: machine predicate";return;}
        int waiting=0,truth=0;boolean changed=false,fluidMissing=false;
        for(int i=0;i<values.length;i++){
            ScriptRule r=e.scriptRules.get(i);WirelessEndpoint target=data.devices.get(r.target);boolean authorized=target!=null&&target.kind==WirelessEndpoint.Kind.CONTROL&&target.owner.equals(e.owner)&&target.network.equals(e.network)&&target.dimension==e.dimension;
            if(values[i]==null){waiting++;if(r.kind==ScriptRule.Kind.FLUID||r.kind==ScriptRule.Kind.TANK_EMPTY||r.kind==ScriptRule.Kind.TANK_FULL)fluidMissing=true;continue;}
            if(!authorized||WirelessAccess.status(target,e.dimension)!=WirelessAccess.Status.ONLINE){waiting++;continue;}
            boolean value=values[i];if(value)truth++;
            if(value&&!r.latched)actions.put(target.id,r);
            if(value!=r.latched){r.latched=value;changed=true;}
        }
        for(ScriptRule r:actions.values())WirelessAccess.command(data,data.devices.get(r.target),r.action.name(),r.seconds);
        if(changed)data.markDirty();e.automationStatus=fluidMissing?"FLUID DATA UNAVAILABLE":waiting>0?"WAITING: "+waiting+" rule(s); "+truth+" true":truth+" / "+values.length+" conditions true";
    }
    private ScriptEngine(){}
}
