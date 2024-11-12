package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class MultiPartGenerator implements BlockStateGenerator {
   private final Block block;
   private final List<MultiPartGenerator.Entry> parts = Lists.newArrayList();

   private MultiPartGenerator(Block pBlock) {
      this.block = pBlock;
   }

   public Block getBlock() {
      return this.block;
   }

   public static MultiPartGenerator multiPart(Block pBlock) {
      return new MultiPartGenerator(pBlock);
   }

   public MultiPartGenerator with(List<Variant> pVariants) {
      this.parts.add(new MultiPartGenerator.Entry(pVariants));
      return this;
   }

   public MultiPartGenerator with(Variant pVariant) {
      return this.with(ImmutableList.of(pVariant));
   }

   public MultiPartGenerator with(Condition pCondition, List<Variant> pVariants) {
      this.parts.add(new MultiPartGenerator.ConditionalEntry(pCondition, pVariants));
      return this;
   }

   public MultiPartGenerator with(Condition pCondition, Variant... pVariants) {
      return this.with(pCondition, ImmutableList.copyOf(pVariants));
   }

   public MultiPartGenerator with(Condition pCondition, Variant pVariant) {
      return this.with(pCondition, ImmutableList.of(pVariant));
   }

   public JsonElement get() {
      StateDefinition<Block, BlockState> statedefinition = this.block.getStateDefinition();
      this.parts.forEach((p_125208_) -> {
         p_125208_.validate(statedefinition);
      });
      JsonArray jsonarray = new JsonArray();
      this.parts.stream().map(MultiPartGenerator.Entry::get).forEach(jsonarray::add);
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("multipart", jsonarray);
      return jsonobject;
   }

   static class ConditionalEntry extends MultiPartGenerator.Entry {
      private final Condition condition;

      ConditionalEntry(Condition pCondition, List<Variant> pVariants) {
         super(pVariants);
         this.condition = pCondition;
      }

      public void validate(StateDefinition<?, ?> pStateDefinition) {
         this.condition.validate(pStateDefinition);
      }

      public void decorate(JsonObject pJsonObject) {
         pJsonObject.add("when", this.condition.get());
      }
   }

   static class Entry implements Supplier<JsonElement> {
      private final List<Variant> variants;

      Entry(List<Variant> pVariants) {
         this.variants = pVariants;
      }

      public void validate(StateDefinition<?, ?> pStateDefinition) {
      }

      public void decorate(JsonObject pJsonObject) {
      }

      public JsonElement get() {
         JsonObject jsonobject = new JsonObject();
         this.decorate(jsonobject);
         jsonobject.add("apply", Variant.convertList(this.variants));
         return jsonobject;
      }
   }
}