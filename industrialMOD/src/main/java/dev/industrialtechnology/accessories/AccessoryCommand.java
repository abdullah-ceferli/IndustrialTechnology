package dev.industrialtechnology.accessories;
import cpw.mods.fml.common.network.simpleimpl.*;import net.minecraft.entity.player.EntityPlayerMP;import net.minecraft.item.ItemStack;import dev.industrialtechnology.core.ServerWork;import io.netty.buffer.ByteBuf;
/** Only the sender's current held tool may be charged/fired. Work stays on the server thread. */
public final class AccessoryCommand implements IMessage {
 private int action,slot;public AccessoryCommand(){}public AccessoryCommand(int a,int s){action=a;slot=s;}
 public void toBytes(ByteBuf b){b.writeByte(action);b.writeByte(slot);}public void fromBytes(ByteBuf b){action=b.readUnsignedByte();slot=b.readUnsignedByte();}
 public static final class Handler implements IMessageHandler<AccessoryCommand,IMessage>{public IMessage onMessage(AccessoryCommand m,MessageContext ctx){EntityPlayerMP p=ctx.getServerHandler().playerEntity;ServerWork.enqueue(()->{if(p.isDead||m.slot>8||p.inventory.currentItem!=m.slot||p.openContainer!=p.inventoryContainer)return;ItemStack s=p.getHeldItem();if(s==null||!(s.getItem() instanceof ItemCatalyst))return;ItemCatalyst tool=(ItemCatalyst)s.getItem();long now=p.worldObj.getTotalWorldTime(),last=p.getEntityData().getLong("industrialtechAccessoryAction");if(p.getEntityData().hasKey("industrialtechAccessoryAction")&&now-last<3&&now>=last)return;p.getEntityData().setLong("industrialtechAccessoryAction",now);if(m.action==0)tool.changeCharge(p,s);else if(m.action==1)tool.shoot(p,s);p.inventory.markDirty();p.inventoryContainer.detectAndSendChanges();});return null;}}
}
