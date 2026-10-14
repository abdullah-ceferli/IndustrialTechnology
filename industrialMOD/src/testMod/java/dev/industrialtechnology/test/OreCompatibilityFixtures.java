package dev.industrialtechnology.test;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import java.util.ArrayList;

/** Test-only blocks reproduce modded ore contracts without adding any dependency to the release. */
public final class OreCompatibilityFixtures {
    public static Block unclassified, highLevel, customTool, tileOre, energyMachine, replacingOre;
    public static void register() {
        unclassified=register(new Block(Material.rock){}.setHardness(3),"ore_no_tool");
        highLevel=register(new Block(Material.rock){}.setHardness(3),"ore_high_level");highLevel.setHarvestLevel("pickaxe",12);
        customTool=register(new Block(Material.rock){}.setHardness(3),"ore_custom_tool");customTool.setHarvestLevel("drill",9);
        tileOre=register(new TileOreBlock(),"ore_with_tile");energyMachine=register(new EnergyBlock(),"ore_registered_energy_machine");
        replacingOre=register(new ReplacingOre(),"ore_replacing_itself");
        GameRegistry.registerTileEntity(OreTile.class,"industrialtech_tests:ore_variant");
        GameRegistry.registerTileEntity(EnergyTile.class,"industrialtech_tests:energy_tile");
        OreDictionary.registerOre("oreCompatibilityNatural",new ItemStack(tileOre,1,7));
        OreDictionary.registerOre("oreCompatibilityEnergy",new ItemStack(energyMachine));
    }
    private static Block register(Block block,String name){block.setBlockName("industrialtech_tests."+name);GameRegistry.registerBlock(block,name);return block;}
    public static final class OreTile extends TileEntity {
        public int resource=7;
        @Override public void writeToNBT(net.minecraft.nbt.NBTTagCompound n){super.writeToNBT(n);n.setInteger("Resource",resource);}
        @Override public void readFromNBT(net.minecraft.nbt.NBTTagCompound n){super.readFromNBT(n);resource=n.getInteger("Resource");}
    }
    private static final class TileOreBlock extends Block {
        TileOreBlock(){super(Material.rock);setHardness(3);}
        @Override public boolean hasTileEntity(int meta){return true;}
        @Override public TileEntity createTileEntity(World world,int meta){return new OreTile();}
        @Override public ArrayList<ItemStack> getDrops(World world,int x,int y,int z,int meta,int fortune){
            ArrayList<ItemStack> drops=new ArrayList<ItemStack>();
            OreTile tile=(OreTile)world.getTileEntity(x,y,z);drops.add(new ItemStack(this,1,tile.resource));return drops;
        }
    }
    private static final class EnergyBlock extends Block {
        EnergyBlock(){super(Material.iron);setHardness(3);}
        @Override public boolean hasTileEntity(int meta){return true;}
        @Override public TileEntity createTileEntity(World world,int meta){return new EnergyTile();}
    }
    private static final class ReplacingOre extends Block {
        ReplacingOre(){super(Material.rock);setHardness(3);}
        @Override public void dropBlockAsItemWithChance(World world,int x,int y,int z,int meta,float chance,int fortune){
            world.setBlock(x,y,z,net.minecraft.init.Blocks.water,0,3);super.dropBlockAsItemWithChance(world,x,y,z,meta,chance,fortune);
        }
    }
    public static final class EnergyTile extends TileEntity implements IEnergyHandler {
        public int energy=5000;
        public boolean canConnectEnergy(ForgeDirection side){return true;}
        public int receiveEnergy(ForgeDirection side,int amount,boolean simulate){return 0;}
        public int extractEnergy(ForgeDirection side,int amount,boolean simulate){return 0;}
        public int getEnergyStored(ForgeDirection side){return energy;}
        public int getMaxEnergyStored(ForgeDirection side){return 5000;}
    }
}
