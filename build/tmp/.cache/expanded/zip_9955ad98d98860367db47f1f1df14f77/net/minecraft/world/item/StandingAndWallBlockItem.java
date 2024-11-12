package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StandingAndWallBlockItem extends BlockItem {
   protected final Block wallBlock;
   private final Direction attachmentDirection;

   public StandingAndWallBlockItem(Block pBlock, Block pWallBlock, Item.Properties pProperties, Direction pAttachmentDirection) {
      super(pBlock, pProperties);
      this.wallBlock = pWallBlock;
      this.attachmentDirection = pAttachmentDirection;
   }

   protected boolean canPlace(LevelReader pLevel, BlockState pState, BlockPos pPos) {
      return pState.canSurvive(pLevel, pPos);
   }

   @Nullable
   protected BlockState getPlacementState(BlockPlaceContext pContext) {
      BlockState blockstate = this.wallBlock.getStateForPlacement(pContext);
      BlockState blockstate1 = null;
      LevelReader levelreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();

      for(Direction direction : pContext.getNearestLookingDirections()) {
         if (direction != this.attachmentDirection.getOpposite()) {
            BlockState blockstate2 = direction == this.attachmentDirection ? this.getBlock().getStateForPlacement(pContext) : blockstate;
            if (blockstate2 != null && this.canPlace(levelreader, blockstate2, blockpos)) {
               blockstate1 = blockstate2;
               break;
            }
         }
      }

      return blockstate1 != null && levelreader.isUnobstructed(blockstate1, blockpos, CollisionContext.empty()) ? blockstate1 : null;
   }

   public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
      super.registerBlocks(pBlockToItemMap, pItem);
      pBlockToItemMap.put(this.wallBlock, pItem);
   }

   public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
      super.removeFromBlockToItemMap(blockToItemMap, itemIn);
      blockToItemMap.remove(this.wallBlock);
   }
}
