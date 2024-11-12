package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

public interface FormattedText {
   Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
   FormattedText EMPTY = new FormattedText() {
      public <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130779_) {
         return Optional.empty();
      }

      public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130781_, Style p_130782_) {
         return Optional.empty();
      }
   };

   <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor);

   <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style pStyle);

   static FormattedText of(final String pText) {
      return new FormattedText() {
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130787_) {
            return p_130787_.accept(pText);
         }

         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130789_, Style p_130790_) {
            return p_130789_.accept(p_130790_, pText);
         }
      };
   }

   static FormattedText of(final String pText, final Style pStyle) {
      return new FormattedText() {
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130797_) {
            return p_130797_.accept(pText);
         }

         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130799_, Style p_130800_) {
            return p_130799_.accept(pStyle.applyTo(p_130800_), pText);
         }
      };
   }

   static FormattedText composite(FormattedText... pElements) {
      return composite(ImmutableList.copyOf(pElements));
   }

   static FormattedText composite(final List<? extends FormattedText> pElements) {
      return new FormattedText() {
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130805_) {
            for(FormattedText formattedtext : pElements) {
               Optional<T> optional = formattedtext.visit(p_130805_);
               if (optional.isPresent()) {
                  return optional;
               }
            }

            return Optional.empty();
         }

         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130807_, Style p_130808_) {
            for(FormattedText formattedtext : pElements) {
               Optional<T> optional = formattedtext.visit(p_130807_, p_130808_);
               if (optional.isPresent()) {
                  return optional;
               }
            }

            return Optional.empty();
         }
      };
   }

   default String getString() {
      StringBuilder stringbuilder = new StringBuilder();
      this.visit((p_130767_) -> {
         stringbuilder.append(p_130767_);
         return Optional.empty();
      });
      return stringbuilder.toString();
   }

   public interface ContentConsumer<T> {
      Optional<T> accept(String pContent);
   }

   public interface StyledContentConsumer<T> {
      Optional<T> accept(Style pStyle, String pContent);
   }
}