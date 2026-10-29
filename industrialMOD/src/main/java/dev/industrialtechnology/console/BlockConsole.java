package dev.industrialtechnology.console;
import java.util.ArrayList;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import dev.industrialtechnology.core.*;
public final class BlockConsole extends BlockContainer {
    public static int renderId;
    public final boolean controller;
    public BlockConsole(boolean c){super(Material.iron);controller=c;setBlockName("industrialtech."+(c?"refinery_controller":"tank_history_monitor"));setCreativeTab(ModRegistry.TAB);setHardness(4);setResistance(20);setStepSound(soundTypeMetal);setHarvestLevel("pickaxe",1);}
    @Override public TileEntity createNewTileEntity(World w,int meta){return controller?new TileConsole.Controller():new TileConsole.History();}
    @Override public int getRenderType(){return renderId;}
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack stack){int[] faces={2,5,3,4};w.setBlockMetadataWithNotify(x,y,z,faces[MathHelper.floor_double(e.rotationYaw*4/360+.5)&3],2);if(w.isRemote||!(e instanceof EntityPlayer))return;TileConsole t=(TileConsole)w.getTileEntity(x,y,z);t.owner=e.getUniqueID();if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("Console")){net.minecraft.nbt.NBTTagCompound n=(net.minecraft.nbt.NBTTagCompound)stack.getTagCompound().getCompoundTag("Console").copy();if(e.getUniqueID().toString().equals(n.getString("Owner"))){n.setInteger("x",x);n.setInteger("y",y);n.setInteger("z",z);t.readFromNBT(n);}}t.markDirty();}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){if(w.isRemote)return true;TileConsole t=(TileConsole)w.getTileEntity(x,y,z);if(t==null||!t.usable(p))return true;if(dev.industrialtechnology.wireless.ItemWirelessTerminal.held(p))t.link(p);else p.openGui(IndustrialTechnology.instance,7,w,x,y,z);return true;}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){return harvest||super.removedByPlayer(w,p,x,y,z,false);}
    @Override public void harvestBlock(World w,EntityPlayer p,int x,int y,int z,int meta){try{super.harvestBlock(w,p,x,y,z,meta);}finally{if(!w.isRemote&&w.getBlock(x,y,z)==this)w.setBlockToAir(x,y,z);}}
    @Override public ArrayList<ItemStack> getDrops(World w,int x,int y,int z,int meta,int fortune){ArrayList<ItemStack> drops=new ArrayList<ItemStack>();ItemStack stack=new ItemStack(this);TileEntity te=w.getTileEntity(x,y,z);if(te instanceof TileConsole){net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();n.setTag("Console",((TileConsole)te).itemSettings());stack.setTagCompound(n);}drops.add(stack);return drops;}
    @Override public void breakBlock(World w,int x,int y,int z,Block b,int meta){TileEntity te=w.getTileEntity(x,y,z);if(te instanceof TileConsole&&!w.isRemote)ConsoleService.remove((TileConsole)te);super.breakBlock(w,x,y,z,b,meta);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIcon(int side,int meta){return net.minecraft.init.Blocks.iron_block.getIcon(side,0);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister r){}
}
