package dev.industrialtechnology.integration;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Configuration;

/** Opt-in prerequisite recipes for the separately configured legacy HBM storage blocks. */
public final class LegacyHbmRecipes {
    public static boolean sparkCells;
    public static net.minecraft.item.ItemStack empty(net.minecraft.item.Item item){if(item==null)return null;net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(item);net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();n.setLong("charge",0);stack.setTagCompound(n);return stack;}
    public static final net.minecraft.item.Item[] cells=new net.minecraft.item.Item[4];
    public static void registerItems(){if(Loader.isModLoaded("hbm"))LegacyHbmSparkBridge.registerItems();}
    public static void load(Configuration c){sparkCells=c.getBoolean("restoreLegacyHbmSparkCellRecipes","compatibility",false,"Restore the five original Spark battery/cell crafting-grid recipes needed by the legacy Spark Energy Storage Block. HBM only; outputs are empty.");}
    public static void register(){if(sparkCells&&Loader.isModLoaded("hbm"))LegacyHbmSparkBridge.register();}
    private LegacyHbmRecipes(){}
}
