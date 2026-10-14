package dev.industrialtechnology.quarry;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/** Uncommitted drops, cached while waiting for output space. They are not owned inventory. */
final class HarvestPlan {
    final Block block;
    final int x, y, z, metadata;
    final boolean silk;
    final List<ItemStack> drops = new ArrayList<ItemStack>();
    private final net.minecraft.tileentity.TileEntity tile;
    private final net.minecraft.nbt.NBTTagCompound tileState;
    private Block replacement;
    private int replacementMetadata;
    HarvestPlan(Block block, int x, int y, int z, int metadata, boolean silk, net.minecraft.tileentity.TileEntity tile) {
        this.block = block; this.x = x; this.y = y; this.z = z; this.metadata = metadata; this.silk = silk;
        this.tile = tile; tileState = state(tile);
    }
    private static net.minecraft.nbt.NBTTagCompound state(net.minecraft.tileentity.TileEntity tile) {
        if (tile == null) return null;
        net.minecraft.nbt.NBTTagCompound n = new net.minecraft.nbt.NBTTagCompound(); tile.writeToNBT(n); return n;
    }
    boolean matches(Block block, int x, int y, int z, int metadata, boolean silk, net.minecraft.tileentity.TileEntity current) {
        return this.block == block && this.x == x && this.y == y && this.z == z && this.metadata == metadata && this.silk == silk
            && tile == current && (tileState == null ? current == null : tileState.equals(state(current)));
    }
    /** Some real ore callbacks replace themselves with gas while calculating their drops. */
    boolean restorePreparedReplacement(net.minecraft.world.World world) {
        Block after = world.getBlock(x,y,z); int meta = world.getBlockMetadata(x,y,z);
        if (after == block && meta == metadata) return true;
        // Machine/ore TileEntity mutations cannot be rolled back safely through block placement alone.
        if (tile != null || world.getTileEntity(x,y,z) != null) return false;
        replacement = after; replacementMetadata = meta;
        world.setBlock(x,y,z,block,metadata,2);
        return world.getBlock(x,y,z) == block && world.getBlockMetadata(x,y,z) == metadata;
    }
    void applyReplacement(net.minecraft.world.World world) {
        if (replacement != null) world.setBlock(x,y,z,replacement,replacementMetadata,3);
    }
}
