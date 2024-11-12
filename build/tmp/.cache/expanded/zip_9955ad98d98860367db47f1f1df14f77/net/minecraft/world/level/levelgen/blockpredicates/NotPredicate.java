package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class NotPredicate implements BlockPredicate {
   public static final Codec<NotPredicate> CODEC = RecordCodecBuilder.create((p_190515_) -> {
      return p_190515_.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter((p_190517_) -> {
         return p_190517_.predicate;
      })).apply(p_190515_, NotPredicate::new);
   });
   private final BlockPredicate predicate;

   public NotPredicate(BlockPredicate p_190509_) {
      this.predicate = p_190509_;
   }

   public boolean test(WorldGenLevel pLevel, BlockPos pPos) {
      return !this.predicate.test(pLevel, pPos);
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.NOT;
   }
}