package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicate {
   public static final BlockPredicate ANY = new BlockPredicate((TagKey<Block>)null, (Set<Block>)null, StatePropertiesPredicate.ANY, NbtPredicate.ANY);
   @Nullable
   private final TagKey<Block> tag;
   @Nullable
   private final Set<Block> blocks;
   private final StatePropertiesPredicate properties;
   private final NbtPredicate nbt;

   public BlockPredicate(@Nullable TagKey<Block> pTag, @Nullable Set<Block> pBlocks, StatePropertiesPredicate pProperties, NbtPredicate pNbt) {
      this.tag = pTag;
      this.blocks = pBlocks;
      this.properties = pProperties;
      this.nbt = pNbt;
   }

   public boolean matches(ServerLevel pLevel, BlockPos pPos) {
      if (this == ANY) {
         return true;
      } else if (!pLevel.isLoaded(pPos)) {
         return false;
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (this.tag != null && !blockstate.is(this.tag)) {
            return false;
         } else if (this.blocks != null && !this.blocks.contains(blockstate.getBlock())) {
            return false;
         } else if (!this.properties.matches(blockstate)) {
            return false;
         } else {
            if (this.nbt != NbtPredicate.ANY) {
               BlockEntity blockentity = pLevel.getBlockEntity(pPos);
               if (blockentity == null || !this.nbt.matches(blockentity.saveWithFullMetadata())) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static BlockPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "block");
         NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
         Set<Block> set = null;
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "blocks", (JsonArray)null);
         if (jsonarray != null) {
            ImmutableSet.Builder<Block> builder = ImmutableSet.builder();

            for(JsonElement jsonelement : jsonarray) {
               ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.convertToString(jsonelement, "block"));
               builder.add(BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown block id '" + resourcelocation + "'");
               }));
            }

            set = builder.build();
         }

         TagKey<Block> tagkey = null;
         if (jsonobject.has("tag")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "tag"));
            tagkey = TagKey.create(Registries.BLOCK, resourcelocation1);
         }

         StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(jsonobject.get("state"));
         return new BlockPredicate(tagkey, set, statepropertiespredicate, nbtpredicate);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.blocks != null) {
            JsonArray jsonarray = new JsonArray();

            for(Block block : this.blocks) {
               jsonarray.add(BuiltInRegistries.BLOCK.getKey(block).toString());
            }

            jsonobject.add("blocks", jsonarray);
         }

         if (this.tag != null) {
            jsonobject.addProperty("tag", this.tag.location().toString());
         }

         jsonobject.add("nbt", this.nbt.serializeToJson());
         jsonobject.add("state", this.properties.serializeToJson());
         return jsonobject;
      }
   }

   public static class Builder {
      @Nullable
      private Set<Block> blocks;
      @Nullable
      private TagKey<Block> tag;
      private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;
      private NbtPredicate nbt = NbtPredicate.ANY;

      private Builder() {
      }

      public static BlockPredicate.Builder block() {
         return new BlockPredicate.Builder();
      }

      public BlockPredicate.Builder of(Block... pBlocks) {
         this.blocks = ImmutableSet.copyOf(pBlocks);
         return this;
      }

      public BlockPredicate.Builder of(Iterable<Block> pBlocks) {
         this.blocks = ImmutableSet.copyOf(pBlocks);
         return this;
      }

      public BlockPredicate.Builder of(TagKey<Block> pTag) {
         this.tag = pTag;
         return this;
      }

      public BlockPredicate.Builder hasNbt(CompoundTag pNbt) {
         this.nbt = new NbtPredicate(pNbt);
         return this;
      }

      public BlockPredicate.Builder setProperties(StatePropertiesPredicate pProperties) {
         this.properties = pProperties;
         return this;
      }

      public BlockPredicate build() {
         return new BlockPredicate(this.tag, this.blocks, this.properties, this.nbt);
      }
   }
}