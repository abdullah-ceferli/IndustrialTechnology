package dev.industrialtechnology.wireless;
import java.util.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.ForgeHooks;
import dev.industrialtechnology.core.*;
/** Contains only the player's inventory and one non-owning rule icon. Target inventories are never Slots. */
public final class TerminalContainer extends Container {
    public final EntityPlayer player;
    public final int lockedSlot;
    public final UUID network;
    public final ReactorTerminal reactors=new ReactorTerminal(this);
    public final InventoryBasic ghost=new InventoryBasic("Rule icon",true,1);
    public int tab,page,detailPage,ruleIndex;
    public UUID selected,draftTarget;
    private NBTTagCompound last;private String lastDocument="";public boolean codeLayout;
    private NBTTagList lastCatalog;private int catalogRevision;
    private int ticks,actions;private long actionTick=Long.MIN_VALUE;
    public TerminalContainer(EntityPlayer player){
        this.player=player;lockedSlot=player.inventory.currentItem;ItemStack terminal=player.getHeldItem();
        network=terminal!=null&&terminal.hasTagCompound()?WirelessData.uuid(terminal.getTagCompound().getString("WirelessNetwork")):null;
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player.inventory,9+row*9+col,78+col*18,151+row*18));
        for(int col=0;col<9;col++){
            final int index=col;addSlotToContainer(new Slot(player.inventory,col,78+col*18,209){@Override public boolean canTakeStack(EntityPlayer p){return index!=lockedSlot;}});
        }
        addSlotToContainer(new Slot(ghost,0,128,-10000){@Override public boolean isItemValid(ItemStack stack){return false;}@Override public boolean canTakeStack(EntityPlayer p){return false;}});
    }
    @Override public boolean canInteractWith(EntityPlayer p){
        if(p!=player||p.inventory.currentItem!=lockedSlot||!ItemWirelessTerminal.held(p))return false;
        return p.worldObj.isRemote||network!=null&&network.equals(WirelessAccess.terminal(p,p.getHeldItem(),false));
    }
    public boolean allowAction(){long now=player.worldObj.getTotalWorldTime();if(now!=actionTick){actionTick=now;actions=0;}return ++actions<=8;}
    private WirelessData data(){return WirelessData.get(player.worldObj);}
    private WirelessEndpoint endpoint(){WirelessEndpoint e=data().devices.get(selected);return owns(e)?e:null;}
    public boolean owns(WirelessEndpoint e){return e!=null&&e.network.equals(network)&&e.owner.equals(player.getUniqueID());}
    private List<WirelessEndpoint> listed(){List<WirelessEndpoint> list=new ArrayList<WirelessEndpoint>();for(WirelessEndpoint e:data().linked(network))if(e.kind.ordinal()==tab)list.add(e);return list;}
    private void select(WirelessEndpoint e){selected=e==null?null:e.id;detailPage=ruleIndex=0;loadRule();reactors.select(e);if(reactors.view&&reactors.type==HbmReactors.ZIRNOX&&tab==2)reactors.refreshGhost();}
    public void restoreRuleGhost(){loadRule();}
    private void loadRule(){WirelessEndpoint e=endpoint();AutomationRule r=e!=null&&e.kind==WirelessEndpoint.Kind.AUTOMATION?e.rules[ruleIndex]:null;
        ghost.setInventorySlotContents(0,r==null||r.item==null?null:r.item.copy());draftTarget=r==null?null:r.target;
    }
    public void layout(boolean code){
        code=code&&!reactors.view;codeLayout=code;for(int i=0;i<36;i++)((Slot)inventorySlots.get(i)).yDisplayPosition=code?-10000:i<27?151+i/9*18:209;
        Slot ghostSlot=(Slot)inventorySlots.get(36);
        ghostSlot.xDisplayPosition=reactors.view?245:128;
        ghostSlot.yDisplayPosition=reactors.view?(tab==2&&reactors.type==HbmReactors.ZIRNOX?176:-10000):!code&&tab==2&&selected!=null?111:-10000;
    }
    public void saveScript(UUID id,String text){
        WirelessEndpoint e=endpoint();if(e==null||!e.id.equals(id)||!e.codeMode||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=player.dimension||tab!=2)return;
        long now=player.worldObj.getTotalWorldTime();NBTTagCompound n=player.getEntityData();
        if(n.hasKey("IndustrialScriptSave")&&now-n.getLong("IndustrialScriptSave")<10){e.scriptError="Please wait a moment before saving again.";e.scriptErrorLine=0;return;}
        n.setLong("IndustrialScriptSave",now);dev.industrialtechnology.automation.ScriptEngine.save(data(),e,text);
    }
    private void sendDocument(EntityPlayerMP p){
        WirelessEndpoint e=endpoint();if(e==null||tab!=2||!e.codeMode){lastDocument="";return;}
        String key=e.id+":"+e.scriptRevision;if(!key.equals(lastDocument)){lastDocument=key;IndustrialTechnology.network.sendTo(new dev.industrialtechnology.automation.ScriptDocument(windowId,e.id,e.scriptRevision,e.script),p);}
    }
    public void action(int action,int a,int b,String text){
        WirelessData data=data();WirelessEndpoint e=endpoint();
        if(reactors.action(action,a,b,text,e)){layout(tab==2&&e!=null&&e.codeMode);return;}
        switch(action){
            case WirelessCommand.TAB:if(a<0||a>2)return;tab=a;page=0;select(null);break;
            case WirelessCommand.PAGE:if(a<0||a>21)return;page=a;break;
            case WirelessCommand.SELECT:{WirelessEndpoint candidate=data.devices.get(WirelessData.uuid(text));if(!owns(candidate)||candidate.kind.ordinal()!=tab)return;select(candidate);break;}
            case WirelessCommand.RENAME:if(e==null)return;String name=WirelessData.clean(text,32);if(name.isEmpty())return;e.name=name;data.markDirty();break;
            case WirelessCommand.UNLINK:
                if(e==null)return;WirelessAccess.off(data,e);data.remove(e);if(!e.hasPhysicalStick()){ItemStack refund=new ItemStack(e.kind==WirelessEndpoint.Kind.MONITOR?ModRegistry.greenStick:ModRegistry.blueStick);if(!player.inventory.addItemStackToInventory(refund))player.dropPlayerItemWithRandomChoice(refund,false);}select(null);break;
            case WirelessCommand.TOGGLE:
                if(e==null||e.kind!=WirelessEndpoint.Kind.CONTROL||WirelessAccess.status(e,player.dimension)!=WirelessAccess.Status.ONLINE)return;
                if(WirelessAccess.signal(e,player.worldObj.getTotalWorldTime()))WirelessAccess.off(data,e);else WirelessAccess.activate(data,e);break;
            case WirelessCommand.TRIGGER:if(e==null||e.kind!=WirelessEndpoint.Kind.CONTROL||e.dimension!=player.dimension)return;WirelessAccess.activate(data,e);break;
            case WirelessCommand.MODE:if(e==null||e.kind!=WirelessEndpoint.Kind.CONTROL||e.dimension!=player.dimension)return;e.pulseMode=!e.pulseMode;WirelessAccess.off(data,e);break;
            case WirelessCommand.DURATION:if(e==null||e.kind!=WirelessEndpoint.Kind.CONTROL||e.dimension!=player.dimension||a<5||a>10)return;e.pulseSeconds=a;data.markDirty();break;
            case WirelessCommand.RULE:if(e==null||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=player.dimension||a<0||a>=8)return;ruleIndex=a;loadRule();break;
            case WirelessCommand.TARGET:{if(e==null||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=player.dimension)return;WirelessEndpoint target=data.devices.get(WirelessData.uuid(text));if(!owns(target)||target.kind!=WirelessEndpoint.Kind.CONTROL||target.dimension!=e.dimension)return;draftTarget=target.id;break;}
            case WirelessCommand.SAVE_RULE:{
                if(e==null||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=player.dimension||a<0||a>4||b<1||b>1000000000||!("1".equals(text)||"0".equals(text)))return;
                WirelessEndpoint target=data.devices.get(draftTarget);if(!owns(target)||target.kind!=WirelessEndpoint.Kind.CONTROL||target.dimension!=e.dimension)return;
                ItemStack icon=ghost.getStackInSlot(0);if(a!=2&&a!=3&&icon==null)return;
                AutomationRule old=e.rules[ruleIndex],r=new AutomationRule();r.condition=AutomationRule.Condition.values()[a];r.minimum=b;r.item=icon==null?null:icon.copy();r.target=draftTarget;r.enabled="1".equals(text);
                boolean sameItem=old.item==null?r.item==null:MachineInspector.same(old.item,r.item);
                r.latched=old.latched&&old.condition==r.condition&&old.minimum==r.minimum&&sameItem&&Objects.equals(old.target,r.target)&&old.enabled==r.enabled;
                e.rules[ruleIndex]=r;e.lastPoll=Long.MIN_VALUE;data.markDirty();break;
            }
            case WirelessCommand.CODE_MODE:
                if(e==null||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=player.dimension||a<0||a>1)return;reactors.view=false;loadRule();e.codeMode=a==1;e.lastPoll=Long.MIN_VALUE;data.markDirty();break;
            case WirelessCommand.SCRIPT_ENABLE:
                if(e==null||!e.codeMode||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=player.dimension)return;
                if(e.scriptRules.isEmpty()){e.scriptError="Save a valid script before enabling it.";e.scriptErrorLine=0;return;}
                e.scriptEnabled=!e.scriptEnabled;e.lastPoll=Long.MIN_VALUE;data.markDirty();break;
            case WirelessCommand.DETAIL:if(a>=0&&a<=455)detailPage=a;break;
            default:return;
        }
        layout(tab==2&&endpoint()!=null&&endpoint().codeMode);
    }
    @Override public ItemStack slotClick(int slot,int button,int mode,EntityPlayer p){
        if(!canInteractWith(p)||codeLayout||!ForgeHooks.canInteractWith(p,this))return null;
        if(slot==27+lockedSlot||mode==2&&(button==lockedSlot||slot==27+lockedSlot))return null;
        if(slot==36&&reactors.view){
            if(tab==2&&mode==0&&button>=0&&button<=1&&allowAction()&&dev.industrialtechnology.quarry.VoidFilter.valid(p.inventory.getItemStack()))reactors.ghost(p.worldObj.isRemote?null:endpoint(),p.inventory.getItemStack(),button==1);
            return null;
        }
        if(slot==36){if(tab==2&&(p.worldObj.isRemote||endpoint()!=null)&&mode==0&&button>=0&&button<=1&&allowAction()){
            ItemStack icon=button==1?null:p.inventory.getItemStack();if(dev.industrialtechnology.quarry.VoidFilter.valid(icon)){icon=icon==null?null:icon.copy();if(icon!=null)icon.stackSize=1;ghost.setInventorySlotContents(0,icon);}}
            return null;
        }
        return super.slotClick(slot,button,mode,p);
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int index){
        if(!canInteractWith(p)||index<0||index>=36||index==27+lockedSlot)return null;Slot slot=(Slot)inventorySlots.get(index);if(!slot.getHasStack())return null;ItemStack item=slot.getStack(),before=item.copy();
        if(index<27){if(!mergeItemStack(item,27,36,false))return null;}else if(!mergeItemStack(item,0,27,false))return null;
        if(item.stackSize==0)slot.putStack(null);else slot.onSlotChanged();return before;
    }
    @Override public void addCraftingToCrafters(ICrafting crafter){super.addCraftingToCrafters(crafter);if(crafter instanceof EntityPlayerMP)send((EntityPlayerMP)crafter);}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();if(player.worldObj.isRemote||++ticks<10)return;ticks=0;for(Object listener:crafters)if(listener instanceof EntityPlayerMP)sendCatalog((EntityPlayerMP)listener);NBTTagCompound n=snapshot();if(n.equals(last))return;last=n;
        for(Object listener:crafters)if(listener instanceof EntityPlayerMP){IndustrialTechnology.network.sendTo(new WirelessSnapshot(windowId,n),(EntityPlayerMP)listener);sendDocument((EntityPlayerMP)listener);}
    }
    private void sendCatalog(EntityPlayerMP player){
        NBTTagList all=new NBTTagList();for(WirelessEndpoint e:data().linked(network))if(e.owner.equals(player.getUniqueID())){
            NBTTagCompound row=new NBTTagCompound();row.setString("Id",e.id.toString());row.setString("Name",e.name);row.setString("Machine",e.machine);row.setInteger("Kind",e.kind.ordinal());row.setInteger("Dimension",e.dimension);row.setInteger("X",e.x);row.setInteger("Y",e.y);row.setInteger("Z",e.z);row.setString("Status",WirelessAccess.status(e,player.dimension).name());row.setBoolean("Signal",e.kind==WirelessEndpoint.Kind.CONTROL&&e.dimension==player.dimension&&WirelessAccess.signal(e,player.worldObj.getTotalWorldTime()));all.appendTag(row);
        }if(all.equals(lastCatalog))return;lastCatalog=(NBTTagList)all.copy();catalogRevision=catalogRevision==Integer.MAX_VALUE?1:catalogRevision+1;int parts=Math.max(1,(all.tagCount()+63)/64);
        for(int part=0;part<parts;part++){NBTTagList chunk=new NBTTagList();for(int i=part*64;i<Math.min(all.tagCount(),part*64+64);i++)chunk.appendTag(all.getCompoundTagAt(i).copy());IndustrialTechnology.network.sendTo(new WirelessCatalog(windowId,catalogRevision,part,parts,chunk),player);}
    }
    public void send(EntityPlayerMP player){sendCatalog(player);IndustrialTechnology.network.sendTo(new WirelessSnapshot(windowId,snapshot()),player);sendDocument(player);}
    public NBTTagCompound snapshot(){
        NBTTagCompound n=new NBTTagCompound();n.setInteger("Tab",tab);n.setInteger("Page",page);List<WirelessEndpoint> all=listed();int pages=Math.max(1,(all.size()+5)/6);page=Math.min(page,pages-1);n.setInteger("Page",page);n.setInteger("Pages",pages);n.setInteger("Total",data().linked(network).size());
        if(endpoint()==null||endpoint().kind.ordinal()!=tab)select(all.isEmpty()?null:all.get(Math.min(all.size()-1,page*6)));
        NBTTagList rows=new NBTTagList();for(int i=page*6;i<Math.min(all.size(),page*6+6);i++){WirelessEndpoint e=all.get(i);NBTTagCompound row=new NBTTagCompound();row.setString("Id",e.id.toString());row.setString("Name",e.name);row.setString("Status",WirelessAccess.status(e,player.dimension).toString());
            row.setBoolean("Signal",e.kind==WirelessEndpoint.Kind.CONTROL&&e.dimension==player.dimension&&WirelessAccess.signal(e,player.worldObj.getTotalWorldTime()));rows.appendTag(row);}n.setTag("Devices",rows);
        WirelessEndpoint e=endpoint();if(e==null){((Slot)inventorySlots.get(36)).yDisplayPosition=-10000;return n;}
        n.setString("Selected",e.id.toString());n.setString("Name",e.name);n.setString("Machine",e.machine);n.setString("Location","Dim "+e.dimension+"  "+e.x+", "+e.y+", "+e.z+"  "+net.minecraftforge.common.util.ForgeDirection.getOrientation(e.side));
        n.setTag("Energy",dev.industrialtechnology.console.SensorReading.energy(e,player.dimension).write());
        n.setString("Status",WirelessAccess.status(e,player.dimension).toString());n.setInteger("Kind",e.kind.ordinal());n.setInteger("Dimension",e.dimension);n.setInteger("X",e.x);n.setInteger("Y",e.y);n.setInteger("Z",e.z);n.setInteger("Side",e.side);n.setInteger("DetailPage",detailPage);
        if(e.kind==WirelessEndpoint.Kind.CONTROL){n.setBoolean("Signal",e.dimension==player.dimension&&WirelessAccess.status(e,player.dimension)==WirelessAccess.Status.ONLINE&&WirelessAccess.signal(e,player.worldObj.getTotalWorldTime()));n.setBoolean("TimedOutput",e.powered&&e.pulseEnd>player.worldObj.getTotalWorldTime());n.setBoolean("PulseMode",e.pulseMode);n.setInteger("Seconds",e.pulseSeconds);n.setLong("Remaining",Math.max(0,e.pulseEnd-player.worldObj.getTotalWorldTime()));}
        else {
            MachineInspector.Reading reading=e.kind==WirelessEndpoint.Kind.AUTOMATION&&e.codeMode?new MachineInspector.Reading():MachineInspector.inspect(e,player.dimension,e.kind==WirelessEndpoint.Kind.MONITOR);if(e.kind==WirelessEndpoint.Kind.AUTOMATION&&e.codeMode)reading.status=WirelessAccess.status(e,player.dimension);n.setString("Status",reading.status.toString());n.setString("Message",reading.message);n.setString("Adapter",reading.adapter);
            MachineInspector.InventoryView inv=reading.inventory;if(inv!=null){n.setInteger("Slots",inv.slots.length);n.setInteger("Occupied",inv.occupied);n.setBoolean("HasInventory",true);}
            if(e.kind==WirelessEndpoint.Kind.MONITOR){
                detailPage=Math.min(detailPage,inv==null?0:Math.max(0,(inv.slots.length-1)/27));n.setInteger("DetailPage",detailPage);
                NBTTagList items=new NBTTagList();if(inv!=null)for(int i=detailPage*27;i<Math.min(inv.slots.length,detailPage*27+27);i++){
                    NBTTagCompound t=new NBTTagCompound();t.setInteger("Slot",inv.slots[i]);ItemStack stack=inv.contents[i];if(stack!=null){ItemStack icon=new ItemStack(stack.getItem(),1,stack.getItemDamage());NBTTagCompound iconTag=new NBTTagCompound();icon.writeToNBT(iconTag);t.setTag("Icon",iconTag);t.setString("Name",WirelessData.clean(stack.getDisplayName(),96));t.setInteger("Count",Math.max(0,stack.stackSize));try{t.setBoolean("Input",inv.insertable(i,stack));t.setBoolean("Output",inv.extractable(i,stack));t.setBoolean("DirectionsKnown",true);}catch(RuntimeException|LinkageError unavailable){t.setBoolean("DirectionsKnown",false);}}items.appendTag(t);
                }n.setTag("Items",items);n.setInteger("InventoryPages",inv==null?1:Math.max(1,(inv.slots.length+26)/27));
                NBTTagList tanks=new NBTTagList();for(MachineInspector.Tank tank:reading.tanks){NBTTagCompound t=new NBTTagCompound();t.setString("Name",tank.name);t.setString("FluidId",tank.identifier==null?"":tank.identifier);t.setString("Key",tank.localizationKey);t.setLong("Amount",tank.amount);t.setLong("Capacity",tank.capacity);t.setString("Unit",tank.unit);t.setInteger("Color",tank.color);tanks.appendTag(t);}n.setTag("Tanks",tanks);
            }else {
                AutomationRule r=e.rules[ruleIndex];n.setInteger("Rule",ruleIndex);n.setInteger("Condition",r.condition.ordinal());n.setInteger("Minimum",r.minimum);n.setBoolean("RuleEnabled",r.enabled);n.setBoolean("Latched",r.latched);n.setString("AutomationStatus",e.automationStatus);n.setBoolean("CodeMode",e.codeMode);n.setBoolean("ScriptEnabled",e.scriptEnabled);n.setInteger("ScriptRevision",e.scriptRevision);n.setString("ScriptError",e.scriptError);n.setInteger("ScriptErrorLine",e.scriptErrorLine);n.setInteger("MaxScriptLength",WirelessConfig.scriptLength);
                WirelessEndpoint target=data().devices.get(draftTarget);n.setString("Target",target==null?"":target.id.toString());n.setString("TargetName",target==null?"Choose Yellow Stick":target.name);n.setInteger("TargetSeconds",target==null?5:target.pulseSeconds);
                NBTTagList targets=new NBTTagList();for(WirelessEndpoint t:data().linked(network))if(t.kind==WirelessEndpoint.Kind.CONTROL&&t.dimension==e.dimension){NBTTagCompound row=new NBTTagCompound();row.setString("Id",t.id.toString());row.setString("Name",t.name);targets.appendTag(row);}n.setTag("Targets",targets);
            }
        }
        NBTTagCompound reactor=reactors.snapshot(e);n.setTag("Reactor",reactor);n.setBoolean("ReactorView",reactors.view);
        if(reactors.view){n.setString("Status",reactor.getString("Status"));n.setString("Message",reactor.getString("Message"));}
        layout(tab==2&&e.codeMode);return n;
    }
}
