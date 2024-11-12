package net.minecraft.server.level;

public enum FullChunkStatus {
   INACCESSIBLE,
   FULL,
   BLOCK_TICKING,
   ENTITY_TICKING;

   public boolean isOrAfter(FullChunkStatus pStatus) {
      return this.ordinal() >= pStatus.ordinal();
   }
}