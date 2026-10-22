package dev.industrialtechnology.space;
import java.util.Random;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.client.renderer.texture.IIconRegister;import net.minecraft.util.IIcon;import cpw.mods.fml.relauncher.*;import dev.industrialtechnology.core.ModRegistry;
public final class BlockGreenLamp extends Block {
    @SideOnly(Side.CLIENT)private IIcon off,on;
    public BlockGreenLamp(){super(Material.glass);setBlockName("industrialtech.green_redstone_lamp");setHardness(.7f);setCreativeTab(ModRegistry.TAB);}
    @Override public int getLightValue(IBlockAccess w,int x,int y,int z){return (w.getBlockMetadata(x,y,z)&1)!=0?15:0;}
    private void refresh(World w,int x,int y,int z){if(w.isRemote)return;int n=w.isBlockIndirectlyGettingPowered(x,y,z)?1:0;if(w.getBlockMetadata(x,y,z)!=n){w.setBlockMetadataWithNotify(x,y,z,n,3);w.func_147451_t(x,y,z);}}
    @Override public void onBlockAdded(World w,int x,int y,int z){refresh(w,x,y,z);}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block b){refresh(w,x,y,z);}
    @SideOnly(Side.CLIENT)@Override public void registerBlockIcons(IIconRegister r){off=r.registerIcon("industrialtech:green_lamp_off");on=r.registerIcon("industrialtech:green_lamp_on");}
    @SideOnly(Side.CLIENT)@Override public IIcon getIcon(int s,int m){return (m&1)!=0?on:off;}
}
