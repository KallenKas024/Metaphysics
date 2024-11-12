package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdStructure extends Structure {
   public static final Codec<StrongholdStructure> CODEC = simpleCodec(StrongholdStructure::new);

   public StrongholdStructure(Structure.StructureSettings p_229939_) {
      super(p_229939_);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      return Optional.of(new Structure.GenerationStub(pContext.chunkPos().getWorldPosition(), (p_229944_) -> {
         generatePieces(p_229944_, pContext);
      }));
   }

   private static void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext) {
      int i = 0;

      StrongholdPieces.StartPiece strongholdpieces$startpiece;
      do {
         pBuilder.clear();
         pContext.random().setLargeFeatureSeed(pContext.seed() + (long)(i++), pContext.chunkPos().x, pContext.chunkPos().z);
         StrongholdPieces.resetPieces();
         strongholdpieces$startpiece = new StrongholdPieces.StartPiece(pContext.random(), pContext.chunkPos().getBlockX(2), pContext.chunkPos().getBlockZ(2));
         pBuilder.addPiece(strongholdpieces$startpiece);
         strongholdpieces$startpiece.addChildren(strongholdpieces$startpiece, pBuilder, pContext.random());
         List<StructurePiece> list = strongholdpieces$startpiece.pendingChildren;

         while(!list.isEmpty()) {
            int j = pContext.random().nextInt(list.size());
            StructurePiece structurepiece = list.remove(j);
            structurepiece.addChildren(strongholdpieces$startpiece, pBuilder, pContext.random());
         }

         pBuilder.moveBelowSeaLevel(pContext.chunkGenerator().getSeaLevel(), pContext.chunkGenerator().getMinY(), pContext.random(), 10);
      } while(pBuilder.isEmpty() || strongholdpieces$startpiece.portalRoomPiece == null);

   }

   public StructureType<?> type() {
      return StructureType.STRONGHOLD;
   }
}