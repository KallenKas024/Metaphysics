package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EntityBrushableBlockFieldsRenameFix extends NamedEntityFix {
   public EntityBrushableBlockFieldsRenameFix(Schema pOutputSchema) {
      super(pOutputSchema, false, "EntityBrushableBlockFieldsRenameFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_277830_) {
      return this.renameField(this.renameField(p_277830_, "loot_table", "LootTable"), "loot_table_seed", "LootTableSeed");
   }

   private Dynamic<?> renameField(Dynamic<?> pTag, String pOldName, String pNewName) {
      Optional<? extends Dynamic<?>> optional = pTag.get(pOldName).result();
      Optional<? extends Dynamic<?>> optional1 = optional.map((p_277534_) -> {
         return pTag.remove(pOldName).set(pNewName, p_277534_);
      });
      return DataFixUtils.orElse(optional1, pTag);
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}