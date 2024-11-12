package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

public class EndGatewayFeature extends Feature<EndGatewayConfiguration> {
   public EndGatewayFeature(Codec<EndGatewayConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<EndGatewayConfiguration> pContext) {
      BlockPos blockpos = pContext.origin();
      WorldGenLevel worldgenlevel = pContext.level();
      EndGatewayConfiguration endgatewayconfiguration = pContext.config();

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-1, -2, -1), blockpos.offset(1, 2, 1))) {
         boolean flag = blockpos1.getX() == blockpos.getX();
         boolean flag1 = blockpos1.getY() == blockpos.getY();
         boolean flag2 = blockpos1.getZ() == blockpos.getZ();
         boolean flag3 = Math.abs(blockpos1.getY() - blockpos.getY()) == 2;
         if (flag && flag1 && flag2) {
            BlockPos blockpos2 = blockpos1.immutable();
            this.setBlock(worldgenlevel, blockpos2, Blocks.END_GATEWAY.defaultBlockState());
            endgatewayconfiguration.getExit().ifPresent((p_65699_) -> {
               BlockEntity blockentity = worldgenlevel.getBlockEntity(blockpos2);
               if (blockentity instanceof TheEndGatewayBlockEntity theendgatewayblockentity) {
                  theendgatewayblockentity.setExitPosition(p_65699_, endgatewayconfiguration.isExitExact());
                  blockentity.setChanged();
               }

            });
         } else if (flag1) {
            this.setBlock(worldgenlevel, blockpos1, Blocks.AIR.defaultBlockState());
         } else if (flag3 && flag && flag2) {
            this.setBlock(worldgenlevel, blockpos1, Blocks.BEDROCK.defaultBlockState());
         } else if ((flag || flag2) && !flag3) {
            this.setBlock(worldgenlevel, blockpos1, Blocks.BEDROCK.defaultBlockState());
         } else {
            this.setBlock(worldgenlevel, blockpos1, Blocks.AIR.defaultBlockState());
         }
      }

      return true;
   }
}