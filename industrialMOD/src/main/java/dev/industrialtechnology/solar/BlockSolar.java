package dev.industrialtechnology.solar;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.tileentity.TileEntity;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.client.renderer.texture.IIconRegister;import net.minecraft.util.IIcon;import dev.industrialtechnology.core.*;
public final class BlockSolar extends BlockContainer {
 public static int renderId;public final int tier;public IIcon frame;private IIcon top,side;public BlockSolar(int t){super(Material.iron);tier=t;setBlockName("industrialtech.solar_"+t);setCreativeTab(ModRegistry.TAB);setHardness(3);setResistance(10);setStepSound(soundTypeMetal);setBlockBounds(0,0,0,1,.125f,1);}
 @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}@Override public int getRenderType(){return renderId;}
 @Override public void setBlockBoundsForItemRender(){setBlockBounds(0,0,0,1,.125f,1);}
 @Override public TileEntity createNewTileEntity(World w,int m){return new TileSolar();}
 @Override public void registerBlockIcons(IIconRegister r){frame=r.registerIcon("industrialtech:solar_frame_"+tier);top=r.registerIcon("industrialtech:solar_"+tier+"_top");side=r.registerIcon("industrialtech:solar_"+tier+"_side");}
 @Override public IIcon getIcon(int s,int m){return s==1?top:side;}
 @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block b){TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileSolar)((TileSolar)t).changed();}
 @Override public void breakBlock(World w,int x,int y,int z,Block b,int m){TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileSolar)((TileSolar)t).detach();super.breakBlock(w,x,y,z,b,m);}
}
