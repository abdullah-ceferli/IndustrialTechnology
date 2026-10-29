package dev.industrialtechnology.railway;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.tileentity.TileEntity;import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import dev.industrialtechnology.core.*;
public final class BlockRailStation extends BlockContainer {
 public final int kind;public BlockRailStation(int k){super(Material.iron);kind=k;setBlockName("industrialtech.rail_station_"+k);setBlockTextureName("industrialtech:rail_station_"+k);setCreativeTab(ModRegistry.TAB);setHardness(5);setResistance(20);}
 @Override public TileEntity createNewTileEntity(World w,int m){return new TileRailStation();}@Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int s,float a,float b,float c){if(!w.isRemote)p.openGui(IndustrialTechnology.instance,5,w,x,y,z);return true;}
 @Override public void breakBlock(World w,int x,int y,int z,Block b,int m){TileEntity t=w.getTileEntity(x,y,z);if(!w.isRemote&&t instanceof TileRailStation)((TileRailStation)t).drop();super.breakBlock(w,x,y,z,b,m);}
}
