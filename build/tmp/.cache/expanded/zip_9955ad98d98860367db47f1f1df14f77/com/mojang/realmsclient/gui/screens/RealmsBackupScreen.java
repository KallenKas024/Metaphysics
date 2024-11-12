package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.DownloadTask;
import com.mojang.realmsclient.util.task.RestoreTask;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ResourceLocation PLUS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/plus_icon.png");
   static final ResourceLocation RESTORE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/restore_icon.png");
   static final Component RESTORE_TOOLTIP = Component.translatable("mco.backup.button.restore");
   static final Component HAS_CHANGES_TOOLTIP = Component.translatable("mco.backup.changes.tooltip");
   private static final Component TITLE = Component.translatable("mco.configure.world.backup");
   private static final Component NO_BACKUPS_LABEL = Component.translatable("mco.backup.nobackups");
   private final RealmsConfigureWorldScreen lastScreen;
   List<Backup> backups = Collections.emptyList();
   RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
   int selectedBackup = -1;
   private final int slotId;
   private Button downloadButton;
   private Button restoreButton;
   private Button changesButton;
   Boolean noBackups = false;
   final RealmsServer serverData;
   private static final String UPLOADED_KEY = "uploaded";

   public RealmsBackupScreen(RealmsConfigureWorldScreen pLastScreen, RealmsServer pServerData, int pSlotId) {
      super(Component.translatable("mco.configure.world.backup"));
      this.lastScreen = pLastScreen;
      this.serverData = pServerData;
      this.slotId = pSlotId;
   }

   public void init() {
      this.backupObjectSelectionList = new RealmsBackupScreen.BackupObjectSelectionList();
      (new Thread("Realms-fetch-backups") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               List<Backup> list = realmsclient.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
               RealmsBackupScreen.this.minecraft.execute(() -> {
                  RealmsBackupScreen.this.backups = list;
                  RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                  RealmsBackupScreen.this.backupObjectSelectionList.clear();

                  for(Backup backup : RealmsBackupScreen.this.backups) {
                     RealmsBackupScreen.this.backupObjectSelectionList.addEntry(backup);
                  }

               });
            } catch (RealmsServiceException realmsserviceexception) {
               RealmsBackupScreen.LOGGER.error("Couldn't request backups", (Throwable)realmsserviceexception);
            }

         }
      }).start();
      this.downloadButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.backup.button.download"), (p_88185_) -> {
         this.downloadClicked();
      }).bounds(this.width - 135, row(1), 120, 20).build());
      this.restoreButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.backup.button.restore"), (p_88179_) -> {
         this.restoreClicked(this.selectedBackup);
      }).bounds(this.width - 135, row(3), 120, 20).build());
      this.changesButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.backup.changes.tooltip"), (p_280692_) -> {
         this.minecraft.setScreen(new RealmsBackupInfoScreen(this, this.backups.get(this.selectedBackup)));
         this.selectedBackup = -1;
      }).bounds(this.width - 135, row(5), 120, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_280691_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width - 100, this.height - 35, 85, 20).build());
      this.addWidget(this.backupObjectSelectionList);
      this.magicalSpecialHackyFocus(this.backupObjectSelectionList);
      this.updateButtonStates();
   }

   void updateButtonStates() {
      this.restoreButton.visible = this.shouldRestoreButtonBeVisible();
      this.changesButton.visible = this.shouldChangesButtonBeVisible();
   }

   private boolean shouldChangesButtonBeVisible() {
      if (this.selectedBackup == -1) {
         return false;
      } else {
         return !(this.backups.get(this.selectedBackup)).changeList.isEmpty();
      }
   }

   private boolean shouldRestoreButtonBeVisible() {
      if (this.selectedBackup == -1) {
         return false;
      } else {
         return !this.serverData.expired;
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
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   void restoreClicked(int pIndex) {
      if (pIndex >= 0 && pIndex < this.backups.size() && !this.serverData.expired) {
         this.selectedBackup = pIndex;
         Date date = (this.backups.get(pIndex)).lastModifiedDate;
         String s = DateFormat.getDateTimeInstance(3, 3).format(date);
         Component component = RealmsUtil.convertToAgePresentationFromInstant(date);
         Component component1 = Component.translatable("mco.configure.world.restore.question.line1", s, component);
         Component component2 = Component.translatable("mco.configure.world.restore.question.line2");
         this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_280693_) -> {
            if (p_280693_) {
               this.restore();
            } else {
               this.selectedBackup = -1;
               this.minecraft.setScreen(this);
            }

         }, RealmsLongConfirmationScreen.Type.WARNING, component1, component2, true));
      }

   }

   private void downloadClicked() {
      Component component = Component.translatable("mco.configure.world.restore.download.question.line1");
      Component component1 = Component.translatable("mco.configure.world.restore.download.question.line2");
      this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_280690_) -> {
         if (p_280690_) {
            this.downloadWorldData();
         } else {
            this.minecraft.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
   }

   private void downloadWorldData() {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), new DownloadTask(this.serverData.id, this.slotId, this.serverData.name + " (" + this.serverData.slots.get(this.serverData.activeSlot).getSlotName(this.serverData.activeSlot) + ")", this)));
   }

   private void restore() {
      Backup backup = this.backups.get(this.selectedBackup);
      this.selectedBackup = -1;
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), new RestoreTask(backup, this.serverData.id, this.lastScreen)));
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
      this.backupObjectSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 16777215);
      pGuiGraphics.drawString(this.font, TITLE, (this.width - 150) / 2 - 90, 20, 10526880, false);
      if (this.noBackups) {
         pGuiGraphics.drawString(this.font, NO_BACKUPS_LABEL, 20, this.height / 2 - 10, 16777215, false);
      }

      this.downloadButton.active = !this.noBackups;
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   @OnlyIn(Dist.CLIENT)
   class BackupObjectSelectionList extends RealmsObjectSelectionList<RealmsBackupScreen.Entry> {
      public BackupObjectSelectionList() {
         super(RealmsBackupScreen.this.width - 150, RealmsBackupScreen.this.height, 32, RealmsBackupScreen.this.height - 15, 36);
      }

      public void addEntry(Backup pBackup) {
         this.addEntry(RealmsBackupScreen.this.new Entry(pBackup));
      }

      public int getRowWidth() {
         return (int)((double)this.width * 0.93D);
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public void renderBackground(GuiGraphics pGuiGraphics) {
         RealmsBackupScreen.this.renderBackground(pGuiGraphics);
      }

      public int getScrollbarPosition() {
         return this.width - 5;
      }

      public void selectItem(int pIndex) {
         super.selectItem(pIndex);
         this.selectInviteListItem(pIndex);
      }

      public void selectInviteListItem(int pIndex) {
         RealmsBackupScreen.this.selectedBackup = pIndex;
         RealmsBackupScreen.this.updateButtonStates();
      }

      public void setSelected(@Nullable RealmsBackupScreen.Entry pSelected) {
         super.setSelected(pSelected);
         RealmsBackupScreen.this.selectedBackup = this.children().indexOf(pSelected);
         RealmsBackupScreen.this.updateButtonStates();
      }
   }

   @OnlyIn(Dist.CLIENT)
   class Entry extends ObjectSelectionList.Entry<RealmsBackupScreen.Entry> {
      private static final int Y_PADDING = 2;
      private static final int X_PADDING = 7;
      private final Backup backup;
      private final List<AbstractWidget> children = new ArrayList<>();
      @Nullable
      private ImageButton restoreButton;
      @Nullable
      private ImageButton changesButton;

      public Entry(Backup pBackup) {
         this.backup = pBackup;
         this.populateChangeList(pBackup);
         if (!pBackup.changeList.isEmpty()) {
            this.addChangesButton();
         }

         if (!RealmsBackupScreen.this.serverData.expired) {
            this.addRestoreButton();
         }

      }

      private void populateChangeList(Backup pBackup) {
         int i = RealmsBackupScreen.this.backups.indexOf(pBackup);
         if (i != RealmsBackupScreen.this.backups.size() - 1) {
            Backup backup = RealmsBackupScreen.this.backups.get(i + 1);

            for(String s : pBackup.metadata.keySet()) {
               if (!s.contains("uploaded") && backup.metadata.containsKey(s)) {
                  if (!pBackup.metadata.get(s).equals(backup.metadata.get(s))) {
                     this.addToChangeList(s);
                  }
               } else {
                  this.addToChangeList(s);
               }
            }

         }
      }

      private void addToChangeList(String pChange) {
         if (pChange.contains("uploaded")) {
            String s = DateFormat.getDateTimeInstance(3, 3).format(this.backup.lastModifiedDate);
            this.backup.changeList.put(pChange, s);
            this.backup.setUploadedVersion(true);
         } else {
            this.backup.changeList.put(pChange, this.backup.metadata.get(pChange));
         }

      }

      private void addChangesButton() {
         int i = 9;
         int j = 9;
         int k = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 9 - 28;
         int l = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.backup)) + 2;
         this.changesButton = new ImageButton(k, l, 9, 9, 0, 0, 9, RealmsBackupScreen.PLUS_ICON_LOCATION, 9, 18, (p_279278_) -> {
            RealmsBackupScreen.this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.backup));
         });
         this.changesButton.setTooltip(Tooltip.create(RealmsBackupScreen.HAS_CHANGES_TOOLTIP));
         this.children.add(this.changesButton);
      }

      private void addRestoreButton() {
         int i = 17;
         int j = 10;
         int k = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 17 - 7;
         int l = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.backup)) + 2;
         this.restoreButton = new ImageButton(k, l, 17, 10, 0, 0, 10, RealmsBackupScreen.RESTORE_ICON_LOCATION, 17, 20, (p_279191_) -> {
            RealmsBackupScreen.this.restoreClicked(RealmsBackupScreen.this.backups.indexOf(this.backup));
         });
         this.restoreButton.setTooltip(Tooltip.create(RealmsBackupScreen.RESTORE_TOOLTIP));
         this.children.add(this.restoreButton);
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
         if (this.restoreButton != null) {
            this.restoreButton.mouseClicked(pMouseX, pMouseY, pButton);
         }

         if (this.changesButton != null) {
            this.changesButton.mouseClicked(pMouseX, pMouseY, pButton);
         }

         return true;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         int i = this.backup.isUploadedVersion() ? -8388737 : 16777215;
         pGuiGraphics.drawString(RealmsBackupScreen.this.font, Component.translatable("mco.backup.entry", RealmsUtil.convertToAgePresentationFromInstant(this.backup.lastModifiedDate)), pLeft, pTop + 1, i, false);
         pGuiGraphics.drawString(RealmsBackupScreen.this.font, this.getMediumDatePresentation(this.backup.lastModifiedDate), pLeft, pTop + 12, 5000268, false);
         this.children.forEach((p_280700_) -> {
            p_280700_.setY(pTop + 2);
            p_280700_.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         });
      }

      private String getMediumDatePresentation(Date pDate) {
         return DateFormat.getDateTimeInstance(3, 3).format(pDate);
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.backup.lastModifiedDate.toString());
      }
   }
}