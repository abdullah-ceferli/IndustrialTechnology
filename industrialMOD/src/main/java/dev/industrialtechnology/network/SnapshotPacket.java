package dev.industrialtechnology.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.machine.TileMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public final class SnapshotPacket implements IMessage {
    private int x, y, z, window;
    private NBTTagCompound state;
    public SnapshotPacket() {}
    public SnapshotPacket(TileMachine tile, int window, int tab) {
        x = tile.xCoord; y = tile.yCoord; z = tile.zCoord; this.window = window;
        state = tile.snapshot(); state.setInteger("Tab", tab);
    }
    @Override public void toBytes(ByteBuf b) { b.writeInt(x); b.writeInt(y); b.writeInt(z); b.writeInt(window); ByteBufUtils.writeTag(b, state); }
    @Override public void fromBytes(ByteBuf b) { x = b.readInt(); y = b.readInt(); z = b.readInt(); window = b.readInt(); state = ByteBufUtils.readTag(b); }
    public static final class Handler implements IMessageHandler<SnapshotPacket, IMessage> {
        @Override public IMessage onMessage(SnapshotPacket message, MessageContext context) {
            if (message.state != null) IndustrialTechnology.proxy.acceptSnapshot(message.x, message.y, message.z, message.window, message.state);
            return null;
        }
    }
}
