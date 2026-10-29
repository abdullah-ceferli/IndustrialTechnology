package dev.industrialtechnology.solar;
import org.junit.Test;import static org.junit.Assert.*;
public class SunlightTest {
 private double f(long t,boolean sky,boolean wet,boolean storm){return Sunlight.factor(t,sky,true,wet,storm,.35,.1,1);}
 @Test public void noonPeaks(){assertEquals(1,f(6000,true,false,false),1e-8);}@Test public void nightIsZero(){assertEquals(0,f(18000,true,false,false),0);}@Test public void roofIsZero(){assertEquals(0,f(6000,false,false,false),0);}@Test public void weatherReducesOutput(){assertEquals(.35,f(6000,true,true,false),1e-8);assertEquals(.1,f(6000,true,true,true),1e-8);}@Test public void morningAndEveningLowerThanNoon(){assertTrue(f(1000,true,false,false)<.5);assertTrue(f(11000,true,false,false)<.5);}@Test public void noSkyDimensionIsZero(){assertEquals(0,Sunlight.factor(6000,true,false,false,false,.35,.1,1),0);}@Test public void dayRepeatsIncludingNegativeTime(){assertEquals(f(6000,true,false,false),f(-18000,true,false,false),0);}
}
