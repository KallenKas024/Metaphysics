package net.minecraft.world.entity;

import javax.annotation.Nullable;

public interface TraceableEntity {
   /**
    * Returns null or the entityliving it was ignited by
    */
   @Nullable
   Entity getOwner();
}