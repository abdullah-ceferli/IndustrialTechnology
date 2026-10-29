package dev.industrialtechnology.steel;

/** Pure, bounded recipe parameters. Each encoded value represents one physical upgrade. */
public final class SteelUpgradeProfile {
    public static final SteelUpgradeProfile BASE=new SteelUpgradeProfile(100,500,4);
    public final int ticks,hePerTick,coal;
    public SteelUpgradeProfile(int ticks,int he,int coal){
        this.ticks=Math.max(5,Math.min(130,ticks));
        this.hePerTick=Math.max(50,Math.min(14000,he));
        this.coal=Math.max(1,Math.min(4,coal));
    }
    public static SteelUpgradeProfile calculate(int a,int b){
        int[] levels=new int[5];
        for(int code:new int[]{a,b}){int kind=code/4,tier=code%4;if(kind>=1&&kind<=4&&tier>=1&&tier<=3)levels[kind]=Math.min(3,levels[kind]+tier);}
        int speed=levels[1],power=levels[2],effect=levels[3],overdrive=levels[4];
        int ticks=(int)Math.ceil((100-25*speed+10*power)/(1.0+overdrive));
        int he=(500+500*speed-150*power+125*effect)*(1+2*overdrive);
        return new SteelUpgradeProfile(ticks,he,4-effect);
    }
}
