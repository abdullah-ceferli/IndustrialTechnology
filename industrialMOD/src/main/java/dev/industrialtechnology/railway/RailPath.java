package dev.industrialtechnology.railway;
/** Analytic metre-gauge-independent centerlines, sampled by distance rather than entity teleports. */
public final class RailPath {
 public final double x,y,z;public final int facing,shape,branch;public final double length;
 public RailPath(double x,double y,double z,int facing,int shape,int branch){this.x=x;this.y=y;this.z=z;this.facing=Math.floorMod(facing,4);this.shape=shape;this.branch=branch;length=curve()?Math.PI*8:shape==3?Math.sqrt(257):16;}
 public boolean curve(){return shape==1||shape==2||shape==4&&branch==1;}
 public double[] point(double s){s=Math.max(0,Math.min(length,s));double u,v,h=y,t=s/length;if(curve()){double a=t*Math.PI/2;u=16*Math.sin(a);v=(shape==1?-1:1)*16*(1-Math.cos(a));}else{u=16*t;v=0;if(shape==3)h+=t;}double a=facing*Math.PI/2;return new double[]{x+u*Math.cos(a)-v*Math.sin(a),h,z+u*Math.sin(a)+v*Math.cos(a)};}
 public double yaw(double s){double[] a=point(Math.max(0,s-.02)),b=point(Math.min(length,s+.02));return Math.toDegrees(Math.atan2(-(b[0]-a[0]),b[2]-a[2]));}
 public double grade(){return shape==3?.0625:0;}public double speedLimit(){return curve()?.45:shape==3?.55:dev.industrialtechnology.expansion.ExpansionConfig.speed;}
 public double nearest(double px,double py,double pz){double low=0,high=length;for(int i=0;i<28;i++){double a=low+(high-low)/3,b=high-(high-low)/3;if(distance(point(a),px,py,pz)<distance(point(b),px,py,pz))high=b;else low=a;}return(low+high)/2;}
 public static double distance(double[] p,double x,double y,double z){return (p[0]-x)*(p[0]-x)+(p[1]-y)*(p[1]-y)+(p[2]-z)*(p[2]-z);}
}
