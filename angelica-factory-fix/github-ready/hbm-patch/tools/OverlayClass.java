import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;

/** Remap the Java-8 helpers and insert the source-equivalent renderer call into the official release. */
public final class OverlayClass {
    public static void main(String[] args) throws Exception {
        final String path="com/gtnewhorizons/angelica/rendering/TileEntityRenderBoundsRegistry.class";
        byte[] compiled=Files.readAllBytes(Paths.get(args[1],path));
        ClassReader reader=new ClassReader(compiled);
        ClassWriter writer=new ClassWriter(0);
        final Map<String,String> fields=new HashMap<String,String>();
        fields.put("minX","field_72340_a");fields.put("minY","field_72338_b");fields.put("minZ","field_72339_c");
        fields.put("maxX","field_72336_d");fields.put("maxY","field_72337_e");fields.put("maxZ","field_72334_f");
        reader.accept(new ClassVisitor(Opcodes.ASM5,writer) {
            public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e) {
                return new MethodVisitor(Opcodes.ASM5,super.visitMethod(a,n,d,s,e)) {
                    public void visitFieldInsn(int opcode,String owner,String name,String desc) {
                        if(owner.equals("net/minecraft/util/AxisAlignedBB") && fields.containsKey(name)) name=fields.get(name);
                        super.visitFieldInsn(opcode,owner,name,desc);
                    }
                };
            }
        },0);
        byte[] replacement=writer.toByteArray();
        final String helper="com/gtnewhorizons/angelica/rendering/HbmFactoryRenderFallback.class";
        byte[] helperBytes=Files.readAllBytes(Paths.get(args[1],helper));
        ClassWriter helperWriter=new ClassWriter(0);
        new ClassReader(helperBytes).accept(new ClassVisitor(Opcodes.ASM5,helperWriter) {
            public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e) {
                return new MethodVisitor(Opcodes.ASM5,super.visitMethod(a,n,d,s,e)) {
                    public void visitFieldInsn(int op,String owner,String name,String desc) {
                        if(owner.equals("net/minecraft/world/World") && name.equals("loadedTileEntityList"))name="field_147482_g";
                        if(owner.equals("net/minecraft/util/AxisAlignedBB") && fields.containsKey(name))name=fields.get(name);
                        if(owner.equals("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher") && name.equals("instance"))name="field_147556_a";
                        super.visitFieldInsn(op,owner,name,desc);
                    }
                    public void visitMethodInsn(int op,String owner,String name,String desc,boolean itf) {
                        if(owner.equals("net/minecraft/tileentity/TileEntity") && name.equals("isInvalid"))name="func_145837_r";
                        if(owner.equals("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher") && name.equals("hasSpecialRenderer"))name="func_147545_a";
                        super.visitMethodInsn(op,owner,name,desc,itf);
                    }
                };
            }
        },0);
        helperBytes=helperWriter.toByteArray();
        final String renderer="com/gtnewhorizons/angelica/rendering/celeritas/CeleritasWorldRenderer";
        final int[] hooks={0};
        boolean replaced=false;
        try(JarFile original=new JarFile(args[0]);JarOutputStream out=new JarOutputStream(new FileOutputStream(args[2]))) {
            Enumeration<JarEntry> entries=original.entries();byte[] buffer=new byte[32768];
            while(entries.hasMoreElements()) {
                JarEntry source=entries.nextElement();
                JarEntry target=new JarEntry(source.getName());target.setTime(source.getTime());out.putNextEntry(target);
                if(source.getName().equals(path)) {out.write(replacement);replaced=true;}
                else if(source.getName().equals(renderer+".class")) {
                    byte[] bytes;try(InputStream in=original.getInputStream(source)) {ByteArrayOutputStream b=new ByteArrayOutputStream();int n;while((n=in.read(buffer))!=-1)b.write(buffer,0,n);bytes=b.toByteArray();}
                    ClassWriter patched=new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM5,patched) {
                        public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e) {
                            MethodVisitor delegate=super.visitMethod(a,n,d,s,e);
                            if(!n.equals("renderBlockEntities") || !d.equals("(F)I"))return delegate;
                            return new MethodVisitor(Opcodes.ASM5,delegate) {
                                public void visitMethodInsn(int op,String owner,String name,String desc,boolean itf) {
                                    super.visitMethodInsn(op,owner,name,desc,itf);
                                    if(op==Opcodes.INVOKESPECIAL && owner.equals("org/embeddedt/embeddium/impl/render/terrain/SimpleWorldRenderer") && name.equals("renderBlockEntities") && desc.equals("(Ljava/lang/Object;)I")) {
                                        hooks[0]++;
                                        super.visitVarInsn(Opcodes.ALOAD,0);
                                        super.visitFieldInsn(Opcodes.GETFIELD,renderer,"world","Ljava/lang/Object;");
                                        super.visitTypeInsn(Opcodes.CHECKCAST,"net/minecraft/world/World");
                                        super.visitVarInsn(Opcodes.ALOAD,0);
                                        super.visitFieldInsn(Opcodes.GETFIELD,renderer,"frameTEs","Lit/unimi/dsi/fastutil/objects/ObjectArrayList;");
                                        super.visitVarInsn(Opcodes.ALOAD,0);
                                        super.visitFieldInsn(Opcodes.GETFIELD,renderer,"currentViewport","Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;");
                                        super.visitMethodInsn(Opcodes.INVOKESTATIC,"com/gtnewhorizons/angelica/rendering/HbmFactoryRenderFallback","reconcile","(Lnet/minecraft/world/World;Ljava/util/List;Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;)V",false);
                                    }
                                }
                            };
                        }
                    },0);
                    if(hooks[0]!=1)throw new IllegalStateException("Expected one main-pass collection hook, got "+hooks[0]);
                    out.write(patched.toByteArray());
                }
                else try(InputStream in=original.getInputStream(source)) {int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);}
                out.closeEntry();
            }
            if(!replaced || hooks[0]!=1)throw new IllegalStateException("Expected original classes not found");
            out.putNextEntry(new JarEntry(helper));out.write(helperBytes);out.closeEntry();
            out.putNextEntry(new JarEntry("META-INF/personal-hbm-fix.txt"));
            out.write(("Personal HBM visibility fallback 3, based on official Angelica 2.2.36 (e84c8c8d7accd6df0c93340c8409d997a43b9aaa).\n"
                +"TileEntityRenderBoundsRegistry and HbmFactoryRenderFallback compiled from source.\n"
                +"One source-equivalent call inserted in CeleritasWorldRenderer's main-pass collection; other entries preserved.\n"
                +"HBM tile entities use live main-thread bounds; visible missing live HBM machines are merged without duplicate rendering.\n"
                +"Experimental compatibility workaround; not an upstream release or a guarantee of fixing every flicker.\n").getBytes("UTF-8"));
            out.closeEntry();
        }
        System.out.println("Built "+args[2]);
    }
}
