package dev.industrialtechnology.console;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.core.IndustrialTechnology;
public final class ConsoleSnapshot implements IMessage {
    private int x,y,z,window;private NBTTagCompound state;
    public ConsoleSnapshot(){}
    public ConsoleSnapshot(TileConsole t,int w,NBTTagCompound n){x=t.xCoord;y=t.yCoord;z=t.zCoord;window=w;state=n;}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeInt(window);ByteBufUtils.writeTag(b,state);}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();window=b.readInt();state=ByteBufUtils.readTag(b);}
    public static final class Handler implements IMessageHandler<ConsoleSnapshot,IMessage>{public IMessage onMessage(ConsoleSnapshot m,MessageContext c){if(m.state!=null)IndustrialTechnology.proxy.acceptConsole(m.x,m.y,m.z,m.window,m.state);return null;}}
}
