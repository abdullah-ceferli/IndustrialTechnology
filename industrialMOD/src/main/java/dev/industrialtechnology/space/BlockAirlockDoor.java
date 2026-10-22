package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.util.AxisAlignedBB;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.*;import net.minecraft.init.Items;import dev.industrialtechnology.core.*;
/** Two-block pressure door; only its controller can change open state. */
public final class BlockAirlockDoor extends Block {
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private net.minecraft.util.IIcon upper,lower,edge;
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister r){lower=r.registerIcon("industrialtech:airlock_door");upper=r.registerIcon("industrialtech:airlock_door_top");edge=r.registerIcon("industrialtech:airlock_door_edge");blockIcon=lower;}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public net.minecraft.util.IIcon getIcon(int face,int meta){boolean open=(meta&4)!=0;return face==(open?4:2)||face==(open?5:3)?((meta&8)!=0?upper:lower):edge;}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public net.minecraft.util.IIcon getIcon(IBlockAccess w,int x,int y,int z,int face){return getIcon(face,w.getBlockMetadata(x,y,z));}
    public BlockAirlockDoor(){super(Material.iron);setBlockName("industrialtech.airlock_door");setBlockTextureName("industrialtech:airlock_door");setHardness(5);setResistance(20);setCreativeTab(ModRegistry.TAB);}
    @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){return y>0&&y<254&&w.isAirBlock(x,y,z)&&w.isAirBlock(x,y+1,z)&&w.isSideSolid(x,y-1,z,net.minecraftforge.common.util.ForgeDirection.UP);}
    @Override public void onBlockAdded(World w,int x,int y,int z){if(!w.isRemote&&(w.getBlockMetadata(x,y,z)&8)==0){if(w.isAirBlock(x,y+1,z))w.setBlock(x,y+1,z,this,8,3);Atmosphere.changed(w);}}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){boolean open=(w.getBlockMetadata(x,y,z)&4)!=0;if(open)setBlockBounds(0,0,0,.12f,1,1);else setBlockBounds(0,0,.4f,1,1,.6f);}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){setBlockBoundsBasedOnState(w,x,y,z);return super.getCollisionBoundingBoxFromPool(w,x,y,z);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){if(w.isRemote)return true;TileSpaceMachine controller=Airlock.controller(w,x,y,z);if(controller!=null&&controller.authorized(p))p.openGui(IndustrialTechnology.instance,2,w,controller.xCoord,controller.yCoord,controller.zCoord);return true;}
    @Override public void breakBlock(World w,int x,int y,int z,Block b,int meta){if(!w.isRemote){int yy=(meta&8)!=0?y-1:y+1;if(w.getBlock(x,yy,z)==this)w.setBlockToAir(x,yy,z);Atmosphere.changed(w);}super.breakBlock(w,x,y,z,b,meta);}
    @Override public Item getItemDropped(int meta,Random r,int fortune){return Item.getItemFromBlock(this);}
}
