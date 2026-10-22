package dev.industrialtechnology.space;
import net.minecraftforge.common.DimensionManager;import net.minecraft.world.biome.BiomeGenBase;import dev.industrialtechnology.core.IndustrialTechnology;
public final class SpaceDimensions {
    public static BiomeGenBase lunar,martian;public static boolean ready;
    private static final class Barren extends BiomeGenBase{Barren(int id,String name){super(id);setBiomeName(name);setDisableRain();setTemperatureRainfall(.1f,0);spawnableCreatureList.clear();spawnableMonsterList.clear();spawnableWaterCreatureList.clear();spawnableCaveCreatureList.clear();}}
    public static void init(){
        if(SpaceConfig.moon==SpaceConfig.mars||SpaceConfig.moonBiome==SpaceConfig.marsBiome||DimensionManager.isDimensionRegistered(SpaceConfig.moon)||DimensionManager.isDimensionRegistered(SpaceConfig.mars)||BiomeGenBase.getBiomeGenArray()[SpaceConfig.moonBiome]!=null||BiomeGenBase.getBiomeGenArray()[SpaceConfig.marsBiome]!=null){IndustrialTechnology.log.error("Space travel disabled: configured dimension/biome IDs conflict. Resolve industrialtech-space.cfg; no existing registration was overwritten.");return;}
        lunar=new Barren(SpaceConfig.moonBiome,"Lunar Highlands and Maria");martian=new Barren(SpaceConfig.marsBiome,"Martian Desert");
        boolean moonType=DimensionManager.registerProviderType(SpaceConfig.moon,WorldProviderMoon.class,false);boolean marsType=DimensionManager.registerProviderType(SpaceConfig.mars,WorldProviderMars.class,false);
        if(!moonType||!marsType){if(moonType)DimensionManager.unregisterProviderType(SpaceConfig.moon);if(marsType)DimensionManager.unregisterProviderType(SpaceConfig.mars);IndustrialTechnology.log.error("Space provider IDs conflict. Travel disabled without replacing another mod's provider.");return;}
        DimensionManager.registerDimension(SpaceConfig.moon,SpaceConfig.moon);DimensionManager.registerDimension(SpaceConfig.mars,SpaceConfig.mars);ready=true;
    }
    public static int planet(int dim){return !ready?-1:dim==SpaceConfig.moon?0:dim==SpaceConfig.mars?1:-1;}
    public static int dimension(int planet){return planet==1?SpaceConfig.mars:SpaceConfig.moon;}
    public static double gravity(int dim){return dim==SpaceConfig.moon?.165:dim==SpaceConfig.mars?.38:1;}
}
