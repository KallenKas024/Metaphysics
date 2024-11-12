package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public class EntityPaintingMotiveFix extends NamedEntityFix {
   private static final Map<String, String> MAP = DataFixUtils.make(Maps.newHashMap(), (p_15532_) -> {
      p_15532_.put("donkeykong", "donkey_kong");
      p_15532_.put("burningskull", "burning_skull");
      p_15532_.put("skullandroses", "skull_and_roses");
   });

   public EntityPaintingMotiveFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "EntityPaintingMotiveFix", References.ENTITY, "minecraft:painting");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15530_) {
      Optional<String> optional = p_15530_.get("Motive").asString().result();
      if (optional.isPresent()) {
         String s = optional.get().toLowerCase(Locale.ROOT);
         return p_15530_.set("Motive", p_15530_.createString((new ResourceLocation(MAP.getOrDefault(s, s))).toString()));
      } else {
         return p_15530_;
      }
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}