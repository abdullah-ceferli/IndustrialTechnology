package personal.offlineskins.client;

import java.io.IOException;
import java.util.*;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;
import personal.offlineskins.OfflineSkins;
import personal.offlineskins.friends.FriendsApi;

public final class FriendsScreen extends GuiScreen {
    private final GuiScreen parent;
    private final ClientSkins skins=(ClientSkins)OfflineSkins.proxy;
    private GuiTextField address,code;
    private FriendsLink.State displayed;
    private List<FriendsApi.Person> people=Collections.emptyList();
    private int tab,page,rows;
    private String error="";
    public FriendsScreen(GuiScreen parent){this.parent=parent;}
    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        String oldAddress=address==null?skins.friendsEndpoint():address.getText(),oldCode=code==null?"":code.getText();
        address=new GuiTextField(fontRendererObj,width/2-150,28,222,18);address.setMaxStringLength(2048);address.setText(oldAddress);
        code=new GuiTextField(fontRendererObj,width/2-150,height-73,208,18);code.setMaxStringLength(16);code.setText(oldCode);
        rebuild();
    }
    private void rebuild(){
        displayed=skins.friendsState();
        people=displayed==null?Collections.emptyList():tab==0?displayed.contacts.friends:tab==1?displayed.contacts.incoming:displayed.contacts.outgoing;
        rows=Math.max(1,(height-208)/22);page=Math.max(0,Math.min(page,Math.max(0,(people.size()-1)/rows)));
        buttonList.clear();int x=width/2-150;
        buttonList.add(new GuiButton(1,x+228,27,72,20,"Connect"));
        buttonList.add(new GuiButton(2,x+228,53,72,20,"Copy code"));
        for(int i=0;i<3;i++)buttonList.add(new GuiButton(10+i,x+i*101,81,98,20,(tab==i?"> ":"")+new String[]{"Friends","Incoming","Sent"}[i]));
        for(int row=0;row<rows && page*rows+row<people.size();row++){
            int y=106+row*22;
            if(tab==1){buttonList.add(new GuiButton(100+row*2,x+172,y,60,20,"Accept"));buttonList.add(new GuiButton(101+row*2,x+236,y,64,20,"Decline"));}
            else buttonList.add(new GuiButton(100+row*2,x+236,y,64,20,tab==0?"Remove":"Cancel"));
        }
        GuiButton previous=new GuiButton(20,x,height-102,30,18,"<");previous.enabled=page>0;buttonList.add(previous);
        GuiButton next=new GuiButton(21,x+270,height-102,30,18,">");next.enabled=(page+1)*rows<people.size();buttonList.add(next);
        buttonList.add(new GuiButton(3,x+214,height-74,86,20,"Send request"));
        buttonList.add(new GuiButton(4,x,height-49,146,20,"Disconnect service"));
        buttonList.add(new GuiButton(5,x+150,height-49,150,20,"Use server sharing"));
        buttonList.add(new GuiButton(0,width/2-70,height-25,140,20,"Done"));
    }
    @Override public void updateScreen(){
        address.updateCursorCounter();code.updateCursorCounter();
        if(displayed!=skins.friendsState())rebuild();
    }
    @Override protected void actionPerformed(GuiButton button){
        error="";
        try {
            if(button.id==0){mc.displayGuiScreen(parent);return;}
            if(button.id==1)skins.connectFriends(address.getText());
            else if(button.id==2 && displayed!=null)setClipboardString(displayed.code);
            else if(button.id==3){skins.friendAction("request",code.getText());code.setText("");}
            else if(button.id==4)skins.disconnectFriends();
            else if(button.id==5)skins.useServerSharing();
            else if(button.id>=10 && button.id<=12){tab=button.id-10;page=0;}
            else if(button.id==20)page--;
            else if(button.id==21)page++;
            else if(button.id>=100){int index=page*rows+(button.id-100)/2;if(index<people.size())skins.friendAction(tab==0?"remove":tab==2?"cancel":button.id%2==0?"accept":"decline",people.get(index).code);}
        }catch(IOException ex){error=ex.getMessage();}
        rebuild();
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partial){
        drawDefaultBackground();int x=width/2-150;
        drawCenteredString(fontRendererObj,"Offline Skins - Friends",width/2,9,0xffffff);
        address.drawTextBox();code.drawTextBox();
        String state=!error.isEmpty()?error:skins.lanSharing()?"LAN skin sharing is active.":mc.isIntegratedServerRunning()?"Friends service paused in single-player.":displayed==null?(skins.friendsOnly()?"Friends mode - connect to your service.":"Server sharing mode - connect for private friends sharing."):displayed.message;
        drawString(fontRendererObj,fontRendererObj.trimStringToWidth(state,222),x,49,!error.isEmpty()?0xff8888:0xcccccc);
        drawString(fontRendererObj,"Your code: "+(displayed==null || displayed.code.isEmpty()?"connect first":displayed.code),x,68,0xaaffaa);
        for(int row=0;row<rows && page*rows+row<people.size();row++){
            FriendsApi.Person person=people.get(page*rows+row);int y=109+row*22;
            drawString(fontRendererObj,fontRendererObj.trimStringToWidth(person.name,tab==1?164:230),x,y,0xffffff);
            drawString(fontRendererObj,person.code,x,y+9,0xaaaaaa);
        }
        if(people.isEmpty())drawString(fontRendererObj,tab==1?"No incoming requests.":tab==2?"No sent requests.":"No accepted friends yet.",x,109,0xcccccc);
        drawCenteredString(fontRendererObj,(page+1)+" / "+Math.max(1,(people.size()+rows-1)/rows),width/2,height-97,0xaaaaaa);
        if(code.getText().isEmpty() && !code.isFocused())drawString(fontRendererObj,"Paste a friend's code",x+4,height-68,0x777777);
        super.drawScreen(mouseX,mouseY,partial);
    }
    @Override protected void mouseClicked(int x,int y,int button){address.mouseClicked(x,y,button);code.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
    @Override protected void keyTyped(char character,int key){if(key==1)mc.displayGuiScreen(parent);else if(!address.textboxKeyTyped(character,key))code.textboxKeyTyped(character,key);}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}
}
