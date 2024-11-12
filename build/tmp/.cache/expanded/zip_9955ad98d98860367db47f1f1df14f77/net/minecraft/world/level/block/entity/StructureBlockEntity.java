package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureBlockEntity extends BlockEntity {
   private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
   public static final int MAX_OFFSET_PER_AXIS = 48;
   public static final int MAX_SIZE_PER_AXIS = 48;
   public static final String AUTHOR_TAG = "author";
   private ResourceLocation structureName;
   private String author = "";
   private String metaData = "";
   private BlockPos structurePos = new BlockPos(0, 1, 0);
   private Vec3i structureSize = Vec3i.ZERO;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private StructureMode mode;
   private boolean ignoreEntities = true;
   private boolean powered;
   private boolean showAir;
   private boolean showBoundingBox = true;
   private float integrity = 1.0F;
   private long seed;

   public StructureBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.STRUCTURE_BLOCK, pPos, pBlockState);
      this.mode = pBlockState.getValue(StructureBlock.MODE);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putString("name", this.getStructureName());
      pTag.putString("author", this.author);
      pTag.putString("metadata", this.metaData);
      pTag.putInt("posX", this.structurePos.getX());
      pTag.putInt("posY", this.structurePos.getY());
      pTag.putInt("posZ", this.structurePos.getZ());
      pTag.putInt("sizeX", this.structureSize.getX());
      pTag.putInt("sizeY", this.structureSize.getY());
      pTag.putInt("sizeZ", this.structureSize.getZ());
      pTag.putString("rotation", this.rotation.toString());
      pTag.putString("mirror", this.mirror.toString());
      pTag.putString("mode", this.mode.toString());
      pTag.putBoolean("ignoreEntities", this.ignoreEntities);
      pTag.putBoolean("powered", this.powered);
      pTag.putBoolean("showair", this.showAir);
      pTag.putBoolean("showboundingbox", this.showBoundingBox);
      pTag.putFloat("integrity", this.integrity);
      pTag.putLong("seed", this.seed);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.setStructureName(pTag.getString("name"));
      this.author = pTag.getString("author");
      this.metaData = pTag.getString("metadata");
      int i = Mth.clamp(pTag.getInt("posX"), -48, 48);
      int j = Mth.clamp(pTag.getInt("posY"), -48, 48);
      int k = Mth.clamp(pTag.getInt("posZ"), -48, 48);
      this.structurePos = new BlockPos(i, j, k);
      int l = Mth.clamp(pTag.getInt("sizeX"), 0, 48);
      int i1 = Mth.clamp(pTag.getInt("sizeY"), 0, 48);
      int j1 = Mth.clamp(pTag.getInt("sizeZ"), 0, 48);
      this.structureSize = new Vec3i(l, i1, j1);

      try {
         this.rotation = Rotation.valueOf(pTag.getString("rotation"));
      } catch (IllegalArgumentException illegalargumentexception2) {
         this.rotation = Rotation.NONE;
      }

      try {
         this.mirror = Mirror.valueOf(pTag.getString("mirror"));
      } catch (IllegalArgumentException illegalargumentexception1) {
         this.mirror = Mirror.NONE;
      }

      try {
         this.mode = StructureMode.valueOf(pTag.getString("mode"));
      } catch (IllegalArgumentException illegalargumentexception) {
         this.mode = StructureMode.DATA;
      }

      this.ignoreEntities = pTag.getBoolean("ignoreEntities");
      this.powered = pTag.getBoolean("powered");
      this.showAir = pTag.getBoolean("showair");
      this.showBoundingBox = pTag.getBoolean("showboundingbox");
      if (pTag.contains("integrity")) {
         this.integrity = pTag.getFloat("integrity");
      } else {
         this.integrity = 1.0F;
      }

      this.seed = pTag.getLong("seed");
      this.updateBlockState();
   }

   private void updateBlockState() {
      if (this.level != null) {
         BlockPos blockpos = this.getBlockPos();
         BlockState blockstate = this.level.getBlockState(blockpos);
         if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(blockpos, blockstate.setValue(StructureBlock.MODE, this.mode), 2);
         }

      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public boolean usedBy(Player pPlayer) {
      if (!pPlayer.canUseGameMasterBlocks()) {
         return false;
      } else {
         if (pPlayer.getCommandSenderWorld().isClientSide) {
            pPlayer.openStructureBlock(this);
         }

         return true;
      }
   }

   public String getStructureName() {
      return this.structureName == null ? "" : this.structureName.toString();
   }

   public String getStructurePath() {
      return this.structureName == null ? "" : this.structureName.getPath();
   }

   public boolean hasStructureName() {
      return this.structureName != null;
   }

   public void setStructureName(@Nullable String pStructureName) {
      this.setStructureName(StringUtil.isNullOrEmpty(pStructureName) ? null : ResourceLocation.tryParse(pStructureName));
   }

   public void setStructureName(@Nullable ResourceLocation pStructureName) {
      this.structureName = pStructureName;
   }

   public void createdBy(LivingEntity pAuthor) {
      this.author = pAuthor.getName().getString();
   }

   public BlockPos getStructurePos() {
      return this.structurePos;
   }

   public void setStructurePos(BlockPos pStructurePos) {
      this.structurePos = pStructurePos;
   }

   public Vec3i getStructureSize() {
      return this.structureSize;
   }

   public void setStructureSize(Vec3i pStructureSize) {
      this.structureSize = pStructureSize;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public void setMirror(Mirror pMirror) {
      this.mirror = pMirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public void setRotation(Rotation pRotation) {
      this.rotation = pRotation;
   }

   public String getMetaData() {
      return this.metaData;
   }

   public void setMetaData(String pMetaData) {
      this.metaData = pMetaData;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public void setMode(StructureMode pMode) {
      this.mode = pMode;
      BlockState blockstate = this.level.getBlockState(this.getBlockPos());
      if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
         this.level.setBlock(this.getBlockPos(), blockstate.setValue(StructureBlock.MODE, pMode), 2);
      }

   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public void setIgnoreEntities(boolean pIgnoreEntities) {
      this.ignoreEntities = pIgnoreEntities;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public void setIntegrity(float pIntegrity) {
      this.integrity = pIntegrity;
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long pSeed) {
      this.seed = pSeed;
   }

   public boolean detectSize() {
      if (this.mode != StructureMode.SAVE) {
         return false;
      } else {
         BlockPos blockpos = this.getBlockPos();
         int i = 80;
         BlockPos blockpos1 = new BlockPos(blockpos.getX() - 80, this.level.getMinBuildHeight(), blockpos.getZ() - 80);
         BlockPos blockpos2 = new BlockPos(blockpos.getX() + 80, this.level.getMaxBuildHeight() - 1, blockpos.getZ() + 80);
         Stream<BlockPos> stream = this.getRelatedCorners(blockpos1, blockpos2);
         return calculateEnclosingBoundingBox(blockpos, stream).filter((p_155790_) -> {
            int j = p_155790_.maxX() - p_155790_.minX();
            int k = p_155790_.maxY() - p_155790_.minY();
            int l = p_155790_.maxZ() - p_155790_.minZ();
            if (j > 1 && k > 1 && l > 1) {
               this.structurePos = new BlockPos(p_155790_.minX() - blockpos.getX() + 1, p_155790_.minY() - blockpos.getY() + 1, p_155790_.minZ() - blockpos.getZ() + 1);
               this.structureSize = new Vec3i(j - 1, k - 1, l - 1);
               this.setChanged();
               BlockState blockstate = this.level.getBlockState(blockpos);
               this.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
               return true;
            } else {
               return false;
            }
         }).isPresent();
      }
   }

   private Stream<BlockPos> getRelatedCorners(BlockPos pMinPos, BlockPos pMaxPos) {
      return BlockPos.betweenClosedStream(pMinPos, pMaxPos).filter((p_272561_) -> {
         return this.level.getBlockState(p_272561_).is(Blocks.STRUCTURE_BLOCK);
      }).map(this.level::getBlockEntity).filter((p_155802_) -> {
         return p_155802_ instanceof StructureBlockEntity;
      }).map((p_155785_) -> {
         return (StructureBlockEntity)p_155785_;
      }).filter((p_155787_) -> {
         return p_155787_.mode == StructureMode.CORNER && Objects.equals(this.structureName, p_155787_.structureName);
      }).map(BlockEntity::getBlockPos);
   }

   private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos pPos, Stream<BlockPos> pRelatedCorners) {
      Iterator<BlockPos> iterator = pRelatedCorners.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BlockPos blockpos = iterator.next();
         BoundingBox boundingbox = new BoundingBox(blockpos);
         if (iterator.hasNext()) {
            iterator.forEachRemaining(boundingbox::encapsulate);
         } else {
            boundingbox.encapsulate(pPos);
         }

         return Optional.of(boundingbox);
      }
   }

   /**
    * Saves the template, writing it to disk.
    * 
    * @return true if the template was successfully saved.
    */
   public boolean saveStructure() {
      return this.saveStructure(true);
   }

   /**
    * Saves the template, either updating the local version or writing it to disk.
    * 
    * @return true if the template was successfully saved.
    */
   public boolean saveStructure(boolean pWriteToDisk) {
      if (this.mode == StructureMode.SAVE && !this.level.isClientSide && this.structureName != null) {
         BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
         ServerLevel serverlevel = (ServerLevel)this.level;
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

         StructureTemplate structuretemplate;
         try {
            structuretemplate = structuretemplatemanager.getOrCreate(this.structureName);
         } catch (ResourceLocationException resourcelocationexception1) {
            return false;
         }

         structuretemplate.fillFromWorld(this.level, blockpos, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
         structuretemplate.setAuthor(this.author);
         if (pWriteToDisk) {
            try {
               return structuretemplatemanager.save(this.structureName);
            } catch (ResourceLocationException resourcelocationexception) {
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean loadStructure(ServerLevel pLevel) {
      return this.loadStructure(pLevel, true);
   }

   public static RandomSource createRandom(long pSeed) {
      return pSeed == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(pSeed);
   }

   /**
    * 
    * @param pDontPlaceOnSizeMismatch If true, the structure won't be placed if the structure block size doesn't match
    * the structure template size
    */
   public boolean loadStructure(ServerLevel pLevel, boolean pDontPlaceOnSizeMismatch) {
      if (this.mode == StructureMode.LOAD && this.structureName != null) {
         StructureTemplateManager structuretemplatemanager = pLevel.getStructureManager();

         Optional<StructureTemplate> optional;
         try {
            optional = structuretemplatemanager.get(this.structureName);
         } catch (ResourceLocationException resourcelocationexception) {
            return false;
         }

         return !optional.isPresent() ? false : this.loadStructure(pLevel, pDontPlaceOnSizeMismatch, optional.get());
      } else {
         return false;
      }
   }

   /**
    * 
    * @param pDontPlaceOnSizeMismatch If true, the structure won't be placed if the structure block size doesn't match
    * the structure template size
    */
   public boolean loadStructure(ServerLevel pLevel, boolean pDontPlaceOnSizeMismatch, StructureTemplate pTemplate) {
      BlockPos blockpos = this.getBlockPos();
      if (!StringUtil.isNullOrEmpty(pTemplate.getAuthor())) {
         this.author = pTemplate.getAuthor();
      }

      Vec3i vec3i = pTemplate.getSize();
      boolean flag = this.structureSize.equals(vec3i);
      if (!flag) {
         this.structureSize = vec3i;
         this.setChanged();
         BlockState blockstate = pLevel.getBlockState(blockpos);
         pLevel.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
      }

      if (pDontPlaceOnSizeMismatch && !flag) {
         return false;
      } else {
         StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
         if (this.integrity < 1.0F) {
            structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
         }

         BlockPos blockpos1 = blockpos.offset(this.structurePos);
         pTemplate.placeInWorld(pLevel, blockpos1, blockpos1, structureplacesettings, createRandom(this.seed), 2);
         return true;
      }
   }

   public void unloadStructure() {
      if (this.structureName != null) {
         ServerLevel serverlevel = (ServerLevel)this.level;
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();
         structuretemplatemanager.remove(this.structureName);
      }
   }

   public boolean isStructureLoadable() {
      if (this.mode == StructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
         ServerLevel serverlevel = (ServerLevel)this.level;
         StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

         try {
            return structuretemplatemanager.get(this.structureName).isPresent();
         } catch (ResourceLocationException resourcelocationexception) {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isPowered() {
      return this.powered;
   }

   public void setPowered(boolean pPowered) {
      this.powered = pPowered;
   }

   public boolean getShowAir() {
      return this.showAir;
   }

   public void setShowAir(boolean pShowAir) {
      this.showAir = pShowAir;
   }

   public boolean getShowBoundingBox() {
      return this.showBoundingBox;
   }

   public void setShowBoundingBox(boolean pShowBoundingBox) {
      this.showBoundingBox = pShowBoundingBox;
   }

   public static enum UpdateType {
      UPDATE_DATA,
      SAVE_AREA,
      LOAD_AREA,
      SCAN_AREA;
   }
}