package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.Backup;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
   private static final Component UNKNOWN = Component.translatable("mco.backup.unknown");
   private final Screen lastScreen;
   final Backup backup;
   private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

   public RealmsBackupInfoScreen(Screen pLastScreen, Backup pBackup) {
      super(Component.translatable("mco.backup.info.title"));
      this.lastScreen = pLastScreen;
      this.backup = pBackup;
   }

   public void tick() {
   }

   public void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_280689_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20).build());
      this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
      this.addWidget(this.backupInfoList);
      this.magicalSpecialHackyFocus(this.backupInfoList);
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
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
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
      this.renderBackground(pGuiGraphics);
      this.backupInfoList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   Component checkForSpecificMetadata(String pKey, String pValue) {
      String s = pKey.toLowerCase(Locale.ROOT);
      if (s.contains("game") && s.contains("mode")) {
         return this.gameModeMetadata(pValue);
      } else {
         return (Component)(s.contains("game") && s.contains("difficulty") ? this.gameDifficultyMetadata(pValue) : Component.literal(pValue));
      }
   }

   private Component gameDifficultyMetadata(String pValue) {
      try {
         return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(pValue)).getDisplayName();
      } catch (Exception exception) {
         return UNKNOWN;
      }
   }

   private Component gameModeMetadata(String pValue) {
      try {
         return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(pValue)).getShortDisplayName();
      } catch (Exception exception) {
         return UNKNOWN;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class BackupInfoList extends ObjectSelectionList<RealmsBackupInfoScreen.BackupInfoListEntry> {
      public BackupInfoList(Minecraft pMinecraft) {
         super(pMinecraft, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
         this.setRenderSelection(false);
         if (RealmsBackupInfoScreen.this.backup.changeList != null) {
            RealmsBackupInfoScreen.this.backup.changeList.forEach((p_88084_, p_88085_) -> {
               this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(p_88084_, p_88085_));
            });
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   class BackupInfoListEntry extends ObjectSelectionList.Entry<RealmsBackupInfoScreen.BackupInfoListEntry> {
      private static final Component TEMPLATE_NAME = Component.translatable("mco.backup.entry.templateName");
      private static final Component GAME_DIFFICULTY = Component.translatable("mco.backup.entry.gameDifficulty");
      private static final Component NAME = Component.translatable("mco.backup.entry.name");
      private static final Component GAME_SERVER_VERSION = Component.translatable("mco.backup.entry.gameServerVersion");
      private static final Component UPLOADED = Component.translatable("mco.backup.entry.uploaded");
      private static final Component ENABLED_PACK = Component.translatable("mco.backup.entry.enabledPack");
      private static final Component DESCRIPTION = Component.translatable("mco.backup.entry.description");
      private static final Component GAME_MODE = Component.translatable("mco.backup.entry.gameMode");
      private static final Component SEED = Component.translatable("mco.backup.entry.seed");
      private static final Component WORLD_TYPE = Component.translatable("mco.backup.entry.worldType");
      private static final Component UNDEFINED = Component.translatable("mco.backup.entry.undefined");
      private final String key;
      private final String value;

      public BackupInfoListEntry(String pKey, String pValue) {
         this.key = pKey;
         this.value = pValue;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         pGuiGraphics.drawString(RealmsBackupInfoScreen.this.font, this.translateKey(this.key), pLeft, pTop, 10526880);
         pGuiGraphics.drawString(RealmsBackupInfoScreen.this.font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), pLeft, pTop + 12, 16777215);
      }

      private Component translateKey(String pKey) {
         Component component;
         switch (pKey) {
            case "template_name":
               component = TEMPLATE_NAME;
               break;
            case "game_difficulty":
               component = GAME_DIFFICULTY;
               break;
            case "name":
               component = NAME;
               break;
            case "game_server_version":
               component = GAME_SERVER_VERSION;
               break;
            case "uploaded":
               component = UPLOADED;
               break;
            case "enabled_pack":
               component = ENABLED_PACK;
               break;
            case "description":
               component = DESCRIPTION;
               break;
            case "game_mode":
               component = GAME_MODE;
               break;
            case "seed":
               component = SEED;
               break;
            case "world_type":
               component = WORLD_TYPE;
               break;
            default:
               component = UNDEFINED;
         }

         return component;
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.key + " " + this.value);
      }
   }
}