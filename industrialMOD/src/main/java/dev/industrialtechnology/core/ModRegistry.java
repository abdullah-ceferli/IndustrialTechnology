package dev.industrialtechnology.core;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.creativetab.CreativeTabs;
import dev.industrialtechnology.quarry.BlockQuarry;
import dev.industrialtechnology.quarry.TileQuarry;
import dev.industrialtechnology.chunkloading.BlockChunkLoader;
import dev.industrialtechnology.chunkloading.TileChunkLoader;
import dev.industrialtechnology.integration.ItemDrill;
import dev.industrialtechnology.quarry.DropCapture;
import net.minecraftforge.common.MinecraftForge;

public final class ModRegistry {
    public static final Block[] quarries = new Block[3];
    public static Block loader, redstoneRelay, monitoringStick, automationStick;
    public static Item drill, circuit, gear, terminal, greenStick, yellowStick, blueStick, automationGuide;
    public static final CreativeTabs TAB = new CreativeTabs(IndustrialTechnology.MODID) {
        @Override public Item getTabIconItem() { return Item.getItemFromBlock(quarries[0]); }
    };
    private ModRegistry() {}
    public static void register() {
        for (int i = 0; i < 3; i++) { quarries[i] = new BlockQuarry(i); GameRegistry.registerBlock(quarries[i], "quarry_mk" + (i + 1)); }
        loader = new BlockChunkLoader(); GameRegistry.registerBlock(loader, "chunk_loader");
        GameRegistry.registerTileEntity(TileQuarry.MK1.class, "industrialtech:quarry_mk1");
        GameRegistry.registerTileEntity(TileQuarry.MK2.class, "industrialtech:quarry_mk2");
        GameRegistry.registerTileEntity(TileQuarry.MK3.class, "industrialtech:quarry_mk3");
        GameRegistry.registerTileEntity(TileChunkLoader.class, "industrialtech:chunk_loader");
        redstoneRelay=new dev.industrialtechnology.wireless.BlockRedstoneRelay();GameRegistry.registerBlock(redstoneRelay,"redstone_relay");
        monitoringStick=new dev.industrialtechnology.wireless.BlockRedstoneRelay(dev.industrialtechnology.wireless.WirelessEndpoint.Kind.MONITOR,"monitoring_stick","green_stick");GameRegistry.registerBlock(monitoringStick,"monitoring_stick");
        automationStick=new dev.industrialtechnology.wireless.BlockRedstoneRelay(dev.industrialtechnology.wireless.WirelessEndpoint.Kind.AUTOMATION,"automation_stick","blue_stick");GameRegistry.registerBlock(automationStick,"automation_stick");
        GameRegistry.registerTileEntity(dev.industrialtechnology.wireless.TileRedstoneRelay.class,"industrialtech:redstone_relay");
        terminal=new dev.industrialtechnology.wireless.ItemWirelessTerminal();GameRegistry.registerItem(terminal,"wireless_terminal");
        greenStick=new dev.industrialtechnology.wireless.ItemWirelessStick(dev.industrialtechnology.wireless.WirelessEndpoint.Kind.MONITOR,"green_stick");GameRegistry.registerItem(greenStick,"green_stick");
        yellowStick=new dev.industrialtechnology.wireless.ItemWirelessStick(dev.industrialtechnology.wireless.WirelessEndpoint.Kind.CONTROL,"yellow_stick");GameRegistry.registerItem(yellowStick,"yellow_stick");
        blueStick=new dev.industrialtechnology.wireless.ItemWirelessStick(dev.industrialtechnology.wireless.WirelessEndpoint.Kind.AUTOMATION,"blue_stick");GameRegistry.registerItem(blueStick,"blue_stick");
        automationGuide=new dev.industrialtechnology.automation.ItemAutomationGuide();GameRegistry.registerItem(automationGuide,"automation_guide");
        drill = new ItemDrill(); GameRegistry.registerItem(drill, "drill");
        circuit = component("circuit"); gear = component("steel_gear");
        MinecraftForge.EVENT_BUS.register(new DropCapture());
    }
    private static Item component(String name) {
        Item item = new Item().setUnlocalizedName(IndustrialTechnology.MODID + "." + name)
            .setTextureName(IndustrialTechnology.MODID + ":" + name).setCreativeTab(TAB);
        GameRegistry.registerItem(item, name); return item;
    }
    public static void recipes() {
        GameRegistry.addShapelessRecipe(new ItemStack(automationGuide),Items.book,Items.redstone,Items.paper);
        GameRegistry.addRecipe(new ItemStack(terminal),"IGI","RDR","IEI",'I',Items.iron_ingot,'G',Items.gold_ingot,'R',Items.redstone,'D',Items.diamond,'E',Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(greenStick)," E "," R "," I ",'E',Items.emerald,'R',Items.redstone,'I',Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(yellowStick)," G "," R "," I ",'G',Items.gold_ingot,'R',Items.redstone,'I',Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(blueStick)," L "," R "," I ",'L',new ItemStack(Items.dye,1,4),'R',Items.redstone,'I',Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(circuit, 2), "RGR", "IPI", "RGR", 'R', Items.redstone, 'G', Items.gold_ingot, 'I', Items.iron_ingot, 'P', Items.paper);
        GameRegistry.addRecipe(new ItemStack(gear), " I ", "ISI", " I ", 'I', Items.iron_ingot, 'S', Blocks.stone);
        GameRegistry.addRecipe(new ItemStack(drill), " D ", "DID", " R ", 'D', Items.diamond, 'I', Items.iron_ingot, 'R', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(quarries[0]), "IGI", "CPC", "IRI", 'I', Items.iron_ingot, 'G', gear, 'C', circuit, 'P', Items.diamond_pickaxe, 'R', Blocks.redstone_block);
        GameRegistry.addRecipe(new ItemStack(quarries[1]), "DGD", "CQC", "IRI", 'D', Items.diamond, 'G', gear, 'C', circuit, 'Q', quarries[0], 'I', Blocks.iron_block, 'R', Blocks.redstone_block);
        GameRegistry.addRecipe(new ItemStack(quarries[2]), "EGE", "DQD", "CRC", 'E', Items.emerald, 'G', Blocks.gold_block, 'D', Blocks.diamond_block, 'Q', quarries[1], 'C', circuit, 'R', Blocks.redstone_block);
        GameRegistry.addRecipe(new ItemStack(loader), "IGI", "CEC", "IRI", 'I', Items.iron_ingot, 'G', Items.gold_ingot, 'C', circuit, 'E', Items.ender_pearl, 'R', Items.redstone);
    }
}
