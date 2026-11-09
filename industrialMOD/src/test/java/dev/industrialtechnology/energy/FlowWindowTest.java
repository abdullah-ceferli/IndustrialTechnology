package dev.industrialtechnology.energy;
import org.junit.Test;
import static org.junit.Assert.*;
public class FlowWindowTest {
    @Test public void simultaneousTransfersRemainSeparate(){FlowWindow f=new FlowWindow(100);for(long t=100;t<=120;t++){f.add(t,1000,1300);f.add(t,1300,1200);}assertArrayEquals(new long[]{300,100,20},f.read(120));}
    @Test public void idleWindowExpiresToZero(){FlowWindow f=new FlowWindow(100);f.add(100,10,110);assertArrayEquals(new long[]{0,0,20},f.read(121));}
    @Test public void initialWindowDoesNotClaimHistoricalSamples(){FlowWindow f=new FlowWindow(10);assertArrayEquals(new long[]{0,0,0},f.read(10));f.add(10,5,15);assertArrayEquals(new long[]{10,0,1},f.read(11));}
    @Test public void currentTickIsNotMistakenForCompletedTick(){FlowWindow f=new FlowWindow(10);f.add(11,0,500);assertEquals(0,f.read(11)[0]);assertEquals(250,f.read(12)[0]);}
    @Test public void invalidBalancesAreIgnoredAndLongAmountsDoNotWrap(){FlowWindow f=new FlowWindow(0);f.add(0,-1,100);f.add(0,0,Long.MAX_VALUE);f.add(0,0,1);assertEquals(Long.MAX_VALUE,f.read(1)[0]);assertEquals(0,f.read(1)[1]);}
}
