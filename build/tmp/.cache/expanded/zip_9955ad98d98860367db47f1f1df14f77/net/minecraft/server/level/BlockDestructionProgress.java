package net.minecraft.server.level;

import net.minecraft.core.BlockPos;

public class BlockDestructionProgress implements Comparable<BlockDestructionProgress> {
   private final int id;
   private final BlockPos pos;
   private int progress;
   private int updatedRenderTick;

   public BlockDestructionProgress(int pId, BlockPos pPos) {
      this.id = pId;
      this.pos = pPos;
   }

   public int getId() {
      return this.id;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   /**
    * Inserts damage value into this partially destroyed Block. -1 causes client renderer to delete it, otherwise ranges
    * from 1 to 10.
    */
   public void setProgress(int pDamage) {
      if (pDamage > 10) {
         pDamage = 10;
      }

      this.progress = pDamage;
   }

   public int getProgress() {
      return this.progress;
   }

   /**
    * Saves the current Cloud update tick into the PartiallyDestroyedBlock.
    */
   public void updateTick(int pCreatedAtCloudUpdateTick) {
      this.updatedRenderTick = pCreatedAtCloudUpdateTick;
   }

   /**
    * Retrieves the 'date' at which the PartiallyDestroyedBlock was created.
    */
   public int getUpdatedRenderTick() {
      return this.updatedRenderTick;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         BlockDestructionProgress blockdestructionprogress = (BlockDestructionProgress)pOther;
         return this.id == blockdestructionprogress.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Integer.hashCode(this.id);
   }

   public int compareTo(BlockDestructionProgress p_139984_) {
      return this.progress != p_139984_.progress ? Integer.compare(this.progress, p_139984_.progress) : Integer.compare(this.id, p_139984_.id);
   }
}