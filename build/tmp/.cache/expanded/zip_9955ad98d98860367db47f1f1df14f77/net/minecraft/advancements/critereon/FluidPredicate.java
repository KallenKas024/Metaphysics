package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidPredicate {
   public static final FluidPredicate ANY = new FluidPredicate((TagKey<Fluid>)null, (Fluid)null, StatePropertiesPredicate.ANY);
   @Nullable
   private final TagKey<Fluid> tag;
   @Nullable
   private final Fluid fluid;
   private final StatePropertiesPredicate properties;

   public FluidPredicate(@Nullable TagKey<Fluid> pTag, @Nullable Fluid pFluid, StatePropertiesPredicate pProperties) {
      this.tag = pTag;
      this.fluid = pFluid;
      this.properties = pProperties;
   }

   public boolean matches(ServerLevel pLevel, BlockPos pPos) {
      if (this == ANY) {
         return true;
      } else if (!pLevel.isLoaded(pPos)) {
         return false;
      } else {
         FluidState fluidstate = pLevel.getFluidState(pPos);
         if (this.tag != null && !fluidstate.is(this.tag)) {
            return false;
         } else if (this.fluid != null && !fluidstate.is(this.fluid)) {
            return false;
         } else {
            return this.properties.matches(fluidstate);
         }
      }
   }

   public static FluidPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "fluid");
         Fluid fluid = null;
         if (jsonobject.has("fluid")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "fluid"));
            fluid = BuiltInRegistries.FLUID.get(resourcelocation);
         }

         TagKey<Fluid> tagkey = null;
         if (jsonobject.has("tag")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "tag"));
            tagkey = TagKey.create(Registries.FLUID, resourcelocation1);
         }

         StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(jsonobject.get("state"));
         return new FluidPredicate(tagkey, fluid, statepropertiespredicate);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.fluid != null) {
            jsonobject.addProperty("fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());
         }

         if (this.tag != null) {
            jsonobject.addProperty("tag", this.tag.location().toString());
         }

         jsonobject.add("state", this.properties.serializeToJson());
         return jsonobject;
      }
   }

   public static class Builder {
      @Nullable
      private Fluid fluid;
      @Nullable
      private TagKey<Fluid> fluids;
      private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

      private Builder() {
      }

      public static FluidPredicate.Builder fluid() {
         return new FluidPredicate.Builder();
      }

      public FluidPredicate.Builder of(Fluid pFluid) {
         this.fluid = pFluid;
         return this;
      }

      public FluidPredicate.Builder of(TagKey<Fluid> pFluids) {
         this.fluids = pFluids;
         return this;
      }

      public FluidPredicate.Builder setProperties(StatePropertiesPredicate pProperties) {
         this.properties = pProperties;
         return this;
      }

      public FluidPredicate build() {
         return new FluidPredicate(this.fluids, this.fluid, this.properties);
      }
   }
}