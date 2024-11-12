package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class EntityPaintingItemFrameDirectionFix extends DataFix {
   private static final int[][] DIRECTIONS = new int[][]{{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};

   public EntityPaintingItemFrameDirectionFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
   }

   private Dynamic<?> doFix(Dynamic<?> pDynamic, boolean p_15511_, boolean p_15512_) {
      if ((p_15511_ || p_15512_) && !pDynamic.get("Facing").asNumber().result().isPresent()) {
         int i;
         if (pDynamic.get("Direction").asNumber().result().isPresent()) {
            i = pDynamic.get("Direction").asByte((byte)0) % DIRECTIONS.length;
            int[] aint = DIRECTIONS[i];
            pDynamic = pDynamic.set("TileX", pDynamic.createInt(pDynamic.get("TileX").asInt(0) + aint[0]));
            pDynamic = pDynamic.set("TileY", pDynamic.createInt(pDynamic.get("TileY").asInt(0) + aint[1]));
            pDynamic = pDynamic.set("TileZ", pDynamic.createInt(pDynamic.get("TileZ").asInt(0) + aint[2]));
            pDynamic = pDynamic.remove("Direction");
            if (p_15512_ && pDynamic.get("ItemRotation").asNumber().result().isPresent()) {
               pDynamic = pDynamic.set("ItemRotation", pDynamic.createByte((byte)(pDynamic.get("ItemRotation").asByte((byte)0) * 2)));
            }
         } else {
            i = pDynamic.get("Dir").asByte((byte)0) % DIRECTIONS.length;
            pDynamic = pDynamic.remove("Dir");
         }

         pDynamic = pDynamic.set("Facing", pDynamic.createByte((byte)i));
      }

      return pDynamic;
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, "Painting");
      OpticFinder<?> opticfinder = DSL.namedChoice("Painting", type);
      Type<?> type1 = this.getInputSchema().getChoiceType(References.ENTITY, "ItemFrame");
      OpticFinder<?> opticfinder1 = DSL.namedChoice("ItemFrame", type1);
      Type<?> type2 = this.getInputSchema().getType(References.ENTITY);
      TypeRewriteRule typerewriterule = this.fixTypeEverywhereTyped("EntityPaintingFix", type2, (p_15516_) -> {
         return p_15516_.updateTyped(opticfinder, type, (p_145300_) -> {
            return p_145300_.update(DSL.remainderFinder(), (p_145302_) -> {
               return this.doFix(p_145302_, true, false);
            });
         });
      });
      TypeRewriteRule typerewriterule1 = this.fixTypeEverywhereTyped("EntityItemFrameFix", type2, (p_15504_) -> {
         return p_15504_.updateTyped(opticfinder1, type1, (p_145296_) -> {
            return p_145296_.update(DSL.remainderFinder(), (p_145298_) -> {
               return this.doFix(p_145298_, false, true);
            });
         });
      });
      return TypeRewriteRule.seq(typerewriterule, typerewriterule1);
   }
}