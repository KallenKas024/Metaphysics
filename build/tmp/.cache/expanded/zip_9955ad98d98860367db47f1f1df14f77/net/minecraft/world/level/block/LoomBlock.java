package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class LoomBlock extends HorizontalDirectionalBlock {
   private static final Component CONTAINER_TITLE = Component.translatable("container.loom");

   public LoomBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         pPlayer.awardStat(Stats.INTERACT_WITH_LOOM);
         return InteractionResult.CONSUME;
      }
   }

   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      return new SimpleMenuProvider((p_54783_, p_54784_, p_54785_) -> {
         return new LoomMenu(p_54783_, p_54784_, ContainerLevelAccess.create(pLevel, pPos));
      }, CONTAINER_TITLE);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }
}