package dev.industrialtechnology.wireless;
import net.minecraftforge.common.config.Configuration;
public final class WirelessConfig {
    public static int endpoints=128, terminals=8, globalEndpoints=8192, pollTicks=10, pollsPerTick=64, scriptLength=4096;
    public static void load(Configuration c){
        scriptLength=c.getInt("maximumScriptCharacters","wireless",4096,256,8192,"Maximum saved script characters; eight rules maximum.");
        endpoints=c.getInt("endpointsPerTerminal","wireless",128,1,128,"Maximum linked devices per terminal.");
        terminals=c.getInt("terminalsPerOwner","wireless",8,1,32,"Maximum independent terminal networks per owner.");
        globalEndpoints=c.getInt("globalEndpoints","wireless",8192,128,32768,"World-wide device registry bound.");
        pollTicks=c.getInt("automationPollTicks","wireless",10,5,200,"Minimum ticks between inventory scans per automation endpoint.");
        pollsPerTick=c.getInt("automationVisitsPerTick","wireless",64,1,256,"Round-robin endpoint visit budget; large registries may poll more slowly.");
    }
    private WirelessConfig(){}
}
