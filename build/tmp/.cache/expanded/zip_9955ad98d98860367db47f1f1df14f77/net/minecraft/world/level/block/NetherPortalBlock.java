package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherPortalBlock extends Block {
   public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
   protected static final int AABB_OFFSET = 2;
   protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

   public NetherPortalBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch ((Direction.Axis)pState.getValue(AXIS)) {
         case Z:
            return Z_AXIS_AABB;
         case X:
         default:
            return X_AXIS_AABB;
      }
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.dimensionType().natural() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && pRandom.nextInt(2000) < pLevel.getDifficulty().getId()) {
         while(pLevel.getBlockState(pPos).is(this)) {
            pPos = pPos.below();
         }

         if (pLevel.getBlockState(pPos).isValidSpawn(pLevel, pPos, EntityType.ZOMBIFIED_PIGLIN)) {
            Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(pLevel, pPos.above(), MobSpawnType.STRUCTURE);
            if (entity != null) {
               entity.setPortalCooldown();
            }
         }
      }

   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      Direction.Axis direction$axis = pFacing.getAxis();
      Direction.Axis direction$axis1 = pState.getValue(AXIS);
      boolean flag = direction$axis1 != direction$axis && direction$axis.isHorizontal();
      return !flag && !pFacingState.is(this) && !(new PortalShape(pLevel, pCurrentPos, direction$axis1)).isComplete() ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      if (pEntity.canChangeDimensions()) {
         pEntity.handleInsidePortal(pPos);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pRandom.nextInt(100) == 0) {
         pLevel.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, pRandom.nextFloat() * 0.4F + 0.8F, false);
      }

      for(int i = 0; i < 4; ++i) {
         double d0 = (double)pPos.getX() + pRandom.nextDouble();
         double d1 = (double)pPos.getY() + pRandom.nextDouble();
         double d2 = (double)pPos.getZ() + pRandom.nextDouble();
         double d3 = ((double)pRandom.nextFloat() - 0.5D) * 0.5D;
         double d4 = ((double)pRandom.nextFloat() - 0.5D) * 0.5D;
         double d5 = ((double)pRandom.nextFloat() - 0.5D) * 0.5D;
         int j = pRandom.nextInt(2) * 2 - 1;
         if (!pLevel.getBlockState(pPos.west()).is(this) && !pLevel.getBlockState(pPos.east()).is(this)) {
            d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)j;
            d3 = (double)(pRandom.nextFloat() * 2.0F * (float)j);
         } else {
            d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)j;
            d5 = (double)(pRandom.nextFloat() * 2.0F * (float)j);
         }

         pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
      }

   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRot) {
      switch (pRot) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Direction.Axis)pState.getValue(AXIS)) {
               case Z:
                  return pState.setValue(AXIS, Direction.Axis.X);
               case X:
                  return pState.setValue(AXIS, Direction.Axis.Z);
               default:
                  return pState;
            }
         default:
            return pState;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AXIS);
   }
}