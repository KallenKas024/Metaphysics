package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
   ContainerLevelAccess NULL = new ContainerLevelAccess() {
      public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> p_39304_) {
         return Optional.empty();
      }
   };

   static ContainerLevelAccess create(final Level pLevel, final BlockPos pPos) {
      return new ContainerLevelAccess() {
         public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> p_39311_) {
            return Optional.of(p_39311_.apply(pLevel, pPos));
         }
      };
   }

   <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> pLevelPosConsumer);

   default <T> T evaluate(BiFunction<Level, BlockPos, T> pLevelPosConsumer, T pDefaultValue) {
      return this.evaluate(pLevelPosConsumer).orElse(pDefaultValue);
   }

   default void execute(BiConsumer<Level, BlockPos> pLevelPosConsumer) {
      this.evaluate((p_39296_, p_39297_) -> {
         pLevelPosConsumer.accept(p_39296_, p_39297_);
         return Optional.empty();
      });
   }
}