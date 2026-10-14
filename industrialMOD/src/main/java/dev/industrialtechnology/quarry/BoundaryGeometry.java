package dev.industrialtechnology.quarry;

import java.util.Set;
import java.util.LinkedHashSet;

/** Integer, chunk-aligned geometry. Shared pure code for sparse quarry perimeters and loader grids. */
public final class BoundaryGeometry {
    private BoundaryGeometry() {}
    public static final class Segment {
        public final int x1, y1, z1, x2, y2, z2;
        public Segment(int x1, int y1, int z1, int x2, int y2, int z2) {
            boolean reverse = x1 > x2 || x1 == x2 && (y1 > y2 || y1 == y2 && z1 > z2);
            this.x1 = reverse ? x2 : x1; this.y1 = reverse ? y2 : y1; this.z1 = reverse ? z2 : z1;
            this.x2 = reverse ? x1 : x2; this.y2 = reverse ? y1 : y2; this.z2 = reverse ? z1 : z2;
        }
        @Override public boolean equals(Object value) {
            if (!(value instanceof Segment)) return false; Segment s = (Segment) value;
            return x1 == s.x1 && y1 == s.y1 && z1 == s.z1 && x2 == s.x2 && y2 == s.y2 && z2 == s.z2;
        }
        @Override public int hashCode() { int h = x1; h = 31*h+y1; h=31*h+z1; h=31*h+x2; h=31*h+y2; return 31*h+z2; }
    }
    public static Set<Segment> build(int mask, int centerX, int centerZ, int top, boolean quarry) {
        Set<Segment> lines = new LinkedHashSet<Segment>();
        for (int cell = 0; cell < 25; cell++) if ((mask & (1 << cell)) != 0) {
            int col = cell % 5, row = cell / 5;
            int x = (centerX + col - 2) * 16, z = (centerZ + row - 2) * 16;
            if (!quarry || row == 0 || (mask & (1 << (cell - 5))) == 0) edge(lines,x,z,x+16,z,top,quarry);
            if (!quarry || col == 4 || (mask & (1 << (cell + 1))) == 0) edge(lines,x+16,z,x+16,z+16,top,quarry);
            if (!quarry || row == 4 || (mask & (1 << (cell + 5))) == 0) edge(lines,x,z+16,x+16,z+16,top,quarry);
            if (!quarry || col == 0 || (mask & (1 << (cell - 1))) == 0) edge(lines,x,z,x,z+16,top,quarry);
        }
        return lines;
    }
    private static void edge(Set<Segment> lines,int x1,int z1,int x2,int z2,int top,boolean quarry) {
        lines.add(new Segment(x1,top,z1,x2,top,z2));
        if (quarry && top > 0) {
            lines.add(new Segment(x1,0,z1,x2,0,z2));
            lines.add(new Segment(x1,0,z1,x1,top,z1)); lines.add(new Segment(x2,0,z2,x2,top,z2));
        }
    }
}
