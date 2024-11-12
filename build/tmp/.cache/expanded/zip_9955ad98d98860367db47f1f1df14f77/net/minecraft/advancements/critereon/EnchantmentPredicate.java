package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentPredicate {
   public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
   public static final EnchantmentPredicate[] NONE = new EnchantmentPredicate[0];
   @Nullable
   private final Enchantment enchantment;
   private final MinMaxBounds.Ints level;

   public EnchantmentPredicate() {
      this.enchantment = null;
      this.level = MinMaxBounds.Ints.ANY;
   }

   public EnchantmentPredicate(@Nullable Enchantment pEnchantment, MinMaxBounds.Ints pLevel) {
      this.enchantment = pEnchantment;
      this.level = pLevel;
   }

   public boolean containedIn(Map<Enchantment, Integer> pEnchantments) {
      if (this.enchantment != null) {
         if (!pEnchantments.containsKey(this.enchantment)) {
            return false;
         }

         int i = pEnchantments.get(this.enchantment);
         if (this.level != MinMaxBounds.Ints.ANY && !this.level.matches(i)) {
            return false;
         }
      } else if (this.level != MinMaxBounds.Ints.ANY) {
         for(Integer integer : pEnchantments.values()) {
            if (this.level.matches(integer)) {
               return true;
            }
         }

         return false;
      }

      return true;
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.enchantment != null) {
            jsonobject.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment).toString());
         }

         jsonobject.add("levels", this.level.serializeToJson());
         return jsonobject;
      }
   }

   public static EnchantmentPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "enchantment");
         Enchantment enchantment = null;
         if (jsonobject.has("enchantment")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "enchantment"));
            enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown enchantment '" + resourcelocation + "'");
            });
         }

         MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(jsonobject.get("levels"));
         return new EnchantmentPredicate(enchantment, minmaxbounds$ints);
      } else {
         return ANY;
      }
   }

   public static EnchantmentPredicate[] fromJsonArray(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonArray jsonarray = GsonHelper.convertToJsonArray(pJson, "enchantments");
         EnchantmentPredicate[] aenchantmentpredicate = new EnchantmentPredicate[jsonarray.size()];

         for(int i = 0; i < aenchantmentpredicate.length; ++i) {
            aenchantmentpredicate[i] = fromJson(jsonarray.get(i));
         }

         return aenchantmentpredicate;
      } else {
         return NONE;
      }
   }
}