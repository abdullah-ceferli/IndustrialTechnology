package dev.industrialtechnology.energy;
import java.util.Arrays;
/** Last twenty completed game ticks. Simultaneous input and output stay independent. */
public final class FlowWindow {
    private final long[] times=new long[21],incoming=new long[21],outgoing=new long[21];
    private long start;
    public FlowWindow(long tick){start=tick;Arrays.fill(times,Long.MIN_VALUE);}
    public void add(long tick,long before,long after){
        if(before<0||after<0||before==after)return;
        int i=(int)Math.floorMod(tick,21);if(times[i]!=tick){times[i]=tick;incoming[i]=outgoing[i]=0;}
        if(after>before)incoming[i]=sum(incoming[i],after-before);else outgoing[i]=sum(outgoing[i],before-after);
    }
    public static long sum(long a,long b){return a>Long.MAX_VALUE-b?Long.MAX_VALUE:a+b;}
    public long[] read(long now){
        long in=0,out=0;for(int i=0;i<21;i++)if(times[i]>=Math.max(start,now-20)&&times[i]<now){in=sum(in,incoming[i]);out=sum(out,outgoing[i]);}
        long ticks=Math.max(0,Math.min(20,now-start));return new long[]{ticks==0?0:in/ticks,ticks==0?0:out/ticks,ticks};
    }
}
