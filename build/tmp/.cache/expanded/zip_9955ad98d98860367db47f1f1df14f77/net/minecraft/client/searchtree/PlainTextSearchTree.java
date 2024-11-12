package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PlainTextSearchTree<T> {
   static <T> PlainTextSearchTree<T> empty() {
      return (p_235196_) -> {
         return List.of();
      };
   }

   static <T> PlainTextSearchTree<T> create(List<T> pContents, Function<T, Stream<String>> pIdGetter) {
      if (pContents.isEmpty()) {
         return empty();
      } else {
         SuffixArray<T> suffixarray = new SuffixArray<>();

         for(T t : pContents) {
            pIdGetter.apply(t).forEach((p_235194_) -> {
               suffixarray.add(t, p_235194_.toLowerCase(Locale.ROOT));
            });
         }

         suffixarray.generate();
         return suffixarray::search;
      }
   }

   List<T> search(String pQuery);
}