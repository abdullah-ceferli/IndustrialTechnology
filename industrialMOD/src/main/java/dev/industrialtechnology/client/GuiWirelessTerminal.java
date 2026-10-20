package dev.industrialtechnology.client;
import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import dev.industrialtechnology.wireless.*;
import dev.industrialtechnology.core.IndustrialTechnology;
/** Compact industrial GUI. Server snapshots are read-only; only the player's own inventory has real Slots. */
public final class GuiWirelessTerminal extends GuiTooltips {
    public final TerminalContainer terminal;
    public NBTTagCompound state=new NBTTagCompound();
    private GuiTextField name,quantity;
    public MultilineCodeEditor editor;public GuiAutomationGuide guide;
    private String editorDevice="",pendingDevice="",pendingText="";
    private final Map<String,String> drafts=new HashMap<String,String>();private final Set<String> dirtyDrafts=new HashSet<String>();
    private boolean code(){return terminal.tab==2&&selected()&&state.getBoolean("CodeMode");}
    private void keepDraft(){if(editor!=null&&!editorDevice.isEmpty()){drafts.put(editorDevice,editor.model.text());if(editor.dirty)dirtyDrafts.add(editorDevice);else dirtyDrafts.remove(editorDevice);}}
    public void receiveScript(java.util.UUID device,int revision,String text){String key=device.toString();
        boolean accepted=key.equals(pendingDevice)&&text.equals(pendingText);
        if(!dirtyDrafts.contains(key)||accepted){drafts.put(key,text);dirtyDrafts.remove(key);if(key.equals(editorDevice)&&editor!=null)editor.set(text);}
        if(accepted){pendingDevice=pendingText="";if(editor!=null&&key.equals(editorDevice))editor.dirty=false;}
    }
    private String editKey="";
    private int condition,targetPage,monitorMode,tankPage;
    private boolean ruleEnabled,dirtyRule,choosingTarget;
    private final List<MetalButton> controls=new ArrayList<MetalButton>();
    private static final String[] CONDITIONS={"Contains item","Item amount reached","Inventory full","Inventory empty","Item missing"};
    public GuiWirelessTerminal(TerminalContainer terminal){super(terminal);this.terminal=terminal;xSize=316;ySize=236;}
    @Override public void initGui(){
        super.initGui();org.lwjgl.input.Keyboard.enableRepeatEvents(true);buttonList.clear();controls.clear();
        if(editor==null)editor=new MultilineCodeEditor(mc);editor.bounds(guiLeft+122,guiTop+68,182,133);
        button(1,117,7,58,15,"Monitors","Read-only item and fluid monitoring");button(2,178,7,58,15,"Controls","Real Yellow Stick redstone relays");button(3,239,7,68,15,"Automation","Blue Stick inventory conditions");
        button(4,10,144,25,13,"<","Previous linked devices");button(5,43,144,25,13,">","Next linked devices");
        button(6,245,33,58,13,"Rename","Rename the selected device");button(7,10,181,58,13,"Unlink","Remove this registration; a relay drops its stick when its chunk is loaded");
        button(9,123,94,86,13,"Manual mode","Change output mode; changing mode turns the signal OFF");button(10,214,94,88,13,"5 seconds","Pulse duration: cycle from 5 through 10 seconds");
        button(11,123,116,87,13,"Trigger pulse","Trigger once; active pulses ignore repeated requests");
        button(12,123,75,18,12,"<","Previous rule");button(13,285,75,18,12,">","Next rule; up to eight rules per Blue Stick");
        button(14,123,91,180,13,CONDITIONS[0],"Cycle condition; item rules compare exact item, metadata, and NBT");
        button(15,123,131,72,13,"Disabled","Enable or disable this rule; click Apply to save");button(16,200,131,103,13,"Apply rule","Save the ghost item, quantity, condition, and linked Yellow target");
        button(17,213,111,90,14,"Target...","Choose a linked Yellow Stick; its own pulse settings apply");
        button(18,123,70,54,12,"Items","View accessible slots on the registered face");button(19,181,70,54,12,"Tanks","View each real tank separately");
        button(20,242,70,18,12,"<","Previous inventory/tank page");button(21,285,70,18,12,">","Next inventory/tank page");
        button(22,123,52,73,13,"SIMPLE","Use saved button-based rules");button(23,200,52,103,13,"CODE","Use the saved command program; SIMPLE rules stay saved");
        button(24,123,205,49,13,"Save","Validate and save this draft; invalid text never replaces a working script");
        button(25,177,205,42,13,"Help","Read the classic Automation Guide without losing your draft");
        button(26,224,205,79,13,"Enable","Enable or disable the last successfully saved program");
        name=new GuiTextField(fontRendererObj,guiLeft+123,guiTop+33,116,13);name.setMaxStringLength(32);name.setText(state.getString("Name"));
        quantity=new GuiTextField(fontRendererObj,guiLeft+151,guiTop+112,56,13);quantity.setMaxStringLength(10);quantity.setText(Integer.toString(Math.max(1,state.getInteger("Minimum"))));
        editKey="";receive(state);
    }
    private void button(int id,int x,int y,int w,int h,String label,String tooltip){MetalButton b=new MetalButton(id,guiLeft+x,guiTop+y,w,h,label,tooltip);controls.add(b);buttonList.add(b);}
    public void receive(NBTTagCompound n){
        keepDraft();boolean wasCode=code();state=n;terminal.tab=n.getInteger("Tab");terminal.page=n.getInteger("Page");terminal.ruleIndex=n.getInteger("Rule");
        terminal.selected=WirelessData.uuid(n.getString("Selected"));terminal.layout(code());
        String device=n.getString("Selected");if(editor!=null&&!device.equals(editorDevice)){editorDevice=device;editor.set(drafts.containsKey(device)?drafts.get(device):"");editor.dirty=dirtyDrafts.contains(device);}
        if(editor!=null){editor.model.limit=n.hasKey("MaxScriptLength")?n.getInteger("MaxScriptLength"):4096;editor.errorLine=n.getInteger("ScriptErrorLine");if(code()&&!wasCode)editor.focused=true;}
        String key=n.getString("Selected")+":"+n.getInteger("Rule");
        if(!key.equals(editKey)){
            editKey=key;dirtyRule=false;monitorMode=tankPage=0;choosingTarget=false;
            if(name!=null){name.setText(n.getString("Name"));name.setCursorPositionZero();}if(quantity!=null)quantity.setText(Integer.toString(Math.max(1,n.getInteger("Minimum"))));
        }
        if(!dirtyRule){condition=Math.max(0,Math.min(4,n.getInteger("Condition")));ruleEnabled=n.getBoolean("RuleEnabled");if(quantity!=null&&!quantity.isFocused())quantity.setText(Integer.toString(Math.max(1,n.getInteger("Minimum"))));}
        updateButtons();
    }
    private void send(int action,int a,int b,String text){IndustrialTechnology.network.sendToServer(new WirelessCommand(terminal.windowId,action,a,b,text));}
    private boolean selected(){return !state.getString("Selected").isEmpty();}
    private void updateButtons(){
        for(MetalButton b:controls){b.visible=true;b.enabled=true;b.selected=false;
            if(b.id<=3){b.selected=b.id-1==terminal.tab;continue;}
            if(b.id==4){b.enabled=state.getInteger("Page")>0;continue;}if(b.id==5){b.enabled=state.getInteger("Page")+1<state.getInteger("Pages");continue;}
            b.visible=selected();if(b.id==6||b.id==7)continue;
            if(b.id==9||b.id==10||b.id==11){b.visible=selected()&&terminal.tab==1;
                if("OTHER_DIMENSION".equals(state.getString("Status")))b.enabled=false;
                if(b.id==9)b.displayString=state.getBoolean("PulseMode")?"Pulse mode":"Manual mode";
                if(b.id==10)b.displayString=state.getInteger("Seconds")+" seconds";
                if(b.id==11)b.enabled="ONLINE".equals(state.getString("Status"))&&state.getBoolean("PulseMode")&&!state.getBoolean("Signal");continue;}
            if(b.id>=12&&b.id<=17){b.visible=selected()&&terminal.tab==2&&!code();
                if(b.id==12)b.enabled=state.getInteger("Rule")>0;if(b.id==13)b.enabled=state.getInteger("Rule")<7;
                if(b.id==14)b.displayString=CONDITIONS[condition];if(b.id==15){b.displayString=ruleEnabled?"Enabled":"Disabled";b.selected=ruleEnabled;}
                if(b.id==17)b.displayString=shorten(state.getString("TargetName"),78);
                if(b.id==16)b.enabled=state.getTagList("Targets",10).tagCount()>0;continue;}
            if(b.id==22||b.id==23){b.visible=selected()&&terminal.tab==2;b.selected=b.id==23?code():!code();continue;}
            if(b.id>=24){b.visible=code();if(b.id==26){b.displayString=state.getBoolean("ScriptEnabled")?"Disable":"Enable";b.selected=state.getBoolean("ScriptEnabled");}continue;}
            if(b.id>=18){b.visible=selected()&&terminal.tab==0;
                if(b.id==18)b.selected=monitorMode==0;if(b.id==19)b.selected=monitorMode==1;
                if(b.id==20)b.enabled=(monitorMode==0?state.getInteger("DetailPage"):tankPage)>0;
                if(b.id==21)b.enabled=monitorMode==0?state.getInteger("DetailPage")+1<state.getInteger("InventoryPages"):(tankPage+1)*4<state.getTagList("Tanks",10).tagCount();
            }
        }
    }
    @Override protected void actionPerformed(GuiButton b){
        if(!b.enabled||choosingTarget)return;
        switch(b.id){
            case 1:case 2:case 3:dirtyRule=false;send(WirelessCommand.TAB,b.id-1,0,"");break;
            case 4:case 5:send(WirelessCommand.PAGE,Math.max(0,state.getInteger("Page")+(b.id==5?1:-1)),0,"");break;
            case 6:send(WirelessCommand.RENAME,0,0,name.getText());name.setFocused(false);break;
            case 7:send(WirelessCommand.UNLINK,0,0,"");break;
            case 9:send(WirelessCommand.MODE,0,0,"");break;
            case 10:send(WirelessCommand.DURATION,state.getInteger("Seconds")>=10?5:state.getInteger("Seconds")+1,0,"");break;
            case 11:send(WirelessCommand.TRIGGER,0,0,"");break;
            case 12:case 13:dirtyRule=false;send(WirelessCommand.RULE,state.getInteger("Rule")+(b.id==13?1:-1),0,"");break;
            case 14:condition=(condition+1)%5;dirtyRule=true;break;
            case 15:ruleEnabled=!ruleEnabled;dirtyRule=true;break;
            case 16:
                try{int minimum=Integer.parseInt(quantity.getText());if(minimum<1||minimum>1000000000)return;send(WirelessCommand.SAVE_RULE,condition,minimum,ruleEnabled?"1":"0");dirtyRule=false;}catch(NumberFormatException ignored){}break;
            case 22:case 23:keepDraft();send(WirelessCommand.CODE_MODE,b.id==23?1:0,0,"");break;
            case 24:keepDraft();pendingDevice=state.getString("Selected");pendingText=editor.model.text();IndustrialTechnology.network.sendToServer(new dev.industrialtechnology.automation.ScriptUpload(terminal.windowId,java.util.UUID.fromString(pendingDevice),pendingText));break;
            case 25:keepDraft();guide=new GuiAutomationGuide(()->{guide=null;editor.focused=true;});guide.setWorldAndResolution(mc,width,height);break;
            case 26:send(WirelessCommand.SCRIPT_ENABLE,0,0,"");break;
            case 17:targetPage=0;choosingTarget=true;break;
            case 18:monitorMode=0;break;case 19:monitorMode=1;break;
            case 20:case 21:if(monitorMode==0)send(WirelessCommand.DETAIL,Math.max(0,state.getInteger("DetailPage")+(b.id==21?1:-1)),0,"");else tankPage=Math.max(0,tankPage+(b.id==21?1:-1));break;
            default:break;
        }updateButtons();
    }
    @Override protected void mouseClicked(int mouseX,int mouseY,int button){
        if(guide!=null){guide.click(mouseX,mouseY,button);return;}
        if(code()&&editor.click(mouseX,mouseY,button))return;
        int x=mouseX-guiLeft,y=mouseY-guiTop;
        if(choosingTarget){
            NBTTagList targets=state.getTagList("Targets",10);
            if(button==0&&x>=125&&x<302&&y>=61&&y<121){int i=targetPage*5+(y-61)/12;if(i<targets.tagCount()){send(WirelessCommand.TARGET,0,0,targets.getCompoundTagAt(i).getString("Id"));choosingTarget=false;}}
            else if(button==0&&y>=124&&y<138){if(x>=125&&x<163)targetPage=Math.max(0,targetPage-1);else if(x>=263&&x<302)targetPage=Math.min(Math.max(0,(targets.tagCount()-1)/5),targetPage+1);}
            else choosingTarget=false;return;
        }
        if(button==0&&x>=10&&x<112&&y>=48&&y<132){int index=(y-48)/14;NBTTagList rows=state.getTagList("Devices",10);
            if(index<rows.tagCount()){NBTTagCompound row=rows.getCompoundTagAt(index);send(WirelessCommand.SELECT,0,0,row.getString("Id"));
                if(terminal.tab==1&&x>=81&&"ONLINE".equals(row.getString("Status")))send(WirelessCommand.TOGGLE,0,0,"");}return;}
        if(button==0&&terminal.tab==1&&selected()&&x>=250&&x<303&&y>=116&&y<130){send(WirelessCommand.TOGGLE,0,0,"");return;}
        if(selected())name.mouseClicked(mouseX,mouseY,button);if(terminal.tab==2&&selected()&&!code())quantity.mouseClicked(mouseX,mouseY,button);
        super.mouseClicked(mouseX,mouseY,button);
    }
    @Override protected void keyTyped(char c,int key){
        if(guide!=null){guide.key(c,key);return;}
        if(code()&&editor.key(c,key)){keepDraft();return;}
        if(choosingTarget){if(key==1)choosingTarget=false;return;}
        if(selected()&&name.textboxKeyTyped(c,key))return;
        if(terminal.tab==2&&selected()&&!code()&&quantity.textboxKeyTyped(c,key)){dirtyRule=true;return;}
        super.keyTyped(c,key);
    }
    @Override protected void mouseClickMove(int x,int y,int button,long duration){if(guide!=null)return;if(code()&&editor.dragging){editor.drag(x,y);return;}super.mouseClickMove(x,y,button,duration);}
    @Override protected void mouseMovedOrUp(int x,int y,int button){if(editor!=null)editor.dragging=false;if(guide==null)super.mouseMovedOrUp(x,y,button);}
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(guide==null&&code()&&wheel!=0)editor.scroll(wheel>0?-3:3);}
    @Override public void onGuiClosed(){keepDraft();org.lwjgl.input.Keyboard.enableRepeatEvents(false);super.onGuiClosed();}
    @Override public void updateScreen(){super.updateScreen();if(editor!=null)editor.tick();if(name!=null)name.updateCursorCounter();if(quantity!=null)quantity.updateCursorCounter();updateButtons();}
    @Override public void drawScreen(int x,int y,float partial){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
        try{GL11.glColor4f(1,1,1,1);if(guide!=null)guide.drawScreen(x,y,partial);else super.drawScreen(x,y,partial);}finally{GL11.glPopClientAttrib();GL11.glPopAttrib();}
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(new ResourceLocation("industrialtech","textures/gui/wireless_terminal.png"));
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.addVertexWithUV(guiLeft,guiTop+ySize,0,0,ySize/256.0);t.addVertexWithUV(guiLeft+xSize,guiTop+ySize,0,xSize/512.0,ySize/256.0);t.addVertexWithUV(guiLeft+xSize,guiTop,0,xSize/512.0,0);t.addVertexWithUV(guiLeft,guiTop,0,0,0);t.draw();
        int[] accents={0xff70b37a,0xffd5b35d,0xff69a3d4};for(int i=0;i<3;i++)drawRect(guiLeft+117+i*61,guiTop+25,guiLeft+175+i*61,guiTop+26,accents[i]);
        if(code()){drawRect(guiLeft+76,guiTop+149,guiLeft+241,guiTop+226,0xff3b4449);drawRect(guiLeft+119,guiTop+29,guiLeft+307,guiTop+232,0xff262f35);editor.draw();}
        if(selected()){name.drawTextBox();if(terminal.tab==2&&!code()){quantity.drawTextBox();drawRect(guiLeft+127,guiTop+110,guiLeft+145,guiTop+128,0xff141c22);drawRect(guiLeft+128,guiTop+111,guiLeft+144,guiTop+127,0xff3a4750);}}
    }
    @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
        text("Wireless Terminal",10,10,0xe3d7ba);text("LINKED: "+state.getInteger("Total"),10,35,0x929c9c);if(!code())text("Inventory",78,141,0xa1aaa9);
        NBTTagList rows=state.getTagList("Devices",10);int x=mx-guiLeft,y=my-guiTop;
        for(int i=0;i<rows.tagCount();i++){
            NBTTagCompound row=rows.getCompoundTagAt(i);int py=48+i*14;boolean active=row.getString("Id").equals(state.getString("Selected"));
            drawRect(9,py-1,113,py+12,active?0xff46585f:0xff2a343b);drawRect(10,py,12,py+10,statusColor(row.getString("Status"))|0xff000000);
            text(shorten(row.getString("Name"),terminal.tab==1?63:96),15,py+2,0xc9d0cd);
            if(terminal.tab==1)slider(81,py,30,11,row.getBoolean("Signal"),"ONLINE".equals(row.getString("Status")));
        }
        text((state.getInteger("Page")+1)+" / "+Math.max(1,state.getInteger("Pages")),15,162,0x9ca8ac);
        if(!state.getString("Error").isEmpty())text(shorten(state.getString("Error"),180),123,55,0xd6a16d);
        if(!selected()){text("No linked devices",123,49,0xc9d0cd);text("Carry this terminal.",123,70,0x9ca8ac);text("Right-click a machine",123,83,0x9ca8ac);text("with a colored stick.",123,96,0x9ca8ac);return;}
        if(terminal.tab!=2)text(shorten(net.minecraft.util.StatCollector.translateToLocal(state.getString("Machine")),180),123,49,0xb4bdbc);
        String status=state.getString("Status");String statusLabel=status.replace('_',' ');if(terminal.tab==0&&state.getBoolean("HasInventory"))statusLabel+="  "+state.getInteger("Occupied")+"/"+state.getInteger("Slots")+" slots";if(terminal.tab!=2)text(shorten(statusLabel,174),123,60,statusColor(status));
        if(!code()){text("Dim:"+state.getInteger("Dimension"),245,155,0x8e9a9d);text("X:"+state.getInteger("X"),245,167,0x8e9a9d);text("Y:"+state.getInteger("Y"),245,179,0x8e9a9d);text("Z:"+state.getInteger("Z"),245,191,0x8e9a9d);text(net.minecraftforge.common.util.ForgeDirection.getOrientation(state.getInteger("Side")).toString(),245,203,0x8e9a9d);}
        if(terminal.tab==0)drawMonitor(x,y);else if(terminal.tab==1){
            text("Redstone: "+(state.getBoolean("Signal")?"15":"0"),123,78,0xc9d0cd);
            if(state.getBoolean("Signal")&&state.getBoolean("TimedOutput"))text(String.format(java.util.Locale.ROOT,"%.1fs",state.getLong("Remaining")/20.0),249,78,0xd8c17d);
            slider(250,116,53,13,state.getBoolean("Signal"),"ONLINE".equals(status));
        }else if(code()){
            String error=state.getString("ScriptError");String label=!editor.notice.isEmpty()?editor.notice:!error.isEmpty()?error:editor.dirty?"Unsaved changes":state.getBoolean("ScriptEnabled")?state.getString("AutomationStatus"):"Script disabled";
            text(shorten(label,178),123,222,error.isEmpty()&&editor.notice.isEmpty()?0x9fb4b4:0xdd9a89);
            if(x>=122&&x<304&&y>=220&&y<232)tooltip(Arrays.asList(label,"Saved program: "+(state.getBoolean("ScriptEnabled")?"enabled":"disabled"),"Save validates; Enable runs the last valid saved code."),mx,my);
        }else{
            text("Rule "+(state.getInteger("Rule")+1)+" / 8",164,77,0x9bbbd3);text(state.getBoolean("Latched")?"TRUE":"FALSE",240,77,state.getBoolean("Latched")?0x8ec793:0x9ca8ac);

            if(x>=128&&x<145&&y>=111&&y<128&&terminal.ghost.getStackInSlot(0)==null)tooltip(Arrays.asList("Ghost item: left-click to copy your carried item", "Right-click to clear; no item is consumed", "Minimum quantity is the adjacent number field"),mx,my);
            if(x>=213&&x<303&&y>=111&&y<126)tooltip(Arrays.asList(state.getString("TargetName"),"Uses the Yellow Stick's mode and "+state.getInteger("TargetSeconds")+"-second pulse"),mx,my);
        }
        if(choosingTarget){drawTargetPicker();return;}
        if(x>=10&&x<112&&y>=48&&y<132){int i=(y-48)/14;if(i<rows.tagCount()){NBTTagCompound row=rows.getCompoundTagAt(i);tooltip(Arrays.asList(row.getString("Name"),row.getString("Status"),"id:"+row.getString("Id")),mx,my);}}
        else for(MetalButton b:controls)if(b.hovered(mx,my))tooltip(Arrays.asList(b.tooltip),mx,my);
    }
    private void drawMonitor(int x,int y){
        text(Integer.toString((monitorMode==0?state.getInteger("DetailPage"):tankPage)+1),266,72,0xb4bdbc);
        if(!"ONLINE".equals(state.getString("Status")))return;
        if(monitorMode==0){
            NBTTagList items=state.getTagList("Items",10);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{RenderHelper.enableGUIStandardItemLighting();
                for(int i=0;i<items.tagCount();i++){int px=128+i%9*18,py=86+i/9*18;drawRect(px-1,py-1,px+17,py+17,0xff141c22);drawRect(px,py,px+16,py+16,0xff36434a);ItemStack icon=ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i).getCompoundTag("Icon"));if(icon!=null){GL11.glColor4f(1,1,1,1);itemRender.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),icon,px,py);itemRender.renderItemOverlayIntoGUI(fontRendererObj,mc.getTextureManager(),icon,px,py,compact(items.getCompoundTagAt(i).getInteger("Count")));}}
            }finally{GL11.glPopAttrib();}
            if(!state.getBoolean("HasInventory"))text("No inventory on this face",123,91,0x9ca8ac);
            if(x>=128&&x<290&&y>=86&&y<140){int i=(y-86)/18*9+(x-128)/18;if(i<items.tagCount()){NBTTagCompound item=items.getCompoundTagAt(i);tooltip(Arrays.asList(item.hasKey("Icon")?item.getString("Name")+" x "+item.getInteger("Count"):"Empty slot", "Slot "+item.getInteger("Slot")+" | "+(item.getBoolean("Input")?"Input ":"")+(item.getBoolean("Output")?"Output":""), "Read-only: remote item transfer is unavailable"),x+guiLeft,y+guiTop);}}
        }else{
            NBTTagList tanks=state.getTagList("Tanks",10);if(tanks.tagCount()==0)text("No supported tanks",123,91,0x9ca8ac);
            for(int i=tankPage*4;i<Math.min(tanks.tagCount(),tankPage*4+4);i++){
                NBTTagCompound tank=tanks.getCompoundTagAt(i);int py=86+(i%4)*14;long amount=tank.getLong("Amount"),capacity=tank.getLong("Capacity");
                text(shorten(tankName(tank),86),123,py,0xc3cdca);text(shorten(compact(amount)+"/"+compact(capacity)+" "+tank.getString("Unit"),90),215,py,0x9db4bf);
                drawRect(123,py+9,303,py+12,0xff151e24);int bar=(int)Math.min(180,180.0*amount/Math.max(1,capacity));drawRect(123,py+9,123+bar,py+12,0xff000000|(tank.getInteger("Color")&0xffffff));
                if(x>=123&&x<303&&y>=py&&y<py+14)tooltip(Arrays.asList(tankName(tank),"Fluid ID: "+(tank.getString("FluidId").isEmpty()?"empty":tank.getString("FluidId")),"Tank "+(i+1)+" in exposed set",amount+" / "+capacity+" "+tank.getString("Unit"),String.format(java.util.Locale.ROOT,"%.1f%%",100.0*amount/Math.max(1,capacity))),x+guiLeft,y+guiTop);
            }
        }
    }
    private static String tankName(NBTTagCompound n){String key=n.getString("Key");if(!key.isEmpty()){String translated=net.minecraft.util.StatCollector.translateToLocal(key);if(!translated.equals(key))return translated;}return n.getString("Name");}
    private void drawTargetPicker(){
        drawRect(120,45,307,140,0xff151e25);drawRect(122,47,305,138,0xff34434f);text("LINKED YELLOW STICKS",127,50,0xe0bf71);
        NBTTagList list=state.getTagList("Targets",10);for(int i=targetPage*5;i<Math.min(list.tagCount(),targetPage*5+5);i++)text(shorten(list.getCompoundTagAt(i).getString("Name"),170),127,62+i%5*12,0xd3d9d5);
        text("< Prev",127,126,0xb5c9d4);text((targetPage+1)+" / "+Math.max(1,(list.tagCount()+4)/5),194,126,0xb5c9d4);text("Next >",265,126,0xb5c9d4);
    }
    private void slider(int x,int y,int width,int height,boolean on,boolean online){
        drawRect(x,y,x+width,y+height,0xff10181c);drawRect(x+1,y+1,x+width-1,y+height-1,!online?0xff4b565d:on?0xff39734a:0xff853f38);
        int knob=on?x+width-8:x+1;drawRect(knob,y+1,knob+7,y+height-1,0xffb9c4bc);
        text(online?(on?"ON":"OFF"):"--",on?x+2:x+10,y+(height-8)/2,0xe1e6dc);
    }
    private void text(String text,int x,int y,int color){fontRendererObj.drawString(text,x,y,color);}
    private String shorten(String s,int pixels){return fontRendererObj.getStringWidth(s)<=pixels?s:fontRendererObj.trimStringToWidth(s,Math.max(6,pixels-6))+"~";}
    private static String compact(long count){if(count<1000)return Long.toString(count);return count<1000000?(count/1000)+"k":(count/1000000)+"M";}
    private static int statusColor(String status){return "ONLINE".equals(status)?0x8ec793:"OFFLINE".equals(status)||"OTHER_DIMENSION".equals(status)?0xb7a372:0xcd8e7a;}
    private void tooltip(List<String> lines,int mouseX,int mouseY){
        GL11.glPushMatrix();try{GL11.glTranslatef(-guiLeft,-guiTop,0);drawHoveringText(lines,mouseX,mouseY,fontRendererObj);}finally{GL11.glPopMatrix();}
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
