import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import personal.factory.*;
import net.minecraft.block.*;
import net.minecraft.world.World;
import com.hbm.blocks.gas.*;

public final class VerifyGasFix {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static byte[] read(InputStream input)throws IOException{
        try(InputStream in=input;ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] bytes=new byte[8192];int count;while((count=in.read(bytes))>=0)out.write(bytes,0,count);return out.toByteArray();
        }
    }
    private static int flags(World world,Block next,int meta){return GasRedrawFix.renderFlags(3,world,0,64,0,next,meta);}
    public static void main(String[] args)throws Exception{
        Block radon=new BlockGasRadon(), asbestos=new BlockGasAsbestos();
        World world=new World();
        check(flags(world,radon,0)==1,"Clear-space air to radon removes only render bit");
        world.setBlock(0,64,0,radon,0,3);
        check(flags(world,world.air,0)==1,"Radon to air remains eligible");
        check(flags(world,asbestos,0)==1,"Two audited gases remain invisible");
        check(flags(world,radon,0)==3,"Unchanged state is not counted as a saved redraw");
        Block solid=new Block();solid.render=0;solid.opaque=true;world.setBlock(2,66,2,solid,0,3);
        check(flags(world,world.air,0)==3,"Diagonal geometry at halo edge preserves render flags");
        world.setBlock(2,66,2,world.air,0,3);
        world.isRemote=false;check(flags(world,world.air,0)==3,"Server flags unchanged");world.isRemote=true;
        world.loaded=false;int reads=world.reads;check(flags(world,world.air,0)==3 && reads==world.reads,"Missing chunks preserve flags without block reads");world.loaded=true;
        world.orphan=true;check(flags(world,world.air,0)==3,"Unexpected tile entity preserves render update");world.orphan=false;
        GasRedrawFix.toggle();check(flags(world,world.air,0)==3,"Disabled filter preserves original flags");GasRedrawFix.toggle();
        Block modified=new BlockGasRadon();modified.light=1;check(flags(world,modified,0)==3,"Light emission is never filtered");
        modified.light=0;modified.opacity=1;check(flags(world,modified,0)==3,"Light obstruction is never filtered");
        modified.opacity=0;modified.tile=true;check(flags(world,modified,0)==3,"Tile-bearing variant is never filtered");
        modified.tile=false;modified.render=0;check(flags(world,modified,0)==3,"Renderable variant is never filtered");
        check(flags(world,new BlockGasRadon(){},0)==3,"Unrecognized subclasses fail open");
        check(GasRedrawFix.renderFlags(3,world,0,0,0,world.air,0)==3,"World-height boundary fails open");
        check(GasRedrawFix.renderFlags(1,world,0,64,0,world.air,0)==1,"No new flag bits are added");

        String type="net.minecraft.client.multiplayer.WorldClient";
        byte[] original=read(VerifyGasFix.class.getClassLoader().getResourceAsStream(type.replace('.','/')+".class"));
        byte[] transformed=new ProbeTransformer().transform(type,type,original);
        Class<?> fixture=new ClassLoader(VerifyGasFix.class.getClassLoader()){
            Class<?> define(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}
        }.define(transformed);
        World client=(World)fixture.newInstance();
        java.lang.reflect.Method update=fixture.getMethod("func_147492_c",int.class,int.class,int.class,Block.class,int.class);
        Object result=update.invoke(client,0,64,0,radon,7);
        check(Boolean.TRUE.equals(result) && client.setCalls==1 && client.invalidations==1,"Original update executes exactly once and preserves result/prework");
        check(client.lastFlags==1 && client.getBlock(0,64,0)==radon && client.getBlockMetadata(0,64,0)==7,"Block and metadata update normally with only render bit changed");
        client.setBlock(1,64,0,solid,0,3);update.invoke(client,0,64,0,client.air,0);
        check(client.lastFlags==3 && client.getBlock(0,64,0)==client.air,"Near geometry retains original flags and data updates");
        verifyReal(args[0],args[1]);
        check(GasRedrawFix.renderFlags(3,new Object(),0,64,0,radon,0)==3,"Unsupported runtime API fails open");
        long failures=GasRedrawFix.failures;
        check(GasRedrawFix.failedOpen && flags(world,world.air,0)==3 && GasRedrawFix.failures==failures,"Failure latch preserves flags without repeating exceptions");
        System.out.println("PASS gas guards, disabled/server/boundaries/lighting/tile/unknown behavior and actual transformed packet execution");
    }
    private static void verifyReal(String path,String srg)throws Exception{
        Map<String,String> mappings=new HashMap<String,String>();
        for(String line:Files.readAllLines(Paths.get(srg))){String[] p=line.split(" ");if(p.length==5 && p[0].equals("MD:")){
            int slash=p[1].lastIndexOf('/');mappings.put(p[1].substring(0,slash)+"."+p[1].substring(slash+1)+p[2],p[3].substring(p[3].lastIndexOf('/')+1));
        }}
        String type="net.minecraft.client.multiplayer.WorldClient";
        try(JarFile jar=new JarFile(path)){
            byte[] input=read(jar.getInputStream(jar.getJarEntry(type.replace('.','/')+".class")));
            for(boolean production:new boolean[]{false,true}){
                byte[] original=input;
                if(production){ClassWriter mapped=new ClassWriter(0);new ClassReader(input).accept(new RemappingClassAdapter(mapped,new SimpleRemapper(mappings)),ClassReader.EXPAND_FRAMES);original=mapped.toByteArray();}
                ClassNode node=new ClassNode();new ClassReader(new ProbeTransformer().transform(type,type,original)).accept(node,0);
                int hook=0,originalCalls=0;
                for(MethodNode method:node.methods){
                    if(method.instructions.size()>0)new Analyzer(new BasicVerifier()).analyze(node.name,method);
                    if(!method.name.equals("func_147492_c"))continue;
                    for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)instruction;
                        if(call.owner.equals("personal/factory/GasRedrawFix") && call.name.equals("renderFlags"))hook++;
                        if(call.owner.equals("net/minecraft/world/World") && (call.name.equals("setBlock") || call.name.equals("func_147465_d")) && call.getOpcode()==Opcodes.INVOKESPECIAL)originalCalls++;
                    }
                }
                check(hook==1 && originalCalls==1,"One gas flag hook and original super call in "+(production?"SRG":"MCP"));
                System.out.println("PASS real WorldClient "+(production?"SRG":"MCP")+" stack and original call preserved");
            }
        }
    }
}
