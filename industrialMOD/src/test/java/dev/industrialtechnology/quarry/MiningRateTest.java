package dev.industrialtechnology.quarry;

import org.junit.Test;
import static org.junit.Assert.*;

public class MiningRateTest {
    @Test public void sustainableRatesMatchTheRequestedTable() {
        int[] power = {0,200,2000,5000,10000,15000,20000}, expected = {0,1,10,25,50,75,100};
        for (int i=0;i<power.length;i++) {
            double credit=0; int energy=0,mined=0;
            for (int tick=0;tick<200;tick++) {
                energy+=power[i]; credit=MiningRate.advance(credit,power[i],20000,5,1.0,5);
                for (int count=0;count<5&&credit+1e-9>=1&&energy>=4000;count++) {credit=Math.max(0,credit-1);energy-=4000;mined++;}
            }
            assertEquals(expected[i]*10,mined); assertEquals(power[i]*200-mined*4000,energy);
        }
    }
    @Test public void storedEnergyAndBacklogCannotBypassBudgets() {
        assertEquals(5,MiningRate.advance(10000,20000,20000,16,4.0,5),0);
        assertEquals(0,MiningRate.advance(0,0,20000,5,1.0,5),0);
        assertEquals(2,MiningRate.advance(0,20000,20000,5,1.0,2),0);
        assertEquals(.25,MiningRate.advance(0,2000,20000,5,.5,5),1e-9);
    }
    @Test public void measuredSpeedExpiresAndDoesNotCountAirSearches() {
        MiningMetrics m=new MiningMetrics();
        for(int tick=0;tick<20;tick++)for(int block=0;block<4;block++)m.record(tick);
        assertEquals(80,m.blocksPerSecond());m.advance(20);assertEquals(76,m.blocksPerSecond());
        m.advance(100);assertEquals(0,m.blocksPerSecond());m.record(100);assertEquals(1,m.blocksPerSecond());
    }
}
