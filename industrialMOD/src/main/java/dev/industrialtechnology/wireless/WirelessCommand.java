package dev.industrialtechnology.wireless;
import java.nio.charset.StandardCharsets;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.EntityPlayerMP;
import dev.industrialtechnology.core.ServerWork;
import net.minecraftforge.common.ForgeHooks;
public final class WirelessCommand implements IMessage {
    public static final int OPEN=0,TAB=1,PAGE=2,SELECT=3,RENAME=4,UNLINK=5,TOGGLE=6,MODE=7,DURATION=8,RULE=9,TARGET=10,SAVE_RULE=11,DETAIL=12,TRIGGER=13,CODE_MODE=14,SCRIPT_ENABLE=15;
    private int window,action,a,b;private String text="";
    public WirelessCommand(){}
    public WirelessCommand(int window,int action,int a,int b,String text){this.window=window;this.action=action;this.a=a;this.b=b;this.text=text==null?"":text;}
    @Override public void toBytes(ByteBuf out){byte[] bytes=text.getBytes(StandardCharsets.UTF_8);if(bytes.length>128)throw new IllegalArgumentException("Command text too long");out.writeInt(window);out.writeByte(action);out.writeInt(a);out.writeInt(b);out.writeShort(bytes.length);out.writeBytes(bytes);}
    @Override public void fromBytes(ByteBuf in){if(in.readableBytes()<15||in.readableBytes()>143)throw new IllegalArgumentException("Invalid command size");window=in.readInt();action=in.readUnsignedByte();a=in.readInt();b=in.readInt();int size=in.readUnsignedShort();if(size>128||size!=in.readableBytes())throw new IllegalArgumentException("Invalid command text");byte[] bytes=new byte[size];in.readBytes(bytes);text=new String(bytes,StandardCharsets.UTF_8);}
    public static final class Handler implements IMessageHandler<WirelessCommand,IMessage>{
        @Override public IMessage onMessage(final WirelessCommand command,MessageContext ctx){final EntityPlayerMP p=ctx.getServerHandler().playerEntity;
            ServerWork.enqueue(()->{if(!p.isEntityAlive())return;if(command.action==OPEN){ItemWirelessTerminal.open(p);return;}
                if(!(p.openContainer instanceof TerminalContainer))return;TerminalContainer c=(TerminalContainer)p.openContainer;
                if(c.windowId!=command.window||!c.canInteractWith(p)||!c.allowAction()||!ForgeHooks.canInteractWith(p,c))return;
                c.action(command.action,command.a,command.b,command.text);c.detectAndSendChanges();c.send(p);
            });return null;
        }
    }
}
