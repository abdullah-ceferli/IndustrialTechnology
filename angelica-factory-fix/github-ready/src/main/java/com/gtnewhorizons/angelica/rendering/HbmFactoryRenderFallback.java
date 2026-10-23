// SPDX-License-Identifier: LGPL-3.0-only
// Unofficial HBM compatibility addition, 2026-10-23. See CREDITS.md and hbm-patch/CHANGES.md.
package com.gtnewhorizons.angelica.rendering;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;

/** Main-thread fallback for working HBM machines omitted from an asynchronous section render list. */
public final class HbmFactoryRenderFallback {
    private static final IdentityHashMap<TileEntity,Boolean> seen=new IdentityHashMap<TileEntity,Boolean>();
    private static boolean reported;
    public static long rescued;
    private HbmFactoryRenderFallback() {}

    public static void reconcile(World world,List<TileEntity> candidates,Viewport viewport) {
        if(world==null)return;
        try {
            for(Iterator<TileEntity> it=candidates.iterator();it.hasNext();) {
                TileEntity te=it.next();
                if(te==null)continue;
                boolean hbm=te.getClass().getName().startsWith("com.hbm.");
                if(hbm && (te.isInvalid() || seen.containsKey(te))){it.remove();continue;}
                seen.put(te,Boolean.TRUE);
            }
            List<?> live=world.loadedTileEntityList;
            int added=0;
            for(int i=0;i<live.size();i++) {
                Object entry=live.get(i);
                if(!(entry instanceof TileEntity))continue;
                TileEntity te=(TileEntity)entry;
                if(!te.getClass().getName().startsWith("com.hbm.") || te.isInvalid() || seen.containsKey(te))continue;
                if(!TileEntityRendererDispatcher.instance.hasSpecialRenderer(te) || !visible(te,viewport))continue;
                // Existing dispatcher still enforces render distance and render-pass rules.
                seen.put(te,Boolean.TRUE);candidates.add(te);added++;
            }
            rescued+=added;
            if(added>0 && !reported) {
                reported=true;
                System.out.println("[PersonalAngelica] HBM live-list fallback active; recovered "+added+" machine entries for this frame.");
            }
        } finally {
            // Never retain a world or tile entities after the frame or on world unload.
            seen.clear();
        }
    }
    private static boolean visible(TileEntity te,Viewport viewport) {
        if(viewport==null)return true;
        try {
            AxisAlignedBB b=te.getRenderBoundingBox();
            if(TileEntityRenderBoundsRegistry.isInfiniteExtentsBox(b))return true;
            if(Double.isNaN(b.minX)||Double.isNaN(b.minY)||Double.isNaN(b.minZ)||Double.isNaN(b.maxX)||Double.isNaN(b.maxY)||Double.isNaN(b.maxZ)
                ||b.minX>b.maxX||b.minY>b.maxY||b.minZ>b.maxZ)return true;
            return viewport.isBoxVisible(b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);
        } catch(RuntimeException unavailableBounds) {
            // An uncertain bound must not make a working factory disappear.
            return true;
        }
    }
}
