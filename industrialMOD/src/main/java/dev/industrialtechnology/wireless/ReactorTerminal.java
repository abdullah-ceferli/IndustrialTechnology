package dev.industrialtechnology.wireless;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

/** Window-scoped reactor drafts. No client-provided fuel stacks or coordinates are trusted. */
public final class ReactorTerminal {
    public static final int VIEW=16,SLOT=17,ENABLE=18,APPLY=19,CLEAR=20,ALL=21,SUPPLY=22,RUN=23,INSERTION=24;
    private final TerminalContainer terminal;
    public boolean view;public int type,slot;public ReactorSettings draft=new ReactorSettings();
    public String feedback="";
    ReactorTerminal(TerminalContainer terminal){this.terminal=terminal;}
    public void select(WirelessEndpoint e){view=false;type=0;slot=0;feedback="";draft=e==null?new ReactorSettings():e.reactor.copy();
        if(e!=null){type=e.reactorType;if(e.dimension==terminal.player.dimension){HbmReactors.Access r=HbmReactors.access(e);type=r.type;}if(e.kind==WirelessEndpoint.Kind.MONITOR&&type>0)view=true;}
    }
    public NBTTagCompound snapshot(WirelessEndpoint e){
        NBTTagCompound n;
        if(e.dimension!=terminal.player.dimension){n=new NBTTagCompound();n.setInteger("Type",e.reactorType);n.setString("Status","OTHER_DIMENSION");n.setString("Message","Wireless reactor access is limited to your current dimension");return n;}
        try{n=HbmReactors.describe(e,draft,view);type=n.getInteger("Type");}
        catch(RuntimeException|LinkageError unavailable){n=new NBTTagCompound();n.setInteger("Type",type);n.setString("Status","DATA_UNAVAILABLE");n.setString("Message","Reactor data unavailable");}
        n.setString("Feedback",feedback.isEmpty()?e.reactorMessage:feedback);n.setInteger("SelectedSlot",slot);return n;
    }
    public void ghost(WirelessEndpoint e,ItemStack cursor,boolean clear){
        if(type!=HbmReactors.ZIRNOX||e==null)return;
        if(!clear&&!HbmReactors.validFuel(cursor)){feedback="Choose an actual supported ZIRNOX fuel rod";return;}
        draft.assign(slot,clear?null:cursor);if(!clear)draft.enabled[slot]=true;refreshGhost();feedback="Draft changed; Apply saves it";
    }
    public void refreshGhost(){terminal.ghost.setInventorySlotContents(0,draft.icon(slot));}
    public boolean action(int action,int a,int b,String text,WirelessEndpoint e){
        if(action<VIEW||action>INSERTION)return false;
        if(e==null||e.kind==WirelessEndpoint.Kind.CONTROL)return true;
        if(action==VIEW){if(a<0||a>1||e.dimension!=terminal.player.dimension)return true;HbmReactors.Access r=HbmReactors.access(e);type=r.type;if(type==0)return true;view=a==1;if(view)refreshGhost();else terminal.restoreRuleGhost();return true;}
        if(!view||e.kind!=WirelessEndpoint.Kind.AUTOMATION||e.dimension!=terminal.player.dimension)return true;
        if(action==INSERTION){
            if(type!=HbmReactors.RBMK_CONTROL||a<0||a>100||a%10!=0)return true;
            long now=terminal.player.worldObj.getTotalWorldTime();if(e.reactorLastCommand!=Long.MIN_VALUE&&now-e.reactorLastCommand<10){feedback="Wait before another rod command";return true;}
            e.reactorLastCommand=now;feedback=HbmReactors.setInsertion(e,a,text);return true;
        }
        if(type!=HbmReactors.ZIRNOX)return true;
        switch(action){
            case SLOT:if(a>=0&&a<ReactorSettings.MAX_FUEL){slot=a;refreshGhost();}break;
            case ENABLE:if(a<0||a>1||draft.items[slot].isEmpty())break;draft.enabled[slot]=a==1;feedback="Draft changed; Apply saves it";break;
            case CLEAR:draft.assign(slot,null);refreshGhost();feedback="Draft cleared; Apply saves it";break;
            case ALL:if(a>=0&&a<=1){for(int i=0;i<24;i++)draft.enabled[i]=a==1&&!draft.items[i].isEmpty();feedback="Draft changed; Apply saves it";}break;
            case SUPPLY:if(a>=-1&&a<=5){draft.supply=a;feedback="Supply draft changed; Apply saves it";}break;
            case APPLY:
                for(int i=0;i<24;i++)if(!draft.items[i].isEmpty()&&!HbmReactors.validFuel(draft.icon(i))){feedback="Unsupported fuel in assignment "+(i+1);return true;}
                HbmReactors.Access r=HbmReactors.access(e);
                if(r.online()&&!r.binding.equals(text)){feedback="Controller changed; reopen the fuel grid";break;}
                boolean running=e.reactor.running;e.reactor=draft.copy();e.reactor.running=running;e.reactorNextPoll=Long.MIN_VALUE;WirelessData.get(terminal.player.worldObj).markDirty();feedback="Assignments saved";break;
            case RUN:e.reactor.running=!e.reactor.running;e.reactorNextPoll=Long.MIN_VALUE;WirelessData.get(terminal.player.worldObj).markDirty();feedback=e.reactor.running?"Saved assignments enabled":"Fuel automation disabled";break;
            default:break;
        }return true;
    }
}
