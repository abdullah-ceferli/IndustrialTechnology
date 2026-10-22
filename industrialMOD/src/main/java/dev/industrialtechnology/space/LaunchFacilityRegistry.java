package dev.industrialtechnology.space;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.item.*;import net.minecraft.init.*;import cpw.mods.fml.common.registry.GameRegistry;import dev.industrialtechnology.core.ModRegistry;
public final class LaunchFacilityRegistry {
    public static Block facility,part;public static Item section;
    public static void register(){part=new BlockLaunchPart();GameRegistry.registerBlock(part,"launch_facility_part");facility=new BlockLaunchFacility();GameRegistry.registerBlock(facility,ItemLaunchFacility.class,"shuttle_launch_facility");GameRegistry.registerTileEntity(TileLaunchFacility.class,"industrialtech:launch_facility");section=new Item().setUnlocalizedName("industrialtech.launch_facility_section").setTextureName("industrialtech:launch_facility_section").setCreativeTab(ModRegistry.TAB);GameRegistry.registerItem(section,"launch_facility_section");}
    public static void recipes(){GameRegistry.addRecipe(new ItemStack(section),"IPI","PCP","IPI",'I',Blocks.iron_block,'P',SpaceRegistry.launchpad,'C',SpaceRegistry.hull);GameRegistry.addRecipe(new ItemStack(facility),"SSS","SCS","SSS",'S',section,'C',SpaceRegistry.machines[SpaceRegistry.LAUNCH]);}
}
