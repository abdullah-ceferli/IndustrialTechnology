package dev.industrialtechnology.wireless;
import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.core.ModRegistry;
public final class ItemWirelessTerminal extends Item {
    public ItemWirelessTerminal(){setMaxStackSize(1);setUnlocalizedName("industrialtech.wireless_terminal");setTextureName("industrialtech:wireless_terminal");setCreativeTab(ModRegistry.TAB);}
    public static boolean held(EntityPlayer p){return p!=null&&p.getHeldItem()!=null&&p.getHeldItem().getItem()==ModRegistry.terminal;}
    public static void open(EntityPlayer p){
        if(p.worldObj.isRemote||!held(p))return;long now=p.worldObj.getTotalWorldTime();
        if(p.getEntityData().hasKey("IndustrialOpenTick")&&now-p.getEntityData().getLong("IndustrialOpenTick")<5)return;
        p.getEntityData().setLong("IndustrialOpenTick",now);UUID id=WirelessAccess.terminal(p,p.getHeldItem(),true);
        if(id==null){p.addChatMessage(new ChatComponentText("This terminal is owned by another player, or your network limit is reached."));return;}
        p.getEntityData().setString("IndustrialTerminal",id.toString());p.openGui(IndustrialTechnology.instance,1,p.worldObj,0,0,0);
    }
    @Override public ItemStack onItemRightClick(ItemStack stack,World w,EntityPlayer p){open(p);return stack;}
    @Override public boolean onItemUseFirst(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float a,float b,float c){
        if(w.isRemote)return false; // Allow the normal client block-use packet to reach the server.
        net.minecraft.tileentity.TileEntity tile=w.getTileEntity(x,y,z);
        if(tile instanceof TileRedstoneRelay)ItemWirelessStick.linkPlaced(p,(TileRedstoneRelay)tile);else open(p);return true;
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float a,float b,float c){return true;}
    @Override public boolean onBlockStartBreak(ItemStack stack,int x,int y,int z,EntityPlayer p){open(p);return true;}
    @Override public boolean onLeftClickEntity(ItemStack stack,EntityPlayer p,Entity entity){open(p);return true;}
}
