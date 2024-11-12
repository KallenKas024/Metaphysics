package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBlockRenderer {
   private static final int FACE_CUBIC = 0;
   private static final int FACE_PARTIAL = 1;
   static final Direction[] DIRECTIONS = Direction.values();
   private final BlockColors blockColors;
   private static final int CACHE_SIZE = 100;
   static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(ModelBlockRenderer.Cache::new);

   public ModelBlockRenderer(BlockColors pBlockColors) {
      this.blockColors = pBlockColors;
   }

   /**
    * 
    * @param pCheckSides if {@code true}, only renders each side if {@link
    * net.minecraft.world.level.block.Block#shouldRenderFace(net.minecraft.world.level.block.state.BlockState,
    * net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction,
    * net.minecraft.core.BlockPos)} returns {@code true}
    */
   @Deprecated //Forge: Model data and render type parameter
   public void tesselateBlock(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay) {
      tesselateBlock(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
   }
   public void tesselateBlock(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType) {
      boolean flag = Minecraft.useAmbientOcclusion() && pState.getLightEmission(pLevel, pPos) == 0 && pModel.useAmbientOcclusion(pState, renderType);
      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      pPoseStack.translate(vec3.x, vec3.y, vec3.z);

      try {
         if (flag) {
            this.tesselateWithAO(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, modelData, renderType);
         } else {
            this.tesselateWithoutAO(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, modelData, renderType);
         }

      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block model");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block model being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, pLevel, pPos, pState);
         crashreportcategory.setDetail("Using AO", flag);
         throw new ReportedException(crashreport);
      }
   }

   /**
    * 
    * @param pCheckSides if {@code true}, only renders each side if {@link
    * net.minecraft.world.level.block.Block#shouldRenderFace(net.minecraft.world.level.block.state.BlockState,
    * net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction,
    * net.minecraft.core.BlockPos)} returns {@code true}
    */
   @Deprecated //Forge: Model data and render type parameter
   public void tesselateWithAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay) {
       tesselateWithAO(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
   }
   public void tesselateWithAO(BlockAndTintGetter p_111079_, BakedModel p_111080_, BlockState p_111081_, BlockPos p_111082_, PoseStack p_111083_, VertexConsumer p_111084_, boolean p_111085_, RandomSource p_111086_, long p_111087_, int p_111088_, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType) {
      float[] afloat = new float[DIRECTIONS.length * 2];
      BitSet bitset = new BitSet(3);
      ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = p_111082_.mutable();

      for(Direction direction : DIRECTIONS) {
         p_111086_.setSeed(p_111087_);
         List<BakedQuad> list = p_111080_.getQuads(p_111081_, direction, p_111086_, modelData, renderType);
         if (!list.isEmpty()) {
            blockpos$mutableblockpos.setWithOffset(p_111082_, direction);
            if (!p_111085_ || Block.shouldRenderFace(p_111081_, p_111079_, p_111082_, direction, blockpos$mutableblockpos)) {
               this.renderModelFaceAO(p_111079_, p_111081_, p_111082_, p_111083_, p_111084_, list, afloat, bitset, modelblockrenderer$ambientocclusionface, p_111088_);
            }
         }
      }

      p_111086_.setSeed(p_111087_);
      List<BakedQuad> list1 = p_111080_.getQuads(p_111081_, (Direction)null, p_111086_, modelData, renderType);
      if (!list1.isEmpty()) {
         this.renderModelFaceAO(p_111079_, p_111081_, p_111082_, p_111083_, p_111084_, list1, afloat, bitset, modelblockrenderer$ambientocclusionface, p_111088_);
      }

   }

   /**
    * 
    * @param pCheckSides if {@code true}, only renders each side if {@link
    * net.minecraft.world.level.block.Block#shouldRenderFace(net.minecraft.world.level.block.state.BlockState,
    * net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.core.Direction,
    * net.minecraft.core.BlockPos)} returns {@code true}
    */
   @Deprecated //Forge: Model data and render type parameter
   public void tesselateWithoutAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay) {
       tesselateWithoutAO(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
   }
   public void tesselateWithoutAO(BlockAndTintGetter p_111091_, BakedModel p_111092_, BlockState p_111093_, BlockPos p_111094_, PoseStack p_111095_, VertexConsumer p_111096_, boolean p_111097_, RandomSource p_111098_, long p_111099_, int p_111100_, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType) {
      BitSet bitset = new BitSet(3);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = p_111094_.mutable();

      for(Direction direction : DIRECTIONS) {
         p_111098_.setSeed(p_111099_);
         List<BakedQuad> list = p_111092_.getQuads(p_111093_, direction, p_111098_, modelData, renderType);
         if (!list.isEmpty()) {
            blockpos$mutableblockpos.setWithOffset(p_111094_, direction);
            if (!p_111097_ || Block.shouldRenderFace(p_111093_, p_111091_, p_111094_, direction, blockpos$mutableblockpos)) {
               int i = LevelRenderer.getLightColor(p_111091_, p_111093_, blockpos$mutableblockpos);
               this.renderModelFaceFlat(p_111091_, p_111093_, p_111094_, i, p_111100_, false, p_111095_, p_111096_, list, bitset);
            }
         }
      }

      p_111098_.setSeed(p_111099_);
      List<BakedQuad> list1 = p_111092_.getQuads(p_111093_, (Direction)null, p_111098_, modelData, renderType);
      if (!list1.isEmpty()) {
         this.renderModelFaceFlat(p_111091_, p_111093_, p_111094_, -1, p_111100_, true, p_111095_, p_111096_, list1, bitset);
      }

   }

   /**
    * 
    * @param pShape the array, of length 12, to store the shape bounds in
    * @param pShapeFlags the bit set to store the shape flags in. The first bit will be {@code true} if the face should
    * be offset, and the second if the face is less than a block in width and height.
    */
   private void renderModelFaceAO(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, List<BakedQuad> pQuads, float[] pShape, BitSet pShapeFlags, ModelBlockRenderer.AmbientOcclusionFace pAoFace, int pPackedOverlay) {
      for(BakedQuad bakedquad : pQuads) {
         this.calculateShape(pLevel, pState, pPos, bakedquad.getVertices(), bakedquad.getDirection(), pShape, pShapeFlags);
         if (!net.minecraftforge.client.ForgeHooksClient.calculateFaceWithoutAO(pLevel, pState, pPos, bakedquad, pShapeFlags.get(0), pAoFace.brightness, pAoFace.lightmap))
         pAoFace.calculate(pLevel, pState, pPos, bakedquad.getDirection(), pShape, pShapeFlags, bakedquad.isShade());
         this.putQuadData(pLevel, pState, pPos, pConsumer, pPoseStack.last(), bakedquad, pAoFace.brightness[0], pAoFace.brightness[1], pAoFace.brightness[2], pAoFace.brightness[3], pAoFace.lightmap[0], pAoFace.lightmap[1], pAoFace.lightmap[2], pAoFace.lightmap[3], pPackedOverlay);
      }

   }

   private void putQuadData(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3, int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay) {
      float f;
      float f1;
      float f2;
      if (pQuad.isTinted()) {
         int i = this.blockColors.getColor(pState, pLevel, pPos, pQuad.getTintIndex());
         f = (float)(i >> 16 & 255) / 255.0F;
         f1 = (float)(i >> 8 & 255) / 255.0F;
         f2 = (float)(i & 255) / 255.0F;
      } else {
         f = 1.0F;
         f1 = 1.0F;
         f2 = 1.0F;
      }

      pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, new int[]{pLightmap0, pLightmap1, pLightmap2, pLightmap3}, pPackedOverlay, true);
   }

   /**
    * Calculates the shape and corresponding flags for the specified {@code direction} and {@code vertices}, storing the
    * resulting shape in the specified {@code shape} array and the shape flags in {@code shapeFlags}.
    * @param pShape the array, of length 12, to store the shape bounds in, or {@code null} to only calculate shape flags
    * @param pShapeFlags the bit set to store the shape flags in. The first bit will be {@code true} if the face should
    * be offset, and the second if the face is less than a block in width and height.
    */
   private void calculateShape(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, int[] pVertices, Direction pDirection, @Nullable float[] pShape, BitSet pShapeFlags) {
      float f = 32.0F;
      float f1 = 32.0F;
      float f2 = 32.0F;
      float f3 = -32.0F;
      float f4 = -32.0F;
      float f5 = -32.0F;

      for(int i = 0; i < 4; ++i) {
         float f6 = Float.intBitsToFloat(pVertices[i * 8]);
         float f7 = Float.intBitsToFloat(pVertices[i * 8 + 1]);
         float f8 = Float.intBitsToFloat(pVertices[i * 8 + 2]);
         f = Math.min(f, f6);
         f1 = Math.min(f1, f7);
         f2 = Math.min(f2, f8);
         f3 = Math.max(f3, f6);
         f4 = Math.max(f4, f7);
         f5 = Math.max(f5, f8);
      }

      if (pShape != null) {
         pShape[Direction.WEST.get3DDataValue()] = f;
         pShape[Direction.EAST.get3DDataValue()] = f3;
         pShape[Direction.DOWN.get3DDataValue()] = f1;
         pShape[Direction.UP.get3DDataValue()] = f4;
         pShape[Direction.NORTH.get3DDataValue()] = f2;
         pShape[Direction.SOUTH.get3DDataValue()] = f5;
         int j = DIRECTIONS.length;
         pShape[Direction.WEST.get3DDataValue() + j] = 1.0F - f;
         pShape[Direction.EAST.get3DDataValue() + j] = 1.0F - f3;
         pShape[Direction.DOWN.get3DDataValue() + j] = 1.0F - f1;
         pShape[Direction.UP.get3DDataValue() + j] = 1.0F - f4;
         pShape[Direction.NORTH.get3DDataValue() + j] = 1.0F - f2;
         pShape[Direction.SOUTH.get3DDataValue() + j] = 1.0F - f5;
      }

      float f9 = 1.0E-4F;
      float f10 = 0.9999F;
      switch (pDirection) {
         case DOWN:
            pShapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f1 == f4 && (f1 < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case UP:
            pShapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f1 == f4 && (f4 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case NORTH:
            pShapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            pShapeFlags.set(0, f2 == f5 && (f2 < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case SOUTH:
            pShapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            pShapeFlags.set(0, f2 == f5 && (f5 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case WEST:
            pShapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f == f3 && (f < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case EAST:
            pShapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f == f3 && (f3 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
      }

   }

   /**
    * 
    * @param pRepackLight {@code true} if packed light should be re-calculated
    * @param pShapeFlags the bit set to store the shape flags in. The first bit will be {@code true} if the face should
    * be offset, and the second if the face is less than a block in width and height.
    */
   private void renderModelFaceFlat(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, int pPackedLight, int pPackedOverlay, boolean pRepackLight, PoseStack pPoseStack, VertexConsumer pConsumer, List<BakedQuad> pQuads, BitSet pShapeFlags) {
      for(BakedQuad bakedquad : pQuads) {
         if (pRepackLight) {
            this.calculateShape(pLevel, pState, pPos, bakedquad.getVertices(), bakedquad.getDirection(), (float[])null, pShapeFlags);
            BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(bakedquad.getDirection()) : pPos;
            pPackedLight = LevelRenderer.getLightColor(pLevel, pState, blockpos);
         }

         float f = pLevel.getShade(bakedquad.getDirection(), bakedquad.isShade());
         this.putQuadData(pLevel, pState, pPos, pConsumer, pPoseStack.last(), bakedquad, f, f, f, f, pPackedLight, pPackedLight, pPackedLight, pPackedLight, pPackedOverlay);
      }

   }

   @Deprecated //Forge: Model data and render type parameter
   public void renderModel(PoseStack.Pose pPose, VertexConsumer pConsumer, @Nullable BlockState pState, BakedModel pModel, float pRed, float pGreen, float pBlue, int pPackedLight, int pPackedOverlay) {
      renderModel(pPose, pConsumer, pState, pModel, pRed, pGreen, pBlue, pPackedLight, pPackedOverlay, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
   }
   public void renderModel(PoseStack.Pose pPose, VertexConsumer pConsumer, @Nullable BlockState pState, BakedModel pModel, float pRed, float pGreen, float pBlue, int pPackedLight, int pPackedOverlay, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType) {
      RandomSource randomsource = RandomSource.create();
      long i = 42L;

      for(Direction direction : DIRECTIONS) {
         randomsource.setSeed(42L);
         renderQuadList(pPose, pConsumer, pRed, pGreen, pBlue, pModel.getQuads(pState, direction, randomsource, modelData, renderType), pPackedLight, pPackedOverlay);
      }

      randomsource.setSeed(42L);
      renderQuadList(pPose, pConsumer, pRed, pGreen, pBlue, pModel.getQuads(pState, (Direction)null, randomsource, modelData, renderType), pPackedLight, pPackedOverlay);
   }

   private static void renderQuadList(PoseStack.Pose pPose, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, List<BakedQuad> pQuads, int pPackedLight, int pPackedOverlay) {
      for(BakedQuad bakedquad : pQuads) {
         float f;
         float f1;
         float f2;
         if (bakedquad.isTinted()) {
            f = Mth.clamp(pRed, 0.0F, 1.0F);
            f1 = Mth.clamp(pGreen, 0.0F, 1.0F);
            f2 = Mth.clamp(pBlue, 0.0F, 1.0F);
         } else {
            f = 1.0F;
            f1 = 1.0F;
            f2 = 1.0F;
         }

         pConsumer.putBulkData(pPose, bakedquad, f, f1, f2, pPackedLight, pPackedOverlay);
      }

   }

   public static void enableCaching() {
      CACHE.get().enable();
   }

   public static void clearCache() {
      CACHE.get().disable();
   }

   @OnlyIn(Dist.CLIENT)
   protected static enum AdjacencyInfo {
      DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH}),
      UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH}),
      NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST}),
      SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST}),
      WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH}),
      EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH});

      final Direction[] corners;
      final boolean doNonCubicWeight;
      final ModelBlockRenderer.SizeInfo[] vert0Weights;
      final ModelBlockRenderer.SizeInfo[] vert1Weights;
      final ModelBlockRenderer.SizeInfo[] vert2Weights;
      final ModelBlockRenderer.SizeInfo[] vert3Weights;
      private static final ModelBlockRenderer.AdjacencyInfo[] BY_FACING = Util.make(new ModelBlockRenderer.AdjacencyInfo[6], (p_111134_) -> {
         p_111134_[Direction.DOWN.get3DDataValue()] = DOWN;
         p_111134_[Direction.UP.get3DDataValue()] = UP;
         p_111134_[Direction.NORTH.get3DDataValue()] = NORTH;
         p_111134_[Direction.SOUTH.get3DDataValue()] = SOUTH;
         p_111134_[Direction.WEST.get3DDataValue()] = WEST;
         p_111134_[Direction.EAST.get3DDataValue()] = EAST;
      });

      private AdjacencyInfo(Direction[] pCorners, float pShadeBrightness, boolean pDoNonCubicWeight, ModelBlockRenderer.SizeInfo[] pVert0Weights, ModelBlockRenderer.SizeInfo[] pVert1Weights, ModelBlockRenderer.SizeInfo[] pVert2Weights, ModelBlockRenderer.SizeInfo[] pVert3Weights) {
         this.corners = pCorners;
         this.doNonCubicWeight = pDoNonCubicWeight;
         this.vert0Weights = pVert0Weights;
         this.vert1Weights = pVert1Weights;
         this.vert2Weights = pVert2Weights;
         this.vert3Weights = pVert3Weights;
      }

      public static ModelBlockRenderer.AdjacencyInfo fromFacing(Direction pFacing) {
         return BY_FACING[pFacing.get3DDataValue()];
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class AmbientOcclusionFace {
      final float[] brightness = new float[4];
      final int[] lightmap = new int[4];

      public AmbientOcclusionFace() {
      }

      /**
       * 
       * @param pShape the array, of length 12, containing the shape bounds
       * @param pShapeFlags the bit set to store the shape flags in. The first bit will be {@code true} if the face
       * should be offset, and the second if the face is less than a block in width and height.
       */
      public void calculate(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, Direction pDirection, float[] pShape, BitSet pShapeFlags, boolean pShade) {
         BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
         ModelBlockRenderer.AdjacencyInfo modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(pDirection);
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         ModelBlockRenderer.Cache modelblockrenderer$cache = ModelBlockRenderer.CACHE.get();
         blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]);
         BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
         int i = modelblockrenderer$cache.getLightColor(blockstate, pLevel, blockpos$mutableblockpos);
         float f = modelblockrenderer$cache.getShadeBrightness(blockstate, pLevel, blockpos$mutableblockpos);
         blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]);
         BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);
         int j = modelblockrenderer$cache.getLightColor(blockstate1, pLevel, blockpos$mutableblockpos);
         float f1 = modelblockrenderer$cache.getShadeBrightness(blockstate1, pLevel, blockpos$mutableblockpos);
         blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]);
         BlockState blockstate2 = pLevel.getBlockState(blockpos$mutableblockpos);
         int k = modelblockrenderer$cache.getLightColor(blockstate2, pLevel, blockpos$mutableblockpos);
         float f2 = modelblockrenderer$cache.getShadeBrightness(blockstate2, pLevel, blockpos$mutableblockpos);
         blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]);
         BlockState blockstate3 = pLevel.getBlockState(blockpos$mutableblockpos);
         int l = modelblockrenderer$cache.getLightColor(blockstate3, pLevel, blockpos$mutableblockpos);
         float f3 = modelblockrenderer$cache.getShadeBrightness(blockstate3, pLevel, blockpos$mutableblockpos);
         BlockState blockstate4 = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]).move(pDirection));
         boolean flag = !blockstate4.isViewBlocking(pLevel, blockpos$mutableblockpos) || blockstate4.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
         BlockState blockstate5 = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]).move(pDirection));
         boolean flag1 = !blockstate5.isViewBlocking(pLevel, blockpos$mutableblockpos) || blockstate5.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
         BlockState blockstate6 = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]).move(pDirection));
         boolean flag2 = !blockstate6.isViewBlocking(pLevel, blockpos$mutableblockpos) || blockstate6.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
         BlockState blockstate7 = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]).move(pDirection));
         boolean flag3 = !blockstate7.isViewBlocking(pLevel, blockpos$mutableblockpos) || blockstate7.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
         float f4;
         int i1;
         if (!flag2 && !flag) {
            f4 = f;
            i1 = i;
         } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate8 = pLevel.getBlockState(blockpos$mutableblockpos);
            f4 = modelblockrenderer$cache.getShadeBrightness(blockstate8, pLevel, blockpos$mutableblockpos);
            i1 = modelblockrenderer$cache.getLightColor(blockstate8, pLevel, blockpos$mutableblockpos);
         }

         float f5;
         int j1;
         if (!flag3 && !flag) {
            f5 = f;
            j1 = i;
         } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate10 = pLevel.getBlockState(blockpos$mutableblockpos);
            f5 = modelblockrenderer$cache.getShadeBrightness(blockstate10, pLevel, blockpos$mutableblockpos);
            j1 = modelblockrenderer$cache.getLightColor(blockstate10, pLevel, blockpos$mutableblockpos);
         }

         float f6;
         int k1;
         if (!flag2 && !flag1) {
            f6 = f;
            k1 = i;
         } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate11 = pLevel.getBlockState(blockpos$mutableblockpos);
            f6 = modelblockrenderer$cache.getShadeBrightness(blockstate11, pLevel, blockpos$mutableblockpos);
            k1 = modelblockrenderer$cache.getLightColor(blockstate11, pLevel, blockpos$mutableblockpos);
         }

         float f7;
         int l1;
         if (!flag3 && !flag1) {
            f7 = f;
            l1 = i;
         } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate12 = pLevel.getBlockState(blockpos$mutableblockpos);
            f7 = modelblockrenderer$cache.getShadeBrightness(blockstate12, pLevel, blockpos$mutableblockpos);
            l1 = modelblockrenderer$cache.getLightColor(blockstate12, pLevel, blockpos$mutableblockpos);
         }

         int i3 = modelblockrenderer$cache.getLightColor(pState, pLevel, pPos);
         blockpos$mutableblockpos.setWithOffset(pPos, pDirection);
         BlockState blockstate9 = pLevel.getBlockState(blockpos$mutableblockpos);
         if (pShapeFlags.get(0) || !blockstate9.isSolidRender(pLevel, blockpos$mutableblockpos)) {
            i3 = modelblockrenderer$cache.getLightColor(blockstate9, pLevel, blockpos$mutableblockpos);
         }

         float f8 = pShapeFlags.get(0) ? modelblockrenderer$cache.getShadeBrightness(pLevel.getBlockState(blockpos), pLevel, blockpos) : modelblockrenderer$cache.getShadeBrightness(pLevel.getBlockState(pPos), pLevel, pPos);
         ModelBlockRenderer.AmbientVertexRemap modelblockrenderer$ambientvertexremap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(pDirection);
         if (pShapeFlags.get(1) && modelblockrenderer$adjacencyinfo.doNonCubicWeight) {
            float f29 = (f3 + f + f5 + f8) * 0.25F;
            float f31 = (f2 + f + f4 + f8) * 0.25F;
            float f32 = (f2 + f1 + f6 + f8) * 0.25F;
            float f33 = (f3 + f1 + f7 + f8) * 0.25F;
            float f13 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[1].shape];
            float f14 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[3].shape];
            float f15 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[5].shape];
            float f16 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[7].shape];
            float f17 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[1].shape];
            float f18 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[3].shape];
            float f19 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[5].shape];
            float f20 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[7].shape];
            float f21 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[1].shape];
            float f22 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[3].shape];
            float f23 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[5].shape];
            float f24 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[7].shape];
            float f25 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[1].shape];
            float f26 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[3].shape];
            float f27 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[5].shape];
            float f28 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[7].shape];
            this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f29 * f13 + f31 * f14 + f32 * f15 + f33 * f16;
            this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f29 * f17 + f31 * f18 + f32 * f19 + f33 * f20;
            this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f29 * f21 + f31 * f22 + f32 * f23 + f33 * f24;
            this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f29 * f25 + f31 * f26 + f32 * f27 + f33 * f28;
            int i2 = this.blend(l, i, j1, i3);
            int j2 = this.blend(k, i, i1, i3);
            int k2 = this.blend(k, j, k1, i3);
            int l2 = this.blend(l, j, l1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(i2, j2, k2, l2, f13, f14, f15, f16);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(i2, j2, k2, l2, f17, f18, f19, f20);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(i2, j2, k2, l2, f21, f22, f23, f24);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(i2, j2, k2, l2, f25, f26, f27, f28);
         } else {
            float f9 = (f3 + f + f5 + f8) * 0.25F;
            float f10 = (f2 + f + f4 + f8) * 0.25F;
            float f11 = (f2 + f1 + f6 + f8) * 0.25F;
            float f12 = (f3 + f1 + f7 + f8) * 0.25F;
            this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(l, i, j1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(k, i, i1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(k, j, k1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(l, j, l1, i3);
            this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f9;
            this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f10;
            this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f11;
            this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f12;
         }

         float f30 = pLevel.getShade(pDirection, pShade);

         for(int j3 = 0; j3 < this.brightness.length; ++j3) {
            this.brightness[j3] *= f30;
         }

      }

      /**
       * @return the ambient occlusion light color
       */
      private int blend(int pLightColor0, int pLightColor1, int pLightColor2, int pLightColor3) {
         if (pLightColor0 == 0) {
            pLightColor0 = pLightColor3;
         }

         if (pLightColor1 == 0) {
            pLightColor1 = pLightColor3;
         }

         if (pLightColor2 == 0) {
            pLightColor2 = pLightColor3;
         }

         return pLightColor0 + pLightColor1 + pLightColor2 + pLightColor3 >> 2 & 16711935;
      }

      private int blend(int pBrightness0, int pBrightness1, int pBrightness2, int pBrightness3, float pWeight0, float pWeight1, float pWeight2, float pWeight3) {
         int i = (int)((float)(pBrightness0 >> 16 & 255) * pWeight0 + (float)(pBrightness1 >> 16 & 255) * pWeight1 + (float)(pBrightness2 >> 16 & 255) * pWeight2 + (float)(pBrightness3 >> 16 & 255) * pWeight3) & 255;
         int j = (int)((float)(pBrightness0 & 255) * pWeight0 + (float)(pBrightness1 & 255) * pWeight1 + (float)(pBrightness2 & 255) * pWeight2 + (float)(pBrightness3 & 255) * pWeight3) & 255;
         return i << 16 | j;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static enum AmbientVertexRemap {
      DOWN(0, 1, 2, 3),
      UP(2, 3, 0, 1),
      NORTH(3, 0, 1, 2),
      SOUTH(0, 1, 2, 3),
      WEST(3, 0, 1, 2),
      EAST(1, 2, 3, 0);

      final int vert0;
      final int vert1;
      final int vert2;
      final int vert3;
      private static final ModelBlockRenderer.AmbientVertexRemap[] BY_FACING = Util.make(new ModelBlockRenderer.AmbientVertexRemap[6], (p_111204_) -> {
         p_111204_[Direction.DOWN.get3DDataValue()] = DOWN;
         p_111204_[Direction.UP.get3DDataValue()] = UP;
         p_111204_[Direction.NORTH.get3DDataValue()] = NORTH;
         p_111204_[Direction.SOUTH.get3DDataValue()] = SOUTH;
         p_111204_[Direction.WEST.get3DDataValue()] = WEST;
         p_111204_[Direction.EAST.get3DDataValue()] = EAST;
      });

      private AmbientVertexRemap(int pVert0, int pVert1, int pVert2, int pVert3) {
         this.vert0 = pVert0;
         this.vert1 = pVert1;
         this.vert2 = pVert2;
         this.vert3 = pVert3;
      }

      public static ModelBlockRenderer.AmbientVertexRemap fromFacing(Direction pFacing) {
         return BY_FACING[pFacing.get3DDataValue()];
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class Cache {
      private boolean enabled;
      private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
         Long2IntLinkedOpenHashMap long2intlinkedopenhashmap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int p_111238_) {
            }
         };
         long2intlinkedopenhashmap.defaultReturnValue(Integer.MAX_VALUE);
         return long2intlinkedopenhashmap;
      });
      private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
         Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int p_111245_) {
            }
         };
         long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
         return long2floatlinkedopenhashmap;
      });

      private Cache() {
      }

      public void enable() {
         this.enabled = true;
      }

      public void disable() {
         this.enabled = false;
         this.colorCache.clear();
         this.brightnessCache.clear();
      }

      public int getLightColor(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos) {
         long i = pPos.asLong();
         if (this.enabled) {
            int j = this.colorCache.get(i);
            if (j != Integer.MAX_VALUE) {
               return j;
            }
         }

         int k = LevelRenderer.getLightColor(pLevel, pState, pPos);
         if (this.enabled) {
            if (this.colorCache.size() == 100) {
               this.colorCache.removeFirstInt();
            }

            this.colorCache.put(i, k);
         }

         return k;
      }

      public float getShadeBrightness(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos) {
         long i = pPos.asLong();
         if (this.enabled) {
            float f = this.brightnessCache.get(i);
            if (!Float.isNaN(f)) {
               return f;
            }
         }

         float f1 = pState.getShadeBrightness(pLevel, pPos);
         if (this.enabled) {
            if (this.brightnessCache.size() == 100) {
               this.brightnessCache.removeFirstFloat();
            }

            this.brightnessCache.put(i, f1);
         }

         return f1;
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected static enum SizeInfo {
      DOWN(Direction.DOWN, false),
      UP(Direction.UP, false),
      NORTH(Direction.NORTH, false),
      SOUTH(Direction.SOUTH, false),
      WEST(Direction.WEST, false),
      EAST(Direction.EAST, false),
      FLIP_DOWN(Direction.DOWN, true),
      FLIP_UP(Direction.UP, true),
      FLIP_NORTH(Direction.NORTH, true),
      FLIP_SOUTH(Direction.SOUTH, true),
      FLIP_WEST(Direction.WEST, true),
      FLIP_EAST(Direction.EAST, true);

      final int shape;

      private SizeInfo(Direction pDirection, boolean pFlip) {
         this.shape = pDirection.get3DDataValue() + (pFlip ? ModelBlockRenderer.DIRECTIONS.length : 0);
      }
   }
}
