package dev.industrialtechnology.capacitor;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import dev.industrialtechnology.core.*;
import cpw.mods.fml.relauncher.*;

public final class BlockCapacitorBank extends BlockContainer {
    public static int renderId;
    @SideOnly(Side.CLIENT) public IIcon base,border,gauge,fill,input,output,disabled;
    @Override public int getRenderType(){return renderId;}
    public BlockCapacitorBank(){super(Material.iron);setBlockName("industrialtech.advanced_capacitor_bank");setCreativeTab(ModRegistry.TAB);setHardness(5);setResistance(20);setStepSound(soundTypeMetal);setHarvestLevel("pickaxe",1);}
    @Override public TileEntity createNewTileEntity(World w,int meta){return new TileCapacitorBank();}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){
        if(p.isSneaking()&&p.getHeldItem()==null){if(!w.isRemote){TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileCapacitorBank)((TileCapacitorBank)t).toggleGauge(side);}return true;}
        if(!w.isRemote)p.openGui(IndustrialTechnology.instance,2,w,x,y,z);return true;
    }
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase p,ItemStack stack){int[] faces={2,5,3,4};w.setBlockMetadataWithNotify(x,y,z,faces[MathHelper.floor_double(p.rotationYaw*4/360+.5)&3],2);if(!w.isRemote){TileEntity te=w.getTileEntity(x,y,z);if(te instanceof TileCapacitorBank){((TileCapacitorBank)te).restoreItem(stack);BankManager.of(w).changed((TileCapacitorBank)te);}}}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block b){if(!w.isRemote){TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileCapacitorBank)BankManager.of(w).changed((TileCapacitorBank)t);}}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){TileEntity te=w.getTileEntity(x,y,z);if(te instanceof TileCapacitorBank){BankManager.Network n=((TileCapacitorBank)te).network();if(n!=null&&n.busy)return false;}return harvest||super.removedByPlayer(w,p,x,y,z,false);}
    @Override public void harvestBlock(World w,EntityPlayer p,int x,int y,int z,int meta){
        try{if(!w.isRemote){TileEntity te=w.getTileEntity(x,y,z);ArrayList<ItemStack> drops=new ArrayList<ItemStack>();drops.add(te instanceof TileCapacitorBank?((TileCapacitorBank)te).drop(true):new ItemStack(this));p.addExhaustion(.025F);float chance=ForgeEventFactory.fireBlockHarvesting(drops,w,this,x,y,z,meta,0,1,false,p);for(ItemStack stack:drops)if(w.rand.nextFloat()<=chance)dropBlockAsItem(w,x,y,z,stack);}}
        finally{if(w.getBlock(x,y,z)==this)w.setBlockToAir(x,y,z);}
    }
    @Override public ArrayList<ItemStack> getDrops(World w,int x,int y,int z,int meta,int fortune){ArrayList<ItemStack> result=new ArrayList<ItemStack>();TileEntity te=w.getTileEntity(x,y,z);result.add(te instanceof TileCapacitorBank?((TileCapacitorBank)te).drop(false):new ItemStack(this));return result;}
    @Override public void breakBlock(World w,int x,int y,int z,Block old,int meta){TileEntity t=w.getTileEntity(x,y,z);if(!w.isRemote&&t instanceof TileCapacitorBank)BankManager.of(w).detach((TileCapacitorBank)t);super.breakBlock(w,x,y,z,old,meta);}

    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r){
        base=r.registerIcon("industrialtech:capacitor/base");border=r.registerIcon("industrialtech:capacitor/border");gauge=r.registerIcon("industrialtech:capacitor/gauge");fill=r.registerIcon("industrialtech:capacitor/fill");input=r.registerIcon("industrialtech:capacitor/input");output=r.registerIcon("industrialtech:capacitor/output");disabled=r.registerIcon("industrialtech:capacitor/disabled");
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int meta){return base;}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(IBlockAccess w,int x,int y,int z,int side){TileEntity te=w.getTileEntity(x,y,z);SideMode mode=te instanceof TileCapacitorBank?((TileCapacitorBank)te).mode(ForgeDirection.getOrientation(side)):SideMode.BOTH;return mode==SideMode.DISABLED?disabled:base;}
}
