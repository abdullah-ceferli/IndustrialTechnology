package dev.industrialtechnology.quarry;
import dev.industrialtechnology.machine.BlockMachine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
public final class BlockQuarry extends BlockMachine {
    private final int tier;
    public BlockQuarry(int tier) { super("quarry_mk" + (tier + 1)); this.tier = tier; }
    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return tier == 0 ? new TileQuarry.MK1() : tier == 1 ? new TileQuarry.MK2() : new TileQuarry.MK3();
    }
}
