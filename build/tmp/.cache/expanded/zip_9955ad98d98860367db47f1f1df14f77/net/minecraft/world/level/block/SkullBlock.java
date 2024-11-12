package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SkullBlock extends AbstractSkullBlock {
   public static final int MAX = RotationSegment.getMaxSegmentIndex();
   private static final int ROTATIONS = MAX + 1;
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);
   protected static final VoxelShape PIGLIN_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);

   public SkullBlock(SkullBlock.Type pType, BlockBehaviour.Properties pProperties) {
      super(pType, pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.getType() == SkullBlock.Types.PIGLIN ? PIGLIN_SHAPE : SHAPE;
   }

   public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return Shapes.empty();
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(ROTATION, Integer.valueOf(RotationSegment.convertToSegment(pContext.getRotation())));
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(ROTATION, Integer.valueOf(pRotation.rotate(pState.getValue(ROTATION), ROTATIONS)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(ROTATION, Integer.valueOf(pMirror.mirror(pState.getValue(ROTATION), ROTATIONS)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(ROTATION);
   }

   public interface Type {
   }

   public static enum Types implements SkullBlock.Type {
      SKELETON,
      WITHER_SKELETON,
      PLAYER,
      ZOMBIE,
      CREEPER,
      PIGLIN,
      DRAGON;
   }
}