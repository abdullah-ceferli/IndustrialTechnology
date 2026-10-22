package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.item.*;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.creativetab.CreativeTabs;import net.minecraftforge.fluids.*;import dev.industrialtechnology.core.ModRegistry;
/** Finite 1 L canister. Own fluids only; all changes use simulated acceptance first. */
public final class ItemSpaceCanister extends Item implements IFluidContainerItem {
    public ItemSpaceCanister(){setMaxStackSize(1);setUnlocalizedName("industrialtech.space_canister");setTextureName("industrialtech:space_canister");setCreativeTab(ModRegistry.TAB);}
    @Override public boolean doesSneakBypassUse(net.minecraft.world.World w,int x,int y,int z,EntityPlayer p){return w.getBlock(x,y,z) instanceof BlockSpaceMachine;}
    @Override public FluidStack getFluid(ItemStack s){return s!=null&&s.hasTagCompound()?FluidStack.loadFluidStackFromNBT(s.getTagCompound().getCompoundTag("Fluid")):null;}
    @Override public int getCapacity(ItemStack s){return 1000;}
    @Override public int fill(ItemStack s,FluidStack r,boolean execute){if(s==null||r==null||r.amount<=0)return 0;boolean own=false;for(int i=1;i<=5;i++)if(r.getFluid()==SpaceRegistry.fluidFor(i))own=true;if(!own)return 0;FluidStack old=getFluid(s);if(old!=null&&!old.isFluidEqual(r))return 0;int n=Math.min(r.amount,1000-(old==null?0:old.amount));if(execute&&n>0){FluidStack v=r.copy();v.amount=n+(old==null?0:old.amount);NBTTagCompound tag=new NBTTagCompound();v.writeToNBT(tag);if(!s.hasTagCompound())s.setTagCompound(new NBTTagCompound());s.getTagCompound().setTag("Fluid",tag);}return n;}
    @Override public FluidStack drain(ItemStack s,int max,boolean execute){FluidStack old=getFluid(s);if(old==null||max<=0)return null;int n=Math.min(max,old.amount);FluidStack result=old.copy();result.amount=n;if(execute){old.amount-=n;if(old.amount<=0)s.getTagCompound().removeTag("Fluid");else{NBTTagCompound v=new NBTTagCompound();old.writeToNBT(v);s.getTagCompound().setTag("Fluid",v);}}return result;}
    @Override public void addInformation(ItemStack s,EntityPlayer p,List out,boolean a){FluidStack f=getFluid(s);out.add(f==null?"Empty (1000 mB)":f.getLocalizedName()+": "+f.amount+" mB");out.add("Right click a space machine; sneak to withdraw.");}
    @Override public void getSubItems(Item item,CreativeTabs tab,List out){out.add(new ItemStack(this));for(int i=1;i<=5;i++){ItemStack s=new ItemStack(this);fill(s,new FluidStack(SpaceRegistry.fluidFor(i),1000),true);out.add(s);}}
}
