package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MipmapGenerator {
   private static final int ALPHA_CUTOUT_CUTOFF = 96;
   private static final float[] POW22 = Util.make(new float[256], (p_118058_) -> {
      for(int i = 0; i < p_118058_.length; ++i) {
         p_118058_[i] = (float)Math.pow((double)((float)i / 255.0F), 2.2D);
      }

   });

   private MipmapGenerator() {
   }

   public static NativeImage[] generateMipLevels(NativeImage[] pImages, int pMipLevel) {
      if (pMipLevel + 1 <= pImages.length) {
         return pImages;
      } else {
         NativeImage[] anativeimage = new NativeImage[pMipLevel + 1];
         anativeimage[0] = pImages[0];
         boolean flag = hasTransparentPixel(anativeimage[0]);

         int maxMipmapLevel = net.minecraftforge.client.ForgeHooksClient.getMaxMipmapLevel(anativeimage[0].getWidth(), anativeimage[0].getHeight());
         for(int i = 1; i <= pMipLevel; ++i) {
            if (i < pImages.length) {
               anativeimage[i] = pImages[i];
            } else {
               NativeImage nativeimage = anativeimage[i - 1];
               // Forge: Guard against invalid texture size, because we allow generating mipmaps regardless of texture sizes
               NativeImage nativeimage1 = new NativeImage(Math.max(1, nativeimage.getWidth() >> 1), Math.max(1, nativeimage.getHeight() >> 1), false);
               if (i <= maxMipmapLevel) {
               int j = nativeimage1.getWidth();
               int k = nativeimage1.getHeight();

               for(int l = 0; l < j; ++l) {
                  for(int i1 = 0; i1 < k; ++i1) {
                     nativeimage1.setPixelRGBA(l, i1, alphaBlend(nativeimage.getPixelRGBA(l * 2 + 0, i1 * 2 + 0), nativeimage.getPixelRGBA(l * 2 + 1, i1 * 2 + 0), nativeimage.getPixelRGBA(l * 2 + 0, i1 * 2 + 1), nativeimage.getPixelRGBA(l * 2 + 1, i1 * 2 + 1), flag));
                  }
               }
               }

               anativeimage[i] = nativeimage1;
            }
         }

         return anativeimage;
      }
   }

   private static boolean hasTransparentPixel(NativeImage pImage) {
      for(int i = 0; i < pImage.getWidth(); ++i) {
         for(int j = 0; j < pImage.getHeight(); ++j) {
            if (pImage.getPixelRGBA(i, j) >> 24 == 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static int alphaBlend(int pCol0, int pCol1, int pCol2, int pCol3, boolean pTransparent) {
      if (pTransparent) {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         float f3 = 0.0F;
         if (pCol0 >> 24 != 0) {
            f += getPow22(pCol0 >> 24);
            f1 += getPow22(pCol0 >> 16);
            f2 += getPow22(pCol0 >> 8);
            f3 += getPow22(pCol0 >> 0);
         }

         if (pCol1 >> 24 != 0) {
            f += getPow22(pCol1 >> 24);
            f1 += getPow22(pCol1 >> 16);
            f2 += getPow22(pCol1 >> 8);
            f3 += getPow22(pCol1 >> 0);
         }

         if (pCol2 >> 24 != 0) {
            f += getPow22(pCol2 >> 24);
            f1 += getPow22(pCol2 >> 16);
            f2 += getPow22(pCol2 >> 8);
            f3 += getPow22(pCol2 >> 0);
         }

         if (pCol3 >> 24 != 0) {
            f += getPow22(pCol3 >> 24);
            f1 += getPow22(pCol3 >> 16);
            f2 += getPow22(pCol3 >> 8);
            f3 += getPow22(pCol3 >> 0);
         }

         f /= 4.0F;
         f1 /= 4.0F;
         f2 /= 4.0F;
         f3 /= 4.0F;
         int i1 = (int)(Math.pow((double)f, 0.45454545454545453D) * 255.0D);
         int j1 = (int)(Math.pow((double)f1, 0.45454545454545453D) * 255.0D);
         int k1 = (int)(Math.pow((double)f2, 0.45454545454545453D) * 255.0D);
         int l1 = (int)(Math.pow((double)f3, 0.45454545454545453D) * 255.0D);
         if (i1 < 96) {
            i1 = 0;
         }

         return i1 << 24 | j1 << 16 | k1 << 8 | l1;
      } else {
         int i = gammaBlend(pCol0, pCol1, pCol2, pCol3, 24);
         int j = gammaBlend(pCol0, pCol1, pCol2, pCol3, 16);
         int k = gammaBlend(pCol0, pCol1, pCol2, pCol3, 8);
         int l = gammaBlend(pCol0, pCol1, pCol2, pCol3, 0);
         return i << 24 | j << 16 | k << 8 | l;
      }
   }

   private static int gammaBlend(int pCol0, int pCol1, int pCol2, int pCol3, int pBitOffset) {
      float f = getPow22(pCol0 >> pBitOffset);
      float f1 = getPow22(pCol1 >> pBitOffset);
      float f2 = getPow22(pCol2 >> pBitOffset);
      float f3 = getPow22(pCol3 >> pBitOffset);
      float f4 = (float)((double)((float)Math.pow((double)(f + f1 + f2 + f3) * 0.25D, 0.45454545454545453D)));
      return (int)((double)f4 * 255.0D);
   }

   private static float getPow22(int pValue) {
      return POW22[pValue & 255];
   }
}
