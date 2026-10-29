package dev.industrialtechnology.capacitor;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.ForgeHooks;
import dev.industrialtechnology.core.ServerWork;
/** Client sends settings only. Energy, membership and ownership are always authoritative. */
public final class BankCommand implements IMessage {
    private int window,action,side;private long value;
    public BankCommand(){}
    public BankCommand(int w,int a,long v,int s){window=w;action=a;value=v;side=s;}
    @Override public void toBytes(ByteBuf b){b.writeInt(window);b.writeByte(action);b.writeLong(value);b.writeByte(side);}
    @Override public void fromBytes(ByteBuf b){window=b.readInt();action=b.readUnsignedByte();value=b.readLong();side=b.readUnsignedByte();}
    public static final class Handler implements IMessageHandler<BankCommand,IMessage> {
        @Override public IMessage onMessage(final BankCommand m,MessageContext ctx){final EntityPlayerMP p=ctx.getServerHandler().playerEntity;ServerWork.enqueue(()->apply(p,m));return null;}
    }
    public static void apply(EntityPlayerMP p,BankCommand m){
        if(!(p.openContainer instanceof BankContainer))return;BankContainer c=(BankContainer)p.openContainer;
        if(c.windowId!=m.window||!c.canInteractWith(p)||!ForgeHooks.canInteractWith(p,c)||!c.allowAction())return;
        BankManager.Network n=c.bank.network();if(n==null||n.busy)return;
        if(m.action>=0&&m.action<=2){if(m.value < -1)return;n.configure(m.action,m.value);}
        else if(m.action==3){if(m.side>5||m.value<0||m.value>3)return;c.bank.setSide(m.side,(int)m.value);}else return;
        c.send(p);
    }
}
