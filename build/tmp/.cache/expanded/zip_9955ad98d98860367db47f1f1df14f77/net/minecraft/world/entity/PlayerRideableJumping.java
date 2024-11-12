package net.minecraft.world.entity;

public interface PlayerRideableJumping extends PlayerRideable {
   void onPlayerJump(int pJumpPower);

   boolean canJump();

   void handleStartJump(int pJumpPower);

   void handleStopJump();

   default int getJumpCooldown() {
      return 0;
   }
}