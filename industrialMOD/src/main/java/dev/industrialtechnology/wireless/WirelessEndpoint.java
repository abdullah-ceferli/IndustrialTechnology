package dev.industrialtechnology.wireless;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
public final class WirelessEndpoint {
    public enum Kind { MONITOR, CONTROL, AUTOMATION }
    public UUID id=UUID.randomUUID(), network, owner, tileToken;
    public Kind kind=Kind.MONITOR;
    public int dimension,x,y,z,side,pulseSeconds=5;
    public String name="Device", block="", machine="";
    public boolean enabled=true, pulseMode, powered, physical;
    public boolean hasPhysicalStick(){return kind==Kind.CONTROL||physical;}
    public long pulseEnd;
    public boolean codeMode,scriptEnabled;
    public String script="";
    public java.util.List<dev.industrialtechnology.automation.ScriptRule> scriptRules=new java.util.ArrayList<dev.industrialtechnology.automation.ScriptRule>();
    public int scriptRevision;
    public transient boolean scriptChecked;
    public transient String scriptError="";public transient int scriptErrorLine;
    public final AutomationRule[] rules=new AutomationRule[8];
    public transient long lastPoll=Long.MIN_VALUE;
    public transient String automationStatus="Waiting for inventory";
    public WirelessEndpoint(){for(int i=0;i<rules.length;i++)rules[i]=new AutomationRule();}
    public NBTTagCompound write(){
        NBTTagCompound n=new NBTTagCompound();n.setString("Id",id.toString());n.setString("Network",network.toString());n.setString("Owner",owner.toString());
        if(tileToken!=null)n.setString("TileToken",tileToken.toString());
        n.setInteger("Kind",kind.ordinal());n.setInteger("Dimension",dimension);n.setInteger("X",x);n.setInteger("Y",y);n.setInteger("Z",z);n.setInteger("Side",side);
        n.setString("Name",name);n.setString("Block",block);n.setString("Machine",machine);n.setBoolean("Enabled",enabled);n.setBoolean("Physical",physical);
        n.setBoolean("PulseMode",pulseMode);n.setInteger("PulseSeconds",pulseSeconds);n.setBoolean("Powered",powered);n.setLong("PulseEnd",pulseEnd);
        NBTTagList list=new NBTTagList();for(AutomationRule r:rules)list.appendTag(r.write());n.setTag("Rules",list);
        n.setString("Script",script);n.setBoolean("CodeMode",codeMode);n.setBoolean("ScriptEnabled",scriptEnabled);n.setInteger("ScriptRevision",scriptRevision);
        NBTTagList compiled=new NBTTagList();for(dev.industrialtechnology.automation.ScriptRule r:scriptRules)compiled.appendTag(r.write());n.setTag("ScriptRules",compiled);return n;
    }
    public static WirelessEndpoint read(NBTTagCompound n){
        WirelessEndpoint e=new WirelessEndpoint();e.id=WirelessData.uuid(n.getString("Id"));e.network=WirelessData.uuid(n.getString("Network"));e.owner=WirelessData.uuid(n.getString("Owner"));
        if(e.id==null||e.network==null||e.owner==null)return null;e.tileToken=WirelessData.uuid(n.getString("TileToken"));
        e.kind=Kind.values()[Math.max(0,Math.min(2,n.getInteger("Kind")))];e.dimension=n.getInteger("Dimension");e.x=n.getInteger("X");e.y=n.getInteger("Y");e.z=n.getInteger("Z");
        e.side=Math.max(0,Math.min(5,n.getInteger("Side")));e.name=WirelessData.clean(n.getString("Name"),32);e.block=WirelessData.clean(n.getString("Block"),128);e.machine=WirelessData.clean(n.getString("Machine"),64);
        e.enabled=n.getBoolean("Enabled");e.physical=n.getBoolean("Physical");e.pulseMode=n.getBoolean("PulseMode");e.pulseSeconds=Math.max(5,Math.min(10,n.getInteger("PulseSeconds")));
        e.powered=n.getBoolean("Powered");e.pulseEnd=Math.max(0,n.getLong("PulseEnd"));NBTTagList rules=n.getTagList("Rules",10);
        for(int i=0;i<Math.min(8,rules.tagCount());i++)e.rules[i]=AutomationRule.read(rules.getCompoundTagAt(i));
        e.codeMode=n.getBoolean("CodeMode");e.scriptEnabled=n.getBoolean("ScriptEnabled");e.script=n.getString("Script");e.scriptRevision=Math.max(0,n.getInteger("ScriptRevision"));
        NBTTagList compiled=n.getTagList("ScriptRules",10);boolean valid=e.script.length()<=dev.industrialtechnology.automation.ScriptCompiler.HARD_LENGTH&&compiled.tagCount()<=8;
        for(int i=0;i<Math.min(8,compiled.tagCount());i++){dev.industrialtechnology.automation.ScriptRule r=dev.industrialtechnology.automation.ScriptRule.read(compiled.getCompoundTagAt(i));if(r==null)valid=false;else e.scriptRules.add(r);}
        if(e.script.length()>dev.industrialtechnology.automation.ScriptCompiler.HARD_LENGTH)e.script="";
        if(!valid){e.scriptEnabled=false;e.scriptRules.clear();e.scriptError="Saved program is invalid; open Code and save again.";}return e;
    }
}
