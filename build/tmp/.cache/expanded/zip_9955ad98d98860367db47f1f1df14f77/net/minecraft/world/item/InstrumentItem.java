package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {
   private static final String TAG_INSTRUMENT = "instrument";
   private final TagKey<Instrument> instruments;

   public InstrumentItem(Item.Properties pProperties, TagKey<Instrument> pInstruments) {
      super(pProperties);
      this.instruments = pInstruments;
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
      super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
      Optional<ResourceKey<Instrument>> optional = this.getInstrument(pStack).flatMap(Holder::unwrapKey);
      if (optional.isPresent()) {
         MutableComponent mutablecomponent = Component.translatable(Util.makeDescriptionId("instrument", optional.get().location()));
         pTooltipComponents.add(mutablecomponent.withStyle(ChatFormatting.GRAY));
      }

   }

   public static ItemStack create(Item pItem, Holder<Instrument> pInstrument) {
      ItemStack itemstack = new ItemStack(pItem);
      setSoundVariantId(itemstack, pInstrument);
      return itemstack;
   }

   public static void setRandom(ItemStack pStack, TagKey<Instrument> pInstrumentTag, RandomSource pRandom) {
      Optional<Holder<Instrument>> optional = BuiltInRegistries.INSTRUMENT.getTag(pInstrumentTag).flatMap((p_220103_) -> {
         return p_220103_.getRandomElement(pRandom);
      });
      optional.ifPresent((p_248417_) -> {
         setSoundVariantId(pStack, p_248417_);
      });
   }

   private static void setSoundVariantId(ItemStack pStack, Holder<Instrument> pSoundVariantId) {
      CompoundTag compoundtag = pStack.getOrCreateTag();
      compoundtag.putString("instrument", pSoundVariantId.unwrapKey().orElseThrow(() -> {
         return new IllegalStateException("Invalid instrument");
      }).location().toString());
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
      Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack);
      if (optional.isPresent()) {
         Instrument instrument = optional.get().value();
         pPlayer.startUsingItem(pUsedHand);
         play(pLevel, pPlayer, instrument);
         pPlayer.getCooldowns().addCooldown(this, instrument.useDuration());
         pPlayer.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResultHolder.consume(itemstack);
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      Optional<? extends Holder<Instrument>> optional = this.getInstrument(pStack);
      return optional.map((p_248418_) -> {
         return p_248418_.value().useDuration();
      }).orElse(0);
   }

   private Optional<? extends Holder<Instrument>> getInstrument(ItemStack pStack) {
      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null && compoundtag.contains("instrument", 8)) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(compoundtag.getString("instrument"));
         if (resourcelocation != null) {
            return BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, resourcelocation));
         }
      }

      Iterator<Holder<Instrument>> iterator = BuiltInRegistries.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
      return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.TOOT_HORN;
   }

   private static void play(Level pLevel, Player pPlayer, Instrument pInstrument) {
      SoundEvent soundevent = pInstrument.soundEvent().value();
      float f = pInstrument.range() / 16.0F;
      pLevel.playSound(pPlayer, pPlayer, soundevent, SoundSource.RECORDS, f, 1.0F);
      pLevel.gameEvent(GameEvent.INSTRUMENT_PLAY, pPlayer.position(), GameEvent.Context.of(pPlayer));
   }
}