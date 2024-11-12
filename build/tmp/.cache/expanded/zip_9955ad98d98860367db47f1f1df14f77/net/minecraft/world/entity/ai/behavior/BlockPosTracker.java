package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlockPosTracker implements PositionTracker {
   private final BlockPos blockPos;
   private final Vec3 centerPosition;

   public BlockPosTracker(BlockPos pBlockPos) {
      this.blockPos = pBlockPos.immutable();
      this.centerPosition = Vec3.atCenterOf(pBlockPos);
   }

   public BlockPosTracker(Vec3 pCenterPosition) {
      this.blockPos = BlockPos.containing(pCenterPosition);
      this.centerPosition = pCenterPosition;
   }

   public Vec3 currentPosition() {
      return this.centerPosition;
   }

   public BlockPos currentBlockPosition() {
      return this.blockPos;
   }

   public boolean isVisibleBy(LivingEntity pEntity) {
      return true;
   }

   public String toString() {
      return "BlockPosTracker{blockPos=" + this.blockPos + ", centerPosition=" + this.centerPosition + "}";
   }
}