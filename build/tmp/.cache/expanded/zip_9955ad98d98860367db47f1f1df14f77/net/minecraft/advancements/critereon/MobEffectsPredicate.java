package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobEffectsPredicate {
   public static final MobEffectsPredicate ANY = new MobEffectsPredicate(Collections.emptyMap());
   private final Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> effects;

   public MobEffectsPredicate(Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> pEffects) {
      this.effects = pEffects;
   }

   public static MobEffectsPredicate effects() {
      return new MobEffectsPredicate(Maps.newLinkedHashMap());
   }

   public MobEffectsPredicate and(MobEffect pEffect) {
      this.effects.put(pEffect, new MobEffectsPredicate.MobEffectInstancePredicate());
      return this;
   }

   public MobEffectsPredicate and(MobEffect pEffect, MobEffectsPredicate.MobEffectInstancePredicate pPredicate) {
      this.effects.put(pEffect, pPredicate);
      return this;
   }

   public boolean matches(Entity pEntity) {
      if (this == ANY) {
         return true;
      } else {
         return pEntity instanceof LivingEntity ? this.matches(((LivingEntity)pEntity).getActiveEffectsMap()) : false;
      }
   }

   public boolean matches(LivingEntity pEntity) {
      return this == ANY ? true : this.matches(pEntity.getActiveEffectsMap());
   }

   public boolean matches(Map<MobEffect, MobEffectInstance> pEffects) {
      if (this == ANY) {
         return true;
      } else {
         for(Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.effects.entrySet()) {
            MobEffectInstance mobeffectinstance = pEffects.get(entry.getKey());
            if (!entry.getValue().matches(mobeffectinstance)) {
               return false;
            }
         }

         return true;
      }
   }

   public static MobEffectsPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "effects");
         Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map = Maps.newLinkedHashMap();

         for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            ResourceLocation resourcelocation = new ResourceLocation(entry.getKey());
            MobEffect mobeffect = BuiltInRegistries.MOB_EFFECT.getOptional(resourcelocation).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown effect '" + resourcelocation + "'");
            });
            MobEffectsPredicate.MobEffectInstancePredicate mobeffectspredicate$mobeffectinstancepredicate = MobEffectsPredicate.MobEffectInstancePredicate.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()));
            map.put(mobeffect, mobeffectspredicate$mobeffectinstancepredicate);
         }

         return new MobEffectsPredicate(map);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();

         for(Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.effects.entrySet()) {
            jsonobject.add(BuiltInRegistries.MOB_EFFECT.getKey(entry.getKey()).toString(), entry.getValue().serializeToJson());
         }

         return jsonobject;
      }
   }

   public static class MobEffectInstancePredicate {
      private final MinMaxBounds.Ints amplifier;
      private final MinMaxBounds.Ints duration;
      @Nullable
      private final Boolean ambient;
      @Nullable
      private final Boolean visible;

      public MobEffectInstancePredicate(MinMaxBounds.Ints pAmplifier, MinMaxBounds.Ints pDuration, @Nullable Boolean pAmbient, @Nullable Boolean pVisible) {
         this.amplifier = pAmplifier;
         this.duration = pDuration;
         this.ambient = pAmbient;
         this.visible = pVisible;
      }

      public MobEffectInstancePredicate() {
         this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, (Boolean)null, (Boolean)null);
      }

      public boolean matches(@Nullable MobEffectInstance pEffect) {
         if (pEffect == null) {
            return false;
         } else if (!this.amplifier.matches(pEffect.getAmplifier())) {
            return false;
         } else if (!this.duration.matches(pEffect.getDuration())) {
            return false;
         } else if (this.ambient != null && this.ambient != pEffect.isAmbient()) {
            return false;
         } else {
            return this.visible == null || this.visible == pEffect.isVisible();
         }
      }

      public JsonElement serializeToJson() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("amplifier", this.amplifier.serializeToJson());
         jsonobject.add("duration", this.duration.serializeToJson());
         jsonobject.addProperty("ambient", this.ambient);
         jsonobject.addProperty("visible", this.visible);
         return jsonobject;
      }

      public static MobEffectsPredicate.MobEffectInstancePredicate fromJson(JsonObject pJson) {
         MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("amplifier"));
         MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(pJson.get("duration"));
         Boolean obool = pJson.has("ambient") ? GsonHelper.getAsBoolean(pJson, "ambient") : null;
         Boolean obool1 = pJson.has("visible") ? GsonHelper.getAsBoolean(pJson, "visible") : null;
         return new MobEffectsPredicate.MobEffectInstancePredicate(minmaxbounds$ints, minmaxbounds$ints1, obool, obool1);
      }
   }
}