package com.mojang.blaze3d.platform;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFWVidMode;

@OnlyIn(Dist.CLIENT)
public final class VideoMode {
   private final int width;
   private final int height;
   private final int redBits;
   private final int greenBits;
   private final int blueBits;
   private final int refreshRate;
   private static final Pattern PATTERN = Pattern.compile("(\\d+)x(\\d+)(?:@(\\d+)(?::(\\d+))?)?");

   public VideoMode(int pWidth, int pHeight, int pRedBits, int pGreenBits, int pBlueBits, int pRefreshRate) {
      this.width = pWidth;
      this.height = pHeight;
      this.redBits = pRedBits;
      this.greenBits = pGreenBits;
      this.blueBits = pBlueBits;
      this.refreshRate = pRefreshRate;
   }

   public VideoMode(GLFWVidMode.Buffer pBufferVideoMode) {
      this.width = pBufferVideoMode.width();
      this.height = pBufferVideoMode.height();
      this.redBits = pBufferVideoMode.redBits();
      this.greenBits = pBufferVideoMode.greenBits();
      this.blueBits = pBufferVideoMode.blueBits();
      this.refreshRate = pBufferVideoMode.refreshRate();
   }

   public VideoMode(GLFWVidMode pGlfwVideoMode) {
      this.width = pGlfwVideoMode.width();
      this.height = pGlfwVideoMode.height();
      this.redBits = pGlfwVideoMode.redBits();
      this.greenBits = pGlfwVideoMode.greenBits();
      this.blueBits = pGlfwVideoMode.blueBits();
      this.refreshRate = pGlfwVideoMode.refreshRate();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getRedBits() {
      return this.redBits;
   }

   public int getGreenBits() {
      return this.greenBits;
   }

   public int getBlueBits() {
      return this.blueBits;
   }

   public int getRefreshRate() {
      return this.refreshRate;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         VideoMode videomode = (VideoMode)pOther;
         return this.width == videomode.width && this.height == videomode.height && this.redBits == videomode.redBits && this.greenBits == videomode.greenBits && this.blueBits == videomode.blueBits && this.refreshRate == videomode.refreshRate;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.width, this.height, this.redBits, this.greenBits, this.blueBits, this.refreshRate);
   }

   public String toString() {
      return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
   }

   public static Optional<VideoMode> read(@Nullable String pVideoMode) {
      if (pVideoMode == null) {
         return Optional.empty();
      } else {
         try {
            Matcher matcher = PATTERN.matcher(pVideoMode);
            if (matcher.matches()) {
               int i = Integer.parseInt(matcher.group(1));
               int j = Integer.parseInt(matcher.group(2));
               String s = matcher.group(3);
               int k;
               if (s == null) {
                  k = 60;
               } else {
                  k = Integer.parseInt(s);
               }

               String s1 = matcher.group(4);
               int l;
               if (s1 == null) {
                  l = 24;
               } else {
                  l = Integer.parseInt(s1);
               }

               int i1 = l / 3;
               return Optional.of(new VideoMode(i, j, i1, i1, i1, k));
            }
         } catch (Exception exception) {
         }

         return Optional.empty();
      }
   }

   public String write() {
      return String.format(Locale.ROOT, "%sx%s@%s:%s", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
   }
}