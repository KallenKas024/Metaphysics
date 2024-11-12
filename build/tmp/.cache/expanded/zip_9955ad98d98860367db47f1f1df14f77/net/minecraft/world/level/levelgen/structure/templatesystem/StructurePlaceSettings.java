package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructurePlaceSettings {
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private BlockPos rotationPivot = BlockPos.ZERO;
   private boolean ignoreEntities;
   @Nullable
   private BoundingBox boundingBox;
   private boolean keepLiquids = true;
   @Nullable
   private RandomSource random;
   private int palette;
   private final List<StructureProcessor> processors = Lists.newArrayList();
   private boolean knownShape;
   private boolean finalizeEntities;

   public StructurePlaceSettings copy() {
      StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
      structureplacesettings.mirror = this.mirror;
      structureplacesettings.rotation = this.rotation;
      structureplacesettings.rotationPivot = this.rotationPivot;
      structureplacesettings.ignoreEntities = this.ignoreEntities;
      structureplacesettings.boundingBox = this.boundingBox;
      structureplacesettings.keepLiquids = this.keepLiquids;
      structureplacesettings.random = this.random;
      structureplacesettings.palette = this.palette;
      structureplacesettings.processors.addAll(this.processors);
      structureplacesettings.knownShape = this.knownShape;
      structureplacesettings.finalizeEntities = this.finalizeEntities;
      return structureplacesettings;
   }

   public StructurePlaceSettings setMirror(Mirror pMirror) {
      this.mirror = pMirror;
      return this;
   }

   public StructurePlaceSettings setRotation(Rotation pRotation) {
      this.rotation = pRotation;
      return this;
   }

   public StructurePlaceSettings setRotationPivot(BlockPos pRotationPivot) {
      this.rotationPivot = pRotationPivot;
      return this;
   }

   public StructurePlaceSettings setIgnoreEntities(boolean pIgnoreEntities) {
      this.ignoreEntities = pIgnoreEntities;
      return this;
   }

   public StructurePlaceSettings setBoundingBox(BoundingBox pBoundingBox) {
      this.boundingBox = pBoundingBox;
      return this;
   }

   public StructurePlaceSettings setRandom(@Nullable RandomSource pRandom) {
      this.random = pRandom;
      return this;
   }

   public StructurePlaceSettings setKeepLiquids(boolean pKeepLiquids) {
      this.keepLiquids = pKeepLiquids;
      return this;
   }

   public StructurePlaceSettings setKnownShape(boolean pKnownShape) {
      this.knownShape = pKnownShape;
      return this;
   }

   public StructurePlaceSettings clearProcessors() {
      this.processors.clear();
      return this;
   }

   public StructurePlaceSettings addProcessor(StructureProcessor pProcessor) {
      this.processors.add(pProcessor);
      return this;
   }

   public StructurePlaceSettings popProcessor(StructureProcessor pProcessor) {
      this.processors.remove(pProcessor);
      return this;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public BlockPos getRotationPivot() {
      return this.rotationPivot;
   }

   public RandomSource getRandom(@Nullable BlockPos pSeedPos) {
      if (this.random != null) {
         return this.random;
      } else {
         return pSeedPos == null ? RandomSource.create(Util.getMillis()) : RandomSource.create(Mth.getSeed(pSeedPos));
      }
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   @Nullable
   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public boolean getKnownShape() {
      return this.knownShape;
   }

   public List<StructureProcessor> getProcessors() {
      return this.processors;
   }

   public boolean shouldKeepLiquids() {
      return this.keepLiquids;
   }

   public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> pPalettes, @Nullable BlockPos pPos) {
      int i = pPalettes.size();
      if (i == 0) {
         throw new IllegalStateException("No palettes");
      } else {
         return pPalettes.get(this.getRandom(pPos).nextInt(i));
      }
   }

   public StructurePlaceSettings setFinalizeEntities(boolean pFinalizeEntities) {
      this.finalizeEntities = pFinalizeEntities;
      return this;
   }

   public boolean shouldFinalizeEntities() {
      return this.finalizeEntities;
   }
}