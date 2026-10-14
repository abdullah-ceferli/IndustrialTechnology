package dev.industrialtechnology.chunkloading;

import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.machine.MachineStatus;

public final class TileChunkLoader extends TileMachine {
    @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox() {
        return net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord,xCoord+1,yCoord+1.8,zCoord+1);
    }
    public TileChunkLoader() { status = MachineStatus.DISABLED; }
    @Override public String machineName() { return "Chunk Loader"; }
    @Override public boolean wantsChunks() {
        return enabled;
    }
    @Override public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        if (!enabled) status = MachineStatus.DISABLED;
        else if (!ticketError.isEmpty()) status = MachineStatus.CHUNK_LOADING_ERROR;
        else {
            status = MachineStatus.ACTIVE;
        }
    }
    @Override public void toggleEnabled() {
        super.toggleEnabled();
        if (enabled && ticketError.isEmpty()) status = MachineStatus.ACTIVE;
    }
}
