package net.minecraft.world.entity.ai.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

public class WalkTarget {
   private final PositionTracker target;
   private final float speedModifier;
   private final int closeEnoughDist;

   /**
    * Constructs a walk target using a vector that's directly converted to a BlockPos.
    */
   public WalkTarget(BlockPos pPos, float pSpeedModifier, int pCloseEnoughDist) {
      this(new BlockPosTracker(pPos), pSpeedModifier, pCloseEnoughDist);
   }

   /**
    * Constructs a walk target using a vector that's directly converted to a BlockPos.
    */
   public WalkTarget(Vec3 pVectorPos, float pSpeedModifier, int pCloseEnoughDist) {
      this(new BlockPosTracker(BlockPos.containing(pVectorPos)), pSpeedModifier, pCloseEnoughDist);
   }

   /**
    * Constructs a walk target using a vector that's directly converted to a BlockPos.
    */
   public WalkTarget(Entity pTargetEntity, float pSpeedModifier, int pCloseEnoughDist) {
      this(new EntityTracker(pTargetEntity, false), pSpeedModifier, pCloseEnoughDist);
   }

   /**
    * Constructs a walk target using a vector that's directly converted to a BlockPos.
    */
   public WalkTarget(PositionTracker pTarget, float pSpeedModifier, int pCloseEnoughDist) {
      this.target = pTarget;
      this.speedModifier = pSpeedModifier;
      this.closeEnoughDist = pCloseEnoughDist;
   }

   public PositionTracker getTarget() {
      return this.target;
   }

   public float getSpeedModifier() {
      return this.speedModifier;
   }

   public int getCloseEnoughDist() {
      return this.closeEnoughDist;
   }
}