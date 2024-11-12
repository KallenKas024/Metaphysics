package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PlaceCommand {
   private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.feature.failed"));
   private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
   private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.structure.failed"));
   private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType((p_214582_) -> {
      return Component.translatable("commands.place.template.invalid", p_214582_);
   });
   private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.template.failed"));
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = (p_214552_, p_214553_) -> {
      StructureTemplateManager structuretemplatemanager = p_214552_.getSource().getLevel().getStructureManager();
      return SharedSuggestionProvider.suggestResource(structuretemplatemanager.listTemplates(), p_214553_);
   };

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("place").requires((p_214560_) -> {
         return p_214560_.hasPermission(2);
      }).then(Commands.literal("feature").then(Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE)).executes((p_274824_) -> {
         return placeFeature(p_274824_.getSource(), ResourceKeyArgument.getConfiguredFeature(p_274824_, "feature"), BlockPos.containing(p_274824_.getSource().getPosition()));
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_248163_) -> {
         return placeFeature(p_248163_.getSource(), ResourceKeyArgument.getConfiguredFeature(p_248163_, "feature"), BlockPosArgument.getLoadedBlockPos(p_248163_, "pos"));
      })))).then(Commands.literal("jigsaw").then(Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL)).then(Commands.argument("target", ResourceLocationArgument.id()).then(Commands.argument("max_depth", IntegerArgumentType.integer(1, 7)).executes((p_274825_) -> {
         return placeJigsaw(p_274825_.getSource(), ResourceKeyArgument.getStructureTemplatePool(p_274825_, "pool"), ResourceLocationArgument.getId(p_274825_, "target"), IntegerArgumentType.getInteger(p_274825_, "max_depth"), BlockPos.containing(p_274825_.getSource().getPosition()));
      }).then(Commands.argument("position", BlockPosArgument.blockPos()).executes((p_248167_) -> {
         return placeJigsaw(p_248167_.getSource(), ResourceKeyArgument.getStructureTemplatePool(p_248167_, "pool"), ResourceLocationArgument.getId(p_248167_, "target"), IntegerArgumentType.getInteger(p_248167_, "max_depth"), BlockPosArgument.getLoadedBlockPos(p_248167_, "position"));
      })))))).then(Commands.literal("structure").then(Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE)).executes((p_274826_) -> {
         return placeStructure(p_274826_.getSource(), ResourceKeyArgument.getStructure(p_274826_, "structure"), BlockPos.containing(p_274826_.getSource().getPosition()));
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_248168_) -> {
         return placeStructure(p_248168_.getSource(), ResourceKeyArgument.getStructure(p_248168_, "structure"), BlockPosArgument.getLoadedBlockPos(p_248168_, "pos"));
      })))).then(Commands.literal("template").then(Commands.argument("template", ResourceLocationArgument.id()).suggests(SUGGEST_TEMPLATES).executes((p_274827_) -> {
         return placeTemplate(p_274827_.getSource(), ResourceLocationArgument.getId(p_274827_, "template"), BlockPos.containing(p_274827_.getSource().getPosition()), Rotation.NONE, Mirror.NONE, 1.0F, 0);
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_214596_) -> {
         return placeTemplate(p_214596_.getSource(), ResourceLocationArgument.getId(p_214596_, "template"), BlockPosArgument.getLoadedBlockPos(p_214596_, "pos"), Rotation.NONE, Mirror.NONE, 1.0F, 0);
      }).then(Commands.argument("rotation", TemplateRotationArgument.templateRotation()).executes((p_214594_) -> {
         return placeTemplate(p_214594_.getSource(), ResourceLocationArgument.getId(p_214594_, "template"), BlockPosArgument.getLoadedBlockPos(p_214594_, "pos"), TemplateRotationArgument.getRotation(p_214594_, "rotation"), Mirror.NONE, 1.0F, 0);
      }).then(Commands.argument("mirror", TemplateMirrorArgument.templateMirror()).executes((p_214592_) -> {
         return placeTemplate(p_214592_.getSource(), ResourceLocationArgument.getId(p_214592_, "template"), BlockPosArgument.getLoadedBlockPos(p_214592_, "pos"), TemplateRotationArgument.getRotation(p_214592_, "rotation"), TemplateMirrorArgument.getMirror(p_214592_, "mirror"), 1.0F, 0);
      }).then(Commands.argument("integrity", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((p_214586_) -> {
         return placeTemplate(p_214586_.getSource(), ResourceLocationArgument.getId(p_214586_, "template"), BlockPosArgument.getLoadedBlockPos(p_214586_, "pos"), TemplateRotationArgument.getRotation(p_214586_, "rotation"), TemplateMirrorArgument.getMirror(p_214586_, "mirror"), FloatArgumentType.getFloat(p_214586_, "integrity"), 0);
      }).then(Commands.argument("seed", IntegerArgumentType.integer()).executes((p_214550_) -> {
         return placeTemplate(p_214550_.getSource(), ResourceLocationArgument.getId(p_214550_, "template"), BlockPosArgument.getLoadedBlockPos(p_214550_, "pos"), TemplateRotationArgument.getRotation(p_214550_, "rotation"), TemplateMirrorArgument.getMirror(p_214550_, "mirror"), FloatArgumentType.getFloat(p_214550_, "integrity"), IntegerArgumentType.getInteger(p_214550_, "seed"));
      })))))))));
   }

   public static int placeFeature(CommandSourceStack pSource, Holder.Reference<ConfiguredFeature<?, ?>> pFeature, BlockPos pPos) throws CommandSyntaxException {
      ServerLevel serverlevel = pSource.getLevel();
      ConfiguredFeature<?, ?> configuredfeature = pFeature.value();
      ChunkPos chunkpos = new ChunkPos(pPos);
      checkLoaded(serverlevel, new ChunkPos(chunkpos.x - 1, chunkpos.z - 1), new ChunkPos(chunkpos.x + 1, chunkpos.z + 1));
      if (!configuredfeature.place(serverlevel, serverlevel.getChunkSource().getGenerator(), serverlevel.getRandom(), pPos)) {
         throw ERROR_FEATURE_FAILED.create();
      } else {
         String s = pFeature.key().location().toString();
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.place.feature.success", s, pPos.getX(), pPos.getY(), pPos.getZ());
         }, true);
         return 1;
      }
   }

   public static int placeJigsaw(CommandSourceStack pSource, Holder<StructureTemplatePool> pTemplatePool, ResourceLocation pTarget, int pMaxDepth, BlockPos pPos) throws CommandSyntaxException {
      ServerLevel serverlevel = pSource.getLevel();
      if (!JigsawPlacement.generateJigsaw(serverlevel, pTemplatePool, pTarget, pMaxDepth, pPos, false)) {
         throw ERROR_JIGSAW_FAILED.create();
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.place.jigsaw.success", pPos.getX(), pPos.getY(), pPos.getZ());
         }, true);
         return 1;
      }
   }

   public static int placeStructure(CommandSourceStack pSource, Holder.Reference<Structure> pStructure, BlockPos pPos) throws CommandSyntaxException {
      ServerLevel serverlevel = pSource.getLevel();
      Structure structure = pStructure.value();
      ChunkGenerator chunkgenerator = serverlevel.getChunkSource().getGenerator();
      StructureStart structurestart = structure.generate(pSource.registryAccess(), chunkgenerator, chunkgenerator.getBiomeSource(), serverlevel.getChunkSource().randomState(), serverlevel.getStructureManager(), serverlevel.getSeed(), new ChunkPos(pPos), 0, serverlevel, (p_214580_) -> {
         return true;
      });
      if (!structurestart.isValid()) {
         throw ERROR_STRUCTURE_FAILED.create();
      } else {
         BoundingBox boundingbox = structurestart.getBoundingBox();
         ChunkPos chunkpos = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.minX()), SectionPos.blockToSectionCoord(boundingbox.minZ()));
         ChunkPos chunkpos1 = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.maxX()), SectionPos.blockToSectionCoord(boundingbox.maxZ()));
         checkLoaded(serverlevel, chunkpos, chunkpos1);
         ChunkPos.rangeClosed(chunkpos, chunkpos1).forEach((p_289290_) -> {
            structurestart.placeInChunk(serverlevel, serverlevel.structureManager(), chunkgenerator, serverlevel.getRandom(), new BoundingBox(p_289290_.getMinBlockX(), serverlevel.getMinBuildHeight(), p_289290_.getMinBlockZ(), p_289290_.getMaxBlockX(), serverlevel.getMaxBuildHeight(), p_289290_.getMaxBlockZ()), p_289290_);
         });
         String s = pStructure.key().location().toString();
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.place.structure.success", s, pPos.getX(), pPos.getY(), pPos.getZ());
         }, true);
         return 1;
      }
   }

   public static int placeTemplate(CommandSourceStack pSource, ResourceLocation pTemplate, BlockPos pPos, Rotation pRotation, Mirror pMirror, float pIntegrity, int pSeed) throws CommandSyntaxException {
      ServerLevel serverlevel = pSource.getLevel();
      StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

      Optional<StructureTemplate> optional;
      try {
         optional = structuretemplatemanager.get(pTemplate);
      } catch (ResourceLocationException resourcelocationexception) {
         throw ERROR_TEMPLATE_INVALID.create(pTemplate);
      }

      if (optional.isEmpty()) {
         throw ERROR_TEMPLATE_INVALID.create(pTemplate);
      } else {
         StructureTemplate structuretemplate = optional.get();
         checkLoaded(serverlevel, new ChunkPos(pPos), new ChunkPos(pPos.offset(structuretemplate.getSize())));
         StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(pMirror).setRotation(pRotation);
         if (pIntegrity < 1.0F) {
            structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(pIntegrity)).setRandom(StructureBlockEntity.createRandom((long)pSeed));
         }

         boolean flag = structuretemplate.placeInWorld(serverlevel, pPos, pPos, structureplacesettings, StructureBlockEntity.createRandom((long)pSeed), 2);
         if (!flag) {
            throw ERROR_TEMPLATE_FAILED.create();
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.place.template.success", pTemplate, pPos.getX(), pPos.getY(), pPos.getZ());
            }, true);
            return 1;
         }
      }
   }

   private static void checkLoaded(ServerLevel pLevel, ChunkPos pStart, ChunkPos pEnd) throws CommandSyntaxException {
      if (ChunkPos.rangeClosed(pStart, pEnd).filter((p_214542_) -> {
         return !pLevel.isLoaded(p_214542_.getWorldPosition());
      }).findAny().isPresent()) {
         throw BlockPosArgument.ERROR_NOT_LOADED.create();
      }
   }
}