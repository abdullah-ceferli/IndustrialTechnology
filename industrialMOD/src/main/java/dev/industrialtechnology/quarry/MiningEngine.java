package dev.industrialtechnology.quarry;

import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.core.ModRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraft.server.MinecraftServer;

public final class MiningEngine {
    public enum Result { SKIPPED, MINED, WAIT }
    private MiningEngine() {}
    public static Result step(TileQuarry quarry) {
        quarry.ensureMiningHeight();
        if (!quarry.cursor.choose(quarry.selected)) return Result.WAIT;
        int x = quarry.cursor.x(quarry.xCoord), y = quarry.cursor.y(), z = quarry.cursor.z(quarry.zCoord);
        WorldServer world = (WorldServer) quarry.getWorldObj();
        if (!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) {
            quarry.status = dev.industrialtechnology.machine.MachineStatus.CHUNK_LOADING_ERROR;
            quarry.issue = "Target chunk is not loaded";
            return Result.WAIT;
        }
        if (y >= quarry.miningCeiling) { quarry.cursor.clampBelow(quarry.miningCeiling); quarry.markDirty(); return Result.SKIPPED; }
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (block.isAir(world, x, y, z) || block == Blocks.bedrock || block.getBlockHardness(world, x, y, z) < 0
            || MiningProtection.isProtected(world, x, y, z, block, meta)) {
            return skip(quarry);
        }
        if (!quarry.ready() || !quarry.output.pending.isEmpty()) return Result.WAIT;
        if (!quarry.filter.enabled && !quarry.output.hasSpace()) return full(quarry);
        FakePlayer player = FakePlayerFactory.get(world, quarry.ownerProfile());
        player.setPosition(x + .5, y + .5, z + .5);
        player.inventory.currentItem = 0;
        ItemStack drill = new ItemStack(ModRegistry.drill);
        dev.industrialtechnology.integration.ItemDrill.prepareQuarryTool(drill, block, meta);
        if (quarry.usesSilk()) drill.addEnchantment(Enchantment.silkTouch, 1);
        else if (quarry.fortuneLevel() > 0) drill.addEnchantment(Enchantment.fortune, quarry.fortuneLevel());
        player.inventory.setInventorySlotContents(0, drill);
        boolean removed = false;
        HarvestPlan plan = quarry.waitingHarvest;
        List<ItemStack> extra = new ArrayList<ItemStack>();
        try {
            if (!world.canMineBlock(player, x, y, z) || MinecraftServer.getServer().isBlockProtected(world, x, y, z, player)
                || !block.canHarvestBlock(player, meta)) return skip(quarry);
            // ForgeHooks.onBlockBreakEvent sends packets to a real connection; a 1.7.10 FakePlayer has none.
            // Its SURVIVAL permission path is this same cancellable event, after the checks above.
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(x, y, z, world, block, meta, player);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return skip(quarry);
            // Recheck: a permission handler may have changed the target during the event.
            if (world.getBlock(x, y, z) != block || world.getBlockMetadata(x, y, z) != meta) return skip(quarry);
            if (MiningProtection.isProtected(world, x, y, z, block, meta)) return skip(quarry);
            if (plan == null || !plan.matches(block, x, y, z, meta, quarry.usesSilk(), world.getTileEntity(x, y, z))) {
                plan = new HarvestPlan(block, x, y, z, meta, quarry.usesSilk(), world.getTileEntity(x, y, z));
                // Stage the actual Fortune/Silk harvest and Forge-modified drops while the block is intact.
                // Machines stay excluded; registered ore TileEntities retain their variant data during preparation.
                DropCapture.begin(quarry, plan.drops);
                try { block.harvestBlock(world, player, x, y, z, meta); }
                finally { DropCapture.end(); collectDirect(player, drill, plan.drops); }
                quarry.waitingHarvest = plan;
                DropCapture.begin(quarry, new ArrayList<ItemStack>());
                boolean restored;
                try { restored = plan.restorePreparedReplacement(world); }
                finally { DropCapture.end(); }
                if (!restored) {
                    quarry.miningFailure("Harvester made an unsupported TileEntity change during drop preparation"); return Result.WAIT;
                }
            }
            List<ItemStack> keep = new ArrayList<ItemStack>();
            for (ItemStack drop : plan.drops) if (!quarry.filter.matches(drop)) keep.add(drop);
            if (!quarry.output.canFit(keep)) return full(quarry);
            // Only now may the block be removed. Prepared drops remain uncommitted if removal fails.
            DropCapture.begin(quarry, extra);
            try {
                block.onBlockHarvested(world, x, y, z, meta, player);
                removed = block.removedByPlayer(world, player, x, y, z, true);
                if (removed) { block.onBlockDestroyedByPlayer(world, x, y, z, meta); plan.applyReplacement(world); }
            } finally {
                DropCapture.end(); collectDirect(player, drill, extra);
            }
            if (removed) {
                commit(quarry, plan.drops); commit(quarry, extra); quarry.output.flush(); quarry.waitingHarvest = null;
                quarry.harvested(); quarry.cursor.advance();
                if (!quarry.output.pending.isEmpty()) quarry.status = dev.industrialtechnology.machine.MachineStatus.OUTPUT_FULL;
                return Result.MINED;
            }
            return skip(quarry);
        } catch (RuntimeException e) {
            // A third-party harvester can fail after removal; never repeat that position or refund its drops.
            boolean changed = removed || world.getBlock(x, y, z) != block;
            if (changed) {
                if (plan != null) commit(quarry, plan.drops); commit(quarry, extra); quarry.output.flush();
                quarry.harvested(); quarry.cursor.advance(); quarry.waitingHarvest = null;
            }
            quarry.miningFailure("Harvest failed: " + block.getUnlocalizedName());
            IndustrialTechnology.log.error("Quarry harvest failed at " + x + "," + y + "," + z, e);
            return changed ? Result.MINED : Result.WAIT;
        } finally {
            player.inventory.setInventorySlotContents(0, null);
        }
    }
    private static void collectDirect(FakePlayer player, ItemStack drill, List<ItemStack> drops) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack != drill) { drops.add(stack.copy()); player.inventory.setInventorySlotContents(i, null); }
        }
    }
    private static void commit(TileQuarry quarry, List<ItemStack> drops) {
        for (ItemStack drop : drops) if (!quarry.filter.matches(drop)) quarry.output.capture(drop);
    }
    private static Result full(TileQuarry quarry) {
        quarry.status = dev.industrialtechnology.machine.MachineStatus.OUTPUT_FULL; return Result.WAIT;
    }
    private static Result skip(TileQuarry quarry) {
        quarry.waitingHarvest = null; quarry.skipped++; quarry.cursor.advance(); quarry.markDirty(); return Result.SKIPPED;
    }
}
