package dev.industrialtechnology.console;

/** One hour at five-second intervals, including explicit unavailable samples. */
public final class TrendSeries {
    public static final int LIMIT=720;
    public static final long GAP=-1;
    private final long[] times=new long[LIMIT],values=new long[LIMIT];
    private int start,size;
    public int size(){return size;}
    public long time(int i){return times[(start+i)%LIMIT];}
    public long value(int i){return values[(start+i)%LIMIT];}
    public void clear(){start=size=0;}
    public void add(long tick,long value){
        if(tick<0||value< -1)return;
        if(size>0&&tick<=time(size-1))return;
        int index;if(size<LIMIT){index=(start+size)%LIMIT;size++;}else{index=start;start=(start+1)%LIMIT;}
        times[index]=tick;values[index]=value;
    }
    public double ratePerSecond(){
        if(size<2||value(size-1)<0)return Double.NaN;
        int last=size-1,first=last;
        while(first>0&&last-first<6&&value(first-1)>=0&&time(first)-time(first-1)<=140)first--;
        long elapsed=time(last)-time(first);if(elapsed<=0)return Double.NaN;
        return (value(last)-value(first))*20.0/elapsed;
    }
    public long[] times(){long[] copy=new long[size];for(int i=0;i<size;i++)copy[i]=time(i);return copy;}
    public long[] values(){long[] copy=new long[size];for(int i=0;i<size;i++)copy[i]=value(i);return copy;}
    public void restore(long[] t,long[] v){clear();if(t==null||v==null)return;for(int i=Math.max(0,Math.min(t.length,v.length)-LIMIT);i<Math.min(t.length,v.length);i++)add(t[i],v[i]);}
}
