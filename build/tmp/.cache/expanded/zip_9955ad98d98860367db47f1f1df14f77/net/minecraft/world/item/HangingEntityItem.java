package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends Item {
   private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
   private final EntityType<? extends HangingEntity> type;

   public HangingEntityItem(EntityType<? extends HangingEntity> pType, Item.Properties pProperties) {
      super(pProperties);
      this.type = pType;
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      Direction direction = pContext.getClickedFace();
      BlockPos blockpos1 = blockpos.relative(direction);
      Player player = pContext.getPlayer();
      ItemStack itemstack = pContext.getItemInHand();
      if (player != null && !this.mayPlace(player, direction, itemstack, blockpos1)) {
         return InteractionResult.FAIL;
      } else {
         Level level = pContext.getLevel();
         HangingEntity hangingentity;
         if (this.type == EntityType.PAINTING) {
            Optional<Painting> optional = Painting.create(level, blockpos1, direction);
            if (optional.isEmpty()) {
               return InteractionResult.CONSUME;
            }

            hangingentity = optional.get();
         } else if (this.type == EntityType.ITEM_FRAME) {
            hangingentity = new ItemFrame(level, blockpos1, direction);
         } else {
            if (this.type != EntityType.GLOW_ITEM_FRAME) {
               return InteractionResult.sidedSuccess(level.isClientSide);
            }

            hangingentity = new GlowItemFrame(level, blockpos1, direction);
         }

         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null) {
            EntityType.updateCustomEntityTag(level, player, hangingentity, compoundtag);
         }

         if (hangingentity.survives()) {
            if (!level.isClientSide) {
               hangingentity.playPlacementSound();
               level.gameEvent(player, GameEvent.ENTITY_PLACE, hangingentity.position());
               level.addFreshEntity(hangingentity);
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.CONSUME;
         }
      }
   }

   protected boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
      return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
      super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
      if (this.type == EntityType.PAINTING) {
         CompoundTag compoundtag = pStack.getTag();
         if (compoundtag != null && compoundtag.contains("EntityTag", 10)) {
            CompoundTag compoundtag1 = compoundtag.getCompound("EntityTag");
            Painting.loadVariant(compoundtag1).ifPresentOrElse((p_270767_) -> {
               p_270767_.unwrapKey().ifPresent((p_270217_) -> {
                  pTooltipComponents.add(Component.translatable(p_270217_.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
                  pTooltipComponents.add(Component.translatable(p_270217_.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
               });
               pTooltipComponents.add(Component.translatable("painting.dimensions", Mth.positiveCeilDiv(p_270767_.value().getWidth(), 16), Mth.positiveCeilDiv(p_270767_.value().getHeight(), 16)));
            }, () -> {
               pTooltipComponents.add(TOOLTIP_RANDOM_VARIANT);
            });
         } else if (pIsAdvanced.isCreative()) {
            pTooltipComponents.add(TOOLTIP_RANDOM_VARIANT);
         }
      }

   }
}