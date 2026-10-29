package dev.industrialtechnology.test;
import cofh.api.energy.IEnergyReceiver;import net.minecraft.tileentity.TileEntity;import net.minecraft.block.BlockContainer;import net.minecraft.block.material.Material;import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;import cpw.mods.fml.common.registry.GameRegistry;import dev.industrialtechnology.capacitor.TileCapacitorBank;
public final class BankReceiverFixture extends TileEntity implements IEnergyReceiver {
    public static net.minecraft.block.Block block;
    public TileCapacitorBank source;public int accepted,limit=7000,reentrant=-1;
    public static void register(){block=new BlockContainer(Material.iron){public TileEntity createNewTileEntity(World w,int m){return new BankReceiverFixture();}};GameRegistry.registerBlock(block,"bank_receiver_fixture");GameRegistry.registerTileEntity(BankReceiverFixture.class,"industrialtech_tests:bank_receiver");}
    @Override public boolean canUpdate(){return false;}
    public boolean canConnectEnergy(ForgeDirection d){return true;}
    public int receiveEnergy(ForgeDirection d,int n,boolean simulate){int moved=Math.min(n,limit);if(!simulate){reentrant=source.extractEnergy(ForgeDirection.EAST,1000,false)+source.receiveEnergy(ForgeDirection.WEST,1000,false);accepted+=moved;}return moved;}
    public int getEnergyStored(ForgeDirection d){return accepted;}
    public int getMaxEnergyStored(ForgeDirection d){return 1000000;}
}
