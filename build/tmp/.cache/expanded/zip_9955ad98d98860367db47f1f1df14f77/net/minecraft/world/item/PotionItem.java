package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends Item {
   private static final int DRINK_DURATION = 32;

   public PotionItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public ItemStack getDefaultInstance() {
      return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
      Player player = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;
      if (player instanceof ServerPlayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, pStack);
      }

      if (!pLevel.isClientSide) {
         for(MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(pStack)) {
            if (mobeffectinstance.getEffect().isInstantenous()) {
               mobeffectinstance.getEffect().applyInstantenousEffect(player, player, pEntityLiving, mobeffectinstance.getAmplifier(), 1.0D);
            } else {
               pEntityLiving.addEffect(new MobEffectInstance(mobeffectinstance));
            }
         }
      }

      if (player != null) {
         player.awardStat(Stats.ITEM_USED.get(this));
         if (!player.getAbilities().instabuild) {
            pStack.shrink(1);
         }
      }

      if (player == null || !player.getAbilities().instabuild) {
         if (pStack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if (player != null) {
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      pEntityLiving.gameEvent(GameEvent.DRINK);
      return pStack;
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      Player player = pContext.getPlayer();
      ItemStack itemstack = pContext.getItemInHand();
      BlockState blockstate = level.getBlockState(blockpos);
      if (pContext.getClickedFace() != Direction.DOWN && blockstate.is(BlockTags.CONVERTABLE_TO_MUD) && PotionUtils.getPotion(itemstack) == Potions.WATER) {
         level.playSound((Player)null, blockpos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
         player.setItemInHand(pContext.getHand(), ItemUtils.createFilledResult(itemstack, player, new ItemStack(Items.GLASS_BOTTLE)));
         player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
         if (!level.isClientSide) {
            ServerLevel serverlevel = (ServerLevel)level;

            for(int i = 0; i < 5; ++i) {
               serverlevel.sendParticles(ParticleTypes.SPLASH, (double)blockpos.getX() + level.random.nextDouble(), (double)(blockpos.getY() + 1), (double)blockpos.getZ() + level.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
            }
         }

         level.playSound((Player)null, blockpos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.gameEvent((Entity)null, GameEvent.FLUID_PLACE, blockpos);
         level.setBlockAndUpdate(blockpos, Blocks.MUD.defaultBlockState());
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 32;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.DRINK;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      PotionUtils.addPotionTooltip(pStack, pTooltip, 1.0F);
   }
}