package dev.industrialtechnology.wireless;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.quarry.VoidFilter;
public final class AutomationRule {
    public enum Condition { CONTAINS_ITEM, ITEM_AMOUNT_REACHED, INVENTORY_FULL, INVENTORY_EMPTY, ITEM_MISSING }
    public Condition condition=Condition.CONTAINS_ITEM;
    public ItemStack item;
    public int minimum=1;
    public UUID target;
    public boolean enabled, latched;
    public NBTTagCompound write(){
        NBTTagCompound n=new NBTTagCompound();n.setInteger("Condition",condition.ordinal());n.setInteger("Minimum",minimum);
        n.setBoolean("Enabled",enabled);n.setBoolean("Latched",latched);if(target!=null)n.setString("Target",target.toString());
        if(item!=null){NBTTagCompound i=new NBTTagCompound();item.writeToNBT(i);n.setTag("Item",i);}return n;
    }
    public static AutomationRule read(NBTTagCompound n){
        AutomationRule r=new AutomationRule();r.condition=Condition.values()[Math.max(0,Math.min(4,n.getInteger("Condition")))];
        r.minimum=Math.max(1,Math.min(1000000000,n.getInteger("Minimum")));r.enabled=n.getBoolean("Enabled");r.latched=n.getBoolean("Latched");
        r.target=WirelessData.uuid(n.getString("Target"));r.item=ItemStack.loadItemStackFromNBT(n.getCompoundTag("Item"));
        if(!VoidFilter.valid(r.item))r.item=null;if(r.item!=null)r.item.stackSize=1;return r;
    }
    public boolean evaluate(MachineInspector.InventoryView view){
        if(view==null || !view.complete)return false;
        long amount=item==null?0:view.amount(item);
        switch(condition){
            case CONTAINS_ITEM: case ITEM_AMOUNT_REACHED:return item!=null&&amount>=minimum;
            case ITEM_MISSING:return item!=null&&amount<minimum;
            case INVENTORY_EMPTY:return view.occupied==0;
            case INVENTORY_FULL:return view.full(item);
            default:return false;
        }
    }
}
