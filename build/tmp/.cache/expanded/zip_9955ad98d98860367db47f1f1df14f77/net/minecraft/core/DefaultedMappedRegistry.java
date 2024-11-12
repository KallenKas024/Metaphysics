package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class DefaultedMappedRegistry<T> extends MappedRegistry<T> implements DefaultedRegistry<T> {
   private final ResourceLocation defaultKey;
   private Holder.Reference<T> defaultValue;

   public DefaultedMappedRegistry(String pDefaultKey, ResourceKey<? extends Registry<T>> pKey, Lifecycle pRegistryLifecycle, boolean pHasIntrusiveHolders) {
      super(pKey, pRegistryLifecycle, pHasIntrusiveHolders);
      this.defaultKey = new ResourceLocation(pDefaultKey);
   }

   public Holder.Reference<T> registerMapping(int pId, ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle) {
      Holder.Reference<T> reference = super.registerMapping(pId, pKey, pValue, pLifecycle);
      if (this.defaultKey.equals(pKey.location())) {
         this.defaultValue = reference;
      }

      return reference;
   }

   /**
    * @return the integer ID used to identify the given object
    */
   public int getId(@Nullable T pValue) {
      int i = super.getId(pValue);
      return i == -1 ? super.getId(this.defaultValue.value()) : i;
   }

   /**
    * @return the name used to identify the given object within this registry or {@code null} if the object is not
    * within this registry
    */
   @Nonnull
   public ResourceLocation getKey(T pValue) {
      ResourceLocation resourcelocation = super.getKey(pValue);
      return resourcelocation == null ? this.defaultKey : resourcelocation;
   }

   @Nonnull
   public T get(@Nullable ResourceLocation pName) {
      T t = super.get(pName);
      return (T)(t == null ? this.defaultValue.value() : t);
   }

   public Optional<T> getOptional(@Nullable ResourceLocation pName) {
      return Optional.ofNullable(super.get(pName));
   }

   @Nonnull
   public T byId(int pId) {
      T t = super.byId(pId);
      return (T)(t == null ? this.defaultValue.value() : t);
   }

   public Optional<Holder.Reference<T>> getRandom(RandomSource pRandom) {
      return super.getRandom(pRandom).or(() -> {
         return Optional.of(this.defaultValue);
      });
   }

   public ResourceLocation getDefaultKey() {
      return this.defaultKey;
   }
}