package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements ErrorCallback {
   private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Screen lastScreen;
   private volatile Component title = CommonComponents.EMPTY;
   @Nullable
   private volatile Component errorMessage;
   private volatile boolean aborted;
   private int animTicks;
   private final LongRunningTask task;
   private final int buttonLength = 212;
   private Button cancelOrBackButton;
   public static final String[] SYMBOLS = new String[]{"\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _"};

   public RealmsLongRunningMcoTaskScreen(Screen pLastScreen, LongRunningTask pTask) {
      super(GameNarrator.NO_TITLE);
      this.lastScreen = pLastScreen;
      this.task = pTask;
      pTask.setScreen(this);
      Thread thread = new Thread(pTask, "Realms-long-running-task");
      thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public void tick() {
      super.tick();
      REPEATED_NARRATOR.narrate(this.minecraft.getNarrator(), this.title);
      ++this.animTicks;
      this.task.tick();
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.cancelOrBackButtonClicked();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public void init() {
      this.task.init();
      this.cancelOrBackButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_88795_) -> {
         this.cancelOrBackButtonClicked();
      }).bounds(this.width / 2 - 106, row(12), 212, 20).build());
   }

   private void cancelOrBackButtonClicked() {
      this.aborted = true;
      this.task.abortTask();
      this.minecraft.setScreen(this.lastScreen);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, row(3), 16777215);
      Component component = this.errorMessage;
      if (component == null) {
         pGuiGraphics.drawCenteredString(this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, row(8), 8421504);
      } else {
         pGuiGraphics.drawCenteredString(this.font, component, this.width / 2, row(8), 16711680);
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void error(Component pError) {
      this.errorMessage = pError;
      this.minecraft.getNarrator().sayNow(pError);
      this.minecraft.execute(() -> {
         this.removeWidget(this.cancelOrBackButton);
         this.cancelOrBackButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_88790_) -> {
            this.cancelOrBackButtonClicked();
         }).bounds(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20).build());
      });
   }

   public void setTitle(Component pTitle) {
      this.title = pTitle;
   }

   public boolean aborted() {
      return this.aborted;
   }
}