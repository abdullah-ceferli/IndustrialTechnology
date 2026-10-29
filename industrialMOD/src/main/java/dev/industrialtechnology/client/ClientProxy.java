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
    @Override public void acceptExpansion(final int window,final NBTTagCompound state){queue.add(()->{Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen instanceof GuiExpansion){GuiExpansion g=(GuiExpansion)mc.currentScreen;if(g.expansion.windowId==window)g.receive(state);}});}
    @Override public void init() {
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new RailResources());
        dev.industrialtechnology.solar.BlockSolar.renderId=cpw.mods.fml.client.registry.RenderingRegistry.getNextAvailableRenderId();
        cpw.mods.fml.client.registry.RenderingRegistry.registerBlockHandler(new SolarRenderer());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(dev.industrialtechnology.railway.EntityRollingStock.class,new RollingStockRenderer());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(dev.industrialtechnology.railway.TileTrack.class,new TrackRenderer());
        FMLCommonHandler.instance().bus().register(new TrainInput());
        FMLCommonHandler.instance().bus().register(new RailCamera());
        dev.industrialtechnology.capacitor.BlockCapacitorBank.renderId=cpw.mods.fml.client.registry.RenderingRegistry.getNextAvailableRenderId();
        cpw.mods.fml.client.registry.RenderingRegistry.registerBlockHandler(new CapacitorBankRenderer());
        dev.industrialtechnology.console.BlockConsole.renderId=cpw.mods.fml.client.registry.RenderingRegistry.getNextAvailableRenderId();
        cpw.mods.fml.client.registry.RenderingRegistry.registerBlockHandler(new ConsoleBlockRenderer());
        WirelessInput input=new WirelessInput();MinecraftForge.EVENT_BUS.register(input);FMLCommonHandler.instance().bus().register(input);
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(dev.industrialtechnology.wireless.TileRedstoneRelay.class,new RedstoneRelayRenderer());
        OverlaySettings.load(); FMLCommonHandler.instance().bus().register(this); MinecraftForge.EVENT_BUS.register(new AreaPreview());
        boundaries = new WorldBoundaries(); FMLCommonHandler.instance().bus().register(boundaries); MinecraftForge.EVENT_BUS.register(boundaries);
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(dev.industrialtechnology.chunkloading.TileChunkLoader.class, new ChunkLoaderRenderer());
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==8){dev.industrialtechnology.bag.BagContainer c=dev.industrialtechnology.bag.BagContainer.open(player,true);return c==null?null:new GuiStorageBag(c);}
        if(id==3)return null; // Retired solar GUI ID stays reserved.
        if(id==1)return new GuiWirelessTerminal(new dev.industrialtechnology.wireless.TerminalContainer(player));
        if(id==4){net.minecraft.entity.Entity e=world.getEntityByID(x);if(!(e instanceof dev.industrialtechnology.railway.EntityRollingStock))return null;dev.industrialtechnology.railway.EntityRollingStock train=(dev.industrialtechnology.railway.EntityRollingStock)e;return new GuiExpansion(new dev.industrialtechnology.expansion.ExpansionContainer(player,train,null));}
        TileEntity tile = world.getTileEntity(x, y, z);
        if(id==7&&tile instanceof dev.industrialtechnology.console.TileConsole)return new GuiConsole(new dev.industrialtechnology.console.ConsoleContainer((dev.industrialtechnology.console.TileConsole)tile));
        if(id==6&&tile instanceof dev.industrialtechnology.steel.TileSteelFactory)return new GuiSteelFactory(new dev.industrialtechnology.steel.SteelContainer(player.inventory,(dev.industrialtechnology.steel.TileSteelFactory)tile));
        if(id==5&&tile instanceof dev.industrialtechnology.railway.TileRailStation)return new GuiExpansion(new dev.industrialtechnology.expansion.ExpansionContainer(player,null,(dev.industrialtechnology.railway.TileRailStation)tile));
        if(id==2&&tile instanceof dev.industrialtechnology.capacitor.TileCapacitorBank)return new GuiCapacitorBank(new dev.industrialtechnology.capacitor.BankContainer((dev.industrialtechnology.capacitor.TileCapacitorBank)tile));
        if (id != 0 || !(tile instanceof TileMachine)) return null;
        MachineContainer container = new MachineContainer(player.inventory, (TileMachine) tile);
        return ((TileMachine) tile).isQuarry() ? new GuiMachine(container) : new GuiChunkLoader(container);
    }
    @Override public void acceptConsole(final int x,final int y,final int z,final int window,final NBTTagCompound state){queue.add(()->{Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen instanceof GuiConsole){GuiConsole g=(GuiConsole)mc.currentScreen;dev.industrialtechnology.console.TileConsole t=g.console.tile;if(g.console.windowId==window&&t.xCoord==x&&t.yCoord==y&&t.zCoord==z)g.receive(state);}});}
    @Override public void acceptBankSnapshot(final int x,final int y,final int z,final int window,final NBTTagCompound state){
        queue.add(()->{Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen instanceof GuiCapacitorBank&&mc.thePlayer!=null){GuiCapacitorBank gui=(GuiCapacitorBank)mc.currentScreen;dev.industrialtechnology.capacitor.TileCapacitorBank b=gui.bankContainer.bank;if(gui.bankContainer.windowId==window&&b.xCoord==x&&b.yCoord==y&&b.zCoord==z)gui.receive(state);}});
    }
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
