package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum SpecialGlyphs implements GlyphInfo {
   WHITE(() -> {
      return generate(5, 8, (p_232613_, p_232614_) -> {
         return -1;
      });
   }),
   MISSING(() -> {
      int i = 5;
      int j = 8;
      return generate(5, 8, (p_232606_, p_232607_) -> {
         boolean flag = p_232606_ == 0 || p_232606_ + 1 == 5 || p_232607_ == 0 || p_232607_ + 1 == 8;
         return flag ? -1 : 0;
      });
   });

   final NativeImage image;

   private static NativeImage generate(int pWidth, int pHeight, SpecialGlyphs.PixelProvider pPixelProvider) {
      NativeImage nativeimage = new NativeImage(NativeImage.Format.RGBA, pWidth, pHeight, false);

      for(int i = 0; i < pHeight; ++i) {
         for(int j = 0; j < pWidth; ++j) {
            nativeimage.setPixelRGBA(j, i, pPixelProvider.getColor(j, i));
         }
      }

      nativeimage.untrack();
      return nativeimage;
   }

   private SpecialGlyphs(Supplier<NativeImage> pImage) {
      this.image = pImage.get();
   }

   public float getAdvance() {
      return (float)(this.image.getWidth() + 1);
   }

   public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> pGlyphProvider) {
      return pGlyphProvider.apply(new SheetGlyphInfo() {
         public int getPixelWidth() {
            return SpecialGlyphs.this.image.getWidth();
         }

         public int getPixelHeight() {
            return SpecialGlyphs.this.image.getHeight();
         }

         public float getOversample() {
            return 1.0F;
         }

         public void upload(int p_232629_, int p_232630_) {
            SpecialGlyphs.this.image.upload(0, p_232629_, p_232630_, false);
         }

         public boolean isColored() {
            return true;
         }
      });
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   interface PixelProvider {
      int getColor(int pX, int pY);
   }
}