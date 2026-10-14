package dev.industrialtechnology.quarry;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Uses mod-provided ore dictionary registration, never display names or hardcoded mod ore lists. */
public final class OreRecognition {
    private static final Map<Long, Boolean> CACHE = new HashMap<Long, Boolean>();
    private static Set<Item> oreBlockItems;
    private static boolean oreName(String name) {
        return name.startsWith("ore") && name.length() > 3 && Character.isUpperCase(name.charAt(3));
    }
    public static boolean isOre(Block block, int metadata) {
        if (Item.getItemFromBlock(block) == null) return false;
        long key = ((long) Block.getIdFromBlock(block) << 32) | (metadata & 0xffffffffL);
        Boolean cached = CACHE.get(key); if (cached != null) return cached;
        boolean ore = false;
        for (int id : OreDictionary.getOreIDs(new ItemStack(block, 1, metadata))) {
            String name = OreDictionary.getOreName(id);
            if (oreName(name)) { ore = true; break; }
        }
        if (!ore) {
            // Some ore TileEntities store their resource variant in NBT rather than world metadata.
            // A registered ore variant identifies the shared ore block item; machines are checked separately.
            if (oreBlockItems == null) {
                oreBlockItems = new HashSet<Item>();
                for (String name : OreDictionary.getOreNames()) if (oreName(name))
                    for (ItemStack stack : OreDictionary.getOres(name)) if (stack != null && stack.getItem() instanceof net.minecraft.item.ItemBlock)
                        oreBlockItems.add(stack.getItem());
            }
            ore = oreBlockItems.contains(Item.getItemFromBlock(block));
        }
        if (CACHE.size() >= 8192) CACHE.clear(); CACHE.put(key, ore); return ore;
    }
    @SubscribeEvent public void registered(OreDictionary.OreRegisterEvent event) { CACHE.clear(); oreBlockItems = null; }
}
