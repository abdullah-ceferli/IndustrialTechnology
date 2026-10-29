package dev.industrialtechnology.console;
/** Stateful two-limit control. Refill and surplus-release both have a stable middle band. */
public final class TankBand {
    public static final long MAX=1000000000000L;
    public static boolean valid(long low,long high){return low>=0&&low<high&&high<=MAX;}
    public static boolean next(boolean refill,boolean previous,long value,long low,long high){
        if(!valid(low,high)||value<0)return false;
        if(value>=high)return !refill;
        if(value<=low)return refill;
        return previous;
    }
    private TankBand(){}
}
