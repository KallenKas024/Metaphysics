package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StateTestingPredicate implements BlockPredicate {
   protected final Vec3i offset;

   protected static <P extends StateTestingPredicate> Products.P1<RecordCodecBuilder.Mu<P>, Vec3i> stateTestingCodec(RecordCodecBuilder.Instance<P> pInstance) {
      return pInstance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter((p_190549_) -> {
         return p_190549_.offset;
      }));
   }

   protected StateTestingPredicate(Vec3i pOffset) {
      this.offset = pOffset;
   }

   public final boolean test(WorldGenLevel pLevel, BlockPos pPos) {
      return this.test(pLevel.getBlockState(pPos.offset(this.offset)));
   }

   protected abstract boolean test(BlockState pState);
}