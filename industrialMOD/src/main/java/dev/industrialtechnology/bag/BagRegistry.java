package dev.industrialtechnology.bag;

import net.minecraft.item.*;
import net.minecraft.init.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public final class BagRegistry {
    public static Item bag;
    public static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    public static boolean valid(ItemStack s){return s!=null&&s.getItem()==bag&&s.stackSize==1&&s.getItemDamage()>=0&&s.getItemDamage()<16;}
    public static int color(ItemStack s){int c=s==null?0:s.getItemDamage();return c>=0&&c<16?c:0;}
    public static void register(){bag=new ItemStorageBag();GameRegistry.registerItem(bag,"storage_bag");}
    public static void recipes(){for(int i=0;i<16;i++){
        GameRegistry.addRecipe(new ItemStack(bag,1,i),"WLW","LEL","WLW",'W',new ItemStack(Blocks.wool,1,i),'L',Items.leather,'E',Blocks.ender_chest);
        GameRegistry.addShapelessRecipe(new ItemStack(bag,1,i),new ItemStack(bag,1,OreDictionary.WILDCARD_VALUE),new ItemStack(Items.dye,1,15-i));
    }}
    private BagRegistry(){}
}
