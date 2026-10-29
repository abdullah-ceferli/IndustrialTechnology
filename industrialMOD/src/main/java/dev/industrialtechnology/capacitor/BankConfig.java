package dev.industrialtechnology.capacitor;
import net.minecraftforge.common.config.Configuration;
public final class BankConfig {
    public static long capacity=25000000L;
    public static int input=25000,output=25000,visualInterval=10,guiInterval=10;
    public static void load(Configuration c){
        try{capacity=Math.max(1,Math.min(1000000000000L,Long.parseLong(c.get("capacitor","capacityPerBank","25000000","RF per bank, 1 to 1 trillion. Aggregate storage uses 64-bit arithmetic.").getString())));}catch(NumberFormatException bad){capacity=25000000L;}
        input=c.getInt("inputPerBank","capacitor",25000,1,1000000000,"RF/t added to the shared input budget by each bank.");
        output=c.getInt("outputPerBank","capacitor",25000,1,1000000000,"RF/t added to the shared output budget by each bank.");
        visualInterval=c.getInt("indicatorUpdateTicks","capacitor",10,5,100,"Minimum interval between quantized charge-indicator updates. No emitted-light updates.");
        guiInterval=c.getInt("guiUpdateTicks","capacitor",10,5,100,"Interval between changed GUI snapshots for open capacitor screens.");
    }
    private BankConfig(){}
}
