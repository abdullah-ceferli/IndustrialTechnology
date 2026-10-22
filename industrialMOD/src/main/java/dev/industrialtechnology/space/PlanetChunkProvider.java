package dev.industrialtechnology.space;
import java.util.*;import net.minecraft.block.Block;import net.minecraft.init.Blocks;import net.minecraft.world.*;import net.minecraft.world.chunk.*;import net.minecraft.entity.EnumCreatureType;import net.minecraft.util.IProgressUpdate;
public final class PlanetChunkProvider implements IChunkProvider {
    private final World world;private final boolean mars;private final PlanetTerrain terrain;
    public PlanetChunkProvider(World w,boolean mars){world=w;this.mars=mars;terrain=new PlanetTerrain(w.getSeed()^(mars?0x4d415253:0x4d4f4f4e),mars);}
    @Override public Chunk provideChunk(int cx,int cz){Block[] blocks=new Block[65536];byte[] meta=new byte[65536];Arrays.fill(blocks,Blocks.air);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){int wx=cx*16+x,wz=cz*16+z,h=terrain.height(wx,wz);boolean mare=terrain.mare(wx,wz),ice=terrain.ice(wx,wz);
            for(int y=0;y<=h;y++){Block b=y==0?Blocks.bedrock:mars?SpaceRegistry.marsRock:SpaceRegistry.moonRock;
                if(y>h-4)b=mars?(y==h?SpaceRegistry.marsDust:SpaceRegistry.marsLayer):(mare?SpaceRegistry.basalt:y==h?SpaceRegistry.lunarDust:SpaceRegistry.regolith);
                if(!mars&&y<h-4&&y>h-12&&terrain.noise(wx,wz,12,y/3)>.72)b=SpaceRegistry.breccia;
                if(ice&&y>h-6&&y<h-2)b=SpaceRegistry.ice;
                // Resource deposits are generated below after the terrain column is complete.
                blocks[(x*16+z)*256+y]=b;}
            if(terrain.boulder(wx,wz)&&h<190)blocks[(x*16+z)*256+h+1]=mars?SpaceRegistry.marsRock:SpaceRegistry.breccia;
        }
        PlanetResources.generate(blocks,cx,cz,world.getSeed(),mars,terrain);Chunk c=new Chunk(world,blocks,meta,cx,cz);Arrays.fill(c.getBiomeArray(),(byte)(mars?SpaceConfig.marsBiome:SpaceConfig.moonBiome));c.generateSkylightMap();return c;
    }
    @Override public Chunk loadChunk(int x,int z){return provideChunk(x,z);}
    @Override public void populate(IChunkProvider p,int x,int z){} // All features are column-local: generation never cascades into neighbours.
    @Override public boolean chunkExists(int x,int z){return true;}
    @Override public boolean saveChunks(boolean all,IProgressUpdate p){return true;}
    @Override public boolean unloadQueuedChunks(){return false;}
    @Override public boolean canSave(){return true;}
    @Override public String makeString(){return "IndustrialTechPlanet";}
    @Override public List getPossibleCreatures(EnumCreatureType t,int x,int y,int z){return Collections.emptyList();}
    @Override public ChunkPosition func_147416_a(World w,String name,int x,int y,int z){return null;}
    @Override public int getLoadedChunkCount(){return 0;}
    @Override public void recreateStructures(int x,int z){}
    @Override public void saveExtraData(){}
}
