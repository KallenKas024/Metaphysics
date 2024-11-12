package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
   private static final int MAX_BOOKS_IN_STORAGE = 6;
   public static final int BOOKS_PER_ROW = 3;
   public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED);

   public ChiseledBookShelfBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      BlockState blockstate = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

      for(BooleanProperty booleanproperty : SLOT_OCCUPIED_PROPERTIES) {
         blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
      }

      this.registerDefaultState(blockstate);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      BlockEntity $$8 = pLevel.getBlockEntity(pPos);
      if ($$8 instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity) {
         Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(pHit, pState.getValue(HorizontalDirectionalBlock.FACING));
         if (optional.isEmpty()) {
            return InteractionResult.PASS;
         } else {
            int i = getHitSlot(optional.get());
            if (pState.getValue(SLOT_OCCUPIED_PROPERTIES.get(i))) {
               removeBook(pLevel, pPos, pPlayer, chiseledbookshelfblockentity, i);
               return InteractionResult.sidedSuccess(pLevel.isClientSide);
            } else {
               ItemStack itemstack = pPlayer.getItemInHand(pHand);
               if (itemstack.is(ItemTags.BOOKSHELF_BOOKS)) {
                  addBook(pLevel, pPos, pPlayer, chiseledbookshelfblockentity, itemstack, i);
                  return InteractionResult.sidedSuccess(pLevel.isClientSide);
               } else {
                  return InteractionResult.CONSUME;
               }
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult pHitResult, Direction pFace) {
      Direction direction = pHitResult.getDirection();
      if (pFace != direction) {
         return Optional.empty();
      } else {
         BlockPos blockpos = pHitResult.getBlockPos().relative(direction);
         Vec3 vec3 = pHitResult.getLocation().subtract((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
         double d0 = vec3.x();
         double d1 = vec3.y();
         double d2 = vec3.z();
         Optional optional;
         switch (direction) {
            case NORTH:
               optional = Optional.of(new Vec2((float)(1.0D - d0), (float)d1));
               break;
            case SOUTH:
               optional = Optional.of(new Vec2((float)d0, (float)d1));
               break;
            case WEST:
               optional = Optional.of(new Vec2((float)d2, (float)d1));
               break;
            case EAST:
               optional = Optional.of(new Vec2((float)(1.0D - d2), (float)d1));
               break;
            case DOWN:
            case UP:
               optional = Optional.empty();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return optional;
      }
   }

   private static int getHitSlot(Vec2 pHitPos) {
      int i = pHitPos.y >= 0.5F ? 0 : 1;
      int j = getSection(pHitPos.x);
      return j + i * 3;
   }

   private static int getSection(float pX) {
      float f = 0.0625F;
      float f1 = 0.375F;
      if (pX < 0.375F) {
         return 0;
      } else {
         float f2 = 0.6875F;
         return pX < 0.6875F ? 1 : 2;
      }
   }

   private static void addBook(Level pLevel, BlockPos pPos, Player pPlayer, ChiseledBookShelfBlockEntity pBlockEntity, ItemStack pBookStack, int pSlot) {
      if (!pLevel.isClientSide) {
         pPlayer.awardStat(Stats.ITEM_USED.get(pBookStack.getItem()));
         SoundEvent soundevent = pBookStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
         pBlockEntity.setItem(pSlot, pBookStack.split(1));
         pLevel.playSound((Player)null, pPos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
         if (pPlayer.isCreative()) {
            pBookStack.grow(1);
         }

         pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
      }
   }

   private static void removeBook(Level pLevel, BlockPos pPos, Player pPlayer, ChiseledBookShelfBlockEntity pBlockEntity, int pSlot) {
      if (!pLevel.isClientSide) {
         ItemStack itemstack = pBlockEntity.removeItem(pSlot, 1);
         SoundEvent soundevent = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
         pLevel.playSound((Player)null, pPos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
         if (!pPlayer.getInventory().add(itemstack)) {
            pPlayer.drop(itemstack, false);
         }

         pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
      }
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new ChiseledBookShelfBlockEntity(pPos, pState);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HorizontalDirectionalBlock.FACING);
      SLOT_OCCUPIED_PROPERTIES.forEach((p_261456_) -> {
         pBuilder.add(p_261456_);
      });
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
      if (!pState.is(pNewState.getBlock())) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ChiseledBookShelfBlockEntity) {
            ChiseledBookShelfBlockEntity chiseledbookshelfblockentity = (ChiseledBookShelfBlockEntity)blockentity;
            if (!chiseledbookshelfblockentity.isEmpty()) {
               for(int i = 0; i < 6; ++i) {
                  ItemStack itemstack = chiseledbookshelfblockentity.getItem(i);
                  if (!itemstack.isEmpty()) {
                     Containers.dropItemStack(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), itemstack);
                  }
               }

               chiseledbookshelfblockentity.clearContent();
               pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, pContext.getHorizontalDirection().getOpposite());
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(HorizontalDirectionalBlock.FACING, pRotation.rotate(pState.getValue(HorizontalDirectionalBlock.FACING)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(HorizontalDirectionalBlock.FACING)));
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
      if (pLevel.isClientSide()) {
         return 0;
      } else {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ChiseledBookShelfBlockEntity) {
            ChiseledBookShelfBlockEntity chiseledbookshelfblockentity = (ChiseledBookShelfBlockEntity)blockentity;
            return chiseledbookshelfblockentity.getLastInteractedSlot() + 1;
         } else {
            return 0;
         }
      }
   }
}