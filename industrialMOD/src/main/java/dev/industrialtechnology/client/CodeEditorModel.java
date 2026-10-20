package dev.industrialtechnology.client;
import java.util.*;
/** UI-independent text state. Offsets are bounded; all editing honors selection. */
public final class CodeEditorModel {
    private String text="";public int cursor,anchor;public int limit=4096;private int preferred=-1;
    private final Deque<State> undo=new ArrayDeque<State>(),redo=new ArrayDeque<State>();
    private static final class State{String t;int c,a;State(CodeEditorModel m){t=m.text;c=m.cursor;a=m.anchor;}}
    public String text(){return text;}
    public void set(String value){text=value.replace("\r\n","\n").replace('\r','\n');cursor=anchor=0;preferred=-1;undo.clear();redo.clear();}
    public String selected(){return text.substring(Math.min(cursor,anchor),Math.max(cursor,anchor));}
    private void remember(){undo.push(new State(this));while(undo.size()>64)undo.removeLast();redo.clear();}
    public boolean insert(String input){String value=input.replace("\r\n","\n").replace('\r','\n');int a=Math.min(cursor,anchor),b=Math.max(cursor,anchor);
        if(text.length()-(b-a)+value.length()>limit)return false;remember();text=text.substring(0,a)+value+text.substring(b);cursor=anchor=a+value.length();preferred=-1;return true;}
    public void back(){if(cursor==anchor&&cursor>0)anchor=cursor-1;if(cursor!=anchor)insert("");}
    public void delete(){if(cursor==anchor&&cursor<text.length())anchor=cursor+1;if(cursor!=anchor)insert("");}
    public void move(int position,boolean select){cursor=Math.max(0,Math.min(text.length(),position));if(!select)anchor=cursor;preferred=-1;}
    public int row(){return row(cursor);}
    public int row(int offset){int n=0;for(int i=0;i<Math.min(offset,text.length());i++)if(text.charAt(i)=='\n')n++;return n;}
    public String[] lines(){return text.split("\n",-1);}
    public int start(int row){int n=0,p=0;while(n<row){int next=text.indexOf('\n',p);if(next<0)return text.length();p=next+1;n++;}return p;}
    public int end(int row){int p=start(row),end=text.indexOf('\n',p);return end<0?text.length():end;}
    public void vertical(int rows,boolean select){int col=preferred<0?cursor-start(row()):preferred;int r=Math.max(0,Math.min(lines().length-1,row()+rows));cursor=Math.min(end(r),start(r)+col);if(!select)anchor=cursor;preferred=col;}
    public void all(){anchor=0;cursor=text.length();}
    public void undo(){restore(undo,redo);}
    public void redo(){restore(redo,undo);}
    private void restore(Deque<State> source,Deque<State> target){if(source.isEmpty())return;target.push(new State(this));State s=source.pop();text=s.t;cursor=s.c;anchor=s.a;preferred=-1;}
}
