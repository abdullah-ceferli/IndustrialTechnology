package dev.industrialtechnology.client.space;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/** Low-poly pixel-textured meshes. Coordinates are metres, atlas UVs are pixels. */
public final class SpaceModels {
    private static final int[] LISTS=new int[4];
    private static void draw(int kind){if(LISTS[kind]==0||!GL11.glIsList(LISTS[kind])){LISTS[kind]=net.minecraft.client.renderer.GLAllocation.generateDisplayLists(1);GL11.glNewList(LISTS[kind],GL11.GL_COMPILE);if(kind==0)buildRocket();else if(kind==1)buildComputer();else if(kind==2)buildTelescope();else buildAssembly();GL11.glEndList();}GL11.glCallList(LISTS[kind]);}
    public static void rocket(){draw(0);}public static void computer(){draw(1);}public static void telescope(){draw(2);}public static void assembly(){draw(3);}
    private static void vertex(Tessellator t,double x,double y,double z,double u,double v,double tw,double th){t.addVertexWithUV(x,y,z,u/tw,v/th);}
    private static void quad(Tessellator t,double[] p,double u,double v,double w,double h,double tw,double th){
        vertex(t,p[0],p[1],p[2],u,v+h,tw,th);vertex(t,p[3],p[4],p[5],u+w,v+h,tw,th);vertex(t,p[6],p[7],p[8],u+w,v,tw,th);vertex(t,p[9],p[10],p[11],u,v,tw,th);
    }
    private static void cube(double x,double y,double z,double sx,double sy,double sz,int u,int v,double tw,double th){
        Tessellator t=Tessellator.instance;double X=x+sx,Y=y+sy,Z=z+sz;double a=Math.min(16,sx*16),b=Math.min(16,sy*16),c=Math.min(16,sz*16);t.startDrawingQuads();
        t.setColorOpaque_F(.55f,.55f,.55f);t.setNormal(0,-1,0);quad(t,new double[]{x,y,z,X,y,z,X,y,Z,x,y,Z},u,v,a,c,tw,th);
        t.setColorOpaque_F(1,1,1);t.setNormal(0,1,0);quad(t,new double[]{x,Y,Z,X,Y,Z,X,Y,z,x,Y,z},u,v,a,c,tw,th);
        t.setColorOpaque_F(.78f,.78f,.78f);t.setNormal(0,0,-1);quad(t,new double[]{X,y,z,x,y,z,x,Y,z,X,Y,z},u,v,a,b,tw,th);
        t.setColorOpaque_F(.9f,.9f,.9f);t.setNormal(0,0,1);quad(t,new double[]{x,y,Z,X,y,Z,X,Y,Z,x,Y,Z},u,v,a,b,tw,th);
        t.setColorOpaque_F(.7f,.7f,.7f);t.setNormal(-1,0,0);quad(t,new double[]{x,y,z,x,y,Z,x,Y,Z,x,Y,z},u,v,c,b,tw,th);
        t.setColorOpaque_F(.8f,.8f,.8f);t.setNormal(1,0,0);quad(t,new double[]{X,y,Z,X,y,z,X,Y,z,X,Y,Z},u,v,c,b,tw,th);t.draw();
    }
    private static void part(double x,double y,double z,double sx,double sy,double sz,int material){cube(x,y,z,sx,sy,sz,material*16,0,128,64);}
    private static void panel(double x,double y,double z,double w,double h,int col,int row){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setColorOpaque_F(.9f,.9f,.9f);t.setNormal(0,0,1);quad(t,new double[]{x,y,z,x+w,y,z,x+w,y+h,z,x,y+h,z},col*16,row*16,16,16,128,64);t.draw();
    }
    private static void horizontal(double x,double y,double z,double w,double h,int col,int row){Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setColorOpaque_F(1,1,1);t.setNormal(0,1,0);quad(t,new double[]{x,y,z+h,x+w,y,z+h,x+w,y,z,x,y,z},col*16,row*16,16,16,128,64);t.draw();}
    private static void tube(double y,double h,double r,double R,int v,int vh){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();for(int i=0;i<16;i++){double a=i*Math.PI/8,b=(i+1)*Math.PI/8,mid=(a+b)/2,slope=(r-R)/h,n=Math.sqrt(1+slope*slope);float shade=(float)(.76+.20*Math.cos(mid-.7));t.setColorOpaque_F(shade,shade,shade);t.setNormal((float)(Math.cos(mid)/n),(float)(slope/n),(float)(Math.sin(mid)/n));quad(t,new double[]{Math.cos(b)*r,y,Math.sin(b)*r,Math.cos(a)*r,y,Math.sin(a)*r,Math.cos(a)*R,y+h,Math.sin(a)*R,Math.cos(b)*R,y+h,Math.sin(b)*R},i*8,v,8,vh,128,128);}t.draw();
    }
    private static void fin(){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();
        t.setColorOpaque_F(.9f,.9f,.9f);t.setNormal(0,0,1);quad(t,new double[]{.91,1.05,.085,1.9,1.05,.085,1.48,2.0,.085,.91,3.05,.085},0,96,12,16,128,128);
        t.setColorOpaque_F(.78f,.78f,.78f);t.setNormal(0,0,-1);quad(t,new double[]{1.9,1.05,-.085,.91,1.05,-.085,.91,3.05,-.085,1.48,2.0,-.085},0,96,12,16,128,128);
        t.setNormal(1,1,0);quad(t,new double[]{.91,3.05,-.085,.91,3.05,.085,1.48,2,.085,1.48,2,-.085},0,96,2,16,128,128);
        t.setNormal(1,1,0);quad(t,new double[]{1.48,2,-.085,1.48,2,.085,1.9,1.05,.085,1.9,1.05,-.085},0,96,2,16,128,128);
        t.setColorOpaque_F(.55f,.55f,.55f);t.setNormal(0,-1,0);quad(t,new double[]{.91,1.05,-.085,1.9,1.05,-.085,1.9,1.05,.085,.91,1.05,.085},0,96,12,2,128,128);t.draw();
    }
    private static void buildRocket(){
        // Open bell with inner throat and rim; caps close visible stage interfaces.
        tube(.12,.88,.71,.30,112,16);
        GL11.glCullFace(GL11.GL_FRONT);tube(.13,.84,.64,.24,112,16);GL11.glCullFace(GL11.GL_BACK);tube(.12,.07,.72,.72,96,4);
        tube(1,1.2,1.05,1.05,96,16);tube(2.2,5.6,1.05,1.05,32,56);tube(7.8,.3,1.07,1.07,88,8);
        tube(8.1,1.5,1.05,.8,16,16);tube(9.6,2.1,.8,.015,0,16);
        for(int i=0;i<4;i++){GL11.glPushMatrix();GL11.glRotatef(i*90,0,1,0);fin();
            GL11.glPushMatrix();GL11.glTranslatef(1.06f,1.5f,0);GL11.glRotatef(24,0,0,1);cube(-.10,-1.5,-.10,.2,1.5,.2,0,96,128,128);GL11.glPopMatrix();
            cube(1.48,-.08,-.28,.6,.16,.56,0,96,128,128);GL11.glPopMatrix();}
    }
    private static void buildComputer(){
        part(-.46,0,-.4,.92,.16,.8,0);part(-.37,.17,-.27,.74,.56,.59,0);part(-.28,.21,-.36,.56,.42,.12,6);
        panel(-.37,.17,.323,.74,.56,0,1);part(-.12,.12,-.12,.24,.10,.3,6);
        part(-.43,.16,.34,.86,.055,.25,0);horizontal(-.43,.218,.34,.86,.25,1,1);
        part(.36,.02,.15,.07,.09,.13,6);
    }
    private static void buildTelescope(){
        part(-.16,0,-.16,.32,.10,.32,2);part(-.065,.10,-.065,.13,.48,.13,3);
        for(int i=0;i<3;i++){GL11.glPushMatrix();GL11.glRotatef(i*120,0,1,0);GL11.glTranslatef(0,.42f,0);GL11.glRotatef(28,1,0,0);part(-.035,-.47,-.035,.07,.47,.07,3);GL11.glPopMatrix();}
        part(-.26,.46,-.09,.09,.34,.18,3);part(.17,.46,-.09,.09,.34,.18,3);part(-.25,.68,-.06,.5,.10,.12,2);
        GL11.glPushMatrix();GL11.glTranslatef(0,.75f,0);GL11.glRotatef(-42,1,0,0);
        part(-.155,-.15,-.49,.31,.31,.98,1);part(-.18,-.18,.41,.36,.36,.14,2);part(-.18,-.18,-.35,.36,.36,.08,3);
        part(-.07,-.07,-.68,.14,.14,.2,2);part(-.07,-.16,-.64,.14,.09,.09,3);panel(-.155,-.155,.552,.31,.31,3,1);
        part(.12,.10,-.15,.065,.065,.4,3);GL11.glPopMatrix();
    }
    private static void buildAssembly(){
        part(-.48,0,-.45,.96,.10,.9,3);part(-.44,.10,-.37,.09,.65,.10,2);part(.35,.10,-.37,.09,.65,.10,2);part(-.44,.65,-.37,.88,.09,.10,3);
        part(-.10,.47,-.31,.20,.18,.18,7);part(-.04,.24,-.22,.08,.25,.08,2);part(-.34,.10,-.20,.68,.08,.38,1);
    }
}
