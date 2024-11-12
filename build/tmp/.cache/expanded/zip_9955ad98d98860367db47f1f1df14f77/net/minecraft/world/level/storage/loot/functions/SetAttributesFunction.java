package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * LootItemFunction that adds a list of attribute modifiers to the stacks.
 */
public class SetAttributesFunction extends LootItemConditionalFunction {
   final List<SetAttributesFunction.Modifier> modifiers;

   SetAttributesFunction(LootItemCondition[] pConditions, List<SetAttributesFunction.Modifier> pModifiers) {
      super(pConditions);
      this.modifiers = ImmutableList.copyOf(pModifiers);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_ATTRIBUTES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.modifiers.stream().flatMap((p_279080_) -> {
         return p_279080_.amount.getReferencedContextParams().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      RandomSource randomsource = pContext.getRandom();

      for(SetAttributesFunction.Modifier setattributesfunction$modifier : this.modifiers) {
         UUID uuid = setattributesfunction$modifier.id;
         if (uuid == null) {
            uuid = UUID.randomUUID();
         }

         EquipmentSlot equipmentslot = Util.getRandom(setattributesfunction$modifier.slots, randomsource);
         pStack.addAttributeModifier(setattributesfunction$modifier.attribute, new AttributeModifier(uuid, setattributesfunction$modifier.name, (double)setattributesfunction$modifier.amount.getFloat(pContext), setattributesfunction$modifier.operation), equipmentslot);
      }

      return pStack;
   }

   public static SetAttributesFunction.ModifierBuilder modifier(String pName, Attribute pAttribute, AttributeModifier.Operation pOperation, NumberProvider pValue) {
      return new SetAttributesFunction.ModifierBuilder(pName, pAttribute, pOperation, pValue);
   }

   public static SetAttributesFunction.Builder setAttributes() {
      return new SetAttributesFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
      private final List<SetAttributesFunction.Modifier> modifiers = Lists.newArrayList();

      protected SetAttributesFunction.Builder getThis() {
         return this;
      }

      public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder pModifierBuilder) {
         this.modifiers.add(pModifierBuilder.build());
         return this;
      }

      public LootItemFunction build() {
         return new SetAttributesFunction(this.getConditions(), this.modifiers);
      }
   }

   static class Modifier {
      final String name;
      final Attribute attribute;
      final AttributeModifier.Operation operation;
      final NumberProvider amount;
      @Nullable
      final UUID id;
      final EquipmentSlot[] slots;

      Modifier(String pName, Attribute pAttribute, AttributeModifier.Operation pOperation, NumberProvider pAmount, EquipmentSlot[] pSlots, @Nullable UUID pId) {
         this.name = pName;
         this.attribute = pAttribute;
         this.operation = pOperation;
         this.amount = pAmount;
         this.id = pId;
         this.slots = pSlots;
      }

      public JsonObject serialize(JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("name", this.name);
         jsonobject.addProperty("attribute", BuiltInRegistries.ATTRIBUTE.getKey(this.attribute).toString());
         jsonobject.addProperty("operation", operationToString(this.operation));
         jsonobject.add("amount", pContext.serialize(this.amount));
         if (this.id != null) {
            jsonobject.addProperty("id", this.id.toString());
         }

         if (this.slots.length == 1) {
            jsonobject.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray jsonarray = new JsonArray();

            for(EquipmentSlot equipmentslot : this.slots) {
               jsonarray.add(new JsonPrimitive(equipmentslot.getName()));
            }

            jsonobject.add("slot", jsonarray);
         }

         return jsonobject;
      }

      public static SetAttributesFunction.Modifier deserialize(JsonObject pJsonObj, JsonDeserializationContext pContext) {
         String s = GsonHelper.getAsString(pJsonObj, "name");
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJsonObj, "attribute"));
         Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(resourcelocation);
         if (attribute == null) {
            throw new JsonSyntaxException("Unknown attribute: " + resourcelocation);
         } else {
            AttributeModifier.Operation attributemodifier$operation = operationFromString(GsonHelper.getAsString(pJsonObj, "operation"));
            NumberProvider numberprovider = GsonHelper.getAsObject(pJsonObj, "amount", pContext, NumberProvider.class);
            UUID uuid = null;
            EquipmentSlot[] aequipmentslot;
            if (GsonHelper.isStringValue(pJsonObj, "slot")) {
               aequipmentslot = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(pJsonObj, "slot"))};
            } else {
               if (!GsonHelper.isArrayNode(pJsonObj, "slot")) {
                  throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
               }

               JsonArray jsonarray = GsonHelper.getAsJsonArray(pJsonObj, "slot");
               aequipmentslot = new EquipmentSlot[jsonarray.size()];
               int i = 0;

               for(JsonElement jsonelement : jsonarray) {
                  aequipmentslot[i++] = EquipmentSlot.byName(GsonHelper.convertToString(jsonelement, "slot"));
               }

               if (aequipmentslot.length == 0) {
                  throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
               }
            }

