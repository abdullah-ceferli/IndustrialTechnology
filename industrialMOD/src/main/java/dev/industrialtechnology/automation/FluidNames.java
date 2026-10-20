package dev.industrialtechnology.automation;
import java.lang.reflect.*;
import java.util.*;
import net.minecraftforge.fluids.FluidRegistry;
/** HBM names come from the inspected X5808 Fluids.getAll()/FluidType.getName(). */
public final class FluidNames {
    private static Map<String,String> hbm;
    private static synchronized Map<String,String> hbm(){
        if(hbm!=null)return hbm;Map<String,String> found=new LinkedHashMap<String,String>();
        try{Object all=Class.forName("com.hbm.inventory.fluid.Fluids").getMethod("getAll").invoke(null);
            for(int i=0;i<Array.getLength(all);i++){Object type=Array.get(all,i);if(type==null)continue;String name=String.valueOf(type.getClass().getMethod("getName").invoke(type));if(!name.equalsIgnoreCase("NONE"))found.put(name.toLowerCase(Locale.ROOT),"hbm:"+name);}
        }catch(ReflectiveOperationException|LinkageError|RuntimeException unavailable){}
        hbm=Collections.unmodifiableMap(found);return hbm;
    }
    public static String resolve(String id){
        if(id==null||id.length()>128)throw new IllegalArgumentException("Unknown fluid.");
        if(FluidRegistry.getFluid(id)!=null)return id;
        String raw=id.startsWith("hbm:")?id.substring(4):id;String value=hbm().get(raw.toLowerCase(Locale.ROOT));
        if(value!=null)return value;throw new IllegalArgumentException("Unknown fluid \""+id+"\". Hover a monitored tank for its ID.");
    }
    public static boolean matches(String requested,String actual){
        if(requested.equals(actual))return true;
        // Unqualified Forge/common names also match an HBM tank of that actual name.
        return requested.indexOf(':')<0&&actual.startsWith("hbm:")&&requested.equalsIgnoreCase(actual.substring(4));
    }
    private FluidNames(){}
}
