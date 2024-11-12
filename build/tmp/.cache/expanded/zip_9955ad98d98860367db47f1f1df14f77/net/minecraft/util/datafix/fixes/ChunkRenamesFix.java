package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;

public class ChunkRenamesFix extends DataFix {
   public ChunkRenamesFix(Schema pOutputSchema) {
      super(pOutputSchema, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> opticfinder = type.findField("Level");
      OpticFinder<?> opticfinder1 = opticfinder.type().findField("Structures");
      Type<?> type1 = this.getOutputSchema().getType(References.CHUNK);
      Type<?> type2 = type1.findFieldType("structures");
      return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", type, type1, (p_199427_) -> {
         Typed<?> typed = p_199427_.getTyped(opticfinder);
         Typed<?> typed1 = appendChunkName(typed);
         typed1 = typed1.set(DSL.remainderFinder(), mergeRemainders(p_199427_, typed.get(DSL.remainderFinder())));
         typed1 = renameField(typed1, "TileEntities", "block_entities");
         typed1 = renameField(typed1, "TileTicks", "block_ticks");
         typed1 = renameField(typed1, "Entities", "entities");
         typed1 = renameField(typed1, "Sections", "sections");
         typed1 = typed1.updateTyped(opticfinder1, type2, (p_185128_) -> {
            return renameField(p_185128_, "Starts", "starts");
         });
         typed1 = renameField(typed1, "Structures", "structures");
         return typed1.update(DSL.remainderFinder(), (p_199429_) -> {
            return p_199429_.remove("Level");
         });
      });
   }

   private static Typed<?> renameField(Typed<?> pTyped, String pOldName, String pNewName) {
      return renameFieldHelper(pTyped, pOldName, pNewName, pTyped.getType().findFieldType(pOldName)).update(DSL.remainderFinder(), (p_199439_) -> {
         return p_199439_.remove(pOldName);
      });
   }

   private static <A> Typed<?> renameFieldHelper(Typed<?> pTyped, String pOldName, String pNewName, Type<A> pType) {
      Type<Either<A, Unit>> type = DSL.optional(DSL.field(pOldName, pType));
      Type<Either<A, Unit>> type1 = DSL.optional(DSL.field(pNewName, pType));
      return pTyped.update(type.finder(), type1, Function.identity());
   }

   private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> pTyped) {
      return new Typed<>(DSL.named("chunk", pTyped.getType()), pTyped.getOps(), Pair.of("chunk", pTyped.getValue()));
   }

   private static <T> Dynamic<T> mergeRemainders(Typed<?> pTyped, Dynamic<T> pDynamic) {
      DynamicOps<T> dynamicops = pDynamic.getOps();
      Dynamic<T> dynamic = pTyped.get(DSL.remainderFinder()).convert(dynamicops);
      DataResult<T> dataresult = dynamicops.getMap(pDynamic.getValue()).flatMap((p_199433_) -> {
         return dynamicops.mergeToMap(dynamic.getValue(), p_199433_);
      });
      return dataresult.result().map((p_199436_) -> {
         return new Dynamic<>(dynamicops, p_199436_);
      }).orElse(pDynamic);
   }
}