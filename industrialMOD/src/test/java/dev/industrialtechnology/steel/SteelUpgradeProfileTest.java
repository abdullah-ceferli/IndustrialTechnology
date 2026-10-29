package dev.industrialtechnology.steel;
import org.junit.Test;
import static org.junit.Assert.*;
public class SteelUpgradeProfileTest {
    private void profile(int a,int b,int ticks,int he,int coal){SteelUpgradeProfile p=SteelUpgradeProfile.calculate(a,b);assertEquals(ticks,p.ticks);assertEquals(he,p.hePerTick);assertEquals(coal,p.coal);}
    @Test public void unmodifiedRecipe(){profile(0,0,100,500,4);}
    @Test public void speedTiersAndDuplicateCap(){profile(5,0,75,1000,4);profile(7,0,25,2000,4);profile(5,5,50,1500,4);profile(7,7,25,2000,4);}
    @Test public void powerSavingTradeoff(){profile(9,0,110,350,4);profile(11,0,130,50,4);}
    @Test public void effectivenessSavesCoalWithoutFreeSteel(){profile(13,0,100,625,3);profile(15,0,100,875,1);}
    @Test public void overdriveCostsMoreEnergyForThroughput(){profile(17,0,50,1500,4);profile(19,0,25,3500,4);}
    @Test public void mixedUpgradeCombinations(){profile(7,11,55,1550,4);profile(7,19,7,14000,4);profile(11,15,130,425,1);}
    @Test public void slotOrderDoesNotChangeAnyCombination(){for(int a=0;a<20;a++)for(int b=0;b<20;b++){SteelUpgradeProfile x=SteelUpgradeProfile.calculate(a,b),y=SteelUpgradeProfile.calculate(b,a);assertEquals(x.ticks,y.ticks);assertEquals(x.hePerTick,y.hePerTick);assertEquals(x.coal,y.coal);assertTrue(x.ticks>=5&&x.ticks<=130&&x.hePerTick>=50&&x.hePerTick<=14000&&x.coal>=1&&x.coal<=4);}}
    @Test public void malformedUpgradeCodesLeaveBaseParameters(){profile(Integer.MIN_VALUE,Integer.MAX_VALUE,100,500,4);}
}
