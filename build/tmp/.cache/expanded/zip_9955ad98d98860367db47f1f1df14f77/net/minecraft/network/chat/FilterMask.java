package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;

public class FilterMask {
   public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(FilterMask.Type::values).dispatch(FilterMask::type, FilterMask.Type::codec);
   public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.Type.FULLY_FILTERED);
   public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.Type.PASS_THROUGH);
   public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.filtered")));
   static final Codec<FilterMask> PASS_THROUGH_CODEC = Codec.unit(PASS_THROUGH);
   static final Codec<FilterMask> FULLY_FILTERED_CODEC = Codec.unit(FULLY_FILTERED);
   static final Codec<FilterMask> PARTIALLY_FILTERED_CODEC = ExtraCodecs.BIT_SET.xmap(FilterMask::new, FilterMask::mask);
   private static final char HASH = '#';
   private final BitSet mask;
   private final FilterMask.Type type;

   private FilterMask(BitSet pMask, FilterMask.Type pType) {
      this.mask = pMask;
      this.type = pType;
   }

   private FilterMask(BitSet p_253780_) {
      this.mask = p_253780_;
      this.type = FilterMask.Type.PARTIALLY_FILTERED;
   }

   public FilterMask(int pSize) {
      this(new BitSet(pSize), FilterMask.Type.PARTIALLY_FILTERED);
   }

   private FilterMask.Type type() {
      return this.type;
   }

   private BitSet mask() {
      return this.mask;
   }

   public static FilterMask read(FriendlyByteBuf pBuffer) {
      FilterMask.Type filtermask$type = pBuffer.readEnum(FilterMask.Type.class);
      FilterMask filtermask;
      switch (filtermask$type) {
         case PASS_THROUGH:
            filtermask = PASS_THROUGH;
            break;
         case FULLY_FILTERED:
            filtermask = FULLY_FILTERED;
            break;
         case PARTIALLY_FILTERED:
            filtermask = new FilterMask(pBuffer.readBitSet(), FilterMask.Type.PARTIALLY_FILTERED);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return filtermask;
   }

   public static void write(FriendlyByteBuf pBuffer, FilterMask pMask) {
      pBuffer.writeEnum(pMask.type);
      if (pMask.type == FilterMask.Type.PARTIALLY_FILTERED) {
         pBuffer.writeBitSet(pMask.mask);
      }

   }

   public void setFiltered(int pBitIndex) {
      this.mask.set(pBitIndex);
   }

   @Nullable
   public String apply(String pText) {
      String s;
      switch (this.type) {
         case PASS_THROUGH:
            s = pText;
            break;
         case FULLY_FILTERED:
            s = null;
            break;
         case PARTIALLY_FILTERED:
            char[] achar = pText.toCharArray();

            for(int i = 0; i < achar.length && i < this.mask.length(); ++i) {
               if (this.mask.get(i)) {
                  achar[i] = '#';
               }
            }

            s = new String(achar);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return s;
   }

   @Nullable
   public Component applyWithFormatting(String pText) {
      MutableComponent mutablecomponent1;
      switch (this.type) {
         case PASS_THROUGH:
            mutablecomponent1 = Component.literal(pText);
            break;
         case FULLY_FILTERED:
            mutablecomponent1 = null;
            break;
         case PARTIALLY_FILTERED:
            MutableComponent mutablecomponent = Component.empty();
            int i = 0;
            boolean flag = this.mask.get(0);

            while(true) {
               int j = flag ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
               j = j < 0 ? pText.length() : j;
               if (j == i) {
                  return mutablecomponent;
               }

               if (flag) {
                  mutablecomponent.append(Component.literal(StringUtils.repeat('#', j - i)).withStyle(FILTERED_STYLE));
               } else {
                  mutablecomponent.append(pText.substring(i, j));
               }

               flag = !flag;
               i = j;
            }
         default:
            throw new IncompatibleClassChangeError();
      }

      return mutablecomponent1;
   }

   public boolean isEmpty() {
      return this.type == FilterMask.Type.PASS_THROUGH;
   }

   public boolean isFullyFiltered() {
      return this.type == FilterMask.Type.FULLY_FILTERED;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         FilterMask filtermask = (FilterMask)pOther;
         return this.mask.equals(filtermask.mask) && this.type == filtermask.type;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.mask.hashCode();
      return 31 * i + this.type.hashCode();
   }

   static enum Type implements StringRepresentable {
      PASS_THROUGH("pass_through", () -> {
         return FilterMask.PASS_THROUGH_CODEC;
      }),
      FULLY_FILTERED("fully_filtered", () -> {
         return FilterMask.FULLY_FILTERED_CODEC;
      }),
      PARTIALLY_FILTERED("partially_filtered", () -> {
         return FilterMask.PARTIALLY_FILTERED_CODEC;
      });

      private final String serializedName;
      private final Supplier<Codec<FilterMask>> codec;

      private Type(String pSerializedName, Supplier<Codec<FilterMask>> pCodec) {
         this.serializedName = pSerializedName;
         this.codec = pCodec;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      private Codec<FilterMask> codec() {
         return this.codec.get();
      }
   }
}