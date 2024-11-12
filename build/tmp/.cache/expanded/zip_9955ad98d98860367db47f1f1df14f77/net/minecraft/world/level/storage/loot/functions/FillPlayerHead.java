package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that applies the {@code "SkullOwner"} NBT tag to any player heads based on the given {@link
 * LootContext.EntityTarget}.
 * If the given target does not resolve to a player, nothing happens.
 */
public class FillPlayerHead extends LootItemConditionalFunction {
   final LootContext.EntityTarget entityTarget;

   public FillPlayerHead(LootItemCondition[] pConditions, LootContext.EntityTarget pEntityTarget) {
      super(pConditions);
      this.entityTarget = pEntityTarget;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.FILL_PLAYER_HEAD;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.entityTarget.getParam());
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.is(Items.PLAYER_HEAD)) {
         Entity entity = pContext.getParamOrNull(this.entityTarget.getParam());
         if (entity instanceof Player) {
            GameProfile gameprofile = ((Player)entity).getGameProfile();
            pStack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameprofile));
         }
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget pEntityTarget) {
      return simpleBuilder((p_165211_) -> {
         return new FillPlayerHead(p_165211_, pEntityTarget);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<FillPlayerHead> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, FillPlayerHead pFillPlayerHead, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pFillPlayerHead, pSerializationContext);
         pJson.add("entity", pSerializationContext.serialize(pFillPlayerHead.entityTarget));
      }

      public FillPlayerHead deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         LootContext.EntityTarget lootcontext$entitytarget = GsonHelper.getAsObject(pObject, "entity", pDeserializationContext, LootContext.EntityTarget.class);
         return new FillPlayerHead(pConditions, lootcontext$entitytarget);
      }
   }
}