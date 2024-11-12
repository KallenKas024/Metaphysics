package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BlockPatternBuilder {
   private static final Joiner COMMA_JOINED = Joiner.on(",");
   private final List<String[]> pattern = Lists.newArrayList();
   private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.newHashMap();
   private int height;
   private int width;

   private BlockPatternBuilder() {
      this.lookup.put(' ', (p_187549_) -> {
         return true;
      });
   }

   /**
    * Adds a single aisle to this pattern, going in the z axis. (so multiple calls to this will increase the z-size by
    * 1)
    */
   public BlockPatternBuilder aisle(String... pAisle) {
      if (!ArrayUtils.isEmpty((Object[])pAisle) && !StringUtils.isEmpty(pAisle[0])) {
         if (this.pattern.isEmpty()) {
            this.height = pAisle.length;
            this.width = pAisle[0].length();
         }

         if (pAisle.length != this.height) {
            throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + pAisle.length + ")");
         } else {
            for(String s : pAisle) {
               if (s.length() != this.width) {
                  throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + s.length() + ")");
               }

               for(char c0 : s.toCharArray()) {
                  if (!this.lookup.containsKey(c0)) {
                     this.lookup.put(c0, (Predicate<BlockInWorld>)null);
                  }
               }
            }

            this.pattern.add(pAisle);
            return this;
         }
      } else {
         throw new IllegalArgumentException("Empty pattern for aisle");
      }
   }

   public static BlockPatternBuilder start() {
      return new BlockPatternBuilder();
   }

   public BlockPatternBuilder where(char pSymbol, Predicate<BlockInWorld> pBlockMatcher) {
      this.lookup.put(pSymbol, pBlockMatcher);
      return this;
   }

   public BlockPattern build() {
      return new BlockPattern(this.createPattern());
   }

   private Predicate<BlockInWorld>[][][] createPattern() {
      this.ensureAllCharactersMatched();
      Predicate<BlockInWorld>[][][] predicate = (Predicate[][][])Array.newInstance(Predicate.class, this.pattern.size(), this.height, this.width);

      for(int i = 0; i < this.pattern.size(); ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.width; ++k) {
               predicate[i][j][k] = this.lookup.get((this.pattern.get(i))[j].charAt(k));
            }
         }
      }

      return predicate;
   }

   private void ensureAllCharactersMatched() {
      List<Character> list = Lists.newArrayList();

      for(Map.Entry<Character, Predicate<BlockInWorld>> entry : this.lookup.entrySet()) {
         if (entry.getValue() == null) {
            list.add(entry.getKey());
         }
      }

      if (!list.isEmpty()) {
         throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINED.join(list) + " are missing");
      }
   }
}