package dev.industrialtechnology.wireless;
import net.minecraft.nbt.*;
/** Do not replace a displayed catalog with an incomplete or out-of-order generation. */
public final class CatalogAssembler {
    private int revision;private NBTTagList[] pending;public NBTTagList rows=new NBTTagList();public boolean ready;
    public void accept(WirelessCatalog packet){
        if(packet.revision<revision)return;
        if(packet.revision>revision){revision=packet.revision;pending=new NBTTagList[packet.parts];}
        if(pending==null||pending.length!=packet.parts||packet.part<0||packet.part>=pending.length)return;
        pending[packet.part]=(NBTTagList)packet.data.getTagList("Rows",10).copy();
        for(NBTTagList chunk:pending)if(chunk==null)return;NBTTagList all=new NBTTagList();
        java.util.Set<String> ids=new java.util.HashSet<String>();for(NBTTagList chunk:pending)for(int i=0;i<chunk.tagCount();i++){NBTTagCompound row=chunk.getCompoundTagAt(i);if(ids.add(row.getString("Id")))all.appendTag(row.copy());}
        rows=all;ready=true;
    }
}
