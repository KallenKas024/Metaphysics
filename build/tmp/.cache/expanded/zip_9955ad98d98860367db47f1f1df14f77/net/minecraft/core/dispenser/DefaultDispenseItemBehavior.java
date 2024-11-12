package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior implements DispenseItemBehavior {
   public final ItemStack dispense(BlockSource pSource, ItemStack pStack) {
      ItemStack itemstack = this.execute(pSource, pStack);
      this.playSound(pSource);
      this.playAnimation(pSource, pSource.getBlockState().getValue(DispenserBlock.FACING));
      return itemstack;
   }

   /**
    * Dispense the specified stack, play the dispense sound, and spawn particles.
    */
   protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
      Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
      Position position = DispenserBlock.getDispensePosition(pSource);
      ItemStack itemstack = pStack.split(1);
      spawnItem(pSource.getLevel(), itemstack, 6, direction, position);
      return pStack;
   }

   public static void spawnItem(Level pLevel, ItemStack pStack, int pSpeed, Direction pFacing, Position pPosition) {
      double d0 = pPosition.x();
      double d1 = pPosition.y();
      double d2 = pPosition.z();
      if (pFacing.getAxis() == Direction.Axis.Y) {
         d1 -= 0.125D;
      } else {
         d1 -= 0.15625D;
      }

      ItemEntity itementity = new ItemEntity(pLevel, d0, d1, d2, pStack);
      double d3 = pLevel.random.nextDouble() * 0.1D + 0.2D;
      itementity.setDeltaMovement(pLevel.random.triangle((double)pFacing.getStepX() * d3, 0.0172275D * (double)pSpeed), pLevel.random.triangle(0.2D, 0.0172275D * (double)pSpeed), pLevel.random.triangle((double)pFacing.getStepZ() * d3, 0.0172275D * (double)pSpeed));
      pLevel.addFreshEntity(itementity);
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(BlockSource pSource) {
      pSource.getLevel().levelEvent(1000, pSource.getPos(), 0);
   }

   /**
    * Order clients to display dispense particles from the specified block and facing.
    */
   protected void playAnimation(BlockSource pSource, Direction pFacing) {
      pSource.getLevel().levelEvent(2000, pSource.getPos(), pFacing.get3DDataValue());
   }
}