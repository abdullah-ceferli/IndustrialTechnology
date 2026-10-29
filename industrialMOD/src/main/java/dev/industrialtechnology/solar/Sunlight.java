package dev.industrialtechnology.solar;
/** Pure sunlight curve. Angle zero is dawn; noon is one quarter of a day. */
public final class Sunlight {
 public static double factor(long time,boolean sky,boolean hasSky,boolean wet,boolean storm,double rain,double thunder,double multiplier){if(!sky||!hasSky)return 0;double phase=Math.floorMod(time,24000L)/24000.0;double sun=Math.max(0,Math.sin(phase*Math.PI*2));return Math.max(0,Math.min(4,sun*(storm?thunder:wet?rain:1)*multiplier));}
 private Sunlight(){}
}
