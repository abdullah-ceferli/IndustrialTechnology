package dev.industrialtechnology.space;

import cpw.mods.fml.relauncher.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;
import dev.industrialtechnology.core.*;

/** Metadata 2..5 is horizontal facing. Legacy metadata 0/1 retains a south front. */
public class BlockSpaceMachine extends BlockContainer {
    public final int kind;
    @SideOnly(Side.CLIENT) private IIcon front,side,top,bottom;
    public BlockSpaceMachine(int k){super(Material.iron);kind=k;setBlockName("industrialtech."+SpaceRegistry.MACHINES[k]);setBlockTextureName("industrialtech:"+SpaceRegistry.MACHINES[k]);setHardness(4);setResistance(15);setCreativeTab(ModRegistry.TAB);}
    public static int front(int meta){return meta>=2&&meta<=5?meta:3;}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister r){String n="industrialtech:"+SpaceRegistry.MACHINES[kind];front=r.registerIcon(n);side=r.registerIcon(n+"_side");top=r.registerIcon(n+"_top");bottom=r.registerIcon(n+"_bottom");blockIcon=front;}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int face,int meta){return face==0?bottom:face==1?top:face==(meta>=2&&meta<=5?meta:2)?front:side;}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(IBlockAccess w,int x,int y,int z,int face){return getIcon(face,front(w.getBlockMetadata(x,y,z)));}
    @Override public TileEntity createNewTileEntity(World w,int m){return new TileSpaceMachine(kind);}
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack s){
        if(!w.isRemote){int[] faces={2,5,3,4};int f=faces[MathHelper.floor_double(e.rotationYaw*4/360+.5)&3];w.setBlockMetadataWithNotify(x,y,z,f,3);}
        TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileSpaceMachine&&e instanceof EntityPlayer){((TileSpaceMachine)t).owner=e.getUniqueID().toString();t.markDirty();}Atmosphere.changed(w);
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){if(w.isRemote)return true;TileEntity te=w.getTileEntity(x,y,z);if(!(te instanceof TileSpaceMachine))return false;TileSpaceMachine t=(TileSpaceMachine)te;if(!t.authorized(p))return true;if(t.canister(p))return true;p.openGui(IndustrialTechnology.instance,2,w,x,y,z);return true;}
    @Override public void breakBlock(World w,int x,int y,int z,Block old,int meta){TileEntity t=w.getTileEntity(x,y,z);if(!w.isRemote&&t instanceof TileSpaceMachine){TileSpaceMachine m=(TileSpaceMachine)t;for(int i=0;i<m.getSizeInventory();i++){ItemStack s=m.getStackInSlot(i);if(s!=null){w.spawnEntityInWorld(new EntityItem(w,x+.5,y+.5,z+.5,s.copy()));m.setInventorySlotContents(i,null);}}}Atmosphere.changed(w);super.breakBlock(w,x,y,z,old,meta);}
}
