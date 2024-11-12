package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class GoatHornIdFix extends ItemStackTagFix {
   private static final String[] INSTRUMENTS = new String[]{"minecraft:ponder_goat_horn", "minecraft:sing_goat_horn", "minecraft:seek_goat_horn", "minecraft:feel_goat_horn", "minecraft:admire_goat_horn", "minecraft:call_goat_horn", "minecraft:yearn_goat_horn", "minecraft:dream_goat_horn"};

   public GoatHornIdFix(Schema pOutputSchema) {
      super(pOutputSchema, "GoatHornIdFix", (p_216678_) -> {
         return p_216678_.equals("minecraft:goat_horn");
      });
   }

   protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> pItemStackTag) {
      int i = pItemStackTag.get("SoundVariant").asInt(0);
      String s = INSTRUMENTS[i >= 0 && i < INSTRUMENTS.length ? i : 0];
      return pItemStackTag.remove("SoundVariant").set("instrument", pItemStackTag.createString(s));
   }
}