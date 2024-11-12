package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity {
   private final ChestLidController chestLidController = new ChestLidController();
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void onOpen(Level p_155531_, BlockPos p_155532_, BlockState p_155533_) {
         p_155531_.playSound((Player)null, (double)p_155532_.getX() + 0.5D, (double)p_155532_.getY() + 0.5D, (double)p_155532_.getZ() + 0.5D, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, p_155531_.random.nextFloat() * 0.1F + 0.9F);
      }

      protected void onClose(Level p_155541_, BlockPos p_155542_, BlockState p_155543_) {
         p_155541_.playSound((Player)null, (double)p_155542_.getX() + 0.5D, (double)p_155542_.getY() + 0.5D, (double)p_155542_.getZ() + 0.5D, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, p_155541_.random.nextFloat() * 0.1F + 0.9F);
      }

      protected void openerCountChanged(Level p_155535_, BlockPos p_155536_, BlockState p_155537_, int p_155538_, int p_155539_) {
         p_155535_.blockEvent(EnderChestBlockEntity.this.worldPosition, Blocks.ENDER_CHEST, 1, p_155539_);
      }

      protected boolean isOwnContainer(Player p_155529_) {
         return p_155529_.getEnderChestInventory().isActiveChest(EnderChestBlockEntity.this);
      }
   };

   public EnderChestBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.ENDER_CHEST, pPos, pBlockState);
   }

   public static void lidAnimateTick(Level pLevel, BlockPos pPos, BlockState pState, EnderChestBlockEntity pBlockEntity) {
      pBlockEntity.chestLidController.tickLid();
   }

   public boolean triggerEvent(int pId, int pType) {
      if (pId == 1) {
         this.chestLidController.shouldBeOpen(pType > 0);
         return true;
      } else {
         return super.triggerEvent(pId, pType);
      }
   }

   public void startOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public void stopOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public boolean stillValid(Player pPlayer) {
      return Container.stillValidBlockEntity(this, pPlayer);
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public float getOpenNess(float pPartialTicks) {
      return this.chestLidController.getOpenness(pPartialTicks);
   }
}