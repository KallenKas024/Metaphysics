package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem extends Item {
   public DebugStickItem(Item.Properties pProperties) {
      super(pProperties);
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
      return true;
   }

   public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      if (!pLevel.isClientSide) {
         this.handleInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(InteractionHand.MAIN_HAND));
      }

      return false;
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Player player = pContext.getPlayer();
      Level level = pContext.getLevel();
      if (!level.isClientSide && player != null) {
         BlockPos blockpos = pContext.getClickedPos();
         if (!this.handleInteraction(player, level.getBlockState(blockpos), level, blockpos, true, pContext.getItemInHand())) {
            return InteractionResult.FAIL;
         }
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   private boolean handleInteraction(Player pPlayer, BlockState pStateClicked, LevelAccessor pAccessor, BlockPos pPos, boolean pShouldCycleState, ItemStack pDebugStack) {
      if (!pPlayer.canUseGameMasterBlocks()) {
         return false;
      } else {
         Block block = pStateClicked.getBlock();
         StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
         Collection<Property<?>> collection = statedefinition.getProperties();
         String s = BuiltInRegistries.BLOCK.getKey(block).toString();
         if (collection.isEmpty()) {
            message(pPlayer, Component.translatable(this.getDescriptionId() + ".empty", s));
            return false;
         } else {
            CompoundTag compoundtag = pDebugStack.getOrCreateTagElement("DebugProperty");
            String s1 = compoundtag.getString(s);
            Property<?> property = statedefinition.getProperty(s1);
            if (pShouldCycleState) {
               if (property == null) {
                  property = collection.iterator().next();
               }

               BlockState blockstate = cycleState(pStateClicked, property, pPlayer.isSecondaryUseActive());
               pAccessor.setBlock(pPos, blockstate, 18);
               message(pPlayer, Component.translatable(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockstate, property)));
            } else {
               property = getRelative(collection, property, pPlayer.isSecondaryUseActive());
               String s2 = property.getName();
               compoundtag.putString(s, s2);
               message(pPlayer, Component.translatable(this.getDescriptionId() + ".select", s2, getNameHelper(pStateClicked, property)));
            }

            return true;
         }
      }
   }

   private static <T extends Comparable<T>> BlockState cycleState(BlockState pState, Property<T> pProperty, boolean pBackwards) {
      return pState.setValue(pProperty, getRelative(pProperty.getPossibleValues(), pState.getValue(pProperty), pBackwards));
   }

   private static <T> T getRelative(Iterable<T> pAllowedValues, @Nullable T pCurrentValue, boolean pBackwards) {
      return (T)(pBackwards ? Util.findPreviousInIterable(pAllowedValues, pCurrentValue) : Util.findNextInIterable(pAllowedValues, pCurrentValue));
   }

   private static void message(Player pPlayer, Component pMessageComponent) {
      ((ServerPlayer)pPlayer).sendSystemMessage(pMessageComponent, true);
   }

   private static <T extends Comparable<T>> String getNameHelper(BlockState pState, Property<T> pProperty) {
      return pProperty.getName(pState.getValue(pProperty));
   }
}