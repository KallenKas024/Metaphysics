package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SelectWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final WorldOptions TEST_OPTIONS = new WorldOptions((long)"test1".hashCode(), true, false);
   protected final Screen lastScreen;
   private Button deleteButton;
   private Button selectButton;
   private Button renameButton;
   private Button copyButton;
   protected EditBox searchBox;
   private WorldSelectionList list;

   public SelectWorldScreen(Screen pLastScreen) {
      super(Component.translatable("selectWorld.title"));
      this.lastScreen = pLastScreen;
   }

   public void tick() {
      this.searchBox.tick();
   }

   protected void init() {
      this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, Component.translatable("selectWorld.search"));
      this.searchBox.setResponder((p_232980_) -> {
         this.list.updateFilter(p_232980_);
      });
      this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getValue(), this.list);
      this.addWidget(this.searchBox);
      this.addWidget(this.list);
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.select"), (p_232984_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld);
      }).bounds(this.width / 2 - 154, this.height - 52, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), (p_280918_) -> {
         CreateWorldScreen.openFresh(this.minecraft, this);
      }).bounds(this.width / 2 + 4, this.height - 52, 150, 20).build());
      this.renameButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit"), (p_101378_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld);
      }).bounds(this.width / 2 - 154, this.height - 28, 72, 20).build());
      this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.delete"), (p_101376_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld);
      }).bounds(this.width / 2 - 76, this.height - 28, 72, 20).build());
      this.copyButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.recreate"), (p_101373_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld);
      }).bounds(this.width / 2 + 4, this.height - 28, 72, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280917_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 + 82, this.height - 28, 72, 20).build());
      this.updateButtonStatus(false, false);
      this.setInitialFocus(this.searchBox);
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
      return super.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char pCodePoint, int pModifiers) {
      return this.searchBox.charTyped(pCodePoint, pModifiers);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.list.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void updateButtonStatus(boolean pButtonsActive, boolean pDeleteButtonActive) {
      this.selectButton.active = pButtonsActive;
      this.renameButton.active = pButtonsActive;
      this.copyButton.active = pButtonsActive;
      this.deleteButton.active = pDeleteButtonActive;
   }

   public void removed() {
      if (this.list != null) {
         this.list.children().forEach(WorldSelectionList.Entry::close);
      }

   }
}