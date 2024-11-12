package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressStructure extends Structure {
   public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3), new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4), new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4));
   public static final Codec<NetherFortressStructure> CODEC = simpleCodec(NetherFortressStructure::new);

   public NetherFortressStructure(Structure.StructureSettings p_228521_) {
      super(p_228521_);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      ChunkPos chunkpos = pContext.chunkPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 64, chunkpos.getMinBlockZ());
      return Optional.of(new Structure.GenerationStub(blockpos, (p_228526_) -> {
         generatePieces(p_228526_, pContext);
      }));
   }

   private static void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext) {
      NetherFortressPieces.StartPiece netherfortresspieces$startpiece = new NetherFortressPieces.StartPiece(pContext.random(), pContext.chunkPos().getBlockX(2), pContext.chunkPos().getBlockZ(2));
      pBuilder.addPiece(netherfortresspieces$startpiece);
      netherfortresspieces$startpiece.addChildren(netherfortresspieces$startpiece, pBuilder, pContext.random());
      List<StructurePiece> list = netherfortresspieces$startpiece.pendingChildren;

      while(!list.isEmpty()) {
         int i = pContext.random().nextInt(list.size());
         StructurePiece structurepiece = list.remove(i);
         structurepiece.addChildren(netherfortresspieces$startpiece, pBuilder, pContext.random());
      }

      pBuilder.moveInsideHeights(pContext.random(), 48, 70);
   }

   public StructureType<?> type() {
      return StructureType.FORTRESS;
   }
}