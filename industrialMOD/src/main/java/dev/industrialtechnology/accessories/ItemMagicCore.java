package dev.industrialtechnology.accessories;
import java.util.List;import dev.industrialtechnology.core.ModRegistry;import cpw.mods.fml.relauncher.*;import net.minecraft.item.*;import net.minecraft.util.IIcon;import net.minecraft.client.renderer.texture.IIconRegister;import net.minecraft.creativetab.CreativeTabs;
public final class ItemMagicCore extends Item {
 @SideOnly(Side.CLIENT) private IIcon[] icons;
 public ItemMagicCore(){setHasSubtypes(true);setCreativeTab(ModRegistry.TAB);setUnlocalizedName("industrialtech.magic_core");}
 public String getUnlocalizedName(ItemStack s){return super.getUnlocalizedName()+"_"+Math.max(0,Math.min(2,s.getItemDamage()));}
 @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister r){icons=new IIcon[3];for(int i=0;i<3;i++)icons[i]=r.registerIcon("industrialtech:accessories/magic_core_"+i);}
 @SideOnly(Side.CLIENT) public IIcon getIconFromDamage(int m){return icons[Math.max(0,Math.min(2,m))];}
 @SideOnly(Side.CLIENT) public void getSubItems(Item item,CreativeTabs tab,List list){for(int i=0;i<3;i++)list.add(new ItemStack(item,1,i));}
}
