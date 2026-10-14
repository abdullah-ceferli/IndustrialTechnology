package dev.industrialtechnology.core;

import cpw.mods.fml.common.network.IGuiHandler;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.machine.MachineContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class CommonProxy implements IGuiHandler {
    public void init() {}
    public void acceptSnapshot(int x, int y, int z, int window, NBTTagCompound state) {}
    @Override public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return id == 0 && tile instanceof TileMachine ? new MachineContainer(player.inventory, (TileMachine) tile) : null;
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) { return null; }
}
