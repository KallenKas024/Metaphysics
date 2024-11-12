package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class EditWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
   private Button renameButton;
   private final BooleanConsumer callback;
   private EditBox nameEdit;
   private final LevelStorageSource.LevelStorageAccess levelAccess;

   public EditWorldScreen(BooleanConsumer pCallback, LevelStorageSource.LevelStorageAccess pLevelAccess) {
      super(Component.translatable("selectWorld.edit.title"));
      this.callback = pCallback;
      this.levelAccess = pLevelAccess;
   }

   public void tick() {
      this.nameEdit.tick();
   }

   protected void init() {
      this.renameButton = Button.builder(Component.translatable("selectWorld.edit.save"), (p_101280_) -> {
         this.onRename();
      }).bounds(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20).build();
      this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, Component.translatable("selectWorld.enterName"));
      LevelSummary levelsummary = this.levelAccess.getSummary();
      String s = levelsummary == null ? "" : levelsummary.getLevelName();
      this.nameEdit.setValue(s);
      this.nameEdit.setResponder((p_280914_) -> {
         this.renameButton.active = !p_280914_.trim().isEmpty();
      });
      this.addWidget(this.nameEdit);
      Button button = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.resetIcon"), (p_280916_) -> {
         this.levelAccess.getIconFile().ifPresent((p_182594_) -> {
            FileUtils.deleteQuietly(p_182594_.toFile());
         });
         p_280916_.active = false;
      }).bounds(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.openFolder"), (p_101294_) -> {
         Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile());
      }).bounds(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backup"), (p_101292_) -> {
         boolean flag = makeBackupAndShowToast(this.levelAccess);
         this.callback.accept(!flag);
      }).bounds(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backupFolder"), (p_280915_) -> {
         LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
         Path path = levelstoragesource.getBackupPath();

         try {
            FileUtil.createDirectoriesSafe(path);
         } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
         }

         Util.getPlatform().openFile(path.toFile());
      }).bounds(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.optimize"), (p_280913_) -> {
         this.minecraft.setScreen(new BackupConfirmScreen(this, (p_280911_, p_280912_) -> {
            if (p_280911_) {
               makeBackupAndShowToast(this.levelAccess);
            }

            this.minecraft.setScreen(OptimizeWorldScreen.create(this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, p_280912_));
         }, Component.translatable("optimizeWorld.confirm.title"), Component.translatable("optimizeWorld.confirm.description"), true));
      }).bounds(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20).build());
      this.addRenderableWidget(this.renameButton);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_101273_) -> {
         this.callback.accept(false);
      }).bounds(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20).build());
      button.active = this.levelAccess.getIconFile().filter((p_182587_) -> {
         return Files.isRegularFile(p_182587_);
      }).isPresent();
      this.setInitialFocus(this.nameEdit);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.nameEdit.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.nameEdit.setValue(s);
   }

   public void onClose() {
      this.callback.accept(false);
   }

   /**
    * Saves changes to the world name and closes this GUI.
    */
   private void onRename() {
      try {
         this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
         this.callback.accept(true);
      } catch (IOException ioexception) {
         LOGGER.error("Failed to access world '{}'", this.levelAccess.getLevelId(), ioexception);
         SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
         this.callback.accept(true);
      }

   }

   public static void makeBackupAndShowToast(LevelStorageSource pLevelSource, String pLevelName) {
      boolean flag = false;

      try (LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = pLevelSource.validateAndCreateAccess(pLevelName)) {
         flag = true;
         makeBackupAndShowToast(levelstoragesource$levelstorageaccess);
      } catch (IOException ioexception) {
         if (!flag) {
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), pLevelName);
         }

         LOGGER.warn("Failed to create backup of level {}", pLevelName, ioexception);
      } catch (ContentValidationException contentvalidationexception) {
         LOGGER.warn("{}", (Object)contentvalidationexception.getMessage());
         SystemToast.onWorldAccessFailure(Minecraft.getInstance(), pLevelName);
      }

   }

   public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess pLevelAccess) {
      long i = 0L;
      IOException ioexception = null;

      try {
         i = pLevelAccess.makeWorldBackup();
      } catch (IOException ioexception1) {
         ioexception = ioexception1;
      }

      if (ioexception != null) {
         Component component2 = Component.translatable("selectWorld.edit.backupFailed");
         Component component3 = Component.literal(ioexception.getMessage());
         Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component2, component3));
         return false;
      } else {
         Component component = Component.translatable("selectWorld.edit.backupCreated", pLevelAccess.getLevelId());
         Component component1 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)i / 1048576.0D));
         Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component1));
         return true;
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
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
      pGuiGraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 100, 24, 10526880);
      this.nameEdit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}