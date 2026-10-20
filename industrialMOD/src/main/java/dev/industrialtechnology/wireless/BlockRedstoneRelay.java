package dev.industrialtechnology.wireless;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import dev.industrialtechnology.core.ModRegistry;
public final class BlockRedstoneRelay extends BlockContainer {
    public final WirelessEndpoint.Kind kind;
    private final String texture;
    public BlockRedstoneRelay(){this(WirelessEndpoint.Kind.CONTROL,"redstone_relay","redstone_relay");}
    public BlockRedstoneRelay(WirelessEndpoint.Kind kind,String name,String texture){super(Material.circuits);this.kind=kind;this.texture=texture;setBlockName("industrialtech."+name);setBlockTextureName("industrialtech:"+texture);setHardness(.3f);setStepSound(soundTypeWood);}
    @Override public TileEntity createNewTileEntity(World w,int meta){return new TileRedstoneRelay();}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private net.minecraft.util.IIcon onIcon,offIcon;
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister icons){onIcon=icons.registerIcon("industrialtech:"+texture);offIcon=kind==WirelessEndpoint.Kind.CONTROL?icons.registerIcon("industrialtech:redstone_relay_off"):onIcon;blockIcon=onIcon;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIcon(IBlockAccess w,int x,int y,int z,int side){TileEntity t=w.getTileEntity(x,y,z);return t instanceof TileRedstoneRelay&&((TileRedstoneRelay)t).signal()?onIcon:offIcon;}
    @Override public int getRenderType(){return -1;}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public boolean canProvidePower(){return kind==WirelessEndpoint.Kind.CONTROL;}
    @Override public boolean canConnectRedstone(IBlockAccess w,int x,int y,int z,int side){return kind==WirelessEndpoint.Kind.CONTROL;}
    private int power(IBlockAccess w,int x,int y,int z){TileEntity t=w.getTileEntity(x,y,z);return t instanceof TileRedstoneRelay&&((TileRedstoneRelay)t).signal()?15:0;}
    @Override public int isProvidingWeakPower(IBlockAccess w,int x,int y,int z,int side){return power(w,x,y,z);}
    @Override public int isProvidingStrongPower(IBlockAccess w,int x,int y,int z,int side){return side==w.getBlockMetadata(x,y,z)?power(w,x,y,z):0;}
    @Override public Item getItemDropped(int meta,Random random,int fortune){return ItemWirelessStick.item(kind);}
    @Override public Item getItem(World w,int x,int y,int z){return ItemWirelessStick.item(kind);}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return null;}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){
        switch(w.getBlockMetadata(x,y,z)){
            case 0:setBlockBounds(.4f,.4f,.4f,.6f,1,.6f);break;case 1:setBlockBounds(.4f,0,.4f,.6f,.6f,.6f);break;
            case 2:setBlockBounds(.35f,.2f,.7f,.65f,.8f,1);break;case 3:setBlockBounds(.35f,.2f,0,.65f,.8f,.3f);break;
            case 4:setBlockBounds(.7f,.2f,.35f,1,.8f,.65f);break;default:setBlockBounds(0,.2f,.35f,.3f,.8f,.65f);
        }
    }
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block changed){
        if(w.isRemote)return;ForgeDirection d=ForgeDirection.getOrientation(w.getBlockMetadata(x,y,z));int sx=x-d.offsetX,sy=y-d.offsetY,sz=z-d.offsetZ;
        if(WirelessAccess.loaded(w,sx,sy,sz)&&w.isAirBlock(sx,sy,sz)){dropBlockAsItem(w,x,y,z,0,0);w.setBlockToAir(x,y,z);}
    }
    @Override public void breakBlock(World w,int x,int y,int z,Block old,int meta){
        TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileRedstoneRelay)((TileRedstoneRelay)t).removed();
        super.breakBlock(w,x,y,z,old,meta);w.notifyBlocksOfNeighborChange(x,y,z,this);ForgeDirection d=ForgeDirection.getOrientation(meta);w.notifyBlocksOfNeighborChange(x-d.offsetX,y-d.offsetY,z-d.offsetZ,this);
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){
        net.minecraft.item.ItemStack held=p.getHeldItem();if(held==null||held.getItem()!=ModRegistry.terminal&&!(held.getItem() instanceof ItemWirelessStick))return false;
        if(!w.isRemote){TileEntity tile=w.getTileEntity(x,y,z);if(tile instanceof TileRedstoneRelay)ItemWirelessStick.linkPlaced(p,(TileRedstoneRelay)tile);}return true;
    }
}
