package com.mojang.blaze3d.platform;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GlDebug {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CIRCULAR_LOG_SIZE = 10;
   private static final Queue<GlDebug.LogEntry> MESSAGE_BUFFER = EvictingQueue.create(10);
   @Nullable
   private static volatile GlDebug.LogEntry lastEntry;
   private static final List<Integer> DEBUG_LEVELS = ImmutableList.of(37190, 37191, 37192, 33387);
   private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of(37190, 37191, 37192);
   private static boolean debugEnabled;

   private static String printUnknownToken(int pToken) {
      return "Unknown (0x" + Integer.toHexString(pToken).toUpperCase() + ")";
   }

   public static String sourceToString(int pSource) {
      switch (pSource) {
         case 33350:
            return "API";
         case 33351:
            return "WINDOW SYSTEM";
         case 33352:
            return "SHADER COMPILER";
         case 33353:
            return "THIRD PARTY";
         case 33354:
            return "APPLICATION";
         case 33355:
            return "OTHER";
         default:
            return printUnknownToken(pSource);
      }
   }

   public static String typeToString(int pType) {
      switch (pType) {
         case 33356:
            return "ERROR";
         case 33357:
            return "DEPRECATED BEHAVIOR";
         case 33358:
            return "UNDEFINED BEHAVIOR";
         case 33359:
            return "PORTABILITY";
         case 33360:
            return "PERFORMANCE";
         case 33361:
            return "OTHER";
         case 33384:
            return "MARKER";
         default:
            return printUnknownToken(pType);
      }
   }

   public static String severityToString(int pSeverity) {
      switch (pSeverity) {
         case 33387:
            return "NOTIFICATION";
         case 37190:
            return "HIGH";
         case 37191:
            return "MEDIUM";
         case 37192:
            return "LOW";
         default:
            return printUnknownToken(pSeverity);
      }
   }

   /**
    * 
    * @param p_84039_ The GLenum source represented as an ordinal integer.
    * @param p_84040_ The GLenum type represented as an ordinal integer.
    * @param p_84041_ The unbounded integer id of the message callback.
    * @param p_84042_ The GLenum severity represented as an ordinal integer.
    * @param p_84043_ The {@link org.lwjgl.opengl.GLDebugMessageCallback} length argument.
    * @param p_84044_ The {@link org.lwjgl.opengl.GLDebugMessageCallback} message argument
    * @param p_84045_ A user supplied pointer that will be passed on each invocation of callback.
    */
   private static void printDebugLog(int p_84039_, int p_84040_, int p_84041_, int p_84042_, int p_84043_, long p_84044_, long p_84045_) {
      String s = GLDebugMessageCallback.getMessage(p_84043_, p_84044_);
      GlDebug.LogEntry gldebug$logentry;
      synchronized(MESSAGE_BUFFER) {
         gldebug$logentry = lastEntry;
         if (gldebug$logentry != null && gldebug$logentry.isSame(p_84039_, p_84040_, p_84041_, p_84042_, s)) {
            ++gldebug$logentry.count;
         } else {
            gldebug$logentry = new GlDebug.LogEntry(p_84039_, p_84040_, p_84041_, p_84042_, s);
            MESSAGE_BUFFER.add(gldebug$logentry);
            lastEntry = gldebug$logentry;
         }
      }

      LOGGER.info("OpenGL debug message: {}", (Object)gldebug$logentry);
   }

   public static List<String> getLastOpenGlDebugMessages() {
      synchronized(MESSAGE_BUFFER) {
         List<String> list = Lists.newArrayListWithCapacity(MESSAGE_BUFFER.size());

         for(GlDebug.LogEntry gldebug$logentry : MESSAGE_BUFFER) {
            list.add(gldebug$logentry + " x " + gldebug$logentry.count);
         }

         return list;
      }
   }

   public static boolean isDebugEnabled() {
      return debugEnabled;
   }

   public static void enableDebugCallback(int pDebugVerbosity, boolean pSynchronous) {
      RenderSystem.assertInInitPhase();
      if (pDebugVerbosity > 0) {
         GLCapabilities glcapabilities = GL.getCapabilities();
         if (glcapabilities.GL_KHR_debug) {
            debugEnabled = true;
            GL11.glEnable(37600);
            if (pSynchronous) {
               GL11.glEnable(33346);
            }

            for(int i = 0; i < DEBUG_LEVELS.size(); ++i) {
               boolean flag = i < pDebugVerbosity;
               KHRDebug.glDebugMessageControl(4352, 4352, DEBUG_LEVELS.get(i), (int[])null, flag);
            }

            KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
         } else if (glcapabilities.GL_ARB_debug_output) {
            debugEnabled = true;
            if (pSynchronous) {
               GL11.glEnable(33346);
            }

            for(int j = 0; j < DEBUG_LEVELS_ARB.size(); ++j) {
               boolean flag1 = j < pDebugVerbosity;
               ARBDebugOutput.glDebugMessageControlARB(4352, 4352, DEBUG_LEVELS_ARB.get(j), (int[])null, flag1);
            }

            ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   static class LogEntry {
      private final int id;
      private final int source;
      private final int type;
      private final int severity;
      private final String message;
      int count = 1;

      LogEntry(int pSource, int pType, int pId, int pSeverity, String pMessage) {
         this.id = pId;
         this.source = pSource;
         this.type = pType;
         this.severity = pSeverity;
         this.message = pMessage;
      }

      boolean isSame(int pSource, int pType, int pId, int pSeverity, String pMessage) {
         return pType == this.type && pSource == this.source && pId == this.id && pSeverity == this.severity && pMessage.equals(this.message);
      }

      public String toString() {
         return "id=" + this.id + ", source=" + GlDebug.sourceToString(this.source) + ", type=" + GlDebug.typeToString(this.type) + ", severity=" + GlDebug.severityToString(this.severity) + ", message='" + this.message + "'";
      }
   }
}