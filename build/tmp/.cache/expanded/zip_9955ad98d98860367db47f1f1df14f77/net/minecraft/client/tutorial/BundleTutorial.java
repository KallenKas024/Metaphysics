package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BundleTutorial {
   private final Tutorial tutorial;
   private final Options options;
   @Nullable
   private TutorialToast toast;

   public BundleTutorial(Tutorial pTutorial, Options pOptions) {
      this.tutorial = pTutorial;
      this.options = pOptions;
   }

   private void showToast() {
      if (this.toast != null) {
         this.tutorial.removeTimedToast(this.toast);
      }

      Component component = Component.translatable("tutorial.bundleInsert.title");
      Component component1 = Component.translatable("tutorial.bundleInsert.description");
      this.toast = new TutorialToast(TutorialToast.Icons.RIGHT_CLICK, component, component1, true);
      this.tutorial.addTimedToast(this.toast, 160);
   }

   private void clearToast() {
      if (this.toast != null) {
         this.tutorial.removeTimedToast(this.toast);
         this.toast = null;
      }

      if (!this.options.hideBundleTutorial) {
         this.options.hideBundleTutorial = true;
         this.options.save();
      }

   }

   public void onInventoryAction(ItemStack pCarriedStack, ItemStack pSlottedStack, ClickAction pAction) {
      if (!this.options.hideBundleTutorial) {
         if (!pCarriedStack.isEmpty() && pSlottedStack.is(Items.BUNDLE)) {
            if (pAction == ClickAction.PRIMARY) {
               this.showToast();
            } else if (pAction == ClickAction.SECONDARY) {
               this.clearToast();
            }
         } else if (pCarriedStack.is(Items.BUNDLE) && !pSlottedStack.isEmpty() && pAction == ClickAction.SECONDARY) {
            this.clearToast();
         }

      }
   }
}