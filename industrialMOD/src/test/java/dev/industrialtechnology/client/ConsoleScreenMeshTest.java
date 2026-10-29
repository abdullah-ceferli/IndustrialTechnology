package dev.industrialtechnology.client;
import org.junit.Test;
import static org.junit.Assert.*;

public final class ConsoleScreenMeshTest {
    @Test public void everyScreenPixelIsCoveredOnceWithoutOverlappingQuads() {
        for (boolean controller:new boolean[]{false,true}) {
            int[][] coverage=new int[32][32];
            for (ConsoleScreenMesh.Quad q:ConsoleScreenMesh.quads(controller)) {
                assertTrue(q.left>=0 && q.right<=32 && q.bottom>=0 && q.top<=32);
                assertTrue(q.left<q.right && q.bottom<q.top);
                for (int y=q.bottom;y<q.top;y++) for (int x=q.left;x<q.right;x++) coverage[y][x]++;
            }
            for(int y=0;y<32;y++) for(int x=0;x<32;x++)
                assertEquals("pixel "+x+","+y,x>=3&&x<29&&y>=3&&y<29?1:0,coverage[y][x]);
        }
    }
    @Test public void everyFacingUsesOneExactPlaneOutsideTheIronCasing() {
        for(int f=2;f<=5;f++) for(boolean c:new boolean[]{false,true})
            for(ConsoleScreenMesh.Quad q:ConsoleScreenMesh.quads(c))
                for(int[] uv:new int[][]{{q.left,q.bottom},{q.right,q.bottom},{q.right,q.top},{q.left,q.top}}) {
                    double[] p=ConsoleScreenMesh.point(f,uv[0],uv[1]);
                    int axis=f<4?2:0;
                    assertEquals(f==2||f==4?-ConsoleScreenMesh.OFFSET:1+ConsoleScreenMesh.OFFSET,p[axis],0);
                    for(int a=0;a<3;a++) if(a!=axis) assertTrue(p[a]>=0 && p[a]<=1);
                }
    }
    @Test public void allFacingsHaveOutwardWindingAndKeepTheScreenUpright() {
        int[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for(int f=2;f<=5;f++) {
            double[] a=ConsoleScreenMesh.point(f,3,3),b=ConsoleScreenMesh.point(f,29,3),c=ConsoleScreenMesh.point(f,29,29);
            double[] u={b[0]-a[0],b[1]-a[1],b[2]-a[2]},v={c[0]-b[0],c[1]-b[1],c[2]-b[2]};
            double[] cross={u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]};
            double dot=0;for(int i=0;i<3;i++)dot+=cross[i]*normals[f-2][i];assertTrue(dot>0);
            assertTrue(c[1]>b[1]);assertEquals(a[1],b[1],0);
        }
    }
    @Test public void shapesAreMergedAndCachedRatherThanEmittingPerPixelMeshes() {
        for(boolean c:new boolean[]{false,true}) {
            assertSame(ConsoleScreenMesh.quads(c),ConsoleScreenMesh.quads(c));
            assertTrue(ConsoleScreenMesh.quads(c).size()<80);
            try { ConsoleScreenMesh.quads(c).clear();fail("mutable mesh"); }catch(UnsupportedOperationException expected){}
        }
    }
}
