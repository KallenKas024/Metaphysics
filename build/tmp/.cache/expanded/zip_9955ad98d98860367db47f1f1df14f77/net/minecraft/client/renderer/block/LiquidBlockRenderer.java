package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LiquidBlockRenderer {
   private static final float MAX_FLUID_HEIGHT = 0.8888889F;
   private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
   private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
   private TextureAtlasSprite waterOverlay;

   protected void setupSprites() {
      this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
      this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
      this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
      this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
      this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
   }

   private static boolean isNeighborSameFluid(FluidState pFirstState, FluidState pSecondState) {
      return pSecondState.getType().isSame(pFirstState.getType());
   }

   private static boolean isFaceOccludedByState(BlockGetter pLevel, Direction pFace, float pHeight, BlockPos pPos, BlockState pState) {
      if (pState.canOcclude()) {
         VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)pHeight, 1.0D);
         VoxelShape voxelshape1 = pState.getOcclusionShape(pLevel, pPos);
         return Shapes.blockOccudes(voxelshape, voxelshape1, pFace);
      } else {
         return false;
      }
   }

   private static boolean isFaceOccludedByNeighbor(BlockGetter pLevel, BlockPos pPos, Direction pSide, float pHeight, BlockState pBlockState) {
      return isFaceOccludedByState(pLevel, pSide, pHeight, pPos.relative(pSide), pBlockState);
   }

   private static boolean isFaceOccludedBySelf(BlockGetter pLevel, BlockPos pPos, BlockState pState, Direction pFace) {
      return isFaceOccludedByState(pLevel, pFace.getOpposite(), 1.0F, pPos, pState);
   }

   public static boolean shouldRenderFace(BlockAndTintGetter pLevel, BlockPos pPos, FluidState pFluidState, BlockState pBlockState, Direction pSide, FluidState pNeighborFluid) {
      return !isFaceOccludedBySelf(pLevel, pPos, pBlockState, pSide) && !isNeighborSameFluid(pFluidState, pNeighborFluid);
   }

   public void tesselate(BlockAndTintGetter pLevel, BlockPos pPos, VertexConsumer pVertexConsumer, BlockState pBlockState, FluidState pFluidState) {
      boolean flag = pFluidState.is(FluidTags.LAVA);
      TextureAtlasSprite[] atextureatlassprite = net.minecraftforge.client.ForgeHooksClient.getFluidSprites(pLevel, pPos, pFluidState);
      int i = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(pFluidState).getTintColor(pFluidState, pLevel, pPos);
      float alpha = (float)(i >> 24 & 255) / 255.0F;
      float f = (float)(i >> 16 & 255) / 255.0F;
      float f1 = (float)(i >> 8 & 255) / 255.0F;
      float f2 = (float)(i & 255) / 255.0F;
      BlockState blockstate = pLevel.getBlockState(pPos.relative(Direction.DOWN));
      FluidState fluidstate = blockstate.getFluidState();
      BlockState blockstate1 = pLevel.getBlockState(pPos.relative(Direction.UP));
      FluidState fluidstate1 = blockstate1.getFluidState();
      BlockState blockstate2 = pLevel.getBlockState(pPos.relative(Direction.NORTH));
      FluidState fluidstate2 = blockstate2.getFluidState();
      BlockState blockstate3 = pLevel.getBlockState(pPos.relative(Direction.SOUTH));
      FluidState fluidstate3 = blockstate3.getFluidState();
      BlockState blockstate4 = pLevel.getBlockState(pPos.relative(Direction.WEST));
      FluidState fluidstate4 = blockstate4.getFluidState();
      BlockState blockstate5 = pLevel.getBlockState(pPos.relative(Direction.EAST));
      FluidState fluidstate5 = blockstate5.getFluidState();
      boolean flag1 = !isNeighborSameFluid(pFluidState, fluidstate1);
      boolean flag2 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.DOWN, fluidstate) && !isFaceOccludedByNeighbor(pLevel, pPos, Direction.DOWN, 0.8888889F, blockstate);
      boolean flag3 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.NORTH, fluidstate2);
      boolean flag4 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.SOUTH, fluidstate3);
      boolean flag5 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.WEST, fluidstate4);
      boolean flag6 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.EAST, fluidstate5);
      if (flag1 || flag2 || flag6 || flag5 || flag3 || flag4) {
         float f3 = pLevel.getShade(Direction.DOWN, true);
         float f4 = pLevel.getShade(Direction.UP, true);
         float f5 = pLevel.getShade(Direction.NORTH, true);
         float f6 = pLevel.getShade(Direction.WEST, true);
         Fluid fluid = pFluidState.getType();
         float f11 = this.getHeight(pLevel, fluid, pPos, pBlockState, pFluidState);
         float f7;
         float f8;
         float f9;
         float f10;
         if (f11 >= 1.0F) {
            f7 = 1.0F;
            f8 = 1.0F;
            f9 = 1.0F;
            f10 = 1.0F;
         } else {
            float f12 = this.getHeight(pLevel, fluid, pPos.north(), blockstate2, fluidstate2);
            float f13 = this.getHeight(pLevel, fluid, pPos.south(), blockstate3, fluidstate3);
            float f14 = this.getHeight(pLevel, fluid, pPos.east(), blockstate5, fluidstate5);
            float f15 = this.getHeight(pLevel, fluid, pPos.west(), blockstate4, fluidstate4);
            f7 = this.calculateAverageHeight(pLevel, fluid, f11, f12, f14, pPos.relative(Direction.NORTH).relative(Direction.EAST));
            f8 = this.calculateAverageHeight(pLevel, fluid, f11, f12, f15, pPos.relative(Direction.NORTH).relative(Direction.WEST));
            f9 = this.calculateAverageHeight(pLevel, fluid, f11, f13, f14, pPos.relative(Direction.SOUTH).relative(Direction.EAST));
            f10 = this.calculateAverageHeight(pLevel, fluid, f11, f13, f15, pPos.relative(Direction.SOUTH).relative(Direction.WEST));
         }

         double d1 = (double)(pPos.getX() & 15);
         double d2 = (double)(pPos.getY() & 15);
         double d0 = (double)(pPos.getZ() & 15);
         float f16 = 0.001F;
         float f17 = flag2 ? 0.001F : 0.0F;
         if (flag1 && !isFaceOccludedByNeighbor(pLevel, pPos, Direction.UP, Math.min(Math.min(f8, f10), Math.min(f9, f7)), blockstate1)) {
            f8 -= 0.001F;
            f10 -= 0.001F;
            f9 -= 0.001F;
            f7 -= 0.001F;
            Vec3 vec3 = pFluidState.getFlow(pLevel, pPos);
            float f18;
            float f19;
            float f20;
            float f21;
            float f22;
            float f23;
            float f24;
            float f25;
            if (vec3.x == 0.0D && vec3.z == 0.0D) {
               TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
               f18 = textureatlassprite1.getU(0.0D);
               f22 = textureatlassprite1.getV(0.0D);
               f19 = f18;
               f23 = textureatlassprite1.getV(16.0D);
               f20 = textureatlassprite1.getU(16.0D);
               f24 = f23;
               f21 = f20;
               f25 = f22;
            } else {
               TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
               float f26 = (float)Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
               float f27 = Mth.sin(f26) * 0.25F;
               float f28 = Mth.cos(f26) * 0.25F;
               float f29 = 8.0F;
               f18 = textureatlassprite.getU((double)(8.0F + (-f28 - f27) * 16.0F));
               f22 = textureatlassprite.getV((double)(8.0F + (-f28 + f27) * 16.0F));
               f19 = textureatlassprite.getU((double)(8.0F + (-f28 + f27) * 16.0F));
               f23 = textureatlassprite.getV((double)(8.0F + (f28 + f27) * 16.0F));
               f20 = textureatlassprite.getU((double)(8.0F + (f28 + f27) * 16.0F));
               f24 = textureatlassprite.getV((double)(8.0F + (f28 - f27) * 16.0F));
               f21 = textureatlassprite.getU((double)(8.0F + (f28 - f27) * 16.0F));
               f25 = textureatlassprite.getV((double)(8.0F + (-f28 - f27) * 16.0F));
            }

            float f49 = (f18 + f19 + f20 + f21) / 4.0F;
            float f50 = (f22 + f23 + f24 + f25) / 4.0F;
            float f51 = atextureatlassprite[0].uvShrinkRatio();
            f18 = Mth.lerp(f51, f18, f49);
            f19 = Mth.lerp(f51, f19, f49);
            f20 = Mth.lerp(f51, f20, f49);
            f21 = Mth.lerp(f51, f21, f49);
            f22 = Mth.lerp(f51, f22, f50);
            f23 = Mth.lerp(f51, f23, f50);
            f24 = Mth.lerp(f51, f24, f50);
            f25 = Mth.lerp(f51, f25, f50);
            int l = this.getLightColor(pLevel, pPos);
            float f52 = f4 * f;
            float f30 = f4 * f1;
            float f31 = f4 * f2;
            this.vertex(pVertexConsumer, d1 + 0.0D, d2 + (double)f8, d0 + 0.0D, f52, f30, f31, alpha, f18, f22, l);
            this.vertex(pVertexConsumer, d1 + 0.0D, d2 + (double)f10, d0 + 1.0D, f52, f30, f31, alpha, f19, f23, l);
            this.vertex(pVertexConsumer, d1 + 1.0D, d2 + (double)f9, d0 + 1.0D, f52, f30, f31, alpha, f20, f24, l);
            this.vertex(pVertexConsumer, d1 + 1.0D, d2 + (double)f7, d0 + 0.0D, f52, f30, f31, alpha, f21, f25, l);
            if (pFluidState.shouldRenderBackwardUpFace(pLevel, pPos.above())) {
               this.vertex(pVertexConsumer, d1 + 0.0D, d2 + (double)f8, d0 + 0.0D, f52, f30, f31, alpha, f18, f22, l);
               this.vertex(pVertexConsumer, d1 + 1.0D, d2 + (double)f7, d0 + 0.0D, f52, f30, f31, alpha, f21, f25, l);
               this.vertex(pVertexConsumer, d1 + 1.0D, d2 + (double)f9, d0 + 1.0D, f52, f30, f31, alpha, f20, f24, l);
               this.vertex(pVertexConsumer, d1 + 0.0D, d2 + (double)f10, d0 + 1.0D, f52, f30, f31, alpha, f19, f23, l);
            }
         }

         if (flag2) {
            float f40 = atextureatlassprite[0].getU0();
            float f41 = atextureatlassprite[0].getU1();
            float f42 = atextureatlassprite[0].getV0();
            float f43 = atextureatlassprite[0].getV1();
            int k = this.getLightColor(pLevel, pPos.below());
            float f46 = f3 * f;
            float f47 = f3 * f1;
            float f48 = f3 * f2;
            this.vertex(pVertexConsumer, d1, d2 + (double)f17, d0 + 1.0D, f46, f47, f48, alpha, f40, f43, k);
            this.vertex(pVertexConsumer, d1, d2 + (double)f17, d0, f46, f47, f48, alpha, f40, f42, k);
            this.vertex(pVertexConsumer, d1 + 1.0D, d2 + (double)f17, d0, f46, f47, f48, alpha, f41, f42, k);
            this.vertex(pVertexConsumer, d1 + 1.0D, d2 + (double)f17, d0 + 1.0D, f46, f47, f48, alpha, f41, f43, k);
         }

         int j = this.getLightColor(pLevel, pPos);

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            float f44;
            float f45;
            double d3;
            double d4;
            double d5;
            double d6;
            boolean flag7;
            switch (direction) {
               case NORTH:
                  f44 = f8;
                  f45 = f7;
                  d3 = d1;
                  d5 = d1 + 1.0D;
                  d4 = d0 + (double)0.001F;
                  d6 = d0 + (double)0.001F;
                  flag7 = flag3;
                  break;
               case SOUTH:
                  f44 = f9;
                  f45 = f10;
                  d3 = d1 + 1.0D;
                  d5 = d1;
                  d4 = d0 + 1.0D - (double)0.001F;
                  d6 = d0 + 1.0D - (double)0.001F;
                  flag7 = flag4;
                  break;
               case WEST:
                  f44 = f10;
                  f45 = f8;
                  d3 = d1 + (double)0.001F;
                  d5 = d1 + (double)0.001F;
                  d4 = d0 + 1.0D;
                  d6 = d0;
                  flag7 = flag5;
                  break;
               default:
                  f44 = f7;
                  f45 = f9;
                  d3 = d1 + 1.0D - (double)0.001F;
                  d5 = d1 + 1.0D - (double)0.001F;
                  d4 = d0;
                  d6 = d0 + 1.0D;
                  flag7 = flag6;
            }

            if (flag7 && !isFaceOccludedByNeighbor(pLevel, pPos, direction, Math.max(f44, f45), pLevel.getBlockState(pPos.relative(direction)))) {
               BlockPos blockpos = pPos.relative(direction);
               TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
               if (atextureatlassprite[2] != null) {
                  if (pLevel.getBlockState(blockpos).shouldDisplayFluidOverlay(pLevel, blockpos, pFluidState)) {
                     textureatlassprite2 = atextureatlassprite[2];
                  }
               }

               float f53 = textureatlassprite2.getU(0.0D);
               float f32 = textureatlassprite2.getU(8.0D);
               float f33 = textureatlassprite2.getV((double)((1.0F - f44) * 16.0F * 0.5F));
               float f34 = textureatlassprite2.getV((double)((1.0F - f45) * 16.0F * 0.5F));
               float f35 = textureatlassprite2.getV(8.0D);
               float f36 = direction.getAxis() == Direction.Axis.Z ? f5 : f6;
               float f37 = f4 * f36 * f;
               float f38 = f4 * f36 * f1;
               float f39 = f4 * f36 * f2;
               this.vertex(pVertexConsumer, d3, d2 + (double)f44, d4, f37, f38, f39, alpha, f53, f33, j);
               this.vertex(pVertexConsumer, d5, d2 + (double)f45, d6, f37, f38, f39, alpha, f32, f34, j);
               this.vertex(pVertexConsumer, d5, d2 + (double)f17, d6, f37, f38, f39, alpha, f32, f35, j);
               this.vertex(pVertexConsumer, d3, d2 + (double)f17, d4, f37, f38, f39, alpha, f53, f35, j);
               if (textureatlassprite2 != this.waterOverlay) {
                  this.vertex(pVertexConsumer, d3, d2 + (double)f17, d4, f37, f38, f39, alpha, f53, f35, j);
                  this.vertex(pVertexConsumer, d5, d2 + (double)f17, d6, f37, f38, f39, alpha, f32, f35, j);
                  this.vertex(pVertexConsumer, d5, d2 + (double)f45, d6, f37, f38, f39, alpha, f32, f34, j);
                  this.vertex(pVertexConsumer, d3, d2 + (double)f44, d4, f37, f38, f39, alpha, f53, f33, j);
               }
            }
         }

      }
   }

   private float calculateAverageHeight(BlockAndTintGetter pLevel, Fluid pFluid, float pCurrentHeight, float pHeight1, float pHeight2, BlockPos pPos) {
      if (!(pHeight2 >= 1.0F) && !(pHeight1 >= 1.0F)) {
         float[] afloat = new float[2];
         if (pHeight2 > 0.0F || pHeight1 > 0.0F) {
            float f = this.getHeight(pLevel, pFluid, pPos);
            if (f >= 1.0F) {
               return 1.0F;
            }

            this.addWeightedHeight(afloat, f);
         }

         this.addWeightedHeight(afloat, pCurrentHeight);
         this.addWeightedHeight(afloat, pHeight2);
         this.addWeightedHeight(afloat, pHeight1);
         return afloat[0] / afloat[1];
      } else {
         return 1.0F;
      }
   }

   private void addWeightedHeight(float[] pOutput, float pHeight) {
      if (pHeight >= 0.8F) {
         pOutput[0] += pHeight * 10.0F;
         pOutput[1] += 10.0F;
      } else if (pHeight >= 0.0F) {
         pOutput[0] += pHeight;
         pOutput[1] += 1.0F;
      }

   }

   private float getHeight(BlockAndTintGetter pLevel, Fluid pFluid, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return this.getHeight(pLevel, pFluid, pPos, blockstate, blockstate.getFluidState());
   }

   private float getHeight(BlockAndTintGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pBlockState, FluidState pFluidState) {
      if (pFluid.isSame(pFluidState.getType())) {
         BlockState blockstate = pLevel.getBlockState(pPos.above());
         return pFluid.isSame(blockstate.getFluidState().getType()) ? 1.0F : pFluidState.getOwnHeight();
      } else {
         return !pBlockState.isSolid() ? 0.0F : -1.0F;
      }
   }

   private void vertex(VertexConsumer pConsumer, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float alpha, float pU, float pV, int pPackedLight) {
      pConsumer.vertex(pX, pY, pZ).color(pRed, pGreen, pBlue, alpha).uv(pU, pV).uv2(pPackedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
   }

   private int getLightColor(BlockAndTintGetter pLevel, BlockPos pPos) {
      int i = LevelRenderer.getLightColor(pLevel, pPos);
      int j = LevelRenderer.getLightColor(pLevel, pPos.above());
      int k = i & 255;
      int l = j & 255;
      int i1 = i >> 16 & 255;
      int j1 = j >> 16 & 255;
      return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
   }
}
