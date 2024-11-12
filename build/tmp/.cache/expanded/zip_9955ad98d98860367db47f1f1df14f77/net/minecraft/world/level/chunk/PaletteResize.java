package net.minecraft.world.level.chunk;

interface PaletteResize<T> {
   /**
    * Called when the underlying palette needs to resize itself to support additional objects.
    * @return The new integer mapping for the object added.
    * @param pBits The new palette size, in bits.
    */
   int onResize(int pBits, T pObjectAdded);
}