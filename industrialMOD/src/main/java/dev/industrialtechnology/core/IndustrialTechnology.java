package dev.industrialtechnology.core;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import dev.industrialtechnology.chunkloading.ChunkTickets;
import dev.industrialtechnology.network.ControlPacket;
import dev.industrialtechnology.network.SnapshotPacket;
import dev.industrialtechnology.quarry.MiningScheduler;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = IndustrialTechnology.MODID, name = "Industrial Technology", version = "1.3.0",
     acceptedMinecraftVersions = "[1.7.10]", dependencies = "required-after:Forge@[10.13.4.1614,)")
public final class IndustrialTechnology {
    public static final String MODID = "industrialtech";
    @Mod.Instance(MODID) public static IndustrialTechnology instance;
    @SidedProxy(clientSide = "dev.industrialtechnology.client.ClientProxy",
                serverSide = "dev.industrialtechnology.core.CommonProxy")
    public static CommonProxy proxy;
    public static SimpleNetworkWrapper network;
    public static Logger log;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        ModConfig.load(event.getSuggestedConfigurationFile());
        ModRegistry.register();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(ControlPacket.Handler.class, ControlPacket.class, 0, Side.SERVER);
        // The common handler delegates to the client proxy: no client class is linked on a server.
        network.registerMessage(SnapshotPacket.Handler.class, SnapshotPacket.class, 1, Side.CLIENT);
        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkTickets.Callback());
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        ModRegistry.recipes();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        FMLCommonHandler.instance().bus().register(new ServerWork());
        FMLCommonHandler.instance().bus().register(new MiningScheduler());
        MinecraftForge.EVENT_BUS.register(new ChunkTickets.WorldEvents());
        MinecraftForge.EVENT_BUS.register(new dev.industrialtechnology.quarry.OreRecognition());
        proxy.init();
    }
    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent event) {
        ServerWork.clear();
        MiningScheduler.clear();
    }
}
