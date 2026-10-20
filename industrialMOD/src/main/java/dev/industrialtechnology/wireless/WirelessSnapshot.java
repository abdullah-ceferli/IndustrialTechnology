package dev.industrialtechnology.wireless;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.nbt.*;
import java.io.IOException;
import dev.industrialtechnology.core.IndustrialTechnology;
public final class WirelessSnapshot implements IMessage {
    private int window;private NBTTagCompound state;
    public WirelessSnapshot(){}
    public WirelessSnapshot(int window,NBTTagCompound state){this.window=window;this.state=state;}
    @Override public void toBytes(ByteBuf out){out.writeInt(window);try{if(CompressedStreamTools.compress(state).length>30000){state=new NBTTagCompound();state.setString("Error","Display exceeds packet limit. Reduce device names or inventory page size.");}}catch(IOException e){throw new IllegalStateException(e);}ByteBufUtils.writeTag(out,state);}
    @Override public void fromBytes(ByteBuf in){if(in.readableBytes()>30006)throw new IllegalArgumentException("Snapshot too large");window=in.readInt();state=ByteBufUtils.readTag(in);}
    public static final class Handler implements IMessageHandler<WirelessSnapshot,IMessage>{
        @Override public IMessage onMessage(WirelessSnapshot packet,MessageContext ctx){if(packet.state!=null)IndustrialTechnology.proxy.acceptWireless(packet.window,packet.state);return null;}
    }
}
