package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BoneMealItem extends Item {
   public static final int GRASS_SPREAD_WIDTH = 3;
   public static final int GRASS_SPREAD_HEIGHT = 1;
   public static final int GRASS_COUNT_MULTIPLIER = 3;

   public BoneMealItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockPos blockpos1 = blockpos.relative(pContext.getClickedFace());
      if (applyBonemeal(pContext.getItemInHand(), level, blockpos, pContext.getPlayer())) {
         if (!level.isClientSide) {
            level.levelEvent(1505, blockpos, 0);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         BlockState blockstate = level.getBlockState(blockpos);
         boolean flag = blockstate.isFaceSturdy(level, blockpos, pContext.getClickedFace());
         if (flag && growWaterPlant(pContext.getItemInHand(), level, blockpos1, pContext.getClickedFace())) {
            if (!level.isClientSide) {
               level.levelEvent(1505, blockpos1, 0);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   @Deprecated //Forge: Use Player/Hand version
   public static boolean growCrop(ItemStack pStack, Level pLevel, BlockPos pPos) {
      if (pLevel instanceof net.minecraft.server.level.ServerLevel)
         return applyBonemeal(pStack, pLevel, pPos, net.minecraftforge.common.util.FakePlayerFactory.getMinecraft((net.minecraft.server.level.ServerLevel)pLevel));
      return false;
   }

   public static boolean applyBonemeal(ItemStack pStack, Level pLevel, BlockPos pPos, net.minecraft.world.entity.player.Player player) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      int hook = net.minecraftforge.event.ForgeEventFactory.onApplyBonemeal(player, pLevel, pPos, blockstate, pStack);
      if (hook != 0) return hook > 0;
      if (blockstate.getBlock() instanceof BonemealableBlock) {
         BonemealableBlock bonemealableblock = (BonemealableBlock)blockstate.getBlock();
         if (bonemealableblock.isValidBonemealTarget(pLevel, pPos, blockstate, pLevel.isClientSide)) {
            if (pLevel instanceof ServerLevel) {
               if (bonemealableblock.isBonemealSuccess(pLevel, pLevel.random, pPos, blockstate)) {
                  bonemealableblock.performBonemeal((ServerLevel)pLevel, pLevel.random, pPos, blockstate);
               }

               pStack.shrink(1);
            }

            return true;
         }
      }

      return false;
   }

   public static boolean growWaterPlant(ItemStack pStack, Level pLevel, BlockPos pPos, @Nullable Direction pClickedSide) {
      if (pLevel.getBlockState(pPos).is(Blocks.WATER) && pLevel.getFluidState(pPos).getAmount() == 8) {
         if (!(pLevel instanceof ServerLevel)) {
            return true;
         } else {
            RandomSource randomsource = pLevel.getRandom();

            label78:
            for(int i = 0; i < 128; ++i) {
               BlockPos blockpos = pPos;
               BlockState blockstate = Blocks.SEAGRASS.defaultBlockState();

               for(int j = 0; j < i / 16; ++j) {
                  blockpos = blockpos.offset(randomsource.nextInt(3) - 1, (randomsource.nextInt(3) - 1) * randomsource.nextInt(3) / 2, randomsource.nextInt(3) - 1);
                  if (pLevel.getBlockState(blockpos).isCollisionShapeFullBlock(pLevel, blockpos)) {
                     continue label78;
                  }
               }

               Holder<Biome> holder = pLevel.getBiome(blockpos);
               if (holder.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                  if (i == 0 && pClickedSide != null && pClickedSide.getAxis().isHorizontal()) {
                     blockstate = BuiltInRegistries.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap((p_204098_) -> {
                        return p_204098_.getRandomElement(pLevel.random);
                     }).map((p_204100_) -> {
                        return p_204100_.value().defaultBlockState();
                     }).orElse(blockstate);
                     if (blockstate.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        blockstate = blockstate.setValue(BaseCoralWallFanBlock.FACING, pClickedSide);
                     }
                  } else if (randomsource.nextInt(4) == 0) {
                     blockstate = BuiltInRegistries.BLOCK.getTag(BlockTags.UNDERWATER_BONEMEALS).flatMap((p_204091_) -> {
                        return p_204091_.getRandomElement(pLevel.random);
                     }).map((p_204095_) -> {
                        return p_204095_.value().defaultBlockState();
                     }).orElse(blockstate);
                  }
               }

               if (blockstate.is(BlockTags.WALL_CORALS, (p_204093_) -> {
                  return p_204093_.hasProperty(BaseCoralWallFanBlock.FACING);
               })) {
                  for(int k = 0; !blockstate.canSurvive(pLevel, blockpos) && k < 4; ++k) {
                     blockstate = blockstate.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomsource));
                  }
               }

               if (blockstate.canSurvive(pLevel, blockpos)) {
                  BlockState blockstate1 = pLevel.getBlockState(blockpos);
                  if (blockstate1.is(Blocks.WATER) && pLevel.getFluidState(blockpos).getAmount() == 8) {
                     pLevel.setBlock(blockpos, blockstate, 3);
                  } else if (blockstate1.is(Blocks.SEAGRASS) && randomsource.nextInt(10) == 0) {
                     ((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)pLevel, randomsource, blockpos, blockstate1);
                  }
               }
            }

            pStack.shrink(1);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void addGrowthParticles(LevelAccessor pLevel, BlockPos pPos, int pData) {
      if (pData == 0) {
         pData = 15;
      }

      BlockState blockstate = pLevel.getBlockState(pPos);
      if (!blockstate.isAir()) {
         double d0 = 0.5D;
         double d1;
         if (blockstate.is(Blocks.WATER)) {
            pData *= 3;
            d1 = 1.0D;
            d0 = 3.0D;
         } else if (blockstate.isSolidRender(pLevel, pPos)) {
            pPos = pPos.above();
            pData *= 3;
            d0 = 3.0D;
            d1 = 1.0D;
         } else {
            d1 = blockstate.getShape(pLevel, pPos).max(Direction.Axis.Y);
         }

         pLevel.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
         RandomSource randomsource = pLevel.getRandom();

         for(int i = 0; i < pData; ++i) {
            double d2 = randomsource.nextGaussian() * 0.02D;
            double d3 = randomsource.nextGaussian() * 0.02D;
            double d4 = randomsource.nextGaussian() * 0.02D;
            double d5 = 0.5D - d0;
            double d6 = (double)pPos.getX() + d5 + randomsource.nextDouble() * d0 * 2.0D;
            double d7 = (double)pPos.getY() + randomsource.nextDouble() * d1;
            double d8 = (double)pPos.getZ() + d5 + randomsource.nextDouble() * d0 * 2.0D;
            if (!pLevel.getBlockState(BlockPos.containing(d6, d7, d8).below()).isAir()) {
               pLevel.addParticle(ParticleTypes.HAPPY_VILLAGER, d6, d7, d8, d2, d3, d4);
            }
         }

      }
   }
}
