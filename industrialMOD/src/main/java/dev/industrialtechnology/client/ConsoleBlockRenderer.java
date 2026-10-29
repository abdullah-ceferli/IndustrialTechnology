package dev.industrialtechnology.client;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import dev.industrialtechnology.console.BlockConsole;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Static native pixel geometry. The screen is one surface without depth-overlapping layers. */
public final class ConsoleBlockRenderer implements ISimpleBlockRenderingHandler {
    private static final int[][] U={{1,0,0},{1,0,0},{-1,0,0},{1,0,0},{0,0,1},{0,0,-1}};
    private static final int[][] V={{0,0,1},{0,0,-1},{0,1,0},{0,1,0},{0,1,0},{0,1,0}};
    private static final int[][] N={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    private static final double[][] O={{0,0,0},{0,1,1},{1,0,0},{0,0,1},{0,0,0},{1,0,1}};

    private void face(int facing, double x, double y, double z, boolean controller) {
        Tessellator t=Tessellator.instance;
        IIcon icon=net.minecraft.init.Blocks.wool.getIcon(0,0);
        for (ConsoleScreenMesh.Quad q: ConsoleScreenMesh.quads(controller)) {
            t.setColorOpaque_I(q.color);
            vertex(t,icon,facing,x,y,z,q.left,q.bottom);
            vertex(t,icon,facing,x,y,z,q.right,q.bottom);
            vertex(t,icon,facing,x,y,z,q.right,q.top);
            vertex(t,icon,facing,x,y,z,q.left,q.top);
        }
    }

    private void vertex(Tessellator t, IIcon icon, int f, double x, double y, double z, int u, int v) {
        double[] p=ConsoleScreenMesh.point(f,u,v);
        t.addVertexWithUV(x+p[0],y+p[1],z+p[2],icon.getInterpolatedU(u*.5),icon.getInterpolatedV(16-v*.5));
    }

    @Override public boolean renderWorldBlock(IBlockAccess w, int x, int y, int z, Block block, int id, RenderBlocks renderer) {
        renderer.renderStandardBlock(block,x,y,z);
        int f=w.getBlockMetadata(x,y,z)&7;
        if (f<2 || f>5) f=3;
        Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(w,x+N[f][0],y+N[f][1],z+N[f][2]));
        face(f,x,y,z,((BlockConsole)block).controller);
        return true;
    }

    @Override public void renderInventoryBlock(Block block, int meta, int id, RenderBlocks renderer) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(-.5F,-.5F,-.5F);
            Tessellator t=Tessellator.instance;
            IIcon icon=block.getIcon(0,0);
            for (int f=0; f<6; f++) {
                t.startDrawingQuads();
                t.setNormal(N[f][0],N[f][1],N[f][2]);
                t.setColorOpaque_F(1,1,1);
                double[][] points={{0,0,0,1},{1,0,1,1},{1,1,1,0},{0,1,0,0}};
                for (double[] p:points) t.addVertexWithUV(O[f][0]+U[f][0]*p[0]+V[f][0]*p[1],
                    O[f][1]+U[f][1]*p[0]+V[f][1]*p[1],O[f][2]+U[f][2]*p[0]+V[f][2]*p[1],
                    icon.getInterpolatedU(p[2]*16),icon.getInterpolatedV(p[3]*16));
                if (f==3) face(f,0,0,0,((BlockConsole)block).controller);
                t.draw();
            }
        } finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
    }
    @Override public boolean shouldRender3DInInventory(int id) { return true; }
    @Override public int getRenderId() { return BlockConsole.renderId; }
}
