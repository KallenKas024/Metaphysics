package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class StructurePoolElement {
   public static final Codec<StructurePoolElement> CODEC = BuiltInRegistries.STRUCTURE_POOL_ELEMENT.byNameCodec().dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
   private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));
   @Nullable
   private volatile StructureTemplatePool.Projection projection;

   protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
      return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
   }

   protected StructurePoolElement(StructureTemplatePool.Projection pProjection) {
      this.projection = pProjection;
   }

   public abstract Vec3i getSize(StructureTemplateManager pStructureTemplateManager, Rotation pRotation);

   public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation, RandomSource pRandom);

   public abstract BoundingBox getBoundingBox(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation);

   public abstract boolean place(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, BlockPos pOffset, BlockPos pPos, Rotation pRotation, BoundingBox pBox, RandomSource pRandom, boolean pKeepJigsaws);

   public abstract StructurePoolElementType<?> getType();

   public void handleDataMarker(LevelAccessor pLevel, StructureTemplate.StructureBlockInfo pBlockInfo, BlockPos pPos, Rotation pRotation, RandomSource pRandom, BoundingBox pBox) {
   }

   public StructurePoolElement setProjection(StructureTemplatePool.Projection pProjection) {
      this.projection = pProjection;
      return this;
   }

   public StructureTemplatePool.Projection getProjection() {
      StructureTemplatePool.Projection structuretemplatepool$projection = this.projection;
      if (structuretemplatepool$projection == null) {
         throw new IllegalStateException();
      } else {
         return structuretemplatepool$projection;
      }
   }

   public int getGroundLevelDelta() {
      return 1;
   }

   public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
      return (p_210525_) -> {
         return EmptyPoolElement.INSTANCE;
      };
   }

   public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String pId) {
      return (p_255605_) -> {
         return new LegacySinglePoolElement(Either.left(new ResourceLocation(pId)), EMPTY, p_255605_);
      };
   }

   public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String pId, Holder<StructureProcessorList> pProcessors) {
      return (p_210537_) -> {
         return new LegacySinglePoolElement(Either.left(new ResourceLocation(pId)), pProcessors, p_210537_);
      };
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String pId) {
      return (p_255603_) -> {
         return new SinglePoolElement(Either.left(new ResourceLocation(pId)), EMPTY, p_255603_);
      };
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String pId, Holder<StructureProcessorList> pProcessors) {
      return (p_210518_) -> {
         return new SinglePoolElement(Either.left(new ResourceLocation(pId)), pProcessors, p_210518_);
      };
   }

   public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(Holder<PlacedFeature> pFeature) {
      return (p_210506_) -> {
         return new FeaturePoolElement(pFeature, p_210506_);
      };
   }

   public static Function<StructureTemplatePool.Projection, ListPoolElement> list(List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> pElements) {
      return (p_210523_) -> {
         return new ListPoolElement(pElements.stream().map((p_210482_) -> {
            return p_210482_.apply(p_210523_);
         }).collect(Collectors.toList()), p_210523_);
      };
   }
}