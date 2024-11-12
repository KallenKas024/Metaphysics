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
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SmithingTableBlock extends CraftingTableBlock {
   private static final Component CONTAINER_TITLE = Component.translatable("container.upgrade");

   public SmithingTableBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      return new SimpleMenuProvider((p_277304_, p_277305_, p_277306_) -> {
         return new SmithingMenu(p_277304_, p_277305_, ContainerLevelAccess.create(pLevel, pPos));
      }, CONTAINER_TITLE);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         pPlayer.awardStat(Stats.INTERACT_WITH_SMITHING_TABLE);
         return InteractionResult.CONSUME;
      }
   }
}