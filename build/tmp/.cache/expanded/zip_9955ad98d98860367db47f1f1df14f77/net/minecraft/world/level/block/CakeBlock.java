package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CakeBlock extends Block {
   public static final int MAX_BITES = 6;
   public static final IntegerProperty BITES = BlockStateProperties.BITES;
   public static final int FULL_CAKE_SIGNAL = getOutputSignal(0);
   protected static final float AABB_OFFSET = 1.0F;
   protected static final float AABB_SIZE_PER_BITE = 2.0F;
   protected static final VoxelShape[] SHAPE_BY_BITE = new VoxelShape[]{Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(3.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(5.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(7.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(9.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(11.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(13.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D)};

   public CakeBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(BITES, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE_BY_BITE[pState.getValue(BITES)];
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      if (itemstack.is(ItemTags.CANDLES) && pState.getValue(BITES) == 0) {
         Block block = Block.byItem(item);
         if (block instanceof CandleBlock) {
            if (!pPlayer.isCreative()) {
               itemstack.shrink(1);
            }

            pLevel.playSound((Player)null, pPos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.setBlockAndUpdate(pPos, CandleCakeBlock.byCandle(block));
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
            return InteractionResult.SUCCESS;
         }
      }

      if (pLevel.isClientSide) {
         if (eat(pLevel, pPos, pState, pPlayer).consumesAction()) {
            return InteractionResult.SUCCESS;
         }

         if (itemstack.isEmpty()) {
            return InteractionResult.CONSUME;
         }
      }

      return eat(pLevel, pPos, pState, pPlayer);
   }

   protected static InteractionResult eat(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pPlayer.canEat(false)) {
         return InteractionResult.PASS;
      } else {
         pPlayer.awardStat(Stats.EAT_CAKE_SLICE);
         pPlayer.getFoodData().eat(2, 0.1F);
         int i = pState.getValue(BITES);
         pLevel.gameEvent(pPlayer, GameEvent.EAT, pPos);
         if (i < 6) {
            pLevel.setBlock(pPos, pState.setValue(BITES, Integer.valueOf(i + 1)), 3);
         } else {
            pLevel.removeBlock(pPos, false);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_DESTROY, pPos);
         }

         return InteractionResult.SUCCESS;
      }
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos.below()).isSolid();
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(BITES);
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return getOutputSignal(pBlockState.getValue(BITES));
   }

   public static int getOutputSignal(int pEaten) {
      return (7 - pEaten) * 2;
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}