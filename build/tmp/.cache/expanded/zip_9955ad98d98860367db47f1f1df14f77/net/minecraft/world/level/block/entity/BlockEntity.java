package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class BlockEntity extends net.minecraftforge.common.capabilities.CapabilityProvider<BlockEntity> implements net.minecraftforge.common.extensions.IForgeBlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockEntityType<?> type;
   @Nullable
   protected Level level;
   protected final BlockPos worldPosition;
   protected boolean remove;
   private BlockState blockState;
   private CompoundTag customPersistentData;

   public BlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
      super(BlockEntity.class);
      this.type = pType;
      this.worldPosition = pPos.immutable();
      this.blockState = pBlockState;
      this.gatherCapabilities();
   }

   public static BlockPos getPosFromTag(CompoundTag pTag) {
      return new BlockPos(pTag.getInt("x"), pTag.getInt("y"), pTag.getInt("z"));
   }

   @Nullable
   public Level getLevel() {
      return this.level;
   }

   public void setLevel(Level pLevel) {
      this.level = pLevel;
   }

   /**
    * @return whether this BlockEntity's level has been set
    */
   public boolean hasLevel() {
      return this.level != null;
   }

   public void load(CompoundTag pTag) {
      if (pTag.contains("ForgeData")) this.customPersistentData = pTag.getCompound("ForgeData");
      if (getCapabilities() != null && pTag.contains("ForgeCaps")) deserializeCaps(pTag.getCompound("ForgeCaps"));
   }

   protected void saveAdditional(CompoundTag pTag) {
      if (this.customPersistentData != null) pTag.put("ForgeData", this.customPersistentData.copy());
      if (getCapabilities() != null) pTag.put("ForgeCaps", serializeCaps());
   }

   public final CompoundTag saveWithFullMetadata() {
      CompoundTag compoundtag = this.saveWithoutMetadata();
      this.saveMetadata(compoundtag);
      return compoundtag;
   }

   public final CompoundTag saveWithId() {
      CompoundTag compoundtag = this.saveWithoutMetadata();
      this.saveId(compoundtag);
      return compoundtag;
   }

   public final CompoundTag saveWithoutMetadata() {
      CompoundTag compoundtag = new CompoundTag();
      this.saveAdditional(compoundtag);
      return compoundtag;
   }

   private void saveId(CompoundTag pTag) {
      ResourceLocation resourcelocation = BlockEntityType.getKey(this.getType());
      if (resourcelocation == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         pTag.putString("id", resourcelocation.toString());
      }
   }

   public static void addEntityType(CompoundTag pTag, BlockEntityType<?> pEntityType) {
      pTag.putString("id", BlockEntityType.getKey(pEntityType).toString());
   }

   public void saveToItem(ItemStack pStack) {
      BlockItem.setBlockEntityData(pStack, this.getType(), this.saveWithoutMetadata());
   }

   private void saveMetadata(CompoundTag pTag) {
      this.saveId(pTag);
      pTag.putInt("x", this.worldPosition.getX());
      pTag.putInt("y", this.worldPosition.getY());
      pTag.putInt("z", this.worldPosition.getZ());
   }

   @Nullable
   public static BlockEntity loadStatic(BlockPos pPos, BlockState pState, CompoundTag pTag) {
      String s = pTag.getString("id");
      ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
      if (resourcelocation == null) {
         LOGGER.error("Block entity has invalid type: {}", (Object)s);
         return null;
      } else {
         return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).map((p_155240_) -> {
            try {
               return p_155240_.create(pPos, pState);
            } catch (Throwable throwable) {
               LOGGER.error("Failed to create block entity {}", s, throwable);
               return null;
            }
         }).map((p_155249_) -> {
            try {
               p_155249_.load(pTag);
               return p_155249_;
            } catch (Throwable throwable) {
               LOGGER.error("Failed to load data for block entity {}", s, throwable);
               return null;
            }
         }).orElseGet(() -> {
            LOGGER.warn("Skipping BlockEntity with id {}", (Object)s);
            return null;
         });
      }
   }

   /**
    * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
      if (this.level != null) {
         setChanged(this.level, this.worldPosition, this.blockState);
      }

   }

   protected static void setChanged(Level pLevel, BlockPos pPos, BlockState pState) {
      pLevel.blockEntityChanged(pPos);
      if (!pState.isAir()) {
         pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
      }

   }

   public BlockPos getBlockPos() {
      return this.worldPosition;
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return null;
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return new CompoundTag();
   }

   public boolean isRemoved() {
      return this.remove;
   }

   /**
    * Marks this {@code BlockEntity} as no longer valid (removed from the level).
    */
   public void setRemoved() {
      this.remove = true;
      this.invalidateCaps();
      requestModelDataUpdate();
   }

   @Override
   public void onChunkUnloaded() {
      this.invalidateCaps();
   }

   /**
    * Marks this {@code BlockEntity} as valid again (no longer removed from the level).
    */
   public void clearRemoved() {
      this.remove = false;
   }

   public boolean triggerEvent(int pId, int pType) {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory pReportCategory) {
      pReportCategory.setDetail("Name", () -> {
         return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
      });
      if (this.level != null) {
         CrashReportCategory.populateBlockDetails(pReportCategory, this.level, this.worldPosition, this.getBlockState());
         CrashReportCategory.populateBlockDetails(pReportCategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
      }
   }

   public boolean onlyOpCanSetNbt() {
      return false;
   }

   public BlockEntityType<?> getType() {
      return this.type;
   }

   @Override
   public CompoundTag getPersistentData() {
      if (this.customPersistentData == null)
         this.customPersistentData = new CompoundTag();
      return this.customPersistentData;
   }

   /** @deprecated */
   @Deprecated
   public void setBlockState(BlockState pBlockState) {
      this.blockState = pBlockState;
   }
}
