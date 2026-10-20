package dev.industrialtechnology.automation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
/** One reference source feeds the book, editor help, Markdown generator and coverage tests. */
public final class AutomationReference {
    public static final class Section{public final String title,text,example;Section(String a,String b,String c){title=a;text=b;example=c;}}
    private static JsonObject reference;
    public static synchronized JsonObject data(){if(reference!=null)return reference;
        try(Reader reader=new InputStreamReader(AutomationReference.class.getResourceAsStream("/assets/industrialtech/automation_reference.json"),StandardCharsets.UTF_8)){reference=new JsonParser().parse(reader).getAsJsonObject();return reference;}
        catch(IOException|RuntimeException invalid){throw new IllegalStateException("Automation reference is missing or invalid",invalid);}
    }
    private static String text(JsonObject row,String key){return row.has(key)?row.get(key).getAsString():"";}
    public static List<Section> sections(){List<Section> list=new ArrayList<Section>();JsonObject data=data();
        for(String group:new String[]{"intro","conditions","actions","examples","troubleshooting"})for(JsonElement element:data.getAsJsonArray(group)){
            JsonObject row=element.getAsJsonObject();String title=text(row,"title");if(title.isEmpty())title=text(row,"id").replace('_',' ');
            String body=text(row,"text");if(row.has("syntax"))body+="\n\n"+text(row,"syntax");if(row.has("example")&&!text(row,"example").isEmpty())body+="\n\n"+text(row,"example");
            if(row.has("result"))body+="\n\n"+text(row,"result");if(row.has("limits"))body+="\n\n"+text(row,"limits");list.add(new Section(title,body,text(row,"example")));
        }
        StringBuilder words=new StringBuilder();for(JsonElement element:data.getAsJsonArray("glossary")){JsonObject row=element.getAsJsonObject();words.append(text(row,"word")).append(" - ").append(text(row,"text")).append("\n\n");}list.add(new Section("Code words",words.toString(),""));return list;
    }
    private AutomationReference(){}
}
