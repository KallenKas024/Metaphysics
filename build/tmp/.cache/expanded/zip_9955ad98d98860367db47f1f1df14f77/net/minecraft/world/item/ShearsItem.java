package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShearsItem extends Item {
   public ShearsItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when a {@link net.minecraft.world.level.block.Block} is destroyed using this Item. Return {@code true} to
    * trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      if (!pLevel.isClientSide && !pState.is(BlockTags.FIRE)) {
         pStack.hurtAndBreak(1, pEntityLiving, (p_43076_) -> {
            p_43076_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
         });
      }

      return !pState.is(BlockTags.LEAVES) && !pState.is(Blocks.COBWEB) && !pState.is(Blocks.GRASS) && !pState.is(Blocks.FERN) && !pState.is(Blocks.DEAD_BUSH) && !pState.is(Blocks.HANGING_ROOTS) && !pState.is(Blocks.VINE) && !pState.is(Blocks.TRIPWIRE) && !pState.is(BlockTags.WOOL) ? super.mineBlock(pStack, pLevel, pState, pPos, pEntityLiving) : true;
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      return pBlock.is(Blocks.COBWEB) || pBlock.is(Blocks.REDSTONE_WIRE) || pBlock.is(Blocks.TRIPWIRE);
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      if (!pState.is(Blocks.COBWEB) && !pState.is(BlockTags.LEAVES)) {
         if (pState.is(BlockTags.WOOL)) {
            return 5.0F;
         } else {
            return !pState.is(Blocks.VINE) && !pState.is(Blocks.GLOW_LICHEN) ? super.getDestroySpeed(pStack, pState) : 2.0F;
         }
      } else {
         return 15.0F;
      }
   }

   /**
    * Try interacting with given entity. Return {@code InteractionResult.PASS} if nothing should happen.
    */
   @Override
   public net.minecraft.world.InteractionResult interactLivingEntity(ItemStack stack, net.minecraft.world.entity.player.Player playerIn, LivingEntity entity, net.minecraft.world.InteractionHand hand) {
      if (entity instanceof net.minecraftforge.common.IForgeShearable target) {
         if (entity.level().isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
         BlockPos pos = BlockPos.containing(entity.position());
         if (target.isShearable(stack, entity.level(), pos)) {
            java.util.List<ItemStack> drops = target.onSheared(playerIn, stack, entity.level(), pos,
                    net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE, stack));
            java.util.Random rand = new java.util.Random();
            drops.forEach(d -> {
               net.minecraft.world.entity.item.ItemEntity ent = entity.spawnAtLocation(d, 1.0F);
               ent.setDeltaMovement(ent.getDeltaMovement().add((double)((rand.nextFloat() - rand.nextFloat()) * 0.1F), (double)(rand.nextFloat() * 0.05F), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
            });
            stack.hurtAndBreak(1, playerIn, e -> e.broadcastBreakEvent(hand));
         }
         return net.minecraft.world.InteractionResult.SUCCESS;
      }
      return net.minecraft.world.InteractionResult.PASS;
   }

   @Override
   public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
      return net.minecraftforge.common.ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (block instanceof GrowingPlantHeadBlock growingplantheadblock) {
         if (!growingplantheadblock.isMaxAge(blockstate)) {
            Player player = pContext.getPlayer();
            ItemStack itemstack = pContext.getItemInHand();
            if (player instanceof ServerPlayer) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
            }

            level.playSound(player, blockpos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockState blockstate1 = growingplantheadblock.getMaxAgeState(blockstate);
            level.setBlockAndUpdate(blockpos, blockstate1);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(pContext.getPlayer(), blockstate1));
            if (player != null) {
               itemstack.hurtAndBreak(1, player, (p_186374_) -> {
                  p_186374_.broadcastBreakEvent(pContext.getHand());
               });
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }

      return super.useOn(pContext);
   }
}
