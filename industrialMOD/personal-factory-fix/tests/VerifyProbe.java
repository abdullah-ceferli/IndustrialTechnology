import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import personal.factory.ProbeTransformer;

public final class VerifyProbe {
    private static byte[] read(JarFile jar, String path) throws Exception {
        try(InputStream in=jar.getInputStream(jar.getJarEntry(path)); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[32768];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);return out.toByteArray();
        }
    }
    public static void main(String[] args) throws Exception {
        String name="org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer";
        try(JarFile angelica=new JarFile(args[0]);JarFile previous=new JarFile(args[1]);JarFile current=new JarFile(args[2])) {
            byte[] changed=new ProbeTransformer().transform(name,name,read(angelica,name.replace('.','/')+".class"));
            ClassNode node=new ClassNode(Opcodes.ASM5);new ClassReader(changed).accept(node,0);
            int hooks=0;
            for(Object object:node.methods) {
                MethodNode method=(MethodNode)object;
                if(!method.name.equals("scheduleRebuildForBlockArea"))continue;
                new Analyzer(new BasicVerifier()).analyze(node.name,method);
                for(AbstractInsnNode instruction:method.instructions.toArray()) if(instruction instanceof MethodInsnNode) {
                    MethodInsnNode call=(MethodInsnNode)instruction;
                    if(call.owner.equals("personal/factory/FactoryProbe")&&call.name.equals("region"))hooks++;
                }
            }
            if(hooks!=1)throw new AssertionError("Exactly one real Angelica rebuild-request hook");
            for(String path:Arrays.asList("personal/factory/DuctPacketFix.class","personal/factory/LoadingPlugin.class")) {
                if(!Arrays.equals(read(previous,path),read(current,path)))throw new AssertionError("Unintended change: "+path);
            }
        }
        System.out.println("Probe: Angelica rebuild hook stack verified; existing duct filter/loading plugin byte-identical");
    }
}
