package net.minecraft.data.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public abstract class BlockLootSubProvider implements LootTableSubProvider {
   protected static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
   protected static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
   protected static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
   private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
   private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
   protected final Set<Item> explosionResistant;
   protected final FeatureFlagSet enabledFeatures;
   protected final Map<ResourceLocation, LootTable.Builder> map;
   protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
   private static final float[] NORMAL_LEAVES_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};

   protected BlockLootSubProvider(Set<Item> pExplosionResistant, FeatureFlagSet pEnabledFeatures) {
      this(pExplosionResistant, pEnabledFeatures, new HashMap<>());
   }

   protected BlockLootSubProvider(Set<Item> pExplosionResistant, FeatureFlagSet pEnabledFeatures, Map<ResourceLocation, LootTable.Builder> pMap) {
      this.explosionResistant = pExplosionResistant;
      this.enabledFeatures = pEnabledFeatures;
      this.map = pMap;
   }

   protected <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike pItem, FunctionUserBuilder<T> pFunctionBuilder) {
      return (T)(!this.explosionResistant.contains(pItem.asItem()) ? pFunctionBuilder.apply(ApplyExplosionDecay.explosionDecay()) : pFunctionBuilder.unwrap());
   }

   protected <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike pItem, ConditionUserBuilder<T> pConditionBuilder) {
      return (T)(!this.explosionResistant.contains(pItem.asItem()) ? pConditionBuilder.when(ExplosionCondition.survivesExplosion()) : pConditionBuilder.unwrap());
   }

   public LootTable.Builder createSingleItemTable(ItemLike pItem) {
      return LootTable.lootTable().withPool(this.applyExplosionCondition(pItem, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem))));
   }

   /**
    * If the condition from {@code conditionBuilder} succeeds, drops 1 {@code block}.
    * Otherwise, drops loot specified by {@code alternativeBuilder}.
    */
   protected static LootTable.Builder createSelfDropDispatchTable(Block pBlock, LootItemCondition.Builder pConditionBuilder, LootPoolEntryContainer.Builder<?> pAlternativeBuilder) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).when(pConditionBuilder).otherwise(pAlternativeBuilder)));
   }

   /**
    * If the block is mined with Silk Touch, drops 1 {@code block}.
    * Otherwise, drops loot specified by {@code builder}.
    */
   protected static LootTable.Builder createSilkTouchDispatchTable(Block pBlock, LootPoolEntryContainer.Builder<?> pBuilder) {
      return createSelfDropDispatchTable(pBlock, HAS_SILK_TOUCH, pBuilder);
   }

   /**
    * If the block is mined with Shears, drops 1 {@code block}.
    * Otherwise, drops loot specified by {@code builder}.
    */
   protected static LootTable.Builder createShearsDispatchTable(Block pBlock, LootPoolEntryContainer.Builder<?> pBuilder) {
      return createSelfDropDispatchTable(pBlock, HAS_SHEARS, pBuilder);
   }

   /**
    * If the block is mined either with Silk Touch or Shears, drops 1 {@code block}.
    * Otherwise, drops loot specified by {@code builder}.
    */
   protected static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block pBlock, LootPoolEntryContainer.Builder<?> pBuilder) {
      return createSelfDropDispatchTable(pBlock, HAS_SHEARS_OR_SILK_TOUCH, pBuilder);
   }

   protected LootTable.Builder createSingleItemTableWithSilkTouch(Block pBlock, ItemLike pItem) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionCondition(pBlock, LootItem.lootTableItem(pItem)));
   }

   protected LootTable.Builder createSingleItemTable(ItemLike pItem, NumberProvider pCount) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(this.applyExplosionDecay(pItem, LootItem.lootTableItem(pItem).apply(SetItemCountFunction.setCount(pCount)))));
   }

   protected LootTable.Builder createSingleItemTableWithSilkTouch(Block pBlock, ItemLike pItem, NumberProvider pCount) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pItem).apply(SetItemCountFunction.setCount(pCount))));
   }

   protected static LootTable.Builder createSilkTouchOnlyTable(ItemLike pItem) {
      return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem)));
   }

   protected LootTable.Builder createPotFlowerItemTable(ItemLike pItem) {
      return LootTable.lootTable().withPool(this.applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))).withPool(this.applyExplosionCondition(pItem, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem))));
   }

   protected LootTable.Builder createSlabItemTable(Block pBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pBlock).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))))));
   }

   protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block pBlock, Property<T> pProperty, T pValue) {
      return LootTable.lootTable().withPool(this.applyExplosionCondition(pBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(pProperty, pValue))))));
   }

   protected LootTable.Builder createNameableBlockEntityTable(Block pBlock) {
      return LootTable.lootTable().withPool(this.applyExplosionCondition(pBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))));
   }

   protected LootTable.Builder createShulkerBoxDrop(Block pBlock) {
      return LootTable.lootTable().withPool(this.applyExplosionCondition(pBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)).apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Lock", "BlockEntityTag.Lock").copy("LootTable", "BlockEntityTag.LootTable").copy("LootTableSeed", "BlockEntityTag.LootTableSeed")).apply(SetContainerContents.setContents(BlockEntityType.SHULKER_BOX).withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS))))));
   }

   protected LootTable.Builder createCopperOreDrops(Block pBlock) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(Items.RAW_COPPER).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
   }

   protected LootTable.Builder createLapisOreDrops(Block pBlock) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(Items.LAPIS_LAZULI).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
   }

   protected LootTable.Builder createRedstoneOreDrops(Block pBlock) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 5.0F))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))));
   }

   protected LootTable.Builder createBannerDrop(Block pBlock) {
      return LootTable.lootTable().withPool(this.applyExplosionCondition(pBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)).apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns")))));
   }

   protected static LootTable.Builder createBeeNestDrop(Block pBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees")).apply(CopyBlockState.copyState(pBlock).copy(BeehiveBlock.HONEY_LEVEL))));
   }

   protected static LootTable.Builder createBeeHiveDrop(Block pBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).when(HAS_SILK_TOUCH).apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees")).apply(CopyBlockState.copyState(pBlock).copy(BeehiveBlock.HONEY_LEVEL)).otherwise(LootItem.lootTableItem(pBlock))));
   }

   protected static LootTable.Builder createCaveVinesDrop(Block pBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.GLOW_BERRIES)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))));
   }

   protected LootTable.Builder createOreDrop(Block pBlock, Item pItem) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pItem).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
   }

   protected LootTable.Builder createMushroomBlockDrop(Block pBlock, ItemLike pItem) {
      return createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pItem).apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0F, 2.0F))).apply(LimitCount.limitCount(IntRange.lowerBound(0)))));
   }

   protected LootTable.Builder createGrassDrops(Block pBlock) {
      return createShearsDispatchTable(pBlock, this.applyExplosionDecay(pBlock, LootItem.lootTableItem(Items.WHEAT_SEEDS).when(LootItemRandomChanceCondition.randomChance(0.125F)).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))));
   }

   /**
    * Creates a builder that drops the given IItemProvider in amounts between 0 and 3, based on the AGE property. Only
    * used in vanilla for pumpkin and melon stems.
    */
   public LootTable.Builder createStemDrops(Block pBlock, Item pItem) {
      return LootTable.lootTable().withPool(this.applyExplosionDecay(pBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem).apply(StemBlock.AGE.getPossibleValues(), (p_249795_) -> {
         return SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, (float)(p_249795_ + 1) / 15.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, p_249795_)));
      }))));
   }

   public LootTable.Builder createAttachedStemDrops(Block pBlock, Item pItem) {
      return LootTable.lootTable().withPool(this.applyExplosionDecay(pBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))));
   }

   protected static LootTable.Builder createShearsOnlyDrop(ItemLike pItem) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_SHEARS).add(LootItem.lootTableItem(pItem)));
   }

   protected LootTable.Builder createMultifaceBlockDrops(Block pBlock, LootItemCondition.Builder pBuilder) {
      return LootTable.lootTable().withPool(LootPool.lootPool().add(this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pBlock).when(pBuilder).apply(Direction.values(), (p_251536_) -> {
         return SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty(p_251536_), true)));
      }).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true)))));
   }

   /**
    * Used for all leaves, drops self with silk touch, otherwise drops the second Block param with the passed chances
    * for fortune levels, adding in sticks.
    */
   protected LootTable.Builder createLeavesDrops(Block pLeavesBlock, Block pSaplingBlock, float... pChances) {
      return createSilkTouchOrShearsDispatchTable(pLeavesBlock, this.applyExplosionCondition(pLeavesBlock, LootItem.lootTableItem(pSaplingBlock)).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, pChances))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(this.applyExplosionDecay(pLeavesBlock, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, NORMAL_LEAVES_STICK_CHANCES))));
   }

   /**
    * Used for oak and dark oak, same as droppingWithChancesAndSticks but adding in apples.
    */
   protected LootTable.Builder createOakLeavesDrops(Block pOakLeavesBlock, Block pSaplingBlock, float... pChances) {
      return this.createLeavesDrops(pOakLeavesBlock, pSaplingBlock, pChances).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(this.applyExplosionCondition(pOakLeavesBlock, LootItem.lootTableItem(Items.APPLE)).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F))));
   }

   protected LootTable.Builder createMangroveLeavesDrops(Block pBlock) {
      return createSilkTouchOrShearsDispatchTable(pBlock, this.applyExplosionDecay(Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, NORMAL_LEAVES_STICK_CHANCES)));
   }

   /**
    * If {@code dropGrownCropCondition} fails (i.e. crop is not ready), drops 1 {@code seedsItem}.
    * If {@code dropGrownCropCondition} succeeds (i.e. crop is ready), drops 1 {@code grownCropItem}, and 0-3 {@code
    * seedsItem} with fortune applied.
    */
   protected LootTable.Builder createCropDrops(Block pCropBlock, Item pGrownCropItem, Item pSeedsItem, LootItemCondition.Builder pDropGrownCropCondition) {
      return this.applyExplosionDecay(pCropBlock, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(pGrownCropItem).when(pDropGrownCropCondition).otherwise(LootItem.lootTableItem(pSeedsItem)))).withPool(LootPool.lootPool().when(pDropGrownCropCondition).add(LootItem.lootTableItem(pSeedsItem).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))));
   }

   protected static LootTable.Builder createDoublePlantShearsDrop(Block pSheared) {
      return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SHEARS).add(LootItem.lootTableItem(pSheared).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))));
   }

   protected LootTable.Builder createDoublePlantWithSeedDrops(Block pBlock, Block pSheared) {
      LootPoolEntryContainer.Builder<?> builder = LootItem.lootTableItem(pSheared).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))).when(HAS_SHEARS).otherwise(this.applyExplosionCondition(pBlock, LootItem.lootTableItem(Items.WHEAT_SEEDS)).when(LootItemRandomChanceCondition.randomChance(0.125F)));
      return LootTable.lootTable().withPool(LootPool.lootPool().add(builder).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER).build()).build()), new BlockPos(0, 1, 0)))).withPool(LootPool.lootPool().add(builder).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(pBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER).build()).build()), new BlockPos(0, -1, 0))));
   }

   protected LootTable.Builder createCandleDrops(Block pCandleBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(this.applyExplosionDecay(pCandleBlock, LootItem.lootTableItem(pCandleBlock).apply(List.of(2, 3, 4), (p_249985_) -> {
         return SetItemCountFunction.setCount(ConstantValue.exactly((float)p_249985_.intValue())).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pCandleBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, p_249985_)));
      }))));
   }

   protected LootTable.Builder createPetalsDrops(Block pPetalBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(this.applyExplosionDecay(pPetalBlock, LootItem.lootTableItem(pPetalBlock).apply(IntStream.rangeClosed(1, 4).boxed().toList(), (p_272348_) -> {
         return SetItemCountFunction.setCount(ConstantValue.exactly((float)p_272348_.intValue())).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pPetalBlock).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PinkPetalsBlock.AMOUNT, p_272348_)));
      }))));
   }

   protected static LootTable.Builder createCandleCakeDrops(Block pCandleCakeBlock) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pCandleCakeBlock)));
   }

   public static LootTable.Builder noDrop() {
      return LootTable.lootTable();
   }

   protected abstract void generate();

   protected Iterable<Block> getKnownBlocks() {
      return BuiltInRegistries.BLOCK;
   }

   public void generate(BiConsumer<ResourceLocation, LootTable.Builder> pOutput) {
      this.generate();
      Set<ResourceLocation> set = new HashSet<>();

      for(Block block : getKnownBlocks()) {
         if (block.isEnabled(this.enabledFeatures)) {
            ResourceLocation resourcelocation = block.getLootTable();
            if (resourcelocation != BuiltInLootTables.EMPTY && set.add(resourcelocation)) {
               LootTable.Builder loottable$builder = this.map.remove(resourcelocation);
               if (loottable$builder == null) {
                  throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourcelocation, BuiltInRegistries.BLOCK.getKey(block)));
               }

               pOutput.accept(resourcelocation, loottable$builder);
            }
         }
      }

      if (!this.map.isEmpty()) {
         throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
      }
   }

   protected void addNetherVinesDropTable(Block pVines, Block pPlant) {
      LootTable.Builder loottable$builder = createSilkTouchOrShearsDispatchTable(pVines, LootItem.lootTableItem(pVines).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.33F, 0.55F, 0.77F, 1.0F)));
      this.add(pVines, loottable$builder);
      this.add(pPlant, loottable$builder);
   }

   protected LootTable.Builder createDoorTable(Block pDoorBlock) {
      return this.createSinglePropConditionTable(pDoorBlock, DoorBlock.HALF, DoubleBlockHalf.LOWER);
   }

   protected void dropPottedContents(Block pFlowerPot) {
      this.add(pFlowerPot, (p_250193_) -> {
         return this.createPotFlowerItemTable(((FlowerPotBlock)p_250193_).getContent());
      });
   }

   protected void otherWhenSilkTouch(Block pBlock, Block pOther) {
      this.add(pBlock, createSilkTouchOnlyTable(pOther));
   }

   protected void dropOther(Block pBlock, ItemLike pItem) {
      this.add(pBlock, this.createSingleItemTable(pItem));
   }

   protected void dropWhenSilkTouch(Block pBlock) {
      this.otherWhenSilkTouch(pBlock, pBlock);
   }

   protected void dropSelf(Block pBlock) {
      this.dropOther(pBlock, pBlock);
   }

   protected void add(Block pBlock, Function<Block, LootTable.Builder> pFactory) {
      this.add(pBlock, pFactory.apply(pBlock));
   }

   protected void add(Block pBlock, LootTable.Builder pBuilder) {
      this.map.put(pBlock.getLootTable(), pBuilder);
   }
}
