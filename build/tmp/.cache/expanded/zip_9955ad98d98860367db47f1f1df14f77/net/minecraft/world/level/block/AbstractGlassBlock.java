package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractGlassBlock extends HalfTransparentBlock {
   protected AbstractGlassBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
      return Shapes.empty();
   }

   public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return 1.0F;
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return true;
   }
}