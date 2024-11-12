package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1460 extends NamespacedSchema {
   public V1460(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return V100.equipment(pSchema);
      });
   }

   protected static void registerInventory(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)));
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
      pSchema.registerSimple(map, "minecraft:area_effect_cloud");
      registerMob(pSchema, map, "minecraft:armor_stand");
      pSchema.register(map, "minecraft:arrow", (p_17683_) -> {
         return DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:bat");
      registerMob(pSchema, map, "minecraft:blaze");
      pSchema.registerSimple(map, "minecraft:boat");
      registerMob(pSchema, map, "minecraft:cave_spider");
      pSchema.register(map, "minecraft:chest_minecart", (p_17680_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema), "Items", DSL.list(References.ITEM_STACK.in(pSchema)));
      });
      registerMob(pSchema, map, "minecraft:chicken");
      pSchema.register(map, "minecraft:commandblock_minecart", (p_17677_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:cow");
      registerMob(pSchema, map, "minecraft:creeper");
      pSchema.register(map, "minecraft:donkey", (p_17674_) -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      pSchema.registerSimple(map, "minecraft:dragon_fireball");
      pSchema.registerSimple(map, "minecraft:egg");
      registerMob(pSchema, map, "minecraft:elder_guardian");
      pSchema.registerSimple(map, "minecraft:ender_crystal");
      registerMob(pSchema, map, "minecraft:ender_dragon");
      pSchema.register(map, "minecraft:enderman", (p_17671_) -> {
         return DSL.optionalFields("carriedBlockState", References.BLOCK_STATE.in(pSchema), V100.equipment(pSchema));
      });
      registerMob(pSchema, map, "minecraft:endermite");
      pSchema.registerSimple(map, "minecraft:ender_pearl");
      pSchema.registerSimple(map, "minecraft:evocation_fangs");
      registerMob(pSchema, map, "minecraft:evocation_illager");
      pSchema.registerSimple(map, "minecraft:eye_of_ender_signal");
      pSchema.register(map, "minecraft:falling_block", (p_17668_) -> {
         return DSL.optionalFields("BlockState", References.BLOCK_STATE.in(pSchema), "TileEntityData", References.BLOCK_ENTITY.in(pSchema));
      });
      pSchema.registerSimple(map, "minecraft:fireball");
      pSchema.register(map, "minecraft:fireworks_rocket", (p_17665_) -> {
         return DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(pSchema));
      });
      pSchema.register(map, "minecraft:furnace_minecart", (p_17654_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:ghast");
      registerMob(pSchema, map, "minecraft:giant");
      registerMob(pSchema, map, "minecraft:guardian");
      pSchema.register(map, "minecraft:hopper_minecart", (p_17651_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema), "Items", DSL.list(References.ITEM_STACK.in(pSchema)));
      });
      pSchema.register(map, "minecraft:horse", (p_17648_) -> {
         return DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(pSchema), "SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      registerMob(pSchema, map, "minecraft:husk");
      pSchema.registerSimple(map, "minecraft:illusion_illager");
      pSchema.register(map, "minecraft:item", (p_17645_) -> {
         return DSL.optionalFields("Item", References.ITEM_STACK.in(pSchema));
      });
      pSchema.register(map, "minecraft:item_frame", (p_17642_) -> {
         return DSL.optionalFields("Item", References.ITEM_STACK.in(pSchema));
      });
      pSchema.registerSimple(map, "minecraft:leash_knot");
      pSchema.register(map, "minecraft:llama", (p_17639_) -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "SaddleItem", References.ITEM_STACK.in(pSchema), "DecorItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      pSchema.registerSimple(map, "minecraft:llama_spit");
      registerMob(pSchema, map, "minecraft:magma_cube");
      pSchema.register(map, "minecraft:minecart", (p_17634_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:mooshroom");
      pSchema.register(map, "minecraft:mule", (p_17629_) -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      registerMob(pSchema, map, "minecraft:ocelot");
      pSchema.registerSimple(map, "minecraft:painting");
      pSchema.registerSimple(map, "minecraft:parrot");
      registerMob(pSchema, map, "minecraft:pig");
      registerMob(pSchema, map, "minecraft:polar_bear");
      pSchema.register(map, "minecraft:potion", (p_17624_) -> {
         return DSL.optionalFields("Potion", References.ITEM_STACK.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:rabbit");
      registerMob(pSchema, map, "minecraft:sheep");
      registerMob(pSchema, map, "minecraft:shulker");
      pSchema.registerSimple(map, "minecraft:shulker_bullet");
      registerMob(pSchema, map, "minecraft:silverfish");
      registerMob(pSchema, map, "minecraft:skeleton");
      pSchema.register(map, "minecraft:skeleton_horse", (p_17619_) -> {
         return DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      registerMob(pSchema, map, "minecraft:slime");
      pSchema.registerSimple(map, "minecraft:small_fireball");
      pSchema.registerSimple(map, "minecraft:snowball");
      registerMob(pSchema, map, "minecraft:snowman");
      pSchema.register(map, "minecraft:spawner_minecart", (p_17614_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema), References.UNTAGGED_SPAWNER.in(pSchema));
      });
      pSchema.register(map, "minecraft:spectral_arrow", (p_17609_) -> {
         return DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:spider");
      registerMob(pSchema, map, "minecraft:squid");
      registerMob(pSchema, map, "minecraft:stray");
      pSchema.registerSimple(map, "minecraft:tnt");
      pSchema.register(map, "minecraft:tnt_minecart", (p_17604_) -> {
         return DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(pSchema));
      });
      registerMob(pSchema, map, "minecraft:vex");
      pSchema.register(map, "minecraft:villager", (p_17598_) -> {
         return DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(pSchema), "buyB", References.ITEM_STACK.in(pSchema), "sell", References.ITEM_STACK.in(pSchema)))), V100.equipment(pSchema));
      });
      registerMob(pSchema, map, "minecraft:villager_golem");
      registerMob(pSchema, map, "minecraft:vindication_illager");
      registerMob(pSchema, map, "minecraft:witch");
      registerMob(pSchema, map, "minecraft:wither");
      registerMob(pSchema, map, "minecraft:wither_skeleton");
      pSchema.registerSimple(map, "minecraft:wither_skull");
      registerMob(pSchema, map, "minecraft:wolf");
      pSchema.registerSimple(map, "minecraft:xp_bottle");
      pSchema.registerSimple(map, "minecraft:xp_orb");
      registerMob(pSchema, map, "minecraft:zombie");
      pSchema.register(map, "minecraft:zombie_horse", (p_17592_) -> {
         return DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      registerMob(pSchema, map, "minecraft:zombie_pigman");
      registerMob(pSchema, map, "minecraft:zombie_villager");
      return map;
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
      registerInventory(pSchema, map, "minecraft:furnace");
      registerInventory(pSchema, map, "minecraft:chest");
      registerInventory(pSchema, map, "minecraft:trapped_chest");
      pSchema.registerSimple(map, "minecraft:ender_chest");
      pSchema.register(map, "minecraft:jukebox", (p_17586_) -> {
         return DSL.optionalFields("RecordItem", References.ITEM_STACK.in(pSchema));
      });
      registerInventory(pSchema, map, "minecraft:dispenser");
      registerInventory(pSchema, map, "minecraft:dropper");
      pSchema.registerSimple(map, "minecraft:sign");
      pSchema.register(map, "minecraft:mob_spawner", (p_17574_) -> {
         return References.UNTAGGED_SPAWNER.in(pSchema);
      });
      pSchema.register(map, "minecraft:piston", (p_17559_) -> {
         return DSL.optionalFields("blockState", References.BLOCK_STATE.in(pSchema));
      });
      registerInventory(pSchema, map, "minecraft:brewing_stand");
      pSchema.registerSimple(map, "minecraft:enchanting_table");
      pSchema.registerSimple(map, "minecraft:end_portal");
      pSchema.registerSimple(map, "minecraft:beacon");
      pSchema.registerSimple(map, "minecraft:skull");
      pSchema.registerSimple(map, "minecraft:daylight_detector");
      registerInventory(pSchema, map, "minecraft:hopper");
      pSchema.registerSimple(map, "minecraft:comparator");
      pSchema.registerSimple(map, "minecraft:banner");
      pSchema.registerSimple(map, "minecraft:structure_block");
      pSchema.registerSimple(map, "minecraft:end_gateway");
      pSchema.registerSimple(map, "minecraft:command_block");
      registerInventory(pSchema, map, "minecraft:shulker_box");
      pSchema.registerSimple(map, "minecraft:bed");
      return map;
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      pSchema.registerType(false, References.LEVEL, DSL::remainder);
      pSchema.registerType(false, References.RECIPE, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.PLAYER, () -> {
         return DSL.optionalFields("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(pSchema)), "Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "EnderItems", DSL.list(References.ITEM_STACK.in(pSchema)), DSL.optionalFields("ShoulderEntityLeft", References.ENTITY_TREE.in(pSchema), "ShoulderEntityRight", References.ENTITY_TREE.in(pSchema), "recipeBook", DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(pSchema)), "toBeDisplayed", DSL.list(References.RECIPE.in(pSchema)))));
      });
      pSchema.registerType(false, References.CHUNK, () -> {
         return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(pSchema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(pSchema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(pSchema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in(pSchema))))));
      });
      pSchema.registerType(true, References.BLOCK_ENTITY, () -> {
         return DSL.taggedChoiceLazy("id", namespacedString(), pBlockEntityTypes);
      });
      pSchema.registerType(true, References.ENTITY_TREE, () -> {
         return DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(pSchema)), References.ENTITY.in(pSchema));
      });
      pSchema.registerType(true, References.ENTITY, () -> {
         return DSL.taggedChoiceLazy("id", namespacedString(), pEntityTypes);
      });
      pSchema.registerType(true, References.ITEM_STACK, () -> {
         return DSL.hook(DSL.optionalFields("id", References.ITEM_NAME.in(pSchema), "tag", DSL.optionalFields("EntityTag", References.ENTITY_TREE.in(pSchema), "BlockEntityTag", References.BLOCK_ENTITY.in(pSchema), "CanDestroy", DSL.list(References.BLOCK_NAME.in(pSchema)), "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(pSchema)), "Items", DSL.list(References.ITEM_STACK.in(pSchema)))), V705.ADD_NAMES, HookFunction.IDENTITY);
      });
      pSchema.registerType(false, References.HOTBAR, () -> {
         return DSL.compoundList(DSL.list(References.ITEM_STACK.in(pSchema)));
      });
      pSchema.registerType(false, References.OPTIONS, DSL::remainder);
      pSchema.registerType(false, References.STRUCTURE, () -> {
         return DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(pSchema))), "blocks", DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(pSchema))), "palette", DSL.list(References.BLOCK_STATE.in(pSchema)));
      });
      pSchema.registerType(false, References.BLOCK_NAME, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.ITEM_NAME, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.BLOCK_STATE, DSL::remainder);
      Supplier<TypeTemplate> supplier = () -> {
         return DSL.compoundList(References.ITEM_NAME.in(pSchema), DSL.constType(DSL.intType()));
      };
      pSchema.registerType(false, References.STATS, () -> {
         return DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in(pSchema), DSL.constType(DSL.intType())), "minecraft:crafted", supplier.get(), "minecraft:used", supplier.get(), "minecraft:broken", supplier.get(), "minecraft:picked_up", supplier.get(), DSL.optionalFields("minecraft:dropped", supplier.get(), "minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType())))));
      });
      pSchema.registerType(false, References.SAVED_DATA, () -> {
         return DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(pSchema)), "Objectives", DSL.list(References.OBJECTIVE.in(pSchema)), "Teams", DSL.list(References.TEAM.in(pSchema))));
      });
      pSchema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
      Map<String, Supplier<TypeTemplate>> map = V1451_6.createCriterionTypes(pSchema);
      pSchema.registerType(false, References.OBJECTIVE, () -> {
         return DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map)), V1451_6.UNPACK_OBJECTIVE_ID, V1451_6.REPACK_OBJECTIVE_ID);
      });
      pSchema.registerType(false, References.TEAM, DSL::remainder);
      pSchema.registerType(true, References.UNTAGGED_SPAWNER, () -> {
         return DSL.optionalFields("SpawnPotentials", DSL.list(DSL.fields("Entity", References.ENTITY_TREE.in(pSchema))), "SpawnData", References.ENTITY_TREE.in(pSchema));
      });
      pSchema.registerType(false, References.ADVANCEMENTS, () -> {
         return DSL.optionalFields("minecraft:adventure/adventuring_time", DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in(pSchema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_a_mob", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_all_mobs", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.string()))), "minecraft:husbandry/bred_all_animals", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.string()))));
      });
      pSchema.registerType(false, References.BIOME, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.ENTITY_NAME, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.POI_CHUNK, DSL::remainder);
      pSchema.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
      pSchema.registerType(false, References.ENTITY_CHUNK, () -> {
         return DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(pSchema)));
      });
   }
}