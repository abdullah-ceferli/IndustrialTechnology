package dev.industrialtechnology.capacitor;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.*;
public final class ItemBlockCapacitorBank extends ItemBlock {
    private static final String[] BANK_KEYS={"BankVersion","StoredRF","DisplayFaces","Sides","InputLimit","OutputLimit","Revision","Redstone"};
    public ItemBlockCapacitorBank(Block block){super(block);setMaxStackSize(64);}
    @Override public int getItemStackLimit(ItemStack stack){return stack!=null&&stack.hasTagCompound()&&stack.getTagCompound().getLong("StoredRF")>0?1:64;}
    /** Default empty drops use the same item data as crafted banks. Configured or named items keep their data. */
    public static void normalizeEmptyDefault(ItemStack stack){
        if(stack==null||!stack.hasTagCompound())return;NBTTagCompound tag=stack.getTagCompound();
        if(tag.getInteger("BankVersion")!=1||tag.getLong("StoredRF")!=0||tag.getLong("Revision")!=0||tag.getInteger("Redstone")!=0||tag.getByte("DisplayFaces")!=0)return;
        if(tag.hasKey("InputLimit")&&tag.getLong("InputLimit")!=-1||tag.hasKey("OutputLimit")&&tag.getLong("OutputLimit")!=-1)return;
        if(tag.hasKey("Sides")){byte[] sides=tag.getByteArray("Sides");if(sides.length!=6)return;for(byte side:sides)if(side!=2)return;}
        for(String key:BANK_KEYS)tag.removeTag(key);
        if(tag.hasNoTags())stack.setTagCompound(null);
    }
    @Override public void onUpdate(ItemStack stack,World world,Entity holder,int slot,boolean held){if(!world.isRemote)normalizeEmptyDefault(stack);}
    @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack s,EntityPlayer p,List lines,boolean advanced){long energy=s.hasTagCompound()?Math.max(0,Math.min(BankConfig.capacity,s.getTagCompound().getLong("StoredRF"))):0;lines.add(String.format(java.util.Locale.US,"%,d / %,d RF",energy,BankConfig.capacity));lines.add(String.format(java.util.Locale.US,"%,d RF/t in / %,d RF/t out",BankConfig.input,BankConfig.output));lines.add("Touching banks share storage and transfer limits");lines.add("Break with a pickaxe to retain RF and settings");lines.add(energy>0?"Charged banks stay separate":"Empty banks stack to 64 with matching saved settings");}
}
