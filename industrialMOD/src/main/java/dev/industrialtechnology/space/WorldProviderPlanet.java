package dev.industrialtechnology.space;
import net.minecraft.world.*;import net.minecraft.world.biome.*;import net.minecraft.world.chunk.IChunkProvider;import net.minecraft.util.Vec3;
public abstract class WorldProviderPlanet extends WorldProvider {
    protected abstract boolean mars();
    @Override protected void registerWorldChunkManager(){worldChunkMgr=new WorldChunkManagerHell(mars()?SpaceDimensions.martian:SpaceDimensions.lunar,0);hasNoSky=false;isHellWorld=false;}
    @Override public IChunkProvider createChunkGenerator(){return new PlanetChunkProvider(worldObj,mars());}
    @Override public String getDimensionName(){return mars()?"Mars":"Moon";}
    @Override public String getSaveFolder(){return mars()?"IndustrialTechMars":"IndustrialTechMoon";}
    @Override public boolean canRespawnHere(){return false;}
    @Override public int getRespawnDimension(net.minecraft.entity.player.EntityPlayerMP p){return 0;}
    @Override public boolean isSurfaceWorld(){return true;}
    @Override public boolean canCoordinateBeSpawn(int x,int z){return false;}
    @Override public double getMovementFactor(){return 1;}
    @Override public float getCloudHeight(){return -1000;}
    @Override public boolean getWorldHasVoidParticles(){return false;}
    @Override public double getHorizon(){return -100;}
    @Override public boolean isSkyColored(){return mars();}
    @Override public Vec3 getFogColor(float angle,float partial){return mars()?Vec3.createVectorHelper(.43,.25,.17):Vec3.createVectorHelper(0,0,0);}
    @Override public Vec3 getSkyColor(net.minecraft.entity.Entity e,float partial){return mars()?Vec3.createVectorHelper(.55,.31,.21):Vec3.createVectorHelper(0,0,0);}
    @Override public float getStarBrightness(float partial){return mars()?super.getStarBrightness(partial)*.7f:.8f;}
    @Override public float[] calcSunriseSunsetColors(float a,float p){return mars()?super.calcSunriseSunsetColors(a,p):null;}
    @Override public float calculateCelestialAngle(long time,float partial){long day=mars()?SpaceConfig.marsDay:SpaceConfig.moonDay;float f=((time%day)+partial)/day-.25f;if(f<0)f++;if(f>1)f--;float old=f;f=1-(float)((Math.cos(f*Math.PI)+1)/2);return old+(f-old)/3;}
    @Override public void updateWeather(){worldObj.getWorldInfo().setRaining(false);worldObj.getWorldInfo().setThundering(false);worldObj.rainingStrength=worldObj.thunderingStrength=0;}
    @Override public boolean canDoLightning(net.minecraft.world.chunk.Chunk c){return false;}
    @Override public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk c){return false;}
}
