package dev.industrialtechnology.space;
import net.minecraft.block.Block;import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.tileentity.TileEntity;
public final class BlockLaunchFacility extends BlockSpaceMachine {
    public BlockLaunchFacility(){super(SpaceRegistry.LAUNCH);setBlockName("industrialtech.shuttle_launch_facility");setHardness(5);setResistance(30);}
    @Override public TileEntity createNewTileEntity(World w,int meta){return new TileLaunchFacility();}
    @Override public java.util.ArrayList<net.minecraft.item.ItemStack> getDrops(net.minecraft.world.World w,int x,int y,int z,int meta,int fortune){return new java.util.ArrayList<net.minecraft.item.ItemStack>();}
    @Override public boolean canDropFromExplosion(net.minecraft.world.Explosion e){return false;}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){if(!w.isRemote)FacilityIndex.request(w,x,y,z,p);return false;}
    @Override public void breakBlock(World w,int x,int y,int z,Block b,int meta){TileEntity t=w.getTileEntity(x,y,z);if(!w.isRemote&&t instanceof TileLaunchFacility)FacilityIndex.destroyRoot((TileLaunchFacility)t);w.removeTileEntity(x,y,z);Atmosphere.changed(w);}
}
