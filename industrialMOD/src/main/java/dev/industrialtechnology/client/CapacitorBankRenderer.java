package dev.industrialtechnology.client;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import dev.industrialtechnology.capacitor.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
/** Exact, unmodified Ender IO texture layers in a cached chunk mesh; no tile renderer or animation ticks. */
public final class CapacitorBankRenderer implements ISimpleBlockRenderingHandler {
    private static final int[][] U={{1,0,0},{1,0,0},{-1,0,0},{1,0,0},{0,0,1},{0,0,-1}};
    private static final int[][] V={{0,0,1},{0,0,-1},{0,1,0},{0,1,0},{0,1,0},{0,1,0}};
    private static final double[][] O={{0,0,0},{0,1,1},{1,0,0},{0,0,1},{0,0,0},{1,0,1}};
    private static final int[][] N={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    private static final float[] SHADE={.5F,1F,.8F,.8F,.6F,.6F};
    private void quad(IIcon icon,int face,double x,double y,double z,double u0,double v0,double u1,double v1,double offset){
        texturedQuad(icon,face,x,y,z,u0,v0,u1,v1,u0,1-v0,u1,1-v1,offset);
    }
    private void texturedQuad(IIcon icon,int face,double x,double y,double z,double u0,double v0,double u1,double v1,double tu0,double tv0,double tu1,double tv1,double offset){
        Tessellator t=Tessellator.instance;double[][] points={{u0,v0,tu0,tv0},{u1,v0,tu1,tv0},{u1,v1,tu1,tv1},{u0,v1,tu0,tv1}};
        for(double[] p:points)t.addVertexWithUV(x+O[face][0]+U[face][0]*p[0]+V[face][0]*p[1]+N[face][0]*offset,y+O[face][1]+U[face][1]*p[0]+V[face][1]*p[1]+N[face][1]*offset,z+O[face][2]+U[face][2]*p[0]+V[face][2]*p[1]+N[face][2]*offset,icon.getInterpolatedU(p[2]*16),icon.getInterpolatedV(p[3]*16));
    }
    private boolean neighbor(IBlockAccess w,Block b,int x,int y,int z,int[] axis,int sign){return w!=null&&w.getBlock(x+axis[0]*sign,y+axis[1]*sign,z+axis[2]*sign)==b;}
    private void layers(IBlockAccess w,BlockCapacitorBank b,int x,int y,int z,int face,int front,int level,boolean gauge){
        double edge=.125;
        if(!neighbor(w,b,x,y,z,U[face],-1))quad(b.border,face,x,y,z,0,0,edge,1,.0003);
        if(!neighbor(w,b,x,y,z,U[face],1))quad(b.border,face,x,y,z,1-edge,0,1,1,.0003);
        if(!neighbor(w,b,x,y,z,V[face],-1))quad(b.border,face,x,y,z,0,0,1,edge,.0004);
        if(!neighbor(w,b,x,y,z,V[face],1))quad(b.border,face,x,y,z,0,1-edge,1,1,.0004);
        if(gauge){
            // The original atlas has FOUR alternative strips, not three pipes to paint across a face.
            // SINGLE is its first quarter; Ender IO's meter is 0.25 wide and 0.8 high.
            texturedQuad(b.gauge,face,x,y,z,.375,.1,.625,.9,0,1,.25,0,.0006);
            double charge=Math.max(0,Math.min(1,level/16.0));
            if(charge>0){
                if(level==16){Tessellator.instance.setColorOpaque_F(.96F*SHADE[face],.76F*SHADE[face],.25F*SHADE[face]);quad(net.minecraft.init.Blocks.wool.getIcon(0,0),face,x,y,z,.405,.12,.595,.88,.0008);}
                else texturedQuad(b.fill,face,x,y,z,.375,.1,.625,.1+.8*charge,0,1,.25,1-charge,.0008);
            }
        }
    }
    @Override public boolean renderWorldBlock(IBlockAccess w,int x,int y,int z,Block block,int model,RenderBlocks r){
        BlockCapacitorBank b=(BlockCapacitorBank)block;r.renderStandardBlock(block,x,y,z);TileEntity te=w.getTileEntity(x,y,z);TileCapacitorBank tile=te instanceof TileCapacitorBank?(TileCapacitorBank)te:null;int front=w.getBlockMetadata(x,y,z),level=tile==null?0:tile.displayCharge;
        Tessellator t=Tessellator.instance;t.setBrightness(block.getMixedBrightnessForBlock(w,x,y,z));
        for(int face=0;face<6;face++)if(block.shouldSideBeRendered(w,x+N[face][0],y+N[face][1],z+N[face][2],face)){t.setBrightness(block.getMixedBrightnessForBlock(w,x+N[face][0],y+N[face][1],z+N[face][2]));t.setColorOpaque_F(SHADE[face],SHADE[face],SHADE[face]);layers(w,b,x,y,z,face,front,level,tile!=null&&tile.showsGauge(face)&&tile.mode(net.minecraftforge.common.util.ForgeDirection.getOrientation(face))==SideMode.BOTH);
            SideMode mode=tile==null?SideMode.BOTH:tile.mode(net.minecraftforge.common.util.ForgeDirection.getOrientation(face));
            if(mode==SideMode.INPUT||mode==SideMode.OUTPUT){int c=mode.color;t.setColorOpaque_F(((c>>16)&255)/255F*SHADE[face],((c>>8)&255)/255F*SHADE[face],(c&255)/255F*SHADE[face]);IIcon white=net.minecraft.init.Blocks.wool.getIcon(0,0);quad(white,face,x,y,z,.28,.44,.72,.56,.001);if(mode==SideMode.INPUT)quad(white,face,x,y,z,.44,.28,.56,.72,.0011);}
        }
        return true;
    }
    @Override public void renderInventoryBlock(Block block,int meta,int model,RenderBlocks r){
        BlockCapacitorBank b=(BlockCapacitorBank)block;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{GL11.glTranslatef(-.5F,-.5F,-.5F);GL11.glColor4f(1,1,1,1);Tessellator t=Tessellator.instance;
            for(int face=0;face<6;face++){t.startDrawingQuads();t.setNormal(N[face][0],N[face][1],N[face][2]);t.setColorOpaque_F(1,1,1);quad(b.base,face,0,0,0,0,0,1,1,0);layers(null,b,0,0,0,face,3,0,false);t.draw();}
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
    @Override public boolean shouldRender3DInInventory(int model){return true;}
    @Override public int getRenderId(){return BlockCapacitorBank.renderId;}
}
