package dev.industrialtechnology.client.space;
import org.lwjgl.opengl.GL11;import net.minecraft.client.renderer.Tessellator;
/** Authored metre-scale mesh: ET, SRBs, delta-wing orbiter and three open engine bells. */
public final class ShuttleModel {
    private static int list;
    private static void quad(double[] p,int mat,float shade){Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setColorOpaque_F(shade,shade,shade);double u=(mat%8)*16/128.0,v=(mat/8)*16/128.0;for(int i=0;i<4;i++)t.addVertexWithUV(p[i*3],p[i*3+1],p[i*3+2],u+(i==1||i==2?1:0)*.125,v+(i<2?1:0)*.125);t.draw();}
    private static void box(double x,double y,double z,double X,double Y,double Z,int m){quad(new double[]{x,y,z,X,y,z,X,y,Z,x,y,Z},m,.55f);quad(new double[]{x,Y,Z,X,Y,Z,X,Y,z,x,Y,z},m,1);quad(new double[]{X,y,z,x,y,z,x,Y,z,X,Y,z},m,.75f);quad(new double[]{x,y,Z,X,y,Z,X,Y,Z,x,Y,Z},m,.9f);quad(new double[]{x,y,z,x,y,Z,x,Y,Z,x,Y,z},m,.7f);quad(new double[]{X,y,Z,X,y,z,X,Y,z,X,Y,Z},m,.85f);}
    public static void girder(double x,double y,double z,double X,double Y,double Z){box(x,y,z,X,Y,Z,5);}
    private static void ring(double x,double z,double y,double h,double r,double R,int m){int segments=32;int steps=Math.max(1,(int)Math.ceil(h/2));for(int j=0;j<steps;j++){double t=(double)j/steps,T=(double)(j+1)/steps,aR=r+(R-r)*t,bR=r+(R-r)*T;for(int i=0;i<segments;i++){double a=i*Math.PI*2/segments,b=(i+1)*Math.PI*2/segments;quad(new double[]{x+Math.cos(b)*aR,y+h*t,z+Math.sin(b)*aR,x+Math.cos(a)*aR,y+h*t,z+Math.sin(a)*aR,x+Math.cos(a)*bR,y+h*T,z+Math.sin(a)*bR,x+Math.cos(b)*bR,y+h*T,z+Math.sin(b)*bR},m,(float)(.78+.18*Math.cos((a+b)/2-.7)));}}}
    private static void fuselage(double y,double h,double rx,double rz,double RX,double RZ,int mat){int n=16;int steps=Math.max(1,(int)Math.ceil(h/2));for(int j=0;j<steps;j++){double t=(double)j/steps,T=(double)(j+1)/steps;for(int i=0;i<n;i++){double a=i*Math.PI*2/n,b=(i+1)*Math.PI*2/n;int m=mat==0&&Math.sin((a+b)/2)<-.25?1:mat;quad(new double[]{Math.cos(b)*(rx+(RX-rx)*t),y+h*t,7+Math.sin(b)*(rz+(RZ-rz)*t),Math.cos(a)*(rx+(RX-rx)*t),y+h*t,7+Math.sin(a)*(rz+(RZ-rz)*t),Math.cos(a)*(rx+(RX-rx)*T),y+h*T,7+Math.sin(a)*(rz+(RZ-rz)*T),Math.cos(b)*(rx+(RX-rx)*T),y+h*T,7+Math.sin(b)*(rz+(RZ-rz)*T)},m,(float)(.8+.16*Math.cos((a+b)/2)));}}}
    private static void wing(double side){double[] q={2.4*side,12.1,6.4,11.9*side,12.8,6.4,10.6*side,18.2,6.4,2.4*side,33,6.4};quad(q,1,.7f);double[] upper=q.clone();for(int i=2;i<12;i+=3)upper[i]+=.35;quad(new double[]{upper[9],upper[10],upper[11],upper[6],upper[7],upper[8],upper[3],upper[4],upper[5],upper[0],upper[1],upper[2]},0,.95f);for(int i=0;i<4;i++){int j=(i+1)%4;quad(new double[]{q[i*3],q[i*3+1],q[i*3+2],q[j*3],q[j*3+1],q[j*3+2],upper[j*3],upper[j*3+1],upper[j*3+2],upper[i*3],upper[i*3+1],upper[i*3+2]},1,.8f);}}
    private static void tail(){for(double x:new double[]{-.22,.22})quad(new double[]{x,11.5,9,x,12.5,21.7,x,17.4,21.7,x,24,9},0,x<0?.7f:.9f);quad(new double[]{-.22,12.5,21.7,.22,12.5,21.7,.22,17.4,21.7,-.22,17.4,21.7},1,.7f);quad(new double[]{-.22,17.4,21.7,.22,17.4,21.7,.22,24,9,-.22,24,9},1,.8f);}
    private static void bell(double x,double y,double z,double r,double h){ring(x,z,y,h,r,r*.42,4);GL11.glCullFace(GL11.GL_FRONT);ring(x,z,y+.02,h-.02,r*.92,r*.35,1);GL11.glCullFace(GL11.GL_BACK);}
    private static void build(){
        // External tank: length 46.9 m; diameter 8.4 m, nose ends at 56.1 m.
        ring(0,0,9.2,4,.7,4.2,2);ring(0,0,13.2,33.2,4.2,4.2,2);ring(0,0,46.4,6,4.2,2.7,2);ring(0,0,52.4,3.7,2.7,.015,2);
        for(double x:new double[]{-6.4,6.4}){bell(x,0,0,1.8,3);ring(x,0,3,38.4,1.85,1.85,0);ring(x,0,41.4,4,1.85,.015,0);for(double y:new double[]{5,13,23,33,40})ring(x,0,y,.25,1.89,1.89,3);box(Math.min(x,0),13,-.4,Math.max(x,0),13.5,.4,5);box(Math.min(x,0),36,-.4,Math.max(x,0),36.5,.4,5);}
        // Orbiter: length 37.2 m from engine exit to nose; white dorsal TPS / black belly.
        fuselage(12.8,5.2,2.7,2.6,2.7,2.6,0);fuselage(18,20,2.7,2.6,2.65,2.5,0);fuselage(38,5.2,2.65,2.5,1.65,1.65,0);fuselage(43.2,4,1.65,1.65,.015,.015,1);
        wing(-1);wing(1);tail();
        bell(-1.25,10,7,1.1,2.9);bell(1.25,10,7,1.1,2.9);bell(0,10,9.1,1.1,2.9);
        // Cockpit windows and payload-bay seam follow the orbiter dorsal face.
        for(double x:new double[]{-1.4,-.6,.2,1})box(x,40.7,8.95,x+.55,42.2,9.12,6);
        box(-.06,18,9.58,.06,37,9.69,3);box(-2.15,27,9.1,2.15,27.15,9.65,3);box(-2.15,18,9.1,2.15,18.15,9.65,3);
    }
    public static void draw(){if(list==0||!GL11.glIsList(list)){list=net.minecraft.client.renderer.GLAllocation.generateDisplayLists(1);GL11.glNewList(list,GL11.GL_COMPILE);build();GL11.glEndList();}GL11.glCallList(list);}
}
