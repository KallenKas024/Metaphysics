package net.minecraft.world.damagesource;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record FallLocation(String id) {
   public static final FallLocation GENERIC = new FallLocation("generic");
   public static final FallLocation LADDER = new FallLocation("ladder");
   public static final FallLocation VINES = new FallLocation("vines");
   public static final FallLocation WEEPING_VINES = new FallLocation("weeping_vines");
   public static final FallLocation TWISTING_VINES = new FallLocation("twisting_vines");
   public static final FallLocation SCAFFOLDING = new FallLocation("scaffolding");
   public static final FallLocation OTHER_CLIMBABLE = new FallLocation("other_climbable");
   public static final FallLocation WATER = new FallLocation("water");

   public static FallLocation blockToFallLocation(BlockState pState) {
      if (!pState.is(Blocks.LADDER) && !pState.is(BlockTags.TRAPDOORS)) {
         if (pState.is(Blocks.VINE)) {
            return VINES;
         } else if (!pState.is(Blocks.WEEPING_VINES) && !pState.is(Blocks.WEEPING_VINES_PLANT)) {
            if (!pState.is(Blocks.TWISTING_VINES) && !pState.is(Blocks.TWISTING_VINES_PLANT)) {
               return pState.is(Blocks.SCAFFOLDING) ? SCAFFOLDING : OTHER_CLIMBABLE;
            } else {
               return TWISTING_VINES;
            }
         } else {
            return WEEPING_VINES;
         }
      } else {
         return LADDER;
      }
   }

   @Nullable
   public static FallLocation getCurrentFallLocation(LivingEntity pEntity) {
      Optional<BlockPos> optional = pEntity.getLastClimbablePos();
      if (optional.isPresent()) {
         BlockState blockstate = pEntity.level().getBlockState(optional.get());
         return blockToFallLocation(blockstate);
      } else {
         return pEntity.isInWater() ? WATER : null;
      }
   }

   public String languageKey() {
      return "death.fell.accident." + this.id;
   }
}