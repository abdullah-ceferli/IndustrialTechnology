package dev.industrialtechnology.lighting;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.*;
import dev.industrialtechnology.core.ModRegistry;

/** Full collision cube with solid attachment faces, including when powered. */
public final class BlockGreenLamp extends Block {
    @SideOnly(Side.CLIENT) private IIcon off,on;
    public BlockGreenLamp(){super(Material.glass);setBlockName("industrialtech.green_redstone_lamp");setHardness(.7f);setCreativeTab(ModRegistry.TAB);}
    @Override public boolean isSideSolid(IBlockAccess world,int x,int y,int z,ForgeDirection side){return side!=ForgeDirection.UNKNOWN;}
    @Override public boolean canPlaceTorchOnTop(World world,int x,int y,int z){return true;}
    @Override public int getLightValue(IBlockAccess world,int x,int y,int z){return (world.getBlockMetadata(x,y,z)&1)!=0?15:0;}
    private void refresh(World world,int x,int y,int z){if(world.isRemote)return;int powered=world.isBlockIndirectlyGettingPowered(x,y,z)?1:0;if(world.getBlockMetadata(x,y,z)!=powered){world.setBlockMetadataWithNotify(x,y,z,powered,3);world.func_147451_t(x,y,z);}}
    @Override public void onBlockAdded(World world,int x,int y,int z){refresh(world,x,y,z);}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){refresh(world,x,y,z);}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register){off=register.registerIcon("industrialtech:green_lamp_off");on=register.registerIcon("industrialtech:green_lamp_on");}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int metadata){return (metadata&1)!=0?on:off;}
}
