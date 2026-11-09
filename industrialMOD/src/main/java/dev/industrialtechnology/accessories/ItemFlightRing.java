package dev.industrialtechnology.accessories;
import baubles.api.*;import baubles.common.lib.PlayerHandler;import net.minecraft.inventory.IInventory;import net.minecraft.item.ItemStack;import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;
public final class ItemFlightRing extends ItemWearable {
 public BaubleType getBaubleType(ItemStack s){return BaubleType.RING;}
 private void grant(EntityLivingBase e){if(e instanceof EntityPlayer){EntityPlayer p=(EntityPlayer)e;if(!p.capabilities.allowFlying){p.capabilities.allowFlying=true;if(!p.worldObj.isRemote)p.sendPlayerAbilities();}}}
 public void onEquipped(ItemStack s,EntityLivingBase e){grant(e);}public void onWornTick(ItemStack s,EntityLivingBase e){grant(e);}
 public void onUnequipped(ItemStack s,EntityLivingBase e){if(!(e instanceof EntityPlayer))return;EntityPlayer p=(EntityPlayer)e;if(p.capabilities.isCreativeMode)return;IInventory inv=PlayerHandler.getPlayerBaubles(p);for(int i=0;i<inv.getSizeInventory();i++){ItemStack other=inv.getStackInSlot(i);if(other!=null&&other!=s&&(other.getItem() instanceof ItemFlightRing||other.getItem().getClass().getName().equals("com.baublelicious.items.ItemFlyingRing")))return;}p.capabilities.isFlying=false;p.capabilities.allowFlying=false;if(!p.worldObj.isRemote)p.sendPlayerAbilities();}
}
