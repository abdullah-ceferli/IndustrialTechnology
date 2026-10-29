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
    public void acceptConsole(int x,int y,int z,int window,NBTTagCompound state){}
    public void acceptExpansion(int window,NBTTagCompound state){}
    public void acceptBankSnapshot(int x,int y,int z,int window,NBTTagCompound state) {}
    public void acceptCatalog(dev.industrialtechnology.wireless.WirelessCatalog packet) {}
    public void acceptWireless(int window,NBTTagCompound state) {}
    public void acceptScript(int window,java.util.UUID device,int revision,String text) {}
    public void openAutomationGuide() {}
    public void acceptSnapshot(int x, int y, int z, int window, NBTTagCompound state) {}
    @Override public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==8)return dev.industrialtechnology.bag.BagContainer.open(player,false);
        if(id==3)return null; // Retired solar GUI ID stays reserved.
        if(id==1)return dev.industrialtechnology.wireless.ItemWirelessTerminal.held(player)&&dev.industrialtechnology.wireless.WirelessAccess.terminal(player,player.getHeldItem(),false)!=null?new dev.industrialtechnology.wireless.TerminalContainer(player):null;
        if(id==4){net.minecraft.entity.Entity e=world.getEntityByID(x);if(!(e instanceof dev.industrialtechnology.railway.EntityRollingStock))return null;dev.industrialtechnology.railway.EntityRollingStock train=(dev.industrialtechnology.railway.EntityRollingStock)e;if(!train.permitted(player))return null;return new dev.industrialtechnology.expansion.ExpansionContainer(player,train,null);}
        TileEntity tile = world.getTileEntity(x, y, z);
        if(id==7&&tile instanceof dev.industrialtechnology.console.TileConsole&&((dev.industrialtechnology.console.TileConsole)tile).usable(player))return new dev.industrialtechnology.console.ConsoleContainer((dev.industrialtechnology.console.TileConsole)tile);
        if(id==6&&tile instanceof dev.industrialtechnology.steel.TileSteelFactory)return new dev.industrialtechnology.steel.SteelContainer(player.inventory,(dev.industrialtechnology.steel.TileSteelFactory)tile);
        if(id==5&&tile instanceof dev.industrialtechnology.railway.TileRailStation)return new dev.industrialtechnology.expansion.ExpansionContainer(player,null,(dev.industrialtechnology.railway.TileRailStation)tile);
        if(id==2&&tile instanceof dev.industrialtechnology.capacitor.TileCapacitorBank)return new dev.industrialtechnology.capacitor.BankContainer((dev.industrialtechnology.capacitor.TileCapacitorBank)tile);
        return id == 0 && tile instanceof TileMachine ? new MachineContainer(player.inventory, (TileMachine) tile) : null;
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) { return null; }
}
