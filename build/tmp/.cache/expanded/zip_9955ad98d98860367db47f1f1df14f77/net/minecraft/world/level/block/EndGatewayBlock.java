package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class EndGatewayBlock extends BaseEntityBlock {
   public EndGatewayBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new TheEndGatewayBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.END_GATEWAY, pLevel.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::teleportTick);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof TheEndGatewayBlockEntity) {
         int i = ((TheEndGatewayBlockEntity)blockentity).getParticleAmount();

         for(int j = 0; j < i; ++j) {
            double d0 = (double)pPos.getX() + pRandom.nextDouble();
            double d1 = (double)pPos.getY() + pRandom.nextDouble();
            double d2 = (double)pPos.getZ() + pRandom.nextDouble();
            double d3 = (pRandom.nextDouble() - 0.5D) * 0.5D;
            double d4 = (pRandom.nextDouble() - 0.5D) * 0.5D;
            double d5 = (pRandom.nextDouble() - 0.5D) * 0.5D;
            int k = pRandom.nextInt(2) * 2 - 1;
            if (pRandom.nextBoolean()) {
               d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)k;
               d5 = (double)(pRandom.nextFloat() * 2.0F * (float)k);
            } else {
               d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)k;
               d3 = (double)(pRandom.nextFloat() * 2.0F * (float)k);
            }

            pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
         }

      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }

   public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
      return false;
   }
}