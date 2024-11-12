package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V100 extends Schema {
   public V100(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   protected static TypeTemplate equipment(Schema pSchema) {
      return DSL.optionalFields("ArmorItems", DSL.list(References.ITEM_STACK.in(pSchema)), "HandItems", DSL.list(References.ITEM_STACK.in(pSchema)));
   }

   protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return equipment(pSchema);
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      registerMob(pSchema, map, "ArmorStand");
      registerMob(pSchema, map, "Creeper");
      registerMob(pSchema, map, "Skeleton");
      registerMob(pSchema, map, "Spider");
      registerMob(pSchema, map, "Giant");
      registerMob(pSchema, map, "Zombie");
      registerMob(pSchema, map, "Slime");
      registerMob(pSchema, map, "Ghast");
      registerMob(pSchema, map, "PigZombie");
      pSchema.register(map, "Enderman", (p_17348_) -> {
         return DSL.optionalFields("carried", References.BLOCK_NAME.in(pSchema), equipment(pSchema));
      });
      registerMob(pSchema, map, "CaveSpider");
      registerMob(pSchema, map, "Silverfish");
      registerMob(pSchema, map, "Blaze");
      registerMob(pSchema, map, "LavaSlime");
      registerMob(pSchema, map, "EnderDragon");
      registerMob(pSchema, map, "WitherBoss");
      registerMob(pSchema, map, "Bat");
      registerMob(pSchema, map, "Witch");
      registerMob(pSchema, map, "Endermite");
      registerMob(pSchema, map, "Guardian");
      registerMob(pSchema, map, "Pig");
      registerMob(pSchema, map, "Sheep");
      registerMob(pSchema, map, "Cow");
      registerMob(pSchema, map, "Chicken");
      registerMob(pSchema, map, "Squid");
      registerMob(pSchema, map, "Wolf");
      registerMob(pSchema, map, "MushroomCow");
      registerMob(pSchema, map, "SnowMan");
      registerMob(pSchema, map, "Ozelot");
      registerMob(pSchema, map, "VillagerGolem");
      pSchema.register(map, "EntityHorse", (p_17343_) -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "ArmorItem", References.ITEM_STACK.in(pSchema), "SaddleItem", References.ITEM_STACK.in(pSchema), equipment(pSchema));
      });
      registerMob(pSchema, map, "Rabbit");
      pSchema.register(map, "Villager", (p_17334_) -> {
         return DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(pSchema), "buyB", References.ITEM_STACK.in(pSchema), "sell", References.ITEM_STACK.in(pSchema)))), equipment(pSchema));
      });
      registerMob(pSchema, map, "Shulker");
      pSchema.registerSimple(map, "AreaEffectCloud");
      pSchema.registerSimple(map, "ShulkerBullet");
      return map;
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      super.registerTypes(pSchema, pEntityTypes, pBlockEntityTypes);
      pSchema.registerType(false, References.STRUCTURE, () -> {
         return DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(pSchema))), "blocks", DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(pSchema))), "palette", DSL.list(References.BLOCK_STATE.in(pSchema)));
      });
      pSchema.registerType(false, References.BLOCK_STATE, DSL::remainder);
   }
}