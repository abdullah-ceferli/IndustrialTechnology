package dev.industrialtechnology.accessories;
import cpw.mods.fml.common.registry.*;import dev.industrialtechnology.core.*;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraftforge.oredict.ShapedOreRecipe;
public final class AccessoryRegistry {
 public static Item flightRing,nightAmulet,ring,amulet,core,catalyst,hyperLens,catalyticLens,projectileIcon,lootIcon;
 public static void register(){
  ring=item(new Item(),"plain_ring");amulet=item(new Item(),"plain_amulet");core=new ItemMagicCore();GameRegistry.registerItem(core,"magic_core");
  flightRing=item(new ItemFlightRing(),"flight_ring");nightAmulet=item(new ItemNightAmulet(),"nightvision_amulet");
  catalyst=item(new ItemCatalyst(0),"destruction_catalyst");hyperLens=item(new ItemCatalyst(1),"hyperkinetic_lens");catalyticLens=item(new ItemCatalyst(2),"catalytic_lens");
  projectileIcon=item(new Item(),"lens_projectile").setCreativeTab(null);lootIcon=item(new Item(),"loot_ball").setCreativeTab(null);
  EntityRegistry.registerModEntity(EntityLensProjectile.class,"lens_projectile",2,IndustrialTechnology.instance,256,10,true);
  EntityRegistry.registerModEntity(EntityLootBall.class,"loot_ball",3,IndustrialTechnology.instance,64,10,true);
 }
 private static Item item(Item i,String n){i.setUnlocalizedName("industrialtech."+n).setTextureName("industrialtech:accessories/"+n).setCreativeTab(ModRegistry.TAB);GameRegistry.registerItem(i,n);return i;}
 public static void recipes(){
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ring)," G ","I I"," G ",'G',"nuggetGold",'I',"ingotIron"));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(amulet)," L ","L L"," I ",'L',Items.leather,'I',"ingotIron"));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(core,1,0)," R ","GDG"," R ",'R',"dustRedstone",'G',"dustGlowstone",'D',"gemDiamond"));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(core,1,1)," D ","RGR"," D ",'R',"dustRedstone",'G',"blockGold",'D',"gemDiamond"));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(core,1,2)," G ","RNR"," G ",'R',"dustRedstone",'G',"ingotGold",'N',Items.nether_star));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flightRing)," C ","FRF"," D ",'C',new ItemStack(core,1,2),'F',Items.feather,'R',ring,'D',"gemDiamond"));
  GameRegistry.addRecipe(new ItemStack(nightAmulet)," C ","GAG"," P ",'C',new ItemStack(core,1,1),'G',Items.golden_carrot,'A',amulet,'P',new ItemStack(Items.potionitem,1,8198));
  // User-requested ordinary-material alternatives: no ProjectE matter/fuel prerequisites.
  GameRegistry.addRecipe(new ItemStack(catalyst),"GOG","RDR","GOG",'G',Items.glowstone_dust,'O',Blocks.obsidian,'R',Blocks.redstone_block,'D',Items.diamond);
  GameRegistry.addRecipe(new ItemStack(hyperLens),"GQG","DED","GQG",'G',Blocks.glass,'Q',Items.quartz,'D',Items.diamond,'E',Items.ender_pearl);
  GameRegistry.addRecipe(new ItemStack(catalyticLens),"DND","HRC","DND",'D',Items.diamond,'N',Items.nether_star,'H',hyperLens,'R',Blocks.redstone_block,'C',catalyst);
 }
 private AccessoryRegistry(){}
}
