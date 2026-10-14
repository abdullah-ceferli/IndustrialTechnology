package dev.industrialtechnology.client;

import dev.industrialtechnology.chunkloading.TileChunkLoader;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Eight original cyan crystal facets. One revolution per 100 ticks; OFF is completely stationary. */
public final class ChunkLoaderRenderer extends TileEntitySpecialRenderer {
    private static final double[][] RING = {{-.19,-.19},{.19,-.19},{.19,.19},{-.19,.19}};
    private static final int[][] COLORS = {{76,211,218},{113,238,235},{40,156,180},{64,188,204}};
    public static double angle(boolean enabled, double time) { return enabled ? (time % 100.0) * 3.6 : 0; }
    public static double bob(boolean enabled, double time) { return enabled ? .025 * Math.sin(time * Math.PI / 50.0) : 0; }
    @Override public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float partial) {
        TileChunkLoader tile = (TileChunkLoader) entity;
        double time = tile.getWorldObj() == null ? 0 : tile.getWorldObj().getTotalWorldTime() + partial;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS); GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT); GL11.glPushMatrix();
        try {
            GL11.glTranslated(x+.5,y+1.42+bob(tile.enabled,time),z+.5); GL11.glRotated(angle(tile.enabled,time),0,1,0);
            // Untextured facets must disable both base texture and the world's multiplicative lightmap.
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit); GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit); GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING); GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator t = Tessellator.instance; t.startDrawing(GL11.GL_TRIANGLES);
            for (int i=0;i<4;i++) {
                double[] a=RING[i], b=RING[(i+1)%4]; int[] c=COLORS[i];
                t.setColorRGBA(c[0],c[1],c[2],235); t.addVertex(0,.32,0); t.addVertex(a[0],0,a[1]); t.addVertex(b[0],0,b[1]);
                t.setColorRGBA(c[0]*3/4,c[1]*3/4,c[2]*3/4,235); t.addVertex(0,-.26,0); t.addVertex(b[0],0,b[1]); t.addVertex(a[0],0,a[1]);
            }
            t.draw();
        } finally { GL11.glPopMatrix(); GL11.glPopClientAttrib(); GL11.glPopAttrib(); }
    }
}
