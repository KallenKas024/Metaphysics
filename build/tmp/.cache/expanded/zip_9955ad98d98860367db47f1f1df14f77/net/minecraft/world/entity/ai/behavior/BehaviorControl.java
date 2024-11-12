package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface BehaviorControl<E extends LivingEntity> {
   Behavior.Status getStatus();

   boolean tryStart(ServerLevel pLevel, E pEntity, long pGameTime);

   void tickOrStop(ServerLevel pLevel, E pEntity, long pGameTime);

   void doStop(ServerLevel pLevel, E pEntity, long pGameTime);

   String debugString();
}