package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public class BlockAgeProcessor extends StructureProcessor {
   public static final Codec<BlockAgeProcessor> CODEC = Codec.FLOAT.fieldOf("mossiness").xmap(BlockAgeProcessor::new, (p_74023_) -> {
      return p_74023_.mossiness;
   }).codec();
   private static final float PROBABILITY_OF_REPLACING_FULL_BLOCK = 0.5F;
   private static final float PROBABILITY_OF_REPLACING_STAIRS = 0.5F;
   private static final float PROBABILITY_OF_REPLACING_OBSIDIAN = 0.15F;
   private static final BlockState[] NON_MOSSY_REPLACEMENTS = new BlockState[]{Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()};
   private final float mossiness;

   public BlockAgeProcessor(float p_74013_) {
      this.mossiness = p_74013_;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader pLevel, BlockPos pOffset, BlockPos pPos, StructureTemplate.StructureBlockInfo pBlockInfo, StructureTemplate.StructureBlockInfo pRelativeBlockInfo, StructurePlaceSettings pSettings) {
      RandomSource randomsource = pSettings.getRandom(pRelativeBlockInfo.pos());
      BlockState blockstate = pRelativeBlockInfo.state();
      BlockPos blockpos = pRelativeBlockInfo.pos();
      BlockState blockstate1 = null;
      if (!blockstate.is(Blocks.STONE_BRICKS) && !blockstate.is(Blocks.STONE) && !blockstate.is(Blocks.CHISELED_STONE_BRICKS)) {
         if (blockstate.is(BlockTags.STAIRS)) {
            blockstate1 = this.maybeReplaceStairs(randomsource, pRelativeBlockInfo.state());
         } else if (blockstate.is(BlockTags.SLABS)) {
            blockstate1 = this.maybeReplaceSlab(randomsource);
         } else if (blockstate.is(BlockTags.WALLS)) {
            blockstate1 = this.maybeReplaceWall(randomsource);
         } else if (blockstate.is(Blocks.OBSIDIAN)) {
            blockstate1 = this.maybeReplaceObsidian(randomsource);
         }
      } else {
         blockstate1 = this.maybeReplaceFullStoneBlock(randomsource);
      }

      return blockstate1 != null ? new StructureTemplate.StructureBlockInfo(blockpos, blockstate1, pRelativeBlockInfo.nbt()) : pRelativeBlockInfo;
   }

   @Nullable
   private BlockState maybeReplaceFullStoneBlock(RandomSource pRandom) {
      if (pRandom.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] ablockstate = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(pRandom, Blocks.STONE_BRICK_STAIRS)};
         BlockState[] ablockstate1 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(pRandom, Blocks.MOSSY_STONE_BRICK_STAIRS)};
         return this.getRandomBlock(pRandom, ablockstate, ablockstate1);
      }
   }

   @Nullable
   private BlockState maybeReplaceStairs(RandomSource pRandom, BlockState pState) {
      Direction direction = pState.getValue(StairBlock.FACING);
      Half half = pState.getValue(StairBlock.HALF);
      if (pRandom.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] ablockstate = new BlockState[]{Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction).setValue(StairBlock.HALF, half), Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()};
         return this.getRandomBlock(pRandom, NON_MOSSY_REPLACEMENTS, ablockstate);
      }
   }

   @Nullable
   private BlockState maybeReplaceSlab(RandomSource pRandom) {
      return pRandom.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : null;
   }

   @Nullable
   private BlockState maybeReplaceWall(RandomSource pRandom) {
      return pRandom.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : null;
   }

   @Nullable
   private BlockState maybeReplaceObsidian(RandomSource pRandom) {
      return pRandom.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
   }

   private static BlockState getRandomFacingStairs(RandomSource pRandom, Block pStairsBlock) {
      return pStairsBlock.defaultBlockState().setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(pRandom)).setValue(StairBlock.HALF, Util.getRandom(Half.values(), pRandom));
   }

   private BlockState getRandomBlock(RandomSource pRandom, BlockState[] pNormalStates, BlockState[] pMossyStates) {
      return pRandom.nextFloat() < this.mossiness ? getRandomBlock(pRandom, pMossyStates) : getRandomBlock(pRandom, pNormalStates);
   }

   private static BlockState getRandomBlock(RandomSource pRandom, BlockState[] pStates) {
      return pStates[pRandom.nextInt(pStates.length)];
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_AGE;
   }
}