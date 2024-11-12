package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that sets the contents of a container such as a chest by setting the {@code BlocKEntityTag} of the
 * stacks.
 * The contents are based on a list of loot pools.
 */
public class SetContainerContents extends LootItemConditionalFunction {
   final List<LootPoolEntryContainer> entries;
   final BlockEntityType<?> type;

   SetContainerContents(LootItemCondition[] pConditions, BlockEntityType<?> pType, List<LootPoolEntryContainer> pEntries) {
      super(pConditions);
      this.type = pType;
      this.entries = ImmutableList.copyOf(pEntries);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_CONTENTS;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isEmpty()) {
         return pStack;
      } else {
         NonNullList<ItemStack> nonnulllist = NonNullList.create();
         this.entries.forEach((p_80916_) -> {
            p_80916_.expand(pContext, (p_287573_) -> {
               p_287573_.createItemStack(LootTable.createStackSplitter(pContext.getLevel(), nonnulllist::add), pContext);
            });
         });
         CompoundTag compoundtag = new CompoundTag();
         ContainerHelper.saveAllItems(compoundtag, nonnulllist);
         CompoundTag compoundtag1 = BlockItem.getBlockEntityData(pStack);
         if (compoundtag1 == null) {
            compoundtag1 = compoundtag;
         } else {
            compoundtag1.merge(compoundtag);
         }

         BlockItem.setBlockEntityData(pStack, this.type, compoundtag1);
         return pStack;
      }
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      super.validate(pContext);

      for(int i = 0; i < this.entries.size(); ++i) {
         this.entries.get(i).validate(pContext.forChild(".entry[" + i + "]"));
      }

   }

   public static SetContainerContents.Builder setContents(BlockEntityType<?> pType) {
      return new SetContainerContents.Builder(pType);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
      private final BlockEntityType<?> type;

      public Builder(BlockEntityType<?> pType) {
         this.type = pType;
      }

      protected SetContainerContents.Builder getThis() {
         return this;
      }

      public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> pLootEntryBuilder) {
         this.entries.add(pLootEntryBuilder.build());
         return this;
      }

      public LootItemFunction build() {
         return new SetContainerContents(this.getConditions(), this.type, this.entries);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerContents> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetContainerContents pSetContainerContents, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pSetContainerContents, pSerializationContext);
         pJson.addProperty("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(pSetContainerContents.type).toString());
         pJson.add("entries", pSerializationContext.serialize(pSetContainerContents.entries));
      }

      public SetContainerContents deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(pObject, "entries", pDeserializationContext, LootPoolEntryContainer[].class);
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pObject, "type"));
         BlockEntityType<?> blockentitytype = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block entity type id '" + resourcelocation + "'");
         });
         return new SetContainerContents(pConditions, blockentitytype, Arrays.asList(alootpoolentrycontainer));
      }
   }
}