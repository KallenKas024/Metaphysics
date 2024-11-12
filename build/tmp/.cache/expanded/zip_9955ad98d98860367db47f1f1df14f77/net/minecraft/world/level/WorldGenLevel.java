package net.minecraft.world.level;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public interface WorldGenLevel extends ServerLevelAccessor {
   /**
    * Gets the random world seed.
    */
   long getSeed();

   default boolean ensureCanWrite(BlockPos pPos) {
      return true;
   }

   default void setCurrentlyGenerating(@Nullable Supplier<String> pCurrentlyGenerating) {
   }
}