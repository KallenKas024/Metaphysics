package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
   private static final String VARIANT_KEY = "variant";
   final Codec<V> variantCodec;
   final Function<Entity, Optional<V>> getter;
   final EntitySubPredicate.Type type;

   public static <V> EntityVariantPredicate<V> create(Registry<V> pRegistry, Function<Entity, Optional<V>> pGetter) {
      return new EntityVariantPredicate<>(pRegistry.byNameCodec(), pGetter);
   }

   public static <V> EntityVariantPredicate<V> create(Codec<V> pVariantCodec, Function<Entity, Optional<V>> pGetter) {
      return new EntityVariantPredicate<>(pVariantCodec, pGetter);
   }

   private EntityVariantPredicate(Codec<V> pVariantCodec, Function<Entity, Optional<V>> pGetter) {
      this.variantCodec = pVariantCodec;
      this.getter = pGetter;
      this.type = (p_262519_) -> {
         JsonElement jsonelement = p_262519_.get("variant");
         if (jsonelement == null) {
            throw new JsonParseException("Missing variant field");
         } else {
            V v = Util.getOrThrow(pVariantCodec.decode(new Dynamic<>(JsonOps.INSTANCE, jsonelement)), JsonParseException::new).getFirst();
            return this.createPredicate(v);
         }
      };
   }

   public EntitySubPredicate.Type type() {
      return this.type;
   }

   public EntitySubPredicate createPredicate(final V pVariant) {
      return new EntitySubPredicate() {
         public boolean matches(Entity p_219105_, ServerLevel p_219106_, @Nullable Vec3 p_219107_) {
            return EntityVariantPredicate.this.getter.apply(p_219105_).filter((p_219110_) -> {
               return p_219110_.equals(pVariant);
            }).isPresent();
         }

         public JsonObject serializeCustomData() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("variant", Util.getOrThrow(EntityVariantPredicate.this.variantCodec.encodeStart(JsonOps.INSTANCE, pVariant), (p_262521_) -> {
               return new JsonParseException("Can't serialize variant " + pVariant + ", message " + p_262521_);
            }));
            return jsonobject;
         }

         public EntitySubPredicate.Type type() {
            return EntityVariantPredicate.this.type;
         }
      };
   }
}