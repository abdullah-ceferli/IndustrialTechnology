package dev.industrialtechnology.client;

import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.quarry.TileQuarry;
import dev.industrialtechnology.quarry.BoundaryGeometry;
import dev.industrialtechnology.quarry.BoundaryGeometry.Segment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

/** World-visible state comes from vanilla description packets, including initial chunk watches. */
public final class WorldBoundaries {
    private final Map<TileMachine, Cache> caches = new IdentityHashMap<TileMachine, Cache>();
    private World world;
    private Set<Segment> geometry = new LinkedHashSet<Segment>();
    private int ticks;
    private static final class Cache {
        int mask = -1, top = -1, x, z;
        Set<Segment> lines;
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++ticks % 5 != 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (world != mc.theWorld) { world = mc.theWorld; caches.clear(); geometry.clear(); }
        if (world == null || mc.thePlayer == null) return;
        Set<TileMachine> seen = new HashSet<TileMachine>();
        Set<Segment> next = new LinkedHashSet<Segment>();
        double range = Math.min(160, (mc.gameSettings.renderDistanceChunks + 2) * 16);
        for (Object object : world.loadedTileEntityList) {
            if (!(object instanceof TileMachine)) continue;
            TileMachine tile = (TileMachine) object;
            if (tile.isInvalid() || !tile.enabled || mc.thePlayer.getDistanceSq(tile.xCoord+.5, tile.yCoord+.5, tile.zCoord+.5) > range*range) continue;
            boolean quarry = tile instanceof TileQuarry;
            if (quarry ? !OverlaySettings.quarries : !OverlaySettings.loaders) continue;
            int mask = quarry ? tile.selected : tile.loadedMask;
            if (mask == 0) continue;
            int top = quarry ? Math.max(0, ((TileQuarry) tile).miningCeiling) : tile.yCoord + 1;
            seen.add(tile); Cache cache = caches.get(tile);
            if (cache == null) { cache = new Cache(); caches.put(tile, cache); }
            if (cache.mask != mask || cache.top != top || cache.x != tile.xCoord || cache.z != tile.zCoord) {
                cache.mask = mask; cache.top = top; cache.x = tile.xCoord; cache.z = tile.zCoord;
                cache.lines = BoundaryGeometry.build(mask, tile.xCoord >> 4, tile.zCoord >> 4, top, quarry);
            }
            next.addAll(cache.lines);
        }
        caches.keySet().retainAll(seen);
        // Deduplicate coincident segments across every nearby machine. No geometry work occurs per frame.
        if (!geometry.equals(next)) geometry = next;
    }
    public int segmentCount() { return geometry.size(); }
    @SubscribeEvent public void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (world == null || world != mc.theWorld || geometry.isEmpty() || mc.renderViewEntity == null) return;
        Entity camera = mc.renderViewEntity;
        double x = camera.lastTickPosX + (camera.posX-camera.lastTickPosX)*event.partialTicks;
        double y = camera.lastTickPosY + (camera.posY-camera.lastTickPosY)*event.partialTicks;
        double z = camera.lastTickPosZ + (camera.posZ-camera.lastTickPosZ)*event.partialTicks;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS); GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT); GL11.glPushMatrix();
        try {
            GL11.glTranslated(-x,-y,-z); GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false); GL11.glDisable(GL11.GL_DEPTH_TEST); GL11.glLineWidth(1.5f);
            Tessellator t = Tessellator.instance; t.startDrawing(GL11.GL_LINES); t.setColorRGBA(255,48,48,160);
            for (Segment s : geometry) { t.addVertex(s.x1,s.y1+.02,s.z1); t.addVertex(s.x2,s.y2+.02,s.z2); }
            t.draw();
        } finally { GL11.glPopMatrix(); GL11.glPopClientAttrib(); GL11.glPopAttrib(); }
    }
}
