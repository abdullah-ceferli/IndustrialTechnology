package dev.industrialtechnology.integration.nei;
import java.util.*;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.oredict.OreDictionary;
import dev.industrialtechnology.steel.*;
import dev.industrialtechnology.client.GuiSteelFactory;
import org.lwjgl.opengl.GL11;

public final class BulkSteelRecipeHandler extends TemplateRecipeHandler {
    public static final String ID="industrialtech.bulksteel";
    @Override public String getRecipeName(){return "Bulk Steel Factory";}
    @Override public String getGuiTexture(){return "hbm:textures/gui/GUIDiFurnace.png";}
    @Override public String getOverlayIdentifier(){return ID;}
    @Override public int recipiesPerPage(){return 1;}
    @Override public void loadCraftingRecipes(String id,Object... results){if(ID.equals(id)){if(SteelRegistry.hasHBM)arecipes.add(new Recipe());}else super.loadCraftingRecipes(id,results);}
    @Override public void loadCraftingRecipes(ItemStack result){ItemStack steel=SteelRegistry.steel(1);if(steel!=null&&result!=null&&steel.isItemEqual(result))arecipes.add(new Recipe());}
    @Override public void loadUsageRecipes(ItemStack input){if(SteelRegistry.hasHBM&&(TileSteelFactory.iron(input)||TileSteelFactory.coal(input)||input!=null&&input.getItem()==net.minecraft.item.Item.getItemFromBlock(SteelRegistry.factory)))arecipes.add(new Recipe());}
    private static List<ItemStack> alternatives(String ore,ItemStack fallback,int count){List<ItemStack> all=new ArrayList<ItemStack>();for(ItemStack item:OreDictionary.getOres(ore)){ItemStack s=item.copy();s.stackSize=count;all.add(s);}if(all.isEmpty()){fallback.stackSize=count;all.add(fallback);}return all;}
    public final class Recipe extends CachedRecipe {
        private final List<PositionedStack> inputs=Arrays.asList(new PositionedStack(alternatives("ingotIron",new ItemStack(Items.iron_ingot),16),32,54),new PositionedStack(alternatives("dustCoal",new ItemStack(com.hbm.items.ModItems.powder_coal),4),32,18));
        @Override public List<PositionedStack> getIngredients(){return getCycledIngredients(cycleticks/20,inputs);}
        @Override public PositionedStack getResult(){return new PositionedStack(SteelRegistry.steel(16),87,36);}
        @Override public PositionedStack getOtherStack(){return new PositionedStack(new ItemStack(SteelRegistry.factory),114,36);}
    }
    @Override public void drawBackground(int recipe){dev.industrialtechnology.client.SteelGuiArt.process(0,0,1000000,cycleticks%101,100,true,true);}
    @Override public void drawExtras(int recipe){Minecraft mc=Minecraft.getMinecraft();mc.fontRenderer.drawString("16 iron + 4 coal dust",5,77,0x404040);mc.fontRenderer.drawString("5s / 500 HE/t",5,89,0x404040);mc.fontRenderer.drawString("50,000 HE per batch",5,101,0x404040);mc.fontRenderer.drawString("2 optional HBM upgrades",5,117,0x404040);}
}
