package dev.industrialtechnology.space;
import io.netty.buffer.ByteBuf;import cpw.mods.fml.common.network.simpleimpl.*;import net.minecraft.entity.player.EntityPlayerMP;import net.minecraftforge.common.ForgeHooks;import dev.industrialtechnology.core.ServerWork;
public final class SpaceCommand implements IMessage {
    private int window,action,value;public SpaceCommand(){}public SpaceCommand(int w,int a,int v){window=w;action=a;value=v;}
    @Override public void toBytes(ByteBuf b){b.writeInt(window);b.writeByte(action);b.writeInt(value);}@Override public void fromBytes(ByteBuf b){window=b.readInt();action=b.readUnsignedByte();value=b.readInt();}
    public static final class Handler implements IMessageHandler<SpaceCommand,IMessage>{@Override public IMessage onMessage(final SpaceCommand m,MessageContext ctx){final EntityPlayerMP p=ctx.getServerHandler().playerEntity;ServerWork.enqueue(()->{if(!(p.openContainer instanceof SpaceContainer)||m.action>7)return;SpaceContainer c=(SpaceContainer)p.openContainer;if(c.windowId==m.window&&c.canInteractWith(p)&&ForgeHooks.canInteractWith(p,c))c.action(p,m.action,m.value);});return null;}}
}
