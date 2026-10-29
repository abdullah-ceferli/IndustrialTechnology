package dev.industrialtechnology.console;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.ForgeHooks;
import dev.industrialtechnology.core.ServerWork;
public final class ConsoleCommand implements IMessage {
    private int window,action,row,value;private long low,high;private String text="";
    public ConsoleCommand(){}
    public ConsoleCommand(int window,int action,int row,int value,long low,long high,String text){this.window=window;this.action=action;this.row=row;this.value=value;this.low=low;this.high=high;this.text=text==null?"":text;}
    @Override public void toBytes(ByteBuf b){b.writeInt(window);b.writeByte(action);b.writeByte(row);b.writeInt(value);b.writeLong(low);b.writeLong(high);ByteBufUtils.writeUTF8String(b,text);}
    @Override public void fromBytes(ByteBuf b){if(b.readableBytes()>512)throw new IllegalArgumentException("Console command too large");window=b.readInt();action=b.readUnsignedByte();row=b.readUnsignedByte();value=b.readInt();low=b.readLong();high=b.readLong();text=ByteBufUtils.readUTF8String(b);}
    public static final class Handler implements IMessageHandler<ConsoleCommand,IMessage>{public IMessage onMessage(ConsoleCommand m,MessageContext c){EntityPlayerMP p=c.getServerHandler().playerEntity;ServerWork.enqueue(()->apply(p,m));return null;}}
    public static void apply(EntityPlayerMP p,ConsoleCommand m){if(!(p.openContainer instanceof ConsoleContainer)||m.text.length()>128)return;ConsoleContainer c=(ConsoleContainer)p.openContainer;if(c.windowId!=m.window||!c.canInteractWith(p)||!ForgeHooks.canInteractWith(p,c)||!c.allow())return;c.action(m.action,m.row,m.value,m.low,m.high,m.text);c.send(p);}
}
