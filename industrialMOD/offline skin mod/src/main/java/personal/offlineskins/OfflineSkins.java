package personal.offlineskins;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import org.apache.logging.log4j.Logger;

@Mod(modid = OfflineSkins.ID, name = "Offline Skins", version = "1.2.0",
        acceptableRemoteVersions = "*", guiFactory = "personal.offlineskins.client.SkinGuiFactory")
public final class OfflineSkins {
    public static final String ID = "offlineskins";
    public static final int PROTOCOL = 1;
    public static SimpleNetworkWrapper network;
    public static Logger log;
    public static ServerSkins server;

    @SidedProxy(clientSide = "personal.offlineskins.client.ClientSkins",
            serverSide = "personal.offlineskins.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(ID);
        network.registerMessage(Packets.HelloHandler.class, Packets.Hello.class, 0, Side.CLIENT);
        network.registerMessage(Packets.UploadHandler.class, Packets.Upload.class, 1, Side.SERVER);
        network.registerMessage(Packets.SkinHandler.class, Packets.Skin.class, 2, Side.CLIENT);
        File directory = new File(event.getModConfigurationDirectory(), ID);
        server = new ServerSkins(new File(directory, "server-skins"));
        FMLCommonHandler.instance().bus().register(server);
        proxy.initialize(directory);
    }

    @Mod.EventHandler public void serverStopped(FMLServerStoppedEvent event) {
        server.reset();
    }
}
