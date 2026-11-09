package dev.industrialtechnology.accessories;
import java.util.*;import cpw.mods.fml.common.registry.GameRegistry;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.tileentity.TileEntityFurnace;import net.minecraftforge.oredict.OreDictionary;
/** Explicit coal/coke families only. Furnace burnability by itself never qualifies an item. */
public final class CoalFuel {
 private static final Set<String> COAL_ORES=new HashSet<String>(Arrays.asList("coal","fuelCoal","fuelCoke","coalCoke","coalCharcoal","coalLignite","coalPetroleum","blockCoal","blockFuelCoke"));
 private static final class HbmCoal {static final Set<Item> ITEMS=load();private static Set<Item> load(){Set<Item> out=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());try{Class<?> items=Class.forName("com.hbm.items.ModItems");for(String name:new String[]{"coke","lignite","briquette","coal_infernal","coal_eternal","pellet_coal"})try{Object item=items.getField(name).get(null);if(item instanceof Item)out.add((Item)item);}catch(ReflectiveOperationException ignored){}}catch(ClassNotFoundException absent){}return out;}}
 public static int value(ItemStack s){
  if(s==null||s.stackSize<=0)return 0;Item i=s.getItem();if(i==Items.coal)return s.getItemDamage()==0?128:s.getItemDamage()==1?32:0;if(i==Item.getItemFromBlock(Blocks.coal_block))return 1152;
  boolean coal=HbmCoal.ITEMS.contains(i);
  if(!coal)for(int id:OreDictionary.getOreIDs(s))if(COAL_ORES.contains(OreDictionary.getOreName(id))){coal=true;break;}
  if(!coal||i==Items.stick||i==Item.getItemFromBlock(Blocks.log)||i==Item.getItemFromBlock(Blocks.log2)||i==Item.getItemFromBlock(Blocks.planks))return 0;
  int burn=TileEntityFurnace.getItemBurnTime(s);return (int)Math.max(1,Math.min(1048576L,128L*Math.max(1,burn)/1600L));
 }
 private static NBTTagCompound data(ItemStack s){if(!s.hasTagCompound())s.setTagCompound(new NBTTagCompound());return s.getTagCompound();}
 public static double stored(ItemStack s){double v=data(s).getDouble("StoredEMC");return Double.isFinite(v)&&v>0?v:0;}
 public static boolean consume(EntityPlayer p,ItemStack tool,int amount){
  if(amount<=0||p.capabilities.isCreativeMode)return true;double available=stored(tool);if(available>=amount){data(tool).setDouble("StoredEMC",available-amount);return true;}
  int[] remove=new int[p.inventory.getSizeInventory()];long gathered=0;double need=amount-available;
  for(int slot=0;slot<remove.length;slot++){ItemStack fuel=p.inventory.getStackInSlot(slot);int unit=value(fuel);if(unit<=0)continue;int count=(int)Math.min(fuel.stackSize,Math.ceil((need-gathered)/unit));remove[slot]=Math.max(0,count);gathered+=(long)unit*remove[slot];if(gathered>=need)break;}
  if(gathered<need)return false;for(int slot=0;slot<remove.length;slot++)if(remove[slot]>0)p.inventory.decrStackSize(slot,remove[slot]);data(tool).setDouble("StoredEMC",available+gathered-amount);p.inventory.markDirty();return true;
 }
 private CoalFuel(){}
}
