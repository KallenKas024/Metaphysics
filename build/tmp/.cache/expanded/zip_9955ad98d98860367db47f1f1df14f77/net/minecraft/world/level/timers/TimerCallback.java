package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface TimerCallback<T> {
   void handle(T pObj, TimerQueue<T> pManager, long pGameTime);

   public abstract static class Serializer<T, C extends TimerCallback<T>> {
      private final ResourceLocation id;
      private final Class<?> cls;

      public Serializer(ResourceLocation pId, Class<?> pCls) {
         this.id = pId;
         this.cls = pCls;
      }

      public ResourceLocation getId() {
         return this.id;
      }

      public Class<?> getCls() {
         return this.cls;
      }

      public abstract void serialize(CompoundTag pTag, C pCallback);

      public abstract C deserialize(CompoundTag pTag);
   }
}