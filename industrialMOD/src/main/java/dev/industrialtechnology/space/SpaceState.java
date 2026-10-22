package dev.industrialtechnology.space;
import io.netty.buffer.ByteBuf;import cpw.mods.fml.common.network.ByteBufUtils;import cpw.mods.fml.common.network.simpleimpl.*;import net.minecraft.nbt.NBTTagCompound;import dev.industrialtechnology.core.IndustrialTechnology;
public final class SpaceState implements IMessage {
    public int window;public NBTTagCompound state;public SpaceState(){}public SpaceState(int w,NBTTagCompound s){window=w;state=s;}
    @Override public void toBytes(ByteBuf b){b.writeInt(window);ByteBufUtils.writeTag(b,state);}@Override public void fromBytes(ByteBuf b){window=b.readInt();if(b.readableBytes()>16384)throw new IllegalArgumentException("Oversized space snapshot");state=ByteBufUtils.readTag(b);}
    public static final class Handler implements IMessageHandler<SpaceState,IMessage>{@Override public IMessage onMessage(SpaceState m,MessageContext c){IndustrialTechnology.proxy.acceptSpace(m.window,m.state);return null;}}
}
