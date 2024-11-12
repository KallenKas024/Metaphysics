package net.minecraft.world.level.entity;

public interface LevelCallback<T> {
   void onCreated(T pEntity);

   void onDestroyed(T pEntity);

   void onTickingStart(T pEntity);

   void onTickingEnd(T pEntity);

   void onTrackingStart(T pEntity);

   void onTrackingEnd(T pEntity);

   void onSectionChange(T pEntity);
}