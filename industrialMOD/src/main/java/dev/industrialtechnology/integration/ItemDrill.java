package dev.industrialtechnology.integration;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.core.ModRegistry;

/** Real Forge tool classes let modded blocks decide whether the machine may harvest them. */
public final class ItemDrill extends ItemPickaxe {
    private static final Set<String> CLASSES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("pickaxe", "shovel", "axe")));
    public ItemDrill() {
        super(Item.ToolMaterial.EMERALD);
        setUnlocalizedName(IndustrialTechnology.MODID + ".drill"); setTextureName(IndustrialTechnology.MODID + ":drill");
        setHarvestLevel("pickaxe", 4); setHarvestLevel("shovel", 4); setHarvestLevel("axe", 4);
        setCreativeTab(ModRegistry.TAB);
    }
    /** The quarry's virtual tool meets the block's declared level; the craftable hand tool stays level four. */
    public static void prepareQuarryTool(ItemStack stack, Block block, int metadata) {
        NBTTagCompound n = new NBTTagCompound();
        n.setBoolean("QuarryTool", true); n.setInteger("RequiredLevel", Math.max(4, block.getHarvestLevel(metadata)));
        String tool = block.getHarvestTool(metadata);
        if (tool != null && tool.length() <= 64) n.setString("RequiredTool", tool);
        stack.setTagCompound(n);
    }
    @Override public int getHarvestLevel(ItemStack stack, String tool) {
        NBTTagCompound n = stack.getTagCompound();
        if (n != null && n.getBoolean("QuarryTool") && (CLASSES.contains(tool) || tool != null && tool.equals(n.getString("RequiredTool"))))
            return Math.max(4, n.getInteger("RequiredLevel"));
        return super.getHarvestLevel(stack, tool);
    }
    @Override public Set<String> getToolClasses(ItemStack stack) {
        NBTTagCompound n = stack.getTagCompound();
        if (n == null || !n.getBoolean("QuarryTool") || n.getString("RequiredTool").isEmpty() || CLASSES.contains(n.getString("RequiredTool"))) return CLASSES;
        Set<String> classes = new HashSet<String>(CLASSES); classes.add(n.getString("RequiredTool")); return classes;
    }
}
