package net.minecraft.world.level.chunk;

public class MissingPaletteEntryException extends RuntimeException {
   public MissingPaletteEntryException(int pIndex) {
      super("Missing Palette entry for index " + pIndex + ".");
   }
}