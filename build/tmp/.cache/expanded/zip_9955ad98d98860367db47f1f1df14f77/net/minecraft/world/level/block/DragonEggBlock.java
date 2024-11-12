package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DragonEggBlock extends FallingBlock {
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public DragonEggBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      this.teleport(pState, pLevel, pPos);
      return InteractionResult.sidedSuccess(pLevel.isClientSide);
   }

   public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      this.teleport(pState, pLevel, pPos);
   }

   private void teleport(BlockState pState, Level pLevel, BlockPos pPos) {
      WorldBorder worldborder = pLevel.getWorldBorder();

      for(int i = 0; i < 1000; ++i) {
         BlockPos blockpos = pPos.offset(pLevel.random.nextInt(16) - pLevel.random.nextInt(16), pLevel.random.nextInt(8) - pLevel.random.nextInt(8), pLevel.random.nextInt(16) - pLevel.random.nextInt(16));
         if (pLevel.getBlockState(blockpos).isAir() && worldborder.isWithinBounds(blockpos)) {
            if (pLevel.isClientSide) {
               for(int j = 0; j < 128; ++j) {
                  double d0 = pLevel.random.nextDouble();
                  float f = (pLevel.random.nextFloat() - 0.5F) * 0.2F;
                  float f1 = (pLevel.random.nextFloat() - 0.5F) * 0.2F;
                  float f2 = (pLevel.random.nextFloat() - 0.5F) * 0.2F;
                  double d1 = Mth.lerp(d0, (double)blockpos.getX(), (double)pPos.getX()) + (pLevel.random.nextDouble() - 0.5D) + 0.5D;
                  double d2 = Mth.lerp(d0, (double)blockpos.getY(), (double)pPos.getY()) + pLevel.random.nextDouble() - 0.5D;
                  double d3 = Mth.lerp(d0, (double)blockpos.getZ(), (double)pPos.getZ()) + (pLevel.random.nextDouble() - 0.5D) + 0.5D;
                  pLevel.addParticle(ParticleTypes.PORTAL, d1, d2, d3, (double)f, (double)f1, (double)f2);
               }
            } else {
               pLevel.setBlock(blockpos, pState, 2);
               pLevel.removeBlock(pPos, false);
            }

            return;
         }
      }

   }

   protected int getDelayAfterPlace() {
      return 5;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}