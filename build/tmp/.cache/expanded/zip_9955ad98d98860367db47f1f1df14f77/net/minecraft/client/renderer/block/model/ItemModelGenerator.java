package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ItemModelGenerator {
   public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");
   private static final float MIN_Z = 7.5F;
   private static final float MAX_Z = 8.5F;

   public BlockModel generateBlockModel(Function<Material, TextureAtlasSprite> pSpriteGetter, BlockModel pModel) {
      Map<String, Either<Material, String>> map = Maps.newHashMap();
      List<BlockElement> list = Lists.newArrayList();

      for(int i = 0; i < LAYERS.size(); ++i) {
         String s = LAYERS.get(i);
         if (!pModel.hasTexture(s)) {
            break;
         }

         Material material = pModel.getMaterial(s);
         map.put(s, Either.left(material));
         SpriteContents spritecontents = pSpriteGetter.apply(material).contents();
         list.addAll(this.processFrames(i, s, spritecontents));
      }

      map.put("particle", pModel.hasTexture("particle") ? Either.left(pModel.getMaterial("particle")) : map.get("layer0"));
      BlockModel blockmodel = new BlockModel((ResourceLocation)null, list, map, false, pModel.getGuiLight(), pModel.getTransforms(), pModel.getOverrides());
      blockmodel.name = pModel.name;
      blockmodel.customData.copyFrom(pModel.customData);
      blockmodel.customData.setGui3d(false);
      return blockmodel;
   }

   public List<BlockElement> processFrames(int pTintIndex, String pTexture, SpriteContents pSprite) {
      Map<Direction, BlockElementFace> map = Maps.newHashMap();
      map.put(Direction.SOUTH, new BlockElementFace((Direction)null, pTintIndex, pTexture, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
      map.put(Direction.NORTH, new BlockElementFace((Direction)null, pTintIndex, pTexture, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
      List<BlockElement> list = Lists.newArrayList();
      list.add(new BlockElement(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, (BlockElementRotation)null, true));
      list.addAll(this.createSideElements(pSprite, pTexture, pTintIndex));
      return list;
   }

   private List<BlockElement> createSideElements(SpriteContents pSprite, String pTexture, int pTintIndex) {
      float f = (float)pSprite.width();
      float f1 = (float)pSprite.height();
      List<BlockElement> list = Lists.newArrayList();

      for(ItemModelGenerator.Span itemmodelgenerator$span : this.getSpans(pSprite)) {
         float f2 = 0.0F;
         float f3 = 0.0F;
         float f4 = 0.0F;
         float f5 = 0.0F;
         float f6 = 0.0F;
         float f7 = 0.0F;
         float f8 = 0.0F;
         float f9 = 0.0F;
         float f10 = 16.0F / f;
         float f11 = 16.0F / f1;
         float f12 = (float)itemmodelgenerator$span.getMin();
         float f13 = (float)itemmodelgenerator$span.getMax();
         float f14 = (float)itemmodelgenerator$span.getAnchor();
         ItemModelGenerator.SpanFacing itemmodelgenerator$spanfacing = itemmodelgenerator$span.getFacing();
         switch (itemmodelgenerator$spanfacing) {
            case UP:
               f6 = f12;
               f2 = f12;
               f4 = f7 = f13 + 1.0F;
               f8 = f14;
               f3 = f14;
               f5 = f14;
               f9 = f14 + 1.0F;
               break;
            case DOWN:
               f8 = f14;
               f9 = f14 + 1.0F;
               f6 = f12;
               f2 = f12;
               f4 = f7 = f13 + 1.0F;
               f3 = f14 + 1.0F;
               f5 = f14 + 1.0F;
               break;
            case LEFT:
               f6 = f14;
               f2 = f14;
               f4 = f14;
               f7 = f14 + 1.0F;
               f9 = f12;
               f3 = f12;
               f5 = f8 = f13 + 1.0F;
               break;
            case RIGHT:
               f6 = f14;
               f7 = f14 + 1.0F;
               f2 = f14 + 1.0F;
               f4 = f14 + 1.0F;
               f9 = f12;
               f3 = f12;
               f5 = f8 = f13 + 1.0F;
         }

         f2 *= f10;
         f4 *= f10;
         f3 *= f11;
         f5 *= f11;
         f3 = 16.0F - f3;
         f5 = 16.0F - f5;
         f6 *= f10;
         f7 *= f10;
         f8 *= f11;
         f9 *= f11;
         Map<Direction, BlockElementFace> map = Maps.newHashMap();
         map.put(itemmodelgenerator$spanfacing.getDirection(), new BlockElementFace((Direction)null, pTintIndex, pTexture, new BlockFaceUV(new float[]{f6, f8, f7, f9}, 0)));
         switch (itemmodelgenerator$spanfacing) {
            case UP:
               list.add(new BlockElement(new Vector3f(f2, f3, 7.5F), new Vector3f(f4, f3, 8.5F), map, (BlockElementRotation)null, true));
               break;
            case DOWN:
               list.add(new BlockElement(new Vector3f(f2, f5, 7.5F), new Vector3f(f4, f5, 8.5F), map, (BlockElementRotation)null, true));
               break;
            case LEFT:
               list.add(new BlockElement(new Vector3f(f2, f3, 7.5F), new Vector3f(f2, f5, 8.5F), map, (BlockElementRotation)null, true));
               break;
            case RIGHT:
               list.add(new BlockElement(new Vector3f(f4, f3, 7.5F), new Vector3f(f4, f5, 8.5F), map, (BlockElementRotation)null, true));
         }
      }

      return list;
   }

   private List<ItemModelGenerator.Span> getSpans(SpriteContents pSprite) {
      int i = pSprite.width();
      int j = pSprite.height();
      List<ItemModelGenerator.Span> list = Lists.newArrayList();
      pSprite.getUniqueFrames().forEach((p_173444_) -> {
         for(int k = 0; k < j; ++k) {
            for(int l = 0; l < i; ++l) {
               boolean flag = !this.isTransparent(pSprite, p_173444_, l, k, i, j);
               this.checkTransition(ItemModelGenerator.SpanFacing.UP, list, pSprite, p_173444_, l, k, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, list, pSprite, p_173444_, l, k, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, list, pSprite, p_173444_, l, k, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, list, pSprite, p_173444_, l, k, i, j, flag);
            }
         }

      });
      return list;
   }

   private void checkTransition(ItemModelGenerator.SpanFacing pSpanFacing, List<ItemModelGenerator.Span> pListSpans, SpriteContents pContents, int pFrameIndex, int pPixelX, int pPixelY, int pSpriteWidth, int pSpriteHeight, boolean pTransparent) {
      boolean flag = this.isTransparent(pContents, pFrameIndex, pPixelX + pSpanFacing.getXOffset(), pPixelY + pSpanFacing.getYOffset(), pSpriteWidth, pSpriteHeight) && pTransparent;
      if (flag) {
         this.createOrExpandSpan(pListSpans, pSpanFacing, pPixelX, pPixelY);
      }

   }

   private void createOrExpandSpan(List<ItemModelGenerator.Span> pListSpans, ItemModelGenerator.SpanFacing pSpanFacing, int pPixelX, int pPixelY) {
      ItemModelGenerator.Span itemmodelgenerator$span = null;

      for(ItemModelGenerator.Span itemmodelgenerator$span1 : pListSpans) {
         if (itemmodelgenerator$span1.getFacing() == pSpanFacing) {
            int i = pSpanFacing.isHorizontal() ? pPixelY : pPixelX;
            if (itemmodelgenerator$span1.getAnchor() == i) {
               itemmodelgenerator$span = itemmodelgenerator$span1;
               break;
            }
         }
      }

      int j = pSpanFacing.isHorizontal() ? pPixelY : pPixelX;
      int k = pSpanFacing.isHorizontal() ? pPixelX : pPixelY;
      if (itemmodelgenerator$span == null) {
         pListSpans.add(new ItemModelGenerator.Span(pSpanFacing, k, j));
      } else {
         itemmodelgenerator$span.expand(k);
      }

   }

   private boolean isTransparent(SpriteContents pSprite, int pFrameIndex, int pPixelX, int pPixelY, int pSpriteWidth, int pSpriteHeight) {
      return pPixelX >= 0 && pPixelY >= 0 && pPixelX < pSpriteWidth && pPixelY < pSpriteHeight ? pSprite.isTransparent(pFrameIndex, pPixelX, pPixelY) : true;
   }

   @OnlyIn(Dist.CLIENT)
   static class Span {
      private final ItemModelGenerator.SpanFacing facing;
      private int min;
      private int max;
      private final int anchor;

      public Span(ItemModelGenerator.SpanFacing pFacing, int pMinMax, int pAnchor) {
         this.facing = pFacing;
         this.min = pMinMax;
         this.max = pMinMax;
         this.anchor = pAnchor;
      }

      public void expand(int pPos) {
         if (pPos < this.min) {
            this.min = pPos;
         } else if (pPos > this.max) {
            this.max = pPos;
         }

      }

      public ItemModelGenerator.SpanFacing getFacing() {
         return this.facing;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public int getAnchor() {
         return this.anchor;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static enum SpanFacing {
      UP(Direction.UP, 0, -1),
      DOWN(Direction.DOWN, 0, 1),
      LEFT(Direction.EAST, -1, 0),
      RIGHT(Direction.WEST, 1, 0);

      private final Direction direction;
      private final int xOffset;
      private final int yOffset;

      private SpanFacing(Direction pDirection, int pXOffset, int pYOffset) {
         this.direction = pDirection;
         this.xOffset = pXOffset;
         this.yOffset = pYOffset;
      }

      /**
       * Gets the direction of the block's facing.
       */
      public Direction getDirection() {
         return this.direction;
      }

      public int getXOffset() {
         return this.xOffset;
      }

      public int getYOffset() {
         return this.yOffset;
      }

      boolean isHorizontal() {
         return this == DOWN || this == UP;
      }
   }
}
