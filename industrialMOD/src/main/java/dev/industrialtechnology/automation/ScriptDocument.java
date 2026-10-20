package dev.industrialtechnology.automation;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import cpw.mods.fml.common.network.simpleimpl.*;
import dev.industrialtechnology.core.IndustrialTechnology;
public final class ScriptDocument implements IMessage {
    public int window,revision;public UUID device;public String text="";
    public ScriptDocument(){}
    public ScriptDocument(int window,UUID device,int revision,String text){this.window=window;this.device=device;this.revision=revision;this.text=text;}
    public void toBytes(ByteBuf out){out.writeInt(window);out.writeLong(device.getMostSignificantBits());out.writeLong(device.getLeastSignificantBits());out.writeInt(revision);ScriptUpload.writeText(out,text);}
    public void fromBytes(ByteBuf in){if(in.readableBytes()<26||in.readableBytes()>26+ScriptCompiler.MAX_BYTES)throw new IllegalArgumentException("Invalid document size");window=in.readInt();device=new UUID(in.readLong(),in.readLong());revision=in.readInt();text=ScriptUpload.readText(in);}
    public static final class Handler implements IMessageHandler<ScriptDocument,IMessage>{public IMessage onMessage(ScriptDocument m,MessageContext ctx){IndustrialTechnology.proxy.acceptScript(m.window,m.device,m.revision,m.text);return null;}}
}
