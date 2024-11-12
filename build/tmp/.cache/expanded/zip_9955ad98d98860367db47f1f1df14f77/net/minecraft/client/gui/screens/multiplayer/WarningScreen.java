package net.minecraft.client.gui.screens.multiplayer;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WarningScreen extends Screen {
   private final Component content;
   @Nullable
   private final Component check;
   private final Component narration;
   @Nullable
   protected Checkbox stopShowing;
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   protected WarningScreen(Component pTitle, Component pContent, Component pNarration) {
      this(pTitle, pContent, (Component)null, pNarration);
   }

   protected WarningScreen(Component pTitle, Component pContent, @Nullable Component pCheck, Component pNarration) {
      super(pTitle);
      this.content = pContent;
      this.check = pCheck;
      this.narration = pNarration;
   }

   protected abstract void initButtons(int pYOffset);

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.content, this.width - 100);
      int i = (this.message.getLineCount() + 1) * this.getLineHeight();
      if (this.check != null) {
         int j = this.font.width(this.check);
         this.stopShowing = new Checkbox(this.width / 2 - j / 2 - 8, 76 + i, j + 24, 20, this.check, false);
         this.addRenderableWidget(this.stopShowing);
      }

      this.initButtons(i);
   }

   public Component getNarrationMessage() {
      return this.narration;
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
      this.renderTitle(pGuiGraphics);
      int i = this.width / 2 - this.message.getWidth() / 2;
      this.message.renderLeftAligned(pGuiGraphics, i, 70, this.getLineHeight(), 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   protected void renderTitle(GuiGraphics pGuiGraphics) {
      pGuiGraphics.drawString(this.font, this.title, 25, 30, 16777215);
   }

   protected int getLineHeight() {
      return 9 * 2;
   }
}