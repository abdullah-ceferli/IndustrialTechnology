package dev.industrialtechnology.space;
import java.util.*;
import cpw.mods.fml.common.registry.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.oredict.*;
import dev.industrialtechnology.core.*;
public final class SpaceRegistry {
    public static final String[] MACHINES={"research_computer","telescope","rocket_assembly","fuel_refinery","oxygen_plant","air_compressor","suit_refill_station","atmosphere_pressurizer","launch_controller","fuel_port","airlock_controller"};
    public static final int COMPUTER=0,TELESCOPE=1,ASSEMBLY=2,REFINERY=3,OXYGEN=4,COMPRESSOR=5,REFILL=6,PRESSURIZER=7,LAUNCH=8,PORT=9,AIRLOCK=10;
    public static final String[] PARTS={"rocket_engine","rocket_fuel_tank","oxidizer_tank","guidance_computer","navigation_electronics","rocket_hull","rocket_fins","rocket_nozzle","crew_capsule","heat_shield","landing_legs"};
    public static Block[] machines=new Block[MACHINES.length];public static Item[] parts=new Item[PARTS.length];
    public static Block launchpad,hull,glass,door,lamp,moonRock,regolith,basalt,breccia,lunarDust,marsRock,marsDust,marsLayer,ice;
    public static Item hdd,rocket,canister,steel,titanium,alloy,airFilter;public static ItemSpaceSuit[][] suits=new ItemSpaceSuit[3][4];
    public static Fluid crude,kerosene,oxygen,lox,air;
    private static Item item(String name){Item i=new Item().setUnlocalizedName("industrialtech."+name).setTextureName("industrialtech:"+name).setCreativeTab(ModRegistry.TAB);GameRegistry.registerItem(i,name);return i;}
    private static Block block(String name,Material material,float hardness){Block b=new Block(material){}.setBlockName("industrialtech."+name).setBlockTextureName("industrialtech:"+name).setHardness(hardness).setResistance(12).setCreativeTab(ModRegistry.TAB);GameRegistry.registerBlock(b,name);return b;}
    private static Fluid fluid(String name,int density,int temperature){Fluid f=new Fluid("industrialtech_"+name).setDensity(density).setTemperature(temperature);if(!FluidRegistry.registerFluid(f))f=FluidRegistry.getFluid(f.getName());return f;}
    public static void register(){

        for(int i=0;i<MACHINES.length;i++){machines[i]=new BlockSpaceMachine(i);GameRegistry.registerBlock(machines[i],MACHINES[i]);}
        GameRegistry.registerTileEntity(TileSpaceMachine.class,"industrialtech:space_machine");
        launchpad=new BlockSpaceStructure("rocket_launchpad",true);GameRegistry.registerBlock(launchpad,"rocket_launchpad");hull=new BlockSpaceStructure("airtight_hull",false);GameRegistry.registerBlock(hull,"airtight_hull");
        glass=new BlockGlass(Material.glass,false).setBlockName("industrialtech.airtight_glass").setBlockTextureName("industrialtech:airtight_glass").setHardness(2).setResistance(12).setCreativeTab(ModRegistry.TAB);GameRegistry.registerBlock(glass,"airtight_glass");
        door=new BlockAirlockDoor();GameRegistry.registerBlock(door,"airlock_door");lamp=new BlockGreenLamp();GameRegistry.registerBlock(lamp,"green_redstone_lamp");
        moonRock=block("moon_rock",Material.rock,2);regolith=block("lunar_regolith",Material.ground,.7f);basalt=block("lunar_basalt",Material.rock,3);breccia=block("lunar_breccia",Material.rock,2);lunarDust=block("lunar_dust",Material.ground,.5f);
        marsRock=block("mars_rock",Material.rock,2);marsDust=block("mars_regolith",Material.ground,.6f);marsLayer=block("mars_layered_rock",Material.rock,2);ice=block("subsurface_ice",Material.rock,1);
        for(int i=0;i<PARTS.length;i++)parts[i]=item(PARTS[i]);steel=item("space_steel");titanium=item("space_titanium");alloy=item("thermal_alloy");airFilter=item("air_filter").setMaxStackSize(1).setMaxDamage(256);
        hdd=new ItemSpaceData();GameRegistry.registerItem(hdd,"research_hdd");rocket=new ItemRocket();GameRegistry.registerItem(rocket,"assembled_rocket");canister=new ItemSpaceCanister();GameRegistry.registerItem(canister,"space_canister");
        for(int tier=0;tier<3;tier++)for(int slot=0;slot<4;slot++){suits[tier][slot]=new ItemSpaceSuit(tier,slot);GameRegistry.registerItem(suits[tier][slot],"spacesuit_"+(tier+1)+"_"+slot);}
        crude=fluid("crude_oil",900,300);kerosene=fluid("kerosene",810,300);oxygen=fluid("oxygen",-1,300);lox=fluid("liquid_oxygen",1140,90);air=fluid("breathable_air",-1,293);oxygen.setGaseous(true);air.setGaseous(true);
        EntityRegistry.registerModEntity(EntitySpaceRocket.class,"space_rocket",60,IndustrialTechnology.instance,384,2,true);
        PlanetResources.register();LaunchFacilityRegistry.register();
    }
    public static Object material(String nativeName,Item fallback){Item nativeItem=HbmSpaceBridge.material(nativeName);return nativeItem==null?fallback:nativeItem;}
    private static void shaped(ItemStack out,Object... r){GameRegistry.addRecipe(new ShapedOreRecipe(out,r));}
    public static void recipes(){
        PlanetResources.recipes();LaunchFacilityRegistry.recipes();
        Object s=material("ingot_steel",steel),t=material("ingot_titanium",titanium),sp=material("plate_steel",steel),tp=material("plate_titanium",titanium);
        // Standalone metallurgy remains deliberately expensive and also works without HBM.
        shaped(new ItemStack(steel,2),"CCC","III","CCC",'C',Items.coal,'I',Items.iron_ingot);
        shaped(new ItemStack(titanium,2),"SDS","DAD","SDS",'S',s,'D',Items.diamond,'A',alloy);
        shaped(new ItemStack(alloy,2),"SOS","OGO","SOS",'S',s,'O',Blocks.obsidian,'G',Items.gold_ingot);
        shaped(new ItemStack(airFilter),"WCW","IPI","WCW",'W',Blocks.wool,'C',Items.coal,'I',Items.iron_ingot,'P',Items.paper);
        shaped(new ItemStack(parts[4],2),"RGR","CDC","RGR",'R',Items.redstone,'G',Items.gold_ingot,'C',ModRegistry.circuit,'D',Items.diamond);
        shaped(new ItemStack(parts[7]),"SAS"," A ","SAS",'S',sp,'A',alloy);
        shaped(new ItemStack(parts[0]),"TNT","SCS","ANA",'T',t,'N',parts[7],'S',sp,'C',parts[4],'A',alloy);
        shaped(new ItemStack(parts[1]),"SSS","S S","SSS",'S',sp);shaped(new ItemStack(parts[2]),"TAT","A A","TAT",'T',tp,'A',alloy);
        shaped(new ItemStack(parts[3]),"ECE","CDC","ECE",'E',parts[4],'C',ModRegistry.circuit,'D',Items.diamond);
        shaped(new ItemStack(parts[5]),"TTT","S S","TTT",'T',tp,'S',sp);shaped(new ItemStack(parts[6])," T ","TST","TST",'T',tp,'S',sp);
        shaped(new ItemStack(parts[9]),"AAA","OOO","AAA",'A',alloy,'O',Blocks.obsidian);
        shaped(new ItemStack(parts[8]),"TGT","ECE","TH T".replace(" ",""),'T',tp,'G',glass,'E',parts[4],'C',parts[3],'H',parts[9]);
        shaped(new ItemStack(parts[10]),"STS","SGS","T T",'S',sp,'T',tp,'G',ModRegistry.gear);
        shaped(new ItemStack(hdd),"SSS","ECE","SSS",'S',sp,'E',parts[4],'C',Items.comparator);
        shaped(new ItemStack(canister,4)," S ","S S","SSS",'S',sp);
        shaped(new ItemStack(launchpad,16),"SSS","IRI","SSS",'S',sp,'I',Blocks.iron_block,'R',Items.redstone);
        shaped(new ItemStack(hull,8),"SSS","SAS","SSS",'S',sp,'A',alloy);shaped(new ItemStack(glass,8),"GGG","GAG","GGG",'G',Blocks.glass,'A',alloy);
        shaped(new ItemStack(door),"HGH","HCH","HHH",'H',hull,'G',glass,'C',parts[4]);
        shaped(new ItemStack(lamp,4),"GGG","DLD","GRG",'G',Blocks.glass,'D',new ItemStack(Items.dye,1,2),'L',Blocks.glowstone,'R',Items.redstone);
        for(int i=0;i<machines.length;i++){
            if(i==TELESCOPE)shaped(new ItemStack(machines[i]),"GSG","SDS","STS",'G',Blocks.glass,'S',sp,'D',Items.diamond,'T',t);
            else shaped(new ItemStack(machines[i]),"SCS","EAE","STS",'S',sp,'C',i==COMPUTER?parts[3]:parts[4],'E',i==OXYGEN||i==COMPRESSOR?airFilter:ModRegistry.circuit,'A',i==ASSEMBLY?Blocks.anvil:alloy,'T',i==PORT?parts[1]:t);
        }
        String[][] patterns={{"AGA","A A","   "},{"A A","AFA","AAA"},{"AAA","A A","A A"},{"   ","A A","A A"}};
        for(int tier=0;tier<3;tier++)for(int slot=0;slot<4;slot++){
            Object mat=tier==0?s:tier==1?t:alloy;String[] p=patterns[slot];shaped(new ItemStack(suits[tier][slot]),p[0],p[1],p[2],'A',mat,'G',glass,'F',airFilter);
            if(tier>0)shaped(new ItemStack(suits[tier][slot]),"AAA","ASA","AAA",'A',mat,'S',suits[tier-1][slot]);
        }
        GameRegistry.addSmelting(new ItemStack(ice),new ItemStack(Items.snowball,4),.1f);
    }
    public static Fluid fluidFor(int n){return n==1?crude:n==2?kerosene:n==3?oxygen:n==4?lox:n==5?air:null;}
}
