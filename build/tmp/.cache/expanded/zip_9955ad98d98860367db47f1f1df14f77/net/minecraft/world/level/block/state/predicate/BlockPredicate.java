package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicate implements Predicate<BlockState> {
   private final Block block;

   public BlockPredicate(Block pBlock) {
      this.block = pBlock;
   }

   public static BlockPredicate forBlock(Block pBlock) {
      return new BlockPredicate(pBlock);
   }

   public boolean test(@Nullable BlockState pState) {
      return pState != null && pState.is(this.block);
   }
}