package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * A NbtProvider that provides either the {@linkplain LootContextParams#BLOCK_ENTITY block entity}'s NBT data or an
 * entity's NBT data based on an {@link LootContext.EntityTarget}.
 */
public class ContextNbtProvider implements NbtProvider {
   private static final String BLOCK_ENTITY_ID = "block_entity";
   private static final ContextNbtProvider.Getter BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.Getter() {
      public Tag get(LootContext p_165582_) {
         BlockEntity blockentity = p_165582_.getParamOrNull(LootContextParams.BLOCK_ENTITY);
         return blockentity != null ? blockentity.saveWithFullMetadata() : null;
      }

      public String getId() {
         return "block_entity";
      }

      public Set<LootContextParam<?>> getReferencedContextParams() {
         return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
      }
   };
   public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
   final ContextNbtProvider.Getter getter;

   private static ContextNbtProvider.Getter forEntity(final LootContext.EntityTarget pEntityTarget) {
      return new ContextNbtProvider.Getter() {
         @Nullable
         public Tag get(LootContext p_165589_) {
            Entity entity = p_165589_.getParamOrNull(pEntityTarget.getParam());
            return entity != null ? NbtPredicate.getEntityTagToCompare(entity) : null;
         }

         public String getId() {
            return pEntityTarget.getName();
         }

         public Set<LootContextParam<?>> getReferencedContextParams() {
            return ImmutableSet.of(pEntityTarget.getParam());
         }
      };
   }

   private ContextNbtProvider(ContextNbtProvider.Getter pGetter) {
      this.getter = pGetter;
   }

   public LootNbtProviderType getType() {
      return NbtProviders.CONTEXT;
   }

   @Nullable
   public Tag get(LootContext pLootContext) {
      return this.getter.get(pLootContext);
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.getter.getReferencedContextParams();
   }

   public static NbtProvider forContextEntity(LootContext.EntityTarget pEntityTarget) {
      return new ContextNbtProvider(forEntity(pEntityTarget));
   }

   static ContextNbtProvider createFromContext(String pTargetName) {
      if (pTargetName.equals("block_entity")) {
         return new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
      } else {
         LootContext.EntityTarget lootcontext$entitytarget = LootContext.EntityTarget.getByName(pTargetName);
         return new ContextNbtProvider(forEntity(lootcontext$entitytarget));
      }
   }

   interface Getter {
      @Nullable
      Tag get(LootContext pLootContext);

      String getId();

      Set<LootContextParam<?>> getReferencedContextParams();
   }

   public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextNbtProvider> {
      public JsonElement serialize(ContextNbtProvider p_165597_, JsonSerializationContext p_165598_) {
         return new JsonPrimitive(p_165597_.getter.getId());
      }

      public ContextNbtProvider deserialize(JsonElement p_165603_, JsonDeserializationContext p_165604_) {
         String s = p_165603_.getAsString();
         return ContextNbtProvider.createFromContext(s);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextNbtProvider> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165610_, ContextNbtProvider p_165611_, JsonSerializationContext p_165612_) {
         p_165610_.addProperty("target", p_165611_.getter.getId());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public ContextNbtProvider deserialize(JsonObject p_165618_, JsonDeserializationContext p_165619_) {
         String s = GsonHelper.getAsString(p_165618_, "target");
         return ContextNbtProvider.createFromContext(s);
      }
   }
}
