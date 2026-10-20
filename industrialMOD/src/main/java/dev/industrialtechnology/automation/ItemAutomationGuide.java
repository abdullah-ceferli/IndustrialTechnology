package dev.industrialtechnology.automation;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import dev.industrialtechnology.core.*;
public final class ItemAutomationGuide extends Item {
    public ItemAutomationGuide(){setMaxStackSize(1);setUnlocalizedName("industrialtech.automation_guide");setTextureName("industrialtech:automation_guide");setCreativeTab(ModRegistry.TAB);}
    @Override public ItemStack onItemRightClick(ItemStack stack,World w,EntityPlayer p){if(w.isRemote)IndustrialTechnology.proxy.openAutomationGuide();return stack;}
    @Override public boolean onItemUseFirst(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float a,float b,float c){if(w.isRemote)IndustrialTechnology.proxy.openAutomationGuide();return true;}
}
