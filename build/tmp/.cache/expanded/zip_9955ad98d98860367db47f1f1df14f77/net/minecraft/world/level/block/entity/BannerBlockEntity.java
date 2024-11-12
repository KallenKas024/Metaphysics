package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntity extends BlockEntity implements Nameable {
   public static final int MAX_PATTERNS = 6;
   public static final String TAG_PATTERNS = "Patterns";
   public static final String TAG_PATTERN = "Pattern";
   public static final String TAG_COLOR = "Color";
   @Nullable
   private Component name;
   private DyeColor baseColor;
   /** A list of all the banner patterns. */
   @Nullable
   private ListTag itemPatterns;
   /** A list of all patterns stored on this banner. */
   @Nullable
   private List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

   public BannerBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.BANNER, pPos, pBlockState);
      this.baseColor = ((AbstractBannerBlock)pBlockState.getBlock()).getColor();
   }

   public BannerBlockEntity(BlockPos pPos, BlockState pBlockState, DyeColor pBaseColor) {
      this(pPos, pBlockState);
      this.baseColor = pBaseColor;
   }

   @Nullable
   public static ListTag getItemPatterns(ItemStack pStack) {
      ListTag listtag = null;
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null && compoundtag.contains("Patterns", 9)) {
         listtag = compoundtag.getList("Patterns", 10).copy();
      }

      return listtag;
   }

   public void fromItem(ItemStack pStack, DyeColor pColor) {
      this.baseColor = pColor;
      this.fromItem(pStack);
   }

   public void fromItem(ItemStack pItem) {
      this.itemPatterns = getItemPatterns(pItem);
      this.patterns = null;
      this.name = pItem.hasCustomHoverName() ? pItem.getHoverName() : null;
   }

   public Component getName() {
      return (Component)(this.name != null ? this.name : Component.translatable("block.minecraft.banner"));
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }

   public void setCustomName(Component pName) {
      this.name = pName;
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (this.itemPatterns != null) {
         pTag.put("Patterns", this.itemPatterns);
      }

      if (this.name != null) {
         pTag.putString("CustomName", Component.Serializer.toJson(this.name));
      }

   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("CustomName", 8)) {
         this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
      }

      this.itemPatterns = pTag.getList("Patterns", 10);
      this.patterns = null;
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

   /**
    * @return the amount of patterns stored in the given ItemStack. Defaults to zero if none are stored.
    */
   public static int getPatternCount(ItemStack pStack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      return compoundtag != null && compoundtag.contains("Patterns") ? compoundtag.getList("Patterns", 10).size() : 0;
   }

   /**
    * @return the patterns for this banner.
    */
   public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns() {
      if (this.patterns == null) {
         this.patterns = createPatterns(this.baseColor, this.itemPatterns);
      }

      return this.patterns;
   }

   public static List<Pair<Holder<BannerPattern>, DyeColor>> createPatterns(DyeColor pColor, @Nullable ListTag pListTag) {
      List<Pair<Holder<BannerPattern>, DyeColor>> list = Lists.newArrayList();
      list.add(Pair.of(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(BannerPatterns.BASE), pColor));
      if (pListTag != null) {
         for(int i = 0; i < pListTag.size(); ++i) {
            CompoundTag compoundtag = pListTag.getCompound(i);
            Holder<BannerPattern> holder = BannerPattern.byHash(compoundtag.getString("Pattern"));
            if (holder != null) {
               int j = compoundtag.getInt("Color");
               list.add(Pair.of(holder, DyeColor.byId(j)));
            }
         }
      }

      return list;
   }

   /**
    * Removes all banner data from the given ItemStack.
    */
   public static void removeLastPattern(ItemStack pStack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null && compoundtag.contains("Patterns", 9)) {
         ListTag listtag = compoundtag.getList("Patterns", 10);
         if (!listtag.isEmpty()) {
            listtag.remove(listtag.size() - 1);
            if (listtag.isEmpty()) {
               compoundtag.remove("Patterns");
            }

            BlockItem.setBlockEntityData(pStack, BlockEntityType.BANNER, compoundtag);
         }
      }
   }

   public ItemStack getItem() {
      ItemStack itemstack = new ItemStack(BannerBlock.byColor(this.baseColor));
      if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.put("Patterns", this.itemPatterns.copy());
         BlockItem.setBlockEntityData(itemstack, this.getType(), compoundtag);
      }

      if (this.name != null) {
         itemstack.setHoverName(this.name);
      }

      return itemstack;
   }

   public DyeColor getBaseColor() {
      return this.baseColor;
   }
}