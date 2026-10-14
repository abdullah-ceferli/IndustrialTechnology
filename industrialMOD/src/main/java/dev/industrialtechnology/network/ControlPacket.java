package dev.industrialtechnology.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import dev.industrialtechnology.core.ServerWork;
import dev.industrialtechnology.core.ModConfig;
import dev.industrialtechnology.machine.MachineContainer;
import dev.industrialtechnology.quarry.TileQuarry;
import dev.industrialtechnology.quarry.ChunkArea;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.ForgeHooks;
import io.netty.buffer.ByteBuf;

public final class ControlPacket implements IMessage {
    public static final int TOGGLE = 0, APPLY = 1, FORTUNE = 2, SILK = 3, TAB = 4, VOID_TOGGLE = 5, VOID_MODE = 6, VOID_CLEAR = 7;
    private int window, action, value;
    public ControlPacket() {}
    public ControlPacket(int window, int action, int value) { this.window = window; this.action = action; this.value = value; }
    @Override public void toBytes(ByteBuf b) { b.writeInt(window); b.writeByte(action); b.writeInt(value); }
    @Override public void fromBytes(ByteBuf b) { window = b.readInt(); action = b.readUnsignedByte(); value = b.readInt(); }
    public static final class Handler implements IMessageHandler<ControlPacket, IMessage> {
        @Override public IMessage onMessage(final ControlPacket message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerWork.enqueue(new Runnable() { @Override public void run() {
                if (!(player.openContainer instanceof MachineContainer)) return;
                MachineContainer container = (MachineContainer) player.openContainer;
                if (container.windowId != message.window || !container.canInteractWith(player) || !container.allowAction()
                    || !ForgeHooks.canInteractWith(player, container)) return;
                switch (message.action) {
                    case TOGGLE: container.machine.toggleEnabled(); break;
                    case APPLY:
                        if (!ChunkArea.valid(message.value, ModConfig.maxChunks)) return;
                        container.machine.applySelection(message.value); break;
                    case FORTUNE:
                        if (container.machine instanceof TileQuarry) ((TileQuarry) container.machine).setMode(false); break;
                    case SILK:
                        if (container.machine instanceof TileQuarry) ((TileQuarry) container.machine).setMode(true); break;
                    case TAB: if (message.value < 0 || message.value > 2) return; container.setTab(message.value); break;
                    case VOID_TOGGLE: case VOID_MODE: case VOID_CLEAR:
                        if (!(container.machine instanceof TileQuarry) || container.tab != 2) return;
                        TileQuarry quarry = (TileQuarry) container.machine;
                        if (message.action == VOID_TOGGLE) quarry.filter.enabled = !quarry.filter.enabled;
                        else if (message.action == VOID_MODE) quarry.filter.ignoreMetadata = !quarry.filter.ignoreMetadata;
                        else quarry.filter.clear();
                        quarry.markDirty(); break;
                    default: return;
                }
                container.send(player);
            }});
            return null;
        }
    }
}
