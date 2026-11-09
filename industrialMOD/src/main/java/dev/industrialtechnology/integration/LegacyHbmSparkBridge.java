package dev.industrialtechnology.integration;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import static com.hbm.inventory.OreDictManager.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Original pre-battery-rework recipes from HBM ef2b20d357ce159f7c3861cfecf8169796023a83. */
final class LegacyHbmSparkBridge {
    static void registerItems(){
        int[] sizes={6,25,100,1000};
        for(int i=0;i<sizes.length;i++){
            String name="battery_spark_cell_"+sizes[i];
            net.minecraft.item.Item existing=GameRegistry.findItem("hbm","item."+name);
            if(existing==null)existing=GameRegistry.findItem("hbm",name);
            if(existing!=null){LegacyHbmRecipes.cells[i]=existing;continue;}
            long rate=i==3?20000000L:2000000L;
            net.minecraft.item.Item item=new ItemBattery(100000000L*sizes[i],rate,rate).setUnlocalizedName(name).setMaxStackSize(1).setCreativeTab(LegacyHbmRecipes.sparkCells?dev.industrialtechnology.core.ModRegistry.TAB:null).setTextureName("industrialtech:legacy_hbm/"+name);
            GameRegistry.registerItem(item,"legacy_"+name);LegacyHbmRecipes.cells[i]=item;
        }
    }
    private static void add(ItemStack output,Object... pattern){
        if(output==null)return;
        GameRegistry.addRecipe(new ShapedOreRecipe(output,pattern));
    }
    static void register(){
        add(LegacyHbmRecipes.empty(ModItems.battery_spark),"P","S","S",'P',ModItems.plate_dineutronium,'S',ModItems.powder_spark_mix);
        add(LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[0]),"BW","PW","BW",'W',MAGTUNG.wireFine(),'P',ModItems.powder_spark_mix,'B',LegacyHbmRecipes.empty(ModItems.battery_spark));
        add(LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[1]),"W W","SCS","PSP",'W',MAGTUNG.wireFine(),'P',ModItems.plate_dineutronium,'S',ModItems.powder_spark_mix,'C',LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[0]));
        add(LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[2]),"W W","BPB","SSS",'W',MAGTUNG.wireFine(),'P',ModItems.plate_dineutronium,'S',ModItems.powder_spark_mix,'B',LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[1]));
        add(LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[3]),"PCP","CSC","PCP",'S',ModItems.singularity_spark,'P',ModItems.powder_spark_mix,'C',LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[2]));
    }
    private LegacyHbmSparkBridge(){}
}
