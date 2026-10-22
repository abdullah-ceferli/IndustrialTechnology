package dev.industrialtechnology.space;
import java.util.List;import net.minecraft.item.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.client.renderer.texture.IIconRegister;import net.minecraft.util.IIcon;import dev.industrialtechnology.core.ModRegistry;
public final class ItemRocket extends Item {
    private IIcon shuttle;
    public ItemRocket(){setMaxStackSize(1);setUnlocalizedName("industrialtech.assembled_rocket");setTextureName("industrialtech:assembled_rocket");setCreativeTab(ModRegistry.TAB);}
    @Override public void registerIcons(IIconRegister r){super.registerIcons(r);shuttle=r.registerIcon("industrialtech:space_shuttle");}
    @Override public IIcon getIconIndex(ItemStack s){return ShuttleVehicle.is(s)?shuttle:itemIcon;}
    @Override public String getUnlocalizedName(ItemStack s){return ShuttleVehicle.is(s)?"item.industrialtech.space_shuttle":super.getUnlocalizedName(s);}
    @Override public void addInformation(ItemStack s,EntityPlayer p,List out,boolean a){if(ShuttleVehicle.is(s)){out.add("56.1 m stack / 23.8 m orbiter wingspan");out.add("Deploy the LC-39 facility kit before launch.");}else out.add("11 verified components; insert into launch controller.");out.add("Kerosene + liquid oxygen: gameplay propellants.");if(s.hasTagCompound())out.add("Return propellant retained in vehicle NBT.");}
}
