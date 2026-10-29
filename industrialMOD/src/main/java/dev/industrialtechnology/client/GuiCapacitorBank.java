package dev.industrialtechnology.client;

import dev.industrialtechnology.capacitor.*;
import dev.industrialtechnology.core.IndustrialTechnology;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;
import java.util.*;

/** Normal textured container: no TESR, no persistent OpenGL color or light changes. */
public final class GuiCapacitorBank extends GuiTooltips {
    private static final ResourceLocation PANEL=new ResourceLocation("industrialtech","textures/gui/capacitor.png");
    public final BankContainer bankContainer;
    public NBTTagCompound state=new NBTTagCompound();
    private GuiTextField input,output;private int tab,face=2;
    private final String[] faces={"Down","Up","North","South","West","East"};
    private final int[][] facePos={{76,138},{76,66},{76,102},{76,174},{38,102},{114,102}};
    private String feedback="";
    public GuiCapacitorBank(BankContainer c){super(c);bankContainer=c;xSize=256;ySize=236;}
    @Override public void initGui(){super.initGui();Keyboard.enableRepeatEvents(true);input=new GuiTextField(fontRendererObj,guiLeft+43,guiTop+172,139,14);output=new GuiTextField(fontRendererObj,guiLeft+43,guiTop+191,139,14);input.setMaxStringLength(19);output.setMaxStringLength(19);rebuild();fillFields();}
    private void button(int id,int x,int y,int w,int h,String label,String tip){buttonList.add(new MetalButton(id,guiLeft+x,guiTop+y,w,h,label,tip));}
    private void rebuild(){
        buttonList.clear();button(10,8,27,116,18,"ENERGY","Shared statistics and network transfer limits");button(11,130,27,118,18,"SIDE CONFIG","Configure the faces of this bank");
        ((MetalButton)buttonList.get(0)).selected=tab==0;((MetalButton)buttonList.get(1)).selected=tab==1;
        if(tab==0){button(0,187,171,29,16,"Set","Apply input RF/t limit");button(1,187,190,29,16,"Set","Apply output RF/t limit");button(4,219,171,29,16,"Max","Use all connected banks' input capacity");button(5,219,190,29,16,"Max","Use all connected banks' output capacity");button(2,8,212,240,19,redstoneLabel(),"Controls output; input continues. A signal at any connected bank counts.");}
        else{for(int i=0;i<6;i++)button(20+i,facePos[i][0],facePos[i][1],35,32,faces[i].substring(0,1),faces[i]+" face");button(3,160,110,87,22,mode().label,"Cycle Input / Output / Both / Disabled");}
    }
    private String redstoneLabel(){return new String[]{"Always active","Active with signal","Active without signal"}[Math.max(0,Math.min(2,state.getInteger("Redstone")))];}
    private SideMode mode(){byte[] b=state.getByteArray("Sides");return b.length==6?SideMode.of(b[face]):SideMode.BOTH;}
    public void receive(NBTTagCompound s){state=s;fillFields();rebuild();}
    private void fillFields(){if(input==null)return;if(!input.isFocused())input.setText(Long.toString(state.getLong("LimitIn")));if(!output.isFocused())output.setText(Long.toString(state.getLong("LimitOut")));}
    private void send(int action,long value,int side){IndustrialTechnology.network.sendToServer(new BankCommand(bankContainer.windowId,action,value,side));}
    @Override protected void actionPerformed(GuiButton b){if(b.id==10||b.id==11){tab=b.id-10;input.setFocused(false);output.setFocused(false);rebuild();}else if(b.id==0||b.id==1){GuiTextField f=b.id==0?input:output;try{long v=Long.parseLong(f.getText());if(v<0)throw new NumberFormatException();send(b.id,v,0);f.setFocused(false);feedback="";}catch(NumberFormatException bad){feedback="Enter a whole RF/t value (0 or more)";}}else if(b.id==2)send(2,0,0);else if(b.id==4||b.id==5){input.setFocused(false);output.setFocused(false);send(b.id-4,-1,0);}else if(b.id==3)send(3,(mode().ordinal()+1)%4,face);else if(b.id>=20&&b.id<26){face=b.id-20;rebuild();}}
    @Override protected void mouseClicked(int x,int y,int b){super.mouseClicked(x,y,b);if(tab==0){input.mouseClicked(x,y,b);output.mouseClicked(x,y,b);}}
    @Override protected void keyTyped(char c,int key){if(tab==0){if(key==Keyboard.KEY_TAB){boolean f=input.isFocused();input.setFocused(!f);output.setFocused(f);return;}if(key==Keyboard.KEY_RETURN||key==Keyboard.KEY_NUMPADENTER){if(input.isFocused())actionPerformed((GuiButton)buttonList.get(2));else if(output.isFocused())actionPerformed((GuiButton)buttonList.get(3));return;}if(input.isFocused()||output.isFocused()){if(key==Keyboard.KEY_ESCAPE){input.setFocused(false);output.setFocused(false);}else if(Character.isDigit(c)||Character.isISOControl(c)){input.textboxKeyTyped(c,key);output.textboxKeyTyped(c,key);}return;}}super.keyTyped(c,key);}
    @Override public void updateScreen(){super.updateScreen();input.updateCursorCounter();output.updateCursorCounter();}
    @Override public void onGuiClosed(){super.onGuiClosed();Keyboard.enableRepeatEvents(false);}
    public static String number(long n){return String.format(Locale.US,"%,d",Math.max(0,n));}
    private void text(String s,int x,int y,int color){fontRendererObj.drawString(s,x,y,color);}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(PANEL);drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);if(tab==0){input.drawTextBox();output.drawTextBox();}GL11.glColor4f(1,1,1,1);}
    @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
        int white=0xd0d4d2,muted=0x91a7ad,gold=0xe5bd75;long stored=state.getLong("Stored"),cap=state.getLong("Capacity");double charge=cap==0?0:100.0*stored/cap;
        text("ADVANCED CAPACITOR BANK",12,11,gold);
        if(tab==0){
            drawRect(12,55,34,170,0xff10171c);int fill=cap==0?0:(int)(109.0*stored/cap);drawRect(15,167-fill,31,167,(cap>0&&stored>=cap?0xffdcb347:0xff419aaf));drawRect(16,167-fill,19,167,(cap>0&&stored>=cap?0xffffdf7a:0xff71c7d6));for(int y=62;y<168;y+=12)drawRect(15,y,31,y+1,0xff262e32);
            text("Banks: "+state.getInteger("Banks")+"   "+String.format(Locale.US,"%.1f%%",charge),43,54,gold);
            text("Stored RF",43,66,muted);text(number(stored),43,78,cap>0&&stored>=cap?gold:white);text("/ "+number(cap),43,90,white);
            text("In:  "+number(state.getLong("In"))+" RF/t",43,105,SideMode.INPUT.color);text("Out: "+number(state.getLong("Out"))+" RF/t",43,117,SideMode.OUTPUT.color);
            text("Max in:  "+number(state.getLong("PhysicalIn"))+" RF/t",43,133,muted);text("Max out: "+number(state.getLong("PhysicalOut"))+" RF/t",43,145,muted);
            text(!feedback.isEmpty()?"Enter a whole RF/t value":state.getBoolean("Active")?"Output enabled":"Output paused by redstone",43,157,state.getBoolean("Active")?0x90bb98:gold);
            text("IN",12,175,SideMode.INPUT.color);text("OUT",12,194,SideMode.OUTPUT.color);
        }else{
            text("Faces of this bank",12,51,muted);byte[] b=state.getByteArray("Sides");for(int i=0;i<6;i++){int color=SideMode.of(b.length==6?b[i]:2).color;int x=facePos[i][0],y=facePos[i][1];drawRect(x+3,y+27,x+32,y+30,0xff000000|color);if(i==face){drawRect(x-1,y-1,x+36,y,0xffe5bd75);drawRect(x-1,y+32,x+36,y+33,0xffe5bd75);}}
            text(faces[face],161,82,gold);text("Face mode",161,98,muted);text("Front: "+faces[Math.max(0,Math.min(5,state.getInteger("Facing")))],12,209,muted);text("Touching banks connect in every mode.",12,223,muted);
        }
        RenderHelper.disableStandardItemLighting();GL11.glColor4f(1,1,1,1);
    }
    @Override public void drawScreen(int mx,int my,float partial){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
        try{GL11.glColor4f(1,1,1,1);drawContents(mx,my,partial);}finally{GL11.glPopClientAttrib();GL11.glPopAttrib();}
    }
    private void drawContents(int mx,int my,float partial){super.drawScreen(mx,my,partial);List<String> tips=null;
        if(tab==0&&mx>=guiLeft+12&&mx<guiLeft+34&&my>=guiTop+55&&my<guiTop+170)tips=Arrays.asList(number(state.getLong("Stored"))+" / "+number(state.getLong("Capacity"))+" RF","Shared by "+state.getInteger("Banks")+" loaded banks");
        for(Object o:buttonList)if(o instanceof MetalButton&&((MetalButton)o).hovered(mx,my)){tips=Arrays.asList(((MetalButton)o).tooltip);break;}
        if(tips!=null)drawHoveringText(tips,mx,my,fontRendererObj);RenderHelper.disableStandardItemLighting();GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
