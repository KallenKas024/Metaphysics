package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldSlotButton extends Button {
   public static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("realms", "textures/gui/realms/empty_frame.png");
   public static final ResourceLocation CHECK_MARK_LOCATION = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
   public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
   public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
   public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
   private static final Component SLOT_ACTIVE_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.active");
   private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
   private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
   private static final Component MINIGAME = Component.translatable("mco.worldSlot.minigame");
   private final Supplier<RealmsServer> serverDataProvider;
   private final Consumer<Component> toolTipSetter;
   private final int slotIndex;
   @Nullable
   private RealmsWorldSlotButton.State state;

   public RealmsWorldSlotButton(int pX, int pY, int pWidth, int pHeight, Supplier<RealmsServer> pServerDataProvider, Consumer<Component> pToolTipSetter, int pSlotIndex, Button.OnPress pOnPress) {
      super(pX, pY, pWidth, pHeight, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
      this.serverDataProvider = pServerDataProvider;
      this.slotIndex = pSlotIndex;
      this.toolTipSetter = pToolTipSetter;
   }

   @Nullable
   public RealmsWorldSlotButton.State getState() {
      return this.state;
   }

   public void tick() {
      RealmsServer realmsserver = this.serverDataProvider.get();
      if (realmsserver != null) {
         RealmsWorldOptions realmsworldoptions = realmsserver.slots.get(this.slotIndex);
         boolean flag2 = this.slotIndex == 4;
         boolean flag;
         String s;
         long i;
         String s1;
         boolean flag1;
         if (flag2) {
            flag = realmsserver.worldType == RealmsServer.WorldType.MINIGAME;
            s = MINIGAME.getString();
            i = (long)realmsserver.minigameId;
            s1 = realmsserver.minigameImage;
            flag1 = realmsserver.minigameId == -1;
         } else {
            flag = realmsserver.activeSlot == this.slotIndex && realmsserver.worldType != RealmsServer.WorldType.MINIGAME;
            s = realmsworldoptions.getSlotName(this.slotIndex);
            i = realmsworldoptions.templateId;
            s1 = realmsworldoptions.templateImage;
            flag1 = realmsworldoptions.empty;
         }

         RealmsWorldSlotButton.Action realmsworldslotbutton$action = getAction(realmsserver, flag, flag2);
         Pair<Component, Component> pair = this.getTooltipAndNarration(realmsserver, s, flag1, flag2, realmsworldslotbutton$action);
         this.state = new RealmsWorldSlotButton.State(flag, s, i, s1, flag1, flag2, realmsworldslotbutton$action, pair.getFirst());
         this.setMessage(pair.getSecond());
      }
   }

   private static RealmsWorldSlotButton.Action getAction(RealmsServer pRealmsServer, boolean pIsCurrentlyActiveSlot, boolean pMinigame) {
      if (pIsCurrentlyActiveSlot) {
         if (!pRealmsServer.expired && pRealmsServer.state != RealmsServer.State.UNINITIALIZED) {
            return RealmsWorldSlotButton.Action.JOIN;
         }
      } else {
         if (!pMinigame) {
            return RealmsWorldSlotButton.Action.SWITCH_SLOT;
         }

         if (!pRealmsServer.expired) {
            return RealmsWorldSlotButton.Action.SWITCH_SLOT;
         }
      }

      return RealmsWorldSlotButton.Action.NOTHING;
   }

   private Pair<Component, Component> getTooltipAndNarration(RealmsServer pRealmsServer, String pSlotName, boolean pEmpty, boolean pMinigame, RealmsWorldSlotButton.Action pAction) {
      if (pAction == RealmsWorldSlotButton.Action.NOTHING) {
         return Pair.of((Component)null, Component.literal(pSlotName));
      } else {
         Component component;
         if (pMinigame) {
            if (pEmpty) {
               component = CommonComponents.EMPTY;
            } else {
               component = CommonComponents.space().append(pSlotName).append(CommonComponents.SPACE).append(pRealmsServer.minigameName);
            }
         } else {
            component = CommonComponents.space().append(pSlotName);
         }

         Component component1;
         if (pAction == RealmsWorldSlotButton.Action.JOIN) {
            component1 = SLOT_ACTIVE_TOOLTIP;
         } else {
            component1 = pMinigame ? SWITCH_TO_MINIGAME_SLOT_TOOLTIP : SWITCH_TO_WORLD_SLOT_TOOLTIP;
         }

         Component component2 = component1.copy().append(component);
         return Pair.of(component1, component2);
      }
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.state != null) {
         this.drawSlotFrame(pGuiGraphics, this.getX(), this.getY(), pMouseX, pMouseY, this.state.isCurrentlyActiveSlot, this.state.slotName, this.slotIndex, this.state.imageId, this.state.image, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
      }
   }

   private void drawSlotFrame(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, boolean pIsSelected, String pText, int pSlotIndex, long pImageId, @Nullable String pImage, boolean pEmpty, boolean pMinigame, RealmsWorldSlotButton.Action pAction, @Nullable Component pTooltip) {
      boolean flag = this.isHoveredOrFocused();
      if (this.isMouseOver((double)pMouseX, (double)pMouseY) && pTooltip != null) {
         this.toolTipSetter.accept(pTooltip);
      }

      Minecraft minecraft = Minecraft.getInstance();
      ResourceLocation resourcelocation;
      if (pMinigame) {
         resourcelocation = RealmsTextureManager.worldTemplate(String.valueOf(pImageId), pImage);
      } else if (pEmpty) {
         resourcelocation = EMPTY_SLOT_LOCATION;
      } else if (pImage != null && pImageId != -1L) {
         resourcelocation = RealmsTextureManager.worldTemplate(String.valueOf(pImageId), pImage);
      } else if (pSlotIndex == 1) {
         resourcelocation = DEFAULT_WORLD_SLOT_1;
      } else if (pSlotIndex == 2) {
         resourcelocation = DEFAULT_WORLD_SLOT_2;
      } else if (pSlotIndex == 3) {
         resourcelocation = DEFAULT_WORLD_SLOT_3;
      } else {
         resourcelocation = EMPTY_SLOT_LOCATION;
      }

      if (pIsSelected) {
         pGuiGraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      pGuiGraphics.blit(resourcelocation, pX + 3, pY + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      boolean flag1 = flag && pAction != RealmsWorldSlotButton.Action.NOTHING;
      if (flag1) {
         pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else if (pIsSelected) {
         pGuiGraphics.setColor(0.8F, 0.8F, 0.8F, 1.0F);
      } else {
         pGuiGraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      pGuiGraphics.blit(SLOT_FRAME_LOCATION, pX, pY, 0.0F, 0.0F, 80, 80, 80, 80);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (pIsSelected) {
         this.renderCheckMark(pGuiGraphics, pX, pY);
      }

      pGuiGraphics.drawCenteredString(minecraft.font, pText, pX + 40, pY + 66, 16777215);
   }

   private void renderCheckMark(GuiGraphics pGuiGraphics, int pX, int pY) {
      RenderSystem.enableBlend();
      pGuiGraphics.blit(CHECK_MARK_LOCATION, pX + 67, pY + 4, 0.0F, 0.0F, 9, 8, 9, 8);
      RenderSystem.disableBlend();
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Action {
      NOTHING,
      SWITCH_SLOT,
      JOIN;
   }

   @OnlyIn(Dist.CLIENT)
   public static class State {
      final boolean isCurrentlyActiveSlot;
      final String slotName;
      final long imageId;
      @Nullable
      final String image;
      public final boolean empty;
      public final boolean minigame;
      public final RealmsWorldSlotButton.Action action;
      @Nullable
      final Component actionPrompt;

      State(boolean pIsCurrentlyActiveSlot, String pSlotName, long pImageId, @Nullable String pImage, boolean pEmpty, boolean pMinigame, RealmsWorldSlotButton.Action pAction, @Nullable Component pActionPrompt) {
         this.isCurrentlyActiveSlot = pIsCurrentlyActiveSlot;
         this.slotName = pSlotName;
         this.imageId = pImageId;
         this.image = pImage;
         this.empty = pEmpty;
         this.minigame = pMinigame;
         this.action = pAction;
         this.actionPrompt = pActionPrompt;
      }
   }
}