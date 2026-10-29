package dev.industrialtechnology.bag;

import java.util.*;
import net.minecraft.world.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.DimensionManager;

/** Canonical personal/color storage, independent of bag item copies, death and dimensions. */
public final class BagData extends WorldSavedData {
    private static final String KEY="industrialtech_bags";
    private final Map<UUID,Map<Integer,BagInventory>> players=new LinkedHashMap<UUID,Map<Integer,BagInventory>>();
    public BagData(){super(KEY);}
    public BagData(String key){super(key);}
    public static BagData get(World w) {
        if(w==null||w.isRemote)throw new IllegalStateException("Server storage only");
        WorldServer home=DimensionManager.getWorld(0);
        if(home==null)throw new IllegalStateException("Overworld unavailable");
        BagData data=(BagData)home.mapStorage.loadData(BagData.class,KEY);
        if(data==null){data=new BagData();home.mapStorage.setData(KEY,data);data.markDirty();}
        return data;
    }
    public BagInventory inventory(UUID owner,int color) {
        if(owner==null||color<0||color>15)throw new IllegalArgumentException("Invalid bag owner/color");
        Map<Integer,BagInventory> colors=players.get(owner);
        if(colors==null){colors=new LinkedHashMap<Integer,BagInventory>();players.put(owner,colors);}
        BagInventory inv=colors.get(color);
        if(inv==null){inv=new BagInventory(owner,color,this);colors.put(color,inv);markDirty();}
        return inv;
    }
    @Override public void writeToNBT(NBTTagCompound n) {
        NBTTagList list=new NBTTagList();
        for(Map.Entry<UUID,Map<Integer,BagInventory>> p:players.entrySet())for(Map.Entry<Integer,BagInventory> c:p.getValue().entrySet()) {
            NBTTagCompound row=new NBTTagCompound();row.setString("Owner",p.getKey().toString());row.setByte("Color",c.getKey().byteValue());
            row.setTag("Items",c.getValue().write());list.appendTag(row);
        }
        n.setInteger("Version",1);n.setTag("Bags",list);
    }
    @Override public void readFromNBT(NBTTagCompound n) {
        players.clear();NBTTagList list=n.getTagList("Bags",10);
        for(int i=0;i<list.tagCount();i++) {
            NBTTagCompound row=list.getCompoundTagAt(i);int color=row.getByte("Color");UUID owner;
            try{owner=UUID.fromString(row.getString("Owner"));}catch(IllegalArgumentException bad){continue;}
            if(color>=0&&color<16)inventory(owner,color).read(row.getTagList("Items",10));
        }
    }
}
