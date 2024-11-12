package net.minecraft.data.advancements.packs;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ChanneledLightningTrigger;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LighthingBoltPredicate;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.SlideDownBlockTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.advancements.critereon.TargetBlockTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.DecoratedPotRecipe;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class VanillaAdventureAdvancements implements AdvancementSubProvider {
   private static final int DISTANCE_FROM_BOTTOM_TO_TOP = 384;
   private static final int Y_COORDINATE_AT_TOP = 320;
   private static final int Y_COORDINATE_AT_BOTTOM = -64;
   private static final int BEDROCK_THICKNESS = 5;
   private static final EntityType<?>[] MOBS_TO_KILL = new EntityType[]{EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN};

   private static LightningStrikeTrigger.TriggerInstance fireCountAndBystander(MinMaxBounds.Ints pBlocksSetOnFire, EntityPredicate pPredicate) {
      return LightningStrikeTrigger.TriggerInstance.lighthingStrike(EntityPredicate.Builder.entity().distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(30.0D))).subPredicate(LighthingBoltPredicate.blockSetOnFire(pBlocksSetOnFire)).build(), pPredicate);
   }

   private static UsingItemTrigger.TriggerInstance lookAtThroughItem(EntityType<?> pEntityType, Item pItem) {
      return UsingItemTrigger.TriggerInstance.lookingAt(EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().setLookingAt(EntityPredicate.Builder.entity().of(pEntityType).build()).build()), ItemPredicate.Builder.item().of(pItem));
   }

   public void generate(HolderLookup.Provider pRegistries, Consumer<Advancement> pWriter) {
      Advancement advancement = Advancement.Builder.advancement().display(Items.MAP, Component.translatable("advancements.adventure.root.title"), Component.translatable("advancements.adventure.root.description"), new ResourceLocation("textures/gui/advancements/backgrounds/adventure.png"), FrameType.TASK, false, false, false).requirements(RequirementsStrategy.OR).addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity()).addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer()).save(pWriter, "adventure/root");
      Advancement advancement1 = Advancement.Builder.advancement().parent(advancement).display(Blocks.RED_BED, Component.translatable("advancements.adventure.sleep_in_bed.title"), Component.translatable("advancements.adventure.sleep_in_bed.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("slept_in_bed", PlayerTrigger.TriggerInstance.sleptInBed()).save(pWriter, "adventure/sleep_in_bed");
      createAdventuringTime(pWriter, advancement1, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);
      Advancement advancement2 = Advancement.Builder.advancement().parent(advancement).display(Items.EMERALD, Component.translatable("advancements.adventure.trade.title"), Component.translatable("advancements.adventure.trade.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("traded", TradeTrigger.TriggerInstance.tradedWithVillager()).save(pWriter, "adventure/trade");
      Advancement.Builder.advancement().parent(advancement2).display(Items.EMERALD, Component.translatable("advancements.adventure.trade_at_world_height.title"), Component.translatable("advancements.adventure.trade_at_world_height.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("trade_at_world_height", TradeTrigger.TriggerInstance.tradedWithVillager(EntityPredicate.Builder.entity().located(LocationPredicate.atYLocation(MinMaxBounds.Doubles.atLeast(319.0D))))).save(pWriter, "adventure/trade_at_world_height");
      Advancement advancement3 = addMobsToKill(Advancement.Builder.advancement()).parent(advancement).display(Items.IRON_SWORD, Component.translatable("advancements.adventure.kill_a_mob.title"), Component.translatable("advancements.adventure.kill_a_mob.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).requirements(RequirementsStrategy.OR).save(pWriter, "adventure/kill_a_mob");
      addMobsToKill(Advancement.Builder.advancement()).parent(advancement3).display(Items.DIAMOND_SWORD, Component.translatable("advancements.adventure.kill_all_mobs.title"), Component.translatable("advancements.adventure.kill_all_mobs.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save(pWriter, "adventure/kill_all_mobs");
      Advancement advancement4 = Advancement.Builder.advancement().parent(advancement3).display(Items.BOW, Component.translatable("advancements.adventure.shoot_arrow.title"), Component.translatable("advancements.adventure.shoot_arrow.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("shot_arrow", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntity(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS))))).save(pWriter, "adventure/shoot_arrow");
      Advancement advancement5 = Advancement.Builder.advancement().parent(advancement3).display(Items.TRIDENT, Component.translatable("advancements.adventure.throw_trident.title"), Component.translatable("advancements.adventure.throw_trident.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("shot_trident", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntity(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of(EntityType.TRIDENT))))).save(pWriter, "adventure/throw_trident");
      Advancement.Builder.advancement().parent(advancement5).display(Items.TRIDENT, Component.translatable("advancements.adventure.very_very_frightening.title"), Component.translatable("advancements.adventure.very_very_frightening.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("struck_villager", ChanneledLightningTrigger.TriggerInstance.channeledLightning(EntityPredicate.Builder.entity().of(EntityType.VILLAGER).build())).save(pWriter, "adventure/very_very_frightening");
      Advancement.Builder.advancement().parent(advancement2).display(Blocks.CARVED_PUMPKIN, Component.translatable("advancements.adventure.summon_iron_golem.title"), Component.translatable("advancements.adventure.summon_iron_golem.description"), (ResourceLocation)null, FrameType.GOAL, true, true, false).addCriterion("summoned_golem", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(EntityType.IRON_GOLEM))).save(pWriter, "adventure/summon_iron_golem");
      Advancement.Builder.advancement().parent(advancement4).display(Items.ARROW, Component.translatable("advancements.adventure.sniper_duel.title"), Component.translatable("advancements.adventure.sniper_duel.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("killed_skeleton", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.SKELETON).distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(50.0D))), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)))).save(pWriter, "adventure/sniper_duel");
      Advancement.Builder.advancement().parent(advancement3).display(Items.TOTEM_OF_UNDYING, Component.translatable("advancements.adventure.totem_of_undying.title"), Component.translatable("advancements.adventure.totem_of_undying.description"), (ResourceLocation)null, FrameType.GOAL, true, true, false).addCriterion("used_totem", UsedTotemTrigger.TriggerInstance.usedTotem(Items.TOTEM_OF_UNDYING)).save(pWriter, "adventure/totem_of_undying");
      Advancement advancement6 = Advancement.Builder.advancement().parent(advancement).display(Items.CROSSBOW, Component.translatable("advancements.adventure.ol_betsy.title"), Component.translatable("advancements.adventure.ol_betsy.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("shot_crossbow", ShotCrossbowTrigger.TriggerInstance.shotCrossbow(Items.CROSSBOW)).save(pWriter, "adventure/ol_betsy");
      Advancement.Builder.advancement().parent(advancement6).display(Items.CROSSBOW, Component.translatable("advancements.adventure.whos_the_pillager_now.title"), Component.translatable("advancements.adventure.whos_the_pillager_now.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("kill_pillager", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(EntityPredicate.Builder.entity().of(EntityType.PILLAGER))).save(pWriter, "adventure/whos_the_pillager_now");
      Advancement.Builder.advancement().parent(advancement6).display(Items.CROSSBOW, Component.translatable("advancements.adventure.two_birds_one_arrow.title"), Component.translatable("advancements.adventure.two_birds_one_arrow.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(65)).addCriterion("two_birds", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(EntityPredicate.Builder.entity().of(EntityType.PHANTOM), EntityPredicate.Builder.entity().of(EntityType.PHANTOM))).save(pWriter, "adventure/two_birds_one_arrow");
      Advancement.Builder.advancement().parent(advancement6).display(Items.CROSSBOW, Component.translatable("advancements.adventure.arbalistic.title"), Component.translatable("advancements.adventure.arbalistic.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(85)).addCriterion("arbalistic", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(MinMaxBounds.Ints.exactly(5))).save(pWriter, "adventure/arbalistic");
      Advancement advancement7 = Advancement.Builder.advancement().parent(advancement).display(Raid.getLeaderBannerInstance(), Component.translatable("advancements.adventure.voluntary_exile.title"), Component.translatable("advancements.adventure.voluntary_exile.description"), (ResourceLocation)null, FrameType.TASK, true, true, true).addCriterion("voluntary_exile", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.CAPTAIN))).save(pWriter, "adventure/voluntary_exile");
      Advancement.Builder.advancement().parent(advancement7).display(Raid.getLeaderBannerInstance(), Component.translatable("advancements.adventure.hero_of_the_village.title"), Component.translatable("advancements.adventure.hero_of_the_village.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(100)).addCriterion("hero_of_the_village", PlayerTrigger.TriggerInstance.raidWon()).save(pWriter, "adventure/hero_of_the_village");
      Advancement.Builder.advancement().parent(advancement).display(Blocks.HONEY_BLOCK.asItem(), Component.translatable("advancements.adventure.honey_block_slide.title"), Component.translatable("advancements.adventure.honey_block_slide.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("honey_block_slide", SlideDownBlockTrigger.TriggerInstance.slidesDownBlock(Blocks.HONEY_BLOCK)).save(pWriter, "adventure/honey_block_slide");
      Advancement.Builder.advancement().parent(advancement4).display(Blocks.TARGET.asItem(), Component.translatable("advancements.adventure.bullseye.title"), Component.translatable("advancements.adventure.bullseye.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("bullseye", TargetBlockTrigger.TriggerInstance.targetHit(MinMaxBounds.Ints.exactly(15), EntityPredicate.wrap(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(30.0D))).build()))).save(pWriter, "adventure/bullseye");
      Advancement.Builder.advancement().parent(advancement1).display(Items.LEATHER_BOOTS, Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"), Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("walk_on_powder_snow_with_leather_boots", PlayerTrigger.TriggerInstance.walkOnBlockWithEquipment(Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)).save(pWriter, "adventure/walk_on_powder_snow_with_leather_boots");
      Advancement.Builder.advancement().parent(advancement).display(Items.LIGHTNING_ROD, Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.title"), Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("lightning_rod_with_villager_no_fire", fireCountAndBystander(MinMaxBounds.Ints.exactly(0), EntityPredicate.Builder.entity().of(EntityType.VILLAGER).build())).save(pWriter, "adventure/lightning_rod_with_villager_no_fire");
      Advancement advancement8 = Advancement.Builder.advancement().parent(advancement).display(Items.SPYGLASS, Component.translatable("advancements.adventure.spyglass_at_parrot.title"), Component.translatable("advancements.adventure.spyglass_at_parrot.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("spyglass_at_parrot", lookAtThroughItem(EntityType.PARROT, Items.SPYGLASS)).save(pWriter, "adventure/spyglass_at_parrot");
      Advancement advancement9 = Advancement.Builder.advancement().parent(advancement8).display(Items.SPYGLASS, Component.translatable("advancements.adventure.spyglass_at_ghast.title"), Component.translatable("advancements.adventure.spyglass_at_ghast.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("spyglass_at_ghast", lookAtThroughItem(EntityType.GHAST, Items.SPYGLASS)).save(pWriter, "adventure/spyglass_at_ghast");
      Advancement.Builder.advancement().parent(advancement1).display(Items.JUKEBOX, Component.translatable("advancements.adventure.play_jukebox_in_meadows.title"), Component.translatable("advancements.adventure.play_jukebox_in_meadows.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("play_jukebox_in_meadows", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBiome(Biomes.MEADOW).setBlock(BlockPredicate.Builder.block().of(Blocks.JUKEBOX).build()), ItemPredicate.Builder.item().of(ItemTags.MUSIC_DISCS))).save(pWriter, "adventure/play_jukebox_in_meadows");
      Advancement.Builder.advancement().parent(advancement9).display(Items.SPYGLASS, Component.translatable("advancements.adventure.spyglass_at_dragon.title"), Component.translatable("advancements.adventure.spyglass_at_dragon.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("spyglass_at_dragon", lookAtThroughItem(EntityType.ENDER_DRAGON, Items.SPYGLASS)).save(pWriter, "adventure/spyglass_at_dragon");
      Advancement.Builder.advancement().parent(advancement).display(Items.WATER_BUCKET, Component.translatable("advancements.adventure.fall_from_world_height.title"), Component.translatable("advancements.adventure.fall_from_world_height.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("fall_from_world_height", DistanceTrigger.TriggerInstance.fallFromHeight(EntityPredicate.Builder.entity().located(LocationPredicate.atYLocation(MinMaxBounds.Doubles.atMost(-59.0D))), DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(379.0D)), LocationPredicate.atYLocation(MinMaxBounds.Doubles.atLeast(319.0D)))).save(pWriter, "adventure/fall_from_world_height");
      Advancement.Builder.advancement().parent(advancement3).display(Blocks.SCULK_CATALYST, Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.title"), Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).addCriterion("kill_mob_near_sculk_catalyst", KilledTrigger.TriggerInstance.playerKilledEntityNearSculkCatalyst()).save(pWriter, "adventure/kill_mob_near_sculk_catalyst");
      Advancement.Builder.advancement().parent(advancement).display(Blocks.SCULK_SENSOR, Component.translatable("advancements.adventure.avoid_vibration.title"), Component.translatable("advancements.adventure.avoid_vibration.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("avoid_vibration", PlayerTrigger.TriggerInstance.avoidVibration()).save(pWriter, "adventure/avoid_vibration");
      Advancement advancement10 = respectingTheRemnantsCriterions(Advancement.Builder.advancement()).parent(advancement).display(Items.BRUSH, Component.translatable("advancements.adventure.salvage_sherd.title"), Component.translatable("advancements.adventure.salvage_sherd.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).save(pWriter, "adventure/salvage_sherd");
      Advancement.Builder.advancement().parent(advancement10).display(DecoratedPotRecipe.createDecoratedPotItem(new DecoratedPotBlockEntity.Decorations(Items.BRICK, Items.HEART_POTTERY_SHERD, Items.BRICK, Items.EXPLORER_POTTERY_SHERD)), Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.title"), Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("pot_crafted_using_only_sherds", RecipeCraftedTrigger.TriggerInstance.craftedItem(new ResourceLocation("minecraft:decorated_pot"), List.of(ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS).build(), ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS).build(), ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS).build(), ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS).build()))).save(pWriter, "adventure/craft_decorated_pot_using_only_sherds");
      Advancement advancement11 = craftingANewLook(Advancement.Builder.advancement()).parent(advancement).display(new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE), Component.translatable("advancements.adventure.trim_with_any_armor_pattern.title"), Component.translatable("advancements.adventure.trim_with_any_armor_pattern.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).save(pWriter, "adventure/trim_with_any_armor_pattern");
      smithingWithStyle(Advancement.Builder.advancement()).parent(advancement11).display(new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.title"), Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).save(pWriter, "adventure/trim_with_all_exclusive_armor_patterns");
      Advancement.Builder.advancement().parent(advancement).display(Items.CHISELED_BOOKSHELF, Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.title"), Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).requirements(RequirementsStrategy.OR).addCriterion("chiseled_bookshelf", placedBlockReadByComparator(Blocks.CHISELED_BOOKSHELF)).addCriterion("comparator", placedComparatorReadingBlock(Blocks.CHISELED_BOOKSHELF)).save(pWriter, "adventure/read_power_of_chiseled_bookshelf");
   }

   private static CriterionTriggerInstance placedBlockReadByComparator(Block pBlock) {
      LootItemCondition.Builder[] alootitemcondition$builder = ComparatorBlock.FACING.getPossibleValues().stream().map((p_286187_) -> {
         StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, p_286187_).build();
         BlockPredicate blockpredicate = BlockPredicate.Builder.block().of(Blocks.COMPARATOR).setProperties(statepropertiespredicate).build();
         return LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(blockpredicate), new BlockPos(p_286187_.getOpposite().getNormal()));
      }).toArray((p_286188_) -> {
         return new LootItemCondition.Builder[p_286188_];
      });
      return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock), AnyOfCondition.anyOf(alootitemcondition$builder));
   }

   private static CriterionTriggerInstance placedComparatorReadingBlock(Block pBlock) {
      LootItemCondition.Builder[] alootitemcondition$builder = ComparatorBlock.FACING.getPossibleValues().stream().map((p_286190_) -> {
         StatePropertiesPredicate.Builder statepropertiespredicate$builder = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, p_286190_);
         LootItemBlockStatePropertyCondition.Builder lootitemblockstatepropertycondition$builder = (new LootItemBlockStatePropertyCondition.Builder(Blocks.COMPARATOR)).setProperties(statepropertiespredicate$builder);
         LootItemCondition.Builder lootitemcondition$builder = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(pBlock).build()), new BlockPos(p_286190_.getNormal()));
         return AllOfCondition.allOf(lootitemblockstatepropertycondition$builder, lootitemcondition$builder);
      }).toArray((p_286191_) -> {
         return new LootItemCondition.Builder[p_286191_];
      });
      return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AnyOfCondition.anyOf(alootitemcondition$builder));
   }

   private static Advancement.Builder smithingWithStyle(Advancement.Builder pBuilder) {
      pBuilder.requirements(RequirementsStrategy.AND);
      Map<Item, ResourceLocation> map = VanillaRecipeProvider.smithingTrims();
      Stream.of(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE).forEach((p_284946_) -> {
         ResourceLocation resourcelocation = map.get(p_284946_);
         pBuilder.addCriterion("armor_trimmed_" + resourcelocation, RecipeCraftedTrigger.TriggerInstance.craftedItem(resourcelocation));
      });
      return pBuilder;
   }

   private static Advancement.Builder craftingANewLook(Advancement.Builder pBuilder) {
      pBuilder.requirements(RequirementsStrategy.OR);

      for(ResourceLocation resourcelocation : VanillaRecipeProvider.smithingTrims().values()) {
         pBuilder.addCriterion("armor_trimmed_" + resourcelocation, RecipeCraftedTrigger.TriggerInstance.craftedItem(resourcelocation));
      }

      return pBuilder;
   }

   private static Advancement.Builder respectingTheRemnantsCriterions(Advancement.Builder pBuilder) {
      pBuilder.addCriterion("desert_pyramid", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY));
      pBuilder.addCriterion("desert_well", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY));
      pBuilder.addCriterion("ocean_ruin_cold", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY));
      pBuilder.addCriterion("ocean_ruin_warm", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY));
      pBuilder.addCriterion("trail_ruins_rare", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE));
      pBuilder.addCriterion("trail_ruins_common", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON));
      String[] astring = pBuilder.getCriteria().keySet().toArray((p_285038_) -> {
         return new String[p_285038_];
      });
      String s = "has_sherd";
      pBuilder.addCriterion("has_sherd", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS).build()));
      pBuilder.requirements(new String[][]{astring, {"has_sherd"}});
      return pBuilder;
   }

   protected static void createAdventuringTime(Consumer<Advancement> pConsumer, Advancement pAdvancement, MultiNoiseBiomeSourceParameterList.Preset pPreset) {
      addBiomes(Advancement.Builder.advancement(), pPreset.usedBiomes().toList()).parent(pAdvancement).display(Items.DIAMOND_BOOTS, Component.translatable("advancements.adventure.adventuring_time.title"), Component.translatable("advancements.adventure.adventuring_time.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(500)).save(pConsumer, "adventure/adventuring_time");
   }

   /**
    * Adds all the entities in {@link #MOBS_TO_KILL} to the given advancement's criteria
    */
   private static Advancement.Builder addMobsToKill(Advancement.Builder pBuilder) {
      for(EntityType<?> entitytype : MOBS_TO_KILL) {
         pBuilder.addCriterion(BuiltInRegistries.ENTITY_TYPE.getKey(entitytype).toString(), KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entitytype)));
      }

      return pBuilder;
   }

   protected static Advancement.Builder addBiomes(Advancement.Builder pBuilder, List<ResourceKey<Biome>> pBiomes) {
      for(ResourceKey<Biome> resourcekey : pBiomes) {
         pBuilder.addCriterion(resourcekey.location().toString(), PlayerTrigger.TriggerInstance.located(LocationPredicate.inBiome(resourcekey)));
      }

      return pBuilder;
   }
}