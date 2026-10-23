import java.io.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import personal.factory.ProbeTransformer;

public final class VerifyHooks {
    public static void main(String[] args) throws Exception {
        for(String jar:args)try(JarFile archive=new JarFile(jar)) {
            String[] targets=jar.contains("ThermalDynamics-")?new String[]{"cofh.thermaldynamics.block.TileTDBase"}:
                jar.contains("CoFHCore-")?new String[]{"cofh.core.network.PacketTile"}:
                new String[]{"net.minecraft.client.renderer.RenderGlobal","net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher"};
            for(String type:targets) {
                JarEntry entry=archive.getJarEntry(type.replace('.','/')+".class");
                if(entry==null)throw new AssertionError("Missing fixture "+type);
                ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                try(InputStream input=archive.getInputStream(entry)) {
                    byte[] chunk=new byte[8192];int n;while((n=input.read(chunk))!=-1)buffer.write(chunk,0,n);
                }
                byte[] result=new ProbeTransformer().transform(type,type,buffer.toByteArray());
                ClassNode node=new ClassNode();new ClassReader(result).accept(node,0);
                int hooks=0;
                for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
                    if(instruction instanceof MethodInsnNode && ((MethodInsnNode)instruction).owner.startsWith("personal/factory/"))hooks++;
                int expected=type.endsWith("RenderGlobal")||type.endsWith("TileTDBase")?1:type.endsWith("PacketTile")?3:2;
                if(hooks!=expected)throw new AssertionError("Unexpected hook count: "+type+" = "+hooks);
                StringWriter errors=new StringWriter();
                org.objectweb.asm.util.CheckClassAdapter.verify(new ClassReader(result),false,new PrintWriter(errors));
                if(errors.toString().length()!=0)throw new AssertionError(errors.toString());
                System.out.println("PASS "+jar+" | "+type+" | hooks="+hooks);
            }
        }
    }
}
