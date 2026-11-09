package dev.industrialtechnology.accessories;
import baubles.api.*;import baubles.common.lib.PlayerHandler;import net.minecraft.inventory.IInventory;import net.minecraft.item.*;import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;
/** Uses the real Baubles inventory and its ordinary container synchronization. */
public abstract class ItemWearable extends Item implements IBauble {
 public ItemWearable(){setMaxStackSize(1);}
 public boolean canEquip(ItemStack s,EntityLivingBase e){return true;}public boolean canUnequip(ItemStack s,EntityLivingBase e){return true;}
 public void onEquipped(ItemStack s,EntityLivingBase e){}public void onUnequipped(ItemStack s,EntityLivingBase e){}public void onWornTick(ItemStack s,EntityLivingBase e){}
 public ItemStack onItemRightClick(ItemStack s,World w,EntityPlayer p){
  // Vanilla 1.7.10 dereferences the returned stack before clearing a zero-count held slot.
  // Return the consumed stack, never null, so its normal inventory synchronization runs.
  if(w.isRemote)return s;IInventory inv=PlayerHandler.getPlayerBaubles(p);
  for(int i=0;i<inv.getSizeInventory();i++)if(inv.isItemValidForSlot(i,s)&&inv.getStackInSlot(i)==null){inv.setInventorySlotContents(i,s.copy());inv.markDirty();if(!p.capabilities.isCreativeMode)s.stackSize--;break;}return s;
 }
}
