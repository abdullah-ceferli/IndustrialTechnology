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
    public void acceptCatalog(dev.industrialtechnology.wireless.WirelessCatalog packet) {}
    public void acceptWireless(int window,NBTTagCompound state) {}
    public void acceptScript(int window,java.util.UUID device,int revision,String text) {}
    public void openAutomationGuide() {}
    public void acceptSnapshot(int x, int y, int z, int window, NBTTagCompound state) {}
    @Override public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==1)return dev.industrialtechnology.wireless.ItemWirelessTerminal.held(player)&&dev.industrialtechnology.wireless.WirelessAccess.terminal(player,player.getHeldItem(),false)!=null?new dev.industrialtechnology.wireless.TerminalContainer(player):null;
        TileEntity tile = world.getTileEntity(x, y, z);
        return id == 0 && tile instanceof TileMachine ? new MachineContainer(player.inventory, (TileMachine) tile) : null;
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) { return null; }
}
