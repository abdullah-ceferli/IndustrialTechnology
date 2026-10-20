package dev.industrialtechnology.automation;
import org.junit.Test;
import static org.junit.Assert.*;
/** Deterministic game-clock tests: no waits or Minecraft world needed. */
public class RepeatTimingTest {
    private ScriptRule every(long ticks){ScriptRule r=new ScriptRule();r.intervalTicks=ticks;return r;}
    @Test public void legacyWithoutEveryStillUsesOneRisingEdge(){ScriptRule r=every(0);assertTrue(r.due(true,0));assertFalse(r.due(true,100000));assertFalse(r.due(false,100001));assertTrue(r.due(true,100002));assertEquals(0,r.nextRun);}
    @Test public void runsImmediatelyThenExactlyAtTheInterval(){ScriptRule r=every(400);assertTrue(r.due(true,100));assertEquals(500,r.nextRun);assertFalse(r.due(true,499));assertTrue(r.due(true,500));assertEquals(900,r.nextRun);assertFalse(r.due(true,500));}
    @Test public void falseStopsRepeatsAndNextTrueStartsFresh(){ScriptRule r=every(100);r.due(true,100);assertFalse(r.due(false,150));assertEquals(0,r.nextRun);assertFalse(r.latched);assertFalse(r.due(false,9999));assertTrue(r.due(true,10000));assertEquals(10100,r.nextRun);}
    @Test public void missedIntervalsProduceOneActionAndNoCatchUpBurst(){ScriptRule r=every(100);r.due(true,0);assertTrue(r.due(true,20000));assertEquals(20100,r.nextRun);assertFalse(r.due(true,20000));assertFalse(r.due(true,20099));}
    @Test public void minutesAndTwentyHoursKeepTheirFullDelay(){for(long ticks:new long[]{20*1200L,20*72000L}){ScriptRule r=every(ticks);r.due(true,200);assertFalse(r.due(true,200+ticks-1));assertTrue(r.due(true,200+ticks));}}
    @Test public void unchangedRulesMatchDespiteTriggerMemoryButIntervalChangesReset(){ScriptRule a=every(400),b=every(400);a.kind=b.kind=ScriptRule.Kind.TANK_FULL;a.action=b.action=ScriptRule.Action.ON;a.due(true,100);assertTrue(a.same(b));b.intervalTicks=500;assertFalse(a.same(b));}
    @Test public void extremeWorldTimeDoesNotOverflowDeadline(){ScriptRule r=every(400);assertTrue(r.due(true,Long.MAX_VALUE-100));assertEquals(Long.MAX_VALUE,r.nextRun);assertFalse(r.due(true,Long.MAX_VALUE-1));}
}
