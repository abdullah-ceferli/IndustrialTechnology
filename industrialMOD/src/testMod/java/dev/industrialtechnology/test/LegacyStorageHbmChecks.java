package dev.industrialtechnology.test;
import java.util.*;import java.io.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;
import com.google.gson.*;import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;import com.hbm.items.ModItems;import com.hbm.items.machine.ItemBattery;
import static com.hbm.inventory.OreDictManager.*;
import com.hbm.inventory.RecipesCommon.*;import com.hbm.inventory.recipes.AssemblyMachineRecipes;import com.hbm.inventory.recipes.loader.GenericRecipe;import com.hbm.module.machine.ModuleMachineAssembler;import api.hbm.energymk2.IEnergyHandlerMK2;
import net.minecraft.item.*;import net.minecraft.block.Block;import net.minecraft.world.WorldServer;import net.minecraft.init.Items;import net.minecraft.inventory.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.crafting.CraftingManager;
import dev.industrialtechnology.integration.LegacyHbmRecipes;
final class LegacyStorageHbmChecks {
    static final String[] NAMES={"ass.battery","ass.batterylithium","ass.batteryschrabidium","ass.batterydnt"};
    private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    static GenericRecipe[] expected(){return new GenericRecipe[]{
        new GenericRecipe(NAMES[0]).setup(100,100).outputItems(new ItemStack(ModBlocks.machine_battery)).inputItems(new OreDictStack(STEEL.plateWelded(),1),new OreDictStack(S.dust(),12),new OreDictStack(PB.dust(),12)),
        new GenericRecipe(NAMES[1]).setup(100,100).outputItems(new ItemStack(ModBlocks.machine_lithium_battery)).inputItems(new OreDictStack(ANY_PLASTIC.ingot(),8),new OreDictStack(CO.dust(),12),new OreDictStack(LI.dust(),12)),
        new GenericRecipe(NAMES[2]).setup(200,100).outputItems(new ItemStack(ModBlocks.machine_schrabidium_battery)).inputItems(new OreDictStack(DESH.ingot(),16),new OreDictStack(NP237.dust(),12),new OreDictStack(SA326.dust(),12)),
        new GenericRecipe(NAMES[3]).setup(400,100).outputItems(new ItemStack(ModBlocks.machine_dineutronium_battery)).inputItems(new OreDictStack(DNT.ingot(),24),new ComparableStack(ModItems.powder_spark_mix,12),new ComparableStack(LegacyHbmRecipes.cells[3],1),new OreDictStack(CMB.ingot(),32))};}
    static JsonObject json(GenericRecipe r)throws Exception{StringWriter out=new StringWriter();JsonWriter writer=new JsonWriter(out);writer.beginObject();AssemblyMachineRecipes.INSTANCE.writeRecipe(r,writer);writer.endObject();writer.close();return new JsonParser().parse(out.toString()).getAsJsonObject();}
    static void run(WorldServer w,LegacyStorageServerChecks test)throws Exception {
        final AssemblyMachineRecipes manager=AssemblyMachineRecipes.INSTANCE;
        JsonObject baseline=new JsonParser().parse(new FileReader("baseline-assembly.json")).getAsJsonObject();
        File file=new File("config/hbmRecipes/hbmAssemblyMachine.json");
        if(!new File("legacy-generated.done").exists()) {
            JsonObject complete=new JsonParser().parse(baseline.toString()).getAsJsonObject();JsonArray list=complete.getAsJsonArray("recipes"),added=new JsonArray();
            for(GenericRecipe recipe:expected()){JsonObject row=json(recipe);list.add(row);added.add(row);}
            Files.write(file.toPath(),new GsonBuilder().setPrettyPrinting().create().toJson(complete).getBytes(StandardCharsets.UTF_8));
            Files.write(Paths.get("added-legacy-recipes.json"),new GsonBuilder().setPrettyPrinting().create().toJson(added).getBytes(StandardCharsets.UTF_8));
            Files.write(Paths.get("legacy-generated.done"),Arrays.asList("generated"),StandardCharsets.UTF_8);manager.deleteRecipes();manager.readRecipeFile(file);
        }
        test.test("All original assembler JSON entries remain unchanged and only four legacy recipes are appended",()->{JsonObject actual=new JsonParser().parse(new FileReader(file)).getAsJsonObject();JsonArray a=actual.getAsJsonArray("recipes"),b=baseline.getAsJsonArray("recipes");check(a.size()==b.size()+4,"recipe count");for(int i=0;i<b.size();i++)check(a.get(i).equals(b.get(i)),"changed default "+i);});
        test.test("All four loaded HBM recipes match the last original ingredients, quantities, HE and duration",()->{for(GenericRecipe r:expected()){GenericRecipe actual=manager.recipeNameMap.get(r.getInternalName());check(actual!=null&&json(actual).equals(json(r)),r.getInternalName()+" mismatch");}});
        test.test("Every restored assembler ingredient resolves to real registered items or material ore entries",()->{for(GenericRecipe r:expected())for(AStack input:r.inputItem)check(!input.extractForNEI().isEmpty(),r.getInternalName()+": missing "+input);});
        for(int index=0;index<4;index++){final int i=index;test.test("Real HBM assembler module crafts "+NAMES[i]+" and consumes the exact original HE/materials",()->{
            GenericRecipe r=manager.recipeNameMap.get(NAMES[i]);ItemStack[] slots=new ItemStack[16];for(int j=0;j<r.inputItem.length;j++){slots[j]=r.inputItem[j].extractForNEI().get(0).copy();slots[j].stackSize=r.inputItem[j].stacksize;}
            w.setBlock(80+i,70,40,ModBlocks.machine_battery,0,3);IEnergyHandlerMK2 battery=(IEnergyHandlerMK2)w.getTileEntity(80+i,70,40);battery.setPower(100000);
            ModuleMachineAssembler m=new ModuleMachineAssembler(0,battery,slots);m.itemInput(0);m.itemOutput(15);m.setRecipe(r.getInternalName(),false);
            int ticks=0;while(slots[15]==null&&ticks<500){check(m.canProcess(r,1,1),"cannot process");m.process(r,1,1);ticks++;}
            check(slots[15]!=null&&slots[15].isItemEqual(r.outputItem[0].getSingle())&&slots[15].stackSize==1,"output");check(ticks>=r.duration&&ticks<=r.duration+1&&100000-battery.getPower()==ticks*r.power,"HE/time "+ticks+" "+battery.getPower());for(int j=0;j<r.inputItem.length;j++)check(slots[j]==null||slots[j].stackSize==0,"input remainder");
        });}
        test.test("Original 3x3 Spark cell crafting layouts produce the right cells with zero charge",()->{
            ItemStack plate=new ItemStack(ModItems.plate_dineutronium),blend=new ItemStack(ModItems.powder_spark_mix),wire=new OreDictStack(MAGTUNG.wireFine(),1).extractForNEI().get(0).copy();wire.stackSize=1;
            ItemStack base=craft(w,new String[]{"P","S","S"},'P',plate,'S',blend);check(base!=null&&base.getItem()==ModItems.battery_spark&&base.hasTagCompound()&&base.getTagCompound().getLong("charge")==0,"base battery");
            ItemStack six=craft(w,new String[]{"BW","PW","BW"},'B',base,'W',wire,'P',blend);checkCell(six,0);
            ItemStack twentyfive=craft(w,new String[]{"W W","SCS","PSP"},'W',wire,'S',blend,'C',six,'P',plate);checkCell(twentyfive,1);
            ItemStack hundred=craft(w,new String[]{"W W","BPB","SSS"},'W',wire,'B',twentyfive,'P',plate,'S',blend);checkCell(hundred,2);
            ItemStack thousand=craft(w,new String[]{"PCP","CSC","PCP"},'P',blend,'C',hundred,'S',new ItemStack(ModItems.singularity_spark));checkCell(thousand,3);
        });
        test.test("Restored cells retain the original 600M/2.5G/10G/100G HE capacities",()->{long[] expected={600000000L,2500000000L,10000000000L,100000000000L};for(int i=0;i<4;i++)check(((ItemBattery)LegacyHbmRecipes.cells[i]).getMaxCharge(new ItemStack(LegacyHbmRecipes.cells[i]))==expected[i],"cell "+i);});
        test.test("Opt-in setting is active and unrelated modern capacitor recipes remain registered",()->{check(LegacyHbmRecipes.sparkCells,"flag");check(manager.recipeNameMap.containsKey("ass.capacitorspark"),"modern capacitor recipe missing");});
    }
    static void checkCell(ItemStack s,int index){check(s!=null&&s.getItem()==LegacyHbmRecipes.cells[index]&&s.hasTagCompound()&&s.getTagCompound().getLong("charge")==0,"cell "+index);}
    static ItemStack craft(WorldServer w,String[] rows,Object... kv){Map<Character,ItemStack> map=new HashMap<Character,ItemStack>();for(int i=0;i<kv.length;i+=2)map.put((Character)kv[i],(ItemStack)kv[i+1]);InventoryCrafting grid=new InventoryCrafting(new Container(){@Override public boolean canInteractWith(EntityPlayer p){return true;}},3,3);for(int y=0;y<rows.length;y++)for(int x=0;x<rows[y].length();x++){ItemStack s=map.get(rows[y].charAt(x));if(s!=null){s=s.copy();s.stackSize=1;grid.setInventorySlotContents(y*3+x,s);}}return CraftingManager.getInstance().findMatchingRecipe(grid,w);}
}
