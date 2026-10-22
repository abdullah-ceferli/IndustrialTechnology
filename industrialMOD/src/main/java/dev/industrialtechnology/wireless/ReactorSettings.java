package dev.industrialtechnology.wireless;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

/** Configuration only. Live fuel values are never saved in the terminal registry. */
public final class ReactorSettings {
    public static final int MAX_FUEL=24;
    public final String[] items=new String[MAX_FUEL];
    public final int[] metadata=new int[MAX_FUEL];
    public final boolean[] enabled=new boolean[MAX_FUEL];
    public boolean running;
    public int supply=-1;
    public ReactorSettings(){java.util.Arrays.fill(items,"");}
    public ItemStack icon(int slot){
        if(slot<0||slot>=MAX_FUEL||items[slot].isEmpty())return null;
        Object item=Item.itemRegistry.getObject(items[slot]);
        return item instanceof Item?new ItemStack((Item)item,1,metadata[slot]):null;
    }
    public void assign(int slot,ItemStack stack){
        items[slot]=stack==null?"":String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
        metadata[slot]=stack==null?0:stack.getItemDamage();if(stack==null)enabled[slot]=false;
    }
    public boolean matches(int slot,ItemStack stack){return stack!=null&&stack.getItemDamage()==metadata[slot]&&items[slot].equals(Item.itemRegistry.getNameForObject(stack.getItem()));}
    public ReactorSettings copy(){return read(write());}
    public NBTTagCompound write(){
        NBTTagCompound n=new NBTTagCompound();n.setBoolean("Running",running);n.setInteger("Supply",supply);NBTTagList list=new NBTTagList();
        for(int i=0;i<MAX_FUEL;i++)if(!items[i].isEmpty()){
            NBTTagCompound row=new NBTTagCompound();row.setInteger("Slot",i);row.setString("Item",items[i]);row.setInteger("Meta",metadata[i]);row.setBoolean("Enabled",enabled[i]);list.appendTag(row);
        }n.setTag("Assignments",list);return n;
    }
    public static ReactorSettings read(NBTTagCompound n){
        ReactorSettings r=new ReactorSettings();r.running=n.getBoolean("Running");r.supply=n.hasKey("Supply")?n.getInteger("Supply"):-1;
        if(r.supply< -1||r.supply>5){r.supply=-1;r.running=false;}
        NBTTagList list=n.getTagList("Assignments",10);if(list.tagCount()>MAX_FUEL){r.running=false;return r;}
        boolean[] seen=new boolean[MAX_FUEL];
        for(int i=0;i<list.tagCount();i++){
            NBTTagCompound row=list.getCompoundTagAt(i);int slot=row.getInteger("Slot"),meta=row.getInteger("Meta");String id=row.getString("Item");
            if(slot<0||slot>=MAX_FUEL||seen[slot]||meta<0||meta>32767||id.length()>128||id.indexOf(':')<1){r.running=false;continue;}
            seen[slot]=true;r.items[slot]=id;r.metadata[slot]=meta;r.enabled[slot]=row.getBoolean("Enabled");
        }return r;
    }
}
