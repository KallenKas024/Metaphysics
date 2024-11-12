package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterlilyBlock extends BushBlock {
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);

   public WaterlilyBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      super.entityInside(pState, pLevel, pPos, pEntity);
      if (pLevel instanceof ServerLevel && pEntity instanceof Boat) {
         pLevel.destroyBlock(new BlockPos(pPos), true, pEntity);
      }

   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return AABB;
   }

   protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      FluidState fluidstate = pLevel.getFluidState(pPos);
      FluidState fluidstate1 = pLevel.getFluidState(pPos.above());
      return (fluidstate.getType() == Fluids.WATER || pState.getBlock() instanceof IceBlock) && fluidstate1.getType() == Fluids.EMPTY;
   }
}