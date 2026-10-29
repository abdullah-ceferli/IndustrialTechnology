package dev.industrialtechnology.test;
import java.util.*;import java.lang.reflect.*;
import cpw.mods.fml.common.Loader;
import dev.industrialtechnology.capacitor.*;import dev.industrialtechnology.core.ModRegistry;
import net.minecraft.block.Block;import net.minecraft.world.WorldServer;import net.minecraft.world.WorldSettings;import net.minecraft.item.*;import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.*;
/** Uses the installed mods' real block placement and tick-driven cable networks, via optional reflection. */
public final class BankPipeChecks {
    private static final List<TileCapacitorBank[]> routes=new ArrayList<TileCapacitorBank[]>();
    private static TileCapacitorBank bank(WorldServer w,int x,int z,boolean source){w.getChunkFromChunkCoords(x>>4,z>>4);w.setBlock(x,70,z,ModRegistry.capacitorBank,3,3);TileCapacitorBank b=(TileCapacitorBank)w.getTileEntity(x,70,z);if(source){ItemStack s=new ItemStack(ModRegistry.capacitorBank);net.minecraft.nbt.NBTTagCompound t=new net.minecraft.nbt.NBTTagCompound();t.setInteger("BankVersion",1);t.setLong("StoredRF",1000000);s.setTagCompound(t);b.restoreItem(s);}for(int side=0;side<6;side++)b.setSide(side,3);b.setSide(source?5:4,source?1:0);return b;}
    public static void setup(WorldServer w)throws Exception{
        routes.clear();if(!Loader.isModLoaded("ThermalDynamics"))return;
        TileCapacitorBank a=bank(w,4000,3300,true),b=bank(w,4004,3300,false);
        FakePlayer p=new FakePlayer(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BankPipeTest")){@Override public void sendPlayerAbilities(){}};p.theItemInWorldManager.setGameType(WorldSettings.GameType.SURVIVAL);p.setPosition(4000,70,3302);
        Object duct=Class.forName("cofh.thermaldynamics.duct.TDDucts").getField("energyResonant").get(null);ItemStack item=(ItemStack)duct.getClass().getField("itemStack").get(duct);
        for(int i=1;i<=3;i++){w.setBlock(4000+i,69,3300,Blocks.stone);if(!item.copy().tryPlaceItemIntoWorld(p,w,4000+i,69,3300,1,.5F,1,.5F))throw new AssertionError("Actual fluxduct placement failed");}
        routes.add(new TileCapacitorBank[]{a,b});
        if(!Loader.isModLoaded("EnderIO"))return;
        a=bank(w,4040,3300,true);b=bank(w,4044,3300,false);
        Class<?> conduit=Class.forName("crazypants.enderio.conduit.power.PowerConduit"),iface=Class.forName("crazypants.enderio.conduit.IConduit"),cm=Class.forName("crazypants.enderio.conduit.ConnectionMode"),rs=Class.forName("crazypants.enderio.machine.RedstoneControlMode");
        Block bundle=(Block)Class.forName("crazypants.enderio.EnderIO").getField("blockConduitBundle").get(null);
        for(int i=1;i<=3;i++){
            w.setBlock(4040+i,70,3300,bundle,0,3);Object tile=w.getTileEntity(4040+i,70,3300),power=conduit.getConstructor(int.class).newInstance(2);
            tile.getClass().getMethod("addConduit",iface).invoke(tile,power);
            if(i==1){conduit.getMethod("setConnectionMode",ForgeDirection.class,cm).invoke(power,ForgeDirection.WEST,cm.getField("INPUT").get(null));conduit.getMethod("setExtractionRedstoneMode",rs,ForgeDirection.class).invoke(power,rs.getField("IGNORE").get(null),ForgeDirection.WEST);}
            if(i==3)conduit.getMethod("setConnectionMode",ForgeDirection.class,cm).invoke(power,ForgeDirection.EAST,cm.getField("OUTPUT").get(null));
        }
        routes.add(new TileCapacitorBank[]{a,b});
    }
    public static void verify(java.util.function.Consumer<String> pass){
        for(int i=0;i<routes.size();i++){TileCapacitorBank[] r=routes.get(i);long from=r[0].network().ledger.stored(),to=r[1].network().ledger.stored();if(to<=0||from>=1000000||from+to>1000000)throw new AssertionError("Real cable route "+i+" failed: source="+from+" sink="+to);pass.accept((i==0?"Thermal Dynamics resonant fluxducts":"Ender IO ender energy conduits")+" move real RF from output bank through three cable blocks into input bank (sink "+to+" RF)");}
    }
}
