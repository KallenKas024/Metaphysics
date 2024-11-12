package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public class PlayerWallHeadBlock extends WallSkullBlock {
   public PlayerWallHeadBlock(BlockBehaviour.Properties p_55185_) {
      super(SkullBlock.Types.PLAYER, p_55185_);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      Blocks.PLAYER_HEAD.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
   }

   public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
      return Blocks.PLAYER_HEAD.getDrops(pState, pParams);
   }
}