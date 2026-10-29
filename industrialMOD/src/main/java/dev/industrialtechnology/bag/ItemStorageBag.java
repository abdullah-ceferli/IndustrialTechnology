package dev.industrialtechnology.bag;

import java.util.List;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.*;
import dev.industrialtechnology.core.*;

public final class ItemStorageBag extends Item {
    @SideOnly(Side.CLIENT) private IIcon[] icons;
    public ItemStorageBag(){setUnlocalizedName("industrialtech.storage_bag");setCreativeTab(ModRegistry.TAB);setMaxStackSize(1);setHasSubtypes(true);setMaxDamage(0);}
    @Override public String getUnlocalizedName(ItemStack s){return "item.industrialtech.storage_bag_"+BagRegistry.COLORS[BagRegistry.color(s)];}
    @Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister r){icons=new IIcon[16];for(int i=0;i<16;i++)icons[i]=r.registerIcon("industrialtech:bags/"+BagRegistry.COLORS[i]);}
    @Override @SideOnly(Side.CLIENT) public IIcon getIconFromDamage(int color){return icons[color>=0&&color<16?color:0];}
    @Override @SideOnly(Side.CLIENT) public void getSubItems(Item item,CreativeTabs tab,List out){for(int i=0;i<16;i++)out.add(new ItemStack(this,1,i));}
    @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack s,EntityPlayer p,List out,boolean advanced){out.add("104 slots (13 x 8)");out.add("Same color shares your personal storage");out.add("Right-click to open; other colors stay separate");}
    @Override public ItemStack onItemRightClick(ItemStack s,World w,EntityPlayer p){if(!w.isRemote&&BagRegistry.valid(s))p.openGui(IndustrialTechnology.instance,8,w,0,0,0);return s;}
}
