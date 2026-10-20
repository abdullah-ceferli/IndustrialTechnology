package dev.industrialtechnology.automation;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class ScriptCompilerTest {
    private static final UUID TARGET=UUID.fromString("b18d1cf8-e530-4f6f-bd4a-5892ad0467c0");
    private final ScriptCompiler.Resolver resolver=new ScriptCompiler.Resolver(){
        public String item(String id){if(!id.startsWith("minecraft:")||id.endsWith("unknown"))throw new IllegalArgumentException("Unknown item");return id;}
        public String fluid(String id){if(!id.equals("water")&&!id.equals("oil"))throw new IllegalArgumentException("Unknown fluid");return id;}
        public UUID control(String name){if(!name.equals("Pump")&&!name.equals("Machine"))throw new IllegalArgumentException("Unknown control");return TARGET;}
    };
    private List<ScriptRule> parse(String text)throws Exception{return ScriptCompiler.compile(text,4096,resolver);}
    private void rejects(String text,int line,String message)throws Exception{try{parse(text);fail("Expected rejection");}catch(ScriptCompiler.Error e){assertEquals(line,e.line);assertTrue(e.display(),e.getMessage().contains(message));}}
    @Test public void repeatSecondsMinutesHoursAndCaseCompile()throws Exception{
        for(String unit:new String[]{"s","m","h"}){long factor=unit.equals("h")?72000:unit.equals("m")?1200:20;
            ScriptRule r=parse("WHEN FLUID \"water\" >= 200mb\nEVERY 20"+unit+"\nTHEN PULSE \"Pump\" 5s\nEND").get(0);assertEquals(20*factor,r.intervalTicks);assertEquals(5,r.seconds);}
        assertEquals(100,parse("when tank full\n# delay\nevery  5 S\nthen on \"Pump\"\nend").get(0).intervalTicks);
    }
    @Test public void repeatErrorsIdentifyTheActualLine()throws Exception{
        for(String invalid:new String[]{"0s","-5s","1.5s","20","20d","20ss","999999999999999999999h","999999999999999h"})
            rejects("WHEN TANK FULL\nEVERY "+invalid+"\nTHEN ON \"Pump\"\nEND",2,"EVERY");
        rejects("WHEN TANK FULL\nEVERY 5s\nEVERY 10s\nTHEN ON \"Pump\"\nEND",3,"one EVERY");
        rejects("EVERY 5s\nWHEN TANK FULL\nTHEN ON \"Pump\"\nEND",1,"Expected WHEN");
        rejects("WHEN TANK FULL\nTHEN ON \"Pump\"\nEVERY 5s\nEND",3,"END");
    }
    @Test public void repeatDoesNotChangeActionAndRuleLimits()throws Exception{
        for(String action:new String[]{"PULSE \"Pump\"","ON \"Pump\"","OFF \"Pump\""})assertEquals(20,parse("WHEN TANK EMPTY\nEVERY 1s\nTHEN "+action+"\nEND").get(0).intervalTicks);
        rejects("WHEN TANK EMPTY\nEVERY 20s\nTHEN PULSE \"Pump\" 2s\nEND",3,"5-10");
    }
    @Test public void exactWaterExampleCompiles()throws Exception{ScriptRule r=parse("WHEN FLUID \"water\" >= 200mb\nTHEN PULSE \"Pump\" 7s\nEND").get(0);assertEquals(ScriptRule.Kind.FLUID,r.kind);assertEquals(200,r.amount);assertEquals(7,r.seconds);assertEquals(TARGET,r.target);}
    @Test public void extraSpacesCaseAndCommentsWork()throws Exception{assertEquals(1,parse("# note\n  when  item \"minecraft:stone\" >= 1 \n then pulse \"Machine\"\nend\n").size());}
    @Test public void allConditionsAndActionsCompile()throws Exception{for(String condition:new String[]{"ITEM \"minecraft:stone\" >= 1","ITEM \"minecraft:coal\" < 16","FLUID \"oil\" >= 1000mb","FLUID \"water\" < 500mb","INVENTORY EMPTY","INVENTORY FULL","TANK EMPTY","TANK FULL"})for(String action:new String[]{"PULSE \"Pump\"","PULSE \"Pump\" 10s","ON \"Pump\"","OFF \"Pump\""})assertEquals(1,parse("WHEN "+condition+"\nTHEN "+action+"\nEND").size());}
    @Test public void multipleRulesKeepTheirLines()throws Exception{List<ScriptRule> r=parse("WHEN TANK EMPTY\nTHEN ON \"Pump\"\nEND\n\nWHEN TANK FULL\nTHEN OFF \"Pump\"\nEND");assertEquals(2,r.size());assertEquals(5,r.get(1).line);}
    @Test public void invalidNamesDoNotBecomeZero()throws Exception{rejects("WHEN FLUID \"unknown\" < 200mb\nTHEN ON \"Pump\"\nEND",1,"Unknown fluid");rejects("WHEN ITEM \"minecraft:unknown\" < 16\nTHEN ON \"Pump\"\nEND",1,"Unknown item");}
    @Test public void invalidControlsAndDurationsHaveActionLine()throws Exception{rejects("WHEN TANK EMPTY\nTHEN ON \"Missing\"\nEND",2,"Unknown control");for(int n:new int[]{0,4,11,999})rejects("WHEN TANK EMPTY\nTHEN PULSE \"Pump\" "+n+"s\nEND",2,"5-10");}
    @Test public void unknownCommandsAndMissingEndAreRejected()throws Exception{rejects("RUN JAVA",1,"Expected WHEN");rejects("WHEN TANK EMPTY\nTHEN STARTMACHINE \"Pump\"\nEND",2,"Expected THEN");rejects("WHEN TANK EMPTY\nTHEN ON \"Pump\"",2,"Missing END");}
    @Test public void unitsAndAmountsAreStrict()throws Exception{rejects("WHEN FLUID \"water\" >= 200\nTHEN ON \"Pump\"\nEND",1,"need mb");rejects("WHEN ITEM \"minecraft:stone\" >= 1mb\nTHEN ON \"Pump\"\nEND",1,"do not use mb");rejects("WHEN TANK EMPTY\nTHEN ON \"Pump\" 5s\nEND",2,"Only PULSE");rejects("WHEN ITEM \"minecraft:stone\" >= 999999999999999999999\nTHEN ON \"Pump\"\nEND",1,"Amount");}
    @Test public void eightRulesAndCharacterBoundsAreEnforced()throws Exception{String rule="WHEN TANK EMPTY\nTHEN ON \"Pump\"\nEND\n",text="";for(int i=0;i<8;i++)text+=rule;assertEquals(8,parse(text).size());rejects(text+rule,25,"eight");try{ScriptCompiler.compile(rule,10,resolver);fail();}catch(ScriptCompiler.Error e){assertTrue(e.getMessage().contains("long"));}}
}
