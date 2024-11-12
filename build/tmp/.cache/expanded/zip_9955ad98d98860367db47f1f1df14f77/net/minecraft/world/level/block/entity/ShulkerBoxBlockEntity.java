package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
   public static final int COLUMNS = 9;
   public static final int ROWS = 3;
   public static final int CONTAINER_SIZE = 27;
   public static final int EVENT_SET_OPEN_COUNT = 1;
   public static final int OPENING_TICK_LENGTH = 10;
   public static final float MAX_LID_HEIGHT = 0.5F;
   public static final float MAX_LID_ROTATION = 270.0F;
   public static final String ITEMS_TAG = "Items";
   private static final int[] SLOTS = IntStream.range(0, 27).toArray();
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
   private int openCount;
   private ShulkerBoxBlockEntity.AnimationStatus animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
   private float progress;
   private float progressOld;
   @Nullable
   private final DyeColor color;

   public ShulkerBoxBlockEntity(@Nullable DyeColor pColor, BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.SHULKER_BOX, pPos, pBlockState);
      this.color = pColor;
   }

   public ShulkerBoxBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.SHULKER_BOX, pPos, pBlockState);
      this.color = ShulkerBoxBlock.getColorFromBlock(pBlockState.getBlock());
   }

   public static void tick(Level pLevel, BlockPos pPos, BlockState pState, ShulkerBoxBlockEntity pBlockEntity) {
      pBlockEntity.updateAnimation(pLevel, pPos, pState);
   }

   private void updateAnimation(Level pLevel, BlockPos pPos, BlockState pState) {
      this.progressOld = this.progress;
      switch (this.animationStatus) {
         case CLOSED:
            this.progress = 0.0F;
            break;
         case OPENING:
            this.progress += 0.1F;
            if (this.progress >= 1.0F) {
               this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENED;
               this.progress = 1.0F;
               doNeighborUpdates(pLevel, pPos, pState);
            }

            this.moveCollidedEntities(pLevel, pPos, pState);
            break;
         case CLOSING:
            this.progress -= 0.1F;
            if (this.progress <= 0.0F) {
               this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
               this.progress = 0.0F;
               doNeighborUpdates(pLevel, pPos, pState);
            }
            break;
         case OPENED:
            this.progress = 1.0F;
      }

   }

   public ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus() {
      return this.animationStatus;
   }

   public AABB getBoundingBox(BlockState pState) {
      return Shulker.getProgressAabb(pState.getValue(ShulkerBoxBlock.FACING), 0.5F * this.getProgress(1.0F));
   }

   private void moveCollidedEntities(Level pLevel, BlockPos pPos, BlockState pState) {
      if (pState.getBlock() instanceof ShulkerBoxBlock) {
         Direction direction = pState.getValue(ShulkerBoxBlock.FACING);
         AABB aabb = Shulker.getProgressDeltaAabb(direction, this.progressOld, this.progress).move(pPos);
         List<Entity> list = pLevel.getEntities((Entity)null, aabb);
         if (!list.isEmpty()) {
            for(int i = 0; i < list.size(); ++i) {
               Entity entity = list.get(i);
               if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                  entity.move(MoverType.SHULKER_BOX, new Vec3((aabb.getXsize() + 0.01D) * (double)direction.getStepX(), (aabb.getYsize() + 0.01D) * (double)direction.getStepY(), (aabb.getZsize() + 0.01D) * (double)direction.getStepZ()));
               }
            }

         }
      }
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.itemStacks.size();
   }

   public boolean triggerEvent(int pId, int pType) {
      if (pId == 1) {
         this.openCount = pType;
         if (pType == 0) {
            this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSING;
            doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
         }

         if (pType == 1) {
            this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENING;
            doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
         }

         return true;
      } else {
         return super.triggerEvent(pId, pType);
      }
   }

   private static void doNeighborUpdates(Level pLevel, BlockPos pPos, BlockState pState) {
      pState.updateNeighbourShapes(pLevel, pPos, 3);
   }

   public void startOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         if (this.openCount < 0) {
            this.openCount = 0;
         }

         ++this.openCount;
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
         if (this.openCount == 1) {
            this.level.gameEvent(pPlayer, GameEvent.CONTAINER_OPEN, this.worldPosition);
            this.level.playSound((Player)null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }
      }

   }

   public void stopOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         --this.openCount;
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
         if (this.openCount <= 0) {
            this.level.gameEvent(pPlayer, GameEvent.CONTAINER_CLOSE, this.worldPosition);
            this.level.playSound((Player)null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }
      }

   }

   protected Component getDefaultName() {
      return Component.translatable("container.shulkerBox");
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.loadFromTag(pTag);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.trySaveLootTable(pTag)) {
         ContainerHelper.saveAllItems(pTag, this.itemStacks, false);
      }

   }

   public void loadFromTag(CompoundTag pTag) {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(pTag) && pTag.contains("Items", 9)) {
         ContainerHelper.loadAllItems(pTag, this.itemStacks);
      }

   }

   protected NonNullList<ItemStack> getItems() {
      return this.itemStacks;
   }

   protected void setItems(NonNullList<ItemStack> pItems) {
      this.itemStacks = pItems;
   }

   public int[] getSlotsForFace(Direction pSide) {
      return SLOTS;
   }

   /**
    * Returns {@code true} if automation can insert the given item in the given slot from the given side.
    */
   public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
      return !(Block.byItem(pItemStack.getItem()) instanceof ShulkerBoxBlock) && pItemStack.getItem().canFitInsideContainerItems(); // FORGE: Make shulker boxes respect Item#canFitInsideContainerItems
   }

   /**
    * Returns {@code true} if automation can extract the given item in the given slot from the given side.
    */
   public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
      return true;
   }

   public float getProgress(float pPartialTicks) {
      return Mth.lerp(pPartialTicks, this.progressOld, this.progress);
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
      return new ShulkerBoxMenu(pId, pPlayer, this);
   }

   public boolean isClosed() {
      return this.animationStatus == ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
   }

   @Override
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.wrapper.SidedInvWrapper(this, Direction.UP);
   }

   public static enum AnimationStatus {
      CLOSED,
      OPENING,
      OPENED,
      CLOSING;
   }
}
