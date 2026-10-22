package dev.industrialtechnology.client.space;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.fluids.*;
import org.lwjgl.opengl.GL11;
import dev.industrialtechnology.space.*;
import dev.industrialtechnology.core.IndustrialTechnology;

/** Space-only industrial panel. Container and packet semantics are unchanged. */
public final class GuiSpace extends GuiContainer {
    private static final ResourceLocation PANEL=new ResourceLocation("industrialtech:textures/gui/space_console.png");
    public final SpaceContainer container;public NBTTagCompound state=new NBTTagCompound();
    private final Map<Integer,Integer> values=new HashMap<Integer,Integer>();
    public GuiSpace(SpaceContainer c){super(c);container=c;xSize=320;ySize=238;state=c.tile.snapshot();}
    private void button(int id,int x,int w,String text,int value){buttonList.add(new PanelButton(id,guiLeft+x,guiTop+108,w,text));values.put(id,value);}
    @Override public void initGui(){super.initGui();buttonList.clear();values.clear();int k=container.tile.kind;
        if(k==SpaceRegistry.COMPUTER){button(1,8,96,"Research Moon",0);button(2,110,96,"Research Mars",0);button(0,212,100,"Pause / resume",0);}
        else if(k==SpaceRegistry.LAUNCH){button(4,8,92,"Destination",0);button(5,106,100,"Return reserve",0);button(6,212,100,"Board & launch",0);}
        else if(k==SpaceRegistry.ASSEMBLY){button(3,8,142,"Assemble (50,000 RF)",0);button(8,156,50,"Type",0);button(0,212,100,"Pause / resume",0);}
        else if(k==SpaceRegistry.AIRLOCK){String[] n={"Shut both","Inner","Outer","To base","Outside"};for(int i=0;i<5;i++)button(20+i,8+i*61,59,n[i],i);}
        else button(0,212,100,"Pause / resume",0);
    }
    @Override protected void actionPerformed(GuiButton b){IndustrialTechnology.network.sendToServer(new SpaceCommand(container.windowId,b.id>=20?7:b.id,values.containsKey(b.id)?values.get(b.id):0));}
    private int tankCount(){int k=container.tile.kind;return k==SpaceRegistry.LAUNCH||k==SpaceRegistry.PORT?3:k==SpaceRegistry.REFINERY||k==SpaceRegistry.OXYGEN?2:k==SpaceRegistry.COMPRESSOR||k==SpaceRegistry.REFILL||k==SpaceRegistry.PRESSURIZER||k==SpaceRegistry.AIRLOCK?1:0;}
    private boolean powered(){int k=container.tile.kind;return k!=SpaceRegistry.LAUNCH&&k!=SpaceRegistry.PORT&&k!=SpaceRegistry.TELESCOPE;}
    private FluidStack tank(int i){return FluidStack.loadFluidStackFromNBT(state.getCompoundTag("Tank"+i));}
    private String fluid(int i){FluidStack f=tank(i);return f==null?"Empty":f.getLocalizedName()+" "+f.amount+" mB";}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(PANEL);Tessellator t=Tessellator.instance;t.startDrawingQuads();t.addVertexWithUV(guiLeft,guiTop+ySize,zLevel,0,ySize/256.0);t.addVertexWithUV(guiLeft+xSize,guiTop+ySize,zLevel,xSize/512.0,ySize/256.0);t.addVertexWithUV(guiLeft+xSize,guiTop,zLevel,xSize/512.0,0);t.addVertexWithUV(guiLeft,guiTop,zLevel,0,0);t.draw();
            if(container.tile.kind==SpaceRegistry.COMPUTER){drawRect(guiLeft+6,guiTop+21,guiLeft+314,guiTop+104,0xff20372f);}
            drawRect(guiLeft+6,guiTop+91,guiLeft+314,guiTop+104,0xff384247);
            if(powered()){int power=Math.max(0,Math.min(120,state.getInteger("Energy")*120/1000000));drawRect(guiLeft+188,guiTop+160,guiLeft+310,guiTop+167,0xff384247);drawRect(guiLeft+189,guiTop+161,guiLeft+189+power,guiTop+166,0xff9ab68b);}
            for(int i=0;i<tankCount();i++){int x=guiLeft+189+i*41;FluidStack f=tank(i);int h=f==null?0:Math.max(0,Math.min(39,(int)((long)f.amount*39/SpaceConfig.tankCapacity)));drawRect(x,guiTop+183,x+26,guiTop+225,0xff384247);int c=i==0?0xffb5a070:i==1?0xff85adb0:0xffaabcb5;if(f!=null&&f.getFluid()==SpaceRegistry.air)c=0xffaabcb5;drawRect(x+2,guiTop+223-h,x+24,guiTop+223,c);for(int j=1;j<4;j++)drawRect(x+2,guiTop+223-j*10,x+7,guiTop+224-j*10,0xff69767a);}
        }finally{GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}
    }
    @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{
            int k=container.tile.kind;String title=state.getBoolean("Facility")?"LC-39 Shuttle launch facility":StatCollector.translateToLocal(container.tile.getInventoryName());fontRendererObj.drawString(title,8,7,0xe3e5d8);int y=25;List<String> lines=new ArrayList<String>();
            if(k==SpaceRegistry.LAUNCH){int target=state.getInteger("Target");lines.add("DESTINATION: "+(target==2?"Earth":target==1?"Mars":"Moon")+" / "+(state.getBoolean("Reserve")?"RETURN RESERVE":"ONE WAY"));lines.add("Fuel: "+fluid(0));lines.add("Oxidizer: "+fluid(1));lines.add("Cabin: "+fluid(2));lines.add("1  Assembled rocket    2  Navigation HDD");lines.add(container.rocket==null?"Check pad clearance before launch.":"Landed vehicle / select destination, then board.");}
            else if(k==SpaceRegistry.PRESSURIZER){lines.add("ROOM: "+state.getString("Room"));lines.add("Pressure "+state.getInteger("Pressure")+"% / "+state.getInteger("Volume")+" cubic metres");lines.add("Supply: "+fluid(0));lines.add("Fill 500 mB/s maximum; finite air supply.");lines.add("A breach loses 20% room pressure per second.");lines.add("Room probe: air block above the machine.");}
            else if(k==SpaceRegistry.AIRLOCK){lines.add("CHAMBER: "+state.getInteger("LockPressure")+"%");lines.add("Base "+state.getInteger("BasePressure")+"% / outside "+state.getInteger("OutsidePressure")+"%");lines.add("Inner "+(state.getBoolean("InnerOpen")?"OPEN":"CLOSED")+"   Outer "+(state.getBoolean("OuterOpen")?"OPEN":"CLOSED"));lines.add("Supply: "+fluid(0));lines.add(state.getString("Lock"));lines.add("Close doors, match pressure, then open.");}
            else if(k==SpaceRegistry.ASSEMBLY){lines.add(state.getInteger("Target")==1?"SPACE SHUTTLE / 11 COMPONENTS":"LEGACY ROCKET / 11 COMPONENTS");for(int row=0;row<3;row++){StringBuilder s=new StringBuilder();for(int j=row*4;j<Math.min(11,row*4+4);j++)s.append(j+1).append(container.tile.items[j]==null?" --   ":" OK   ");lines.add(s.toString());}lines.add("Hover a component slot to read its name.");lines.add("Finished rocket: slot 12. Assembly: 50,000 RF.");}
            else if(k==SpaceRegistry.COMPUTER){lines.add("OPTICAL RESEARCH / REMOVABLE DISK");lines.add("Insert HDD in slot 1. Keep telescope sky clear.");lines.add("Moon: "+ResearchData.progress(container.tile.items[0],0)+" / "+SpaceConfig.moonResearch+" ticks");lines.add("Mars: "+ResearchData.progress(container.tile.items[0],1)+" / "+SpaceConfig.marsResearch+" ticks");lines.add("Research is saved on this disk.");lines.add(state.getBoolean("Enabled")?"Observatory online":"Observatory paused");}
            else if(k==SpaceRegistry.TELESCOPE){lines.add("OBSERVATORY / OPTICAL INSTRUMENT");lines.add("Keep the sky directly above unobstructed.");lines.add("Place beside your Research Computer.");lines.add("The computer powers the research process.");}
            else{lines.add(state.getBoolean("Enabled")?"PROCESS ENABLED":"PROCESS PAUSED");lines.add("Tank 1: "+fluid(0));lines.add("Tank 2: "+fluid(1));lines.add(k==SpaceRegistry.REFILL?"Slot 1: suit chest pack. Refill consumes air.":k==SpaceRegistry.REFINERY?"Slot 3: coal. Slot 1: fluid canister.":k==SpaceRegistry.COMPRESSOR?"Slot 3: filter. Requires Earth's atmosphere.":k==SpaceRegistry.PORT?"Connect to an owned launch controller nearby.":"Right-click: fill. Sneak-right-click: withdraw.");}
            int ink=k==SpaceRegistry.COMPUTER?0xb6c5a2:0x303b3d;for(String s:lines){if(y>81)break;fontRendererObj.drawString(fontRendererObj.trimStringToWidth(s,302),9,y,ink);y+=10;}
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth(state.getString("Status"),302),9,94,0xe4c68e);
            if(powered())fontRendererObj.drawString(state.getInteger("Energy")+" RF",189,150,0x384247);
            if(k==SpaceRegistry.COMPUTER){fontRendererObj.drawString("DISK PROGRESS",189,175,0x384247);for(int i=0;i<2;i++){int yy=189+i*19;int n=ResearchData.progress(container.tile.items[0],i),max=ResearchData.required(i);fontRendererObj.drawString((i==0?"Moon ":"Mars ")+n*100/max+"%",189,yy,0x384247);drawRect(189,yy+10,310,yy+14,0xff777968);drawRect(189,yy+10,189+(int)((long)n*121/max),yy+14,0xff9ab68b);}}
            else if(k==SpaceRegistry.TELESCOPE){fontRendererObj.drawString("PASSIVE OPTICS",189,175,0x384247);fontRendererObj.drawString("Sky clearance",189,193,0x555e54);fontRendererObj.drawString("required above",189,205,0x555e54);}
            else if(k==SpaceRegistry.ASSEMBLY){int ready=0;for(int i=0;i<11;i++)if(container.tile.items[i]!=null)ready++;fontRendererObj.drawString("WORKBENCH",189,175,0x384247);fontRendererObj.drawString(ready+" / 11 parts",189,193,0x384247);fontRendererObj.drawString("Output: slot 12",189,211,0x555e54);}
            for(int i=0;i<tankCount();i++)fontRendererObj.drawString("T"+(i+1),189+i*41,173,0x384247);
            if(k==SpaceRegistry.ASSEMBLY&&my>=guiTop+131&&my<guiTop+148){int slot=(mx-guiLeft-8)/25;if(slot>=0&&slot<11&&mx>=guiLeft+8){List<String> tip=new ArrayList<String>();tip.add((slot+1)+": "+StatCollector.translateToLocal("item.industrialtech."+SpaceRegistry.PARTS[slot]+".name"));drawHoveringText(tip,mx-guiLeft,my-guiTop,fontRendererObj);}}
            for(int i=0;i<tankCount();i++){int x=guiLeft+189+i*41;if(mx>=x&&mx<x+26&&my>=guiTop+183&&my<guiTop+225)drawHoveringText(Arrays.asList(fluid(i),"Capacity: "+SpaceConfig.tankCapacity+" mB"),mx-guiLeft,my-guiTop,fontRendererObj);}
        }finally{GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}
    }
    private static final class PanelButton extends GuiButton {
        PanelButton(int id,int x,int y,int w,String label){super(id,x,y,w,18,label);}
        @Override public void drawButton(Minecraft mc,int mx,int my){if(!visible)return;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{boolean over=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;int c=!enabled?0xff777968:over?0xffd2c9a8:0xffb9b5a2;drawRect(xPosition,yPosition,xPosition+width,yPosition+height,0xff252c30);drawRect(xPosition+1,yPosition+1,xPosition+width-1,yPosition+height-1,c);drawRect(xPosition+1,yPosition+1,xPosition+width-1,yPosition+2,0xffe3e5d8);drawRect(xPosition+2,yPosition+height-2,xPosition+width-1,yPosition+height-1,0xff777968);mc.fontRenderer.drawString(displayString,xPosition+(width-mc.fontRenderer.getStringWidth(displayString))/2,yPosition+5,enabled?0x303b3d:0x62695f);}finally{GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}}
    }
}
