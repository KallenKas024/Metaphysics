package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountPlacement extends RepeatingPlacement {
   public static final Codec<CountPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountPlacement::new, (p_191633_) -> {
      return p_191633_.count;
   }).codec();
   private final IntProvider count;

   private CountPlacement(IntProvider p_191627_) {
      this.count = p_191627_;
   }

   public static CountPlacement of(IntProvider pCount) {
      return new CountPlacement(pCount);
   }

   public static CountPlacement of(int pCount) {
      return of(ConstantInt.of(pCount));
   }

   protected int count(RandomSource pRandom, BlockPos pPos) {
      return this.count.sample(pRandom);
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.COUNT;
   }
}