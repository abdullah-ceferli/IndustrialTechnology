package dev.industrialtechnology.console;
import java.util.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.wireless.*;

public final class ConsoleContainer extends Container {
    public final TileConsole tile;
    public int selected,page,picker,pickerPage;
    private int ticks,actions;private long actionTick=Long.MIN_VALUE;
    public String notice="";
    public ConsoleContainer(TileConsole t){tile=t;}
    @Override public boolean canInteractWith(EntityPlayer p){return tile.usable(p);}
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int index){return null;}
    public boolean allow(){long now=tile.getWorldObj().getTotalWorldTime();if(now!=actionTick){actionTick=now;actions=0;}return ++actions<=8;}
    @Override public void addCraftingToCrafters(ICrafting c){super.addCraftingToCrafters(c);if(c instanceof EntityPlayerMP)send((EntityPlayerMP)c);}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();if(tile.getWorldObj().isRemote||++ticks<10)return;ticks=0;for(Object o:crafters)if(o instanceof EntityPlayerMP)send((EntityPlayerMP)o);}
    public void send(EntityPlayerMP p){IndustrialTechnology.network.sendTo(new ConsoleSnapshot(tile,windowId,snapshot()),p);}
    public List<WirelessEndpoint> choices(){List<WirelessEndpoint> list=new ArrayList<WirelessEndpoint>();if(tile.network!=null)for(WirelessEndpoint e:tile.data().linked(tile.network))if(tile.owner.equals(e.owner)&&e.dimension==tile.getWorldObj().provider.dimensionId&&(picker==2?e.kind==WirelessEndpoint.Kind.CONTROL:tile.sourceKind(e)))list.add(e);list.sort(Comparator.comparing((WirelessEndpoint e)->e.name).thenComparing(e->e.id.toString()));return list;}
    private String name(UUID id,String fallback){WirelessEndpoint e=tile.endpoint(id);return e==null?fallback:e.name;}
    public NBTTagCompound snapshot(){
        NBTTagCompound n=new NBTTagCompound();n.setBoolean("Controller",tile.controller);n.setBoolean("Linked",tile.network!=null&&tile.data().owns(tile.network,tile.owner));n.setInteger("Selected",selected);n.setInteger("Page",page);n.setInteger("Picker",picker);n.setLong("Now",tile.getWorldObj().getTotalWorldTime());n.setString("Notice",notice);
        NBTTagList rows=new NBTTagList();for(int i=page*7;i<Math.min(16,page*7+7);i++){ConsoleChannel c=tile.channels[i];NBTTagCompound row=new NBTTagCompound();row.setInteger("Index",i);row.setString("Name",name(c.source,"Empty channel"));row.setString("Status",c.message);row.setBoolean("Enabled",c.enabled);row.setBoolean("On",c.latched);rows.appendTag(row);}n.setTag("Rows",rows);
        ConsoleChannel c=tile.channels[selected];NBTTagCompound row=c.write();row.setTag("Reading",c.reading.write());row.setString("Message",c.message);double rate=c.history.ratePerSecond();row.setBoolean("RateKnown",!Double.isNaN(rate)&&!Double.isInfinite(rate));if(!Double.isNaN(rate)&&!Double.isInfinite(rate))row.setDouble("Rate",rate);n.setTag("Row",row);n.setString("SourceName",name(c.source,tile.controller?"Choose Blue sensor":"Choose Green / Yellow sensor"));n.setString("TargetName",name(c.target,"Choose Yellow output"));
        if(picker>0){List<WirelessEndpoint> all=choices();int pages=Math.max(1,(all.size()+6)/7);pickerPage=Math.min(pickerPage,pages-1);n.setInteger("PickerPage",pickerPage);n.setInteger("PickerPages",pages);NBTTagList list=new NBTTagList();for(int i=pickerPage*7;i<Math.min(all.size(),pickerPage*7+7);i++){WirelessEndpoint e=all.get(i);NBTTagCompound x=new NBTTagCompound();x.setString("Id",e.id.toString());x.setString("Name",e.name);x.setString("Status",WirelessAccess.status(e,tile.getWorldObj().provider.dimensionId).toString());list.appendTag(x);}n.setTag("Choices",list);}return n;
    }
    public void action(int action,int row,int value,long low,long high,String text){
        if(action==0){if(row>=0&&row<16){selected=row;page=row/7;picker=0;notice="";}return;}
        if(action==1){page=Math.max(0,Math.min(2,value));return;}
        if(row!=selected)return;ConsoleChannel c=tile.channels[selected];notice="";
        if(action==2||action==3){if(tile.network==null){notice="Link with your Wireless Terminal first";return;}if(action==3&&!tile.controller)return;picker=action==2?1:2;pickerPage=0;return;}
        if(action==4){pickerPage=Math.max(0,Math.min(18,value));return;}
        if(action==14){picker=0;return;}
        if(action==5){if(picker==0)return;WirelessEndpoint e=tile.endpoint(WirelessData.uuid(text));if(e==null||(picker==2?e.kind!=WirelessEndpoint.Kind.CONTROL:!tile.sourceKind(e))){notice="Invalid or foreign sensor";return;}tile.stop(selected);c.enabled=false;if(picker==1){c.source=e.id;c.autoMetric=true;c.energy=e.kind==WirelessEndpoint.Kind.CONTROL;c.tank=0;c.reset();}else c.target=e.id;picker=0;tile.markDirty();return;}
        if(action==12){tile.stop(selected);tile.channels[selected]=new ConsoleChannel();tile.markDirty();return;}
        if(action==13){for(int i=0;i<16;i++){tile.stop(i);tile.channels[i].enabled=false;}tile.markDirty();return;}
        if(action==6){tile.stop(selected);c.enabled=false;if(c.reading.tanks<1){notice="Wait for tank data";return;}c.tank=Math.floorMod(c.tank+Math.max(-1,Math.min(1,value)),c.reading.tanks);c.reset();tile.markDirty();return;}
        if(action==7){tile.stop(selected);c.enabled=false;c.energy=!c.energy;c.autoMetric=false;c.reset();tile.markDirty();return;}
        if(action==8){tile.stop(selected);c.enabled=false;c.refill=!c.refill;tile.markDirty();return;}
        if(action==9){if(value< -1||value>1)return;c.manual=value;c.lastPoll=Long.MIN_VALUE;tile.markDirty();return;}
        if(action==10){if(tile.controller&&!TankBand.valid(low,high)||!tile.controller&&(high<1||high>TankBand.MAX)){notice="Use 0 <= Low < High (max 1 trillion)";return;}tile.stop(selected);c.latched=false;c.low=tile.controller?low:0;c.high=high;c.lastPoll=Long.MIN_VALUE;tile.markDirty();notice="Limits saved";return;}
        if(action==11&&tile.controller){if(c.enabled){tile.stop(selected);c.enabled=false;}else{WirelessEndpoint source=tile.endpoint(c.source),target=tile.endpoint(c.target);if(!tile.sourceKind(source)||target==null||target.kind!=WirelessEndpoint.Kind.CONTROL){notice="Choose Blue and Yellow first";return;}if(ConsoleService.blueControls(tile.data(),tile.network,c.target)){notice="Pause the Blue program for this Yellow first";return;}if(!ConsoleService.claim(tile,selected,c.target)){notice="Yellow already assigned to another rule";return;}c.enabled=true;c.lastPoll=Long.MIN_VALUE;}tile.markDirty();return;}
    }
}
