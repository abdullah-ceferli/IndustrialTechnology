package dev.industrialtechnology.machine;

import java.util.Random;
import dev.industrialtechnology.core.IndustrialTechnology;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.MathHelper;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockMachine extends BlockContainer {
    private final String texture;
    @SideOnly(Side.CLIENT) private IIcon front, frontActive, side, top, bottom;
    protected BlockMachine(String name) {
        super(Material.iron); texture = name;
        setBlockName(IndustrialTechnology.MODID + "." + name); setHardness(4.5f); setResistance(12);
        setStepSound(soundTypeMetal); setHarvestLevel("pickaxe", 1);
        setCreativeTab(dev.industrialtechnology.core.ModRegistry.TAB);
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileMachine)
            player.openGui(IndustrialTechnology.instance, 0, world, x, y, z);
        return true;
    }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int rotation = MathHelper.floor_double(entity.rotationYaw * 4.0 / 360.0 + .5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, new int[]{2, 5, 3, 4}[rotation], 2);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileMachine && entity instanceof EntityPlayer) ((TileMachine) te).setOwner((EntityPlayer) entity);
    }
    @Override public int damageDropped(int metadata) { return 0; }
    @Override public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileMachine && !world.isRemote) {
            TileMachine machine = (TileMachine) te;
            machine.destroyed();
            if (machine instanceof TilePoweredMachine) {
                TilePoweredMachine powered = (TilePoweredMachine) machine;
                for (int i = 0; i < powered.getSizeInventory(); i++) {
                    drop(world, x, y, z, powered.getStackInSlot(i)); powered.output.slots[i] = null;
                }
                for (ItemStack stack : powered.output.pending) drop(world, x, y, z, stack);
                powered.output.pending.clear();
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
    private void drop(World world, int x, int y, int z, ItemStack stack) {
        if (stack == null) return;
        int count = stack.stackSize;
        while (count > 0) {
            ItemStack piece = stack.copy(); piece.stackSize = Math.min(count, piece.getMaxStackSize()); count -= piece.stackSize;
            EntityItem entity = new EntityItem(world, x + .5, y + .6, z + .5, piece);
            entity.delayBeforeCanPickup = 10; world.spawnEntityInWorld(entity);
        }
    }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r) {
        String prefix = IndustrialTechnology.MODID + ":" + texture;
        side = r.registerIcon(prefix + "_side"); top = r.registerIcon(prefix + "_top"); bottom = r.registerIcon(prefix + "_bottom");
        front = r.registerIcon(prefix + "_front"); frontActive = r.registerIcon(prefix + "_front_active");
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int face, int meta) {
        return face == 0 ? bottom : face == 1 ? top : face == (meta < 2 ? 3 : meta) ? front : side;
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(IBlockAccess world, int x, int y, int z, int face) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (face == world.getBlockMetadata(x, y, z) && te instanceof TileMachine && ((TileMachine) te).enabled) return frontActive;
        return getIcon(face, world.getBlockMetadata(x, y, z));
    }
}
