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
    @Override public void init() {
        OverlaySettings.load(); FMLCommonHandler.instance().bus().register(this); MinecraftForge.EVENT_BUS.register(new AreaPreview());
        boundaries = new WorldBoundaries(); FMLCommonHandler.instance().bus().register(boundaries); MinecraftForge.EVENT_BUS.register(boundaries);
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(dev.industrialtechnology.chunkloading.TileChunkLoader.class, new ChunkLoaderRenderer());
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (id != 0 || !(tile instanceof TileMachine)) return null;
        MachineContainer container = new MachineContainer(player.inventory, (TileMachine) tile);
        return ((TileMachine) tile).isQuarry() ? new GuiMachine(container) : new GuiChunkLoader(container);
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
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        for (int i = 0; i < 128; i++) { Runnable task = queue.poll(); if (task == null) break; task.run(); }
    }
}
