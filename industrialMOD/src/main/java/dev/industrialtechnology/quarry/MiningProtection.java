package dev.industrialtechnology.quarry;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.world.World;

/** Machines remain protected. Registered natural ore TileEntities can be harvested safely. */
public final class MiningProtection {
    private static final Set<Block> EXCLUDED = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    private MiningProtection() {}
    public static void exclude(Block block) { if (block != null) EXCLUDED.add(block); }
    public static boolean isProtected(World world, int x, int y, int z, Block block, int metadata) {
        if (EXCLUDED.contains(block)) return true;
        net.minecraft.tileentity.TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null && !block.hasTileEntity(metadata)) return false;
        if (tile instanceof dev.industrialtechnology.machine.TileMachine || tile instanceof net.minecraft.inventory.IInventory
            || tile instanceof cofh.api.energy.IEnergyConnection || tile instanceof net.minecraftforge.fluids.IFluidHandler) return true;
        // A mod must positively register this ore block item. Other TileEntities stay excluded.
        return !OreRecognition.isOre(block, metadata);
    }
}
