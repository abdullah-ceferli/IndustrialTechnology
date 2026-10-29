package dev.industrialtechnology.console;
import org.junit.Test;
import static org.junit.Assert.*;
public class ConsoleMathTest {
    @Test public void releaseHasStableMiddleBand(){assertTrue(TankBand.next(false,false,2000000,1800000,2000000));assertTrue(TankBand.next(false,true,1900000,1800000,2000000));assertFalse(TankBand.next(false,true,1800000,1800000,2000000));assertFalse(TankBand.next(false,false,1900000,1800000,2000000));}
    @Test public void refillReversesLimits(){assertTrue(TankBand.next(true,false,100,100,200));assertTrue(TankBand.next(true,true,150,100,200));assertFalse(TankBand.next(true,true,200,100,200));assertFalse(TankBand.next(true,false,150,100,200));}
    @Test public void invalidDataFailsClosed(){assertFalse(TankBand.next(false,true,-1,1,2));assertFalse(TankBand.next(true,true,1,10,2));assertFalse(TankBand.valid(10,10));assertFalse(TankBand.valid(-1,5));assertFalse(TankBand.valid(0,Long.MAX_VALUE));}
    @Test public void largeAmountsRemainLong(){assertTrue(TankBand.next(false,false,5000000000L,3000000000L,4000000000L));}
    @Test public void ringKeepsNewest720(){TrendSeries s=new TrendSeries();for(int i=0;i<800;i++)s.add(i*100L,i);assertEquals(720,s.size());assertEquals(8000,s.time(0));assertEquals(799,s.value(719));}
    @Test public void rateUsesActualSampleTicks(){TrendSeries s=new TrendSeries();s.add(100,1000);s.add(200,2000);assertEquals(200,s.ratePerSecond(),.0001);s.add(312,760);assertEquals((760-1000)*20.0/212,s.ratePerSecond(),.0001);}
    @Test public void offlineGapNeverBecomesAnEmptyTank(){TrendSeries s=new TrendSeries();s.add(100,1000);s.add(200,-1);assertTrue(Double.isNaN(s.ratePerSecond()));s.add(300,2000);assertTrue(Double.isNaN(s.ratePerSecond()));s.add(400,2100);assertEquals(20,s.ratePerSecond(),.0001);}
    @Test public void unloadGapDoesNotProduceFalseRate(){TrendSeries s=new TrendSeries();s.add(100,1000);s.add(10000,5000);assertTrue(Double.isNaN(s.ratePerSecond()));}
    @Test public void duplicateOrBackwardsTicksAreRejected(){TrendSeries s=new TrendSeries();s.add(100,5);s.add(100,99);s.add(90,99);assertEquals(1,s.size());assertEquals(5,s.value(0));}
    @Test public void restorePreservesGapsAndLongValues(){TrendSeries s=new TrendSeries();s.restore(new long[]{10,110,210},new long[]{5000000000L,-1,4900000000L});assertEquals(3,s.size());assertArrayEquals(new long[]{5000000000L,-1,4900000000L},s.values());assertTrue(Double.isNaN(s.ratePerSecond()));}
    @Test public void flatStorageIsZeroNet(){TrendSeries s=new TrendSeries();s.add(0,100);s.add(100,100);assertEquals(0,s.ratePerSecond(),0);}
    @Test public void snapshotsAreCopies(){TrendSeries s=new TrendSeries();s.add(0,100);s.values()[0]=7;assertEquals(100,s.value(0));}
}
