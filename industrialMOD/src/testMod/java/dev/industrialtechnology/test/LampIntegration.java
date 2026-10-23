package dev.industrialtechnology.test;
import java.util.*;import java.util.function.BiConsumer;import com.mojang.authlib.GameProfile;
import net.minecraft.world.*;import net.minecraft.entity.player.*;import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.item.crafting.*;import net.minecraft.inventory.*;import net.minecraftforge.common.*;import net.minecraftforge.common.util.*;
import dev.industrialtechnology.core.*;
public final class LampIntegration {
    private static void check(boolean b,String text){if(!b)throw new AssertionError(text);}
    public static void run(WorldServer w,BiConsumer<String,Runnable> test){
        test.accept("Space registrations, dimensions, fluids and recipes are absent; green lamp remains",()->{check(!DimensionManager.isDimensionRegistered(180)&&!DimensionManager.isDimensionRegistered(181),"retired dimensions registered");for(String n:ContentMigration.retiredNames()){check(cpw.mods.fml.common.registry.GameRegistry.findBlock("industrialtech",n)==null,"retired block "+n);check(cpw.mods.fml.common.registry.GameRegistry.findItem("industrialtech",n)==null,"retired item "+n);}for(String n:new String[]{"crude_oil","kerosene","oxygen","liquid_oxygen","breathable_air"})check(net.minecraftforge.fluids.FluidRegistry.getFluid("industrialtech_"+n)==null,"retired fluid "+n);check(cpw.mods.fml.common.registry.GameRegistry.findBlock("industrialtech","green_redstone_lamp")==ModRegistry.greenLamp,"lamp registry identity changed");});
        test.accept("Green lamp still crafts four blocks with its original glass, green dye, glowstone and redstone recipe",()->{InventoryCrafting c=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);for(int i:new int[]{0,1,2,6,8})c.setInventorySlotContents(i,new ItemStack(Blocks.glass));c.setInventorySlotContents(3,new ItemStack(Items.dye,1,2));c.setInventorySlotContents(4,new ItemStack(Blocks.glowstone));c.setInventorySlotContents(5,new ItemStack(Items.dye,1,2));c.setInventorySlotContents(7,new ItemStack(Items.redstone));ItemStack result=CraftingManager.getInstance().findMatchingRecipe(c,w);check(result!=null&&result.getItem()==Item.getItemFromBlock(ModRegistry.greenLamp)&&result.stackSize==4,"lamp recipe missing");});
        for(int dimension:new int[]{180,181})test.accept("Saved player from removed dimension "+dimension+" returns before login with inventory and XP intact",()->{
            EntityPlayerMP p=new FakePlayer(w,new GameProfile(UUID.randomUUID(),"RecoveryChecks")){@Override public void sendPlayerAbilities(){}};
            p.inventory.mainInventory[0]=new ItemStack(Items.diamond,23);p.experienceLevel=37;
            net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();p.writeToNBT(n);n.setInteger("Dimension",dimension);p.readFromNBT(n);
            check(p.dimension==0&&p.worldObj==w&&p.fallDistance==0,"dimension not recovered");check(p.inventory.mainInventory[0].getItem()==Items.diamond&&p.inventory.mainInventory[0].stackSize==23&&p.experienceLevel==37,"inventory or XP changed");
        });
        test.accept("Overworld player data is unaffected by retired dimension recovery",()->{
            EntityPlayerMP p=new FakePlayer(w,new GameProfile(UUID.randomUUID(),"NormalChecks")){@Override public void sendPlayerAbilities(){}};p.setPosition(2250.5,90,2250.5);
            net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();p.writeToNBT(n);p.readFromNBT(n);check(p.dimension==0&&p.posX==2250.5&&p.posY==90&&p.posZ==2250.5,"ordinary position changed");
        });
        for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS)test.accept("Survival lever attaches to green lamp face "+side+" and switches real light without falling off",()->{
            int x=2200+side.ordinal()*8,y=70,z=2200;for(int cx=(x-20)>>4;cx<=(x+20)>>4;cx++)for(int cz=(z-20)>>4;cz<=(z+20)>>4;cz++)w.getChunkFromChunkCoords(cx,cz);
            for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS)w.setBlockToAir(x+d.offsetX,y+d.offsetY,z+d.offsetZ);w.setBlock(x,y,z,ModRegistry.greenLamp,0,3);
            EntityPlayerMP p=new FakePlayer(w,new GameProfile(UUID.fromString("a119f069-337c-4e54-999c-5cb44df0e020"),"LampChecks")){@Override public void sendPlayerAbilities(){}};p.theItemInWorldManager.setGameType(WorldSettings.GameType.SURVIVAL);p.capabilities.allowEdit=true;p.setPosition(x+side.offsetX*2+.5,y+side.offsetY*2+.5,z+side.offsetZ*2+.5);p.inventory.currentItem=0;ItemStack lever=new ItemStack(Blocks.lever);p.inventory.mainInventory[0]=lever;
            check(p.theItemInWorldManager.activateBlockOrUseItem(p,w,p.getHeldItem(),x,y,z,side.ordinal(),.5f,.5f,.5f),"lever placement rejected");int lx=x+side.offsetX,ly=y+side.offsetY,lz=z+side.offsetZ;check(w.getBlock(lx,ly,lz)==Blocks.lever&&lever.stackSize==0,"lever was not placed/consumed");
            Blocks.lever.onBlockActivated(w,lx,ly,lz,p,side.ordinal(),.5f,.5f,.5f);check(ModRegistry.greenLamp.getLightValue(w,x,y,z)==15&&w.getSavedLightValue(EnumSkyBlock.Block,x,y,z)>=15,"lever did not light lamp");check(w.getBlock(lx,ly,lz)==Blocks.lever,"lever detached when lamp lit");
            Blocks.lever.onBlockActivated(w,lx,ly,lz,p,side.ordinal(),.5f,.5f,.5f);check(ModRegistry.greenLamp.getLightValue(w,x,y,z)==0,"lever did not extinguish lamp");check(w.getBlock(lx,ly,lz)==Blocks.lever,"lever detached when lamp switched off");
            if(side==ForgeDirection.UP)Blocks.lever.onBlockActivated(w,lx,ly,lz,p,side.ordinal(),.5f,.5f,.5f);
        });
    }
    public static void restore(WorldServer w,BiConsumer<String,Runnable> test){test.accept("Saved green lamp and directly attached powered lever survive a real JVM restart",()->{int x=2208,y=70,z=2200;for(int cx=(x-20)>>4;cx<=(x+20)>>4;cx++)for(int cz=(z-20)>>4;cz<=(z+20)>>4;cz++)w.getChunkFromChunkCoords(cx,cz);check(w.getBlock(x,y,z)==ModRegistry.greenLamp&&w.getBlock(x,y+1,z)==Blocks.lever,"lamp/lever missing");check((w.getBlockMetadata(x,y+1,z)&8)!=0&&ModRegistry.greenLamp.getLightValue(w,x,y,z)==15,"saved lamp lost power");check(w.isSideSolid(x,y,z,ForgeDirection.UP),"saved lamp no longer supports lever");});}
}
