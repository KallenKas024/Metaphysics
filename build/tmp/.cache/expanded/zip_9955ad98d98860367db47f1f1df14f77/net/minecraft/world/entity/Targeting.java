package net.minecraft.world.entity;

import javax.annotation.Nullable;

public interface Targeting {
   /**
    * Gets the active target the Goal system uses for tracking
    */
   @Nullable
   LivingEntity getTarget();
}