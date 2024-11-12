package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
   static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
   private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
   private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
   private final List<BeaconScreen.BeaconButton> beaconButtons = Lists.newArrayList();
   @Nullable
   MobEffect primary;
   @Nullable
   MobEffect secondary;

   public BeaconScreen(final BeaconMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 230;
      this.imageHeight = 219;
      pMenu.addSlotListener(new ContainerListener() {
         /**
          * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
          * contents of that slot.
          */
         public void slotChanged(AbstractContainerMenu p_97973_, int p_97974_, ItemStack p_97975_) {
         }

         public void dataChanged(AbstractContainerMenu p_169628_, int p_169629_, int p_169630_) {
            BeaconScreen.this.primary = pMenu.getPrimaryEffect();
            BeaconScreen.this.secondary = pMenu.getSecondaryEffect();
         }
      });
   }

   private <T extends AbstractWidget & BeaconScreen.BeaconButton> void addBeaconButton(T pBeaconButton) {
      this.addRenderableWidget(pBeaconButton);
      this.beaconButtons.add(pBeaconButton);
   }

   protected void init() {
      super.init();
      this.beaconButtons.clear();
      this.addBeaconButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
      this.addBeaconButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));

      for(int i = 0; i <= 2; ++i) {
         int j = BeaconBlockEntity.BEACON_EFFECTS[i].length;
         int k = j * 22 + (j - 1) * 2;

         for(int l = 0; l < j; ++l) {
            MobEffect mobeffect = BeaconBlockEntity.BEACON_EFFECTS[i][l];
            BeaconScreen.BeaconPowerButton beaconscreen$beaconpowerbutton = new BeaconScreen.BeaconPowerButton(this.leftPos + 76 + l * 24 - k / 2, this.topPos + 22 + i * 25, mobeffect, true, i);
            beaconscreen$beaconpowerbutton.active = false;
            this.addBeaconButton(beaconscreen$beaconpowerbutton);
         }
      }

      int i1 = 3;
      int j1 = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
      int k1 = j1 * 22 + (j1 - 1) * 2;

      for(int l1 = 0; l1 < j1 - 1; ++l1) {
         MobEffect mobeffect1 = BeaconBlockEntity.BEACON_EFFECTS[3][l1];
         BeaconScreen.BeaconPowerButton beaconscreen$beaconpowerbutton2 = new BeaconScreen.BeaconPowerButton(this.leftPos + 167 + l1 * 24 - k1 / 2, this.topPos + 47, mobeffect1, false, 3);
         beaconscreen$beaconpowerbutton2.active = false;
         this.addBeaconButton(beaconscreen$beaconpowerbutton2);
      }

      BeaconScreen.BeaconPowerButton beaconscreen$beaconpowerbutton1 = new BeaconScreen.BeaconUpgradePowerButton(this.leftPos + 167 + (j1 - 1) * 24 - k1 / 2, this.topPos + 47, BeaconBlockEntity.BEACON_EFFECTS[0][0]);
      beaconscreen$beaconpowerbutton1.visible = false;
      this.addBeaconButton(beaconscreen$beaconpowerbutton1);
   }

   public void containerTick() {
      super.containerTick();
      this.updateButtons();
   }

   void updateButtons() {
      int i = this.menu.getLevels();
      this.beaconButtons.forEach((p_169615_) -> {
         p_169615_.updateStatus(i);
      });
   }

   protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
      pGuiGraphics.drawCenteredString(this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
      pGuiGraphics.drawCenteredString(this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      pGuiGraphics.blit(BEACON_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
      pGuiGraphics.renderItem(new ItemStack(Items.NETHERITE_INGOT), i + 20, j + 109);
      pGuiGraphics.renderItem(new ItemStack(Items.EMERALD), i + 41, j + 109);
      pGuiGraphics.renderItem(new ItemStack(Items.DIAMOND), i + 41 + 22, j + 109);
      pGuiGraphics.renderItem(new ItemStack(Items.GOLD_INGOT), i + 42 + 44, j + 109);
      pGuiGraphics.renderItem(new ItemStack(Items.IRON_INGOT), i + 42 + 66, j + 109);
      pGuiGraphics.pose().popPose();
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
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
   }

   @OnlyIn(Dist.CLIENT)
   interface BeaconButton {
      void updateStatus(int pBeaconTier);
   }

   @OnlyIn(Dist.CLIENT)
   class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
      public BeaconCancelButton(int pX, int pY) {
         super(pX, pY, 112, 220, CommonComponents.GUI_CANCEL);
      }

      public void onPress() {
         BeaconScreen.this.minecraft.player.closeContainer();
      }

      public void updateStatus(int pBeaconTier) {
      }
   }

   @OnlyIn(Dist.CLIENT)
   class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
      public BeaconConfirmButton(int pX, int pY) {
         super(pX, pY, 90, 220, CommonComponents.GUI_DONE);
      }

      public void onPress() {
         BeaconScreen.this.minecraft.getConnection().send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
         BeaconScreen.this.minecraft.player.closeContainer();
      }

      public void updateStatus(int pBeaconTier) {
         this.active = BeaconScreen.this.menu.hasPayment() && BeaconScreen.this.primary != null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
      private final boolean isPrimary;
      protected final int tier;
      private MobEffect effect;
      private TextureAtlasSprite sprite;

      public BeaconPowerButton(int pX, int pY, MobEffect pEffect, boolean pIsPrimary, int pTier) {
         super(pX, pY);
         this.isPrimary = pIsPrimary;
         this.tier = pTier;
         this.setEffect(pEffect);
      }

      protected void setEffect(MobEffect pEffect) {
         this.effect = pEffect;
         this.sprite = Minecraft.getInstance().getMobEffectTextures().get(pEffect);
         this.setTooltip(Tooltip.create(this.createEffectDescription(pEffect), (Component)null));
      }

      protected MutableComponent createEffectDescription(MobEffect pEffect) {
         return Component.translatable(pEffect.getDescriptionId());
      }

      public void onPress() {
         if (!this.isSelected()) {
            if (this.isPrimary) {
               BeaconScreen.this.primary = this.effect;
            } else {
               BeaconScreen.this.secondary = this.effect;
            }

            BeaconScreen.this.updateButtons();
         }
      }

      protected void renderIcon(GuiGraphics pGuiGraphics) {
         pGuiGraphics.blit(this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);
      }

      public void updateStatus(int pBeaconTier) {
         this.active = this.tier < pBeaconTier;
         this.setSelected(this.effect == (this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
      }

      protected MutableComponent createNarrationMessage() {
         return this.createEffectDescription(this.effect);
      }
   }

   @OnlyIn(Dist.CLIENT)
   abstract static class BeaconScreenButton extends AbstractButton implements BeaconScreen.BeaconButton {
      private boolean selected;

      protected BeaconScreenButton(int pX, int pY) {
         super(pX, pY, 22, 22, CommonComponents.EMPTY);
      }

      protected BeaconScreenButton(int pX, int pY, Component pMessage) {
         super(pX, pY, 22, 22, pMessage);
      }

      public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         int i = 219;
         int j = 0;
         if (!this.active) {
            j += this.width * 2;
         } else if (this.selected) {
            j += this.width * 1;
         } else if (this.isHoveredOrFocused()) {
            j += this.width * 3;
         }

         pGuiGraphics.blit(BeaconScreen.BEACON_LOCATION, this.getX(), this.getY(), j, 219, this.width, this.height);
         this.renderIcon(pGuiGraphics);
      }

      protected abstract void renderIcon(GuiGraphics pGuiGraphics);

      public boolean isSelected() {
         return this.selected;
      }

      public void setSelected(boolean pSelected) {
         this.selected = pSelected;
      }

      public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
         this.defaultButtonNarrationText(pNarrationElementOutput);
      }
   }

   @OnlyIn(Dist.CLIENT)
   abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
      private final int iconX;
      private final int iconY;

      protected BeaconSpriteScreenButton(int p_169663_, int p_169664_, int p_169665_, int p_169666_, Component p_169667_) {
         super(p_169663_, p_169664_, p_169667_);
         this.iconX = p_169665_;
         this.iconY = p_169666_;
      }

      protected void renderIcon(GuiGraphics p_283624_) {
         p_283624_.blit(BeaconScreen.BEACON_LOCATION, this.getX() + 2, this.getY() + 2, this.iconX, this.iconY, 18, 18);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class BeaconUpgradePowerButton extends BeaconScreen.BeaconPowerButton {
      public BeaconUpgradePowerButton(int pX, int pY, MobEffect pEffect) {
         super(pX, pY, pEffect, false, 3);
      }

      protected MutableComponent createEffectDescription(MobEffect pEffect) {
         return Component.translatable(pEffect.getDescriptionId()).append(" II");
      }

      public void updateStatus(int pBeaconTier) {
         if (BeaconScreen.this.primary != null) {
            this.visible = true;
            this.setEffect(BeaconScreen.this.primary);
            super.updateStatus(pBeaconTier);
         } else {
            this.visible = false;
         }

      }
   }
}