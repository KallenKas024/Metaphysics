package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class LocationPredicate {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LocationPredicate ANY = new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   private final MinMaxBounds.Doubles x;
   private final MinMaxBounds.Doubles y;
   private final MinMaxBounds.Doubles z;
   @Nullable
   private final ResourceKey<Biome> biome;
   @Nullable
   private final ResourceKey<Structure> structure;
   @Nullable
   private final ResourceKey<Level> dimension;
   @Nullable
   private final Boolean smokey;
   private final LightPredicate light;
   private final BlockPredicate block;
   private final FluidPredicate fluid;

   public LocationPredicate(MinMaxBounds.Doubles pX, MinMaxBounds.Doubles pY, MinMaxBounds.Doubles pZ, @Nullable ResourceKey<Biome> pBiome, @Nullable ResourceKey<Structure> pFeature, @Nullable ResourceKey<Level> pDimension, @Nullable Boolean pSmokey, LightPredicate pLight, BlockPredicate pBlock, FluidPredicate pFluid) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.biome = pBiome;
      this.structure = pFeature;
      this.dimension = pDimension;
      this.smokey = pSmokey;
      this.light = pLight;
      this.block = pBlock;
      this.fluid = pFluid;
   }

   public static LocationPredicate inBiome(ResourceKey<Biome> pBiome) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, pBiome, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate inDimension(ResourceKey<Level> pDimension) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, pDimension, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate inStructure(ResourceKey<Structure> pStructure) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, pStructure, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate atYLocation(MinMaxBounds.Doubles pYRange) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, pYRange, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public boolean matches(ServerLevel pLevel, double pX, double pY, double pZ) {
      if (!this.x.matches(pX)) {
         return false;
      } else if (!this.y.matches(pY)) {
         return false;
      } else if (!this.z.matches(pZ)) {
         return false;
      } else if (this.dimension != null && this.dimension != pLevel.dimension()) {
         return false;
      } else {
         BlockPos blockpos = BlockPos.containing(pX, pY, pZ);
         boolean flag = pLevel.isLoaded(blockpos);
         if (this.biome == null || flag && pLevel.getBiome(blockpos).is(this.biome)) {
            if (this.structure == null || flag && pLevel.structureManager().getStructureWithPieceAt(blockpos, this.structure).isValid()) {
               if (this.smokey == null || flag && this.smokey == CampfireBlock.isSmokeyPos(pLevel, blockpos)) {
                  if (!this.light.matches(pLevel, blockpos)) {
                     return false;
                  } else if (!this.block.matches(pLevel, blockpos)) {
                     return false;
                  } else {
                     return this.fluid.matches(pLevel, blockpos);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.add("x", this.x.serializeToJson());
            jsonobject1.add("y", this.y.serializeToJson());
            jsonobject1.add("z", this.z.serializeToJson());
            jsonobject.add("position", jsonobject1);
         }

         if (this.dimension != null) {
            Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(LOGGER::error).ifPresent((p_52633_) -> {
               jsonobject.add("dimension", p_52633_);
            });
         }

         if (this.structure != null) {
            jsonobject.addProperty("structure", this.structure.location().toString());
         }

         if (this.biome != null) {
            jsonobject.addProperty("biome", this.biome.location().toString());
         }

         if (this.smokey != null) {
            jsonobject.addProperty("smokey", this.smokey);
         }

         jsonobject.add("light", this.light.serializeToJson());
         jsonobject.add("block", this.block.serializeToJson());
         jsonobject.add("fluid", this.fluid.serializeToJson());
         return jsonobject;
      }
   }

   public static LocationPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "location");
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "position", new JsonObject());
         MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(jsonobject1.get("x"));
         MinMaxBounds.Doubles minmaxbounds$doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("y"));
         MinMaxBounds.Doubles minmaxbounds$doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("z"));
         ResourceKey<Level> resourcekey = jsonobject.has("dimension") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("dimension")).resultOrPartial(LOGGER::error).map((p_52637_) -> {
            return ResourceKey.create(Registries.DIMENSION, p_52637_);
         }).orElse((ResourceKey<Level>)null) : null;
         ResourceKey<Structure> resourcekey1 = jsonobject.has("structure") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("structure")).resultOrPartial(LOGGER::error).map((p_207927_) -> {
            return ResourceKey.create(Registries.STRUCTURE, p_207927_);
         }).orElse((ResourceKey<Structure>)null) : null;
         ResourceKey<Biome> resourcekey2 = null;
         if (jsonobject.has("biome")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "biome"));
            resourcekey2 = ResourceKey.create(Registries.BIOME, resourcelocation);
         }

         Boolean obool = jsonobject.has("smokey") ? jsonobject.get("smokey").getAsBoolean() : null;
         LightPredicate lightpredicate = LightPredicate.fromJson(jsonobject.get("light"));
         BlockPredicate blockpredicate = BlockPredicate.fromJson(jsonobject.get("block"));
         FluidPredicate fluidpredicate = FluidPredicate.fromJson(jsonobject.get("fluid"));
         return new LocationPredicate(minmaxbounds$doubles, minmaxbounds$doubles1, minmaxbounds$doubles2, resourcekey2, resourcekey1, resourcekey, obool, lightpredicate, blockpredicate, fluidpredicate);
      } else {
         return ANY;
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
      @Nullable
      private ResourceKey<Biome> biome;
      @Nullable
      private ResourceKey<Structure> structure;
      @Nullable
      private ResourceKey<Level> dimension;
      @Nullable
      private Boolean smokey;
      private LightPredicate light = LightPredicate.ANY;
      private BlockPredicate block = BlockPredicate.ANY;
      private FluidPredicate fluid = FluidPredicate.ANY;

      public static LocationPredicate.Builder location() {
         return new LocationPredicate.Builder();
      }

      public LocationPredicate.Builder setX(MinMaxBounds.Doubles pX) {
         this.x = pX;
         return this;
      }

      public LocationPredicate.Builder setY(MinMaxBounds.Doubles pY) {
         this.y = pY;
         return this;
      }

      public LocationPredicate.Builder setZ(MinMaxBounds.Doubles pZ) {
         this.z = pZ;
         return this;
      }

      public LocationPredicate.Builder setBiome(@Nullable ResourceKey<Biome> pBiome) {
         this.biome = pBiome;
         return this;
      }

      public LocationPredicate.Builder setStructure(@Nullable ResourceKey<Structure> pStructure) {
         this.structure = pStructure;
         return this;
      }

      public LocationPredicate.Builder setDimension(@Nullable ResourceKey<Level> pDimension) {
         this.dimension = pDimension;
         return this;
      }

      public LocationPredicate.Builder setLight(LightPredicate pLight) {
         this.light = pLight;
         return this;
      }

      public LocationPredicate.Builder setBlock(BlockPredicate pBlock) {
         this.block = pBlock;
         return this;
      }

      public LocationPredicate.Builder setFluid(FluidPredicate pFluid) {
         this.fluid = pFluid;
         return this;
      }

      public LocationPredicate.Builder setSmokey(Boolean pSmokey) {
         this.smokey = pSmokey;
         return this;
      }

      public LocationPredicate build() {
         return new LocationPredicate(this.x, this.y, this.z, this.biome, this.structure, this.dimension, this.smokey, this.light, this.block, this.fluid);
      }
   }
}