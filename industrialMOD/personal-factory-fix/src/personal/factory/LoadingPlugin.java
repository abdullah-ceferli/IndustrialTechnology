package personal.factory;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1100)
@IFMLLoadingPlugin.TransformerExclusions({"personal.factory."})
public final class LoadingPlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass() { return new String[]{"personal.factory.ProbeTransformer"}; }
    public String getModContainerClass() { return null; }
    public String getSetupClass() { return null; }
    public void injectData(Map<String,Object> data) {}
    public String getAccessTransformerClass() { return null; }
}
