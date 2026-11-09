package dev.industrialtechnology.energy.core;
import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
@IFMLLoadingPlugin.Name("IndustrialTechnologyBatteryMeter")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"dev.industrialtechnology.energy.core"})
public final class BatteryMeterPlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass(){return new String[]{"dev.industrialtechnology.energy.core.BatteryMeterTransformer"};}
    public String getModContainerClass(){return null;}
    public String getSetupClass(){return null;}
    public void injectData(Map<String,Object> data){}
    public String getAccessTransformerClass(){return null;}
}
