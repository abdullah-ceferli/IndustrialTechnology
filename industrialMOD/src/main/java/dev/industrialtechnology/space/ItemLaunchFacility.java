package dev.industrialtechnology.space;
import java.util.List;import java.util.UUID;import net.minecraft.item.*;import net.minecraft.block.Block;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;import net.minecraftforge.common.util.ForgeDirection;
public final class ItemLaunchFacility extends ItemBlock {
    public ItemLaunchFacility(Block b){super(b);setMaxStackSize(1);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int face,float a,float b,float c){if(face!=1)return false;int yy=y+2;if(yy+LaunchFacilityPlan.MAX_Y>254||!w.isAirBlock(x,yy,z)||!p.canPlayerEdit(x,yy,z,face,stack))return false;if(w.isRemote)return true;
        if(!w.setBlock(x,yy,z,field_150939_a,0,3))return false;TileLaunchFacility t=(TileLaunchFacility)w.getTileEntity(x,yy,z);if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("Controller")){t.readFromNBT(stack.getTagCompound().getCompoundTag("Controller"));t.xCoord=x;t.yCoord=yy;t.zCoord=z;}
        field_150939_a.onBlockPlacedBy(w,x,yy,z,p,stack);t.facilityId=UUID.randomUUID().toString();t.phase=0;t.cursor=0;t.preparedChunks=0;t.owner=p.getUniqueID().toString();t.armStart=-1;t.markDirty();if(!p.capabilities.isCreativeMode)stack.stackSize--;return true;
    }
    @Override public void addInformation(ItemStack s,EntityPlayer p,List out,boolean advanced){out.add("LC-39 facility kit / 1 block = 1 metre");out.add("119 x 99 m hardstand, 49 x 41 x 8 m MLP");out.add("75 m service tower / lightning mast to 106 m");out.add("Place on clear ground: extends 74 m behind you.");out.add("Builds in batches; occupied/protected cells abort and refund.");out.add("Breaking any part dismantles the owner's whole kit.");}
}
