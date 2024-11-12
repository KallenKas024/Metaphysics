package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

public class CompassItem extends Item implements Vanishable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String TAG_LODESTONE_POS = "LodestonePos";
   public static final String TAG_LODESTONE_DIMENSION = "LodestoneDimension";
   public static final String TAG_LODESTONE_TRACKED = "LodestoneTracked";

   public CompassItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public static boolean isLodestoneCompass(ItemStack pStack) {
      CompoundTag compoundtag = pStack.getTag();
      return compoundtag != null && (compoundtag.contains("LodestoneDimension") || compoundtag.contains("LodestonePos"));
   }

   private static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag pCompoundTag) {
      return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, pCompoundTag.get("LodestoneDimension")).result();
   }

   @Nullable
   public static GlobalPos getLodestonePosition(CompoundTag pTag) {
      boolean flag = pTag.contains("LodestonePos");
      boolean flag1 = pTag.contains("LodestoneDimension");
      if (flag && flag1) {
         Optional<ResourceKey<Level>> optional = getLodestoneDimension(pTag);
         if (optional.isPresent()) {
            BlockPos blockpos = NbtUtils.readBlockPos(pTag.getCompound("LodestonePos"));
            return GlobalPos.of(optional.get(), blockpos);
         }
      }

      return null;
   }

   @Nullable
   public static GlobalPos getSpawnPosition(Level pLevel) {
      return pLevel.dimensionType().natural() ? GlobalPos.of(pLevel.dimension(), pLevel.getSharedSpawnPos()) : null;
   }

   /**
    * Returns {@code true} if this item has an enchantment glint. By default, this returns
    * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
    * true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return isLodestoneCompass(pStack) || super.isFoil(pStack);
   }

   /**
    * Called each tick as long the item is in a player's inventory. Used by maps to check if it's in a player's hand and
    * update its contents.
    */
   public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
      if (!pLevel.isClientSide) {
         if (isLodestoneCompass(pStack)) {
            CompoundTag compoundtag = pStack.getOrCreateTag();
            if (compoundtag.contains("LodestoneTracked") && !compoundtag.getBoolean("LodestoneTracked")) {
               return;
            }

            Optional<ResourceKey<Level>> optional = getLodestoneDimension(compoundtag);
            if (optional.isPresent() && optional.get() == pLevel.dimension() && compoundtag.contains("LodestonePos")) {
               BlockPos blockpos = NbtUtils.readBlockPos(compoundtag.getCompound("LodestonePos"));
               if (!pLevel.isInWorldBounds(blockpos) || !((ServerLevel)pLevel).getPoiManager().existsAtPosition(PoiTypes.LODESTONE, blockpos)) {
                  compoundtag.remove("LodestonePos");
               }
            }
         }

      }
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      Level level = pContext.getLevel();
      if (!level.getBlockState(blockpos).is(Blocks.LODESTONE)) {
         return super.useOn(pContext);
      } else {
         level.playSound((Player)null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
         Player player = pContext.getPlayer();
         ItemStack itemstack = pContext.getItemInHand();
         boolean flag = !player.getAbilities().instabuild && itemstack.getCount() == 1;
         if (flag) {
            this.addLodestoneTags(level.dimension(), blockpos, itemstack.getOrCreateTag());
         } else {
            ItemStack itemstack1 = new ItemStack(Items.COMPASS, 1);
            CompoundTag compoundtag = itemstack.hasTag() ? itemstack.getTag().copy() : new CompoundTag();
            itemstack1.setTag(compoundtag);
            if (!player.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            this.addLodestoneTags(level.dimension(), blockpos, compoundtag);
            if (!player.getInventory().add(itemstack1)) {
               player.drop(itemstack1, false);
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   private void addLodestoneTags(ResourceKey<Level> pLodestoneDimension, BlockPos pLodestonePos, CompoundTag pCompoundTag) {
      pCompoundTag.put("LodestonePos", NbtUtils.writeBlockPos(pLodestonePos));
      Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pLodestoneDimension).resultOrPartial(LOGGER::error).ifPresent((p_40731_) -> {
         pCompoundTag.put("LodestoneDimension", p_40731_);
      });
      pCompoundTag.putBoolean("LodestoneTracked", true);
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return isLodestoneCompass(pStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(pStack);
   }
}