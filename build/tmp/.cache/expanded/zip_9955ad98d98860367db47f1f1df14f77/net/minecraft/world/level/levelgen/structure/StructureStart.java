package net.minecraft.world.level.levelgen.structure;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.slf4j.Logger;

public final class StructureStart {
   public static final String INVALID_START_ID = "INVALID";
   public static final StructureStart INVALID_START = new StructureStart((Structure)null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Structure structure;
   private final PiecesContainer pieceContainer;
   private final ChunkPos chunkPos;
   private int references;
   @Nullable
   private volatile BoundingBox cachedBoundingBox;

   public StructureStart(Structure pStructure, ChunkPos pChunkPos, int pReferences, PiecesContainer pPieceContainer) {
      this.structure = pStructure;
      this.chunkPos = pChunkPos;
      this.references = pReferences;
      this.pieceContainer = pPieceContainer;
   }

   @Nullable
   public static StructureStart loadStaticStart(StructurePieceSerializationContext pContext, CompoundTag pTag, long pSeed) {
      String s = pTag.getString("id");
      if ("INVALID".equals(s)) {
         return INVALID_START;
      } else {
         Registry<Structure> registry = pContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
         Structure structure = registry.get(new ResourceLocation(s));
         if (structure == null) {
            LOGGER.error("Unknown stucture id: {}", (Object)s);
            return null;
         } else {
            ChunkPos chunkpos = new ChunkPos(pTag.getInt("ChunkX"), pTag.getInt("ChunkZ"));
            int i = pTag.getInt("references");
            ListTag listtag = pTag.getList("Children", 10);

            try {
               PiecesContainer piecescontainer = PiecesContainer.load(listtag, pContext);
               if (structure instanceof OceanMonumentStructure) {
                  piecescontainer = OceanMonumentStructure.regeneratePiecesAfterLoad(chunkpos, pSeed, piecescontainer);
               }

               return new StructureStart(structure, chunkpos, i, piecescontainer);
            } catch (Exception exception) {
               LOGGER.error("Failed Start with id {}", s, exception);
               return null;
            }
         }
      }
   }

   public BoundingBox getBoundingBox() {
      BoundingBox boundingbox = this.cachedBoundingBox;
      if (boundingbox == null) {
         boundingbox = this.structure.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
         this.cachedBoundingBox = boundingbox;
      }

      return boundingbox;
   }

   public void placeInChunk(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos) {
      List<StructurePiece> list = this.pieceContainer.pieces();
      if (!list.isEmpty()) {
         BoundingBox boundingbox = (list.get(0)).boundingBox;
         BlockPos blockpos = boundingbox.getCenter();
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), boundingbox.minY(), blockpos.getZ());

         for(StructurePiece structurepiece : list) {
            if (structurepiece.getBoundingBox().intersects(pBox)) {
               structurepiece.postProcess(pLevel, pStructureManager, pGenerator, pRandom, pBox, pChunkPos, blockpos1);
            }
         }

         this.structure.afterPlace(pLevel, pStructureManager, pGenerator, pRandom, pBox, pChunkPos, this.pieceContainer);
      }
   }

   public CompoundTag createTag(StructurePieceSerializationContext pContext, ChunkPos pChunkPos) {
      CompoundTag compoundtag = new CompoundTag();
      if (this.isValid()) {
         if (pContext.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this.getStructure()) == null) { // FORGE: This is just a more friendly error instead of the 'Null String' below
            throw new RuntimeException("StructureStart \"" + this.getClass().getName() + "\": \"" + this.getStructure() + "\" unregistered, serializing impossible.");
         }
         compoundtag.putString("id", pContext.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this.structure).toString());
         compoundtag.putInt("ChunkX", pChunkPos.x);
         compoundtag.putInt("ChunkZ", pChunkPos.z);
         compoundtag.putInt("references", this.references);
         compoundtag.put("Children", this.pieceContainer.save(pContext));
         return compoundtag;
      } else {
         compoundtag.putString("id", "INVALID");
         return compoundtag;
      }
   }

   public boolean isValid() {
      return !this.pieceContainer.isEmpty();
   }

   public ChunkPos getChunkPos() {
      return this.chunkPos;
   }

   public boolean canBeReferenced() {
      return this.references < this.getMaxReferences();
   }

   public void addReference() {
      ++this.references;
   }

   public int getReferences() {
      return this.references;
   }

   protected int getMaxReferences() {
      return 1;
   }

   public Structure getStructure() {
      return this.structure;
   }

   public List<StructurePiece> getPieces() {
      return this.pieceContainer.pieces();
   }
}
