package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.Vec3;

public class EntityTracker implements PositionTracker {
   private final Entity entity;
   private final boolean trackEyeHeight;

   public EntityTracker(Entity pEntity, boolean pTrackEyeHeight) {
      this.entity = pEntity;
      this.trackEyeHeight = pTrackEyeHeight;
   }

   public Vec3 currentPosition() {
      return this.trackEyeHeight ? this.entity.position().add(0.0D, (double)this.entity.getEyeHeight(), 0.0D) : this.entity.position();
   }

   public BlockPos currentBlockPosition() {
      return this.entity.blockPosition();
   }

   public boolean isVisibleBy(LivingEntity pEntity) {
      Entity $$3 = this.entity;
      if ($$3 instanceof LivingEntity livingentity) {
         if (!livingentity.isAlive()) {
            return false;
         } else {
            Optional<NearestVisibleLivingEntities> optional = pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            return optional.isPresent() && optional.get().contains(livingentity);
         }
      } else {
         return true;
      }
   }

   public Entity getEntity() {
      return this.entity;
   }

   public String toString() {
      return "EntityTracker for " + this.entity;
   }
}