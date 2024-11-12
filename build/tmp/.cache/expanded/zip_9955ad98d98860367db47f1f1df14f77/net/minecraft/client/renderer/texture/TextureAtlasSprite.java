package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureAtlasSprite {
   private final ResourceLocation atlasLocation;
   private final SpriteContents contents;
   final int x;
   final int y;
   private final float u0;
   private final float u1;
   private final float v0;
   private final float v1;

   protected TextureAtlasSprite(ResourceLocation pAtlasLocation, SpriteContents pContents, int pOriginX, int pOriginY, int pX, int pY) {
      this.atlasLocation = pAtlasLocation;
      this.contents = pContents;
      this.x = pX;
      this.y = pY;
      this.u0 = (float)pX / (float)pOriginX;
      this.u1 = (float)(pX + pContents.width()) / (float)pOriginX;
      this.v0 = (float)pY / (float)pOriginY;
      this.v1 = (float)(pY + pContents.height()) / (float)pOriginY;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   /**
    * @return the minimum U coordinate to use when rendering this sprite
    */
   public float getU0() {
      return this.u0;
   }

   /**
    * @return the maximum U coordinate to use when rendering this sprite
    */
   public float getU1() {
      return this.u1;
   }

   public SpriteContents contents() {
      return this.contents;
   }

   @Nullable
   public TextureAtlasSprite.Ticker createTicker() {
      final SpriteTicker spriteticker = this.contents.createTicker();
      return spriteticker != null ? new TextureAtlasSprite.Ticker() {
         public void tickAndUpload() {
            spriteticker.tickAndUpload(TextureAtlasSprite.this.x, TextureAtlasSprite.this.y);
         }

         public void close() {
            spriteticker.close();
         }
      } : null;
   }

   /**
    * @return the specified {@code u} coordinate relative to this sprite
    */
   public float getU(double pU) {
      float f = this.u1 - this.u0;
      return this.u0 + f * (float)pU / 16.0F;
   }

   public float getUOffset(float pOffset) {
      float f = this.u1 - this.u0;
      return (pOffset - this.u0) / f * 16.0F;
   }

   /**
    * @return the minimum V coordinate to use when rendering this sprite
    */
   public float getV0() {
      return this.v0;
   }

   /**
    * @return the maximum V coordinate to use when rendering this sprite
    */
   public float getV1() {
      return this.v1;
   }

   /**
    * @return the specified {@code v} coordinate relative to this sprite
    */
   public float getV(double pV) {
      float f = this.v1 - this.v0;
      return this.v0 + f * (float)pV / 16.0F;
   }

   public float getVOffset(float pOffset) {
      float f = this.v1 - this.v0;
      return (pOffset - this.v0) / f * 16.0F;
   }

   public ResourceLocation atlasLocation() {
      return this.atlasLocation;
   }

   public String toString() {
      return "TextureAtlasSprite{contents='" + this.contents + "', u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + "}";
   }

   public void uploadFirstFrame() {
      this.contents.uploadFirstFrame(this.x, this.y);
   }

   private float atlasSize() {
      float f = (float)this.contents.width() / (this.u1 - this.u0);
      float f1 = (float)this.contents.height() / (this.v1 - this.v0);
      return Math.max(f1, f);
   }

   public float uvShrinkRatio() {
      return 4.0F / this.atlasSize();
   }

   public VertexConsumer wrap(VertexConsumer pConsumer) {
      return new SpriteCoordinateExpander(pConsumer, this);
   }

   @OnlyIn(Dist.CLIENT)
   public interface Ticker extends AutoCloseable {
      void tickAndUpload();

      void close();
   }

   // Forge Start
   public int getPixelRGBA(int frameIndex, int x, int y) {
       if (this.contents.animatedTexture != null) {
           x += this.contents.animatedTexture.getFrameX(frameIndex) * this.contents.width;
           y += this.contents.animatedTexture.getFrameY(frameIndex) * this.contents.height;
       }

       return this.contents.getOriginalImage().getPixelRGBA(x, y);
   }
}