            if (pJsonObj.has("id")) {
               String s1 = GsonHelper.getAsString(pJsonObj, "id");

               try {
                  uuid = UUID.fromString(s1);
               } catch (IllegalArgumentException illegalargumentexception) {
                  throw new JsonSyntaxException("Invalid attribute modifier id '" + s1 + "' (must be UUID format, with dashes)");
               }
            }

            return new SetAttributesFunction.Modifier(s, attribute, attributemodifier$operation, numberprovider, aequipmentslot, uuid);
         }
      }

      private static String operationToString(AttributeModifier.Operation pOperation) {
         switch (pOperation) {
            case ADDITION:
               return "addition";
            case MULTIPLY_BASE:
               return "multiply_base";
            case MULTIPLY_TOTAL:
               return "multiply_total";
            default:
               throw new IllegalArgumentException("Unknown operation " + pOperation);
         }
      }

      private static AttributeModifier.Operation operationFromString(String pName) {
         switch (pName) {
            case "addition":
               return AttributeModifier.Operation.ADDITION;
            case "multiply_base":
               return AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total":
               return AttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
               throw new JsonSyntaxException("Unknown attribute modifier operation " + pName);
         }
      }
   }

   public static class ModifierBuilder {
      private final String name;
      private final Attribute attribute;
      private final AttributeModifier.Operation operation;
      private final NumberProvider amount;
      @Nullable
      private UUID id;
      private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

      public ModifierBuilder(String pName, Attribute pAttribute, AttributeModifier.Operation pOperation, NumberProvider pAmount) {
         this.name = pName;
         this.attribute = pAttribute;
         this.operation = pOperation;
         this.amount = pAmount;
      }

      public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlot pSlot) {
         this.slots.add(pSlot);
         return this;
      }

      public SetAttributesFunction.ModifierBuilder withUuid(UUID pId) {
         this.id = pId;
         return this;
      }

      public SetAttributesFunction.Modifier build() {
         return new SetAttributesFunction.Modifier(this.name, this.attribute, this.operation, this.amount, this.slots.toArray(new EquipmentSlot[0]), this.id);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetAttributesFunction pLootItemConditionalFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pLootItemConditionalFunction, pSerializationContext);
         JsonArray jsonarray = new JsonArray();

         for(SetAttributesFunction.Modifier setattributesfunction$modifier : pLootItemConditionalFunction.modifiers) {
            jsonarray.add(setattributesfunction$modifier.serialize(pSerializationContext));
         }

         pJson.add("modifiers", jsonarray);
      }

      public SetAttributesFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         JsonArray jsonarray = GsonHelper.getAsJsonArray(pObject, "modifiers");
         List<SetAttributesFunction.Modifier> list = Lists.newArrayListWithExpectedSize(jsonarray.size());

         for(JsonElement jsonelement : jsonarray) {
            list.add(SetAttributesFunction.Modifier.deserialize(GsonHelper.convertToJsonObject(jsonelement, "modifier"), pDeserializationContext));
         }

         if (list.isEmpty()) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributesFunction(pConditions, list);
         }
      }
   }
}