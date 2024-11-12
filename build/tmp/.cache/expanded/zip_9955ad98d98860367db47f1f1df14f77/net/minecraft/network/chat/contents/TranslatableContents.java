package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

public class TranslatableContents implements ComponentContents {
   public static final Object[] NO_ARGS = new Object[0];
   private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
   private static final FormattedText TEXT_NULL = FormattedText.of("null");
   private final String key;
   @Nullable
   private final String fallback;
   private final Object[] args;
   @Nullable
   private Language decomposedWith;
   /**
    * The discrete elements that make up this component. For example, this would be ["Prefix, ", "FirstArg",
    * "SecondArg", " again ", "SecondArg", " and ", "FirstArg", " lastly ", "ThirdArg", " and also ", "FirstArg", "
    * again!"] for "translation.test.complex" (see en_us.json)
    */
   private List<FormattedText> decomposedParts = ImmutableList.of();
   private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TranslatableContents(String pKey, @Nullable String pFallback, Object[] pArgs) {
      this.key = pKey;
      this.fallback = pFallback;
      this.args = pArgs;
   }

   /**
    * Ensures that all the children are up to date with the most recent translation mapping.
    */
   private void decompose() {
      Language language = Language.getInstance();
      if (language != this.decomposedWith) {
         this.decomposedWith = language;
         String s = this.fallback != null ? language.getOrDefault(this.key, this.fallback) : language.getOrDefault(this.key);

         try {
            ImmutableList.Builder<FormattedText> builder = ImmutableList.builder();
            this.decomposeTemplate(s, builder::add);
            this.decomposedParts = builder.build();
         } catch (TranslatableFormatException translatableformatexception) {
            this.decomposedParts = ImmutableList.of(FormattedText.of(s));
         }

      }
   }

   private void decomposeTemplate(String pFormatTemplate, Consumer<FormattedText> pConsumer) {
      Matcher matcher = FORMAT_PATTERN.matcher(pFormatTemplate);

      try {
         int i = 0;

         int j;
         int l;
         for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            if (k > j) {
               String s = pFormatTemplate.substring(j, k);
               if (s.indexOf(37) != -1) {
                  throw new IllegalArgumentException();
               }

               pConsumer.accept(FormattedText.of(s));
            }

            String s4 = matcher.group(2);
            String s1 = pFormatTemplate.substring(k, l);
            if ("%".equals(s4) && "%%".equals(s1)) {
               pConsumer.accept(TEXT_PERCENT);
            } else {
               if (!"s".equals(s4)) {
                  throw new TranslatableFormatException(this, "Unsupported format: '" + s1 + "'");
               }

               String s2 = matcher.group(1);
               int i1 = s2 != null ? Integer.parseInt(s2) - 1 : i++;
               pConsumer.accept(this.getArgument(i1));
            }
         }

         if (j == 0) {
            // Forge has some special formatting handlers defined in ForgeI18n, use those if no %s replacements present.
            j = net.minecraftforge.internal.TextComponentMessageFormatHandler.handle(this, pConsumer, this.args, pFormatTemplate);
         }

         if (j < pFormatTemplate.length()) {
            String s3 = pFormatTemplate.substring(j);
            if (s3.indexOf(37) != -1) {
               throw new IllegalArgumentException();
            }

            pConsumer.accept(FormattedText.of(s3));
         }

      } catch (IllegalArgumentException illegalargumentexception) {
         throw new TranslatableFormatException(this, illegalargumentexception);
      }
   }

   private FormattedText getArgument(int pIndex) {
      if (pIndex >= 0 && pIndex < this.args.length) {
         Object object = this.args[pIndex];
         if (object instanceof Component) {
            return (Component)object;
         } else {
            return object == null ? TEXT_NULL : FormattedText.of(object.toString());
         }
      } else {
         throw new TranslatableFormatException(this, pIndex);
      }
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pStyledContentConsumer, Style pStyle) {
      this.decompose();

      for(FormattedText formattedtext : this.decomposedParts) {
         Optional<T> optional = formattedtext.visit(pStyledContentConsumer, pStyle);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }

   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pContentConsumer) {
      this.decompose();

      for(FormattedText formattedtext : this.decomposedParts) {
         Optional<T> optional = formattedtext.visit(pContentConsumer);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }

   public MutableComponent resolve(@Nullable CommandSourceStack pNbtPathPattern, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      Object[] aobject = new Object[this.args.length];

      for(int i = 0; i < aobject.length; ++i) {
         Object object = this.args[i];
         if (object instanceof Component) {
            aobject[i] = ComponentUtils.updateForEntity(pNbtPathPattern, (Component)object, pEntity, pRecursionDepth);
         } else {
            aobject[i] = object;
         }
      }

      return MutableComponent.create(new TranslatableContents(this.key, this.fallback, aobject));
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof TranslatableContents) {
            TranslatableContents translatablecontents = (TranslatableContents)pOther;
            if (Objects.equals(this.key, translatablecontents.key) && Objects.equals(this.fallback, translatablecontents.fallback) && Arrays.equals(this.args, translatablecontents.args)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = Objects.hashCode(this.key);
      i = 31 * i + Objects.hashCode(this.fallback);
      return 31 * i + Arrays.hashCode(this.args);
   }

   public String toString() {
      return "translation{key='" + this.key + "'" + (this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
   }

   public String getKey() {
      return this.key;
   }

   @Nullable
   public String getFallback() {
      return this.fallback;
   }

   public Object[] getArgs() {
      return this.args;
   }
}
