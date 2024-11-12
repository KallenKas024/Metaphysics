package net.minecraft.client.tutorial;

import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FindTreeTutorialStepInstance implements TutorialStepInstance {
   private static final int HINT_DELAY = 6000;
   private static final Component TITLE = Component.translatable("tutorial.find_tree.title");
   private static final Component DESCRIPTION = Component.translatable("tutorial.find_tree.description");
   private final Tutorial tutorial;
   private TutorialToast toast;
   private int timeWaiting;

   public FindTreeTutorialStepInstance(Tutorial pTutorial) {
      this.tutorial = pTutorial;
   }

   public void tick() {
      ++this.timeWaiting;
      if (!this.tutorial.isSurvival()) {
         this.tutorial.setStep(TutorialSteps.NONE);
      } else {
         if (this.timeWaiting == 1) {
            LocalPlayer localplayer = this.tutorial.getMinecraft().player;
            if (localplayer != null && (hasCollectedTreeItems(localplayer) || hasPunchedTreesPreviously(localplayer))) {
               this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
               return;
            }
         }

         if (this.timeWaiting >= 6000 && this.toast == null) {
            this.toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, false);
            this.tutorial.getMinecraft().getToasts().addToast(this.toast);
         }

      }
   }

   public void clear() {
      if (this.toast != null) {
         this.toast.hide();
         this.toast = null;
      }

   }

   /**
    * Handles blocks and entities hovering
    */
   public void onLookAt(ClientLevel pLevel, HitResult pResult) {
      if (pResult.getType() == HitResult.Type.BLOCK) {
         BlockState blockstate = pLevel.getBlockState(((BlockHitResult)pResult).getBlockPos());
         if (blockstate.is(BlockTags.COMPLETES_FIND_TREE_TUTORIAL)) {
            this.tutorial.setStep(TutorialSteps.PUNCH_TREE);
         }
      }

   }

   /**
    * Called when the player pick up an ItemStack
    */
   public void onGetItem(ItemStack pStack) {
      if (pStack.is(ItemTags.COMPLETES_FIND_TREE_TUTORIAL)) {
         this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
      }

   }

   private static boolean hasCollectedTreeItems(LocalPlayer pPlayer) {
      return pPlayer.getInventory().hasAnyMatching((p_235270_) -> {
         return p_235270_.is(ItemTags.COMPLETES_FIND_TREE_TUTORIAL);
      });
   }

   public static boolean hasPunchedTreesPreviously(LocalPlayer pPlayer) {
      for(Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.COMPLETES_FIND_TREE_TUTORIAL)) {
         Block block = holder.value();
         if (pPlayer.getStats().getValue(Stats.BLOCK_MINED.get(block)) > 0) {
            return true;
         }
      }

      return false;
   }
}