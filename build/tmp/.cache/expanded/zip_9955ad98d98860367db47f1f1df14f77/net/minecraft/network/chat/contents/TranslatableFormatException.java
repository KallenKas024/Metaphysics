package net.minecraft.network.chat.contents;

import java.util.Locale;

public class TranslatableFormatException extends IllegalArgumentException {
   public TranslatableFormatException(TranslatableContents pContents, String pError) {
      super(String.format(Locale.ROOT, "Error parsing: %s: %s", pContents, pError));
   }

   public TranslatableFormatException(TranslatableContents pComponent, int pInvalidIndex) {
      super(String.format(Locale.ROOT, "Invalid index %d requested for %s", pInvalidIndex, pComponent));
   }

   public TranslatableFormatException(TranslatableContents pContents, Throwable pCause) {
      super(String.format(Locale.ROOT, "Error while parsing: %s", pContents), pCause);
   }
}