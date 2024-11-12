package net.minecraft.world.entity.animal;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface Bucketable {
   boolean fromBucket();

   void setFromBucket(boolean pFromBucket);

   void saveToBucketTag(ItemStack pStack);

   void loadFromBucketTag(CompoundTag pTag);

   ItemStack getBucketItemStack();

   SoundEvent getPickupSound();

   /** @deprecated */
   @Deprecated
   static void saveDefaultDataToBucketTag(Mob pMob, ItemStack pBucket) {
      CompoundTag compoundtag = pBucket.getOrCreateTag();
      if (pMob.hasCustomName()) {
         pBucket.setHoverName(pMob.getCustomName());
      }

      if (pMob.isNoAi()) {
         compoundtag.putBoolean("NoAI", pMob.isNoAi());
      }

      if (pMob.isSilent()) {
         compoundtag.putBoolean("Silent", pMob.isSilent());
      }

      if (pMob.isNoGravity()) {
         compoundtag.putBoolean("NoGravity", pMob.isNoGravity());
      }

      if (pMob.hasGlowingTag()) {
         compoundtag.putBoolean("Glowing", pMob.hasGlowingTag());
      }

      if (pMob.isInvulnerable()) {
         compoundtag.putBoolean("Invulnerable", pMob.isInvulnerable());
      }

      compoundtag.putFloat("Health", pMob.getHealth());
   }

   /** @deprecated */
   @Deprecated
   static void loadDefaultDataFromBucketTag(Mob pMob, CompoundTag pTag) {
      if (pTag.contains("NoAI")) {
         pMob.setNoAi(pTag.getBoolean("NoAI"));
      }

      if (pTag.contains("Silent")) {
         pMob.setSilent(pTag.getBoolean("Silent"));
      }

      if (pTag.contains("NoGravity")) {
         pMob.setNoGravity(pTag.getBoolean("NoGravity"));
      }

      if (pTag.contains("Glowing")) {
         pMob.setGlowingTag(pTag.getBoolean("Glowing"));
      }

      if (pTag.contains("Invulnerable")) {
         pMob.setInvulnerable(pTag.getBoolean("Invulnerable"));
      }

      if (pTag.contains("Health", 99)) {
         pMob.setHealth(pTag.getFloat("Health"));
      }

   }

   static <T extends LivingEntity & Bucketable> Optional<InteractionResult> bucketMobPickup(Player pPlayer, InteractionHand pHand, T pEntity) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() == Items.WATER_BUCKET && pEntity.isAlive()) {
         pEntity.playSound(pEntity.getPickupSound(), 1.0F, 1.0F);
         ItemStack itemstack1 = pEntity.getBucketItemStack();
         pEntity.saveToBucketTag(itemstack1);
         ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, pPlayer, itemstack1, false);
         pPlayer.setItemInHand(pHand, itemstack2);
         Level level = pEntity.level();
         if (!level.isClientSide) {
            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)pPlayer, itemstack1);
         }

         pEntity.discard();
         return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
      } else {
         return Optional.empty();
      }
   }
}