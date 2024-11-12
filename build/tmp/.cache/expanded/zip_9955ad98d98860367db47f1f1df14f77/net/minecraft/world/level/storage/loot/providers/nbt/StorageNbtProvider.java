package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * An NbtProvider that provides NBT data from a named {@link CommandStorage}.
 */
public class StorageNbtProvider implements NbtProvider {
   final ResourceLocation id;

   StorageNbtProvider(ResourceLocation pResourceLocation) {
      this.id = pResourceLocation;
   }

   public LootNbtProviderType getType() {
      return NbtProviders.STORAGE;
   }

   @Nullable
   public Tag get(LootContext pLootContext) {
      return pLootContext.getLevel().getServer().getCommandStorage().get(this.id);
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of();
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<StorageNbtProvider> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165643_, StorageNbtProvider p_165644_, JsonSerializationContext p_165645_) {
         p_165643_.addProperty("source", p_165644_.id.toString());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public StorageNbtProvider deserialize(JsonObject p_165651_, JsonDeserializationContext p_165652_) {
         String s = GsonHelper.getAsString(p_165651_, "source");
         return new StorageNbtProvider(new ResourceLocation(s));
      }
   }
}