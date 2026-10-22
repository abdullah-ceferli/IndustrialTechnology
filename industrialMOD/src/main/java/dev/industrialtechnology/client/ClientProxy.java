package dev.industrialtechnology.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import dev.industrialtechnology.core.CommonProxy;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.machine.MachineContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy {
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
    public static WorldBoundaries boundaries;
    @Override public void openAutomationGuide(){net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new GuiAutomationGuide(null));}
    @Override public void init() {
        dev.industrialtechnology.client.space.SpaceClient.init();
        WirelessInput input=new WirelessInput();MinecraftForge.EVENT_BUS.register(input);FMLCommonHandler.instance().bus().register(input);
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(dev.industrialtechnology.wireless.TileRedstoneRelay.class,new RedstoneRelayRenderer());
        OverlaySettings.load(); FMLCommonHandler.instance().bus().register(this); MinecraftForge.EVENT_BUS.register(new AreaPreview());
        boundaries = new WorldBoundaries(); FMLCommonHandler.instance().bus().register(boundaries); MinecraftForge.EVENT_BUS.register(boundaries);
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(dev.industrialtechnology.chunkloading.TileChunkLoader.class, new ChunkLoaderRenderer());
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==3){net.minecraft.entity.Entity entity=world.getEntityByID(x);return entity instanceof dev.industrialtechnology.space.EntitySpaceRocket?new dev.industrialtechnology.client.space.GuiSpace(new dev.industrialtechnology.space.SpaceContainer(player,(dev.industrialtechnology.space.EntitySpaceRocket)entity)):null;}
        if(id==2){TileEntity t=world.getTileEntity(x,y,z);return t instanceof dev.industrialtechnology.space.TileSpaceMachine?new dev.industrialtechnology.client.space.GuiSpace(new dev.industrialtechnology.space.SpaceContainer(player,(dev.industrialtechnology.space.TileSpaceMachine)t)):null;}
        if(id==1)return new GuiWirelessTerminal(new dev.industrialtechnology.wireless.TerminalContainer(player));
        TileEntity tile = world.getTileEntity(x, y, z);
        if (id != 0 || !(tile instanceof TileMachine)) return null;
        MachineContainer container = new MachineContainer(player.inventory, (TileMachine) tile);
        return ((TileMachine) tile).isQuarry() ? new GuiMachine(container) : new GuiChunkLoader(container);
    }
    @Override public void rocketParticles(dev.industrialtechnology.space.EntitySpaceRocket r){dev.industrialtechnology.client.space.SpaceClient.particles(r);}
    @Override public void acceptSpace(final int window,final NBTTagCompound state){queue.add(()->dev.industrialtechnology.client.space.SpaceClient.receive(window,state));}
    @Override public void acceptSnapshot(final int x, final int y, final int z, final int window, final NBTTagCompound state) {
        queue.add(new Runnable() { @Override public void run() {
            Minecraft mc = Minecraft.getMinecraft();
            if (!(mc.currentScreen instanceof GuiMachine) || mc.thePlayer == null) return;
            GuiMachine gui = (GuiMachine) mc.currentScreen;
            if (gui.container.windowId == window && gui.container.machine.xCoord == x && gui.container.machine.yCoord == y && gui.container.machine.zCoord == z)
                gui.receive(state);
        }});
    }
    @Override public void acceptScript(final int window,final java.util.UUID device,final int revision,final String text){
        queue.add(()->{Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen instanceof GuiWirelessTerminal){GuiWirelessTerminal gui=(GuiWirelessTerminal)mc.currentScreen;if(gui.terminal.windowId==window)gui.receiveScript(device,revision,text);}});
    }
    @Override public void acceptCatalog(final dev.industrialtechnology.wireless.WirelessCatalog packet){
        queue.add(()->{Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen instanceof GuiWirelessTerminal&&mc.thePlayer!=null){GuiWirelessTerminal gui=(GuiWirelessTerminal)mc.currentScreen;if(gui.terminal.windowId==packet.window)gui.receiveCatalog(packet);}});
    }
    @Override public void acceptWireless(final int window,final NBTTagCompound state){
        queue.add(()->{Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen instanceof GuiWirelessTerminal&&mc.thePlayer!=null){GuiWirelessTerminal gui=(GuiWirelessTerminal)mc.currentScreen;if(gui.terminal.windowId==window)gui.receive(state);}});
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        for (int i = 0; i < 128; i++) { Runnable task = queue.poll(); if (task == null) break; task.run(); }
    }
}
