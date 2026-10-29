package dev.industrialtechnology.client;
import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.nbt.*;
import dev.industrialtechnology.console.*;
import dev.industrialtechnology.core.IndustrialTechnology;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;

public final class GuiConsole extends GuiTooltips {
    public final ConsoleContainer console;
    public NBTTagCompound state=new NBTTagCompound();
    private GuiTextField low,high;private int range=6000;
    public GuiConsole(ConsoleContainer c){super(c);console=c;xSize=360;ySize=236;}
    public static String compact(long value){double v=value;String[] suffix={"","k","M","G","T","P","E"};int i=0;while(Math.abs(v)>=1000&&i<suffix.length-1){v/=1000;i++;}return i==0?Long.toString(value):String.format(Locale.US,"%.2f%s",v,suffix[i]);}
    private String cut(String s,int pixels){while(fontRendererObj.getStringWidth(s)>pixels&&s.length()>1)s=s.substring(0,s.length()-1);return s;}
    private boolean control(){return console.tile.controller;}
    private NBTTagCompound row(){return state.getCompoundTag("Row");}
    public void receive(NBTTagCompound n){int old=state.getInteger("Selected");state=n;if(old!=n.getInteger("Selected")){if(low!=null)low.setFocused(false);if(high!=null)high.setFocused(false);}fill();rebuild();}
    @Override public void initGui(){super.initGui();Keyboard.enableRepeatEvents(true);low=new GuiTextField(fontRendererObj,guiLeft+140,guiTop+150,100,16);high=new GuiTextField(fontRendererObj,guiLeft+(control()?248:184),guiTop+(control()?150:115),control()?100:100,16);low.setMaxStringLength(13);high.setMaxStringLength(13);fill();rebuild();}
    private void fill(){if(low==null)return;if(!low.isFocused())low.setText(Long.toString(row().getLong("Low")));if(!high.isFocused())high.setText(Long.toString(row().getLong("High")));}
    private void button(int id,int x,int y,int w,int h,String label,String tip){buttonList.add(new MetalButton(id,guiLeft+x,guiTop+y,w,h,cut(label,w-8),tip));}
    private void rebuild(){
        buttonList.clear();int picker=state.getInteger("Picker");
        if(picker>0){NBTTagList choices=state.getTagList("Choices",10);for(int i=0;i<choices.tagCount();i++)button(200+i,140,48+i*20,208,18,choices.getCompoundTagAt(i).getString("Name"),choices.getCompoundTagAt(i).getString("Status"));button(40,140,196,44,18,"<","Previous sensor page");button(41,192,196,44,18,">","Next sensor page");button(42,244,196,104,18,"Back","Return without choosing");return;}
        NBTTagList rows=state.getTagList("Rows",10);for(int i=0;i<rows.tagCount();i++){NBTTagCompound r=rows.getCompoundTagAt(i);button(100+r.getInteger("Index"),8,48+i*18,118,17,(r.getInteger("Index")+1)+" "+r.getString("Name"),r.getString("Status"));}
        button(31,8,182,54,18,"<","Previous channel page");button(32,72,182,54,18,">","Next channel page");button(12,8,208,54,18,"Clear","Clear selected channel and turn its owned output OFF");if(control())button(13,72,208,54,18,"Pause all","Pause all controller rules and turn owned outputs OFF");
        button(2,140,45,208,18,state.getString("SourceName"),control()?"Choose a linked Blue sensor":"Choose a linked Green sensor or Yellow energy sensor");
        if(control()){
            button(3,140,67,208,18,state.getString("TargetName"),"Choose the Yellow redstone output");
            button(7,140,89,100,18,row().getBoolean("Energy")?"Energy":"Fluid","Choose the measured resource");button(6,248,89,100,18,"Tank "+(row().getInteger("Tank")+1),"Cycle the sensor's exposed tank");
            button(8,140,111,100,18,row().getBoolean("Refill")?"Refill":"Release surplus","Refill: ON at Low, OFF at High. Release: ON at High, OFF at Low.");
            int m=row().getInteger("Manual");button(9,248,111,100,18,m<0?"Automatic":m==1?"Force ON":"Force OFF","Cycle Automatic / Force ON / Force OFF. Unavailable data fails closed.");
            button(10,140,173,100,18,"Apply limits","Save Low and High limits");button(11,248,173,100,18,row().getBoolean("Enabled")?"Pause":"Enable","Enable the saved rule; pause turns its owned output OFF");
        }else{
            button(7,140,67,100,18,row().getBoolean("Energy")?"Energy":"Fluid","Switch fluid or energy monitoring; history starts a new series");button(6,248,67,100,18,"Tank "+(row().getInteger("Tank")+1),"Cycle tanks exposed by this sensor");button(10,292,114,56,18,"Set","Save target amount for the time estimate");
            button(50,140,209,64,18,"5 min","Show the last five minutes");button(51,212,209,64,18,"15 min","Show the last fifteen minutes");button(52,284,209,64,18,"1 hour","Show one hour of recorded history");
        }
    }
    private void send(int action,int value,long a,long b,String text){IndustrialTechnology.network.sendToServer(new ConsoleCommand(console.windowId,action,state.getInteger("Selected"),value,a,b,text));}
    @Override protected void actionPerformed(GuiButton b){if(b.id>=200){NBTTagList list=state.getTagList("Choices",10);int i=b.id-200;if(i<list.tagCount())send(5,0,0,0,list.getCompoundTagAt(i).getString("Id"));return;}if(b.id>=100){IndustrialTechnology.network.sendToServer(new ConsoleCommand(console.windowId,0,b.id-100,0,0,0,""));return;}if(b.id>=50){range=b.id==50?6000:b.id==51?18000:72000;return;}if(b.id==31||b.id==32){send(1,state.getInteger("Page")+(b.id==31?-1:1),0,0,"");return;}if(b.id==40||b.id==41){send(4,state.getInteger("PickerPage")+(b.id==40?-1:1),0,0,"");return;}if(b.id==42){send(14,0,0,0,"");return;}if(b.id==10){try{long l=control()?Long.parseLong(low.getText()):0,h=Long.parseLong(high.getText());send(10,0,l,h,"");low.setFocused(false);high.setFocused(false);}catch(NumberFormatException bad){state.setString("Notice","Enter whole positive numbers");}return;}send(b.id,b.id==6?1:b.id==9?row().getInteger("Manual")==1?-1:row().getInteger("Manual")+1:0,0,0,"");}
    @Override protected void mouseClicked(int x,int y,int b){super.mouseClicked(x,y,b);if(state.getInteger("Picker")==0){if(control())low.mouseClicked(x,y,b);high.mouseClicked(x,y,b);}}
    @Override protected void keyTyped(char c,int key){if(key==Keyboard.KEY_ESCAPE){super.keyTyped(c,key);return;}if(state.getInteger("Picker")==0&&((control()&&low.textboxKeyTyped(c,key))||high.textboxKeyTyped(c,key)))return;super.keyTyped(c,key);}
    @Override public void updateScreen(){super.updateScreen();if(low!=null){low.updateCursorCounter();high.updateCursorCounter();}}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);super.onGuiClosed();}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
        int x=guiLeft,y=guiTop;drawRect(x,y,x+xSize,y+ySize,0xff758386);drawRect(x+2,y+2,x+xSize-2,y+ySize-2,0xff242f35);drawRect(x+6,y+6,x+xSize-6,y+27,0xff111a20);drawRect(x+132,y+32,x+133,y+229,0xff607177);
        if(state.getInteger("Picker")==0){if(control())low.drawTextBox();high.drawTextBox();}GL11.glColor4f(1,1,1,1);
    }
    private void text(String s,int x,int y,int color){fontRendererObj.drawString(s,x,y,color);}
    @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
        text(control()?"REFINERY CONTROLLER":"TANK HISTORY MONITOR",10,12,0xe5cf98);text(state.getBoolean("Linked")?"Linked":"Terminal link needed",238,12,state.getBoolean("Linked")?0x88c99a:0xe0ac79);
        text("Channel "+(state.getInteger("Selected")+1)+" / 16",10,34,0xa8b7bc);
        if(state.getInteger("Picker")>0){text(state.getInteger("Picker")==2?"Choose Yellow output":"Choose linked sensor",140,34,0xa8cbd1);text((state.getInteger("PickerPage")+1)+" / "+state.getInteger("PickerPages"),267,220,0xa8b7bc);return;}
        String note=state.getString("Notice");if(!note.isEmpty())text(cut(note,208),140,33,0xe0ac79);
        NBTTagCompound r=row(),reading=r.getCompoundTag("Reading");
        if(control()){
            text("Low",140,138,0x9eb5bb);text("High",248,138,0x9eb5bb);text(cut(r.getString("Message"),208),140,198,r.getBoolean("Latched")?0x90d1a2:0xe0bf89);
            text(reading.getBoolean("Available")?compact(reading.getLong("Amount"))+" / "+compact(reading.getLong("Capacity"))+" "+reading.getString("Unit"):reading.getString("Status"),140,211,0xc6d8d8);text("Source/mode changes pause this rule.",140,224,0x91a5ad);
        }else{
            text(reading.getBoolean("Available")?compact(reading.getLong("Amount"))+" / "+compact(reading.getLong("Capacity"))+" "+reading.getString("Unit"):reading.getString("Status"),140,91,0xc6d8d8);
            double rate=r.getDouble("Rate");String speed=r.getBoolean("RateKnown")?String.format(Locale.US,"Net %+.1f %s/s",rate,reading.getString("Unit")):"Net: collecting samples";text(cut(speed,122),140,103,0x9bc3c5);
            String eta="--";if(reading.getBoolean("Available")&&r.getBoolean("RateKnown")&&Math.abs(rate)>.000001){double seconds=(r.getLong("High")-reading.getLong("Amount"))/rate;if(seconds>=0&&seconds<31536000)eta=seconds<60?String.format(Locale.US,"%.0fs",seconds):seconds<3600?String.format(Locale.US,"%.1fm",seconds/60):String.format(Locale.US,"%.1fh",seconds/3600);}
            text("ETA "+eta,278,103,0xb7bca9);text("Target",140,119,0x9eb5bb);graph(r,reading,state.getLong("Now"));
        }
    }
    private void graph(NBTTagCompound r,NBTTagCompound reading,long now){
        int[] th=r.getIntArray("Times"),vh=r.getIntArray("Values");int count=Math.min(th.length,vh.length)/2;long from=Math.max(0,now-range),max=Math.max(1,reading.getLong("Capacity"));for(int i=0;i<count;i++){long t=word(th,i),v=word(vh,i);if(t>=from&&v>max)max=v;}
        drawRect(140,137,348,203,0xff101b21);text(compact(max),141,138,0x78949d);text("0",141,193,0x78949d);for(int j=1;j<4;j++)drawRect(165,141+j*14,346,142+j*14,0xff24373e);
        GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glColor4f(.36f,.82f,.73f,1);boolean open=false;long previous=Long.MIN_VALUE;int visible=0;
        for(int i=0;i<count;i++){long time=word(th,i),value=word(vh,i);if(time<from||time>now||value<0||previous!=Long.MIN_VALUE&&time-previous>140){if(open){GL11.glEnd();open=false;}if(time<from||time>now||value<0){previous=time;continue;}}
            if(!open){GL11.glBegin(GL11.GL_LINE_STRIP);open=true;}double x=165+180.0*(time-from)/Math.max(1,now-from),y=199-54.0*Math.min(max,value)/max;GL11.glVertex2d(x,y);previous=time;visible++;
        }if(open)GL11.glEnd();GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glColor4f(1,1,1,1);if(visible<2)text("Collecting history...",191,164,0x829da5);
    }
    private static long word(int[] a,int i){return ((long)a[i*2]<<32)|(a[i*2+1]&0xffffffffL);}
    @Override public void drawScreen(int x,int y,float p){GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);try{GL11.glColor4f(1,1,1,1);super.drawScreen(x,y,p);for(Object o:buttonList)if(o instanceof MetalButton&&((MetalButton)o).hovered(x,y)){drawHoveringText(fontRendererObj.listFormattedStringToWidth(((MetalButton)o).tooltip,200),x,y,fontRendererObj);break;}if(!control()&&x>=guiLeft+140&&x<guiLeft+348&&y>=guiTop+137&&y<guiTop+203){NBTTagCompound r=row(),read=r.getCompoundTag("Reading");drawHoveringText(Arrays.asList("Stored "+read.getLong("Amount")+" / "+read.getLong("Capacity")+" "+read.getString("Unit"),"Net rate is change in stored amount", "Gaps mean unavailable data; server ticks measure time"),x,y,fontRendererObj);}}finally{GL11.glPopClientAttrib();GL11.glPopAttrib();}}
}
