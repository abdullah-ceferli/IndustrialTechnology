package dev.industrialtechnology.quarry;

import org.junit.Test;
import static org.junit.Assert.*;

public class AreaCursorTest {
    @Test public void masksRejectMissingCenterAndOutsideBits() {
        assertFalse(ChunkArea.valid(0, 25));
        assertFalse(ChunkArea.valid(ChunkArea.CENTER | (1 << 29), 25));
        assertFalse(ChunkArea.valid(ChunkArea.ALL, 15));
        assertTrue(ChunkArea.valid(ChunkArea.sanitize(ChunkArea.ALL, 15), 15));
        assertEquals(15, Integer.bitCount(ChunkArea.sanitize(ChunkArea.ALL, 15)));
        assertEquals(ChunkArea.CENTER, ChunkArea.sanitize(-1, 1));
    }
    @Test public void presetsAndNegativeChunkCoordinatesAreExact() {
        assertEquals(9, Integer.bitCount(ChunkArea.square3()));
        MiningCursor cursor = new MiningCursor();
        assertEquals(-16, cursor.x(-1)); assertEquals(-16, cursor.z(-1));
        assertEquals(255, cursor.y());
        cursor.set(12, 255);
        assertEquals(-1, cursor.x(-1)); assertEquals(-1, cursor.z(-1)); assertEquals(255, cursor.y());
        cursor.advance(); assertEquals(254, cursor.y()); assertEquals(-16, cursor.x(-1));
    }
    @Test public void scansEveryPositionExactlyOnceWithoutCrossingSelection() {
        MiningCursor cursor = new MiningCursor();
        int mask = ChunkArea.CENTER | 1;
        long count = 0;
        while (cursor.choose(mask)) {
            int x = cursor.x(0), z = cursor.z(0), y = cursor.y();
            assertTrue(y >= 0 && y <= 255);
            assertTrue(cursor.cell == 12 ? x >= 0 && x < 16 && z >= 0 && z < 16 : x >= -32 && x < -16 && z >= -32 && z < -16);
            cursor.advance(); count++;
        }
        assertEquals(2L * MiningCursor.CELLS_PER_CHUNK, count);
        assertEquals(mask, cursor.completedMask()); assertEquals(count, cursor.scanned(mask));
        assertEquals(0, cursor.position(1));
    }
    @Test public void cursorSurvivesSparseSelectionChangesAndRoundTrip() {
        MiningCursor first = new MiningCursor(); first.set(12, 9381); first.set(2, 998);
        assertTrue(first.choose(ChunkArea.CENTER | (1 << 2)));
        MiningCursor restored = new MiningCursor(); restored.read(first.copy()); restored.cell = first.cell;
        assertEquals(first.x(-77), restored.x(-77)); assertEquals(first.y(), restored.y());
        assertEquals(first.scanned(ChunkArea.ALL), restored.scanned(ChunkArea.ALL));
        restored.choose(ChunkArea.CENTER); assertEquals(998, restored.position(2));
    }
    @Test public void corruptCursorPositionsAreClamped() {
        MiningCursor c = new MiningCursor(); c.read(new int[]{-999, Integer.MAX_VALUE});
        assertEquals(0, c.position(0)); assertEquals(MiningCursor.CELLS_PER_CHUNK, c.position(1)); assertEquals(0, c.position(12));
    }
}
