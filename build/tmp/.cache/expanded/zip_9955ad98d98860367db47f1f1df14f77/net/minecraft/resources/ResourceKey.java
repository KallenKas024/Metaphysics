package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * An immutable key for a resource, in terms of the name of its parent registry and its location in that registry.
 * <p>
 * {@link net.minecraft.core.Registry} uses this to return resource keys for registry objects via {@link
 * net.minecraft.core.Registry#getResourceKey(Object)}. It also uses this class to store its name, with the parent
 * registry name set to {@code minecraft:root}. When used in this way it is usually referred to as a "registry key".</p>
 * <p>
 * @param <T> The type of the resource represented by this {@code ResourceKey}, or the type of the registry if it is a
 * registry key.
 * @see net.minecraft.resources.ResourceLocation
 */
public class ResourceKey<T> implements Comparable<ResourceKey<?>> {
   private static final ConcurrentMap<ResourceKey.InternKey, ResourceKey<?>> VALUES = (new MapMaker()).weakValues().makeMap();
   /** The name of the parent registry of the resource. */
   private final ResourceLocation registryName;
   /** The location of the resource within the registry. */
   private final ResourceLocation location;

   public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> pRegistryKey) {
      return ResourceLocation.CODEC.xmap((p_195979_) -> {
         return create(pRegistryKey, p_195979_);
      }, ResourceKey::location);
   }

   /**
    * Constructs a new {@code ResourceKey} for a resource with the specified {@code location} within the registry
    * specified by the given {@code registryKey}.
    * 
    * @return the created resource key. The registry name is set to the location of the specified {@code registryKey}
    * and with the specified {@code location} as the location of the resource.
    */
   public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> pRegistryKey, ResourceLocation pLocation) {
      return create(pRegistryKey.location, pLocation);
   }

   /**
    * @return the created registry key. The registry name is set to {@code minecraft:root} and the location the
    * specified {@code registryName}.
    */
   public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation pLocation) {
      return create(BuiltInRegistries.ROOT_REGISTRY_NAME, pLocation);
   }

   private static <T> ResourceKey<T> create(ResourceLocation pRegistryName, ResourceLocation pLocation) {
      return (ResourceKey<T>)VALUES.computeIfAbsent(new ResourceKey.InternKey(pRegistryName, pLocation), (p_258225_) -> {
         return new ResourceKey(p_258225_.registry, p_258225_.location);
      });
   }

   private ResourceKey(ResourceLocation pRegistryName, ResourceLocation pLocation) {
      this.registryName = pRegistryName;
      this.location = pLocation;
   }

   public String toString() {
      return "ResourceKey[" + this.registryName + " / " + this.location + "]";
   }

   /**
    * @return {@code true} if this resource key is a direct child of the specified {@code registryKey}.
    */
   public boolean isFor(ResourceKey<? extends Registry<?>> pRegistryKey) {
      return this.registryName.equals(pRegistryKey.location());
   }

   public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey) {
      return this.isFor(pRegistryKey) ? Optional.of((ResourceKey<E>)this) : Optional.empty();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public ResourceLocation registry() {
      return this.registryName;
   }

   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      return location.equals(((ResourceKey<?>) o).location) && registryName.equals(((ResourceKey<?>) o).registryName);
   }

   @Override
   public int compareTo(ResourceKey<?> o) {
      int ret = this.registry().compareTo(o.registry());
      if (ret == 0) ret = this.location().compareTo(o.location());
      return ret;
   }

   static record InternKey(ResourceLocation registry, ResourceLocation location) {
   }
}
