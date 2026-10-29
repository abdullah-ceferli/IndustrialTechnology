package dev.industrialtechnology.console;
import java.util.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import dev.industrialtechnology.wireless.*;

public class TileConsole extends TileEntity {
    public static final int CHANNELS=16;
    public UUID owner,network;
    public final boolean controller;
    public final ConsoleChannel[] channels=new ConsoleChannel[CHANNELS];
    private int cursor;private boolean registered;
    public TileConsole(){this(false);}
    protected TileConsole(boolean c){controller=c;for(int i=0;i<CHANNELS;i++)channels[i]=new ConsoleChannel();}
    public static final class History extends TileConsole{public History(){super(false);}}
    public static final class Controller extends TileConsole{public Controller(){super(true);}}
    public boolean usable(EntityPlayer p){return p!=null&&p.worldObj==worldObj&&!isInvalid()&&p.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64&&WirelessAccess.loaded(worldObj,xCoord,yCoord,zCoord)&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&(worldObj.isRemote||p.getUniqueID().equals(owner));}
    public WirelessData data(){return WirelessData.get(worldObj);}
    public WirelessEndpoint endpoint(UUID id){if(network==null||owner==null)return null;WirelessEndpoint e=data().devices.get(id);return e!=null&&owner.equals(e.owner)&&network.equals(e.network)&&e.dimension==worldObj.provider.dimensionId?e:null;}
    public boolean sourceKind(WirelessEndpoint e){return e!=null&&(controller?e.kind==WirelessEndpoint.Kind.AUTOMATION:e.kind==WirelessEndpoint.Kind.MONITOR||e.kind==WirelessEndpoint.Kind.CONTROL);}
    public boolean link(EntityPlayer p){
        if(!usable(p))return false;UUID selected=WirelessAccess.terminal(p,p.getHeldItem(),true);if(selected==null){p.addChatMessage(new ChatComponentText("Use your own Wireless Terminal."));return false;}
        if(network!=null&&!network.equals(selected)){p.addChatMessage(new ChatComponentText("This block is already linked to another terminal network."));return false;}
        network=selected;markDirty();
        if(!controller&&channels[0].source==null){List<WirelessEndpoint> all=data().linked(network);all.sort(Comparator.comparing(e->e.name));int i=0;for(WirelessEndpoint e:all)if(e.kind==WirelessEndpoint.Kind.MONITOR&&e.dimension==worldObj.provider.dimensionId&&i<CHANNELS)channels[i++].source=e.id;}
        p.addChatMessage(new ChatComponentText("Linked "+(controller?"Refinery Controller":"Tank History Monitor")+". Right-click the block to configure it."));return true;
    }
    @Override public void updateEntity(){if(worldObj!=null&&!worldObj.isRemote&&!registered){registered=true;ConsoleService.register(this);}}
    public void service(){if(worldObj==null||worldObj.isRemote||network==null||owner==null||!data().owns(network,owner))return;int row=cursor++%CHANNELS;ConsoleChannel c=channels[row];if(c.source==null)return;long now=worldObj.getTotalWorldTime(),interval=controller?20:100;if(c.lastPoll!=Long.MIN_VALUE&&now-c.lastPoll<interval)return;c.lastPoll=now;poll(row,now);}
    public void poll(int row,long now){
        if(row<0||row>=CHANNELS)return;ConsoleChannel c=channels[row];WirelessEndpoint source=endpoint(c.source);
        SensorReading r=new SensorReading();if(sourceKind(source))r=c.energy?SensorReading.energy(source,worldObj.provider.dimensionId):SensorReading.fluid(source,worldObj.provider.dimensionId,c.tank);
        else r.status="SENSOR DISCONNECTED";
        if(!controller&&c.autoMetric&&!r.available&&!c.energy&&sourceKind(source)){SensorReading energy=SensorReading.energy(source,worldObj.provider.dimensionId);if(energy.available){c.energy=true;r=energy;markDirty();}}
        c.reading=r;
        if(!controller){
            if(r.available){String key=r.unit+":"+(r.identifier==null||r.identifier.isEmpty()?c.expected:r.identifier);if(!key.equals(c.seriesKey)){c.history.clear();c.seriesKey=key;}if(r.identifier!=null&&!r.identifier.isEmpty())c.expected=r.identifier;c.history.add(now,r.amount);}
            else c.history.add(now,TrendSeries.GAP);
            c.message=r.status;markDirty();return;
        }
        if(!c.enabled){c.message="Paused";ConsoleService.release(this,row,true);return;}
        WirelessEndpoint target=endpoint(c.target);
        if(target==null||target.kind!=WirelessEndpoint.Kind.CONTROL){fail(row,"YELLOW DISCONNECTED");return;}
        if(ConsoleService.blueControls(data(),network,c.target)){ConsoleService.release(this,row,true);c.latched=false;c.message="Pause the Blue program for this Yellow";return;}
        if(!ConsoleService.claim(this,row,c.target)){c.message="Yellow owned by another controller rule";c.latched=false;return;}
        if(!r.available){fail(row,"Sensor: "+r.status);return;}
        if(WirelessAccess.status(target,worldObj.provider.dimensionId)!=WirelessAccess.Status.ONLINE){fail(row,"Yellow is offline or disabled");return;}
        String identity=r.identifier==null?"":r.identifier;if(!identity.isEmpty()){if(c.expected.isEmpty()){c.expected=identity;markDirty();}else if(!c.expected.equals(identity)){fail(row,"Fluid/type changed - reselect sensor");return;}}
        boolean next=c.manual<0?TankBand.next(c.refill,c.latched,r.amount,c.low,c.high):c.manual==1;
        if(next!=c.latched){c.latched=next;markDirty();}
        if(!setOutput(c,next)){fail(row,"Yellow unavailable");return;}
        c.message=(next?"ON":"OFF")+(c.manual<0?c.refill?" - refill":" - release":" - manual");
    }
    private void fail(int row,String reason){ConsoleChannel c=channels[row];c.latched=false;c.message=reason;setOutputIfClaimed(row,false);markDirty();}
    private void setOutputIfClaimed(int row,boolean state){ConsoleChannel c=channels[row]; // Claiming again cannot seize a target already held elsewhere.
        if(c.target!=null&&ConsoleService.owns(this,row,c.target))setOutput(c,state);
    }
    public boolean setOutput(ConsoleChannel channel,boolean on){
        if(worldObj==null||worldObj.isRemote||network==null)return false;
        try{WirelessEndpoint target=endpoint(channel.target);if(target==null||target.kind!=WirelessEndpoint.Kind.CONTROL)return false;if(!on){if(target.powered||target.pulseEnd!=0)WirelessAccess.off(data(),target);return true;}if(WirelessAccess.status(target,worldObj.provider.dimensionId)!=WirelessAccess.Status.ONLINE)return false;boolean actual=WirelessAccess.signal(target,worldObj.getTotalWorldTime());if(actual==on&&(!on||target.pulseEnd==0))return true;return WirelessAccess.command(data(),target,on?"ON":"OFF",0);}catch(RuntimeException|LinkageError unavailable){return false;}
    }
    public void stop(int row){ConsoleService.release(this,row,true);channels[row].latched=false;}
    public void stopAll(){for(int i=0;i<CHANNELS;i++)ConsoleService.release(this,i,true);}
    @Override public void invalidate(){if(!worldObjIsClient())ConsoleService.remove(this);registered=false;super.invalidate();}
    @Override public void onChunkUnload(){if(!worldObjIsClient())ConsoleService.remove(this);registered=false;super.onChunkUnload();}
    private boolean worldObjIsClient(){return worldObj==null||worldObj.isRemote;}
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);if(owner!=null)n.setString("Owner",owner.toString());if(network!=null)n.setString("Network",network.toString());NBTTagList list=new NBTTagList();for(ConsoleChannel c:channels)list.appendTag(c.write());n.setTag("Channels",list);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);owner=WirelessData.uuid(n.getString("Owner"));network=WirelessData.uuid(n.getString("Network"));NBTTagList list=n.getTagList("Channels",10);for(int i=0;i<CHANNELS;i++)channels[i]=i<list.tagCount()?ConsoleChannel.read(list.getCompoundTagAt(i)):new ConsoleChannel();}
    public NBTTagCompound itemSettings(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);n.removeTag("x");n.removeTag("y");n.removeTag("z");n.removeTag("id");NBTTagList list=n.getTagList("Channels",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound row=list.getCompoundTagAt(i);row.removeTag("Times");row.removeTag("Values");row.setBoolean("Enabled",false);row.setBoolean("Latched",false);}return n;}
}
