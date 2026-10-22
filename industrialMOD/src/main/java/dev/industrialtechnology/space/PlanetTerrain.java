package dev.industrialtechnology.space;
/** Deterministic, bounded procedural terrain informed by LOLA/MOLA morphology, not a geographic map. */
public final class PlanetTerrain {
    private final long seed;private final boolean mars;
    public PlanetTerrain(long seed,boolean mars){this.seed=seed;this.mars=mars;}
    private double rand(int x,int z,int salt){long n=seed+x*341873128712L+z*132897987541L+salt*982451653L;n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return ((n^(n>>>31))>>>11)*0x1.0p-53;}
    private static double smooth(double v){return v*v*(3-2*v);}
    public double noise(double x,double z,int scale,int salt){double xx=x/scale,zz=z/scale;int a=(int)Math.floor(xx),b=(int)Math.floor(zz);double u=smooth(xx-a),v=smooth(zz-b);return (rand(a,b,salt)*(1-u)+rand(a+1,b,salt)*u)*(1-v)+(rand(a,b+1,salt)*(1-u)+rand(a+1,b+1,salt)*u)*v;}
    private double craters(int x,int z,int cell,int salt){int cx=Math.floorDiv(x,cell),cz=Math.floorDiv(z,cell);double change=0;
        for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++){int gx=cx+a,gz=cz+b;if(rand(gx,gz,salt)<.25)continue;double px=(gx+rand(gx,gz,salt+1))*cell,pz=(gz+rand(gx,gz,salt+2))*cell,r=cell*(.12+rand(gx,gz,salt+3)*.28),d=Math.hypot(x-px,z-pz)/r;if(d<1.55){double depth=r*(mars?.16:.24);if(d<1)change-=depth*(1-d*d);else change+=depth*.34*Math.exp(-Math.pow((d-1.06)*7,2));}}
        return change;
    }
    public int height(int x,int z){double low=noise(x,z,512,1),fine=(noise(x,z,24,2)-.5)*3+(noise(x,z,80,3)-.5)*8;double h;
        if(!mars){double high=Math.max(0,(low-.43)*2);h=68+high*42+fine+craters(x,z,192,10)+craters(x,z,48,20);if(high>.18)h+=Math.abs(noise(x,z,100,41)-.5)*18;}
        else{h=77+(low-.5)*38+fine+craters(x,z,256,10);double volcano=Math.hypot(x-8192,z-8192);if(volcano<2200)h+=60*Math.pow(1-volcano/2200,1.3)-18*Math.exp(-volcano*volcano/35000);
            double channel=z-(3000+Math.sin(x/650.0)*150+Math.sin(x/1700.0)*280);if(x>-12000&&x<16000)h-=32*Math.exp(-channel*channel/18000);
            double valley=z-(700+Math.sin(x/300.0)*65);h-=10*Math.exp(-valley*valley/900);if(low<.38)h+=Math.sin(x*.22+z*.08)*1.7;}
        return Math.max(20,Math.min(190,(int)Math.round(h+HeightReference.offset(mars,x,z))));
    }
    public boolean mare(int x,int z){return !mars&&noise(x,z,512,1)<.43;}
    public boolean boulder(int x,int z){return rand(x,z,91)>.998;}
    public boolean ice(int x,int z){return (mars?Math.abs(z)>20000:Math.abs(z)>18000)&&noise(x,z,128,77)>.48;}
}
