package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FontTexture extends AbstractTexture implements Dumpable {
   private static final int SIZE = 256;
   private final GlyphRenderTypes renderTypes;
   private final boolean colored;
   private final FontTexture.Node root;

   public FontTexture(GlyphRenderTypes pRenderTypes, boolean pColored) {
      this.colored = pColored;
      this.root = new FontTexture.Node(0, 0, 256, 256);
      TextureUtil.prepareImage(pColored ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.RED, this.getId(), 256, 256);
      this.renderTypes = pRenderTypes;
   }

   public void load(ResourceManager pManager) {
   }

   public void close() {
      this.releaseId();
   }

   @Nullable
   public BakedGlyph add(SheetGlyphInfo pGlyphInfo) {
      if (pGlyphInfo.isColored() != this.colored) {
         return null;
      } else {
         FontTexture.Node fonttexture$node = this.root.insert(pGlyphInfo);
         if (fonttexture$node != null) {
            this.bind();
            pGlyphInfo.upload(fonttexture$node.x, fonttexture$node.y);
            float f = 256.0F;
            float f1 = 256.0F;
            float f2 = 0.01F;
            return new BakedGlyph(this.renderTypes, ((float)fonttexture$node.x + 0.01F) / 256.0F, ((float)fonttexture$node.x - 0.01F + (float)pGlyphInfo.getPixelWidth()) / 256.0F, ((float)fonttexture$node.y + 0.01F) / 256.0F, ((float)fonttexture$node.y - 0.01F + (float)pGlyphInfo.getPixelHeight()) / 256.0F, pGlyphInfo.getLeft(), pGlyphInfo.getRight(), pGlyphInfo.getUp(), pGlyphInfo.getDown());
         } else {
            return null;
         }
      }
   }

   public void dumpContents(ResourceLocation pResourceLocation, Path pPath) {
      String s = pResourceLocation.toDebugFileName();
      TextureUtil.writeAsPNG(pPath, s, this.getId(), 0, 256, 256, (p_285145_) -> {
         return (p_285145_ & -16777216) == 0 ? -16777216 : p_285145_;
      });
   }

   @OnlyIn(Dist.CLIENT)
   static class Node {
      final int x;
      final int y;
      private final int width;
      private final int height;
      @Nullable
      private FontTexture.Node left;
      @Nullable
      private FontTexture.Node right;
      private boolean occupied;

      Node(int pX, int pY, int pWidth, int pHeight) {
         this.x = pX;
         this.y = pY;
         this.width = pWidth;
         this.height = pHeight;
      }

      @Nullable
      FontTexture.Node insert(SheetGlyphInfo pGlyphInfo) {
         if (this.left != null && this.right != null) {
            FontTexture.Node fonttexture$node = this.left.insert(pGlyphInfo);
            if (fonttexture$node == null) {
               fonttexture$node = this.right.insert(pGlyphInfo);
            }

            return fonttexture$node;
         } else if (this.occupied) {
            return null;
         } else {
            int i = pGlyphInfo.getPixelWidth();
            int j = pGlyphInfo.getPixelHeight();
            if (i <= this.width && j <= this.height) {
               if (i == this.width && j == this.height) {
                  this.occupied = true;
                  return this;
               } else {
                  int k = this.width - i;
                  int l = this.height - j;
                  if (k > l) {
                     this.left = new FontTexture.Node(this.x, this.y, i, this.height);
                     this.right = new FontTexture.Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
                  } else {
                     this.left = new FontTexture.Node(this.x, this.y, this.width, j);
                     this.right = new FontTexture.Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
                  }

                  return this.left.insert(pGlyphInfo);
               }
            } else {
               return null;
            }
         }
      }
   }
}