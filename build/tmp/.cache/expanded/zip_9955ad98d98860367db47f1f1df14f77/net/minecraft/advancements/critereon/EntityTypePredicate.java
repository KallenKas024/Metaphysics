package net.minecraft.advancements.critereon;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

public abstract class EntityTypePredicate {
   public static final EntityTypePredicate ANY = new EntityTypePredicate() {
      public boolean matches(EntityType<?> p_37652_) {
         return true;
      }

      public JsonElement serializeToJson() {
         return JsonNull.INSTANCE;
      }
   };
   private static final Joiner COMMA_JOINER = Joiner.on(", ");

   public abstract boolean matches(EntityType<?> pType);

   public abstract JsonElement serializeToJson();

   public static EntityTypePredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         String s = GsonHelper.convertToString(pJson, "type");
         if (s.startsWith("#")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(s.substring(1));
            return new EntityTypePredicate.TagPredicate(TagKey.create(Registries.ENTITY_TYPE, resourcelocation1));
         } else {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown entity type '" + resourcelocation + "', valid types are: " + COMMA_JOINER.join(BuiltInRegistries.ENTITY_TYPE.keySet()));
            });
            return new EntityTypePredicate.TypePredicate(entitytype);
         }
      } else {
         return ANY;
      }
   }

   public static EntityTypePredicate of(EntityType<?> pType) {
      return new EntityTypePredicate.TypePredicate(pType);
   }

   public static EntityTypePredicate of(TagKey<EntityType<?>> pTag) {
      return new EntityTypePredicate.TagPredicate(pTag);
   }

   static class TagPredicate extends EntityTypePredicate {
      private final TagKey<EntityType<?>> tag;

      public TagPredicate(TagKey<EntityType<?>> pTag) {
         this.tag = pTag;
      }

      public boolean matches(EntityType<?> pType) {
         return pType.is(this.tag);
      }

      public JsonElement serializeToJson() {
         return new JsonPrimitive("#" + this.tag.location());
      }
   }

   static class TypePredicate extends EntityTypePredicate {
      private final EntityType<?> type;

      public TypePredicate(EntityType<?> pType) {
         this.type = pType;
      }

      public boolean matches(EntityType<?> pType) {
         return this.type == pType;
      }

      public JsonElement serializeToJson() {
         return new JsonPrimitive(BuiltInRegistries.ENTITY_TYPE.getKey(this.type).toString());
      }
   }
}