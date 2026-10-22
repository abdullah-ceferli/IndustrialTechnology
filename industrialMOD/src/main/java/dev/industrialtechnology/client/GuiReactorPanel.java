package dev.industrialtechnology.client;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import org.lwjgl.opengl.GL11;
import dev.industrialtechnology.wireless.*;

/** Compact additional reactor view. Existing terminal, player inventory and SIMPLE/CODE stay intact. */
public final class GuiReactorPanel extends Gui {
    private final GuiWirelessTerminal g;
    private int page,selected,requested=-1;private boolean list,dragging,inventory;private String device="";
    public GuiReactorPanel(GuiWirelessTerminal gui){g=gui;}
    public boolean view(){return g.state.getBoolean("ReactorView");}
    public NBTTagCompound data(){return g.state.getCompoundTag("Reactor");}
    public int type(){return data().getInteger("Type");}
    private boolean blue(){return g.terminal.tab==2;}
    private boolean online(){return "ONLINE".equals(data().getString("Status"));}
    public void init(){
        add(40,239,52,64,13,"Reactor","Open reactor monitoring or automation; SIMPLE/CODE remain saved");
        add(41,123,70,54,12,"Grid","All 24 native fuel positions together in seven staggered rows");add(42,181,70,54,12,"List","Fuel names, depletion and empty positions");
        add(43,123,70,92,12,"Supply: Auto","Supply must be an inventory physically adjacent to this linked access point; Apply saves this choice");
        add(44,264,70,18,12,"<","Previous fuel page");add(45,285,70,18,12,">","Next fuel page");
        add(46,267,176,36,16,"ON","Enable the selected assignment in this draft");add(47,245,198,58,13,"Apply","Save all fuel assignments; desired icons are ghosts and consume no items");add(48,245,213,58,13,"Clear","Clear this assignment draft without removing any reactor fuel");
        add(49,10,169,28,13,"All+","Enable all nonempty assignments in the draft; then Apply");add(50,42,169,28,13,"All-","Disable all assignment drafts; then Apply");add(51,10,201,58,13,"Enable","Enable or pause the saved fuel feeder; it only draws real items from the adjacent supply");
        add(52,123,110,86,13,"Fully insert","Set requested insertion to 100%; click Apply to command the reactor");add(53,214,110,88,13,"Withdraw","Set requested insertion to 0%; click Apply to command the reactor");add(54,123,126,70,13,"Apply rods","Send a validated target to connected, loaded manual rods in the linked console footprint");
        add(55,51,185,24,13,"Help","Read the shared Automation Guide without closing the terminal");
        add(56,239,70,64,12,"Inventory","Pick a real fuel rod from your inventory, then click the assignment ghost; no item is consumed");
    }
    private void add(int id,int x,int y,int w,int h,String label,String tip){g.addPanelButton(id,x,y,w,h,label,tip);}
    public void receive(){
        String id=g.state.getString("Selected");if(!id.equals(device)){device=id;page=selected=0;requested=-1;list=false;inventory=false;}
        if(!view()||type()!=HbmReactors.ZIRNOX||!online())inventory=false;
        if(blue())selected=data().getInteger("SelectedSlot");
        page=Math.max(0,Math.min(page,pages()-1));
        if(requested<0&&type()==HbmReactors.RBMK_CONTROL)requested=data().hasKey("TargetInsertion")?snap(data().getDouble("TargetInsertion")):data().getInteger("LastRequest")>=0?snap(data().getInteger("LastRequest")):100;
    }
    private int pages(){return list?Math.max(1,(fuels().tagCount()+3)/4):1;}
    public boolean fullGrid(){return view()&&type()==HbmReactors.ZIRNOX&&(!list||blue());}
    public boolean inventoryOpen(){return fullGrid()&&blue()&&inventory;}
    public void closeInventory(){inventory=false;}
    public int gridX(NBTTagCompound fuel){return 138+fuel.getInteger("GridX")*3/4;}
    public int gridY(NBTTagCompound fuel){return 86+fuel.getInteger("Row")*18;}
    /** Client slot positions change; their authoritative server indices and item ownership stay the same. */
    public void layout(){
        g.terminal.layout(g.terminal.codeLayout);
        for(int i=0;i<36;i++){
            net.minecraft.inventory.Slot slot=(net.minecraft.inventory.Slot)g.terminal.inventorySlots.get(i);
            slot.xDisplayPosition=(inventoryOpen()?132:78)+(i<27?i%9:i-27)*18;
            if(fullGrid())slot.yDisplayPosition=inventoryOpen()?(i<27?104+i/9*18:172):-10000;
        }
        if(inventoryOpen())((net.minecraft.inventory.Slot)g.terminal.inventorySlots.get(36)).yDisplayPosition=-10000;
    }
    public void update(MetalButton b){
        int type=type();boolean active=view(),z=type==HbmReactors.ZIRNOX;
        b.visible=false;
        if(b.id==40){b.visible=type>0&&g.terminal.tab!=1&&!g.state.getString("Selected").isEmpty();b.xPosition=g.panelLeft()+(blue()?239:10);b.yPosition=g.panelTop()+(blue()?52:201);b.width=blue()?64:58;b.displayString=active&&!blue()?"Storage":type==HbmReactors.RBMK_CONTROL?"Rods":"Reactor";b.selected=active;return;}
        if(!active)return;
        if(b.id==55){b.visible=true;return;}
        if(b.id==56){b.visible=z&&blue();b.enabled=online();b.displayString=inventoryOpen()?"Fuel grid":"Inventory";b.selected=inventoryOpen();return;}
        if(inventoryOpen())return;
        if(b.id==41||b.id==42){b.visible=z&&!blue();b.selected=(b.id==42)==list;return;}
        if(b.id==44||b.id==45){b.visible=z&&list&&!blue();b.enabled=b.id==44?page>0:page+1<pages();return;}
        if(b.id==43){b.visible=z&&blue();int side=data().getInteger("Supply");String text=side<0?"Auto":net.minecraftforge.common.util.ForgeDirection.getOrientation(side).toString();b.displayString="Supply: "+text;return;}
        if(b.id>=46&&b.id<=51){b.visible=z&&blue();NBTTagCompound fuel=selectedFuel();if(b.id==46){b.displayString=fuel.getBoolean("Enabled")?"ON":"OFF";b.selected=fuel.getBoolean("Enabled");b.enabled=fuel.hasKey("Desired");}if(b.id==51){b.displayString=data().getBoolean("Running")?"Pause":"Enable";b.selected=data().getBoolean("Running");}return;}
        if(b.id>=52&&b.id<=54){b.visible=type==HbmReactors.RBMK_CONTROL&&blue();b.enabled=online()&&(b.id!=54||data().getInteger("RodCount")>0&&data().getString("GroupMessage").isEmpty());}
    }
    public void action(int id){
        switch(id){
            case 40:g.sendReactor(ReactorTerminal.VIEW,view()&&!blue()?0:1,"");break;
            case 41:list=false;page=0;break;case 42:list=true;page=0;break;
            case 43:int supply=data().getInteger("Supply");g.sendReactor(ReactorTerminal.SUPPLY,supply>=5?-1:supply+1,"");break;
            case 44:page=Math.max(0,page-1);break;case 45:page=Math.min(pages()-1,page+1);break;
            case 46:g.sendReactor(ReactorTerminal.ENABLE,selectedFuel().getBoolean("Enabled")?0:1,"");break;
            case 47:g.sendReactor(ReactorTerminal.APPLY,0,data().getString("Binding"));break;
            case 48:g.sendReactor(ReactorTerminal.CLEAR,0,"");break;
            case 49:case 50:g.sendReactor(ReactorTerminal.ALL,id==49?1:0,"");break;
            case 51:g.sendReactor(ReactorTerminal.RUN,0,"");break;
            case 52:requested=100;break;case 53:requested=0;break;
            case 54:g.sendReactor(ReactorTerminal.INSERTION,requested,data().getString("Binding"));break;
            case 55:g.openGuide();break;
            case 56:inventory=!inventory;break;
            default:break;
        }
    }
    public boolean click(int x,int y,int button){
        if(!view()||button!=0)return false;
        if(type()==HbmReactors.RBMK_CONTROL&&blue()&&online()&&x>=130&&x<295&&y>=94&&y<106){dragging=true;drag(x,y);return true;}
        if(type()!=HbmReactors.ZIRNOX||inventoryOpen())return false;
        NBTTagList fuels=fuels();
        if(list&&!blue()){if(x>=123&&x<303&&y>=86&&y<138){int index=page*4+(y-86)/13;if(index<fuels.tagCount())selected=fuels.getCompoundTagAt(index).getInteger("Slot");return true;}return false;}
        for(int i=0;i<fuels.tagCount();i++){
            NBTTagCompound fuel=fuels.getCompoundTagAt(i);
            int fx=gridX(fuel),fy=gridY(fuel);
            if(x>=fx&&x<fx+16&&y>=fy&&y<fy+16){selected=fuel.getInteger("Slot");if(blue())g.sendReactor(ReactorTerminal.SLOT,selected,"");return true;}
        }return false;
    }
    public boolean dragging(){return dragging;}
    public void release(){dragging=false;}
    private static int snap(double percent){return Math.max(0,Math.min(100,(int)Math.round(percent/10)*10));}
    public void drag(int x,int y){if(dragging)requested=snap((x-130)*100.0/164);}
    private NBTTagList fuels(){return data().getTagList("Fuels",10);}
    private NBTTagCompound selectedFuel(){NBTTagList list=fuels();for(int i=0;i<list.tagCount();i++)if(list.getCompoundTagAt(i).getInteger("Slot")==selected)return list.getCompoundTagAt(i);return new NBTTagCompound();}
    public void background(){
        int left=g.panelLeft(),top=g.panelTop();
        if(fullGrid()){
            drawRect(left+76,top+139,left+241,top+226,0xff262f35);
            drawRect(left+119,top+84,left+307,top+225,0xff262f35);
            if(inventoryOpen())for(int i=0;i<36;i++){
                net.minecraft.inventory.Slot slot=(net.minecraft.inventory.Slot)g.terminal.inventorySlots.get(i);
                int x=left+slot.xDisplayPosition,y=top+slot.yDisplayPosition;
                drawRect(x-1,y-1,x+17,y+17,0xff141c22);drawRect(x,y,x+16,y+16,0xff3a4750);
            }
        }
        if(view()&&blue()&&type()==HbmReactors.ZIRNOX&&!inventoryOpen()){
            int x=left+244,y=top+175;drawRect(x,y,x+19,y+19,0xff141c22);drawRect(x+1,y+1,x+18,y+18,0xff3a4750);
        }
    }
    private FontRenderer font(){return Minecraft.getMinecraft().fontRenderer;}
    private void text(String s,int x,int y,int color){font().drawString(s,x,y,color);}
    private String shortText(String s,int pixels){return font().getStringWidth(s)<=pixels?s:font().trimStringToWidth(s,Math.max(6,pixels-6))+"~";}
    private String metric(NBTTagCompound n,String key,String unit){return n.hasKey(key)?String.format(Locale.ROOT,"%.1f%s",n.getDouble(key),unit):"N/A";}
    private void icon(NBTTagCompound n,String key,int x,int y){if(!n.hasKey(key))return;ItemStack stack=ItemStack.loadItemStackFromNBT(n.getCompoundTag(key));if(stack!=null)g.panelIcon(stack,x,y);}
    public void draw(int x,int y){
        NBTTagCompound data=data();int type=type();List<String> hovered=null;
        if(!online()){text(shortText(data.getString("Status").replace('_',' '),180),123,88,0xdd9a89);text(shortText(data.getString("Message"),180),123,102,0xa4b5bd);g.panelHoverButtons(x+g.panelLeft(),y+g.panelTop());return;}
        if(inventoryOpen()){
            text("Your inventory",132,90,0xcbd5d0);text("Pick a fuel rod to return",132,199,0xa4b5bd);
            text("Then click its ghost slot",132,211,0xa4b5bd);g.panelHoverButtons(x+g.panelLeft(),y+g.panelTop());return;
        }
        if(type==HbmReactors.RBMK_FUEL){
            NBTTagCompound f=fuels().tagCount()>0?fuels().getCompoundTagAt(0):new NBTTagCompound();text("RBMK FUEL",123,72,0xb9c8cd);icon(f,"Icon",128,87);text(shortText(f.hasKey("Name")?f.getString("Name"):"EMPTY",151),150,89,0xd3dbd5);
            text("Depletion: "+metric(f,"Depletion","%"),123,110,0xc9b878);text("Remaining: "+metric(f,"Remaining","%"),123,122,0xa4c3ae);text(shortText(f.getString("State"),180),123,134,0xa4b5bd);
            text("Core "+'\u00b0'+"C",245,155,0x9fb5bb);text(shortText(metric(f,"CoreHeat",""),58),245,167,0xd9c69b);text("Skin "+'\u00b0'+"C",245,182,0x9fb5bb);text(shortText(metric(f,"HullHeat",""),58),245,194,0xd9c69b);text("Xenon %",245,208,0x9fb5bb);text(shortText(metric(f,"Xenon",""),58),245,220,0xb0bdcc);
            if(x>=123&&x<304&&(y>=85&&y<143||x>=245&&y>=150&&y<225))hovered=details(f);
        }else if(type==HbmReactors.ZIRNOX){
            NBTTagList fuels=fuels();page=Math.max(0,Math.min(page,pages()-1));if(list&&!blue())text((page+1)+"/"+pages(),240,72,0xb0bdcc);
            if(list&&!blue()){
                for(int i=page*4;i<Math.min(fuels.tagCount(),page*4+4);i++){NBTTagCompound f=fuels.getCompoundTagAt(i);int fy=86+i%4*13;drawRect(123,fy-1,303,fy+12,f.getInteger("Slot")==selected?0xff40535c:0xff25323a);String label=f.hasKey("Name")?f.getString("Name"):"EMPTY";text(shortText("R"+(f.getInteger("Row")+1)+"C"+(f.getInteger("Column")+1)+" "+label,134),125,fy+1,0xcbd5d0);text(metric(f,"Depletion","%"),264,fy+1,0xc9b878);if(x>=123&&x<303&&y>=fy&&y<fy+13)hovered=details(f);}
            }else{
                for(int i=0;i<fuels.tagCount();i++){NBTTagCompound f=fuels.getCompoundTagAt(i);int row=f.getInteger("Row");int fx=gridX(f),fy=gridY(f);
                    text(Integer.toString(row+1),124,fy+4,0x9fb5bb);drawRect(fx-1,fy-1,fx+17,fy+17,f.getInteger("Slot")==selected?0xffc0a162:0xff121b22);drawRect(fx,fy,fx+16,fy+16,0xff394650);icon(f,"Icon",fx,fy);
                    if(blue()&&!f.hasKey("Icon")&&f.hasKey("Desired")){icon(f,"Desired",fx,fy);badge(fx,fy,"P",true);}
                    if(blue()&&"WRONG FUEL".equals(f.getString("Automation")))badge(fx,fy,"!",false);
                    if(x>=fx&&x<fx+16&&y>=fy&&y<fy+16)hovered=details(f);
                }
            }
            if(blue()){
                int installed=0;for(int i=0;i<fuels.tagCount();i++)if(fuels.getCompoundTagAt(i).hasKey("Icon"))installed++;
                text(!data.getBoolean("AssignmentsSaved")?"APPLY PLAN":data.getBoolean("Running")?"RUNNING":"PAUSED",245,89,data.getBoolean("AssignmentsSaved")&&data.getBoolean("Running")?0x8fca9c:0xc9b878);
                text(shortText(data.hasKey("SupplyName")?net.minecraft.util.StatCollector.translateToLocal(data.getString("SupplyName")):"No supply",58),245,101,"READY".equals(data.getString("SupplyMessage"))?0x8fca9c:0xdd9a89);
                text(installed+"/24 in",245,113,0xcbd5d0);text("P planned",245,125,0x9eb5bd);text("! wrong",245,137,0xdd9a89);
                if(x>=245&&x<304&&y>=86&&y<147)hovered=supplyDetails();
            }
            NBTTagCompound f=selectedFuel();text("R"+(f.getInteger("Row")+1)+" C"+(f.getInteger("Column")+1),245,151,0xc8d6db);
            if(blue())text(shortText(f.getString("Automation"),58),245,163,0xc9b878);
            else{text(shortText("Dpl "+metric(f,"Depletion","%"),58),245,165,0xc9b878);text(shortText("Left "+metric(f,"Remaining","%"),58),245,178,0xa4c3ae);text("Core "+'\u00b0'+"C",245,193,0x9fb5bb);text(shortText(metric(data,"TemperatureC",""),58),245,205,0xd9c69b);text(shortText(f.getString("State"),58),245,220,0xb0bdcc);}
            if(x>=245&&x<304&&y>=150){List<String> lines=details(f);lines.add("Reactor temperature: "+metric(data,"TemperatureC","\u00b0C"));lines.add("Reactor pressure: "+metric(data,"PressureBar"," bar"));if(blue()){lines.add(f.getString("Automation"));lines.add("Ghost: click with a real fuel cursor stack; right-click clears");}hovered=lines;}
        }else if(type==HbmReactors.RBMK_CONTROL){
            text("Manual rods: "+data.getInteger("RodCount")+" | Auto: "+data.getInteger("Automatic"),123,72,0xb0bdcc);text("Rod insertion",123,83,0xd1d8d1);
            drawRect(130,95,295,104,0xff121c24);drawRect(131,96,294,103,0xff465661);int knob=130+requested*164/100;drawRect(knob-2,94,knob+3,105,0xffc5b883);
            text("Target: "+requested+"%",202,128,0xc9b878);text(data.getBoolean("Mixed")?"MIXED":"Current",245,155,0x9fb5bb);text(shortText(metric(data,"AverageInsertion","%"),58),245,167,0xcbd5d0);text("Native tgt",245,181,0x9fb5bb);text(data.getBoolean("MixedTarget")?"MIXED":shortText(metric(data,"TargetInsertion","%"),58),245,193,0xc9b878);
            String status=data.getString("GroupMessage");if(status.isEmpty())status=data.getInteger("Unloaded")+" unloaded";text(shortText(status,58),245,211,0xddbd96);
            if(x>=123&&x<304&&(y>=83&&y<108||x>=245&&y>=150&&y<225)){List<String> lines=new ArrayList<String>();lines.add("Insertion: 100% fully inserted; 0% withdrawn");lines.add("HBM moves rods at its native speed and requires normal power");if(data.getInteger("LastRequest")>=0)lines.add("Last group request: "+data.getInteger("LastRequest")+"% insertion");lines.add("Current average: "+metric(data,"AverageInsertion","%")+(data.getBoolean("Mixed")?" (MIXED rods)":""));lines.add(data.getInteger("RodCount")+" connected manual rods; "+data.getInteger("Automatic")+" automatic excluded");lines.add(data.getInteger("Unloaded")+" unloaded boundaries; no chunks are force-loaded");if(!data.getString("GroupMessage").isEmpty())lines.add(data.getString("GroupMessage"));hovered=lines;}
        }
        text(shortText("X"+data.getInteger("CoreX")+" Y"+data.getInteger("CoreY")+" Z"+data.getInteger("CoreZ"),fullGrid()?112:blue()?162:180),fullGrid()?123:blue()?141:123,fullGrid()?217:blue()?140:227,0x8e9a9d);
        if(x>=(fullGrid()?123:blue()?141:123)&&x<(fullGrid()?235:304)&&(fullGrid()?y>=215&&y<225:blue()?y>=140&&y<150:y>=225))hovered=Arrays.asList("Reactor: "+data.getInteger("CoreX")+", "+data.getInteger("CoreY")+", "+data.getInteger("CoreZ"),"Dimension "+g.state.getInteger("Dimension"),"Access: "+g.state.getInteger("X")+", "+g.state.getInteger("Y")+", "+g.state.getInteger("Z"));
        if(blue()){String feedback=data.getString("Feedback");text(shortText(feedback,180),123,227,0x9fb5bb);if(y>=225)hovered=Arrays.asList(feedback);}
        if(hovered!=null)g.panelTooltip(hovered,x+g.panelLeft(),y+g.panelTop());
        g.panelHoverButtons(x+g.panelLeft(),y+g.panelTop());
    }
    private void badge(int x,int y,String label,boolean planned){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_LIGHTING);
            if(planned)drawRect(x,y,x+16,y+16,0x99394650);
            drawRect(x+9,y+7,x+16,y+16,planned?0xff253a45:0xff6b2525);text(label,x+10,y+8,planned?0xb9d4df:0xffffa5);
        }finally{GL11.glPopAttrib();}
    }
    private String stackName(NBTTagCompound n,String key){ItemStack stack=n.hasKey(key)?ItemStack.loadItemStackFromNBT(n.getCompoundTag(key)):null;return stack==null?"EMPTY":stack.getDisplayName();}
    private List<String> supplyDetails(){
        NBTTagCompound n=data();List<String> lines=new ArrayList<String>();lines.add(n.getBoolean("Running")?"Fuel feeder: RUNNING":"Fuel feeder: PAUSED - click Enable");
        if(!n.getBoolean("AssignmentsSaved"))lines.add("Unsaved fuel pattern: click Apply before Enable");
        lines.add("Supply: "+n.getString("SupplyMessage"));
        if(n.hasKey("SupplyName"))lines.add(net.minecraft.util.StatCollector.translateToLocal(n.getString("SupplyName"))+" at "+n.getInteger("SupplyX")+", "+n.getInteger("SupplyY")+", "+n.getInteger("SupplyZ"));
        lines.add("Chest must touch the linked reactor block:");lines.add(n.getInteger("AccessX")+", "+n.getInteger("AccessY")+", "+n.getInteger("AccessZ"));
        lines.add("Feed pipes into that chest, not into the reactor");lines.add("P = planned only; bright unmarked rod = installed");lines.add("! = wrong installed fuel; remove it in the reactor GUI");
        lines.add("Hover a position for installed and assigned fuel names");return lines;
    }
    private List<String> details(NBTTagCompound f){
        List<String> lines=new ArrayList<String>();lines.add((blue()?"Installed: ":"")+(f.hasKey("Name")?f.getString("Name"):"EMPTY"));
        if(blue()){lines.add("Assigned: "+stackName(f,"Desired"));lines.add(f.getBoolean("Enabled")?"Assignment: ON":"Assignment: OFF");if("WRONG FUEL".equals(f.getString("Automation")))lines.add("Remove the wrong rod through the reactor GUI first");}
        lines.add("Row "+(f.getInteger("Row")+1)+", column "+(f.getInteger("Column")+1)+" | native slot "+f.getInteger("Slot"));lines.add("Fuel depletion: "+metric(f,"Depletion","%"));lines.add("Fuel remaining: "+metric(f,"Remaining","%"));lines.add(f.getString("State"));
        if(f.hasKey("CoreHeat"))lines.add("Rod core: "+metric(f,"CoreHeat","\u00b0C"));if(f.hasKey("HullHeat"))lines.add("Rod skin: "+metric(f,"HullHeat","\u00b0C"));
        if(f.hasKey("MaxLife"))lines.add("Lifetime used: "+f.getLong("Life")+" / "+f.getLong("MaxLife")+" fuel cycles");return lines;
    }
}
