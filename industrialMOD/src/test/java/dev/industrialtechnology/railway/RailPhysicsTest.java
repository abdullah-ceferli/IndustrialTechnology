package dev.industrialtechnology.railway;
import org.junit.Test;import static org.junit.Assert.*;
public class RailPhysicsTest {
 @Test public void gradualAcceleration(){TrainPhysics.Step s=TrainPhysics.tick(0,100000,300000,1,0,0,1,true);assertTrue(s.speed>0&&s.speed<.01);}
 @Test public void loadedTrainAcceleratesSlower(){assertTrue(TrainPhysics.tick(0,500000,300000,1,0,0,1,true).speed<TrainPhysics.tick(0,100000,300000,1,0,0,1,true).speed);}
 @Test public void noPowerCannotAccelerateOnLevelTrack(){assertEquals(0,TrainPhysics.tick(0,100000,300000,1,0,0,1,false).speed,0);}
 @Test public void brakesConsumeDistance(){double v=1,d=0;for(int i=0;i<1000&&v>0;i++){v=TrainPhysics.tick(v,100000,0,0,1,0,1,false).speed;d+=v;}assertEquals(0,v,0);assertTrue(d>50);}
 @Test public void gradeRequiresMoreTraction(){assertTrue(TrainPhysics.tick(.3,100000,300000,.8,0,.1,1,true).speed<TrainPhysics.tick(.3,100000,300000,.8,0,0,1,true).speed);}
 @Test public void stationaryIdleDoesNoWork(){assertEquals(0,TrainPhysics.tick(0,100000,300000,0,1,0,1,true).work,0);}
 @Test public void curvesHaveLimits(){TrainPhysics.Step s=TrainPhysics.tick(1,100000,300000,1,0,0,.45,true);assertTrue(s.speed<=.45);}
 @Test public void heavierCruiseConsumesMoreWork(){TrainPhysics.Step light=TrainPhysics.tick(1,100000,833000,1,0,0,1,true),heavy=TrainPhysics.tick(1,500000,833000,1,0,0,1,true);assertTrue(heavy.work>light.work*2);assertEquals(1,heavy.speed,1e-9);}
 @Test public void forceWorkIsDistanceBased(){TrainPhysics.Step a=TrainPhysics.tick(.1,100000,300000,.5,0,0,1,true),b=TrainPhysics.tick(.7,100000,300000,.5,0,0,1,true);assertTrue(b.work>a.work*4);}
}
