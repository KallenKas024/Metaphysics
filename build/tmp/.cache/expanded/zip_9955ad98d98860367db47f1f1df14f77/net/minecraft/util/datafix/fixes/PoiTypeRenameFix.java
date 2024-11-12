package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import java.util.stream.Stream;

public class PoiTypeRenameFix extends AbstractPoiSectionFix {
   private final Function<String, String> renamer;

   public PoiTypeRenameFix(Schema pSchema, String pName, Function<String, String> pRenamer) {
      super(pSchema, pName);
      this.renamer = pRenamer;
   }

   protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> pRecords) {
      return pRecords.map((p_216714_) -> {
         return p_216714_.update("type", (p_216718_) -> {
            return DataFixUtils.orElse(p_216718_.asString().map(this.renamer).map(p_216718_::createString).result(), p_216718_);
         });
      });
   }
}