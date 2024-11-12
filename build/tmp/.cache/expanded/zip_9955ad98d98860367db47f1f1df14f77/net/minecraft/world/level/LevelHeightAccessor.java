package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
   int getHeight();

   int getMinBuildHeight();

   default int getMaxBuildHeight() {
      return this.getMinBuildHeight() + this.getHeight();
   }

   default int getSectionsCount() {
      return this.getMaxSection() - this.getMinSection();
   }

   default int getMinSection() {
      return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
   }

   default int getMaxSection() {
      return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
   }

   default boolean isOutsideBuildHeight(BlockPos pPos) {
      return this.isOutsideBuildHeight(pPos.getY());
   }

   default boolean isOutsideBuildHeight(int pY) {
      return pY < this.getMinBuildHeight() || pY >= this.getMaxBuildHeight();
   }

   default int getSectionIndex(int pY) {
      return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(pY));
   }

   default int getSectionIndexFromSectionY(int pSectionIndex) {
      return pSectionIndex - this.getMinSection();
   }

   default int getSectionYFromSectionIndex(int pSectionIndex) {
      return pSectionIndex + this.getMinSection();
   }

   static LevelHeightAccessor create(final int pMinBuildHeight, final int pHeight) {
      return new LevelHeightAccessor() {
         public int getHeight() {
            return pHeight;
         }

         public int getMinBuildHeight() {
            return pMinBuildHeight;
         }
      };
   }
}