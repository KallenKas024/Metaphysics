package net.minecraft.client.tutorial;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tutorial {
   private final Minecraft minecraft;
   @Nullable
   private TutorialStepInstance instance;
   private final List<Tutorial.TimedToast> timedToasts = Lists.newArrayList();
   private final BundleTutorial bundleTutorial;

   public Tutorial(Minecraft pMinecraft, Options pOptions) {
      this.minecraft = pMinecraft;
      this.bundleTutorial = new BundleTutorial(this, pOptions);
   }

   public void onInput(Input pInput) {
      if (this.instance != null) {
         this.instance.onInput(pInput);
      }

   }

   public void onMouse(double pVelocityX, double pVelocityY) {
      if (this.instance != null) {
         this.instance.onMouse(pVelocityX, pVelocityY);
      }

   }

   public void onLookAt(@Nullable ClientLevel pLevel, @Nullable HitResult pResult) {
      if (this.instance != null && pResult != null && pLevel != null) {
         this.instance.onLookAt(pLevel, pResult);
      }

   }

   public void onDestroyBlock(ClientLevel pLevel, BlockPos pPos, BlockState pState, float pDiggingStage) {
      if (this.instance != null) {
         this.instance.onDestroyBlock(pLevel, pPos, pState, pDiggingStage);
      }

   }

   /**
    * Called when the player opens his inventory
    */
   public void onOpenInventory() {
      if (this.instance != null) {
         this.instance.onOpenInventory();
      }

   }

   /**
    * Called when the player pick up an ItemStack
    */
   public void onGetItem(ItemStack pStack) {
      if (this.instance != null) {
         this.instance.onGetItem(pStack);
      }

   }

   public void stop() {
      if (this.instance != null) {
         this.instance.clear();
         this.instance = null;
      }
   }

   /**
    * Reloads the tutorial step from the game settings
    */
   public void start() {
      if (this.instance != null) {
         this.stop();
      }

      this.instance = this.minecraft.options.tutorialStep.create(this);
   }

   public void addTimedToast(TutorialToast pToast, int pDurationTicks) {
      this.timedToasts.add(new Tutorial.TimedToast(pToast, pDurationTicks));
      this.minecraft.getToasts().addToast(pToast);
   }

   public void removeTimedToast(TutorialToast pToast) {
      this.timedToasts.removeIf((p_120577_) -> {
         return p_120577_.toast == pToast;
      });
      pToast.hide();
   }

   public void tick() {
      this.timedToasts.removeIf(Tutorial.TimedToast::updateProgress);
      if (this.instance != null) {
         if (this.minecraft.level != null) {
            this.instance.tick();
         } else {
            this.stop();
         }
      } else if (this.minecraft.level != null) {
         this.start();
      }

   }

   /**
    * Sets a new step to the tutorial
    */
   public void setStep(TutorialSteps pStep) {
      this.minecraft.options.tutorialStep = pStep;
      this.minecraft.options.save();
      if (this.instance != null) {
         this.instance.clear();
         this.instance = pStep.create(this);
      }

   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public boolean isSurvival() {
      if (this.minecraft.gameMode == null) {
         return false;
      } else {
         return this.minecraft.gameMode.getPlayerMode() == GameType.SURVIVAL;
      }
   }

   public static Component key(String pKeybind) {
      return Component.keybind("key." + pKeybind).withStyle(ChatFormatting.BOLD);
   }

   public void onInventoryAction(ItemStack pCarriedStack, ItemStack pSlottedStack, ClickAction pAction) {
      this.bundleTutorial.onInventoryAction(pCarriedStack, pSlottedStack, pAction);
   }

   @OnlyIn(Dist.CLIENT)
   static final class TimedToast {
      final TutorialToast toast;
      private final int durationTicks;
      private int progress;

      TimedToast(TutorialToast pToast, int pDurationTicks) {
         this.toast = pToast;
         this.durationTicks = pDurationTicks;
      }

      private boolean updateProgress() {
         this.toast.updateProgress(Math.min((float)(++this.progress) / (float)this.durationTicks, 1.0F));
         if (this.progress > this.durationTicks) {
            this.toast.hide();
            return true;
         } else {
            return false;
         }
      }
   }
}