package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class NoteBlock extends Block {
   public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final IntegerProperty NOTE = BlockStateProperties.NOTE;
   public static final int NOTE_VOLUME = 3;

   public NoteBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, Integer.valueOf(0)).setValue(POWERED, Boolean.valueOf(false)));
   }

   private BlockState setInstrument(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      NoteBlockInstrument noteblockinstrument = pLevel.getBlockState(pPos.above()).instrument();
      if (noteblockinstrument.worksAboveNoteBlock()) {
         return pState.setValue(INSTRUMENT, noteblockinstrument);
      } else {
         NoteBlockInstrument noteblockinstrument1 = pLevel.getBlockState(pPos.below()).instrument();
         NoteBlockInstrument noteblockinstrument2 = noteblockinstrument1.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : noteblockinstrument1;
         return pState.setValue(INSTRUMENT, noteblockinstrument2);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.setInstrument(pContext.getLevel(), pContext.getClickedPos(), this.defaultBlockState());
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      boolean flag = pFacing.getAxis() == Direction.Axis.Y;
      return flag ? this.setInstrument(pLevel, pCurrentPos, pState) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      boolean flag = pLevel.hasNeighborSignal(pPos);
      if (flag != pState.getValue(POWERED)) {
         if (flag) {
            this.playNote((Entity)null, pState, pLevel, pPos);
         }

         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 3);
      }

   }

   private void playNote(@Nullable Entity pEntity, BlockState pState, Level pLevel, BlockPos pPos) {
      if (pState.getValue(INSTRUMENT).worksAboveNoteBlock() || pLevel.getBlockState(pPos.above()).isAir()) {
         pLevel.blockEvent(pPos, this, 0, 0);
         pLevel.gameEvent(pEntity, GameEvent.NOTE_BLOCK_PLAY, pPos);
      }

   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && pHit.getDirection() == Direction.UP) {
         return InteractionResult.PASS;
      } else if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         int _new = net.minecraftforge.common.ForgeHooks.onNoteChange(pLevel, pPos, pState, pState.getValue(NOTE), pState.cycle(NOTE).getValue(NOTE));
         if (_new == -1) return InteractionResult.FAIL;
         pState = pState.setValue(NOTE, _new);
         pLevel.setBlock(pPos, pState, 3);
         this.playNote(pPlayer, pState, pLevel, pPos);
         pPlayer.awardStat(Stats.TUNE_NOTEBLOCK);
         return InteractionResult.CONSUME;
      }
   }

   public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      if (!pLevel.isClientSide) {
         this.playNote(pPlayer, pState, pLevel, pPos);
         pPlayer.awardStat(Stats.PLAY_NOTEBLOCK);
      }
   }

   public static float getPitchFromNote(int pNote) {
      return (float)Math.pow(2.0D, (double)(pNote - 12) / 12.0D);
   }

   /**
    * Called on server when {@link net.minecraft.world.level.Level#blockEvent} is called. If server returns true, then
    * also called on the client. On the Server, this may perform additional changes to the world, like pistons replacing
    * the block with an extended base. On the client, the update may involve replacing block entities or effects such as
    * sounds or particles
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#triggerEvent}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
      net.minecraftforge.event.level.NoteBlockEvent.Play e = new net.minecraftforge.event.level.NoteBlockEvent.Play(pLevel, pPos, pState, pState.getValue(NOTE), pState.getValue(INSTRUMENT));
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) return false;
      pState = pState.setValue(NOTE, e.getVanillaNoteId()).setValue(INSTRUMENT, e.getInstrument());
      NoteBlockInstrument noteblockinstrument = pState.getValue(INSTRUMENT);
      float f;
      if (noteblockinstrument.isTunable()) {
         int i = pState.getValue(NOTE);
         f = getPitchFromNote(i);
         pLevel.addParticle(ParticleTypes.NOTE, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 1.2D, (double)pPos.getZ() + 0.5D, (double)i / 24.0D, 0.0D, 0.0D);
      } else {
         f = 1.0F;
      }

      Holder<SoundEvent> holder;
      if (noteblockinstrument.hasCustomSound()) {
         ResourceLocation resourcelocation = this.getCustomSoundId(pLevel, pPos);
         if (resourcelocation == null) {
            return false;
         }

         holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourcelocation));
      } else {
         holder = noteblockinstrument.getSoundEvent();
      }

      pLevel.playSeededSound((Player)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, holder, SoundSource.RECORDS, 3.0F, f, pLevel.random.nextLong());
      return true;
   }

   @Nullable
   private ResourceLocation getCustomSoundId(Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos.above());
      if (blockentity instanceof SkullBlockEntity skullblockentity) {
         return skullblockentity.getNoteBlockSound();
      } else {
         return null;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(INSTRUMENT, POWERED, NOTE);
   }
}
