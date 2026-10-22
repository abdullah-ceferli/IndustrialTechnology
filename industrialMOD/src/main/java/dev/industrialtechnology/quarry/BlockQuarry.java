package dev.industrialtechnology.quarry;
import java.util.ArrayList;
import dev.industrialtechnology.machine.BlockMachine;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
public final class BlockQuarry extends BlockMachine {
    private final int tier;
    public BlockQuarry(int tier) { super("quarry_mk" + (tier + 1)); this.tier = tier; }
    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return tier == 0 ? new TileQuarry.MK1() : tier == 1 ? new TileQuarry.MK2() : new TileQuarry.MK3();
    }
    private ItemStack configuredItem(World world, int x, int y, int z) {
        ItemStack stack = new ItemStack(this); TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileQuarry) {
            NBTTagCompound tag = new NBTTagCompound(); tag.setTag(ItemBlockQuarry.SETTINGS, QuarryItemSettings.pack(((TileQuarry) tile).itemSettings()));
            stack.setTagCompound(tag);
        }
        return stack;
    }
    @Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>(); drops.add(configuredItem(world, x, y, z)); return drops;
    }
    // Forge 1.7.10 normally removes the TileEntity before harvestBlock. Keep it until getDrops reads settings.
    @Override public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
    }
    @Override public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        try { super.harvestBlock(world, player, x, y, z, metadata); }
        finally { if (!world.isRemote && world.getBlock(x, y, z) == this) world.setBlockToAir(x, y, z); }
    }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileQuarry) ((TileQuarry) tile).applyItemSettings(stack);
        }
    }
}
