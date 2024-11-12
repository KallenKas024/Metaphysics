package net.minecraft.data;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class BlockFamily {
   private final Block baseBlock;
   final Map<BlockFamily.Variant, Block> variants = Maps.newHashMap();
   FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
   boolean generateModel = true;
   boolean generateRecipe = true;
   @Nullable
   String recipeGroupPrefix;
   @Nullable
   String recipeUnlockedBy;

   BlockFamily(Block pBaseBlock) {
      this.baseBlock = pBaseBlock;
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public Map<BlockFamily.Variant, Block> getVariants() {
      return this.variants;
   }

   public Block get(BlockFamily.Variant pVariant) {
      return this.variants.get(pVariant);
   }

   public boolean shouldGenerateModel() {
      return this.generateModel;
   }

   public boolean shouldGenerateRecipe(FeatureFlagSet pEnabledFeatures) {
      return this.generateRecipe && this.requiredFeatures.isSubsetOf(pEnabledFeatures);
   }

   public Optional<String> getRecipeGroupPrefix() {
      return Util.isBlank(this.recipeGroupPrefix) ? Optional.empty() : Optional.of(this.recipeGroupPrefix);
   }

   public Optional<String> getRecipeUnlockedBy() {
      return Util.isBlank(this.recipeUnlockedBy) ? Optional.empty() : Optional.of(this.recipeUnlockedBy);
   }

   public static class Builder {
      private final BlockFamily family;

      public Builder(Block pBaseBlock) {
         this.family = new BlockFamily(pBaseBlock);
      }

      public BlockFamily getFamily() {
         return this.family;
      }

      public BlockFamily.Builder button(Block pButtonBlock) {
         this.family.variants.put(BlockFamily.Variant.BUTTON, pButtonBlock);
         return this;
      }

      public BlockFamily.Builder chiseled(Block pChiseledBlock) {
         this.family.variants.put(BlockFamily.Variant.CHISELED, pChiseledBlock);
         return this;
      }

      public BlockFamily.Builder mosaic(Block pMosaicBlock) {
         this.family.variants.put(BlockFamily.Variant.MOSAIC, pMosaicBlock);
         return this;
      }

      public BlockFamily.Builder cracked(Block pCrackedBlock) {
         this.family.variants.put(BlockFamily.Variant.CRACKED, pCrackedBlock);
         return this;
      }

      public BlockFamily.Builder cut(Block pCutBlock) {
         this.family.variants.put(BlockFamily.Variant.CUT, pCutBlock);
         return this;
      }

      public BlockFamily.Builder door(Block pDoorBlock) {
         this.family.variants.put(BlockFamily.Variant.DOOR, pDoorBlock);
         return this;
      }

      public BlockFamily.Builder customFence(Block pCustomFenceBlock) {
         this.family.variants.put(BlockFamily.Variant.CUSTOM_FENCE, pCustomFenceBlock);
         return this;
      }

      public BlockFamily.Builder fence(Block pFenceBlock) {
         this.family.variants.put(BlockFamily.Variant.FENCE, pFenceBlock);
         return this;
      }

      public BlockFamily.Builder customFenceGate(Block pCustomFenceGateBlock) {
         this.family.variants.put(BlockFamily.Variant.CUSTOM_FENCE_GATE, pCustomFenceGateBlock);
         return this;
      }

      public BlockFamily.Builder fenceGate(Block pFenceGateBlock) {
         this.family.variants.put(BlockFamily.Variant.FENCE_GATE, pFenceGateBlock);
         return this;
      }

      public BlockFamily.Builder sign(Block pSignBlock, Block pWallSignBlock) {
         this.family.variants.put(BlockFamily.Variant.SIGN, pSignBlock);
         this.family.variants.put(BlockFamily.Variant.WALL_SIGN, pWallSignBlock);
         return this;
      }

      public BlockFamily.Builder slab(Block pSlabBlock) {
         this.family.variants.put(BlockFamily.Variant.SLAB, pSlabBlock);
         return this;
      }

      public BlockFamily.Builder stairs(Block pStairsBlock) {
         this.family.variants.put(BlockFamily.Variant.STAIRS, pStairsBlock);
         return this;
      }

      public BlockFamily.Builder pressurePlate(Block pPressurePlateBlock) {
         this.family.variants.put(BlockFamily.Variant.PRESSURE_PLATE, pPressurePlateBlock);
         return this;
      }

      public BlockFamily.Builder polished(Block pPolishedBlock) {
         this.family.variants.put(BlockFamily.Variant.POLISHED, pPolishedBlock);
         return this;
      }

      public BlockFamily.Builder trapdoor(Block pTrapdoorBlock) {
         this.family.variants.put(BlockFamily.Variant.TRAPDOOR, pTrapdoorBlock);
         return this;
      }

      public BlockFamily.Builder wall(Block pWallBlock) {
         this.family.variants.put(BlockFamily.Variant.WALL, pWallBlock);
         return this;
      }

      public BlockFamily.Builder dontGenerateModel() {
         this.family.generateModel = false;
         return this;
      }

      public BlockFamily.Builder dontGenerateRecipe() {
         this.family.generateRecipe = false;
         return this;
      }

      public BlockFamily.Builder featureLockedBehind(FeatureFlag... pFlags) {
         this.family.requiredFeatures = FeatureFlags.REGISTRY.subset(pFlags);
         return this;
      }

      public BlockFamily.Builder recipeGroupPrefix(String pRecipeGroupPrefix) {
         this.family.recipeGroupPrefix = pRecipeGroupPrefix;
         return this;
      }

      public BlockFamily.Builder recipeUnlockedBy(String pRecipeUnlockedBy) {
         this.family.recipeUnlockedBy = pRecipeUnlockedBy;
         return this;
      }
   }

   public static enum Variant {
      BUTTON("button"),
      CHISELED("chiseled"),
      CRACKED("cracked"),
      CUT("cut"),
      DOOR("door"),
      CUSTOM_FENCE("custom_fence"),
      FENCE("fence"),
      CUSTOM_FENCE_GATE("custom_fence_gate"),
      FENCE_GATE("fence_gate"),
      MOSAIC("mosaic"),
      SIGN("sign"),
      SLAB("slab"),
      STAIRS("stairs"),
      PRESSURE_PLATE("pressure_plate"),
      POLISHED("polished"),
      TRAPDOOR("trapdoor"),
      WALL("wall"),
      WALL_SIGN("wall_sign");

      private final String name;

      private Variant(String pVariantName) {
         this.name = pVariantName;
      }

      public String getName() {
         return this.name;
      }
   }
}