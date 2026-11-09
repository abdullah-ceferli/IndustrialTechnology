package personal.offlineskins;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public final class Packets {
    private Packets() { }

    public static byte[] readPng(ByteBuf buf) {
        if (buf.readableBytes() < 4) throw new IllegalArgumentException("Missing skin size");
        int size = buf.readInt();
        if (size < 0 || size > SkinImage.MAX_BYTES || size != buf.readableBytes()) {
            throw new IllegalArgumentException("Invalid skin packet size");
        }
        byte[] png = new byte[size];
        buf.readBytes(png);
        return png;
    }

    private static void writePng(ByteBuf buf, byte[] png) {
        if (png == null || png.length > SkinImage.MAX_BYTES) throw new IllegalArgumentException("Invalid skin size");
        buf.writeInt(png.length);
        buf.writeBytes(png);
    }

    public static final class Hello implements IMessage {
        // Keep the original four-byte greeting. Older clients safely ignore a
        // LAN greeting instead of failing to decode a differently sized packet.
        public static final int LAN_FLAG = 0x10000;
        public int protocol = OfflineSkins.PROTOCOL;
        public Hello() { }
        public Hello(boolean lan) { if (lan) protocol |= LAN_FLAG; }
        public static boolean compatible(int value) {
            return (value & ~LAN_FLAG) == OfflineSkins.PROTOCOL;
        }
        public static boolean isLan(int value) {
            return compatible(value) && (value & LAN_FLAG) != 0;
        }
        @Override public void fromBytes(ByteBuf buf) {
            if (buf.readableBytes() != 4) throw new IllegalArgumentException("Invalid hello");
            protocol = buf.readInt();
        }
        @Override public void toBytes(ByteBuf buf) { buf.writeInt(protocol); }
    }

    /** Identity is deliberately absent: the server derives it from the connection. */
    public static final class Upload implements IMessage {
        public byte[] png;
        public Upload() { }
        public Upload(byte[] png) { this.png = png; }
        @Override public void fromBytes(ByteBuf buf) { png = readPng(buf); }
        @Override public void toBytes(ByteBuf buf) { writePng(buf, png); }
    }

    public static final class Skin implements IMessage {
        public UUID player;
        public byte[] png;
        public Skin() { }
        public Skin(UUID player, byte[] png) { this.player = player; this.png = png; }
        @Override public void fromBytes(ByteBuf buf) {
            if (buf.readableBytes() < 20) throw new IllegalArgumentException("Invalid skin identity");
            player = new UUID(buf.readLong(), buf.readLong());
            png = readPng(buf);
        }
        @Override public void toBytes(ByteBuf buf) {
            buf.writeLong(player.getMostSignificantBits());
            buf.writeLong(player.getLeastSignificantBits());
            writePng(buf, png);
        }
    }

    public static final class HelloHandler implements IMessageHandler<Hello, IMessage> {
        @Override public IMessage onMessage(Hello message, MessageContext context) {
            OfflineSkins.proxy.hello(context.netHandler, message.protocol);
            return null;
        }
    }
    public static final class UploadHandler implements IMessageHandler<Upload, IMessage> {
        @Override public IMessage onMessage(Upload message, MessageContext context) {
            OfflineSkins.server.enqueue(context.getServerHandler(), message.png);
            return null;
        }
    }
    public static final class SkinHandler implements IMessageHandler<Skin, IMessage> {
        @Override public IMessage onMessage(Skin message, MessageContext context) {
            OfflineSkins.proxy.receive(context.netHandler, message.player, message.png);
            return null;
        }
    }
}
