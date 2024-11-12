package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureUtils {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
   public static String testStructuresDir = "gameteststructures";
   private static final int HOW_MANY_CHUNKS_TO_LOAD_IN_EACH_DIRECTION_OF_STRUCTURE = 4;

   public static Rotation getRotationForRotationSteps(int pRotationSteps) {
      switch (pRotationSteps) {
         case 0:
            return Rotation.NONE;
         case 1:
            return Rotation.CLOCKWISE_90;
         case 2:
            return Rotation.CLOCKWISE_180;
         case 3:
            return Rotation.COUNTERCLOCKWISE_90;
         default:
            throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + pRotationSteps);
      }
   }

   public static int getRotationStepsForRotation(Rotation pRotation) {
      switch (pRotation) {
         case NONE:
            return 0;
         case CLOCKWISE_90:
            return 1;
         case CLOCKWISE_180:
            return 2;
         case COUNTERCLOCKWISE_90:
            return 3;
         default:
            throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + pRotation);
      }
   }

   public static void main(String[] pArgs) throws IOException {
      Bootstrap.bootStrap();
      Files.walk(Paths.get(testStructuresDir)).filter((p_177775_) -> {
         return p_177775_.toString().endsWith(".snbt");
      }).forEach((p_177773_) -> {
         try {
            String s = Files.readString(p_177773_);
            CompoundTag compoundtag = NbtUtils.snbtToStructure(s);
            CompoundTag compoundtag1 = StructureUpdater.update(p_177773_.toString(), compoundtag);
            NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, p_177773_, NbtUtils.structureToSnbt(compoundtag1));
         } catch (IOException | CommandSyntaxException commandsyntaxexception) {
            LOGGER.error("Something went wrong upgrading: {}", p_177773_, commandsyntaxexception);
         }

      });
   }

   public static AABB getStructureBounds(StructureBlockEntity pStructureBlockEntity) {
      BlockPos blockpos = pStructureBlockEntity.getBlockPos();
      BlockPos blockpos1 = blockpos.offset(pStructureBlockEntity.getStructureSize().offset(-1, -1, -1));
      BlockPos blockpos2 = StructureTemplate.transform(blockpos1, Mirror.NONE, pStructureBlockEntity.getRotation(), blockpos);
      return new AABB(blockpos, blockpos2);
   }

   public static BoundingBox getStructureBoundingBox(StructureBlockEntity pStructureBlockEntity) {
      BlockPos blockpos = pStructureBlockEntity.getBlockPos();
      BlockPos blockpos1 = blockpos.offset(pStructureBlockEntity.getStructureSize().offset(-1, -1, -1));
      BlockPos blockpos2 = StructureTemplate.transform(blockpos1, Mirror.NONE, pStructureBlockEntity.getRotation(), blockpos);
      return BoundingBox.fromCorners(blockpos, blockpos2);
   }

   public static void addCommandBlockAndButtonToStartTest(BlockPos pStructureBlockPos, BlockPos pOffset, Rotation pRotation, ServerLevel pServerLevel) {
      BlockPos blockpos = StructureTemplate.transform(pStructureBlockPos.offset(pOffset), Mirror.NONE, pRotation, pStructureBlockPos);
      pServerLevel.setBlockAndUpdate(blockpos, Blocks.COMMAND_BLOCK.defaultBlockState());
      CommandBlockEntity commandblockentity = (CommandBlockEntity)pServerLevel.getBlockEntity(blockpos);
      commandblockentity.getCommandBlock().setCommand("test runthis");
      BlockPos blockpos1 = StructureTemplate.transform(blockpos.offset(0, 0, -1), Mirror.NONE, pRotation, blockpos);
      pServerLevel.setBlockAndUpdate(blockpos1, Blocks.STONE_BUTTON.defaultBlockState().rotate(pRotation));
   }

   public static void createNewEmptyStructureBlock(String pStructureName, BlockPos pPos, Vec3i pSize, Rotation pRotation, ServerLevel pServerLevel) {
      BoundingBox boundingbox = getStructureBoundingBox(pPos, pSize, pRotation);
      clearSpaceForStructure(boundingbox, pPos.getY(), pServerLevel);
      pServerLevel.setBlockAndUpdate(pPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
      StructureBlockEntity structureblockentity = (StructureBlockEntity)pServerLevel.getBlockEntity(pPos);
      structureblockentity.setIgnoreEntities(false);
      structureblockentity.setStructureName(new ResourceLocation(pStructureName));
      structureblockentity.setStructureSize(pSize);
      structureblockentity.setMode(StructureMode.SAVE);
      structureblockentity.setShowBoundingBox(true);
   }

   /**
    * 
    * @param pDontPlaceOnSizeMismatch If true, the structure won't be placed if the structure block size doesn't match
    * the structure template size
    */
   public static StructureBlockEntity spawnStructure(String pStructureName, BlockPos pPos, Rotation pRotation, int p_127887_, ServerLevel pServerLevel, boolean pDontPlaceOnSizeMismatch) {
      Vec3i vec3i = getStructureTemplate(pStructureName, pServerLevel).getSize();
      BoundingBox boundingbox = getStructureBoundingBox(pPos, vec3i, pRotation);
      BlockPos blockpos;
      if (pRotation == Rotation.NONE) {
         blockpos = pPos;
      } else if (pRotation == Rotation.CLOCKWISE_90) {
         blockpos = pPos.offset(vec3i.getZ() - 1, 0, 0);
      } else if (pRotation == Rotation.CLOCKWISE_180) {
         blockpos = pPos.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
      } else {
         if (pRotation != Rotation.COUNTERCLOCKWISE_90) {
            throw new IllegalArgumentException("Invalid rotation: " + pRotation);
         }

         blockpos = pPos.offset(0, 0, vec3i.getX() - 1);
      }

      forceLoadChunks(pPos, pServerLevel);
      clearSpaceForStructure(boundingbox, pPos.getY(), pServerLevel);
      StructureBlockEntity structureblockentity = createStructureBlock(pStructureName, blockpos, pRotation, pServerLevel, pDontPlaceOnSizeMismatch);
      pServerLevel.getBlockTicks().clearArea(boundingbox);
      pServerLevel.clearBlockEvents(boundingbox);
      return structureblockentity;
   }

   private static void forceLoadChunks(BlockPos pPos, ServerLevel pServerLevel) {
      ChunkPos chunkpos = new ChunkPos(pPos);

      for(int i = -1; i < 4; ++i) {
         for(int j = -1; j < 4; ++j) {
            int k = chunkpos.x + i;
            int l = chunkpos.z + j;
            pServerLevel.setChunkForced(k, l, true);
         }
      }

   }

   public static void clearSpaceForStructure(BoundingBox pBoundingBox, int pStructureBlockY, ServerLevel pServerLevel) {
      BoundingBox boundingbox = new BoundingBox(pBoundingBox.minX() - 2, pBoundingBox.minY() - 3, pBoundingBox.minZ() - 3, pBoundingBox.maxX() + 3, pBoundingBox.maxY() + 20, pBoundingBox.maxZ() + 3);
      BlockPos.betweenClosedStream(boundingbox).forEach((p_177748_) -> {
         clearBlock(pStructureBlockY, p_177748_, pServerLevel);
      });
      pServerLevel.getBlockTicks().clearArea(boundingbox);
      pServerLevel.clearBlockEvents(boundingbox);
      AABB aabb = new AABB((double)boundingbox.minX(), (double)boundingbox.minY(), (double)boundingbox.minZ(), (double)boundingbox.maxX(), (double)boundingbox.maxY(), (double)boundingbox.maxZ());
      List<Entity> list = pServerLevel.getEntitiesOfClass(Entity.class, aabb, (p_177750_) -> {
         return !(p_177750_ instanceof Player);
      });
      list.forEach(Entity::discard);
   }

   public static BoundingBox getStructureBoundingBox(BlockPos pPos, Vec3i pOffset, Rotation pRotation) {
      BlockPos blockpos = pPos.offset(pOffset).offset(-1, -1, -1);
      BlockPos blockpos1 = StructureTemplate.transform(blockpos, Mirror.NONE, pRotation, pPos);
      BoundingBox boundingbox = BoundingBox.fromCorners(pPos, blockpos1);
      int i = Math.min(boundingbox.minX(), boundingbox.maxX());
      int j = Math.min(boundingbox.minZ(), boundingbox.maxZ());
      return boundingbox.move(pPos.getX() - i, 0, pPos.getZ() - j);
   }

   public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos pPos, int pRadius, ServerLevel pServerLevel) {
      return findStructureBlocks(pPos, pRadius, pServerLevel).stream().filter((p_177756_) -> {
         return doesStructureContain(p_177756_, pPos, pServerLevel);
      }).findFirst();
   }

   @Nullable
   public static BlockPos findNearestStructureBlock(BlockPos pPos, int pRadius, ServerLevel pServerLevel) {
      Comparator<BlockPos> comparator = Comparator.comparingInt((p_177759_) -> {
         return p_177759_.distManhattan(pPos);
      });
      Collection<BlockPos> collection = findStructureBlocks(pPos, pRadius, pServerLevel);
      Optional<BlockPos> optional = collection.stream().min(comparator);
      return optional.orElse((BlockPos)null);
   }

   public static Collection<BlockPos> findStructureBlocks(BlockPos pPos, int pRadius, ServerLevel pServerLevel) {
      Collection<BlockPos> collection = Lists.newArrayList();
      AABB aabb = new AABB(pPos);
      aabb = aabb.inflate((double)pRadius);

      for(int i = (int)aabb.minX; i <= (int)aabb.maxX; ++i) {
         for(int j = (int)aabb.minY; j <= (int)aabb.maxY; ++j) {
            for(int k = (int)aabb.minZ; k <= (int)aabb.maxZ; ++k) {
               BlockPos blockpos = new BlockPos(i, j, k);
               BlockState blockstate = pServerLevel.getBlockState(blockpos);
               if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
                  collection.add(blockpos);
               }
            }
         }
      }

      return collection;
   }

   private static StructureTemplate getStructureTemplate(String pStructureName, ServerLevel pServerLevel) {
      StructureTemplateManager structuretemplatemanager = pServerLevel.getStructureManager();
      Optional<StructureTemplate> optional = structuretemplatemanager.get(new ResourceLocation(pStructureName));
      if (optional.isPresent()) {
         return optional.get();
      } else {
         String s = new ResourceLocation(pStructureName).getPath() + ".snbt";
         Path path = Paths.get(testStructuresDir, s);
         CompoundTag compoundtag = tryLoadStructure(path);
         if (compoundtag == null) {
            throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
         } else {
            return structuretemplatemanager.readStructure(compoundtag);
         }
      }
   }

   private static StructureBlockEntity createStructureBlock(String pStructureName, BlockPos pPos, Rotation pRotation, ServerLevel pServerLevel, boolean pDontPlaceOnSizeMismatch) {
      pServerLevel.setBlockAndUpdate(pPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
      StructureBlockEntity structureblockentity = (StructureBlockEntity)pServerLevel.getBlockEntity(pPos);
      structureblockentity.setMode(StructureMode.LOAD);
      structureblockentity.setRotation(pRotation);
      structureblockentity.setIgnoreEntities(false);
      structureblockentity.setStructureName(new ResourceLocation(pStructureName));
      structureblockentity.loadStructure(pServerLevel, pDontPlaceOnSizeMismatch);
      if (structureblockentity.getStructureSize() != Vec3i.ZERO) {
         return structureblockentity;
      } else {
         StructureTemplate structuretemplate = getStructureTemplate(pStructureName, pServerLevel);
         structureblockentity.loadStructure(pServerLevel, pDontPlaceOnSizeMismatch, structuretemplate);
         if (structureblockentity.getStructureSize() == Vec3i.ZERO) {
            throw new RuntimeException("Failed to load structure " + pStructureName);
         } else {
            return structureblockentity;
         }
      }
   }

   @Nullable
   private static CompoundTag tryLoadStructure(Path pPathToStructure) {
      try {
         BufferedReader bufferedreader = Files.newBufferedReader(pPathToStructure);
         String s = IOUtils.toString((Reader)bufferedreader);
         return NbtUtils.snbtToStructure(s);
      } catch (IOException ioexception) {
         return null;
      } catch (CommandSyntaxException commandsyntaxexception) {
         throw new RuntimeException("Error while trying to load structure " + pPathToStructure, commandsyntaxexception);
      }
   }

   private static void clearBlock(int pStructureBlockY, BlockPos pPos, ServerLevel pServerLevel) {
      BlockState blockstate = null;
      RegistryAccess registryaccess = pServerLevel.registryAccess();
      FlatLevelGeneratorSettings flatlevelgeneratorsettings = FlatLevelGeneratorSettings.getDefault(registryaccess.lookupOrThrow(Registries.BIOME), registryaccess.lookupOrThrow(Registries.STRUCTURE_SET), registryaccess.lookupOrThrow(Registries.PLACED_FEATURE));
      List<BlockState> list = flatlevelgeneratorsettings.getLayers();
      int i = pPos.getY() - pServerLevel.getMinBuildHeight();
      if (pPos.getY() < pStructureBlockY && i > 0 && i <= list.size()) {
         blockstate = list.get(i - 1);
      }

      if (blockstate == null) {
         blockstate = Blocks.AIR.defaultBlockState();
      }

      BlockInput blockinput = new BlockInput(blockstate, Collections.emptySet(), (CompoundTag)null);
      blockinput.place(pServerLevel, pPos, 2);
      pServerLevel.blockUpdated(pPos, blockstate.getBlock());
   }

   private static boolean doesStructureContain(BlockPos pStructureBlockPos, BlockPos pPosToTest, ServerLevel pServerLevel) {
      StructureBlockEntity structureblockentity = (StructureBlockEntity)pServerLevel.getBlockEntity(pStructureBlockPos);
      AABB aabb = getStructureBounds(structureblockentity).inflate(1.0D);
      return aabb.contains(Vec3.atCenterOf(pPosToTest));
   }
}
