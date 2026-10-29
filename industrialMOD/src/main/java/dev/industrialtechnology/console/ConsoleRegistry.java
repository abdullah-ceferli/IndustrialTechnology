package dev.industrialtechnology.console;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.init.*;
import dev.industrialtechnology.core.ModRegistry;
public final class ConsoleRegistry {
    public static Block history,controller;
    public static void register(){history=new BlockConsole(false);controller=new BlockConsole(true);GameRegistry.registerBlock(history,ConsoleItem.class,"tank_history_monitor");GameRegistry.registerBlock(controller,ConsoleItem.class,"refinery_controller");GameRegistry.registerTileEntity(TileConsole.History.class,"industrialtech:tank_history_monitor");GameRegistry.registerTileEntity(TileConsole.Controller.class,"industrialtech:refinery_controller");}
    public static void recipes(){GameRegistry.addRecipe(new ItemStack(history),"IGI","CMC","IRI",'I',Items.iron_ingot,'G',Blocks.glass,'C',ModRegistry.circuit,'M',ModRegistry.greenStick,'R',Items.redstone);GameRegistry.addRecipe(new ItemStack(controller),"IBI","CMC","IYI",'I',Blocks.iron_block,'B',ModRegistry.blueStick,'C',ModRegistry.circuit,'M',Items.ender_pearl,'Y',ModRegistry.yellowStick);}
    public static final class ConsoleItem extends ItemBlock {
        public ConsoleItem(Block b){super(b);}
        @Override public int getItemStackLimit(ItemStack s){return s.hasTagCompound()?1:64;}
        @Override public void addInformation(ItemStack s,net.minecraft.entity.player.EntityPlayer p,java.util.List lines,boolean advanced){boolean c=((BlockConsole)field_150939_a).controller;lines.add(c?"16 tank/energy rules; Blue sensors -> Yellow outputs":"16 Green/Yellow sensors; fluid and HE/RF history");lines.add("Right-click with your Wireless Terminal to link");lines.add("No energy cost; loaded chunks only");if(s.hasTagCompound())lines.add("Settings retained; controller rules start paused");}
    }
}
