package com.mojang.blaze3d.shaders;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class Program {
   private static final int MAX_LOG_LENGTH = 32768;
   private final Program.Type type;
   private final String name;
   private int id;

   protected Program(Program.Type pType, int pId, String pName) {
      this.type = pType;
      this.id = pId;
      this.name = pName;
   }

   public void attachToShader(Shader pShader) {
      RenderSystem.assertOnRenderThread();
      GlStateManager.glAttachShader(pShader.getId(), this.getId());
   }

   public void close() {
      if (this.id != -1) {
         RenderSystem.assertOnRenderThread();
         GlStateManager.glDeleteShader(this.id);
         this.id = -1;
         this.type.getPrograms().remove(this.name);
      }
   }

   public String getName() {
      return this.name;
   }

   public static Program compileShader(Program.Type pType, String pName, InputStream pShaderData, String pSourceName, GlslPreprocessor pPreprocessor) throws IOException {
      RenderSystem.assertOnRenderThread();
      int i = compileShaderInternal(pType, pName, pShaderData, pSourceName, pPreprocessor);
      Program program = new Program(pType, i, pName);
      pType.getPrograms().put(pName, program);
      return program;
   }

   protected static int compileShaderInternal(Program.Type pType, String pName, InputStream pShaderData, String pSourceName, GlslPreprocessor pPreprocessor) throws IOException {
      String s = IOUtils.toString(pShaderData, StandardCharsets.UTF_8);
      if (s == null) {
         throw new IOException("Could not load program " + pType.getName());
      } else {
         int i = GlStateManager.glCreateShader(pType.getGlType());
         GlStateManager.glShaderSource(i, pPreprocessor.process(s));
         GlStateManager.glCompileShader(i);
         if (GlStateManager.glGetShaderi(i, 35713) == 0) {
            String s1 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
            throw new IOException("Couldn't compile " + pType.getName() + " program (" + pSourceName + ", " + pName + ") : " + s1);
         } else {
            return i;
         }
      }
   }

   protected int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Type {
      VERTEX("vertex", ".vsh", 35633),
      FRAGMENT("fragment", ".fsh", 35632);

      private final String name;
      private final String extension;
      private final int glType;
      private final Map<String, Program> programs = Maps.newHashMap();

      private Type(String pName, String pExtension, int pGlType) {
         this.name = pName;
         this.extension = pExtension;
         this.glType = pGlType;
      }

      public String getName() {
         return this.name;
      }

      public String getExtension() {
         return this.extension;
      }

      int getGlType() {
         return this.glType;
      }

      public Map<String, Program> getPrograms() {
         return this.programs;
      }
   }
}