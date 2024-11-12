package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnvilBlock extends FallingBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   private static final VoxelShape BASE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
   private static final VoxelShape X_LEG1 = Block.box(3.0D, 4.0D, 4.0D, 13.0D, 5.0D, 12.0D);
   private static final VoxelShape X_LEG2 = Block.box(4.0D, 5.0D, 6.0D, 12.0D, 10.0D, 10.0D);
   private static final VoxelShape X_TOP = Block.box(0.0D, 10.0D, 3.0D, 16.0D, 16.0D, 13.0D);
   private static final VoxelShape Z_LEG1 = Block.box(4.0D, 4.0D, 3.0D, 12.0D, 5.0D, 13.0D);
   private static final VoxelShape Z_LEG2 = Block.box(6.0D, 5.0D, 4.0D, 10.0D, 10.0D, 12.0D);
   private static final VoxelShape Z_TOP = Block.box(3.0D, 10.0D, 0.0D, 13.0D, 16.0D, 16.0D);
   private static final VoxelShape X_AXIS_AABB = Shapes.or(BASE, X_LEG1, X_LEG2, X_TOP);
   private static final VoxelShape Z_AXIS_AABB = Shapes.or(BASE, Z_LEG1, Z_LEG2, Z_TOP);
   private static final Component CONTAINER_TITLE = Component.translatable("container.repair");
   private static final float FALL_DAMAGE_PER_DISTANCE = 2.0F;
   private static final int FALL_DAMAGE_MAX = 40;

   public AnvilBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getClockWise());
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         pPlayer.awardStat(Stats.INTERACT_WITH_ANVIL);
         return InteractionResult.CONSUME;
      }
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      return new SimpleMenuProvider((p_48785_, p_48786_, p_48787_) -> {
         return new AnvilMenu(p_48785_, p_48786_, ContainerLevelAccess.create(pLevel, pPos));
      }, CONTAINER_TITLE);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Direction direction = pState.getValue(FACING);
      return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
   }

   protected void falling(FallingBlockEntity pFallingEntity) {
      pFallingEntity.setHurtsEntities(2.0F, 40);
   }

   public void onLand(Level pLevel, BlockPos pPos, BlockState pState, BlockState pReplaceableState, FallingBlockEntity pFallingBlock) {
      if (!pFallingBlock.isSilent()) {
         pLevel.levelEvent(1031, pPos, 0);
      }

   }

   public void onBrokenAfterFall(Level pLevel, BlockPos pPos, FallingBlockEntity pFallingBlock) {
      if (!pFallingBlock.isSilent()) {
         pLevel.levelEvent(1029, pPos, 0);
      }

   }

   public DamageSource getFallDamageSource(Entity pEntity) {
      return pEntity.damageSources().anvil(pEntity);
   }

   @Nullable
   public static BlockState damage(BlockState pState) {
      if (pState.is(Blocks.ANVIL)) {
         return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(FACING, pState.getValue(FACING));
      } else {
         return pState.is(Blocks.CHIPPED_ANVIL) ? Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(FACING, pState.getValue(FACING)) : null;
      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRot) {
      return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return pState.getMapColor(pReader, pPos).col;
   }
}