package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.*;import net.minecraft.util.*;import net.minecraft.client.renderer.texture.IIconRegister;import cpw.mods.fml.relauncher.*;
/** Ordinary blocks, no thousands of ticking tile entities. One kit owns the assembly. */
public final class BlockLaunchPart extends Block {
    @SideOnly(Side.CLIENT)private IIcon[] icons;
    public BlockLaunchPart(){super(Material.iron);setBlockName("industrialtech.launch_facility_part");setHardness(5);setResistance(30);}
    @SideOnly(Side.CLIENT)@Override public void registerBlockIcons(IIconRegister r){icons=new IIcon[7];for(int i=0;i<icons.length;i++)icons[i]=r.registerIcon("industrialtech:facility_part_"+i);blockIcon=icons[0];}
    @SideOnly(Side.CLIENT)@Override public IIcon getIcon(int s,int m){return icons[Math.max(0,Math.min(6,m))];}
    @SideOnly(Side.CLIENT)@Override public int getRenderType(){return dev.industrialtechnology.client.space.FacilityPartRenderer.ID;}
    @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){int m=w.getBlockMetadata(x,y,z);if(m==4)setBlockBounds(.3f,0,.3f,.7f,1,.7f);else if(m==5)setBlockBounds(.4f,0,.4f,.6f,1,.6f);else if(m==6)setBlockBounds(0,0,0,1,1,1);else setBlockBounds(0,0,0,1,1,1);}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){setBlockBoundsBasedOnState(w,x,y,z);return w.getBlockMetadata(x,y,z)==6?null:super.getCollisionBoundingBoxFromPool(w,x,y,z);}
    @Override public boolean isLadder(IBlockAccess w,int x,int y,int z,net.minecraft.entity.EntityLivingBase e){return w.getBlockMetadata(x,y,z)==6;}
    @Override public boolean shouldSideBeRendered(IBlockAccess w,int x,int y,int z,int side){if(w.getBlock(x,y,z)==this&&w.getBlockMetadata(x,y,z)<4)return false;return super.shouldSideBeRendered(w,x,y,z,side);}
    @Override public void breakBlock(World w,int x,int y,int z,Block old,int meta){if(!w.isRemote)FacilityIndex.destroyed(w,x,y,z);super.breakBlock(w,x,y,z,old,meta);}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){if(!w.isRemote)FacilityIndex.request(w,x,y,z,p);return false;}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int s,float a,float b,float c){if(!w.isRemote)FacilityIndex.open(w,x,y,z,p);return true;}
    @Override public ArrayList<ItemStack> getDrops(World w,int x,int y,int z,int meta,int fortune){return new ArrayList<ItemStack>();}
    @Override public boolean canDropFromExplosion(net.minecraft.world.Explosion e){return false;}
    @Override public void onBlockDestroyedByExplosion(World w,int x,int y,int z,net.minecraft.world.Explosion e){FacilityIndex.destroyed(w,x,y,z);}
}
