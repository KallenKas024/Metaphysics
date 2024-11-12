package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

/**
 * LootItemFunction that sets a stack's name.
 * The Component for the name is optionally resolved relative to a given {@link LootContext.EntityTarget} for entity-
 * sensitive component data such as scoreboard scores.
 */
public class SetNameFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Component name;
   @Nullable
   final LootContext.EntityTarget resolutionContext;

   SetNameFunction(LootItemCondition[] pConditions, @Nullable Component pName, @Nullable LootContext.EntityTarget pResolutionContext) {
      super(pConditions);
      this.name = pName;
      this.resolutionContext = pResolutionContext;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_NAME;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
   }

   /**
    * Create a UnaryOperator that resolves Components based on the given LootContext and EntityTarget.
    * This will replace for example score components.
    */
   public static UnaryOperator<Component> createResolver(LootContext pLootContext, @Nullable LootContext.EntityTarget pResolutionContext) {
      if (pResolutionContext != null) {
         Entity entity = pLootContext.getParamOrNull(pResolutionContext.getParam());
         if (entity != null) {
            CommandSourceStack commandsourcestack = entity.createCommandSourceStack().withPermission(2);
            return (p_81147_) -> {
               try {
                  return ComponentUtils.updateForEntity(commandsourcestack, p_81147_, entity, 0);
               } catch (CommandSyntaxException commandsyntaxexception) {
                  LOGGER.warn("Failed to resolve text component", (Throwable)commandsyntaxexception);
                  return p_81147_;
               }
            };
         }
      }

      return (p_81152_) -> {
         return p_81152_;
      };
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (this.name != null) {
         pStack.setHoverName(createResolver(pContext, this.resolutionContext).apply(this.name));
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> setName(Component pName) {
      return simpleBuilder((p_165468_) -> {
         return new SetNameFunction(p_165468_, pName, (LootContext.EntityTarget)null);
      });
   }

   public static LootItemConditionalFunction.Builder<?> setName(Component pName, LootContext.EntityTarget pResolutionContext) {
      return simpleBuilder((p_165465_) -> {
         return new SetNameFunction(p_165465_, pName, pResolutionContext);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetNameFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetNameFunction pSetNameFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pSetNameFunction, pSerializationContext);
         if (pSetNameFunction.name != null) {
            pJson.add("name", Component.Serializer.toJsonTree(pSetNameFunction.name));
         }

         if (pSetNameFunction.resolutionContext != null) {
            pJson.add("entity", pSerializationContext.serialize(pSetNameFunction.resolutionContext));
         }

      }

      public SetNameFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         Component component = Component.Serializer.fromJson(pObject.get("name"));
         LootContext.EntityTarget lootcontext$entitytarget = GsonHelper.getAsObject(pObject, "entity", (LootContext.EntityTarget)null, pDeserializationContext, LootContext.EntityTarget.class);
         return new SetNameFunction(pConditions, component, lootcontext$entitytarget);
      }
   }
}