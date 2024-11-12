package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
   public static void bootstrap(BootstapContext<StructureTemplatePool> pContext) {
      PlainVillagePools.bootstrap(pContext);
      SnowyVillagePools.bootstrap(pContext);
      SavannaVillagePools.bootstrap(pContext);
      DesertVillagePools.bootstrap(pContext);
      TaigaVillagePools.bootstrap(pContext);
   }
}