package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Arrays;
import java.util.function.Function;

public class EntityProjectileOwnerFix extends DataFix {
   public EntityProjectileOwnerFix(Schema pOutputSchema) {
      super(pOutputSchema, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      return this.fixTypeEverywhereTyped("EntityProjectileOwner", schema.getType(References.ENTITY), this::updateProjectiles);
   }

   private Typed<?> updateProjectiles(Typed<?> p_15563_) {
      p_15563_ = this.updateEntity(p_15563_, "minecraft:egg", this::updateOwnerThrowable);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:ender_pearl", this::updateOwnerThrowable);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:experience_bottle", this::updateOwnerThrowable);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:snowball", this::updateOwnerThrowable);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:potion", this::updateOwnerThrowable);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:potion", this::updateItemPotion);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:llama_spit", this::updateOwnerLlamaSpit);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:arrow", this::updateOwnerArrow);
      p_15563_ = this.updateEntity(p_15563_, "minecraft:spectral_arrow", this::updateOwnerArrow);
      return this.updateEntity(p_15563_, "minecraft:trident", this::updateOwnerArrow);
   }

   private Dynamic<?> updateOwnerArrow(Dynamic<?> p_15569_) {
      long i = p_15569_.get("OwnerUUIDMost").asLong(0L);
      long j = p_15569_.get("OwnerUUIDLeast").asLong(0L);
      return this.setUUID(p_15569_, i, j).remove("OwnerUUIDMost").remove("OwnerUUIDLeast");
   }

   private Dynamic<?> updateOwnerLlamaSpit(Dynamic<?> p_15578_) {
      OptionalDynamic<?> optionaldynamic = p_15578_.get("Owner");
      long i = optionaldynamic.get("OwnerUUIDMost").asLong(0L);
      long j = optionaldynamic.get("OwnerUUIDLeast").asLong(0L);
      return this.setUUID(p_15578_, i, j).remove("Owner");
   }

   private Dynamic<?> updateItemPotion(Dynamic<?> p_15580_) {
      OptionalDynamic<?> optionaldynamic = p_15580_.get("Potion");
      return p_15580_.set("Item", optionaldynamic.orElseEmptyMap()).remove("Potion");
   }

   private Dynamic<?> updateOwnerThrowable(Dynamic<?> p_15582_) {
      String s = "owner";
      OptionalDynamic<?> optionaldynamic = p_15582_.get("owner");
      long i = optionaldynamic.get("M").asLong(0L);
      long j = optionaldynamic.get("L").asLong(0L);
      return this.setUUID(p_15582_, i, j).remove("owner");
   }

   private Dynamic<?> setUUID(Dynamic<?> pDynamic, long pUuidMost, long pUuidLeast) {
      String s = "OwnerUUID";
      return pUuidMost != 0L && pUuidLeast != 0L ? pDynamic.set("OwnerUUID", pDynamic.createIntList(Arrays.stream(createUUIDArray(pUuidMost, pUuidLeast)))) : pDynamic;
   }

   private static int[] createUUIDArray(long pUuidMost, long pUuidLeast) {
      return new int[]{(int)(pUuidMost >> 32), (int)pUuidMost, (int)(pUuidLeast >> 32), (int)pUuidLeast};
   }

   private Typed<?> updateEntity(Typed<?> pTyped, String pChoiceName, Function<Dynamic<?>, Dynamic<?>> pUpdater) {
      Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, pChoiceName);
      Type<?> type1 = this.getOutputSchema().getChoiceType(References.ENTITY, pChoiceName);
      return pTyped.updateTyped(DSL.namedChoice(pChoiceName, type), type1, (p_15576_) -> {
         return p_15576_.update(DSL.remainderFinder(), pUpdater);
      });
   }
}