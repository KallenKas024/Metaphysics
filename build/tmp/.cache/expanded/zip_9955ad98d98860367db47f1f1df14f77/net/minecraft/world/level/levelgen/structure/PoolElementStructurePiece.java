package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class PoolElementStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final StructurePoolElement element;
   protected BlockPos position;
   private final int groundLevelDelta;
   protected final Rotation rotation;
   private final List<JigsawJunction> junctions = Lists.newArrayList();
   private final StructureTemplateManager structureTemplateManager;

   public PoolElementStructurePiece(StructureTemplateManager pStructureTemplateManager, StructurePoolElement pElement, BlockPos pPosition, int pGroundLevelDelta, Rotation pRotation, BoundingBox pBox) {
      super(StructurePieceType.JIGSAW, 0, pBox);
      this.structureTemplateManager = pStructureTemplateManager;
      this.element = pElement;
      this.position = pPosition;
      this.groundLevelDelta = pGroundLevelDelta;
      this.rotation = pRotation;
   }

   public PoolElementStructurePiece(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      super(StructurePieceType.JIGSAW, pTag);
      this.structureTemplateManager = pContext.structureTemplateManager();
      this.position = new BlockPos(pTag.getInt("PosX"), pTag.getInt("PosY"), pTag.getInt("PosZ"));
      this.groundLevelDelta = pTag.getInt("ground_level_delta");
      DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, pContext.registryAccess());
      this.element = StructurePoolElement.CODEC.parse(dynamicops, pTag.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElseThrow(() -> {
         return new IllegalStateException("Invalid pool element found");
      });
      this.rotation = Rotation.valueOf(pTag.getString("rotation"));
      this.boundingBox = this.element.getBoundingBox(this.structureTemplateManager, this.position, this.rotation);
      ListTag listtag = pTag.getList("junctions", 10);
      this.junctions.clear();
      listtag.forEach((p_204943_) -> {
         this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(dynamicops, p_204943_)));
      });
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      pTag.putInt("PosX", this.position.getX());
      pTag.putInt("PosY", this.position.getY());
      pTag.putInt("PosZ", this.position.getZ());
      pTag.putInt("ground_level_delta", this.groundLevelDelta);
      DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, pContext.registryAccess());
      StructurePoolElement.CODEC.encodeStart(dynamicops, this.element).resultOrPartial(LOGGER::error).ifPresent((p_163125_) -> {
         pTag.put("pool_element", p_163125_);
      });
      pTag.putString("rotation", this.rotation.name());
      ListTag listtag = new ListTag();

      for(JigsawJunction jigsawjunction : this.junctions) {
         listtag.add(jigsawjunction.serialize(dynamicops).getValue());
      }

      pTag.put("junctions", listtag);
   }

   public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
      this.place(pLevel, pStructureManager, pGenerator, pRandom, pBox, pPos, false);
   }

   public void place(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, BlockPos pPos, boolean pKeepJigsaws) {
      this.element.place(this.structureTemplateManager, pLevel, pStructureManager, pGenerator, this.position, pPos, this.rotation, pBox, pRandom, pKeepJigsaws);
   }

   public void move(int pX, int pY, int pZ) {
      super.move(pX, pY, pZ);
      this.position = this.position.offset(pX, pY, pZ);
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String toString() {
      return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
   }

   public StructurePoolElement getElement() {
      return this.element;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public int getGroundLevelDelta() {
      return this.groundLevelDelta;
   }

   public void addJunction(JigsawJunction pJunction) {
      this.junctions.add(pJunction);
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}