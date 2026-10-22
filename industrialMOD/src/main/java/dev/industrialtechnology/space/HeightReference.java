package dev.industrialtechnology.space;
import java.io.File;import java.awt.image.BufferedImage;import javax.imageio.*;import javax.imageio.stream.ImageInputStream;import java.util.Iterator;import dev.industrialtechnology.core.IndustrialTechnology;
/** Optional preprocessed grayscale NASA reference. Read once, bounded before pixel allocation. */
public final class HeightReference {
    private static int[][] maps=new int[2][];private static int[] widths=new int[2],heights=new int[2];
    public static void load(File folder){for(int planet=0;planet<2;planet++){maps[planet]=null;File f=new File(folder,planet==0?"moon-height.png":"mars-height.png");if(!f.isFile())continue;try(ImageInputStream in=ImageIO.createImageInputStream(f)){
        Iterator<ImageReader> readers=ImageIO.getImageReadersByFormatName("png");if(!readers.hasNext())continue;ImageReader reader=readers.next();try{reader.setInput(in);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<2||h<2||w>512||h>512)throw new IllegalArgumentException("Reference must be 2..512 pixels per axis");BufferedImage image=reader.read(0);int[] pixels=new int[w*h];for(int y=0;y<h;y++)for(int x=0;x<w;x++){int rgb=image.getRGB(x,y);pixels[y*w+x]=(((rgb>>>16)&255)+((rgb>>>8)&255)+(rgb&255))/3;}widths[planet]=w;heights[planet]=h;maps[planet]=pixels;IndustrialTechnology.log.info("Loaded bounded optional space height reference {} ({}x{}); affects new chunks only",f,w,h);}finally{reader.dispose();}
    }catch(Exception e){IndustrialTechnology.log.warn("Ignoring invalid space height reference {}: {}",f,e.toString());}}}
    public static double offset(boolean mars,int x,int z){int p=mars?1:0;if(maps[p]==null)return 0;double u=Math.floorMod(x,65536)/65536.0,v=Math.max(0,Math.min(.999999,(z+32768)/65536.0));int xx=(int)(u*widths[p]),yy=(int)(v*heights[p]);return (maps[p][yy*widths[p]+xx]/255.0-.5)*(mars?48:32);}
}
