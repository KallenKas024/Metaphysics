package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
   public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

   public JukeboxBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null && compoundtag.contains("RecordItem")) {
         pLevel.setBlock(pPos, pState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
      }

   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pState.getValue(HAS_RECORD)) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxblockentity = (JukeboxBlockEntity)blockentity;
            jukeboxblockentity.popOutRecord();
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
         }
      }

      return InteractionResult.PASS;
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxblockentity = (JukeboxBlockEntity)blockentity;
            jukeboxblockentity.popOutRecord();
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new JukeboxBlockEntity(pPos, pState);
   }

   /**
    * Returns whether this block is capable of emitting redstone signals.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#isSignalSource}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   /**
    * Returns the signal this block emits in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getSignal}
    * whenever possible. Implementing/overriding is fine.
    */
   public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof JukeboxBlockEntity jukeboxblockentity) {
         if (jukeboxblockentity.isRecordPlaying()) {
            return 15;
         }
      }

      return 0;
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
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof JukeboxBlockEntity jukeboxblockentity) {
         Item item = jukeboxblockentity.getFirstItem().getItem();
         if (item instanceof RecordItem recorditem) {
            return recorditem.getAnalogOutput();
         }
      }

      return 0;
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

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HAS_RECORD);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pState.getValue(HAS_RECORD) ? createTickerHelper(pBlockEntityType, BlockEntityType.JUKEBOX, JukeboxBlockEntity::playRecordTick) : null;
   }
}