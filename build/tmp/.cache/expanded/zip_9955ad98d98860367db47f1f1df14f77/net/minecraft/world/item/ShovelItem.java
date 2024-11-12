package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShovelItem extends DiggerItem {
   /** Map used to lookup shovel right click interactions */
   protected static final Map<Block, BlockState> FLATTENABLES = Maps.newHashMap((new ImmutableMap.Builder()).put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.DIRT, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.PODZOL, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.MYCELIUM, Blocks.DIRT_PATH.defaultBlockState()).put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.defaultBlockState()).build());

   public ShovelItem(Tier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
      super(pAttackDamageModifier, pAttackSpeedModifier, pTier, BlockTags.MINEABLE_WITH_SHOVEL, pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (pContext.getClickedFace() == Direction.DOWN) {
         return InteractionResult.PASS;
      } else {
         Player player = pContext.getPlayer();
         BlockState blockstate1 = blockstate.getToolModifiedState(pContext, net.minecraftforge.common.ToolActions.SHOVEL_FLATTEN, false);
         BlockState blockstate2 = null;
         if (blockstate1 != null && level.isEmptyBlock(blockpos.above())) {
            level.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            blockstate2 = blockstate1;
         } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.getValue(CampfireBlock.LIT)) {
            if (!level.isClientSide()) {
               level.levelEvent((Player)null, 1009, blockpos, 0);
            }

            CampfireBlock.dowse(pContext.getPlayer(), level, blockpos, blockstate);
            blockstate2 = blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
         }

         if (blockstate2 != null) {
            if (!level.isClientSide) {
               level.setBlock(blockpos, blockstate2, 11);
               level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, blockstate2));
               if (player != null) {
                  pContext.getItemInHand().hurtAndBreak(1, player, (p_43122_) -> {
                     p_43122_.broadcastBreakEvent(pContext.getHand());
                  });
               }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   @org.jetbrains.annotations.Nullable
   public static BlockState getShovelPathingState(BlockState originalState) {
      return FLATTENABLES.get(originalState.getBlock());
   }

   @Override
   public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
      return net.minecraftforge.common.ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction);
   }
}
