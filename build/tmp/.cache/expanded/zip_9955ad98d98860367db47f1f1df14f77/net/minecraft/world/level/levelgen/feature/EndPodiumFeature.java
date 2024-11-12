package net.minecraft.world.level.levelgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
   public static final int PODIUM_RADIUS = 4;
   public static final int PODIUM_PILLAR_HEIGHT = 4;
   public static final int RIM_RADIUS = 1;
   public static final float CORNER_ROUNDING = 0.5F;
   private static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
   private final boolean active;

   public static BlockPos getLocation(BlockPos pPos) {
      return END_PODIUM_LOCATION.offset(pPos);
   }

   public EndPodiumFeature(boolean pActive) {
      super(NoneFeatureConfiguration.CODEC);
      this.active = pActive;
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
      BlockPos blockpos = pContext.origin();
      WorldGenLevel worldgenlevel = pContext.level();

      for(BlockPos blockpos1 : BlockPos.betweenClosed(new BlockPos(blockpos.getX() - 4, blockpos.getY() - 1, blockpos.getZ() - 4), new BlockPos(blockpos.getX() + 4, blockpos.getY() + 32, blockpos.getZ() + 4))) {
         boolean flag = blockpos1.closerThan(blockpos, 2.5D);
         if (flag || blockpos1.closerThan(blockpos, 3.5D)) {
            if (blockpos1.getY() < blockpos.getY()) {
               if (flag) {
                  this.setBlock(worldgenlevel, blockpos1, Blocks.BEDROCK.defaultBlockState());
               } else if (blockpos1.getY() < blockpos.getY()) {
                  this.setBlock(worldgenlevel, blockpos1, Blocks.END_STONE.defaultBlockState());
               }
            } else if (blockpos1.getY() > blockpos.getY()) {
               this.setBlock(worldgenlevel, blockpos1, Blocks.AIR.defaultBlockState());
            } else if (!flag) {
               this.setBlock(worldgenlevel, blockpos1, Blocks.BEDROCK.defaultBlockState());
            } else if (this.active) {
               this.setBlock(worldgenlevel, new BlockPos(blockpos1), Blocks.END_PORTAL.defaultBlockState());
            } else {
               this.setBlock(worldgenlevel, new BlockPos(blockpos1), Blocks.AIR.defaultBlockState());
            }
         }
      }

      for(int i = 0; i < 4; ++i) {
         this.setBlock(worldgenlevel, blockpos.above(i), Blocks.BEDROCK.defaultBlockState());
      }

      BlockPos blockpos2 = blockpos.above(2);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         this.setBlock(worldgenlevel, blockpos2.relative(direction), Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, direction));
      }

      return true;
   }
}