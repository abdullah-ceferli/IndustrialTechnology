package dev.industrialtechnology.space;
import java.util.*;
/** 1 block = 1 metre. NASA LC-39 hardstand/MLP/FSS principal dimensions. */
public final class LaunchFacilityPlan {
    public static final int MIN_X=-59,MAX_X=59,MIN_Z=-74,MAX_Z=24,MAX_Y=105,DECK=15,SHIP_X=-3,SHIP_Z=-25;
    public static final double SHUTTLE_HEIGHT=56.1,ORBITER_LENGTH=37.2,WINGSPAN=23.8;
    public static final class Cell {public final int x,y,z,kind;Cell(int x,int y,int z,int k){this.x=x;this.y=y;this.z=z;kind=k;}}
    public static final List<Cell> CELLS;
    private static final Map<Long,Cell> BUILD=new LinkedHashMap<Long,Cell>();
    private static long key(int x,int y,int z){return ((long)(x+128)<<24)|((long)(y+8)<<12)|(z+128);}
    private static void at(int x,int y,int z,int k){if(x==0&&y==0&&z==0)return;BUILD.put(key(x,y,z),new Cell(x,y,z,k));}
    private static void box(int x,int y,int z,int X,int Y,int Z,int kind){for(int a=x;a<=X;a++)for(int b=y;b<=Y;b++)for(int c=z;c<=Z;c++)at(a,b,c,kind);}
    static {
        // Concrete hardstand, 390 x 325 ft rounded to whole-metre voxels.
        box(MIN_X,-1,MIN_Z,MAX_X,-1,MAX_Z,0);
        // Six pedestal supports; hollow two-storey 49 x 41 x 8 m steel platform.
        for(int x:new int[]{-16,16})for(int z:new int[]{-42,-25,-8})box(x-1,0,z-1,x+1,6,z+1,1);
        for(int x=-20;x<=20;x++)for(int z=-49;z<=-1;z++)for(int y=7;y<=14;y++){
            boolean exhaust=(Math.abs(x-SHIP_X)<=2&&z>=-23&&z<=-14)||(Math.abs(x-(SHIP_X-6))<=2||Math.abs(x-(SHIP_X+6))<=2)&&z>=-28&&z<=-22;
            if(!exhaust&&(y==7||y==14||x==-20||x==20||z==-49||z==-1))at(x,y,z,y==14?2:1);
        }
        // Flame deflectors stay above the hardstand: no automatic excavation.
        for(int x=-11;x<=5;x++)for(int z=-28;z<=-15;z++)at(x,0,z,3);
        // Fixed service structure: 12.2 m square; 75 m steel + mast to 106 m.
        for(int y=0;y<=74;y++){for(int x:new int[]{25,36})for(int z:new int[]{-39,-28})at(x,y,z,4);
            if(y>=8&&(y-8)%6==0){for(int x=25;x<=36;x++)for(int z=-39;z<=-28;z++)if(x<28||x>33||z<-36||z>-31)at(x,y,z,2);}
            int r=y%12;at(25+r,y,-39,4);at(36-r,y,-28,4);at(25,y,-39+r,4);at(36,y,-28-r,4);
            at(26,y,-33,1);if(y<74)at(25,y,-33,6);
        }
        box(25,75,-39,36,75,-28,2);box(30,76,-34,31,80,-33,4);box(26,80,-34,40,81,-33,4);box(30,81,-34,30,105,-34,4);
        // Parked rotating-service framework and payload access enclosure.
        for(int x:new int[]{24,39})for(int z:new int[]{-14,8})box(x,0,z,x,47,z,4);
        for(int y:new int[]{9,21,33,45}){box(24,y,-14,39,y,8,2);for(int n=0;n<15;n++){at(24+n,y+n%12,-14,4);at(24+n,y+n%12,8,4);}}
        box(25,24,-13,38,44,-13,1);box(38,24,-13,38,44,6,1);box(25,44,-13,38,44,6,1);
        // External stair to the MLP deck, with rails outside the launch envelope.
        for(int step=0;step<15;step++){box(15,step,15-step,17,step,16-step,2);at(14,step+1,15-step,5);at(18,step+1,15-step,5);}
        List<Cell> result=new ArrayList<Cell>(BUILD.values());BUILD.clear();CELLS=Collections.unmodifiableList(result);
    }
    /** Orientation uses the same front metadata as all existing machines. */
    public static int dx(int x,int z,int facing){return facing==2?-x:facing==4?-z:facing==5?z:x;}
    public static int dz(int x,int z,int facing){return facing==2?-z:facing==4?x:facing==5?-x:z;}
    public static int kindAt(int x,int y,int z){for(Cell c:CELLS)if(c.x==x&&c.y==y&&c.z==z)return c.kind;return -1;}
}
