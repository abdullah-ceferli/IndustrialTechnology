package dev.industrialtechnology.railway;
public enum StockType {
 STEAM("Coal Steam Locomotive",24,100000,220000,.75,8),INDUSTRIAL("Industrial RF Electric",18,90000,300000,1,5.2),ACS64("ACS-64 Passenger Electric",20.32,97000,320000,1.6,4.95),AZ8A("AZ8A Two-section Freight Electric",32,200000,833000,1.05,10.5),FREIGHT("Freight Container Wagon",14,20000,0,1.6,4.6),PASSENGER("Passenger Coach",20,32000,0,1.6,6.5);
 public final String title;public final double length,mass,traction,speed,bogie;StockType(String n,double l,double m,double f,double s,double b){title=n;length=l;mass=m;traction=f;speed=s;bogie=b;}public boolean engine(){return ordinal()<4;}public boolean electric(){return ordinal()>0&&ordinal()<4;}public static StockType safe(int i){return values()[Math.max(0,Math.min(values().length-1,i))];}
}
