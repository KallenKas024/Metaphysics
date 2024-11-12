package net.minecraft.client.tutorial;

import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PunchTreeTutorialStepInstance implements TutorialStepInstance {
   private static final int HINT_DELAY = 600;
   private static final Component TITLE = Component.translatable("tutorial.punch_tree.title");
   private static final Component DESCRIPTION = Component.translatable("tutorial.punch_tree.description", Tutorial.key("attack"));
   private final Tutorial tutorial;
   private TutorialToast toast;
   private int timeWaiting;
   private int resetCount;

   public PunchTreeTutorialStepInstance(Tutorial pTutorial) {
      this.tutorial = pTutorial;
   }

   public void tick() {
      ++this.timeWaiting;
      if (!this.tutorial.isSurvival()) {
         this.tutorial.setStep(TutorialSteps.NONE);
      } else {
         if (this.timeWaiting == 1) {
            LocalPlayer localplayer = this.tutorial.getMinecraft().player;
            if (localplayer != null) {
               if (localplayer.getInventory().contains(ItemTags.LOGS)) {
                  this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                  return;
               }

               if (FindTreeTutorialStepInstance.hasPunchedTreesPreviously(localplayer)) {
                  this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                  return;
               }
            }
         }

         if ((this.timeWaiting >= 600 || this.resetCount > 3) && this.toast == null) {
            this.toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, true);
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
    * Called when a player hits block to destroy it.
    */
   public void onDestroyBlock(ClientLevel pLevel, BlockPos pPos, BlockState pState, float pDiggingStage) {
      boolean flag = pState.is(BlockTags.LOGS);
      if (flag && pDiggingStage > 0.0F) {
         if (this.toast != null) {
            this.toast.updateProgress(pDiggingStage);
         }

         if (pDiggingStage >= 1.0F) {
            this.tutorial.setStep(TutorialSteps.OPEN_INVENTORY);
         }
      } else if (this.toast != null) {
         this.toast.updateProgress(0.0F);
      } else if (flag) {
         ++this.resetCount;
      }

   }

   /**
    * Called when the player pick up an ItemStack
    */
   public void onGetItem(ItemStack pStack) {
      if (pStack.is(ItemTags.LOGS)) {
         this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
      }
   }
}