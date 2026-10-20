package dev.industrialtechnology.wireless;

import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import dev.industrialtechnology.core.ModRegistry;

/** Actual colored torches. Registration is optional until a terminal is available. */
public final class ItemWirelessStick extends Item {
    public final WirelessEndpoint.Kind kind;
    public ItemWirelessStick(WirelessEndpoint.Kind kind,String name){
        this.kind=kind;setMaxStackSize(16);setUnlocalizedName("industrialtech."+name);
        setTextureName("industrialtech:"+name);setCreativeTab(ModRegistry.TAB);
    }
    public static Item item(WirelessEndpoint.Kind kind){
        return kind==WirelessEndpoint.Kind.MONITOR?ModRegistry.greenStick:kind==WirelessEndpoint.Kind.CONTROL?ModRegistry.yellowStick:ModRegistry.blueStick;
    }
    public static Block block(WirelessEndpoint.Kind kind){
        return kind==WirelessEndpoint.Kind.MONITOR?ModRegistry.monitoringStick:kind==WirelessEndpoint.Kind.CONTROL?ModRegistry.redstoneRelay:ModRegistry.automationStick;
    }
    @Override public boolean onItemUseFirst(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float hx,float hy,float hz){
        // Returning true on the client stops PlayerControllerMP before it sends C08.
        if(w.isRemote)return false;
        if(stack==null||stack.stackSize<1||side<0||side>5||!allowed(p,w,x,y,z,side,stack))return true;
        TileEntity clicked=w.getTileEntity(x,y,z);
        if(clicked instanceof TileRedstoneRelay){linkPlaced(p,(TileRedstoneRelay)clicked);return true;}
        Block support=w.getBlock(x,y,z);
        if(support.isReplaceable(w,x,y,z)&&clicked==null){y--;side=1;support=w.getBlock(x,y,z);}
        if(!allowed(p,w,x,y,z,side,stack)||w.isAirBlock(x,y,z)||support.getMaterial().isLiquid())return true;
        ForgeDirection d=ForgeDirection.getOrientation(side);int rx=x+d.offsetX,ry=y+d.offsetY,rz=z+d.offsetZ;
        if(!allowed(p,w,rx,ry,rz,side,stack)||!w.getBlock(rx,ry,rz).isReplaceable(w,rx,ry,rz)){
            message(p,"That face needs an empty space for the torch.");return true;
        }
        BlockSnapshot snapshot=BlockSnapshot.getBlockSnapshot(w,rx,ry,rz);
        if(!w.setBlock(rx,ry,rz,block(kind),side,3))return true;
        if(ForgeEventFactory.onPlayerBlockPlace(p,snapshot,d).isCanceled()){snapshot.restore(true,false);return true;}
        TileEntity placed=w.getTileEntity(rx,ry,rz);
        if(!(placed instanceof TileRedstoneRelay)){snapshot.restore(true,false);return true;}
        TileRedstoneRelay torch=(TileRedstoneRelay)placed;torch.placer=p.getUniqueID();torch.markDirty();
        if(!p.capabilities.isCreativeMode)stack.stackSize--;
        if(WirelessAccess.active(p)!=null)linkPlaced(p,torch);
        else message(p,"Torch placed. Carry a Wireless Terminal and right-click this torch to link it.");
        return true;
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float hx,float hy,float hz){
        return true; // Client swing only; server placement already ran through onItemUseFirst.
    }
    private static boolean allowed(EntityPlayer p,World w,int x,int y,int z,int side,ItemStack stack){
        return WirelessAccess.loaded(w,x,y,z)&&p.getDistanceSq(x+.5,y+.5,z+.5)<=64&&p.canPlayerEdit(x,y,z,side,stack)&&w.canMineBlock(p,x,y,z);
    }
    public static boolean linkPlaced(EntityPlayer p,TileRedstoneRelay torch){
        World w=torch.getWorldObj();if(w==null||w.isRemote)return false;
        Block block=torch.getBlockType();if(!(block instanceof BlockRedstoneRelay))return false;
        WirelessEndpoint.Kind kind=((BlockRedstoneRelay)block).kind;
        if(!allowed(p,w,torch.xCoord,torch.yCoord,torch.zCoord,torch.getBlockMetadata(),p.getHeldItem()))return false;
        if(torch.placer!=null&&!torch.placer.equals(p.getUniqueID())){message(p,"This torch belongs to another player.");return false;}
        UUID network=WirelessAccess.active(p);
        if(network==null){message(p,"Carry your own Wireless Terminal to link this torch.");return false;}
        WirelessData data=WirelessData.get(w);WirelessEndpoint existing=data.devices.get(torch.endpoint);
        if(existing!=null){
            message(p,existing.owner.equals(p.getUniqueID())&&existing.network.equals(network)?"Already linked: "+existing.name:"Unlink this torch from its existing owner's terminal first.");return false;
        }
        ForgeDirection d=ForgeDirection.getOrientation(torch.getBlockMetadata());
        int x=torch.xCoord-d.offsetX,y=torch.yCoord-d.offsetY,z=torch.zCoord-d.offsetZ;
        if(!allowed(p,w,x,y,z,torch.getBlockMetadata(),p.getHeldItem())||w.isAirBlock(x,y,z))return false;
        WirelessEndpoint e=null;
        for(WirelessEndpoint candidate:data.linked(network)){
            if(candidate.kind==kind&&candidate.dimension==w.provider.dimensionId&&candidate.x==x&&candidate.y==y&&candidate.z==z&&candidate.side==torch.getBlockMetadata()){
                if(candidate.hasPhysicalStick()||WirelessAccess.status(candidate,candidate.dimension)==WirelessAccess.Status.DISCONNECTED){
                    message(p,"Unlink the previous registration on this face first.");return false;
                }
                e=candidate;break; // Upgrade a 1.4.0 virtual Green/Blue link without losing its rules/name.
            }
        }
        boolean fresh=e==null;
        if(fresh){
            if(data.linked(network).size()>=WirelessConfig.endpoints||data.devices.size()>=WirelessConfig.globalEndpoints){message(p,"Torch placed but not linked: wireless device limit reached.");return false;}
            e=new WirelessEndpoint();e.network=network;e.owner=p.getUniqueID();e.kind=kind;e.dimension=w.provider.dimensionId;e.x=x;e.y=y;e.z=z;e.side=torch.getBlockMetadata();
            e.block=String.valueOf(Block.blockRegistry.getNameForObject(w.getBlock(x,y,z)));e.machine=WirelessData.clean(w.getBlock(x,y,z).getLocalizedName(),64);
            e.name=WirelessData.clean((kind==WirelessEndpoint.Kind.MONITOR?"Monitor: ":kind==WirelessEndpoint.Kind.CONTROL?"Control: ":"Logic: ")+e.machine,32);
            TileEntity target=w.getTileEntity(x,y,z);if(target!=null)e.tileToken=TargetAnchors.bind(target);
        }
        e.physical=true;e.enabled=true;
        if(fresh&&!data.add(e))return false;
        data.markDirty();torch.endpoint=e.id;torch.placer=p.getUniqueID();torch.markDirty();w.markBlockForUpdate(torch.xCoord,torch.yCoord,torch.zCoord);
        message(p,"Linked "+e.name+". Open the terminal to configure it.");return true;
    }
    private static void message(EntityPlayer p,String text){p.addChatMessage(new ChatComponentText(text));}
}
