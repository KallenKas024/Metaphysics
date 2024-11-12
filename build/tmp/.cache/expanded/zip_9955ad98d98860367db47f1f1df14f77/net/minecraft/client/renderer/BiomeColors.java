package net.minecraft.client.renderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeColors {
   public static final ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
   public static final ColorResolver FOLIAGE_COLOR_RESOLVER = (p_108808_, p_108809_, p_108810_) -> {
      return p_108808_.getFoliageColor();
   };
   public static final ColorResolver WATER_COLOR_RESOLVER = (p_108801_, p_108802_, p_108803_) -> {
      return p_108801_.getWaterColor();
   };

   private static int getAverageColor(BlockAndTintGetter pLevel, BlockPos pBlockPos, ColorResolver pColorResolver) {
      return pLevel.getBlockTint(pBlockPos, pColorResolver);
   }

   public static int getAverageGrassColor(BlockAndTintGetter pLevel, BlockPos pBlockPos) {
      return getAverageColor(pLevel, pBlockPos, GRASS_COLOR_RESOLVER);
   }

   public static int getAverageFoliageColor(BlockAndTintGetter pLevel, BlockPos pBlockPos) {
      return getAverageColor(pLevel, pBlockPos, FOLIAGE_COLOR_RESOLVER);
   }

   public static int getAverageWaterColor(BlockAndTintGetter pLevel, BlockPos pBlockPos) {
      return getAverageColor(pLevel, pBlockPos, WATER_COLOR_RESOLVER);
   }
}