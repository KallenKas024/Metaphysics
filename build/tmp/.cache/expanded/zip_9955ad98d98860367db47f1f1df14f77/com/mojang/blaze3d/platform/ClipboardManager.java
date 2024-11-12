package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import java.nio.ByteBuffer;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class ClipboardManager {
   public static final int FORMAT_UNAVAILABLE = 65545;
   private final ByteBuffer clipboardScratchBuffer = BufferUtils.createByteBuffer(8192);

   public String getClipboard(long pWindow, GLFWErrorCallbackI pErrorCallback) {
      GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(pErrorCallback);
      String s = GLFW.glfwGetClipboardString(pWindow);
      s = s != null ? StringDecomposer.filterBrokenSurrogates(s) : "";
      GLFWErrorCallback glfwerrorcallback1 = GLFW.glfwSetErrorCallback(glfwerrorcallback);
      if (glfwerrorcallback1 != null) {
         glfwerrorcallback1.free();
      }

      return s;
   }

   private static void pushClipboard(long pWindow, ByteBuffer pBuffer, byte[] pClipboardContent) {
      pBuffer.clear();
      pBuffer.put(pClipboardContent);
      pBuffer.put((byte)0);
      pBuffer.flip();
      GLFW.glfwSetClipboardString(pWindow, pBuffer);
   }

   public void setClipboard(long pWindow, String pClipboardContent) {
      byte[] abyte = pClipboardContent.getBytes(Charsets.UTF_8);
      int i = abyte.length + 1;
      if (i < this.clipboardScratchBuffer.capacity()) {
         pushClipboard(pWindow, this.clipboardScratchBuffer, abyte);
      } else {
         ByteBuffer bytebuffer = MemoryUtil.memAlloc(i);

         try {
            pushClipboard(pWindow, bytebuffer, abyte);
         } finally {
            MemoryUtil.memFree(bytebuffer);
         }
      }

   }
}