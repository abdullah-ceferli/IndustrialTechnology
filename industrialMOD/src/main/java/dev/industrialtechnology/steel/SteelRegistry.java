package dev.industrialtechnology.steel;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import dev.industrialtechnology.core.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** HBM-specific class linking is confined to the branch where HBM is present. */
public final class SteelRegistry {
    public static Block factory;
    public static Item core;
    public static boolean hasHBM;
    private SteelRegistry(){}
    public static void register(){
        hasHBM=Loader.isModLoaded("hbm");
        factory=new BlockSteelFactory();GameRegistry.registerBlock(factory,ItemSteelFactory.class,"bulk_steel_factory");
        core=new Item().setUnlocalizedName("industrialtech.steel_factory_core").setTextureName("industrialtech:steel_gear").setCreativeTab(ModRegistry.TAB);
        GameRegistry.registerItem(core,"steel_factory_core");
        if(hasHBM)HBMSteelBridge.register();
        else GameRegistry.registerTileEntity(TileSteelFactory.class,"industrialtech:bulk_steel_factory");
    }
    public static void recipes(){if(hasHBM)HBMSteelBridge.recipes();}
    public static net.minecraft.tileentity.TileEntity tile(){return hasHBM?HBMSteelBridge.tile():new TileSteelFactory();}
    public static ItemStack steel(int n){return hasHBM?HBMSteelBridge.steel(n):null;}
    public static int upgrade(ItemStack s){return hasHBM?HBMSteelBridge.upgrade(s):0;}
    public static boolean coal(ItemStack s){return hasHBM&&HBMSteelBridge.coal(s);}
}
