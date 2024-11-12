package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

/**
 * LootItemFunction that takes the NBT from  an {@link NbtProvider} and applies a set of copy operations that copy from
 * that source NBT into the stack's NBT.
 */
public class CopyNbtFunction extends LootItemConditionalFunction {
   final NbtProvider source;
   final List<CopyNbtFunction.CopyOperation> operations;

   CopyNbtFunction(LootItemCondition[] pConditions, NbtProvider pNbtSource, List<CopyNbtFunction.CopyOperation> pNbtOperations) {
      super(pConditions);
      this.source = pNbtSource;
      this.operations = ImmutableList.copyOf(pNbtOperations);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.COPY_NBT;
   }

   static NbtPathArgument.NbtPath compileNbtPath(String pNbtPath) {
      try {
         return (new NbtPathArgument()).parse(new StringReader(pNbtPath));
      } catch (CommandSyntaxException commandsyntaxexception) {
         throw new IllegalArgumentException("Failed to parse path " + pNbtPath, commandsyntaxexception);
      }
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.source.getReferencedContextParams();
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Tag tag = this.source.get(pContext);
      if (tag != null) {
         this.operations.forEach((p_80255_) -> {
            p_80255_.apply(pStack::getOrCreateTag, tag);
         });
      }

      return pStack;
   }

   /**
    * Create a builder that copies data from the given NbtProvider.
    */
   public static CopyNbtFunction.Builder copyData(NbtProvider pNbtSource) {
      return new CopyNbtFunction.Builder(pNbtSource);
   }

   /**
    * Create a builder that copies the NBT data of the given EntityTarget.
    */
   public static CopyNbtFunction.Builder copyData(LootContext.EntityTarget pEntitySource) {
      return new CopyNbtFunction.Builder(ContextNbtProvider.forContextEntity(pEntitySource));
   }

   public static class Builder extends LootItemConditionalFunction.Builder<CopyNbtFunction.Builder> {
      private final NbtProvider source;
      private final List<CopyNbtFunction.CopyOperation> ops = Lists.newArrayList();

      Builder(NbtProvider pNbtSource) {
         this.source = pNbtSource;
      }

      public CopyNbtFunction.Builder copy(String pSourcePath, String pTargetPath, CopyNbtFunction.MergeStrategy pCopyAction) {
         this.ops.add(new CopyNbtFunction.CopyOperation(pSourcePath, pTargetPath, pCopyAction));
         return this;
      }

      public CopyNbtFunction.Builder copy(String pSourcePath, String pTargetPath) {
         return this.copy(pSourcePath, pTargetPath, CopyNbtFunction.MergeStrategy.REPLACE);
      }

      protected CopyNbtFunction.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new CopyNbtFunction(this.getConditions(), this.source, this.ops);
      }
   }

   static class CopyOperation {
      private final String sourcePathText;
      private final NbtPathArgument.NbtPath sourcePath;
      private final String targetPathText;
      private final NbtPathArgument.NbtPath targetPath;
      private final CopyNbtFunction.MergeStrategy op;

      CopyOperation(String pSourcePathText, String pTargetPathText, CopyNbtFunction.MergeStrategy pMergeStrategy) {
         this.sourcePathText = pSourcePathText;
         this.sourcePath = CopyNbtFunction.compileNbtPath(pSourcePathText);
         this.targetPathText = pTargetPathText;
         this.targetPath = CopyNbtFunction.compileNbtPath(pTargetPathText);
         this.op = pMergeStrategy;
      }

      public void apply(Supplier<Tag> pTargetTag, Tag pSourceTag) {
         try {
            List<Tag> list = this.sourcePath.get(pSourceTag);
            if (!list.isEmpty()) {
               this.op.merge(pTargetTag.get(), this.targetPath, list);
            }
         } catch (CommandSyntaxException commandsyntaxexception) {
         }

      }

      public JsonObject toJson() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("source", this.sourcePathText);
         jsonobject.addProperty("target", this.targetPathText);
         jsonobject.addProperty("op", this.op.name);
         return jsonobject;
      }

      public static CopyNbtFunction.CopyOperation fromJson(JsonObject pJson) {
         String s = GsonHelper.getAsString(pJson, "source");
         String s1 = GsonHelper.getAsString(pJson, "target");
         CopyNbtFunction.MergeStrategy copynbtfunction$mergestrategy = CopyNbtFunction.MergeStrategy.getByName(GsonHelper.getAsString(pJson, "op"));
         return new CopyNbtFunction.CopyOperation(s, s1, copynbtfunction$mergestrategy);
      }
   }

   public static enum MergeStrategy {
      REPLACE("replace") {
         public void merge(Tag p_80362_, NbtPathArgument.NbtPath p_80363_, List<Tag> p_80364_) throws CommandSyntaxException {
            p_80363_.set(p_80362_, Iterables.getLast(p_80364_));
         }
      },
      APPEND("append") {
         public void merge(Tag p_80373_, NbtPathArgument.NbtPath p_80374_, List<Tag> p_80375_) throws CommandSyntaxException {
            List<Tag> list = p_80374_.getOrCreate(p_80373_, ListTag::new);
            list.forEach((p_80371_) -> {
               if (p_80371_ instanceof ListTag) {
                  p_80375_.forEach((p_165187_) -> {
                     ((ListTag)p_80371_).add(p_165187_.copy());
                  });
               }

            });
         }
      },
      MERGE("merge") {
         public void merge(Tag p_80387_, NbtPathArgument.NbtPath p_80388_, List<Tag> p_80389_) throws CommandSyntaxException {
            List<Tag> list = p_80388_.getOrCreate(p_80387_, CompoundTag::new);
            list.forEach((p_80385_) -> {
               if (p_80385_ instanceof CompoundTag) {
                  p_80389_.forEach((p_165190_) -> {
                     if (p_165190_ instanceof CompoundTag) {
                        ((CompoundTag)p_80385_).merge((CompoundTag)p_165190_);
                     }

                  });
               }

            });
         }
      };

      final String name;

      public abstract void merge(Tag pTargetNbt, NbtPathArgument.NbtPath pNbtPath, List<Tag> pSourceNbt) throws CommandSyntaxException;

      MergeStrategy(String pName) {
         this.name = pName;
      }

      public static CopyNbtFunction.MergeStrategy getByName(String pName) {
         for(CopyNbtFunction.MergeStrategy copynbtfunction$mergestrategy : values()) {
            if (copynbtfunction$mergestrategy.name.equals(pName)) {
               return copynbtfunction$mergestrategy;
            }
         }

         throw new IllegalArgumentException("Invalid merge strategy" + pName);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNbtFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CopyNbtFunction pCopyNbtFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pCopyNbtFunction, pSerializationContext);
         pJson.add("source", pSerializationContext.serialize(pCopyNbtFunction.source));
         JsonArray jsonarray = new JsonArray();
         pCopyNbtFunction.operations.stream().map(CopyNbtFunction.CopyOperation::toJson).forEach(jsonarray::add);
         pJson.add("ops", jsonarray);
      }

      public CopyNbtFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         NbtProvider nbtprovider = GsonHelper.getAsObject(pObject, "source", pDeserializationContext, NbtProvider.class);
         List<CopyNbtFunction.CopyOperation> list = Lists.newArrayList();

         for(JsonElement jsonelement : GsonHelper.getAsJsonArray(pObject, "ops")) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "op");
            list.add(CopyNbtFunction.CopyOperation.fromJson(jsonobject));
         }

         return new CopyNbtFunction(pConditions, nbtprovider, list);
      }
   }
}