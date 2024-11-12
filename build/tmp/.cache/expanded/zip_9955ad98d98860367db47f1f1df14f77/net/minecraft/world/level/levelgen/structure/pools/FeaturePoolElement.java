package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class FeaturePoolElement extends StructurePoolElement {
   public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create((p_210213_) -> {
      return p_210213_.group(PlacedFeature.CODEC.fieldOf("feature").forGetter((p_210215_) -> {
         return p_210215_.feature;
      }), projectionCodec()).apply(p_210213_, FeaturePoolElement::new);
   });
   private final Holder<PlacedFeature> feature;
   private final CompoundTag defaultJigsawNBT;

   protected FeaturePoolElement(Holder<PlacedFeature> p_210209_, StructureTemplatePool.Projection p_210210_) {
      super(p_210210_);
      this.feature = p_210209_;
      this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
   }

   private CompoundTag fillDefaultJigsawNBT() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", "minecraft:bottom");
      compoundtag.putString("final_state", "minecraft:air");
      compoundtag.putString("pool", "minecraft:empty");
      compoundtag.putString("target", "minecraft:empty");
      compoundtag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
      return compoundtag;
   }

   public Vec3i getSize(StructureTemplateManager pStructureTemplateManager, Rotation pRotation) {
      return Vec3i.ZERO;
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation, RandomSource pRandom) {
      List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
      list.add(new StructureTemplate.StructureBlockInfo(pPos, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
      return list;
   }

   public BoundingBox getBoundingBox(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation) {
      Vec3i vec3i = this.getSize(pStructureTemplateManager, pRotation);
      return new BoundingBox(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + vec3i.getX(), pPos.getY() + vec3i.getY(), pPos.getZ() + vec3i.getZ());
   }

   public boolean place(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, BlockPos pOffset, BlockPos pPos, Rotation pRotation, BoundingBox pBox, RandomSource pRandom, boolean pKeepJigsaws) {
      return this.feature.value().place(pLevel, pGenerator, pRandom, pOffset);
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.FEATURE;
   }

   public String toString() {
      return "Feature[" + this.feature + "]";
   }
}