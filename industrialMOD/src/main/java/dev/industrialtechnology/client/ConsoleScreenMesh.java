package dev.industrialtechnology.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A single non-overlapping pixel surface, shared by all four console facings. */
public final class ConsoleScreenMesh {
    public static final int SIZE = 32;
    public static final double OFFSET = 1.0 / 64.0;
    private static final List<Quad> HISTORY = build(false);
    private static final List<Quad> CONTROLLER = build(true);

    public static final class Quad {
        public final int left, bottom, right, top, color;
        private Quad(int l, int b, int r, int t, int c) {
            left=l; bottom=b; right=r; top=t; color=c;
        }
    }

    public static List<Quad> quads(boolean controller) {
        return controller ? CONTROLLER : HISTORY;
    }

    private static void paint(int[][] pixels, int l, int b, int r, int t, int color) {
        for (int y=b; y<t; y++) for (int x=l; x<r; x++) pixels[y][x]=color;
    }

    private static List<Quad> build(boolean controller) {
        int[][] pixels = new int[SIZE][SIZE];
        int accent = controller ? 0xd7ad60 : 0x61b9ac;
        paint(pixels,3,3,29,29,0x263238);
        paint(pixels,5,7,27,27,0x0b171d);
        if (controller) {
            for (int i=0; i<3; i++) {
                paint(pixels,7,10+i*5,21,12+i*5,0x54717a);
                paint(pixels,23,10+i*5,25,12+i*5,accent);
            }
        } else {
            int[] heights={4,5,5,7,6,8,9,8,10,11};
            for (int i=0; i<heights.length; i++)
                paint(pixels,7+i*2,9+heights[i],9+i*2,10+heights[i],accent);
        }
        paint(pixels,7,4,10,6,accent);
        paint(pixels,14,4,17,6,0x68848d);
        paint(pixels,20,4,23,6,0x68848d);

        // Merge equally colored cells once. Each surface cell is emitted exactly once:
        // no stacked frame/background/glyph quads for the depth buffer to confuse.
        List<Quad> result = new ArrayList<Quad>();
        for (int y=0; y<SIZE; y++) for (int x=0; x<SIZE; x++) {
            int color=pixels[y][x];
            if (color==0) continue;
            int right=x+1, top=y+1;
            while (right<SIZE && pixels[y][right]==color) right++;
            outer: while (top<SIZE) {
                for (int xx=x; xx<right; xx++) if (pixels[top][xx]!=color) break outer;
                top++;
            }
            result.add(new Quad(x,y,right,top,color));
            paint(pixels,x,y,right,top,0);
        }
        return Collections.unmodifiableList(result);
    }

    /** Coordinates are block-local and binary-exact, including the casing clearance. */
    public static double[] point(int facing, int pixelX, int pixelY) {
        double u=pixelX/(double)SIZE, v=pixelY/(double)SIZE;
        switch (facing) {
            case 2: return new double[]{1-u,v,-OFFSET};
            case 4: return new double[]{-OFFSET,v,u};
            case 5: return new double[]{1+OFFSET,v,1-u};
            default: return new double[]{u,v,1+OFFSET};
        }
    }
    private ConsoleScreenMesh() {}
}
