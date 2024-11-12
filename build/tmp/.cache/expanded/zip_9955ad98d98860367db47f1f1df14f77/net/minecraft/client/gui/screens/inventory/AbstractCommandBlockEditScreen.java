package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractCommandBlockEditScreen extends Screen {
   private static final Component SET_COMMAND_LABEL = Component.translatable("advMode.setCommand");
   private static final Component COMMAND_LABEL = Component.translatable("advMode.command");
   private static final Component PREVIOUS_OUTPUT_LABEL = Component.translatable("advMode.previousOutput");
   protected EditBox commandEdit;
   protected EditBox previousEdit;
   protected Button doneButton;
   protected Button cancelButton;
   protected CycleButton<Boolean> outputButton;
   CommandSuggestions commandSuggestions;

   public AbstractCommandBlockEditScreen() {
      super(GameNarrator.NO_TITLE);
   }

   public void tick() {
      this.commandEdit.tick();
      if (!this.getCommandBlock().isValid()) {
         this.onClose();
      }

   }

   abstract BaseCommandBlock getCommandBlock();

   abstract int getPreviousY();

   protected void init() {
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_97691_) -> {
         this.onDone();
      }).bounds(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20).build());
      this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_289627_) -> {
         this.onClose();
      }).bounds(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20).build());
      boolean flag = this.getCommandBlock().isTrackOutput();
      this.outputButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("O"), Component.literal("X")).withInitialValue(flag).displayOnlyValue().create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, Component.translatable("advMode.trackOutput"), (p_169596_, p_169597_) -> {
         BaseCommandBlock basecommandblock = this.getCommandBlock();
         basecommandblock.setTrackOutput(p_169597_);
         this.updatePreviousOutput(p_169597_);
      }));
      this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, Component.translatable("advMode.command")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.commandEdit.setMaxLength(32500);
      this.commandEdit.setResponder(this::onEdited);
      this.addWidget(this.commandEdit);
      this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, Component.translatable("advMode.previousOutput"));
      this.previousEdit.setMaxLength(32500);
      this.previousEdit.setEditable(false);
      this.previousEdit.setValue("-");
      this.addWidget(this.previousEdit);
      this.setInitialFocus(this.commandEdit);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
      this.commandSuggestions.setAllowSuggestions(true);
      this.commandSuggestions.updateCommandInfo();
      this.updatePreviousOutput(flag);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.commandEdit.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.commandEdit.setValue(s);
      this.commandSuggestions.updateCommandInfo();
   }

   protected void updatePreviousOutput(boolean pTrackOutput) {
      this.previousEdit.setValue(pTrackOutput ? this.getCommandBlock().getLastOutput().getString() : "-");
   }

   protected void onDone() {
      BaseCommandBlock basecommandblock = this.getCommandBlock();
      this.populateAndSendPacket(basecommandblock);
      if (!basecommandblock.isTrackOutput()) {
         basecommandblock.setLastOutput((Component)null);
      }

      this.minecraft.setScreen((Screen)null);
   }

   protected abstract void populateAndSendPacket(BaseCommandBlock pCommandBlock);

   private void onEdited(String p_97689_) {
      this.commandSuggestions.updateCommandInfo();
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
      if (this.commandSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (pKeyCode != 257 && pKeyCode != 335) {
         return false;
      } else {
         this.onDone();
         return true;
      }
   }

   /**
    * Called when the mouse wheel is scrolled within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pDelta the scrolling delta.
    */
   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      return this.commandSuggestions.mouseScrolled(pDelta) ? true : super.mouseScrolled(pMouseX, pMouseY, pDelta);
   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      return this.commandSuggestions.mouseClicked(pMouseX, pMouseY, pButton) ? true : super.mouseClicked(pMouseX, pMouseY, pButton);
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
      pGuiGraphics.drawCenteredString(this.font, SET_COMMAND_LABEL, this.width / 2, 20, 16777215);
      pGuiGraphics.drawString(this.font, COMMAND_LABEL, this.width / 2 - 150, 40, 10526880);
      this.commandEdit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      int i = 75;
      if (!this.previousEdit.getValue().isEmpty()) {
         i += 5 * 9 + 1 + this.getPreviousY() - 135;
         pGuiGraphics.drawString(this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150, i + 4, 10526880);
         this.previousEdit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.commandSuggestions.render(pGuiGraphics, pMouseX, pMouseY);
   }
}