package dev.industrialtechnology.wireless;
import java.util.Locale;
/** Local metadata filtering; typing never sends a search request to the server. */
public final class DeviceSearch {
    public static boolean matches(String query,String name,String machine,int dimension,int x,int y,int z){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);if(q.isEmpty())return true;
        return (name+" "+machine+" dim "+dimension+" "+x+", "+y+", "+z).toLowerCase(Locale.ROOT).contains(q);
    }
    public static int pages(int count){return Math.max(1,(count+5)/6);}
    public static int page(int page,int count){return Math.max(0,Math.min(page,pages(count)-1));}
    private DeviceSearch(){}
}
