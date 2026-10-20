package dev.industrialtechnology.automation;
import io.netty.buffer.ByteBuf;
import java.nio.*;
import java.nio.charset.*;
import java.util.UUID;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.ForgeHooks;
import dev.industrialtechnology.wireless.TerminalContainer;
import dev.industrialtechnology.core.ServerWork;
public final class ScriptUpload implements IMessage {
    public int window;public UUID device;public String text="";
    public ScriptUpload(){}
    public ScriptUpload(int window,UUID device,String text){this.window=window;this.device=device;this.text=text;}
    public static void writeText(ByteBuf out,String text){byte[] bytes=text.getBytes(StandardCharsets.UTF_8);if(text.length()>ScriptCompiler.HARD_LENGTH||bytes.length>ScriptCompiler.MAX_BYTES)throw new IllegalArgumentException("Script too large");out.writeShort(bytes.length);out.writeBytes(bytes);}
    public static String readText(ByteBuf in){if(in.readableBytes()<2)throw new IllegalArgumentException("Truncated script");int count=in.readUnsignedShort();if(count>ScriptCompiler.MAX_BYTES||count!=in.readableBytes())throw new IllegalArgumentException("Invalid script size");byte[] data=new byte[count];in.readBytes(data);
        try{String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data)).toString();if(text.length()>ScriptCompiler.HARD_LENGTH)throw new IllegalArgumentException("Script too long");return text;}catch(CharacterCodingException bad){throw new IllegalArgumentException("Invalid UTF-8",bad);}
    }
    public void toBytes(ByteBuf out){out.writeInt(window);out.writeLong(device.getMostSignificantBits());out.writeLong(device.getLeastSignificantBits());writeText(out,text);}
    public void fromBytes(ByteBuf in){if(in.readableBytes()<22||in.readableBytes()>22+ScriptCompiler.MAX_BYTES)throw new IllegalArgumentException("Invalid upload size");window=in.readInt();device=new UUID(in.readLong(),in.readLong());text=readText(in);}
    public static final class Handler implements IMessageHandler<ScriptUpload,IMessage>{
        public IMessage onMessage(final ScriptUpload m,MessageContext ctx){final EntityPlayerMP p=ctx.getServerHandler().playerEntity;ServerWork.enqueue(()->{
            if(!p.isEntityAlive()||!(p.openContainer instanceof TerminalContainer))return;TerminalContainer c=(TerminalContainer)p.openContainer;
            if(c.windowId!=m.window||!c.canInteractWith(p)||!c.allowAction()||!ForgeHooks.canInteractWith(p,c))return;
            c.saveScript(m.device,m.text);c.send(p);
        });return null;}
    }
}
