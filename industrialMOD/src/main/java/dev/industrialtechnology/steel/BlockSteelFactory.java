package dev.industrialtechnology.steel;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.*;
import dev.industrialtechnology.core.*;
public final class BlockSteelFactory extends BlockContainer {
    @SideOnly(Side.CLIENT) private IIcon frontOff,frontOn,topOff,topOn,sides;
    public BlockSteelFactory(){super(Material.iron);setBlockName("industrialtech.bulk_steel_factory");setCreativeTab(ModRegistry.TAB);setHardness(6);setResistance(30);setStepSound(soundTypeMetal);setHarvestLevel("pickaxe",1);}
    @Override public TileEntity createNewTileEntity(World w,int meta){return SteelRegistry.tile();}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){if(!w.isRemote)p.openGui(IndustrialTechnology.instance,6,w,x,y,z);return true;}
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack s){int[] sides={2,5,3,4};w.setBlockMetadataWithNotify(x,y,z,sides[MathHelper.floor_double(e.rotationYaw*4/360+.5)&3],2);}
    @Override public void breakBlock(World w,int x,int y,int z,Block b,int meta){TileEntity te=w.getTileEntity(x,y,z);if(!w.isRemote&&te instanceof TileSteelFactory){TileSteelFactory t=(TileSteelFactory)te;for(int i=0;i<t.getSizeInventory();i++){ItemStack s=t.getStackInSlotOnClosing(i);if(s!=null)w.spawnEntityInWorld(new EntityItem(w,x+.5,y+.5,z+.5,s));}w.func_147453_f(x,y,z,this);}super.breakBlock(w,x,y,z,b,meta);}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int meta){int facing=meta&7;if(facing<2)facing=3;boolean active=(meta&8)!=0;return side==facing?(active?frontOn:frontOff):side==1?(active?topOn:topOff):sides;}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(IBlockAccess w,int x,int y,int z,int side){return getIcon(side,w.getBlockMetadata(x,y,z));}
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister r){
        // Resolve the installed HBM atlas directly; no copied or recolored HBM assets.
        if(SteelRegistry.hasHBM){frontOff=r.registerIcon("hbm:rtg_difurnace_front_off");frontOn=r.registerIcon("hbm:rtg_difurnace_front_on");topOff=r.registerIcon("hbm:rtg_difurnace_top_off");topOn=r.registerIcon("hbm:rtg_difurnace_top_on");sides=r.registerIcon("hbm:machine_rtg_furnace_side_alt");}
        else{frontOff=frontOn=r.registerIcon("industrialtech:quarry_mk2_front");topOff=topOn=r.registerIcon("industrialtech:quarry_mk2_top");sides=r.registerIcon("industrialtech:quarry_mk2_side");}
    }
}
