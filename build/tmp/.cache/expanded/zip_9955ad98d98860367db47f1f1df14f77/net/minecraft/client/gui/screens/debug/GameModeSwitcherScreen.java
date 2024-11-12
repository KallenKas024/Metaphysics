package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameModeSwitcherScreen extends Screen {
   static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
   private static final int SPRITE_SHEET_WIDTH = 128;
   private static final int SPRITE_SHEET_HEIGHT = 128;
   private static final int SLOT_AREA = 26;
   private static final int SLOT_PADDING = 5;
   private static final int SLOT_AREA_PADDED = 31;
   private static final int HELP_TIPS_OFFSET_Y = 5;
   private static final int ALL_SLOTS_WIDTH = GameModeSwitcherScreen.GameModeIcon.values().length * 31 - 5;
   private static final Component SELECT_KEY = Component.translatable("debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA));
   private final GameModeSwitcherScreen.GameModeIcon previousHovered;
   private GameModeSwitcherScreen.GameModeIcon currentlyHovered;
   private int firstMouseX;
   private int firstMouseY;
   private boolean setFirstMousePos;
   private final List<GameModeSwitcherScreen.GameModeSlot> slots = Lists.newArrayList();

   public GameModeSwitcherScreen() {
      super(GameNarrator.NO_TITLE);
      this.previousHovered = GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.getDefaultSelected());
      this.currentlyHovered = this.previousHovered;
   }

   private GameType getDefaultSelected() {
      MultiPlayerGameMode multiplayergamemode = Minecraft.getInstance().gameMode;
      GameType gametype = multiplayergamemode.getPreviousPlayerMode();
      if (gametype != null) {
         return gametype;
      } else {
         return multiplayergamemode.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
      }
   }

   protected void init() {
      super.init();
      this.currentlyHovered = this.previousHovered;

      for(int i = 0; i < GameModeSwitcherScreen.GameModeIcon.VALUES.length; ++i) {
         GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen$gamemodeicon = GameModeSwitcherScreen.GameModeIcon.VALUES[i];
         this.slots.add(new GameModeSwitcherScreen.GameModeSlot(gamemodeswitcherscreen$gamemodeicon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 31, this.height / 2 - 31));
      }

   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (!this.checkToClose()) {
         pGuiGraphics.pose().pushPose();
         RenderSystem.enableBlend();
         int i = this.width / 2 - 62;
         int j = this.height / 2 - 31 - 27;
         pGuiGraphics.blit(GAMEMODE_SWITCHER_LOCATION, i, j, 0.0F, 0.0F, 125, 75, 128, 128);
         pGuiGraphics.pose().popPose();
         super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         pGuiGraphics.drawCenteredString(this.font, this.currentlyHovered.getName(), this.width / 2, this.height / 2 - 31 - 20, -1);
         pGuiGraphics.drawCenteredString(this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, 16777215);
         if (!this.setFirstMousePos) {
            this.firstMouseX = pMouseX;
            this.firstMouseY = pMouseY;
            this.setFirstMousePos = true;
         }

         boolean flag = this.firstMouseX == pMouseX && this.firstMouseY == pMouseY;

         for(GameModeSwitcherScreen.GameModeSlot gamemodeswitcherscreen$gamemodeslot : this.slots) {
            gamemodeswitcherscreen$gamemodeslot.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            gamemodeswitcherscreen$gamemodeslot.setSelected(this.currentlyHovered == gamemodeswitcherscreen$gamemodeslot.icon);
            if (!flag && gamemodeswitcherscreen$gamemodeslot.isHoveredOrFocused()) {
               this.currentlyHovered = gamemodeswitcherscreen$gamemodeslot.icon;
            }
         }

      }
   }

   private void switchToHoveredGameMode() {
      switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
   }

   private static void switchToHoveredGameMode(Minecraft pMinecraft, GameModeSwitcherScreen.GameModeIcon pGameModeIcon) {
      if (pMinecraft.gameMode != null && pMinecraft.player != null) {
         GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen$gamemodeicon = GameModeSwitcherScreen.GameModeIcon.getFromGameType(pMinecraft.gameMode.getPlayerMode());
         if (pMinecraft.player.hasPermissions(2) && pGameModeIcon != gamemodeswitcherscreen$gamemodeicon) {
            pMinecraft.player.connection.sendUnsignedCommand(pGameModeIcon.getCommand());
         }

      }
   }

   private boolean checkToClose() {
      if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
         this.switchToHoveredGameMode();
         this.minecraft.setScreen((Screen)null);
         return true;
      } else {
         return false;
      }
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
      if (pKeyCode == 293) {
         this.setFirstMousePos = false;
         this.currentlyHovered = this.currentlyHovered.getNext();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   static enum GameModeIcon {
      CREATIVE(Component.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
      SURVIVAL(Component.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
      ADVENTURE(Component.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
      SPECTATOR(Component.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

      protected static final GameModeSwitcherScreen.GameModeIcon[] VALUES = values();
      private static final int ICON_AREA = 16;
      protected static final int ICON_TOP_LEFT = 5;
      final Component name;
      final String command;
      final ItemStack renderStack;

      private GameModeIcon(Component pName, String pCommand, ItemStack pRenderStack) {
         this.name = pName;
         this.command = pCommand;
         this.renderStack = pRenderStack;
      }

      void drawIcon(GuiGraphics pGuiGraphics, int pX, int pY) {
         pGuiGraphics.renderItem(this.renderStack, pX, pY);
      }

      Component getName() {
         return this.name;
      }

      String getCommand() {
         return this.command;
      }

      GameModeSwitcherScreen.GameModeIcon getNext() {
         GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen$gamemodeicon;
         switch (this) {
            case CREATIVE:
               gamemodeswitcherscreen$gamemodeicon = SURVIVAL;
               break;
            case SURVIVAL:
               gamemodeswitcherscreen$gamemodeicon = ADVENTURE;
               break;
            case ADVENTURE:
               gamemodeswitcherscreen$gamemodeicon = SPECTATOR;
               break;
            case SPECTATOR:
               gamemodeswitcherscreen$gamemodeicon = CREATIVE;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return gamemodeswitcherscreen$gamemodeicon;
      }

      static GameModeSwitcherScreen.GameModeIcon getFromGameType(GameType pGameType) {
         GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen$gamemodeicon;
         switch (pGameType) {
            case SPECTATOR:
               gamemodeswitcherscreen$gamemodeicon = SPECTATOR;
               break;
            case SURVIVAL:
               gamemodeswitcherscreen$gamemodeicon = SURVIVAL;
               break;
            case CREATIVE:
               gamemodeswitcherscreen$gamemodeicon = CREATIVE;
               break;
            case ADVENTURE:
               gamemodeswitcherscreen$gamemodeicon = ADVENTURE;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return gamemodeswitcherscreen$gamemodeicon;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public class GameModeSlot extends AbstractWidget {
      final GameModeSwitcherScreen.GameModeIcon icon;
      private boolean isSelected;

      public GameModeSlot(GameModeSwitcherScreen.GameModeIcon pIcon, int pX, int pY) {
         super(pX, pY, 26, 26, pIcon.getName());
         this.icon = pIcon;
      }

      public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         this.drawSlot(pGuiGraphics);
         this.icon.drawIcon(pGuiGraphics, this.getX() + 5, this.getY() + 5);
         if (this.isSelected) {
            this.drawSelection(pGuiGraphics);
         }

      }

      public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
         this.defaultButtonNarrationText(pNarrationElementOutput);
      }

      public boolean isHoveredOrFocused() {
         return super.isHoveredOrFocused() || this.isSelected;
      }

      public void setSelected(boolean pIsSelected) {
         this.isSelected = pIsSelected;
      }

      private void drawSlot(GuiGraphics pGuiGraphics) {
         pGuiGraphics.blit(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION, this.getX(), this.getY(), 0.0F, 75.0F, 26, 26, 128, 128);
      }

      private void drawSelection(GuiGraphics pGuiGraphics) {
         pGuiGraphics.blit(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION, this.getX(), this.getY(), 26.0F, 75.0F, 26, 26, 128, 128);
      }
   }
}