package dev.industrialtechnology.wireless;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
/** Authorizes an owned physical access point using the same owner profile for unattended work. */
public final class ReactorPermissions {
    public static boolean allow(UUID owner,WorldServer world,int x,int y,int z,int side){
        if(!WirelessAccess.loaded(world,x,y,z))return false;
        FakePlayer actor=FakePlayerFactory.get(world,new GameProfile(owner,"IndustrialReactor"));
        actor.setPosition(x+.5,y+.5,z+.5);actor.capabilities.isCreativeMode=false;actor.capabilities.allowEdit=true;
        actor.inventory.currentItem=0;actor.inventory.setInventorySlotContents(0,null);
        if(!world.canMineBlock(actor,x,y,z)||!actor.canPlayerEdit(x,y,z,side,null)||MinecraftServer.getServer().isBlockProtected(world,x,y,z,actor))return false;
        PlayerInteractEvent event=new PlayerInteractEvent(actor,PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,x,y,z,side,world);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled()&&event.useBlock!=cpw.mods.fml.common.eventhandler.Event.Result.DENY;
    }
    private ReactorPermissions(){}
}
