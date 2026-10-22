package dev.industrialtechnology.space;
import java.util.Random;
import net.minecraft.block.Block;import net.minecraft.init.*;import net.minecraft.item.*;
import cpw.mods.fml.common.registry.GameRegistry;import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.block.material.Material;import dev.industrialtechnology.core.ModRegistry;
/** Mineral-bearing host rocks; yields and deposit sizes are gameplay abstractions. */
public final class PlanetResources {
    public static final String[] NAMES={"lunar_iron_rock","lunar_ilmenite","lunar_feldspar","martian_hematite","martian_titanium_rock","martian_silica"};
    public static final Block[] ORES=new Block[6];public static Item titaniumFraction,aluminiumFraction,aluminium;
    private static Item item(String n){Item i=new Item().setUnlocalizedName("industrialtech."+n).setTextureName("industrialtech:"+n).setCreativeTab(ModRegistry.TAB);GameRegistry.registerItem(i,n);return i;}
    public static void register(){for(int i=0;i<6;i++){Block b=new Block(Material.rock){}.setBlockName("industrialtech."+NAMES[i]).setBlockTextureName("industrialtech:"+NAMES[i]).setHardness(3).setResistance(12).setCreativeTab(ModRegistry.TAB);b.setHarvestLevel("pickaxe",1);GameRegistry.registerBlock(b,NAMES[i]);ORES[i]=b;OreDictionary.registerOre(i==0||i==3?"oreIron":i==1||i==4?"oreTitanium":i==2?"oreAluminum":"oreQuartz",b);}
        OreDictionary.registerOre("oreAluminium",ORES[2]);titaniumFraction=item("titanium_concentrate");aluminiumFraction=item("aluminium_concentrate");aluminium=item("space_aluminium");OreDictionary.registerOre("ingotAluminum",aluminium);OreDictionary.registerOre("ingotAluminium",aluminium);
    }
    public static void recipes(){for(int i:new int[]{0,3})GameRegistry.addSmelting(ORES[i],new ItemStack(Items.iron_ingot),.7f);for(int i:new int[]{1,4})GameRegistry.addSmelting(ORES[i],new ItemStack(titaniumFraction,2),.5f);GameRegistry.addSmelting(ORES[2],new ItemStack(aluminiumFraction,2),.5f);GameRegistry.addSmelting(ORES[5],new ItemStack(Items.quartz,2),.2f);
        Item ti=HbmSpaceBridge.material("ingot_titanium"),al=HbmSpaceBridge.material("ingot_aluminium");if(al==null)al=aluminium;
        GameRegistry.addShapelessRecipe(new ItemStack(ti==null?SpaceRegistry.titanium:ti),titaniumFraction,titaniumFraction,titaniumFraction,titaniumFraction,titaniumFraction,titaniumFraction,titaniumFraction,titaniumFraction,titaniumFraction);
        GameRegistry.addShapelessRecipe(new ItemStack(al),aluminiumFraction,aluminiumFraction,aluminiumFraction,aluminiumFraction,aluminiumFraction,aluminiumFraction,aluminiumFraction,aluminiumFraction,aluminiumFraction);
        GameRegistry.addShapedRecipe(new ItemStack(SpaceRegistry.hull,4),"AAA","ASA","AAA",'A',al,'S',SpaceRegistry.steel);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.water_bucket),SpaceRegistry.ice,Items.bucket,Items.coal);
    }
    public static void generate(Block[] blocks,int cx,int cz,long seed,boolean mars,PlanetTerrain terrain){if(!SpaceConfig.planetResources)return;Random r=new Random(seed^((long)cx*341873128712L)^((long)cz*132897987541L)^0x5245534f55524345L);int first=mars?3:0;
        for(int kind=first;kind<first+3;kind++){boolean mare=terrain.mare(cx*16+8,cz*16+8);int veins=(kind==0?(mare?5:2):kind==1?(mare?3:1):kind==2?(mare?1:4):kind==3?5:kind==4?1:2)*SpaceConfig.resourceDensity;
            for(int v=0;v<veins;v++){int x=r.nextInt(16),z=r.nextInt(16),height=terrain.height(cx*16+x,cz*16+z),y=8+r.nextInt(Math.max(1,height-15));int steps=kind==0||kind==3?24:14;
                for(int step=0;step<steps;step++){for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++){int X=x+dx,Y=y+dy,Z=z+dz;if(X<0||X>15||Z<0||Z>15||Y<7||Y>=terrain.height(cx*16+X,cz*16+Z)-4||Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>1)continue;int idx=(X*16+Z)*256+Y;Block old=blocks[idx];if(old==SpaceRegistry.moonRock||old==SpaceRegistry.basalt||old==SpaceRegistry.regolith||old==SpaceRegistry.breccia||old==SpaceRegistry.marsRock||old==SpaceRegistry.marsLayer)blocks[idx]=ORES[kind];}
                    x=Math.max(0,Math.min(15,x+r.nextInt(3)-1));z=Math.max(0,Math.min(15,z+r.nextInt(3)-1));y=Math.max(8,Math.min(height-6,y+r.nextInt(3)-1));}
            }
        }
    }
}
