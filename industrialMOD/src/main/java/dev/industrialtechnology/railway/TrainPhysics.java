package dev.industrialtechnology.railway;
public final class TrainPhysics {
 public static final class Step {public final double speed,force,work;Step(double v,double f,double w){speed=v;force=f;work=w;}}
 public static Step tick(double speed,double mass,double traction,double throttle,double brake,double grade,double limit,boolean powered){mass=Math.max(1000,mass);double v=Math.max(0,speed*20);double force=powered?Math.max(0,traction)*Math.max(0,Math.min(1,throttle)):0;if(brake>=.999)force=0;double rolling=mass*9.81*.0018+.8*v*v,slope=mass*9.81*grade,braking=mass*1.4*Math.max(0,Math.min(1,brake));double governedDemand=Math.max(0,mass*Math.max(0,limit*20-v)/.05+rolling+slope);force=Math.min(force,governedDemand);double nv=Math.max(0,v+(force-rolling-slope-braking)/mass*.05);nv=Math.min(Math.max(0,limit)*20,nv);double d=(v+nv)*.025;return new Step(nv/20,force,Math.max(0,force*d));}
 private TrainPhysics(){}
}
