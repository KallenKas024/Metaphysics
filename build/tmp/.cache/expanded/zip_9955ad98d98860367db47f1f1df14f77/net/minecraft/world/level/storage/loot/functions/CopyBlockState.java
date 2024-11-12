package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that copies a set of block state properties to the {@code "BlockStateTag"} NBT tag of the ItemStack.
 * This tag is checked when the block is placed.
 */
public class CopyBlockState extends LootItemConditionalFunction {
   final Block block;
   final Set<Property<?>> properties;

   CopyBlockState(LootItemCondition[] pConditions, Block pBlock, Set<Property<?>> pProperties) {
      super(pConditions);
      this.block = pBlock;
      this.properties = pProperties;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.COPY_STATE;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.BLOCK_STATE);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   protected ItemStack run(ItemStack pStack, LootContext pContext) {
      BlockState blockstate = pContext.getParamOrNull(LootContextParams.BLOCK_STATE);
      if (blockstate != null) {
         CompoundTag compoundtag = pStack.getOrCreateTag();
         CompoundTag compoundtag1;
         if (compoundtag.contains("BlockStateTag", 10)) {
            compoundtag1 = compoundtag.getCompound("BlockStateTag");
         } else {
            compoundtag1 = new CompoundTag();
            compoundtag.put("BlockStateTag", compoundtag1);
         }

         this.properties.stream().filter(blockstate::hasProperty).forEach((p_80072_) -> {
            compoundtag1.putString(p_80072_.getName(), serialize(blockstate, p_80072_));
         });
      }

      return pStack;
   }

   public static CopyBlockState.Builder copyState(Block pBlock) {
      return new CopyBlockState.Builder(pBlock);
   }

   private static <T extends Comparable<T>> String serialize(BlockState pBlockState, Property<T> pProperty) {
      T t = pBlockState.getValue(pProperty);
      return pProperty.getName(t);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<CopyBlockState.Builder> {
      private final Block block;
      private final Set<Property<?>> properties = Sets.newHashSet();

      Builder(Block pBlock) {
         this.block = pBlock;
      }

      public CopyBlockState.Builder copy(Property<?> pProperty) {
         if (!this.block.getStateDefinition().getProperties().contains(pProperty)) {
            throw new IllegalStateException("Property " + pProperty + " is not present on block " + this.block);
         } else {
            this.properties.add(pProperty);
            return this;
         }
      }

      protected CopyBlockState.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new CopyBlockState(this.getConditions(), this.block, this.properties);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<CopyBlockState> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CopyBlockState pLootItemConditionalFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pLootItemConditionalFunction, pSerializationContext);
         pJson.addProperty("block", BuiltInRegistries.BLOCK.getKey(pLootItemConditionalFunction.block).toString());
         JsonArray jsonarray = new JsonArray();
         pLootItemConditionalFunction.properties.forEach((p_80091_) -> {
            jsonarray.add(p_80091_.getName());
         });
         pJson.add("properties", jsonarray);
      }

      public CopyBlockState deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pObject, "block"));
         Block block = BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + resourcelocation);
         });
         StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
         Set<Property<?>> set = Sets.newHashSet();
         JsonArray jsonarray = GsonHelper.getAsJsonArray(pObject, "properties", (JsonArray)null);
         if (jsonarray != null) {
            jsonarray.forEach((p_80111_) -> {
               set.add(statedefinition.getProperty(GsonHelper.convertToString(p_80111_, "property")));
            });
         }

         return new CopyBlockState(pConditions, block, set);
      }
   }
}