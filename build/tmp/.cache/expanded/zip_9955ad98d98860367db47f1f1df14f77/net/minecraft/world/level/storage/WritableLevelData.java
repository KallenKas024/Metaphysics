package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData {
   /**
    * Set the x spawn position to the passed in value
    */
   void setXSpawn(int pXSpawn);

   /**
    * Sets the y spawn position
    */
   void setYSpawn(int pYSpawn);

   /**
    * Set the z spawn position to the passed in value
    */
   void setZSpawn(int pZSpawn);

   void setSpawnAngle(float pSpawnAngle);

   default void setSpawn(BlockPos pSpawnPoint, float pSpawnAngle) {
      this.setXSpawn(pSpawnPoint.getX());
      this.setYSpawn(pSpawnPoint.getY());
      this.setZSpawn(pSpawnPoint.getZ());
      this.setSpawnAngle(pSpawnAngle);
   }
}