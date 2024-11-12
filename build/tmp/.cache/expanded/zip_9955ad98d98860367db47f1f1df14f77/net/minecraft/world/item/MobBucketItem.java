package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class MobBucketItem extends BucketItem {
   private final java.util.function.Supplier<? extends EntityType<?>> entityTypeSupplier;
   private final java.util.function.Supplier<? extends SoundEvent> emptySoundSupplier;

   @Deprecated
   public MobBucketItem(EntityType<?> pType, Fluid pContent, SoundEvent pEmptySound, Item.Properties pProperties) {
      this(() -> pType, () -> pContent, () -> pEmptySound, pProperties);
   }

   public MobBucketItem(java.util.function.Supplier<? extends EntityType<?>> entitySupplier, java.util.function.Supplier<? extends Fluid> fluidSupplier, java.util.function.Supplier<? extends SoundEvent> soundSupplier, Item.Properties properties) {
      super(fluidSupplier, properties);
      this.emptySoundSupplier = soundSupplier;
      this.entityTypeSupplier = entitySupplier;
   }

   public void checkExtraContent(@Nullable Player pPlayer, Level pLevel, ItemStack pContainerStack, BlockPos pPos) {
      if (pLevel instanceof ServerLevel) {
         this.spawn((ServerLevel)pLevel, pContainerStack, pPos);
         pLevel.gameEvent(pPlayer, GameEvent.ENTITY_PLACE, pPos);
      }

   }

   protected void playEmptySound(@Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos) {
      pLevel.playSound(pPlayer, pPos, getEmptySound(), SoundSource.NEUTRAL, 1.0F, 1.0F);
   }

   private void spawn(ServerLevel pServerLevel, ItemStack pBucketedMobStack, BlockPos pPos) {
      Entity entity = getFishType().spawn(pServerLevel, pBucketedMobStack, (Player)null, pPos, MobSpawnType.BUCKET, true, false);
      if (entity instanceof Bucketable bucketable) {
         bucketable.loadFromBucketTag(pBucketedMobStack.getOrCreateTag());
         bucketable.setFromBucket(true);
      }

   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
      if (getFishType() == EntityType.TROPICAL_FISH) {
         CompoundTag compoundtag = pStack.getTag();
         if (compoundtag != null && compoundtag.contains("BucketVariantTag", 3)) {
            int i = compoundtag.getInt("BucketVariantTag");
            ChatFormatting[] achatformatting = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
            String s = "color.minecraft." + TropicalFish.getBaseColor(i);
            String s1 = "color.minecraft." + TropicalFish.getPatternColor(i);

            for(int j = 0; j < TropicalFish.COMMON_VARIANTS.size(); ++j) {
               if (i == TropicalFish.COMMON_VARIANTS.get(j).getPackedId()) {
                  pTooltipComponents.add(Component.translatable(TropicalFish.getPredefinedName(j)).withStyle(achatformatting));
                  return;
               }
            }

            pTooltipComponents.add(TropicalFish.getPattern(i).displayName().plainCopy().withStyle(achatformatting));
            MutableComponent mutablecomponent = Component.translatable(s);
            if (!s.equals(s1)) {
               mutablecomponent.append(", ").append(Component.translatable(s1));
            }

            mutablecomponent.withStyle(achatformatting);
            pTooltipComponents.add(mutablecomponent);
         }
      }

   }

   // Forge Start
   protected EntityType<?> getFishType() {
      return entityTypeSupplier.get();
   }

   protected SoundEvent getEmptySound() {
      return emptySoundSupplier.get();
   }
}
