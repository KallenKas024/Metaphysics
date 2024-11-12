package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public class EntityHitResult extends HitResult {
   private final Entity entity;

   public EntityHitResult(Entity pEntity) {
      this(pEntity, pEntity.position());
   }

   public EntityHitResult(Entity pEntity, Vec3 pLocation) {
      super(pLocation);
      this.entity = pEntity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public HitResult.Type getType() {
      return HitResult.Type.ENTITY;
   }
}