package dev.industrialtechnology.railway;
import org.junit.Test;import static org.junit.Assert.*;
public class RailPathTest {
 @Test public void straightExactlySixteenMetres(){RailPath p=new RailPath(0,0,0,0,0,0);assertArrayEquals(new double[]{16,0,0},p.point(16),1e-8);}
 @Test public void quarterCurveIsArcLengthParameterized(){RailPath p=new RailPath(0,0,0,0,2,0);assertEquals(8*Math.PI,p.length,1e-8);assertArrayEquals(new double[]{16,0,16},p.point(p.length),1e-8);double sum=0;double[] prev=p.point(0);for(double s=.01;s<p.length;s+=.01){double[] a=p.point(s);sum+=Math.sqrt(RailPath.distance(a,prev[0],prev[1],prev[2]));prev=a;}assertEquals(p.length,sum,.02);}
 @Test public void leftRightMirror(){RailPath a=new RailPath(0,0,0,0,1,0),b=new RailPath(0,0,0,0,2,0);assertEquals(-a.point(a.length)[2],b.point(b.length)[2],0);}
 @Test public void slopeRisesOneMetreOverSixteen(){RailPath p=new RailPath(0,0,0,1,3,0);assertArrayEquals(new double[]{0,1,16},p.point(p.length),1e-8);assertEquals(.0625,p.grade(),0);}
 @Test public void tangentTurnsNinetyDegreesContinuously(){RailPath p=new RailPath(0,0,0,0,2,0);assertEquals(90,p.yaw(p.length)-p.yaw(0),.1);}
 @Test public void nearestPointMatchesCurve(){RailPath p=new RailPath(20,10,-5,3,2,0);double[] a=p.point(12.345);assertEquals(12.345,p.nearest(a[0],a[1],a[2]),.001);}
 @Test public void switchBranchesShareStartAndSeparateEnds(){RailPath a=new RailPath(0,0,0,2,4,0),b=new RailPath(0,0,0,2,4,1);assertArrayEquals(a.point(0),b.point(0),0);assertTrue(RailPath.distance(a.point(a.length),b.point(b.length)[0],0,b.point(b.length)[2])>200);}
}
