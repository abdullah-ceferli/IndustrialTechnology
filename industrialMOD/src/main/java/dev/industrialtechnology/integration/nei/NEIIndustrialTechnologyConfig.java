package dev.industrialtechnology.integration.nei;
import codechicken.nei.api.*;
import dev.industrialtechnology.steel.SteelRegistry;
/** Discovered by NEI only; never linked on a standalone or dedicated server. */
public final class NEIIndustrialTechnologyConfig implements IConfigureNEI {
    @Override public void loadConfig(){if(SteelRegistry.hasHBM){BulkSteelRecipeHandler h=new BulkSteelRecipeHandler();API.registerRecipeHandler(h);API.registerUsageHandler(h);}}
    @Override public String getName(){return "Industrial Technology";}
    @Override public String getVersion(){return "2.5.1";}
}
