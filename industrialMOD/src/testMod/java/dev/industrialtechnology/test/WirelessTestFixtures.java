package dev.industrialtechnology.test;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.*;
import net.minecraftforge.common.util.ForgeDirection;
public final class WirelessTestFixtures {
    public static Block tanks,ports;
    public static int portActivations;
    public static void register(){tanks=new Block(Material.iron){@Override public String getLocalizedName(){return "Chemical Tank";}@Override public boolean hasTileEntity(int meta){return true;}@Override public TileEntity createTileEntity(World w,int meta){return new Tanks();}}.setBlockName("industrialtech_tests.tanks");GameRegistry.registerBlock(tanks,"monitor_tanks");GameRegistry.registerTileEntity(Tanks.class,"industrialtech_tests:monitor_tanks");
        ports=new Block(Material.iron){@Override public boolean hasTileEntity(int meta){return true;}@Override public TileEntity createTileEntity(World w,int meta){return new Ports();}
            @Override public boolean onBlockActivated(World w,int x,int y,int z,net.minecraft.entity.player.EntityPlayer player,int side,float hx,float hy,float hz){portActivations++;return true;}}.setBlockName("industrialtech_tests.ports");
        GameRegistry.registerBlock(ports,"machine_ports");GameRegistry.registerTileEntity(Ports.class,"industrialtech_tests:machine_ports");
    }
    public static final class Ports extends net.minecraft.tileentity.TileEntityChest implements net.minecraft.inventory.ISidedInventory {
        public int[] getAccessibleSlotsFromSide(int side){return side==2?new int[]{0}:side==3?new int[]{1}:new int[0];}
        public boolean canInsertItem(int slot,net.minecraft.item.ItemStack item,int side){return side==2&&slot==0;}
        public boolean canExtractItem(int slot,net.minecraft.item.ItemStack item,int side){return side==3&&slot==1;}
    }
    public static final class Tanks extends TileEntity implements IFluidHandler {
        public int reads;public boolean unavailable;
        public final FluidTank water=new FluidTank(new FluidStack(FluidRegistry.WATER,1000),4000),lava=new FluidTank(new FluidStack(FluidRegistry.LAVA,2000),8000);
        public int fill(ForgeDirection side,FluidStack fluid,boolean commit){return 0;}
        public FluidStack drain(ForgeDirection side,FluidStack fluid,boolean commit){return null;}
        public FluidStack drain(ForgeDirection side,int amount,boolean commit){return null;}
        public boolean canFill(ForgeDirection side,Fluid fluid){return false;}public boolean canDrain(ForgeDirection side,Fluid fluid){return false;}
        public FluidTankInfo[] getTankInfo(ForgeDirection side){reads++;if(unavailable)throw new IllegalStateException("Tank unavailable");return new FluidTankInfo[]{water.getInfo(),lava.getInfo()};}
        @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setTag("Water",water.writeToNBT(new NBTTagCompound()));n.setTag("Lava",lava.writeToNBT(new NBTTagCompound()));}
        @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);if(n.hasKey("Water"))water.readFromNBT(n.getCompoundTag("Water"));if(n.hasKey("Lava"))lava.readFromNBT(n.getCompoundTag("Lava"));}
    }
}
