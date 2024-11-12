package net.minecraft.client.gui.screens.packs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.PackEntry> {
   static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
   static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
   static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
   private final Component title;
   final PackSelectionScreen screen;

   public TransferableSelectionList(Minecraft pMinecraft, PackSelectionScreen pScreen, int pWidth, int pHeight, Component pTitle) {
      super(pMinecraft, pWidth, pHeight, 32, pHeight - 55 + 4, 36);
      this.screen = pScreen;
      this.title = pTitle;
      this.centerListVertically = false;
      this.setRenderHeader(true, (int)(9.0F * 1.5F));
   }

   protected void renderHeader(GuiGraphics pGuiGraphics, int pX, int pY) {
      Component component = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
      pGuiGraphics.drawString(this.minecraft.font, component, pX + this.width / 2 - this.minecraft.font.width(component) / 2, Math.min(this.y0 + 3, pY), 16777215, false);
   }

   public int getRowWidth() {
      return this.width;
   }

   protected int getScrollbarPosition() {
      return this.x1 - 6;
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
      if (this.getSelected() != null) {
         switch (pKeyCode) {
            case 32:
            case 257:
               this.getSelected().keyboardSelection();
               return true;
            default:
               if (Screen.hasShiftDown()) {
                  switch (pKeyCode) {
                     case 264:
                        this.getSelected().keyboardMoveDown();
                        return true;
                     case 265:
                        this.getSelected().keyboardMoveUp();
                        return true;
                  }
               }
         }
      }

      return super.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   @OnlyIn(Dist.CLIENT)
   public static class PackEntry extends ObjectSelectionList.Entry<TransferableSelectionList.PackEntry> {
      private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
      private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
      private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
      private static final int ICON_OVERLAY_X_MOVE_UP = 96;
      private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
      private static final int ICON_OVERLAY_Y_SELECTED = 32;
      private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
      private static final int MAX_NAME_WIDTH_PIXELS = 157;
      private static final String TOO_LONG_NAME_SUFFIX = "...";
      private final TransferableSelectionList parent;
      protected final Minecraft minecraft;
      private final PackSelectionModel.Entry pack;
      private final FormattedCharSequence nameDisplayCache;
      private final MultiLineLabel descriptionDisplayCache;
      private final FormattedCharSequence incompatibleNameDisplayCache;
      private final MultiLineLabel incompatibleDescriptionDisplayCache;

      public PackEntry(Minecraft pMinecraft, TransferableSelectionList pParent, PackSelectionModel.Entry pPack) {
         this.minecraft = pMinecraft;
         this.pack = pPack;
         this.parent = pParent;
         this.nameDisplayCache = cacheName(pMinecraft, pPack.getTitle());
         this.descriptionDisplayCache = cacheDescription(pMinecraft, pPack.getExtendedDescription());
         this.incompatibleNameDisplayCache = cacheName(pMinecraft, TransferableSelectionList.INCOMPATIBLE_TITLE);
         this.incompatibleDescriptionDisplayCache = cacheDescription(pMinecraft, pPack.getCompatibility().getDescription());
      }

      private static FormattedCharSequence cacheName(Minecraft pMinecraft, Component pName) {
         int i = pMinecraft.font.width(pName);
         if (i > 157) {
            FormattedText formattedtext = FormattedText.composite(pMinecraft.font.substrByWidth(pName, 157 - pMinecraft.font.width("...")), FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(formattedtext);
         } else {
            return pName.getVisualOrderText();
         }
      }

      private static MultiLineLabel cacheDescription(Minecraft pMinecraft, Component pText) {
         return MultiLineLabel.create(pMinecraft.font, pText, 157, 2);
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.pack.getTitle());
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         PackCompatibility packcompatibility = this.pack.getCompatibility();
         if (!packcompatibility.isCompatible()) {
            pGuiGraphics.fill(pLeft - 1, pTop - 1, pLeft + pWidth - 9, pTop + pHeight + 1, -8978432);
         }

         pGuiGraphics.blit(this.pack.getIconTexture(), pLeft, pTop, 0.0F, 0.0F, 32, 32, 32, 32);
         FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
         MultiLineLabel multilinelabel = this.descriptionDisplayCache;
         if (this.showHoverOverlay() && (this.minecraft.options.touchscreen().get() || pHovering || this.parent.getSelected() == this && this.parent.isFocused())) {
            pGuiGraphics.fill(pLeft, pTop, pLeft + 32, pTop + 32, -1601138544);
            int i = pMouseX - pLeft;
            int j = pMouseY - pTop;
            if (!this.pack.getCompatibility().isCompatible()) {
               formattedcharsequence = this.incompatibleNameDisplayCache;
               multilinelabel = this.incompatibleDescriptionDisplayCache;
            }

            if (this.pack.canSelect()) {
               if (i < 32) {
                  pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            } else {
               if (this.pack.canUnselect()) {
                  if (i < 16) {
                     pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 32.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 32.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveUp()) {
                  if (i < 32 && i > 16 && j < 16) {
                     pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 96.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 96.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveDown()) {
                  if (i < 32 && i > 16 && j > 16) {
                     pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 64.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     pGuiGraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 64.0F, 0.0F, 32, 32, 256, 256);
                  }
               }
            }
         }

         pGuiGraphics.drawString(this.minecraft.font, formattedcharsequence, pLeft + 32 + 2, pTop + 1, 16777215);
         multilinelabel.renderLeftAligned(pGuiGraphics, pLeft + 32 + 2, pTop + 12, 10, 8421504);
      }

      public String getPackId() {
         return this.pack.getId();
      }

      private boolean showHoverOverlay() {
         return !this.pack.isFixedPosition() || !this.pack.isRequired();
      }

      public void keyboardSelection() {
         if (this.pack.canSelect() && this.handlePackSelection()) {
            this.parent.screen.updateFocus(this.parent);
         } else if (this.pack.canUnselect()) {
            this.pack.unselect();
            this.parent.screen.updateFocus(this.parent);
         }

      }

      void keyboardMoveUp() {
         if (this.pack.canMoveUp()) {
            this.pack.moveUp();
         }

      }

      void keyboardMoveDown() {
         if (this.pack.canMoveDown()) {
            this.pack.moveDown();
         }

      }

      private boolean handlePackSelection() {
         if (this.pack.getCompatibility().isCompatible()) {
            this.pack.select();
            return true;
         } else {
            Component component = this.pack.getCompatibility().getConfirmation();
            this.minecraft.setScreen(new ConfirmScreen((p_264693_) -> {
               this.minecraft.setScreen(this.parent.screen);
               if (p_264693_) {
                  this.pack.select();
               }

            }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, component));
            return false;
         }
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
         if (pButton != 0) {
            return false;
         } else {
            double d0 = pMouseX - (double)this.parent.getRowLeft();
            double d1 = pMouseY - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && d0 <= 32.0D) {
               this.parent.screen.clearSelected();
               if (this.pack.canSelect()) {
                  this.handlePackSelection();
                  return true;
               }

               if (d0 < 16.0D && this.pack.canUnselect()) {
                  this.pack.unselect();
                  return true;
               }

               if (d0 > 16.0D && d1 < 16.0D && this.pack.canMoveUp()) {
                  this.pack.moveUp();
                  return true;
               }

               if (d0 > 16.0D && d1 > 16.0D && this.pack.canMoveDown()) {
                  this.pack.moveDown();
                  return true;
               }
            }

            return false;
         }
      }
   }
}