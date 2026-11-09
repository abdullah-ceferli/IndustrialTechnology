package dev.industrialtechnology.energy.core;
import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
/** One JAR has one core-plugin entry; preserve both upstream OC and our HE observer. */
@IFMLLoadingPlugin.Name("IndustrialTechnologyOpenComputers")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"dev.industrialtechnology.energy.core","li.cil.oc.common.asm"})
public final class CombinedCorePlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass(){return new String[]{"li.cil.oc.common.asm.ClassTransformer","dev.industrialtechnology.energy.core.BatteryMeterTransformer"};}
    public String getModContainerClass(){return "li.cil.oc.common.launch.CoreModContainer";}
    public String getSetupClass(){return null;}
    public String getAccessTransformerClass(){return null;}
    public void injectData(Map<String,Object> data){}
}
