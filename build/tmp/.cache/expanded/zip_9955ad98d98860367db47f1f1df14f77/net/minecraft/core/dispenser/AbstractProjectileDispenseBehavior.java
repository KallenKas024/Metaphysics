package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public abstract class AbstractProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
   /**
    * Dispense the specified stack, play the dispense sound, and spawn particles.
    */
   public ItemStack execute(BlockSource pSource, ItemStack pStack) {
      Level level = pSource.getLevel();
      Position position = DispenserBlock.getDispensePosition(pSource);
      Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
      Projectile projectile = this.getProjectile(level, position, pStack);
      projectile.shoot((double)direction.getStepX(), (double)((float)direction.getStepY() + 0.1F), (double)direction.getStepZ(), this.getPower(), this.getUncertainty());
      level.addFreshEntity(projectile);
      pStack.shrink(1);
      return pStack;
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(BlockSource pSource) {
      pSource.getLevel().levelEvent(1002, pSource.getPos(), 0);
   }

   /**
    * Return the projectile entity spawned by this dispense behavior.
    */
   protected abstract Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack);

   protected float getUncertainty() {
      return 6.0F;
   }

   protected float getPower() {
      return 1.1F;
   }
}