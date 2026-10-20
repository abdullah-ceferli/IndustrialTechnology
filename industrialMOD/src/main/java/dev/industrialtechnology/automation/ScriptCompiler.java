package dev.industrialtechnology.automation;
import java.util.*;
import java.util.regex.*;
/** Line-based grammar. No external language, eval, bytecode or arbitrary calls. */
public final class ScriptCompiler {
    public static final int HARD_LENGTH=8192,MAX_BYTES=24576,MAX_RULES=8;
    public interface Resolver {String item(String name)throws IllegalArgumentException;String fluid(String name)throws IllegalArgumentException;UUID control(String name)throws IllegalArgumentException;}
    public static final class Error extends Exception {
        public final int line;
        public Error(int line,String message){super(message);this.line=line;}
        public String display(){return "Line "+line+": "+getMessage();}
    }
    private static final String QUOTED="\"((?:[^\"\\\\]|\\\\[\"\\\\])*)\"";
    private static Pattern pattern(String regex){return Pattern.compile(regex,Pattern.CASE_INSENSITIVE);}
    private static final Pattern VALUE=pattern("WHEN\\s+(ITEM|FLUID)\\s+"+QUOTED+"\\s*(>=|<)\\s*(\\d+)\\s*(mb)?");
    private static final Pattern STATE=pattern("WHEN\\s+(INVENTORY|TANK)\\s+(EMPTY|FULL)");
    private static final Pattern ACTION=pattern("THEN\\s+(PULSE|ON|OFF)\\s+"+QUOTED+"(?:\\s+(\\d+)\\s*s)?");
    private static String unquote(String text){return text.replace("\\\"","\"").replace("\\\\","\\");}
    public static List<ScriptRule> compile(String text,int limit,Resolver resolver)throws Error {
        if(text==null||text.length()>Math.min(HARD_LENGTH,limit))throw new Error(1,"Script is too long.");
        String[] lines=text.replace("\r\n","\n").replace('\r','\n').split("\n",-1);List<ScriptRule> rules=new ArrayList<ScriptRule>();ScriptRule rule=null;int stage=0;
        for(int i=0;i<lines.length;i++){
            String s=lines[i].trim();int line=i+1;if(s.isEmpty()||s.startsWith("#"))continue;
            if(s.indexOf('\u00a7')>=0)throw new Error(line,"Formatting codes are not commands.");
            try{
                if(stage==0){
                    if(rules.size()==MAX_RULES)throw new Error(line,"Use at most eight WHEN rules.");
                    rule=new ScriptRule();rule.line=line;Matcher v=VALUE.matcher(s),state=STATE.matcher(s);
                    if(v.matches()){
                        boolean fluid=v.group(1).equalsIgnoreCase("FLUID");rule.kind=fluid?ScriptRule.Kind.FLUID:ScriptRule.Kind.ITEM;
                        if(fluid!=(v.group(5)!=null))throw new Error(line,fluid?"Fluid amounts need mb, for example 200mb.":"Item amounts do not use mb.");
                        String id=unquote(v.group(2));rule.identifier=fluid?resolver.fluid(id):resolver.item(id);rule.less="<".equals(v.group(3));
                        try{rule.amount=Long.parseLong(v.group(4));}catch(NumberFormatException large){throw new Error(line,"Amount must be 0 through 1000000000000.");}
                        if(rule.amount>1000000000000L)throw new Error(line,"Amount must be 0 through 1000000000000.");
                    }else if(state.matches()){rule.kind=ScriptRule.Kind.valueOf(state.group(1).toUpperCase(Locale.ROOT)+"_"+state.group(2).toUpperCase(Locale.ROOT));}
                    else throw new Error(line,s.toUpperCase(Locale.ROOT).startsWith("WHEN")?"Expected ITEM, FLUID, INVENTORY EMPTY/FULL or TANK EMPTY/FULL.":"Expected WHEN.");
                    stage=1;
                }else if(stage==1){
                    Matcher a=ACTION.matcher(s);if(!a.matches())throw new Error(line,"Expected THEN PULSE, THEN ON or THEN OFF and a quoted control name.");
                    rule.action=ScriptRule.Action.valueOf(a.group(1).toUpperCase(Locale.ROOT));rule.target=resolver.control(unquote(a.group(2)));
                    if(rule.target==null)throw new Error(line,"Yellow Stick was not found.");
                    if(a.group(3)!=null){
                        if(rule.action!=ScriptRule.Action.PULSE)throw new Error(line,"Only PULSE takes a duration.");
                        try{rule.seconds=Integer.parseInt(a.group(3));}catch(NumberFormatException invalid){throw new Error(line,"Pulse duration must be 5-10 seconds.");}
                        if(rule.seconds<5||rule.seconds>10)throw new Error(line,"Pulse duration must be 5-10 seconds.");
                    }stage=2;
                }else{if(!s.equalsIgnoreCase("END"))throw new Error(line,"Missing END.");rules.add(rule);rule=null;stage=0;}
            }catch(IllegalArgumentException invalid){throw new Error(line,invalid.getMessage()==null?"Invalid command.":invalid.getMessage());}
        }
        if(stage!=0)throw new Error(lines.length,stage==2?"Missing END.":"Expected THEN.");
        if(rules.isEmpty())throw new Error(1,"Expected WHEN. Use Help for your first program.");return rules;
    }
    private ScriptCompiler(){}
}
