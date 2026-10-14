package dev.industrialtechnology.quarry;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Set;
import dev.industrialtechnology.quarry.BoundaryGeometry.Segment;

public class BoundaryGeometryTest {
    @Test public void loaderGridsHaveExactUniqueEdgesForOneFifteenAndTwentyFiveChunks() {
        assertEquals(4, BoundaryGeometry.build(ChunkArea.CENTER,0,0,11,false).size());
        assertEquals(38, BoundaryGeometry.build((1<<15)-1,0,0,11,false).size());
        assertEquals(60, BoundaryGeometry.build(ChunkArea.ALL,0,0,11,false).size());
    }
    @Test public void quarryFramesRespectSparseAreasAndNeverExtendAbovePlacement() {
        Set<Segment> lines = BoundaryGeometry.build(ChunkArea.CENTER|1,-1,-2,10,true);
        assertEquals(24,lines.size());
        for (Segment s:lines) {
            assertTrue(s.y1>=0&&s.y2>=0&&s.y1<=10&&s.y2<=10);
            assertEquals(0,s.x1%16); assertEquals(0,s.z1%16); assertEquals(0,s.x2%16); assertEquals(0,s.z2%16);
        }
        assertFalse(lines.contains(new Segment(-48,10,-64,0,10,-32)));
    }
    @Test public void quarryPerimetersExcludeSharedEdgesAndKeepInteriorHoles() {
        assertEquals(12,BoundaryGeometry.build(ChunkArea.CENTER,0,0,10,true).size());
        assertEquals(60,BoundaryGeometry.build(ChunkArea.ALL,0,0,10,true).size());
        assertEquals(72,BoundaryGeometry.build(ChunkArea.ALL^(1<<6),0,0,10,true).size());
        assertFalse(BoundaryGeometry.build(ChunkArea.ALL,0,0,10,true).contains(new Segment(0,10,0,16,10,0)));
    }
    @Test public void oldAndMalformedCursorsAreClampedBelowThePlacementLayer() {
        MiningCursor cursor=new MiningCursor(); cursor.read(new int[]{0,999,Integer.MAX_VALUE}); cursor.clampBelow(10);
        for(int i=0;i<25;i++) assertTrue(cursor.position(i)>=(256-10)*256);
        assertEquals(9,cursor.y()); assertEquals(0,cursor.scannedBelow(ChunkArea.CENTER,10));
        cursor.advance(); assertEquals(1,cursor.scannedBelow(ChunkArea.CENTER,10));
        cursor.clampBelow(0); assertFalse(cursor.choose(ChunkArea.ALL));
    }
    @Test public void boundarySegmentIdentityIsDirectionIndependent() {
        Segment a=new Segment(-16,10,0,0,10,0), b=new Segment(0,10,0,-16,10,0);
        assertEquals(a,b); assertEquals(a.hashCode(),b.hashCode());
    }
}
