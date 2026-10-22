package dev.industrialtechnology.space;
import org.junit.Test;import static org.junit.Assert.*;
public class PlanetTerrainTest {
    @Test public void terrainIsStableAcrossChunkBoundariesAndSeeds(){for(boolean mars:new boolean[]{false,true}){PlanetTerrain a=new PlanetTerrain(7710,mars),b=new PlanetTerrain(7710,mars),other=new PlanetTerrain(9913,mars);int changed=0;for(int x=-500;x<500;x+=13)for(int z=-300;z<300;z+=19){int h=a.height(x,z);assertEquals(h,b.height(x,z));assertTrue(h>=20&&h<=190);assertTrue("unbounded cliff/seam",Math.abs(h-a.height(x+1,z))<18);if(h!=other.height(x,z))changed++;}assertTrue(changed>500);}}
    @Test public void scaledMartianLandmarksAndPolarIceAreRegional(){PlanetTerrain m=new PlanetTerrain(7,true);assertTrue(m.height(8192+500,8192)>m.height(8192+3000,8192)+15);int icy=0;for(int i=0;i<100;i++){assertFalse(m.ice(i*20,0));if(m.ice(i*20,22000))icy++;}assertTrue(icy>0);}
    @Test public void lunarMariaAndHighlandsBothExist(){PlanetTerrain t=new PlanetTerrain(71,false);int maria=0;for(int i=-10000;i<10000;i+=100)if(t.mare(i,i/3))maria++;assertTrue(maria>5&&maria<190);}
    @Test public void requestedPhysicalAndTimingDefaultsRemainExact(){assertEquals(.165,SpaceDimensions.gravity(SpaceConfig.moon),1e-9);assertEquals(.38,SpaceDimensions.gravity(SpaceConfig.mars),1e-9);assertEquals(1,SpaceDimensions.gravity(0),0);assertEquals(200,SpaceConfig.moonResearch);assertEquals(6000,SpaceConfig.marsResearch);assertTrue(SpaceConfig.pauseOwnedChunkLoadingInUnoccupiedDimensions);}
}
