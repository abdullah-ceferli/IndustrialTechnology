import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import com.gtnewhorizons.angelica.rendering.TileEntityRenderBoundsRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import com.gtnewhorizons.angelica.rendering.HbmFactoryRenderFallback;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;

public final class VerifyOverlay {
    static class StaticTile extends TileEntity {}
    static class DynamicTile extends TileEntity {}
    static class InfiniteTile extends TileEntity {
        public AxisAlignedBB getRenderBoundingBox(){AxisAlignedBB b=new AxisAlignedBB();b.field_72336_d=Double.POSITIVE_INFINITY;return b;}
    }
    public static void main(String[] args)throws Exception {
        com.hbm.test.Machine hbm=new com.hbm.test.Machine();
        require(TileEntityRenderBoundsRegistry.classify(hbm)==TileEntityRenderBoundsRegistry.DYNAMIC,"HBM must use live bounds");
        require(hbm.reads==0,"Do not inspect HBM bounds on a worker thread");
        hbm.unavailable=true;
        require(TileEntityRenderBoundsRegistry.classify(hbm)==TileEntityRenderBoundsRegistry.DYNAMIC && hbm.reads==0,"Do not query transient HBM bounds during worker classification");
        require(TileEntityRenderBoundsRegistry.classify(new StaticTile())==TileEntityRenderBoundsRegistry.STATIC,"Ordinary finite tile");
        TileEntityRenderBoundsRegistry.registerDynamicClass(DynamicTile.class.getName());
        require(TileEntityRenderBoundsRegistry.classify(new DynamicTile())==TileEntityRenderBoundsRegistry.DYNAMIC,"Configured dynamic tile");
        require(TileEntityRenderBoundsRegistry.classify(new InfiniteTile())==TileEntityRenderBoundsRegistry.INFINITE,"SRG fields and infinite tile");
        World world=new World();
        com.hbm.test.Machine missing=new com.hbm.test.Machine(),invalid=new com.hbm.test.Machine(),nonTicking=new com.hbm.test.Machine();
        invalid.invalid=true;
        TileEntity other=new StaticTile();
        world.field_147482_g.add(hbm);world.field_147482_g.add(missing);world.field_147482_g.add(invalid);world.field_147482_g.add(other);
        List<TileEntity> candidates=new ArrayList<TileEntity>(Arrays.asList(hbm,hbm,nonTicking,invalid,other));
        HbmFactoryRenderFallback.reconcile(world,candidates,null);
        require(candidates.size()==4 && candidates.contains(hbm) && candidates.contains(missing) && candidates.contains(nonTicking) && candidates.contains(other),"Recover missing live HBM; preserve static models; remove invalid and duplicate HBM");
        HbmFactoryRenderFallback.reconcile(world,candidates,null);
        require(candidates.size()==4,"Do not render a recovered machine twice");
        List<TileEntity> empty=new ArrayList<TileEntity>();
        HbmFactoryRenderFallback.reconcile(world,empty,null);
        require(empty.size()==2 && !empty.contains(other),"Only valid HBM machines are recovered from the live list");
        HbmFactoryRenderFallback.reconcile(null,empty,null);
        Viewport viewport=new Viewport();viewport.accepts=false;
        hbm.unavailable=false;
        empty.clear();HbmFactoryRenderFallback.reconcile(world,empty,viewport);
        require(empty.isEmpty(),"Do not recover off-screen factories");
        viewport.accepts=true;
        missing.renderable=false;
        HbmFactoryRenderFallback.reconcile(world,empty,viewport);
        require(empty.size()==1 && empty.contains(hbm),"Do not recover non-renderable proxy tiles");
        viewport.accepts=false;hbm.unavailable=true;empty.clear();
        HbmFactoryRenderFallback.reconcile(world,empty,viewport);
        require(empty.size()==1 && empty.contains(hbm),"Uncertain bounds fail open");
        Set<String> expected=new HashSet<String>();expected.add("com/gtnewhorizons/angelica/rendering/TileEntityRenderBoundsRegistry.class");
        expected.add("com/gtnewhorizons/angelica/rendering/celeritas/CeleritasWorldRenderer.class");
        try(JarFile original=new JarFile(args[0]);JarFile patched=new JarFile(args[1])) {
            int changed=0;
            for(Enumeration<JarEntry> all=original.entries();all.hasMoreElements();) {
                JarEntry entry=all.nextElement();JarEntry next=patched.getJarEntry(entry.getName());
                require(next!=null,"Missing entry "+entry.getName());
                byte[] before=read(original.getInputStream(entry)),after=read(patched.getInputStream(next));
                if(!Arrays.equals(before,after)){require(expected.remove(entry.getName()),"Unexpected change "+entry.getName());changed++;}
            }
            require(expected.isEmpty()&&changed==2,"Exactly two original classes changed");
            require(patched.size()==original.size()+2,"One helper and one provenance note added");
            ClassNode cn=new ClassNode(Opcodes.ASM5);
            new ClassReader(read(patched.getInputStream(patched.getJarEntry("com/gtnewhorizons/angelica/rendering/celeritas/CeleritasWorldRenderer.class")))).accept(cn,0);
            int hooks=0;
            for(Object entry:cn.methods) {
                MethodNode method=(MethodNode)entry;
                if(!method.name.equals("renderBlockEntities") || !method.desc.equals("(F)I"))continue;
                new Analyzer(new BasicVerifier()).analyze(cn.name,method);
                for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode) {
                    MethodInsnNode invoke=(MethodInsnNode)instruction;
                    if(invoke.owner.equals("com/gtnewhorizons/angelica/rendering/HbmFactoryRenderFallback") && invoke.name.equals("reconcile"))hooks++;
                }
            }
            require(hooks==1,"Exactly one main-pass fallback call with valid stack flow");
        }
        System.out.println("PASS: HBM bounds and live-list fallbacks; deduplication; invalid and non-ticking models; other mods preserved; injected stack flow; exactly two upstream classes changed, helper and provenance added, all other entries identical.");
    }
    static byte[] read(InputStream in)throws IOException {
        try(InputStream input=in;ByteArrayOutputStream out=new ByteArrayOutputStream()) {byte[] b=new byte[32768];int n;while((n=input.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}
    }
    static void require(boolean test,String reason){if(!test)throw new AssertionError(reason);}
}
