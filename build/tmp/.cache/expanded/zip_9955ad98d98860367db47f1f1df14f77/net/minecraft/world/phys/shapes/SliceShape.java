package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;

public class SliceShape extends VoxelShape {
   private final VoxelShape delegate;
   private final Direction.Axis axis;
   private static final DoubleList SLICE_COORDS = new CubePointRange(1);

   public SliceShape(VoxelShape pDelegate, Direction.Axis pAxis, int pIndex) {
      super(makeSlice(pDelegate.shape, pAxis, pIndex));
      this.delegate = pDelegate;
      this.axis = pAxis;
   }

   private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape pShape, Direction.Axis pAxis, int pIndex) {
      return new SubShape(pShape, pAxis.choose(pIndex, 0, 0), pAxis.choose(0, pIndex, 0), pAxis.choose(0, 0, pIndex), pAxis.choose(pIndex + 1, pShape.xSize, pShape.xSize), pAxis.choose(pShape.ySize, pIndex + 1, pShape.ySize), pAxis.choose(pShape.zSize, pShape.zSize, pIndex + 1));
   }

   protected DoubleList getCoords(Direction.Axis pAxis) {
      return pAxis == this.axis ? SLICE_COORDS : this.delegate.getCoords(pAxis);
   }
}