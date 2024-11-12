package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class IglooPieces {
   public static final int GENERATION_HEIGHT = 90;
   static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
   private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
   private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
   static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7));
   static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2));

   public static void addPieces(StructureTemplateManager pStructureTemplateManager, BlockPos pStartPos, Rotation pRotation, StructurePieceAccessor pPieces, RandomSource pRandom) {
      if (pRandom.nextDouble() < 0.5D) {
         int i = pRandom.nextInt(8) + 4;
         pPieces.addPiece(new IglooPieces.IglooPiece(pStructureTemplateManager, STRUCTURE_LOCATION_LABORATORY, pStartPos, pRotation, i * 3));

         for(int j = 0; j < i - 1; ++j) {
            pPieces.addPiece(new IglooPieces.IglooPiece(pStructureTemplateManager, STRUCTURE_LOCATION_LADDER, pStartPos, pRotation, j * 3));
         }
      }

      pPieces.addPiece(new IglooPieces.IglooPiece(pStructureTemplateManager, STRUCTURE_LOCATION_IGLOO, pStartPos, pRotation, 0));
   }

   public static class IglooPiece extends TemplateStructurePiece {
      public IglooPiece(StructureTemplateManager pStructureTemplateManager, ResourceLocation pLocation, BlockPos pStartPos, Rotation pRotation, int pDown) {
         super(StructurePieceType.IGLOO, 0, pStructureTemplateManager, pLocation, pLocation.toString(), makeSettings(pRotation, pLocation), makePosition(pLocation, pStartPos, pDown));
      }

      public IglooPiece(StructureTemplateManager pStructureTemplateManager, CompoundTag pTag) {
         super(StructurePieceType.IGLOO, pTag, pStructureTemplateManager, (p_227589_) -> {
            return makeSettings(Rotation.valueOf(pTag.getString("Rot")), p_227589_);
         });
      }

      private static StructurePlaceSettings makeSettings(Rotation pRotation, ResourceLocation pLocation) {
         return (new StructurePlaceSettings()).setRotation(pRotation).setMirror(Mirror.NONE).setRotationPivot(IglooPieces.PIVOTS.get(pLocation)).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      private static BlockPos makePosition(ResourceLocation pLocation, BlockPos pPos, int pDown) {
         return pPos.offset(IglooPieces.OFFSETS.get(pLocation)).below(pDown);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
         super.addAdditionalSaveData(pContext, pTag);
         pTag.putString("Rot", this.placeSettings.getRotation().name());
      }

      protected void handleDataMarker(String pName, BlockPos pPos, ServerLevelAccessor pLevel, RandomSource pRandom, BoundingBox pBox) {
         if ("chest".equals(pName)) {
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
            BlockEntity blockentity = pLevel.getBlockEntity(pPos.below());
            if (blockentity instanceof ChestBlockEntity) {
               ((ChestBlockEntity)blockentity).setLootTable(BuiltInLootTables.IGLOO_CHEST, pRandom.nextLong());
            }

         }
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         ResourceLocation resourcelocation = new ResourceLocation(this.templateName);
         StructurePlaceSettings structureplacesettings = makeSettings(this.placeSettings.getRotation(), resourcelocation);
         BlockPos blockpos = IglooPieces.OFFSETS.get(resourcelocation);
         BlockPos blockpos1 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structureplacesettings, new BlockPos(3 - blockpos.getX(), 0, -blockpos.getZ())));
         int i = pLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos1.getX(), blockpos1.getZ());
         BlockPos blockpos2 = this.templatePosition;
         this.templatePosition = this.templatePosition.offset(0, i - 90 - 1, 0);
         super.postProcess(pLevel, pStructureManager, pGenerator, pRandom, pBox, pChunkPos, pPos);
         if (resourcelocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
            BlockPos blockpos3 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structureplacesettings, new BlockPos(3, 0, 5)));
            BlockState blockstate = pLevel.getBlockState(blockpos3.below());
            if (!blockstate.isAir() && !blockstate.is(Blocks.LADDER)) {
               pLevel.setBlock(blockpos3, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
            }
         }

         this.templatePosition = blockpos2;
      }
   }
}