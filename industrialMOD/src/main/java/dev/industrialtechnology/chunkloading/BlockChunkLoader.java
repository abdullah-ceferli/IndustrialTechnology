package dev.industrialtechnology.chunkloading;
import dev.industrialtechnology.machine.BlockMachine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
public final class BlockChunkLoader extends BlockMachine {
    public BlockChunkLoader() { super("chunk_loader"); }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileChunkLoader(); }
}
