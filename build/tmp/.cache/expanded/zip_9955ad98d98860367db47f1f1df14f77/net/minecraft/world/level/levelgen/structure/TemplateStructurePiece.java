package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final String templateName;
   protected StructureTemplate template;
   protected StructurePlaceSettings placeSettings;
   protected BlockPos templatePosition;

   public TemplateStructurePiece(StructurePieceType pType, int pGenDepth, StructureTemplateManager pStructureTemplateManager, ResourceLocation pLocation, String pTemplateName, StructurePlaceSettings pPlaceSettings, BlockPos pTemplatePosition) {
      super(pType, pGenDepth, pStructureTemplateManager.getOrCreate(pLocation).getBoundingBox(pPlaceSettings, pTemplatePosition));
      this.setOrientation(Direction.NORTH);
      this.templateName = pTemplateName;
      this.templatePosition = pTemplatePosition;
      this.template = pStructureTemplateManager.getOrCreate(pLocation);
      this.placeSettings = pPlaceSettings;
   }

   public TemplateStructurePiece(StructurePieceType pType, CompoundTag pTag, StructureTemplateManager pStructureTemplateManager, Function<ResourceLocation, StructurePlaceSettings> pPlaceSettingsFactory) {
      super(pType, pTag);
      this.setOrientation(Direction.NORTH);
      this.templateName = pTag.getString("Template");
      this.templatePosition = new BlockPos(pTag.getInt("TPX"), pTag.getInt("TPY"), pTag.getInt("TPZ"));
      ResourceLocation resourcelocation = this.makeTemplateLocation();
      this.template = pStructureTemplateManager.getOrCreate(resourcelocation);
      this.placeSettings = pPlaceSettingsFactory.apply(resourcelocation);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
   }

   protected ResourceLocation makeTemplateLocation() {
      return new ResourceLocation(this.templateName);
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      pTag.putInt("TPX", this.templatePosition.getX());
      pTag.putInt("TPY", this.templatePosition.getY());
      pTag.putInt("TPZ", this.templatePosition.getZ());
      pTag.putString("Template", this.templateName);
   }

   public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
      this.placeSettings.setBoundingBox(pBox);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
      if (this.template.placeInWorld(pLevel, this.templatePosition, pPos, this.placeSettings, pRandom, 2)) {
         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
            if (structuretemplate$structureblockinfo.nbt() != null) {
               StructureMode structuremode = StructureMode.valueOf(structuretemplate$structureblockinfo.nbt().getString("mode"));
               if (structuremode == StructureMode.DATA) {
                  this.handleDataMarker(structuretemplate$structureblockinfo.nbt().getString("metadata"), structuretemplate$structureblockinfo.pos(), pLevel, pRandom, pBox);
               }
            }
         }

         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
            if (structuretemplate$structureblockinfo1.nbt() != null) {
               String s = structuretemplate$structureblockinfo1.nbt().getString("final_state");
               BlockState blockstate = Blocks.AIR.defaultBlockState();

               try {
                  blockstate = BlockStateParser.parseForBlock(pLevel.holderLookup(Registries.BLOCK), s, true).blockState();
               } catch (CommandSyntaxException commandsyntaxexception) {
                  LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", s, structuretemplate$structureblockinfo1.pos());
               }

               pLevel.setBlock(structuretemplate$structureblockinfo1.pos(), blockstate, 3);
            }
         }
      }

   }

   protected abstract void handleDataMarker(String pName, BlockPos pPos, ServerLevelAccessor pLevel, RandomSource pRandom, BoundingBox pBox);

   /** @deprecated */
   @Deprecated
   public void move(int pX, int pY, int pZ) {
      super.move(pX, pY, pZ);
      this.templatePosition = this.templatePosition.offset(pX, pY, pZ);
   }

   public Rotation getRotation() {
      return this.placeSettings.getRotation();
   }

   public StructureTemplate template() {
      return this.template;
   }

   public BlockPos templatePosition() {
      return this.templatePosition;
   }

   public StructurePlaceSettings placeSettings() {
      return this.placeSettings;
   }
}