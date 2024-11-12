package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlendMode {
   @Nullable
   private static BlendMode lastApplied;
   private final int srcColorFactor;
   private final int srcAlphaFactor;
   private final int dstColorFactor;
   private final int dstAlphaFactor;
   private final int blendFunc;
   private final boolean separateBlend;
   private final boolean opaque;

   private BlendMode(boolean pSeparateBlend, boolean pOpaque, int pSrcColorFactor, int pDstColorFactor, int pSrcAlphaFactor, int pDstAlphaFactor, int pBlendFunc) {
      this.separateBlend = pSeparateBlend;
      this.srcColorFactor = pSrcColorFactor;
      this.dstColorFactor = pDstColorFactor;
      this.srcAlphaFactor = pSrcAlphaFactor;
      this.dstAlphaFactor = pDstAlphaFactor;
      this.opaque = pOpaque;
      this.blendFunc = pBlendFunc;
   }

   public BlendMode() {
      this(false, true, 1, 0, 1, 0, 32774);
   }

   public BlendMode(int pSrcFactor, int pDstFactor, int pBlendFunc) {
      this(false, false, pSrcFactor, pDstFactor, pSrcFactor, pDstFactor, pBlendFunc);
   }

   public BlendMode(int pSrcColorFactor, int pDstColorFactor, int pSrcAlphaFactor, int pDstAlphaFactor, int pBlendFunc) {
      this(true, false, pSrcColorFactor, pDstColorFactor, pSrcAlphaFactor, pDstAlphaFactor, pBlendFunc);
   }

   public void apply() {
      if (!this.equals(lastApplied)) {
         if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
            lastApplied = this;
            if (this.opaque) {
               RenderSystem.disableBlend();
               return;
            }

            RenderSystem.enableBlend();
         }

         RenderSystem.blendEquation(this.blendFunc);
         if (this.separateBlend) {
            RenderSystem.blendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
         } else {
            RenderSystem.blendFunc(this.srcColorFactor, this.dstColorFactor);
         }

      }
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof BlendMode)) {
         return false;
      } else {
         BlendMode blendmode = (BlendMode)pOther;
         if (this.blendFunc != blendmode.blendFunc) {
            return false;
         } else if (this.dstAlphaFactor != blendmode.dstAlphaFactor) {
            return false;
         } else if (this.dstColorFactor != blendmode.dstColorFactor) {
            return false;
         } else if (this.opaque != blendmode.opaque) {
            return false;
         } else if (this.separateBlend != blendmode.separateBlend) {
            return false;
         } else if (this.srcAlphaFactor != blendmode.srcAlphaFactor) {
            return false;
         } else {
            return this.srcColorFactor == blendmode.srcColorFactor;
         }
      }
   }

   public int hashCode() {
      int i = this.srcColorFactor;
      i = 31 * i + this.srcAlphaFactor;
      i = 31 * i + this.dstColorFactor;
      i = 31 * i + this.dstAlphaFactor;
      i = 31 * i + this.blendFunc;
      i = 31 * i + (this.separateBlend ? 1 : 0);
      return 31 * i + (this.opaque ? 1 : 0);
   }

   public boolean isOpaque() {
      return this.opaque;
   }

   /**
    * Converts a blend function name to an id, returning add (32774) if not recognized.
    */
   public static int stringToBlendFunc(String pFuncName) {
      String s = pFuncName.trim().toLowerCase(Locale.ROOT);
      if ("add".equals(s)) {
         return 32774;
      } else if ("subtract".equals(s)) {
         return 32778;
      } else if ("reversesubtract".equals(s)) {
         return 32779;
      } else if ("reverse_subtract".equals(s)) {
         return 32779;
      } else if ("min".equals(s)) {
         return 32775;
      } else {
         return "max".equals(s) ? '\u8008' : '\u8006';
      }
   }

   public static int stringToBlendFactor(String pFactorName) {
      String s = pFactorName.trim().toLowerCase(Locale.ROOT);
      s = s.replaceAll("_", "");
      s = s.replaceAll("one", "1");
      s = s.replaceAll("zero", "0");
      s = s.replaceAll("minus", "-");
      if ("0".equals(s)) {
         return 0;
      } else if ("1".equals(s)) {
         return 1;
      } else if ("srccolor".equals(s)) {
         return 768;
      } else if ("1-srccolor".equals(s)) {
         return 769;
      } else if ("dstcolor".equals(s)) {
         return 774;
      } else if ("1-dstcolor".equals(s)) {
         return 775;
      } else if ("srcalpha".equals(s)) {
         return 770;
      } else if ("1-srcalpha".equals(s)) {
         return 771;
      } else if ("dstalpha".equals(s)) {
         return 772;
      } else {
         return "1-dstalpha".equals(s) ? 773 : -1;
      }
   }
}