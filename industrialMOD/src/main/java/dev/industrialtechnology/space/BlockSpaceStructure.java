package dev.industrialtechnology.space;
import cpw.mods.fml.relauncher.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import dev.industrialtechnology.core.ModRegistry;
/** Same full collision cube and registry identity; only the face artwork differs. */
public final class BlockSpaceStructure extends Block {
    private final String name;private final boolean pad;
    @SideOnly(Side.CLIENT) private IIcon top,side,bottom;
    public BlockSpaceStructure(String n,boolean p){super(Material.iron);name=n;pad=p;setBlockName("industrialtech."+n);setBlockTextureName("industrialtech:"+n);setHardness(5);setResistance(12);setCreativeTab(ModRegistry.TAB);}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister r){top=r.registerIcon("industrialtech:"+name+(pad?"":"_top"));side=r.registerIcon("industrialtech:"+name+(pad?"_side":""));bottom=r.registerIcon("industrialtech:rocket_assembly_bottom");blockIcon=side;}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int s,int m){return s==1?top:s==0?bottom:side;}
}
