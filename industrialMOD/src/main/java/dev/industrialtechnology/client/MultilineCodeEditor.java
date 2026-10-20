package dev.industrialtechnology.client;
import java.util.regex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
/** Real multiline widget; text is never delegated to a single-line GuiTextField. */
public final class MultilineCodeEditor extends Gui {
    public final CodeEditorModel model=new CodeEditorModel();
    public int x,y,width,height,scroll,errorLine;public boolean focused,dirty,dragging;public String notice="";
    private final Minecraft mc;private int horizontal,blink;
    private static final Pattern TOKENS=Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"|\\b(?:WHEN|EVERY|THEN|END|ITEM|FLUID|INVENTORY|TANK|EMPTY|FULL|PULSE|ON|OFF)\\b|\\b[0-9]+(?:mb|s|m|h)?\\b",Pattern.CASE_INSENSITIVE);
    public MultilineCodeEditor(Minecraft mc){this.mc=mc;}
    public void bounds(int x,int y,int width,int height){this.x=x;this.y=y;this.width=width;this.height=height;}
    public int visible(){return Math.max(1,(height-6)/10);}
    public void tick(){blink++;}
    public void set(String text){model.set(text);dirty=false;scroll=horizontal=0;notice="";}
    public void scroll(int delta){scroll=Math.max(0,Math.min(Math.max(0,model.lines().length-visible()),scroll+delta));}
    private void ensure(){int row=model.row();if(row<scroll)scroll=row;if(row>=scroll+visible())scroll=row-visible()+1;
        int pixels=mc.fontRenderer.getStringWidth(model.text().substring(model.start(row),model.cursor));int room=width-31;if(pixels-horizontal>room)horizontal=pixels-room;if(pixels<horizontal)horizontal=pixels;horizontal=Math.max(0,horizontal);}
    private void insert(String text){StringBuilder accepted=new StringBuilder();for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c=='\n'||c=='\r'||c=='\t'||ChatAllowedCharacters.isAllowedCharacter(c))accepted.append(c=='\t'?"  ":String.valueOf(c));}
        if(model.insert(accepted.toString())){dirty=true;notice="";}else notice="Script text limit reached";ensure();}
    public boolean key(char c,int key){if(!focused)return false;boolean shift=GuiScreen.isShiftKeyDown(),ctrl=GuiScreen.isCtrlKeyDown();
        if(ctrl){switch(key){case Keyboard.KEY_A:model.all();ensure();return true;case Keyboard.KEY_C:GuiScreen.setClipboardString(model.selected());return true;
            case Keyboard.KEY_X:GuiScreen.setClipboardString(model.selected());if(!model.selected().isEmpty()){model.insert("");dirty=true;}ensure();return true;
            case Keyboard.KEY_V:insert(GuiScreen.getClipboardString());return true;case Keyboard.KEY_Z:model.undo();dirty=true;ensure();return true;case Keyboard.KEY_Y:model.redo();dirty=true;ensure();return true;default:break;}}
        switch(key){case Keyboard.KEY_RETURN:case Keyboard.KEY_NUMPADENTER:insert("\n");return true;case Keyboard.KEY_TAB:insert("  ");return true;
            case Keyboard.KEY_BACK:model.back();dirty=true;break;case Keyboard.KEY_DELETE:model.delete();dirty=true;break;
            case Keyboard.KEY_LEFT:model.move(model.cursor-1,shift);break;case Keyboard.KEY_RIGHT:model.move(model.cursor+1,shift);break;
            case Keyboard.KEY_UP:model.vertical(-1,shift);break;case Keyboard.KEY_DOWN:model.vertical(1,shift);break;
            case Keyboard.KEY_PRIOR:model.vertical(-visible(),shift);break;case Keyboard.KEY_NEXT:model.vertical(visible(),shift);break;
            case Keyboard.KEY_HOME:model.move(ctrl?0:model.start(model.row()),shift);break;case Keyboard.KEY_END:model.move(ctrl?model.text().length():model.end(model.row()),shift);break;
            case Keyboard.KEY_ESCAPE:return false;
            default:if(ChatAllowedCharacters.isAllowedCharacter(c))insert(String.valueOf(c));return true;
        }ensure();return true;
    }
    public boolean click(int mx,int my,int button){focused=mx>=x&&mx<x+width&&my>=y&&my<y+height;if(!focused||button!=0)return focused;dragging=true;position(mx,my,GuiScreen.isShiftKeyDown());return true;}
    public void drag(int mx,int my){if(!dragging)return;if(my<y)scroll(-1);if(my>=y+height)scroll(1);position(mx,my,true);}
    private void position(int mx,int my,boolean select){String[] lines=model.lines();int row=Math.max(0,Math.min(lines.length-1,scroll+(my-y-3)/10)),px=Math.max(0,mx-x-25+horizontal),col=0;
        while(col<lines[row].length()){int a=mc.fontRenderer.getStringWidth(lines[row].substring(0,col)),b=mc.fontRenderer.getStringWidth(lines[row].substring(0,col+1));if(px<(a+b)/2)break;col++;}
        model.move(model.start(row)+col,select);blink=0;
    }
    private void clip(int cx,int cy,int w,int h){ScaledResolution s=new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);int scale=s.getScaleFactor();GL11.glScissor(cx*scale,mc.displayHeight-(cy+h)*scale,w*scale,h*scale);}
    public void draw(){
        drawRect(x,y,x+width,y+height,0xff10181e);drawRect(x,y,x+22,y+height,0xff1c272e);String[] lines=model.lines();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{GL11.glEnable(GL11.GL_SCISSOR_TEST);clip(x,y,width,height);
            for(int row=scroll;row<Math.min(lines.length,scroll+visible());row++){int py=y+3+(row-scroll)*10;if(errorLine==row+1)drawRect(x,py-1,x+width,py+9,0xff47292d);mc.fontRenderer.drawString(Integer.toString(row+1),x+3,py,0x778b95);}
            clip(x+23,y,width-24,height);int a=Math.min(model.cursor,model.anchor),b=Math.max(model.cursor,model.anchor);
            for(int row=scroll;row<Math.min(lines.length,scroll+visible());row++){int py=y+3+(row-scroll)*10,start=model.start(row),end=model.end(row),left=Math.max(a,start),right=Math.min(b,end);
                if(left<=right&&a!=b&&b>=start&&a<=end){int sx=mc.fontRenderer.getStringWidth(model.text().substring(start,left)),ex=mc.fontRenderer.getStringWidth(model.text().substring(start,right));if(b>end)ex+=4;drawRect(x+25+sx-horizontal,py-1,x+25+ex-horizontal,py+9,0xff3b566b);}
                String line=lines[row];Matcher m=TOKENS.matcher(line);int p=0,tx=x+25-horizontal;
                while(m.find()){String before=line.substring(p,m.start());mc.fontRenderer.drawString(before,tx,py,0xc5cccb);tx+=mc.fontRenderer.getStringWidth(before);String token=m.group();int color=token.startsWith("\"")?0xc9b878:Character.isDigit(token.charAt(0))?0x9fc4a6:0x83b4d3;mc.fontRenderer.drawString(token,tx,py,color);tx+=mc.fontRenderer.getStringWidth(token);p=m.end();}
                mc.fontRenderer.drawString(line.substring(p),tx,py,0xc5cccb);
            }
            if(focused&&blink/6%2==0&&model.row()>=scroll&&model.row()<scroll+visible()){int row=model.row(),cx=x+25-horizontal+mc.fontRenderer.getStringWidth(model.text().substring(model.start(row),model.cursor)),cy=y+3+(row-scroll)*10;drawRect(cx,cy-1,cx+1,cy+9,0xffe2ded0);}
        }finally{GL11.glPopAttrib();}
        if(lines.length>visible()){int size=Math.max(6,height*visible()/lines.length),offset=(height-size)*scroll/Math.max(1,lines.length-visible());drawRect(x+width-2,y+offset,x+width,y+offset+size,0xff6e8998);}
    }
}
