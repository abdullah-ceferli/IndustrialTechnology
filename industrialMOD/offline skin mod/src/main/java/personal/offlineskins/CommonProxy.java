package personal.offlineskins;

import java.io.File;
import java.util.UUID;
import net.minecraft.network.INetHandler;

/** No client classes here: the same JAR loads on a headless dedicated server. */
public class CommonProxy {
    public void initialize(File directory) { }
    public void hello(INetHandler handler, int protocol) { }
    public void receive(INetHandler handler, UUID player, byte[] png) { }
    public boolean isLanOpen() { return false; }
}
