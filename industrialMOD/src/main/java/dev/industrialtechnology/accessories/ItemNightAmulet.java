package dev.industrialtechnology.accessories;
import baubles.api.*;import net.minecraft.item.ItemStack;import net.minecraft.entity.EntityLivingBase;import net.minecraft.potion.*;
public final class ItemNightAmulet extends ItemWearable {
 public BaubleType getBaubleType(ItemStack s){return BaubleType.AMULET;}
 public void onWornTick(ItemStack s,EntityLivingBase e){PotionEffect effect=e.getActivePotionEffect(Potion.nightVision);if(effect==null||effect.getDuration()<900)e.addPotionEffect(new PotionEffect(Potion.nightVision.id,999999,1,true));}
 public void onUnequipped(ItemStack s,EntityLivingBase e){e.removePotionEffect(Potion.nightVision.id);}
}
