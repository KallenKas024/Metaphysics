package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChunkToProtochunkFix extends DataFix {
   private static final int NUM_SECTIONS = 16;

   public ChunkToProtochunkFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
   }

   public TypeRewriteRule makeRule() {
      return this.writeFixAndRead("ChunkToProtoChunkFix", this.getInputSchema().getType(References.CHUNK), this.getOutputSchema().getType(References.CHUNK), (p_199886_) -> {
         return p_199886_.update("Level", ChunkToProtochunkFix::fixChunkData);
      });
   }

   private static <T> Dynamic<T> fixChunkData(Dynamic<T> p_199856_) {
      boolean flag = p_199856_.get("TerrainPopulated").asBoolean(false);
      boolean flag1 = p_199856_.get("LightPopulated").asNumber().result().isEmpty() || p_199856_.get("LightPopulated").asBoolean(false);
      String s;
      if (flag) {
         if (flag1) {
            s = "mobs_spawned";
         } else {
            s = "decorated";
         }
      } else {
         s = "carved";
      }

      return repackTicks(repackBiomes(p_199856_)).set("Status", p_199856_.createString(s)).set("hasLegacyStructureData", p_199856_.createBoolean(true));
   }

   private static <T> Dynamic<T> repackBiomes(Dynamic<T> pDynamic) {
      return pDynamic.update("Biomes", (p_199862_) -> {
         return DataFixUtils.orElse(p_199862_.asByteBufferOpt().result().map((p_199868_) -> {
            int[] aint = new int[256];

            for(int i = 0; i < aint.length; ++i) {
               if (i < p_199868_.capacity()) {
                  aint[i] = p_199868_.get(i) & 255;
               }
            }

            return pDynamic.createIntList(Arrays.stream(aint));
         }), p_199862_);
      });
   }

   private static <T> Dynamic<T> repackTicks(Dynamic<T> pDynamic) {
      return DataFixUtils.orElse(pDynamic.get("TileTicks").asStreamOpt().result().map((p_199871_) -> {
         List<ShortList> list = IntStream.range(0, 16).mapToObj((p_199850_) -> {
            return new ShortArrayList();
         }).collect(Collectors.toList());
         p_199871_.forEach((p_199874_) -> {
            int i = p_199874_.get("x").asInt(0);
            int j = p_199874_.get("y").asInt(0);
            int k = p_199874_.get("z").asInt(0);
            short short1 = packOffsetCoordinates(i, j, k);
            list.get(j >> 4).add(short1);
         });
         return pDynamic.remove("TileTicks").set("ToBeTicked", pDynamic.createList(list.stream().map((p_199865_) -> {
            return pDynamic.createList(p_199865_.intStream().mapToObj((p_199859_) -> {
               return pDynamic.createShort((short)p_199859_);
            }));
         })));
      }), pDynamic);
   }

   private static short packOffsetCoordinates(int pX, int pY, int pZ) {
      return (short)(pX & 15 | (pY & 15) << 4 | (pZ & 15) << 8);
   }
}