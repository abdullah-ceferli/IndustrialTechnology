package dev.industrialtechnology.space;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
public final class SpaceConfig {
    public static int moon=180,mars=181,moonBiome=180,marsBiome=181;
    public static int moonResearch=200,marsResearch=6000,machineRF=200,roomBudget=256,globalRoomBudget=1024,maxRoom=4096,roomRadius=32;
    public static int moonFuel=12000,marsFuel=24000,oxygenRatio=2,tankCapacity=100000,airPerBlock=50,roomRescan=100,landingChecks=49;
    public static int countdown=100,ascent=240,descent=200,particleBudget=12,idleGrace=100;
    public static boolean planetResources=true;public static int resourceDensity=1;
    public static int[] suitMinutes={10,30,90};
    public static long moonDay=480000,marsDay=24600;
    public static boolean pauseOwnedChunkLoadingInUnoccupiedDimensions=true,pausePlanetMachines=true,dustStorms=true;
    private SpaceConfig(){}
    public static void load(File main){
        Configuration c=new Configuration(new File(main.getParentFile(),"industrialtech-space.cfg"));c.load();
        planetResources=c.getBoolean("mineralDeposits","resources",true,"Generate scientifically inspired mineral-bearing rocks in new owned planet chunks only. Existing chunks are never changed.");resourceDensity=c.getInt("depositDensity","resources",1,1,4,"Bounded gameplay vein density, not a measured real-world ore grade.");
        moon=c.getInt("moonDimension","dimensions",180,2,65535,"Reserved ID. Resolve conflicts before enabling travel; existing saves require stable IDs.");
        mars=c.getInt("marsDimension","dimensions",181,2,65535,"Reserved Mars dimension ID.");
        moonBiome=c.getInt("moonBiome","dimensions",180,0,255,"Must be unused by other mods.");marsBiome=c.getInt("marsBiome","dimensions",181,0,255,"Must be unused by other mods.");
        moonResearch=c.getInt("moonTicks","research",200,20,720000,"Powered server ticks stored on the HDD.");marsResearch=c.getInt("marsTicks","research",6000,20,720000,"Powered server ticks stored on the HDD.");
        machineRF=c.getInt("processRFPerTick","machines",200,1,100000,"Manufacturing and research RF/t.");tankCapacity=c.getInt("tankCapacity","machines",100000,10000,1000000,"mB per tank.");
        moonFuel=c.getInt("moonKerosene","missions",12000,1000,100000,"One leg; reserve an additional leg for return.");marsFuel=c.getInt("marsKerosene","missions",24000,1000,100000,"One Mars leg; Moon research required first.");
        oxygenRatio=c.getInt("loxRatio","missions",2,1,4,"LOX mB per kerosene mB.");
        countdown=c.getInt("countdownTicks","missions",100,60,1200,"Countdown before ignition.");ascent=c.getInt("ascentTicks","missions",240,100,2400,"Animated ascent.");descent=c.getInt("descentTicks","missions",200,100,2400,"Animated descent.");
        landingChecks=c.getInt("landingCandidates","performance",49,9,81,"Bounded candidates in a 32 block region.");
        roomBudget=c.getInt("roomNodesPerTick","performance",256,16,2048,"Per room flood fill budget.");globalRoomBudget=c.getInt("globalRoomNodesPerTick","performance",1024,64,16384,"Shared budget across all loaded rooms.");
        maxRoom=c.getInt("maxRoomVolume","atmosphere",4096,16,32768,"Larger spaces report unsealed/too large.");roomRadius=c.getInt("maxRoomRadius","atmosphere",32,4,64,"Maximum flood-fill radius.");
        airPerBlock=c.getInt("airPerBlock","atmosphere",50,1,1000,"Breathable air mB required per cubic metre at nominal pressure.");roomRescan=c.getInt("roomRescanTicks","atmosphere",100,20,600,"Periodic validation catches changes from mods that omit block events.");
        for(int i=0;i<3;i++)suitMinutes[i]=c.getInt("tier"+(i+1)+"Minutes","suits",suitMinutes[i],1,1440,"Chest pack capacity; 1 mB supplies 20 ticks.");
        moonDay=c.getInt("moonDayTicks","environment",480000,24000,2400000,"Compressed lunar daylight cycle.");marsDay=c.getInt("marsDayTicks","environment",24600,12000,240000,"Martian daylight cycle.");
        dustStorms=c.getBoolean("dustStorms","environment",true,"Bounded deterministic regional Mars dust haze.");particleBudget=c.getInt("particlesPerTick","performance",12,0,64,"Client effect limit.");
        pauseOwnedChunkLoadingInUnoccupiedDimensions=c.getBoolean("pauseOwnedChunkLoadingInUnoccupiedDimensions","performance",true,"Suspend only Industrial Technology tickets and quarry work in empty dimensions.");
        pausePlanetMachines=c.getBoolean("pausePlanetMachines","performance",true,"Apply idle suspension on owned planets too.");idleGrace=c.getInt("idleGraceTicks","performance",100,0,1200,"Grace after the last player leaves, plus bounded travel exemptions.");
        boolean heightMaps=c.getBoolean("optionalHeightReferences","environment",false,"Read preprocessed <=512px grayscale PNG references from config/industrialtech-space/. New chunks only; maintain identical files for the save.");
        if(heightMaps)HeightReference.load(new File(main.getParentFile(),"industrialtech-space"));
        if(c.hasChanged())c.save();
    }
}
