package personal.blockhits;

import org.junit.Test;
import static org.junit.Assert.*;

public class HitCounterTest {
    @Test public void startsHiddenAndIgnoresHits() {
        HitCounter counter = new HitCounter();
        counter.hit();
        assertFalse(counter.enabled());
        assertEquals(0, counter.hits());
    }
    @Test public void eachHitCountsAndToggleHidesClearsThenStartsFresh() {
        HitCounter counter = new HitCounter();
        counter.toggle();
        for (int i = 0; i < 7; i++) counter.hit();
        assertEquals(7, counter.hits());
        counter.toggle();
        assertFalse(counter.enabled());
        assertEquals(0, counter.hits());
        counter.hit();
        counter.toggle();
        assertTrue(counter.enabled());
        assertEquals(0, counter.hits());
    }
    @Test public void leavingSessionClearsEnabledCounter() {
        HitCounter counter = new HitCounter();
        counter.toggle(); counter.hit(); counter.reset();
        assertFalse(counter.enabled());
        assertEquals(0, counter.hits());
    }
}
