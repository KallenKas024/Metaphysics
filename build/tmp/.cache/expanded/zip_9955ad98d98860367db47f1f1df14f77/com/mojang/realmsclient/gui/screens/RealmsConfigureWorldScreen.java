package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.task.CloseServerTask;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchMinigameTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConfigureWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
   private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
   private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
   private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
   private static final Component WORLD_LIST_TITLE = Component.translatable("mco.configure.worlds.title");
   private static final Component TITLE = Component.translatable("mco.configure.world.title");
   private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
   private static final Component SERVER_EXPIRING_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
   private static final Component SERVER_EXPIRING_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
   private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
   private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
   private static final int DEFAULT_BUTTON_WIDTH = 80;
   private static final int DEFAULT_BUTTON_OFFSET = 5;
   @Nullable
   private Component toolTip;
   private final RealmsMainScreen lastScreen;
   @Nullable
   private RealmsServer serverData;
   private final long serverId;
   private int leftX;
   private int rightX;
   private Button playersButton;
   private Button settingsButton;
   private Button subscriptionButton;
   private Button optionsButton;
   private Button backupButton;
   private Button resetWorldButton;
   private Button switchMinigameButton;
   private boolean stateChanged;
   private int animTick;
   private int clicks;
   private final List<RealmsWorldSlotButton> slotButtonList = Lists.newArrayList();

   public RealmsConfigureWorldScreen(RealmsMainScreen pLastScreen, long pServerId) {
      super(TITLE);
      this.lastScreen = pLastScreen;
      this.serverId = pServerId;
   }

   public void init() {
      if (this.serverData == null) {
         this.fetchServerData(this.serverId);
      }

      this.leftX = this.width / 2 - 187;
      this.rightX = this.width / 2 + 190;
      this.playersButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.players"), (p_280722_) -> {
         this.minecraft.setScreen(new RealmsPlayerScreen(this, this.serverData));
      }).bounds(this.centerButton(0, 3), row(0), 100, 20).build());
      this.settingsButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.settings"), (p_280716_) -> {
         this.minecraft.setScreen(new RealmsSettingsScreen(this, this.serverData.clone()));
      }).bounds(this.centerButton(1, 3), row(0), 100, 20).build());
      this.subscriptionButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.subscription"), (p_280725_) -> {
         this.minecraft.setScreen(new RealmsSubscriptionInfoScreen(this, this.serverData.clone(), this.lastScreen));
      }).bounds(this.centerButton(2, 3), row(0), 100, 20).build());
      this.slotButtonList.clear();

      for(int i = 1; i < 5; ++i) {
         this.slotButtonList.add(this.addSlotButton(i));
      }

      this.switchMinigameButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.switchminigame"), (p_280711_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME));
      }).bounds(this.leftButton(0), row(13) - 5, 100, 20).build());
      this.optionsButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.options"), (p_280720_) -> {
         this.minecraft.setScreen(new RealmsSlotOptionsScreen(this, this.serverData.slots.get(this.serverData.activeSlot).clone(), this.serverData.worldType, this.serverData.activeSlot));
      }).bounds(this.leftButton(0), row(13) - 5, 90, 20).build());
      this.backupButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.backup"), (p_280715_) -> {
         this.minecraft.setScreen(new RealmsBackupScreen(this, this.serverData.clone(), this.serverData.activeSlot));
      }).bounds(this.leftButton(1), row(13) - 5, 90, 20).build());
      this.resetWorldButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.resetworld"), (p_280724_) -> {
         this.minecraft.setScreen(new RealmsResetWorldScreen(this, this.serverData.clone(), () -> {
            this.minecraft.execute(() -> {
               this.minecraft.setScreen(this.getNewScreen());
            });
         }, () -> {
            this.minecraft.setScreen(this.getNewScreen());
         }));
      }).bounds(this.leftButton(2), row(13) - 5, 90, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_167407_) -> {
         this.backButtonClicked();
      }).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build());
      this.backupButton.active = true;
      if (this.serverData == null) {
         this.hideMinigameButtons();
         this.hideRegularButtons();
         this.playersButton.active = false;
         this.settingsButton.active = false;
         this.subscriptionButton.active = false;
      } else {
         this.disableButtons();
         if (this.isMinigame()) {
            this.hideRegularButtons();
         } else {
            this.hideMinigameButtons();
         }
      }

   }

   private RealmsWorldSlotButton addSlotButton(int pIndex) {
      int i = this.frame(pIndex);
      int j = row(5) + 5;
      RealmsWorldSlotButton realmsworldslotbutton = new RealmsWorldSlotButton(i, j, 80, 80, () -> {
         return this.serverData;
      }, (p_167399_) -> {
         this.toolTip = p_167399_;
      }, pIndex, (p_167389_) -> {
         RealmsWorldSlotButton.State realmsworldslotbutton$state = ((RealmsWorldSlotButton)p_167389_).getState();
         if (realmsworldslotbutton$state != null) {
            switch (realmsworldslotbutton$state.action) {
               case NOTHING:
                  break;
               case JOIN:
                  this.joinRealm(this.serverData);
                  break;
               case SWITCH_SLOT:
                  if (realmsworldslotbutton$state.minigame) {
                     this.switchToMinigame();
                  } else if (realmsworldslotbutton$state.empty) {
                     this.switchToEmptySlot(pIndex, this.serverData);
                  } else {
                     this.switchToFullSlot(pIndex, this.serverData);
                  }
                  break;
               default:
                  throw new IllegalStateException("Unknown action " + realmsworldslotbutton$state.action);
            }
         }

      });
      return this.addRenderableWidget(realmsworldslotbutton);
   }

   private int leftButton(int pIndex) {
      return this.leftX + pIndex * 95;
   }

   private int centerButton(int pRow, int pColumn) {
      return this.width / 2 - (pColumn * 105 - 5) / 2 + pRow * 105;
   }

   public void tick() {
      super.tick();
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

      this.slotButtonList.forEach(RealmsWorldSlotButton::tick);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.toolTip = null;
      this.renderBackground(pGuiGraphics);
      pGuiGraphics.drawCenteredString(this.font, WORLD_LIST_TITLE, this.width / 2, row(4), 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.serverData == null) {
         pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
      } else {
         String s = this.serverData.getName();
         int i = this.font.width(s);
         int j = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
         int k = this.font.width(this.title);
         pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 16777215);
         pGuiGraphics.drawCenteredString(this.font, s, this.width / 2, 24, j);
         int l = Math.min(this.centerButton(2, 3) + 80 - 11, this.width / 2 + i / 2 + k / 2 + 10);
         this.drawServerStatus(pGuiGraphics, l, 7, pMouseX, pMouseY);
         if (this.isMinigame()) {
            pGuiGraphics.drawString(this.font, Component.translatable("mco.configure.world.minigame", this.serverData.getMinigameName()), this.leftX + 80 + 20 + 10, row(13), 16777215, false);
         }

         if (this.toolTip != null) {
            this.renderMousehoverTooltip(pGuiGraphics, this.toolTip, pMouseX, pMouseY);
         }

      }
   }

   private int frame(int p_88488_) {
      return this.leftX + (p_88488_ - 1) * 98;
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
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   private void backButtonClicked() {
      if (this.stateChanged) {
         this.lastScreen.resetScreen();
      }

      this.minecraft.setScreen(this.lastScreen);
   }

   private void fetchServerData(long pServerId) {
      (new Thread(() -> {
         RealmsClient realmsclient = RealmsClient.create();

         try {
            RealmsServer realmsserver = realmsclient.getOwnWorld(pServerId);
            this.minecraft.execute(() -> {
               this.serverData = realmsserver;
               this.disableButtons();
               if (this.isMinigame()) {
                  this.show(this.switchMinigameButton);
               } else {
                  this.show(this.optionsButton);
                  this.show(this.backupButton);
                  this.show(this.resetWorldButton);
               }

            });
         } catch (RealmsServiceException realmsserviceexception) {
            LOGGER.error("Couldn't get own world");
            this.minecraft.execute(() -> {
               this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(realmsserviceexception.getMessage()), this.lastScreen));
            });
         }

      })).start();
   }

   private void disableButtons() {
      this.playersButton.active = !this.serverData.expired;
      this.settingsButton.active = !this.serverData.expired;
      this.subscriptionButton.active = true;
      this.switchMinigameButton.active = !this.serverData.expired;
      this.optionsButton.active = !this.serverData.expired;
      this.resetWorldButton.active = !this.serverData.expired;
   }

   private void joinRealm(RealmsServer pServer) {
      if (this.serverData.state == RealmsServer.State.OPEN) {
         this.lastScreen.play(pServer, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
      } else {
         this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
      }

   }

   private void switchToMinigame() {
      RealmsSelectWorldTemplateScreen realmsselectworldtemplatescreen = new RealmsSelectWorldTemplateScreen(Component.translatable("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME);
      realmsselectworldtemplatescreen.setWarning(Component.translatable("mco.minigame.world.info.line1"), Component.translatable("mco.minigame.world.info.line2"));
      this.minecraft.setScreen(realmsselectworldtemplatescreen);
   }

   private void switchToFullSlot(int pSlot, RealmsServer pServer) {
      Component component = Component.translatable("mco.configure.world.slot.switch.question.line1");
      Component component1 = Component.translatable("mco.configure.world.slot.switch.question.line2");
      this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_280714_) -> {
         if (p_280714_) {
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(pServer.id, pSlot, () -> {
               this.minecraft.execute(() -> {
                  this.minecraft.setScreen(this.getNewScreen());
               });
            })));
         } else {
            this.minecraft.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
   }

   private void switchToEmptySlot(int pSlot, RealmsServer pServer) {
      Component component = Component.translatable("mco.configure.world.slot.switch.question.line1");
      Component component1 = Component.translatable("mco.configure.world.slot.switch.question.line2");
      this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_280719_) -> {
         if (p_280719_) {
            RealmsResetWorldScreen realmsresetworldscreen = new RealmsResetWorldScreen(this, pServer, Component.translatable("mco.configure.world.switch.slot"), Component.translatable("mco.configure.world.switch.slot.subtitle"), 10526880, CommonComponents.GUI_CANCEL, () -> {
               this.minecraft.execute(() -> {
                  this.minecraft.setScreen(this.getNewScreen());
               });
            }, () -> {
               this.minecraft.setScreen(this.getNewScreen());
            });
            realmsresetworldscreen.setSlot(pSlot);
            realmsresetworldscreen.setResetTitle(Component.translatable("mco.create.world.reset.title"));
            this.minecraft.setScreen(realmsresetworldscreen);
         } else {
            this.minecraft.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
   }

   protected void renderMousehoverTooltip(GuiGraphics pGuiGraphics, @Nullable Component pTooltip, int pMouseX, int pMouseY) {
      int i = pMouseX + 12;
      int j = pMouseY - 12;
      int k = this.font.width(pTooltip);
      if (i + k + 3 > this.rightX) {
         i = i - k - 20;
      }

      pGuiGraphics.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
      pGuiGraphics.drawString(this.font, pTooltip, i, j, 16777215);
   }

   private void drawServerStatus(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      if (this.serverData.expired) {
         this.drawExpired(pGuiGraphics, pX, pY, pMouseX, pMouseY);
      } else if (this.serverData.state == RealmsServer.State.CLOSED) {
         this.drawClose(pGuiGraphics, pX, pY, pMouseX, pMouseY);
      } else if (this.serverData.state == RealmsServer.State.OPEN) {
         if (this.serverData.daysLeft < 7) {
            this.drawExpiring(pGuiGraphics, pX, pY, pMouseX, pMouseY, this.serverData.daysLeft);
         } else {
            this.drawOpen(pGuiGraphics, pX, pY, pMouseX, pMouseY);
         }
      }

   }

   private void drawExpired(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(EXPIRED_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 10, 28);
      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27) {
         this.toolTip = SERVER_EXPIRED_TOOLTIP;
      }

   }

   private void drawExpiring(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, int pDaysLeft) {
      if (this.animTick % 20 < 10) {
         pGuiGraphics.blit(EXPIRES_SOON_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         pGuiGraphics.blit(EXPIRES_SOON_ICON_LOCATION, pX, pY, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27) {
         if (pDaysLeft <= 0) {
            this.toolTip = SERVER_EXPIRING_SOON_TOOLTIP;
         } else if (pDaysLeft == 1) {
            this.toolTip = SERVER_EXPIRING_IN_DAY_TOOLTIP;
         } else {
            this.toolTip = Component.translatable("mco.selectServer.expires.days", pDaysLeft);
         }
      }

   }

   private void drawOpen(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(ON_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 10, 28);
      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27) {
         this.toolTip = SERVER_OPEN_TOOLTIP;
      }

   }

   private void drawClose(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(OFF_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 10, 28);
      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27) {
         this.toolTip = SERVER_CLOSED_TOOLTIP;
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
   }

   private void hideRegularButtons() {
      this.hide(this.optionsButton);
      this.hide(this.backupButton);
      this.hide(this.resetWorldButton);
   }

   private void hide(Button pButton) {
      pButton.visible = false;
      this.removeWidget(pButton);
   }

   private void show(Button pButton) {
      pButton.visible = true;
      this.addRenderableWidget(pButton);
   }

   private void hideMinigameButtons() {
      this.hide(this.switchMinigameButton);
   }

   public void saveSlotSettings(RealmsWorldOptions pWorldOptions) {
      RealmsWorldOptions realmsworldoptions = this.serverData.slots.get(this.serverData.activeSlot);
      pWorldOptions.templateId = realmsworldoptions.templateId;
      pWorldOptions.templateImage = realmsworldoptions.templateImage;
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.updateSlot(this.serverData.id, this.serverData.activeSlot, pWorldOptions);
         this.serverData.slots.put(this.serverData.activeSlot, pWorldOptions);
      } catch (RealmsServiceException realmsserviceexception) {
         LOGGER.error("Couldn't save slot settings");
         this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, this));
         return;
      }

      this.minecraft.setScreen(this);
   }

   public void saveSettings(String pKey, String pValue) {
      String s = pValue.trim().isEmpty() ? null : pValue;
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.update(this.serverData.id, pKey, s);
         this.serverData.setName(pKey);
         this.serverData.setDescription(s);
      } catch (RealmsServiceException realmsserviceexception) {
         LOGGER.error("Couldn't save settings");
         this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, this));
         return;
      }

      this.minecraft.setScreen(this);
   }

   public void openTheWorld(boolean pJoin, Screen pLastScreen) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(pLastScreen, new OpenServerTask(this.serverData, this, this.lastScreen, pJoin, this.minecraft)));
   }

   public void closeTheWorld(Screen pLastScreen) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(pLastScreen, new CloseServerTask(this.serverData, this)));
   }

   public void stateChanged() {
      this.stateChanged = true;
   }

   private void templateSelectionCallback(@Nullable WorldTemplate p_167395_) {
      if (p_167395_ != null && WorldTemplate.WorldTemplateType.MINIGAME == p_167395_.type) {
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchMinigameTask(this.serverData.id, p_167395_, this.getNewScreen())));
      } else {
         this.minecraft.setScreen(this);
      }

   }

   public RealmsConfigureWorldScreen getNewScreen() {
      return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
   }
}