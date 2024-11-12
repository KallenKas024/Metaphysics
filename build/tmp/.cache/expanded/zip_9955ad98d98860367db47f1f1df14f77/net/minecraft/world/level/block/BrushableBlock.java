package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BrushableBlock extends BaseEntityBlock implements Fallable {
   private static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;
   public static final int TICK_DELAY = 2;
   private final Block turnsInto;
   private final SoundEvent brushSound;
   private final SoundEvent brushCompletedSound;

   public BrushableBlock(Block pTurnsInto, BlockBehaviour.Properties pProperties, SoundEvent pBrushSound, SoundEvent pBrushCompletedSound) {
      super(pProperties);
      this.turnsInto = pTurnsInto;
      this.brushSound = pBrushSound;
      this.brushCompletedSound = pBrushCompletedSound;
      this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, Integer.valueOf(0)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(DUSTED);
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

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
      pLevel.scheduleTick(pPos, this, 2);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      pLevel.scheduleTick(pPos, this, 2);
      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof BrushableBlockEntity brushableblockentity) {
         brushableblockentity.checkReset();
      }

      if (FallingBlock.isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight()) {
         FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(pLevel, pPos, pState);
         fallingblockentity.disableDrop();
      }
   }

   public void onBrokenAfterFall(Level pLevel, BlockPos pPos, FallingBlockEntity pFallingBlock) {
      Vec3 vec3 = pFallingBlock.getBoundingBox().getCenter();
      pLevel.levelEvent(2001, BlockPos.containing(vec3), Block.getId(pFallingBlock.getBlockState()));
      pLevel.gameEvent(pFallingBlock, GameEvent.BLOCK_DESTROY, vec3);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pRandom.nextInt(16) == 0) {
         BlockPos blockpos = pPos.below();
         if (FallingBlock.isFree(pLevel.getBlockState(blockpos))) {
            double d0 = (double)pPos.getX() + pRandom.nextDouble();
            double d1 = (double)pPos.getY() - 0.05D;
            double d2 = (double)pPos.getZ() + pRandom.nextDouble();
            pLevel.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, pState), d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BrushableBlockEntity(pPos, pState);
   }

   public Block getTurnsInto() {
      return this.turnsInto;
   }

   public SoundEvent getBrushSound() {
      return this.brushSound;
   }

   public SoundEvent getBrushCompletedSound() {
      return this.brushCompletedSound;
   }
}