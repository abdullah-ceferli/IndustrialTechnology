package dev.industrialtechnology.test;
import java.util.*;import java.io.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;
import cpw.mods.fml.common.FMLCommonHandler;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiMainMenu;import net.minecraft.world.*;import net.minecraft.item.*;import net.minecraft.util.ScreenShotHelper;
import dev.industrialtechnology.core.IndustrialTechnology;import dev.industrialtechnology.integration.LegacyHbmRecipes;
import com.hbm.blocks.ModBlocks;import com.hbm.items.ModItems;import com.hbm.items.machine.ItemBattery;
import codechicken.nei.recipe.*;import codechicken.nei.PositionedStack;import org.lwjgl.opengl.GL11;
public final class LegacyStorageClientChecks {
    private final List<String> report=new ArrayList<String>();private int phase,view,recipeIndex,wait=30,age;private boolean done,failed;
    private static final String[] NAMES={"standard-assembler","lithium-assembler","schrabidium-assembler","spark-assembler","spark-battery-grid","spark-cell-6-grid","spark-cell-25-grid","spark-cell-100-grid","spark-cell-1000-grid"};
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    private ItemStack target(){switch(view){case 0:return new ItemStack(ModBlocks.machine_battery);case 1:return new ItemStack(ModBlocks.machine_lithium_battery);case 2:return new ItemStack(ModBlocks.machine_schrabidium_battery);case 3:return new ItemStack(ModBlocks.machine_dineutronium_battery);case 4:return LegacyHbmRecipes.empty(ModItems.battery_spark);default:return LegacyHbmRecipes.empty(LegacyHbmRecipes.cells[view-5]);}}
    private PositionedStack result(IRecipeHandler h,int recipe){PositionedStack out=h.getResultStack(recipe);if(out!=null&&out.item.isItemEqual(target()))return out;for(PositionedStack other:h.getOtherStacks(recipe))if(other.item!=null&&other.item.isItemEqual(target()))return other;return null;}
    private IRecipeHandler selected(){GuiRecipe g=(GuiRecipe)Minecraft.getMinecraft().currentScreen;return g.getCurrentRecipeHandlers().get(g.recipetype);}
    private void open(Minecraft mc){
        mc.displayGuiScreen(null);check(GuiCraftingRecipe.openRecipeGui("item",target()),"No NEI recipe "+NAMES[view]);
        GuiRecipe g=(GuiRecipe)mc.currentScreen;List<? extends IRecipeHandler> handlers=g.getCurrentRecipeHandlers();int[] sizes={3,3,3,4,3,6,8,8,9};
        for(int i=0;i<handlers.size();i++){
            IRecipeHandler h=handlers.get(i);String c=h.getClass().getName();boolean type=view<4?c.startsWith("com.hbm."):c.startsWith("codechicken.nei.")&&c.contains("Shaped");
            if(!type)continue;
            for(int j=0;j<h.numRecipes();j++){
                if(result(h,j)==null||!result(h,j).item.isItemEqual(target())||h.getIngredientStacks(j).size()!=sizes[view])continue;
                ItemStack out=result(h,j).item;if(view>=4&&(!out.hasTagCompound()||out.getTagCompound().getLong("charge")!=0))continue;
                recipeIndex=j;g.recipetype=i;g.page=j/Math.max(1,h.recipiesPerPage());g.refreshPage();return;
            }
        }
        StringBuilder debug=new StringBuilder();for(IRecipeHandler h:handlers){debug.append(h.getClass().getName()).append(" count=").append(h.numRecipes()).append(" name=").append(h.getRecipeName());if(h.numRecipes()>0)debug.append(" result=").append(h.getResultStack(0)).append(" inputs=").append(h.getIngredientStacks(0).size());debug.append("; ");}ScreenShotHelper.saveScreenshot(mc.mcDataDir,"nei-matcher-debug.png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());throw new AssertionError("Original NEI handler missing for "+NAMES[view]+" :: "+debug);
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(done||e.phase!=TickEvent.Phase.END||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();try{
        if(phase==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.showInventoryAchievementHint=false;WorldSettings s=new WorldSettings(271,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);s.func_82750_a("2;7;1;");mc.launchIntegratedServer("legacy-storage-nei","Disposable original HBM recipes QA",s);phase=1;wait=70;return;}
        if(mc.thePlayer==null||mc.theWorld==null){if(++age>100)throw new AssertionError("join timeout");wait=10;return;}
        if(phase==1){mc.displayGuiScreen(null);TooltipClientChecks.prepare(mc);check(LegacyHbmRecipes.sparkCells,"Spark recipes not enabled");open(mc);phase=2;wait=25;return;}
        check(mc.currentScreen instanceof GuiCraftingRecipe,"NEI page closed");IRecipeHandler h=selected();List<PositionedStack> input=h.getIngredientStacks(recipeIndex);int[] sizes={3,3,3,4,3,6,8,8,9};check(input.size()==sizes[view],"Ingredient count "+input.size()+" expected "+sizes[view]);check(result(h,recipeIndex).item.isItemEqual(target()),"Wrong result");
        if(view<4){int[][] counts={{1,12,12},{8,12,12},{16,12,12},{24,12,1,32}};for(int j=0;j<input.size();j++)check(input.get(j).item.stackSize==counts[view][j],"Original ingredient amount");}
        else {check(result(h,recipeIndex).item.getTagCompound()!=null&&result(h,recipeIndex).item.getTagCompound().getLong("charge")==0,"Nonempty crafted cell");}
        check(GL11.glGetError()==0,"NEI OpenGL error");ScreenShotHelper.saveScreenshot(mc.mcDataDir,"hbm-original-"+NAMES[view]+".png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());report.add("PASS: Original "+h.getRecipeName()+" NEI handler displays "+NAMES[view]+" ["+target().getDisplayName()+"] with original HBM icons and ingredient layout");
        if(++view==9){finish();return;}open(mc);wait=25;
    }catch(Throwable t){failed=true;report.add("FAIL: view "+view+" :: "+t);IndustrialTechnology.log.error("Legacy storage NEI",t);finish();}}
    private void finish(){done=true;try{report.add("Failed: "+(failed?1:0));Files.write(new File(Minecraft.getMinecraft().mcDataDir,"legacy-storage-client.txt").toPath(),report,StandardCharsets.UTF_8);}catch(Exception e){IndustrialTechnology.log.error("Legacy storage report",e);}Minecraft.getMinecraft().shutdown();}
}
