package dev.industrialtechnology.accessories;
import java.util.*;import net.minecraft.item.*;import net.minecraft.entity.player.*;import net.minecraft.entity.Entity;import net.minecraft.world.*;import net.minecraft.util.*;
/** Original PE 1.10.1 charge/depth/cost tables; standalone coal-only fuel. */
public final class ItemCatalyst extends Item {
 public final int kind,maxCharge;public static final int[] SHOT_COST={384,768,1536,2304,2304,2304,2304,2304};public static final float[] SHOT_RADIUS={4,8,12,16,16,16,16,16};
 public ItemCatalyst(int type){kind=type;maxCharge=type==2?7:3;setMaxStackSize(1);setNoRepair();}
 public int charge(ItemStack s){return s.hasTagCompound()?Math.max(0,Math.min(maxCharge,s.getTagCompound().getByte("Charge"))):0;}
 public int depth(ItemStack s){int c=charge(s);return c==0?1:kind==2?8+c*8:1<<(1+c);}
 public void changeCharge(EntityPlayer p,ItemStack s){int c=charge(s),next=Math.max(0,Math.min(maxCharge,c+(p.isSneaking()?-1:1)));if(c==next)return;if(!s.hasTagCompound())s.setTagCompound(new net.minecraft.nbt.NBTTagCompound());s.getTagCompound().setByte("Charge",(byte)next);p.worldObj.playSoundAtEntity(p,"industrialtech:accessories."+(p.isSneaking()?"peuncharge":"pecharge"),1,.5f+.5f/maxCharge*c);}
 public boolean showDurabilityBar(ItemStack s){return s.hasTagCompound();}public double getDurabilityForDisplay(ItemStack s){return 1-charge(s)/(double)maxCharge;}
 public ItemStack onItemRightClick(ItemStack s,World w,EntityPlayer p){if(w.isRemote)return s;if(kind==1)shoot(p,s);else{MovingObjectPosition hit=getMovingObjectPositionFromPlayer(w,p,false);if(hit!=null&&hit.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK)AccessoryWorld.mine((EntityPlayerMP)p,s,hit,depth(s));}return s;}
 public boolean shoot(EntityPlayer p,ItemStack s){if(p.worldObj.isRemote||kind==0||!CoalFuel.consume(p,s,SHOT_COST[charge(s)]))return false;p.worldObj.playSoundAtEntity(p,"industrialtech:accessories.pepower",1,1);p.worldObj.spawnEntityInWorld(new EntityLensProjectile(p.worldObj,p,charge(s)));p.swingItem();return true;}
 public void addInformation(ItemStack s,EntityPlayer p,List lines,boolean advanced){lines.add("Charge: "+charge(s)+" / "+maxCharge+" (V; sneak+V lowers)");lines.add("Coal fuel: "+(long)CoalFuel.stored(s)+" EMC");lines.add(kind==1?"Right-click or R: explosive projectile":"Right-click: 3 x 3 x "+depth(s)+" mining");if(kind==2)lines.add("R: explosive projectile");lines.add("Fuel: coal variants / HBM coke only");}
}
