package dev.industrialtechnology.steel;
import com.hbm.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import dev.industrialtechnology.core.ModRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraftforge.oredict.ShapedOreRecipe;
final class HBMSteelBridge {
    static void register(){GameRegistry.registerTileEntity(TileHBMSteelFactory.class,"industrialtech:bulk_steel_factory");}
    static TileSteelFactory tile(){return new TileHBMSteelFactory();}
    static ItemStack steel(int n){return new ItemStack(ModItems.ingot_steel,n);}
    static boolean coal(ItemStack s){return s!=null&&s.getItem()==ModItems.powder_coal;}
    static int upgrade(ItemStack s){
        if(s==null||s.stackSize<=0||!(s.getItem() instanceof com.hbm.items.machine.ItemMachineUpgrade))return 0;
        com.hbm.items.machine.ItemMachineUpgrade u=(com.hbm.items.machine.ItemMachineUpgrade)s.getItem();
        if(u.tier<1||u.tier>3)return 0;
        int kind=u.type==com.hbm.items.machine.ItemMachineUpgrade.UpgradeType.SPEED?1:u.type==com.hbm.items.machine.ItemMachineUpgrade.UpgradeType.POWER?2:u.type==com.hbm.items.machine.ItemMachineUpgrade.UpgradeType.EFFECT?3:u.type==com.hbm.items.machine.ItemMachineUpgrade.UpgradeType.OVERDRIVE?4:0;
        return kind==0?0:kind*4+u.tier;
    }
    static void recipes(){
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SteelRegistry.core),"SSS","SDS","SSS",'S',"blockSteel",'D',Blocks.diamond_block));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SteelRegistry.factory),"TMT","CFC","TMT",'T',"blockTungsten",'M',ModItems.motor,'C',ModRegistry.circuit,'F',SteelRegistry.core));
    }
}
