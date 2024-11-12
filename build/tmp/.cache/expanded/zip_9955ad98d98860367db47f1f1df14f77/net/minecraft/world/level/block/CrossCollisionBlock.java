package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_52346_) -> {
      return p_52346_.getKey().getAxis().isHorizontal();
   }).collect(Util.toMap());
   protected final VoxelShape[] collisionShapeByIndex;
   protected final VoxelShape[] shapeByIndex;
   private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap<>();

   public CrossCollisionBlock(float pNodeWidth, float pExtensionWidth, float pNodeHeight, float pExtensionHeight, float pCollisionHeight, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.collisionShapeByIndex = this.makeShapes(pNodeWidth, pExtensionWidth, pCollisionHeight, 0.0F, pCollisionHeight);
      this.shapeByIndex = this.makeShapes(pNodeWidth, pExtensionWidth, pNodeHeight, 0.0F, pExtensionHeight);

      for(BlockState blockstate : this.stateDefinition.getPossibleStates()) {
         this.getAABBIndex(blockstate);
      }

   }

   protected VoxelShape[] makeShapes(float pNodeWidth, float pExtensionWidth, float pNodeHeight, float pExtensionBottom, float pExtensionHeight) {
      float f = 8.0F - pNodeWidth;
      float f1 = 8.0F + pNodeWidth;
      float f2 = 8.0F - pExtensionWidth;
      float f3 = 8.0F + pExtensionWidth;
      VoxelShape voxelshape = Block.box((double)f, 0.0D, (double)f, (double)f1, (double)pNodeHeight, (double)f1);
      VoxelShape voxelshape1 = Block.box((double)f2, (double)pExtensionBottom, 0.0D, (double)f3, (double)pExtensionHeight, (double)f3);
      VoxelShape voxelshape2 = Block.box((double)f2, (double)pExtensionBottom, (double)f2, (double)f3, (double)pExtensionHeight, 16.0D);
      VoxelShape voxelshape3 = Block.box(0.0D, (double)pExtensionBottom, (double)f2, (double)f3, (double)pExtensionHeight, (double)f3);
      VoxelShape voxelshape4 = Block.box((double)f2, (double)pExtensionBottom, (double)f2, 16.0D, (double)pExtensionHeight, (double)f3);
      VoxelShape voxelshape5 = Shapes.or(voxelshape1, voxelshape4);
      VoxelShape voxelshape6 = Shapes.or(voxelshape2, voxelshape3);
      VoxelShape[] avoxelshape = new VoxelShape[]{Shapes.empty(), voxelshape2, voxelshape3, voxelshape6, voxelshape1, Shapes.or(voxelshape2, voxelshape1), Shapes.or(voxelshape3, voxelshape1), Shapes.or(voxelshape6, voxelshape1), voxelshape4, Shapes.or(voxelshape2, voxelshape4), Shapes.or(voxelshape3, voxelshape4), Shapes.or(voxelshape6, voxelshape4), voxelshape5, Shapes.or(voxelshape2, voxelshape5), Shapes.or(voxelshape3, voxelshape5), Shapes.or(voxelshape6, voxelshape5)};

      for(int i = 0; i < 16; ++i) {
         avoxelshape[i] = Shapes.or(voxelshape, avoxelshape[i]);
      }

      return avoxelshape;
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return !pState.getValue(WATERLOGGED);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapeByIndex[this.getAABBIndex(pState)];
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.collisionShapeByIndex[this.getAABBIndex(pState)];
   }

   private static int indexFor(Direction pFacing) {
      return 1 << pFacing.get2DDataValue();
   }

   protected int getAABBIndex(BlockState pState) {
      return this.stateToIndex.computeIntIfAbsent(pState, (p_52366_) -> {
         int i = 0;
         if (p_52366_.getValue(NORTH)) {
            i |= indexFor(Direction.NORTH);
         }

         if (p_52366_.getValue(EAST)) {
            i |= indexFor(Direction.EAST);
         }

         if (p_52366_.getValue(SOUTH)) {
            i |= indexFor(Direction.SOUTH);
         }

         if (p_52366_.getValue(WEST)) {
            i |= indexFor(Direction.WEST);
         }

         return i;
      });
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRot) {
      switch (pRot) {
         case CLOCKWISE_180:
            return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(EAST, pState.getValue(WEST)).setValue(SOUTH, pState.getValue(NORTH)).setValue(WEST, pState.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return pState.setValue(NORTH, pState.getValue(EAST)).setValue(EAST, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(WEST)).setValue(WEST, pState.getValue(NORTH));
         case CLOCKWISE_90:
            return pState.setValue(NORTH, pState.getValue(WEST)).setValue(EAST, pState.getValue(NORTH)).setValue(SOUTH, pState.getValue(EAST)).setValue(WEST, pState.getValue(SOUTH));
         default:
            return pState;
      }
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      switch (pMirror) {
         case LEFT_RIGHT:
            return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(NORTH));
         case FRONT_BACK:
            return pState.setValue(EAST, pState.getValue(WEST)).setValue(WEST, pState.getValue(EAST));
         default:
            return super.mirror(pState, pMirror);
      }
   }
}