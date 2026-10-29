package dev.industrialtechnology.capacitor;
public enum SideMode {
    INPUT("Input",0x54c878,true,false),OUTPUT("Output",0xde6262,false,true),BOTH("Both",0xb39bc9,true,true),DISABLED("Disabled",0x727c7e,false,false);
    public final String label;public final int color;public final boolean input,output;
    SideMode(String label,int color,boolean input,boolean output){this.label=label;this.color=color;this.input=input;this.output=output;}
    public static SideMode of(int value){return value>=0&&value<values().length?values()[value]:DISABLED;}
}
