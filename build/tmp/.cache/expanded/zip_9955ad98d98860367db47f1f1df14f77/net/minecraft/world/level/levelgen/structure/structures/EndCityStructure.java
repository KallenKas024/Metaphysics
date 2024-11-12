package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class EndCityStructure extends Structure {
   public static final Codec<EndCityStructure> CODEC = simpleCodec(EndCityStructure::new);

   public EndCityStructure(Structure.StructureSettings p_227526_) {
      super(p_227526_);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      Rotation rotation = Rotation.getRandom(pContext.random());
      BlockPos blockpos = this.getLowestYIn5by5BoxOffset7Blocks(pContext, rotation);
      return blockpos.getY() < 60 ? Optional.empty() : Optional.of(new Structure.GenerationStub(blockpos, (p_227538_) -> {
         this.generatePieces(p_227538_, blockpos, rotation, pContext);
      }));
   }

   private void generatePieces(StructurePiecesBuilder pBuilder, BlockPos pStartPos, Rotation pRotation, Structure.GenerationContext pContext) {
      List<StructurePiece> list = Lists.newArrayList();
      EndCityPieces.startHouseTower(pContext.structureTemplateManager(), pStartPos, pRotation, list, pContext.random());
      list.forEach(pBuilder::addPiece);
   }

   public StructureType<?> type() {
      return StructureType.END_CITY;
   }
}