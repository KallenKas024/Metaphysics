package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {
   private static final int SONG_END_PADDING = 20;
   private final NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   private int ticksSinceLastEvent;
   private long tickCount;
   private long recordStartedTick;
   private boolean isPlaying;

   public JukeboxBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.JUKEBOX, pPos, pBlockState);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("RecordItem", 10)) {
         this.items.set(0, ItemStack.of(pTag.getCompound("RecordItem")));
      }

      this.isPlaying = pTag.getBoolean("IsPlaying");
      this.recordStartedTick = pTag.getLong("RecordStartTick");
      this.tickCount = pTag.getLong("TickCount");
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.getFirstItem().isEmpty()) {
         pTag.put("RecordItem", this.getFirstItem().save(new CompoundTag()));
      }

      pTag.putBoolean("IsPlaying", this.isPlaying);
      pTag.putLong("RecordStartTick", this.recordStartedTick);
      pTag.putLong("TickCount", this.tickCount);
   }

   public boolean isRecordPlaying() {
      return !this.getFirstItem().isEmpty() && this.isPlaying;
   }

   private void setHasRecordBlockState(@Nullable Entity pEntity, boolean pHasRecord) {
      if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
         this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(pHasRecord)), 2);
         this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(pEntity, this.getBlockState()));
      }

   }

   @VisibleForTesting
   public void startPlaying() {
      this.recordStartedTick = this.tickCount;
      this.isPlaying = true;
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.level.levelEvent((Player)null, 1010, this.getBlockPos(), Item.getId(this.getFirstItem().getItem()));
      this.setChanged();
   }

   private void stopPlaying() {
      this.isPlaying = false;
      this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.level.levelEvent(1011, this.getBlockPos(), 0);
      this.setChanged();
   }

   private void tick(Level pLevel, BlockPos pPos, BlockState pState) {
      ++this.ticksSinceLastEvent;
      if (this.isRecordPlaying()) {
         Item item = this.getFirstItem().getItem();
         if (item instanceof RecordItem) {
            RecordItem recorditem = (RecordItem)item;
            if (this.shouldRecordStopPlaying(recorditem)) {
               this.stopPlaying();
            } else if (this.shouldSendJukeboxPlayingEvent()) {
               this.ticksSinceLastEvent = 0;
               pLevel.gameEvent(GameEvent.JUKEBOX_PLAY, pPos, GameEvent.Context.of(pState));
               this.spawnMusicParticles(pLevel, pPos);
            }
         }
      }

      ++this.tickCount;
   }

   private boolean shouldRecordStopPlaying(RecordItem pRecord) {
      return this.tickCount >= this.recordStartedTick + (long)pRecord.getLengthInTicks() + 20L;
   }

   private boolean shouldSendJukeboxPlayingEvent() {
      return this.ticksSinceLastEvent >= 20;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pSlot) {
      return this.items.get(pSlot);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pSlot, int pAmount) {
      ItemStack itemstack = Objects.requireNonNullElse(this.items.get(pSlot), ItemStack.EMPTY);
      this.items.set(pSlot, ItemStack.EMPTY);
      if (!itemstack.isEmpty()) {
         this.setHasRecordBlockState((Entity)null, false);
         this.stopPlaying();
      }

      return itemstack;
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pSlot, ItemStack pStack) {
      if (pStack.is(ItemTags.MUSIC_DISCS) && this.level != null) {
         this.items.set(pSlot, pStack);
         this.setHasRecordBlockState((Entity)null, true);
         this.startPlaying();
      }

   }

   /**
    * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
    */
   public int getMaxStackSize() {
      return 1;
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      return Container.stillValidBlockEntity(this, pPlayer);
   }

   /**
    * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
    * For guis use Slot.isItemValid
    */
   public boolean canPlaceItem(int pIndex, ItemStack pStack) {
      return pStack.is(ItemTags.MUSIC_DISCS) && this.getItem(pIndex).isEmpty();
   }

   /**
    * {@return {@code true} if the given stack can be extracted into the target inventory}
    * @param pTarget the container into which the item should be extracted
    * @param pIndex the slot from which to extract the item
    * @param pStack the item to extract
    */
   public boolean canTakeItem(Container pTarget, int pIndex, ItemStack pStack) {
      return pTarget.hasAnyMatching(ItemStack::isEmpty);
   }

   private void spawnMusicParticles(Level pLevel, BlockPos pPos) {
      if (pLevel instanceof ServerLevel serverlevel) {
         Vec3 vec3 = Vec3.atBottomCenterOf(pPos).add(0.0D, (double)1.2F, 0.0D);
         float f = (float)pLevel.getRandom().nextInt(4) / 24.0F;
         serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0D, 0.0D, 1.0D);
      }

   }

   public void popOutRecord() {
      if (this.level != null && !this.level.isClientSide) {
         BlockPos blockpos = this.getBlockPos();
         ItemStack itemstack = this.getFirstItem();
         if (!itemstack.isEmpty()) {
            this.removeFirstItem();
            Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5D, 1.01D, 0.5D).offsetRandom(this.level.random, 0.7F);
            ItemStack itemstack1 = itemstack.copy();
            ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
            itementity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itementity);
         }
      }
   }

   public static void playRecordTick(Level pLevel, BlockPos pPos, BlockState pState, JukeboxBlockEntity pJukebox) {
      pJukebox.tick(pLevel, pPos, pState);
   }

   @VisibleForTesting
   public void setRecordWithoutPlaying(ItemStack pStack) {
      this.items.set(0, pStack);
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.setChanged();
   }
}