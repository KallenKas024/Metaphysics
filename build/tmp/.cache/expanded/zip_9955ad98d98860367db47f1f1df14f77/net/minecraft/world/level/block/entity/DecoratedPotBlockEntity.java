package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecoratedPotBlockEntity extends BlockEntity {
   public static final String TAG_SHERDS = "sherds";
   private DecoratedPotBlockEntity.Decorations decorations = DecoratedPotBlockEntity.Decorations.EMPTY;

   public DecoratedPotBlockEntity(BlockPos pPos, BlockState pState) {
      super(BlockEntityType.DECORATED_POT, pPos, pState);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      this.decorations.save(pTag);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.decorations = DecoratedPotBlockEntity.Decorations.load(pTag);
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

   public Direction getDirection() {
      return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
   }

   public DecoratedPotBlockEntity.Decorations getDecorations() {
      return this.decorations;
   }

   public void setFromItem(ItemStack pItem) {
      this.decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(pItem));
   }

   public static record Decorations(Item back, Item left, Item right, Item front) {
      public static final DecoratedPotBlockEntity.Decorations EMPTY = new DecoratedPotBlockEntity.Decorations(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);

      public CompoundTag save(CompoundTag pTag) {
         ListTag listtag = new ListTag();
         this.sorted().forEach((p_285298_) -> {
            listtag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(p_285298_).toString()));
         });
         pTag.put("sherds", listtag);
         return pTag;
      }

      public Stream<Item> sorted() {
         return Stream.of(this.back, this.left, this.right, this.front);
      }

      public static DecoratedPotBlockEntity.Decorations load(@Nullable CompoundTag pTag) {
         if (pTag != null && pTag.contains("sherds", 9)) {
            ListTag listtag = pTag.getList("sherds", 8);
            return new DecoratedPotBlockEntity.Decorations(itemFromTag(listtag, 0), itemFromTag(listtag, 1), itemFromTag(listtag, 2), itemFromTag(listtag, 3));
         } else {
            return EMPTY;
         }
      }

      private static Item itemFromTag(ListTag pTag, int pIndex) {
         if (pIndex >= pTag.size()) {
            return Items.BRICK;
         } else {
            Tag tag = pTag.get(pIndex);
            return BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getAsString()));
         }
      }
   }
}