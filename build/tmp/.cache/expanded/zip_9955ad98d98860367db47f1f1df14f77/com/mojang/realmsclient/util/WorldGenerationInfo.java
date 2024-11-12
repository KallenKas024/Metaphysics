package com.mojang.realmsclient.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldGenerationInfo {
   private final String seed;
   private final LevelType levelType;
   private final boolean generateStructures;

   public WorldGenerationInfo(String pSeed, LevelType pLevelType, boolean pGenerateStructures) {
      this.seed = pSeed;
      this.levelType = pLevelType;
      this.generateStructures = pGenerateStructures;
   }

   public String getSeed() {
      return this.seed;
   }

   public LevelType getLevelType() {
      return this.levelType;
   }

   public boolean shouldGenerateStructures() {
      return this.generateStructures;
   }
}