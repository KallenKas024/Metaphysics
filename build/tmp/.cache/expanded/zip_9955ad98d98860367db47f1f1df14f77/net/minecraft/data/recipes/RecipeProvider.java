package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider implements DataProvider {
   protected final PackOutput.PathProvider recipePathProvider;
   protected final PackOutput.PathProvider advancementPathProvider;
   private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>>builder().put(BlockFamily.Variant.BUTTON, (p_176733_, p_176734_) -> {
      return buttonBuilder(p_176733_, Ingredient.of(p_176734_));
   }).put(BlockFamily.Variant.CHISELED, (p_248037_, p_248038_) -> {
      return chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, p_248037_, Ingredient.of(p_248038_));
   }).put(BlockFamily.Variant.CUT, (p_248026_, p_248027_) -> {
      return cutBuilder(RecipeCategory.BUILDING_BLOCKS, p_248026_, Ingredient.of(p_248027_));
   }).put(BlockFamily.Variant.DOOR, (p_176714_, p_176715_) -> {
      return doorBuilder(p_176714_, Ingredient.of(p_176715_));
   }).put(BlockFamily.Variant.CUSTOM_FENCE, (p_176708_, p_176709_) -> {
      return fenceBuilder(p_176708_, Ingredient.of(p_176709_));
   }).put(BlockFamily.Variant.FENCE, (p_248031_, p_248032_) -> {
      return fenceBuilder(p_248031_, Ingredient.of(p_248032_));
   }).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (p_176698_, p_176699_) -> {
      return fenceGateBuilder(p_176698_, Ingredient.of(p_176699_));
   }).put(BlockFamily.Variant.FENCE_GATE, (p_248035_, p_248036_) -> {
      return fenceGateBuilder(p_248035_, Ingredient.of(p_248036_));
   }).put(BlockFamily.Variant.SIGN, (p_176688_, p_176689_) -> {
      return signBuilder(p_176688_, Ingredient.of(p_176689_));
   }).put(BlockFamily.Variant.SLAB, (p_248017_, p_248018_) -> {
      return slabBuilder(RecipeCategory.BUILDING_BLOCKS, p_248017_, Ingredient.of(p_248018_));
   }).put(BlockFamily.Variant.STAIRS, (p_176674_, p_176675_) -> {
      return stairBuilder(p_176674_, Ingredient.of(p_176675_));
   }).put(BlockFamily.Variant.PRESSURE_PLATE, (p_248039_, p_248040_) -> {
      return pressurePlateBuilder(RecipeCategory.REDSTONE, p_248039_, Ingredient.of(p_248040_));
   }).put(BlockFamily.Variant.POLISHED, (p_248019_, p_248020_) -> {
      return polishedBuilder(RecipeCategory.BUILDING_BLOCKS, p_248019_, Ingredient.of(p_248020_));
   }).put(BlockFamily.Variant.TRAPDOOR, (p_176638_, p_176639_) -> {
      return trapdoorBuilder(p_176638_, Ingredient.of(p_176639_));
   }).put(BlockFamily.Variant.WALL, (p_248024_, p_248025_) -> {
      return wallBuilder(RecipeCategory.DECORATIONS, p_248024_, Ingredient.of(p_248025_));
   }).build();

   public RecipeProvider(PackOutput pOutput) {
      this.recipePathProvider = pOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
      this.advancementPathProvider = pOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
   }

   public CompletableFuture<?> run(CachedOutput pOutput) {
      Set<ResourceLocation> set = Sets.newHashSet();
      List<CompletableFuture<?>> list = new ArrayList<>();
      this.buildRecipes((p_253413_) -> {
         if (!set.add(p_253413_.getId())) {
            throw new IllegalStateException("Duplicate recipe " + p_253413_.getId());
         } else {
            list.add(DataProvider.saveStable(pOutput, p_253413_.serializeRecipe(), this.recipePathProvider.json(p_253413_.getId())));
            JsonObject jsonobject = p_253413_.serializeAdvancement();
            if (jsonobject != null) {
               var saveAdvancementFuture = saveAdvancement(pOutput, p_253413_, jsonobject);
               if (saveAdvancementFuture != null)
                  list.add(saveAdvancementFuture);
            }

         }
      });
      return CompletableFuture.allOf(list.toArray((p_253414_) -> {
         return new CompletableFuture[p_253414_];
      }));
   }

   /**
    * Called every time a recipe is saved to also save the advancement JSON if it exists.
    *
    * @return A completable future that saves the advancement to disk, or null to cancel saving the advancement.
    */
   @org.jetbrains.annotations.Nullable
   protected CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
      return DataProvider.saveStable(output, advancementJson, this.advancementPathProvider.json(finishedRecipe.getAdvancementId()));
   }

   protected CompletableFuture<?> buildAdvancement(CachedOutput pOutput, ResourceLocation pAdvancementOutputDir, Advancement.Builder pAdvancementBuilder) {
      return DataProvider.saveStable(pOutput, pAdvancementBuilder.serializeToJson(), this.advancementPathProvider.json(pAdvancementOutputDir));
   }

   protected abstract void buildRecipes(Consumer<FinishedRecipe> pWriter);

   protected void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> pFinishedRecipeConsumer, FeatureFlagSet pEnabledFeatures) {
      BlockFamilies.getAllFamilies().filter((p_248034_) -> {
         return p_248034_.shouldGenerateRecipe(pEnabledFeatures);
      }).forEach((p_176624_) -> {
         generateRecipes(pFinishedRecipeConsumer, p_176624_);
      });
   }

   protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pResult, ItemLike pIngredient, @Nullable String pGroup) {
      oneToOneConversionRecipe(pFinishedRecipeConsumer, pResult, pIngredient, pGroup, 1);
   }

   protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pResult, ItemLike pIngredient, @Nullable String pGroup, int pResultCount) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, pResult, pResultCount).requires(pIngredient).group(pGroup).unlockedBy(getHasName(pIngredient), has(pIngredient)).save(pFinishedRecipeConsumer, getConversionRecipeName(pResult, pIngredient));
   }

   protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
      oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
   }

   protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
      oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
   }

   protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
      for(ItemLike itemlike : pIngredients) {
         SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike)).save(pFinishedRecipeConsumer, getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
      }

   }

   protected static void netheriteSmithing(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Item pIngredientItem, RecipeCategory pCategory, Item pResultItem) {
      SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(pIngredientItem), Ingredient.of(Items.NETHERITE_INGOT), pCategory, pResultItem).unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT)).save(pFinishedRecipeConsumer, getItemName(pResultItem) + "_smithing");
   }

   protected static void trimSmithing(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Item pIngredientItem, ResourceLocation pLocation) {
      SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(pIngredientItem), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).unlocks("has_smithing_trim_template", has(pIngredientItem)).save(pFinishedRecipeConsumer, pLocation);
   }

   protected static void twoByTwoPacker(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pPacked, ItemLike pUnpacked) {
      ShapedRecipeBuilder.shaped(pCategory, pPacked, 1).define('#', pUnpacked).pattern("##").pattern("##").unlockedBy(getHasName(pUnpacked), has(pUnpacked)).save(pFinishedRecipeConsumer);
   }

   protected static void threeByThreePacker(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pPacked, ItemLike pUnpacked, String pCriterionName) {
      ShapelessRecipeBuilder.shapeless(pCategory, pPacked).requires(pUnpacked, 9).unlockedBy(pCriterionName, has(pUnpacked)).save(pFinishedRecipeConsumer);
   }

   protected static void threeByThreePacker(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pPacked, ItemLike pUnpacked) {
      threeByThreePacker(pFinishedRecipeConsumer, pCategory, pPacked, pUnpacked, getHasName(pUnpacked));
   }

   protected static void planksFromLog(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pPlanks, TagKey<Item> pLogs, int pResultCount) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, pPlanks, pResultCount).requires(pLogs).group("planks").unlockedBy("has_log", has(pLogs)).save(pFinishedRecipeConsumer);
   }

   protected static void planksFromLogs(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pPlanks, TagKey<Item> pLogs, int pResultCount) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, pPlanks, pResultCount).requires(pLogs).group("planks").unlockedBy("has_logs", has(pLogs)).save(pFinishedRecipeConsumer);
   }

   protected static void woodFromLogs(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pWood, ItemLike pLog) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pWood, 3).define('#', pLog).pattern("##").pattern("##").group("bark").unlockedBy("has_log", has(pLog)).save(pFinishedRecipeConsumer);
   }

   protected static void woodenBoat(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pBoat, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, pBoat).define('#', pMaterial).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", insideOf(Blocks.WATER)).save(pFinishedRecipeConsumer);
   }

   protected static void chestBoat(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pBoat, ItemLike pMaterial) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, pBoat).requires(Blocks.CHEST).requires(pMaterial).group("chest_boat").unlockedBy("has_boat", has(ItemTags.BOATS)).save(pFinishedRecipeConsumer);
   }

   protected static RecipeBuilder buttonBuilder(ItemLike pButton, Ingredient pMaterial) {
      return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, pButton).requires(pMaterial);
   }

   protected static RecipeBuilder doorBuilder(ItemLike pDoor, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, pDoor, 3).define('#', pMaterial).pattern("##").pattern("##").pattern("##");
   }

   protected static RecipeBuilder fenceBuilder(ItemLike pFence, Ingredient pMaterial) {
      int i = pFence == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
      Item item = pFence == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
      return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pFence, i).define('W', pMaterial).define('#', item).pattern("W#W").pattern("W#W");
   }

   protected static RecipeBuilder fenceGateBuilder(ItemLike pFenceGate, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, pFenceGate).define('#', Items.STICK).define('W', pMaterial).pattern("#W#").pattern("#W#");
   }

   protected static void pressurePlate(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pPressurePlate, ItemLike pMaterial) {
      pressurePlateBuilder(RecipeCategory.REDSTONE, pPressurePlate, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static RecipeBuilder pressurePlateBuilder(RecipeCategory pCategory, ItemLike pPressurePlate, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pPressurePlate).define('#', pMaterial).pattern("##");
   }

   protected static void slab(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pPressurePlate, ItemLike pMaterial) {
      slabBuilder(pCategory, pPressurePlate, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static RecipeBuilder slabBuilder(RecipeCategory pCategory, ItemLike pSlab, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pSlab, 6).define('#', pMaterial).pattern("###");
   }

   protected static RecipeBuilder stairBuilder(ItemLike pStairs, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pStairs, 4).define('#', pMaterial).pattern("#  ").pattern("## ").pattern("###");
   }

   protected static RecipeBuilder trapdoorBuilder(ItemLike pTrapdoor, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, pTrapdoor, 2).define('#', pMaterial).pattern("###").pattern("###");
   }

   protected static RecipeBuilder signBuilder(ItemLike pSign, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pSign, 3).group("sign").define('#', pMaterial).define('X', Items.STICK).pattern("###").pattern("###").pattern(" X ");
   }

   protected static void hangingSign(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pSign, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pSign, 6).group("hanging_sign").define('#', pMaterial).define('X', Items.CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static void colorBlockWithDye(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<Item> pDyes, List<Item> pDyeableItems, String pGroup) {
      for(int i = 0; i < pDyes.size(); ++i) {
         Item item = pDyes.get(i);
         Item item1 = pDyeableItems.get(i);
         ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, item1).requires(item).requires(Ingredient.of(pDyeableItems.stream().filter((p_288265_) -> {
            return !p_288265_.equals(item1);
         }).map(ItemStack::new))).group(pGroup).unlockedBy("has_needed_dye", has(item)).save(pFinishedRecipeConsumer, "dye_" + getItemName(item1));
      }

   }

   protected static void carpet(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pCarpet, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pCarpet, 3).define('#', pMaterial).pattern("##").group("carpet").unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static void bedFromPlanksAndWool(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pBed, ItemLike pWool) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pBed).define('#', pWool).define('X', ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(getHasName(pWool), has(pWool)).save(pFinishedRecipeConsumer);
   }

   protected static void banner(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pBanner, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pBanner).define('#', pMaterial).define('|', Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pStainedGlass, ItemLike pDye) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pStainedGlass, 8).define('#', Blocks.GLASS).define('X', pDye).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", has(Blocks.GLASS)).save(pFinishedRecipeConsumer);
   }

   protected static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pStainedGlassPane, ItemLike pStainedGlass) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pStainedGlassPane, 16).define('#', pStainedGlass).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", has(pStainedGlass)).save(pFinishedRecipeConsumer);
   }

   protected static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pStainedGlassPane, ItemLike pDye) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pStainedGlassPane, 8).define('#', Blocks.GLASS_PANE).define('$', pDye).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE)).unlockedBy(getHasName(pDye), has(pDye)).save(pFinishedRecipeConsumer, getConversionRecipeName(pStainedGlassPane, Blocks.GLASS_PANE));
   }

   protected static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pColoredTerracotta, ItemLike pDye) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pColoredTerracotta, 8).define('#', Blocks.TERRACOTTA).define('X', pDye).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", has(Blocks.TERRACOTTA)).save(pFinishedRecipeConsumer);
   }

   protected static void concretePowder(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pDyedConcretePowder, ItemLike pDye) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, pDyedConcretePowder, 8).requires(pDye).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", has(Blocks.SAND)).unlockedBy("has_gravel", has(Blocks.GRAVEL)).save(pFinishedRecipeConsumer);
   }

   protected static void candle(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pCandle, ItemLike pDye) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, pCandle).requires(Blocks.CANDLE).requires(pDye).group("dyed_candle").unlockedBy(getHasName(pDye), has(pDye)).save(pFinishedRecipeConsumer);
   }

   protected static void wall(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pWall, ItemLike pMaterial) {
      wallBuilder(pCategory, pWall, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static RecipeBuilder wallBuilder(RecipeCategory pCategory, ItemLike pWall, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pWall, 6).define('#', pMaterial).pattern("###").pattern("###");
   }

   protected static void polished(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial) {
      polishedBuilder(pCategory, pResult, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static RecipeBuilder polishedBuilder(RecipeCategory pCategory, ItemLike pResult, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pResult, 4).define('S', pMaterial).pattern("SS").pattern("SS");
   }

   protected static void cut(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pCutResult, ItemLike pMaterial) {
      cutBuilder(pCategory, pCutResult, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static ShapedRecipeBuilder cutBuilder(RecipeCategory pCategory, ItemLike pCutResult, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pCutResult, 4).define('#', pMaterial).pattern("##").pattern("##");
   }

   protected static void chiseled(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pChiseledResult, ItemLike pMaterial) {
      chiseledBuilder(pCategory, pChiseledResult, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static void mosaicBuilder(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(pCategory, pResult).define('#', pMaterial).pattern("#").pattern("#").unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer);
   }

   protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory pCategory, ItemLike pChiseledResult, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pChiseledResult).define('#', pMaterial).pattern("#").pattern("#");
   }

   protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial) {
      stonecutterResultFromBase(pFinishedRecipeConsumer, pCategory, pResult, pMaterial, 1);
   }

   protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial, int pResultCount) {
      SingleItemRecipeBuilder.stonecutting(Ingredient.of(pMaterial), pCategory, pResult, pResultCount).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(pFinishedRecipeConsumer, getConversionRecipeName(pResult, pMaterial) + "_stonecutting");
   }

   protected static void smeltingResultFromBase(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pResult, ItemLike pIngredient) {
      SimpleCookingRecipeBuilder.smelting(Ingredient.of(pIngredient), RecipeCategory.BUILDING_BLOCKS, pResult, 0.1F, 200).unlockedBy(getHasName(pIngredient), has(pIngredient)).save(pFinishedRecipeConsumer);
   }

   protected static void nineBlockStorageRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked) {
      nineBlockStorageRecipes(pFinishedRecipeConsumer, pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, getSimpleRecipeName(pPacked), (String)null, getSimpleRecipeName(pUnpacked), (String)null);
   }

   protected static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pPackedName, String pPackedGroup) {
      nineBlockStorageRecipes(pFinishedRecipeConsumer, pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, pPackedName, pPackedGroup, getSimpleRecipeName(pUnpacked), (String)null);
   }

   protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pUnpackedName, String pUnpackedGroup) {
      nineBlockStorageRecipes(pFinishedRecipeConsumer, pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, getSimpleRecipeName(pPacked), (String)null, pUnpackedName, pUnpackedGroup);
   }

   protected static void nineBlockStorageRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pPackedName, @Nullable String pPackedGroup, String pUnpackedName, @Nullable String pUnpackedGroup) {
      ShapelessRecipeBuilder.shapeless(pUnpackedCategory, pUnpacked, 9).requires(pPacked).group(pUnpackedGroup).unlockedBy(getHasName(pPacked), has(pPacked)).save(pFinishedRecipeConsumer, new ResourceLocation(pUnpackedName));
      ShapedRecipeBuilder.shaped(pPackedCategory, pPacked).define('#', pUnpacked).pattern("###").pattern("###").pattern("###").group(pPackedGroup).unlockedBy(getHasName(pUnpacked), has(pUnpacked)).save(pFinishedRecipeConsumer, new ResourceLocation(pPackedName));
   }

   protected static void copySmithingTemplate(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pResult, TagKey<Item> pBaseItem) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pResult, 2).define('#', Items.DIAMOND).define('C', pBaseItem).define('S', pResult).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(pResult), has(pResult)).save(pFinishedRecipeConsumer);
   }

   protected static void copySmithingTemplate(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ItemLike pResult, ItemLike pBaseItem) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pResult, 2).define('#', Items.DIAMOND).define('C', pBaseItem).define('S', pResult).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(pResult), has(pResult)).save(pFinishedRecipeConsumer);
   }

   protected static void cookRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, String pCookingMethod, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, int pCookingTime) {
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.BEEF, Items.COOKED_BEEF, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.COD, Items.COOKED_COD, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.KELP, Items.DRIED_KELP, 0.1F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.SALMON, Items.COOKED_SALMON, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.POTATO, Items.BAKED_POTATO, 0.35F);
      simpleCookingRecipe(pFinishedRecipeConsumer, pCookingMethod, pCookingSerializer, pCookingTime, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
   }

   protected static void simpleCookingRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, String pCookingMethod, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, int pCookingTime, ItemLike pIngredient, ItemLike pResult, float pExperience) {
      SimpleCookingRecipeBuilder.generic(Ingredient.of(pIngredient), RecipeCategory.FOOD, pResult, pExperience, pCookingTime, pCookingSerializer).unlockedBy(getHasName(pIngredient), has(pIngredient)).save(pFinishedRecipeConsumer, getItemName(pResult) + "_from_" + pCookingMethod);
   }

   protected static void waxRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
      HoneycombItem.WAXABLES.get().forEach((p_248022_, p_248023_) -> {
         ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, p_248023_).requires(p_248022_).requires(Items.HONEYCOMB).group(getItemName(p_248023_)).unlockedBy(getHasName(p_248022_), has(p_248022_)).save(pFinishedRecipeConsumer, getConversionRecipeName(p_248023_, Items.HONEYCOMB));
      });
   }

   protected static void generateRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, BlockFamily pFamily) {
      pFamily.getVariants().forEach((p_176529_, p_176530_) -> {
         BiFunction<ItemLike, ItemLike, RecipeBuilder> bifunction = SHAPE_BUILDERS.get(p_176529_);
         ItemLike itemlike = getBaseBlock(pFamily, p_176529_);
         if (bifunction != null) {
            RecipeBuilder recipebuilder = bifunction.apply(p_176530_, itemlike);
            pFamily.getRecipeGroupPrefix().ifPresent((p_176601_) -> {
               recipebuilder.group(p_176601_ + (p_176529_ == BlockFamily.Variant.CUT ? "" : "_" + p_176529_.getName()));
            });
            recipebuilder.unlockedBy(pFamily.getRecipeUnlockedBy().orElseGet(() -> {
               return getHasName(itemlike);
            }), has(itemlike));
            recipebuilder.save(pFinishedRecipeConsumer);
         }

         if (p_176529_ == BlockFamily.Variant.CRACKED) {
            smeltingResultFromBase(pFinishedRecipeConsumer, p_176530_, itemlike);
         }

      });
   }

   protected static Block getBaseBlock(BlockFamily pFamily, BlockFamily.Variant pVariant) {
      if (pVariant == BlockFamily.Variant.CHISELED) {
         if (!pFamily.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
            throw new IllegalStateException("Slab is not defined for the family.");
         } else {
            return pFamily.get(BlockFamily.Variant.SLAB);
         }
      } else {
         return pFamily.getBaseBlock();
      }
   }

   /**
    * Creates a new {@link EnterBlockTrigger} for use with recipe unlock criteria.
    */
   protected static EnterBlockTrigger.TriggerInstance insideOf(Block pBlock) {
      return new EnterBlockTrigger.TriggerInstance(ContextAwarePredicate.ANY, pBlock, StatePropertiesPredicate.ANY);
   }

   protected static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints pCount, ItemLike pItem) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(pItem).withCount(pCount).build());
   }

   /**
    * Creates a new {@link InventoryChangeTrigger} that checks for a player having a certain item.
    */
   protected static InventoryChangeTrigger.TriggerInstance has(ItemLike pItemLike) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(pItemLike).build());
   }

   /**
    * Creates a new {@link InventoryChangeTrigger} that checks for a player having an item within the given tag.
    */
   protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> pTag) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(pTag).build());
   }

   /**
    * Creates a new {@link InventoryChangeTrigger} that checks for a player having a certain item.
    */
   protected static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... pPredicates) {
      return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, pPredicates);
   }

   protected static String getHasName(ItemLike pItemLike) {
      return "has_" + getItemName(pItemLike);
   }

   protected static String getItemName(ItemLike pItemLike) {
      return BuiltInRegistries.ITEM.getKey(pItemLike.asItem()).getPath();
   }

   protected static String getSimpleRecipeName(ItemLike pItemLike) {
      return getItemName(pItemLike);
   }

   protected static String getConversionRecipeName(ItemLike pResult, ItemLike pIngredient) {
      return getItemName(pResult) + "_from_" + getItemName(pIngredient);
   }

   protected static String getSmeltingRecipeName(ItemLike pItemLike) {
      return getItemName(pItemLike) + "_from_smelting";
   }

   protected static String getBlastingRecipeName(ItemLike pItemLike) {
      return getItemName(pItemLike) + "_from_blasting";
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public final String getName() {
      return "Recipes";
   }
}
