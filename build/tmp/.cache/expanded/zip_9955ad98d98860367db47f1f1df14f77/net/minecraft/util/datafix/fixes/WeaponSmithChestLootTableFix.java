package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class WeaponSmithChestLootTableFix extends NamedEntityFix {
   public WeaponSmithChestLootTableFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "WeaponSmithChestLootTableFix", References.BLOCK_ENTITY, "minecraft:chest");
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), (p_203116_) -> {
         String s = p_203116_.get("LootTable").asString("");
         return s.equals("minecraft:chests/village_blacksmith") ? p_203116_.set("LootTable", p_203116_.createString("minecraft:chests/village/village_weaponsmith")) : p_203116_;
      });
   }
}