package dev.industrialtechnology.railway;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.tileentity.TileEntity;import net.minecraft.entity.player.EntityPlayer;import dev.industrialtechnology.core.*;
public final class BlockRailAnchor extends BlockContainer {
 public BlockRailAnchor(){super(Material.iron);setBlockName("industrialtech.rail_anchor");setHardness(4);setResistance(20);setBlockTextureName("industrialtech:rail_anchor");setBlockBounds(0,0,0,1,.15f,1);}
 @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}@Override public int getRenderType(){return -1;}@Override public TileEntity createNewTileEntity(World w,int m){return new TileTrack();}
 @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int s,float a,float b,float c){TileTrack t=(TileTrack)w.getTileEntity(x,y,z);if(!w.isRemote&&t!=null){if(t.shape==4)t.toggle();else p.addChatMessage(new net.minecraft.util.ChatComponentText(t.station?"Station stop: powered redstone holds the train.":"Standard gauge 1.435 m. Use rolling-stock item near this track."));}return true;}
 @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block b){TileTrack t=(TileTrack)w.getTileEntity(x,y,z);if(t!=null&&t.shape==4&&!w.isRemote){boolean v=w.isBlockIndirectlyGettingPowered(x,y,z);if(v!=t.switched)t.toggle();}}
 @Override public net.minecraft.item.Item getItemDropped(int m,java.util.Random r,int f){return RailwayRegistry.trackItems[Math.max(0,Math.min(5,m))];}
}
