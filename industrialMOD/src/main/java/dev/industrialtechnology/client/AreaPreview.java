package dev.industrialtechnology.client;

import dev.industrialtechnology.core.ModConfig;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.quarry.ChunkArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** At most 25 rectangles / 200 vertices, enabled only for a 30-second preview. */
public final class AreaPreview {
    private static int mask, centerX, centerZ, y, dimension;
    private static int ownerX, ownerY, ownerZ;
    private static long expires;
    public static boolean isActive(TileMachine tile) {
        return tile.getWorldObj() != null && mask != 0 && ownerX == tile.xCoord && ownerY == tile.yCoord && ownerZ == tile.zCoord
            && dimension == tile.getWorldObj().provider.dimensionId && tile.getWorldObj().getTotalWorldTime() <= expires;
    }
    public static void toggle(TileMachine tile, int selected) { if (isActive(tile)) mask = 0; else activate(tile, selected); }
    public static void activate(TileMachine tile, int selected) {
        if (!ModConfig.preview) return;
        mask = selected; centerX = tile.xCoord >> 4; centerZ = tile.zCoord >> 4;
        ownerX = tile.xCoord; ownerY = tile.yCoord; ownerZ = tile.zCoord;
        y = tile.yCoord + 1; dimension = tile.getWorldObj().provider.dimensionId;
        expires = tile.getWorldObj().getTotalWorldTime() + 600;
    }
    @SubscribeEvent public void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!ModConfig.preview || mc.theWorld == null || mc.theWorld.provider.dimensionId != dimension || mc.theWorld.getTotalWorldTime() > expires || mask == 0) return;
        Entity viewer = mc.renderViewEntity;
        double vx = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double vy = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double vz = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(-vx, -vy, -vz); GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false); GL11.glLineWidth(1.5f);
            Tessellator t = Tessellator.instance; t.startDrawing(GL11.GL_LINES); t.setColorRGBA(199, 156, 85, 200);
            for (int cell = 0; cell < 25; cell++) if ((mask & (1 << cell)) != 0) {
                int x = (centerX + ChunkArea.dx(cell)) * 16, z = (centerZ + ChunkArea.dz(cell)) * 16;
                line(t, x, z, x + 16, z); line(t, x + 16, z, x + 16, z + 16);
                line(t, x + 16, z + 16, x, z + 16); line(t, x, z + 16, x, z);
            }
            t.draw();
        } finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
    }
    private static void line(Tessellator t, int x1, int z1, int x2, int z2) { t.addVertex(x1, y + .025, z1); t.addVertex(x2, y + .025, z2); }
}
