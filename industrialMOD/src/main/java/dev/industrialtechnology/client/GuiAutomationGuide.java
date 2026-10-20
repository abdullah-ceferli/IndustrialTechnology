package dev.industrialtechnology.client;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import dev.industrialtechnology.automation.AutomationReference;
/** Classic Minecraft paper book, auto-paginated with the actual font at 116px x 117px. */
public final class GuiAutomationGuide extends GuiScreen {
    private static final ResourceLocation BOOK=new ResourceLocation("textures/gui/book.png");
    public static final class Page{public final List<String> lines;public final String example;Page(List<String> lines,String example){this.lines=lines;this.example=example;}}
    public final List<Page> pages=new ArrayList<Page>();public int page;private final Runnable close;
    public GuiAutomationGuide(Runnable close){this.close=close;}
    @Override public void initGui(){pages.clear();
        for(AutomationReference.Section section:AutomationReference.sections()){
            List<String> lines=fontRendererObj.listFormattedStringToWidth(section.title+"\n\n"+section.text,116);
            for(int i=0;i<lines.size();i+=13)pages.add(new Page(new ArrayList<String>(lines.subList(i,Math.min(lines.size(),i+13))),section.example));
        }
        page=Math.min(page,Math.max(0,pages.size()-1));buttonList.clear();int left=(width-192)/2,top=top();
        buttonList.add(new GuiButton(0,width/2-100,top+196,98,20,"Done"));buttonList.add(new GuiButton(3,width/2+2,top+196,98,20,"Copy example"));
        buttonList.add(new PageArrow(1,left+120,top+154,true));buttonList.add(new PageArrow(2,left+38,top+154,false));updateButtons();
    }
    private int top(){return Math.max(2,(height-220)/2);}
    private void updateButtons(){for(Object v:buttonList){GuiButton b=(GuiButton)v;if(b.id==1)b.visible=page+1<pages.size();if(b.id==2)b.visible=page>0;if(b.id==3)b.enabled=!pages.isEmpty()&&!pages.get(page).example.isEmpty();}}
    @Override protected void actionPerformed(GuiButton b){if(b.id==0){if(close!=null)close.run();else mc.displayGuiScreen(null);}else if(b.id==1&&page+1<pages.size())page++;else if(b.id==2&&page>0)page--;else if(b.id==3&&!pages.isEmpty())setClipboardString(pages.get(page).example);updateButtons();}
    @Override protected void keyTyped(char c,int key){if(key==1){if(close!=null)close.run();else mc.displayGuiScreen(null);}else if(key==203&&page>0){page--;updateButtons();}else if(key==205&&page+1<pages.size()){page++;updateButtons();}}
    public void click(int x,int y,int button){mouseClicked(x,y,button);}
    public void key(char c,int key){keyTyped(c,key);}
    @Override public void drawScreen(int mx,int my,float partial){GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{
        drawDefaultBackground();GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BOOK);int left=(width-192)/2,top=top();drawTexturedModalRect(left,top,0,0,192,192);
        String count="Page "+(page+1)+" of "+pages.size();fontRendererObj.drawString(count,left+148-fontRendererObj.getStringWidth(count),top+16,0);
        if(!pages.isEmpty())for(int i=0;i<pages.get(page).lines.size();i++)fontRendererObj.drawString(pages.get(page).lines.get(i),left+36,top+32+i*9,0x27211b);
        super.drawScreen(mx,my,partial);
    }finally{GL11.glPopAttrib();}}
    @Override public boolean doesGuiPauseGame(){return false;}
    private static final class PageArrow extends GuiButton {
        private final boolean forward;
        PageArrow(int id,int x,int y,boolean forward){super(id,x,y,23,13,"");this.forward=forward;}
        @Override public void drawButton(Minecraft mc,int mx,int my){if(!visible)return;GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BOOK);boolean hover=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;drawTexturedModalRect(xPosition,yPosition,hover?23:0,forward?192:205,23,13);}
    }
}
