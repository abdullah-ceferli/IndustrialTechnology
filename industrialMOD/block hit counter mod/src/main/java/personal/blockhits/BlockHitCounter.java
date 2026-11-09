package personal.blockhits;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "blockhitcounter", name = "Block Hit Counter", version = "1.0.0",
        acceptableRemoteVersions = "*")
public final class BlockHitCounter {
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) ClientCounter.initialize();
    }
}
