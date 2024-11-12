package net.minecraft.world.level.storage.loot.parameters;

import net.minecraft.resources.ResourceLocation;

/**
 * A parameter of a {@link LootContext}.
 * 
 * @see LootContextParams
 */
public class LootContextParam<T> {
   private final ResourceLocation name;

   public LootContextParam(ResourceLocation pName) {
      this.name = pName;
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public String toString() {
      return "<parameter " + this.name + ">";
   }
}