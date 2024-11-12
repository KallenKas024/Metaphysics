package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface StructureAccess {
   @Nullable
   StructureStart getStartForStructure(Structure pStructure);

   void setStartForStructure(Structure pStructure, StructureStart pStructureStart);

   LongSet getReferencesForStructure(Structure pStructure);

   void addReferenceForStructure(Structure pStructure, long pReference);

   Map<Structure, LongSet> getAllReferences();

   void setAllReferences(Map<Structure, LongSet> pStructureReferencesMap);
}