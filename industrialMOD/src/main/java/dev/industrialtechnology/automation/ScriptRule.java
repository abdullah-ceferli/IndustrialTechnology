package dev.industrialtechnology.automation;
import java.util.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import dev.industrialtechnology.wireless.MachineInspector;
/** Only these finite conditions and actions can execute. null means unavailable, never zero. */
public final class ScriptRule {
    public enum Kind { ITEM, INVENTORY_EMPTY, INVENTORY_FULL, FLUID, TANK_EMPTY, TANK_FULL }
    public enum Action { PULSE, ON, OFF }
    public Kind kind;public Action action;public String identifier="";public long amount;public boolean less,latched;public int seconds,line;public UUID target;
    public Boolean evaluate(MachineInspector.Reading reading){
        if(reading.status!=dev.industrialtechnology.wireless.WirelessAccess.Status.ONLINE)return null;
        if(kind==Kind.ITEM||kind==Kind.INVENTORY_EMPTY||kind==Kind.INVENTORY_FULL){
            MachineInspector.InventoryView inv=reading.inventory;if(inv==null||!inv.complete)return null;
            if(kind==Kind.INVENTORY_EMPTY)return inv.occupied==0;if(kind==Kind.INVENTORY_FULL)return inv.full(null);
            long total=0;for(ItemStack stack:inv.contents)if(stack!=null&&identifier.equals(ItemStackIdentity.name(stack)))total+=Math.max(0,stack.stackSize);
            return less?total<amount:total>=amount;
        }
        if(!reading.fluidAvailable||reading.tanks.isEmpty())return null;
        long total=0;boolean full=true,capacity=false;
        for(MachineInspector.Tank tank:reading.tanks){
            if(tank.capacity>0){capacity=true;if(tank.amount<tank.capacity)full=false;}
            if(kind!=Kind.FLUID||FluidNames.matches(identifier,tank.identifier))total+=tank.amount;
        }
        if(!capacity)return null;
        if(kind==Kind.TANK_EMPTY)return total==0;if(kind==Kind.TANK_FULL)return full;
        return less?total<amount:total>=amount;
    }
    public boolean same(ScriptRule r){return r!=null&&kind==r.kind&&action==r.action&&identifier.equals(r.identifier)&&amount==r.amount&&less==r.less&&seconds==r.seconds&&Objects.equals(target,r.target);}
    public NBTTagCompound write(){NBTTagCompound n=new NBTTagCompound();n.setString("Kind",kind.name());n.setString("Action",action.name());n.setString("Id",identifier);n.setLong("Amount",amount);n.setBoolean("Less",less);n.setBoolean("Latched",latched);n.setInteger("Seconds",seconds);n.setInteger("Line",line);n.setString("Target",target.toString());return n;}
    public static ScriptRule read(NBTTagCompound n){try{
        ScriptRule r=new ScriptRule();r.kind=Kind.valueOf(n.getString("Kind"));r.action=Action.valueOf(n.getString("Action"));r.identifier=n.getString("Id");r.amount=n.getLong("Amount");r.less=n.getBoolean("Less");r.latched=n.getBoolean("Latched");r.seconds=n.getInteger("Seconds");r.line=n.getInteger("Line");r.target=UUID.fromString(n.getString("Target"));
        if(r.identifier.length()>128||r.amount<0||r.amount>1000000000000L||r.line<1||r.line>8192||r.seconds!=0&&(r.seconds<5||r.seconds>10)||r.action!=Action.PULSE&&r.seconds!=0)return null;
        if((r.kind==Kind.ITEM||r.kind==Kind.FLUID)&&r.identifier.isEmpty())return null;return r;
    }catch(IllegalArgumentException invalid){return null;}}
}
