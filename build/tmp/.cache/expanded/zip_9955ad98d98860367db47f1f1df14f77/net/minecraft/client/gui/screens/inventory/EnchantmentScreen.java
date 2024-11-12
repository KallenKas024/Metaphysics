package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
   /** The ResourceLocation containing the Enchantment GUI texture location */
   private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
   /** The ResourceLocation containing the texture for the Book rendered above the enchantment table */
   private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
   /** A Random instance for use with the enchantment gui */
   private final RandomSource random = RandomSource.create();
   private BookModel bookModel;
   public int time;
   public float flip;
   public float oFlip;
   public float flipT;
   public float flipA;
   public float open;
   public float oOpen;
   private ItemStack last = ItemStack.EMPTY;

   public EnchantmentScreen(EnchantmentMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
   }

   protected void init() {
      super.init();
      this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
   }

   public void containerTick() {
      super.containerTick();
      this.tickBook();
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
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;

      for(int k = 0; k < 3; ++k) {
         double d0 = pMouseX - (double)(i + 60);
         double d1 = pMouseY - (double)(j + 14 + 19 * k);
         if (d0 >= 0.0D && d1 >= 0.0D && d0 < 108.0D && d1 < 19.0D && this.menu.clickMenuButton(this.minecraft.player, k)) {
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, k);
            return true;
         }
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton);
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
      this.renderBook(pGuiGraphics, i, j, pPartialTick);
      EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
      int k = this.menu.getGoldCount();

      for(int l = 0; l < 3; ++l) {
         int i1 = i + 60;
         int j1 = i1 + 20;
         int k1 = (this.menu).costs[l];
         if (k1 == 0) {
            pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i1, j + 14 + 19 * l, 0, 185, 108, 19);
         } else {
            String s = "" + k1;
            int l1 = 86 - this.font.width(s);
            FormattedText formattedtext = EnchantmentNames.getInstance().getRandomName(this.font, l1);
            int i2 = 6839882;
            if (((k < l + 1 || this.minecraft.player.experienceLevel < k1) && !this.minecraft.player.getAbilities().instabuild) || this.menu.enchantClue[l] == -1) { // Forge: render buttons as disabled when enchantable but enchantability not met on lower levels
               pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i1, j + 14 + 19 * l, 0, 185, 108, 19);
               pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i1 + 1, j + 15 + 19 * l, 16 * l, 239, 16, 16);
               pGuiGraphics.drawWordWrap(this.font, formattedtext, j1, j + 16 + 19 * l, l1, (i2 & 16711422) >> 1);
               i2 = 4226832;
            } else {
               int j2 = pMouseX - (i + 60);
               int k2 = pMouseY - (j + 14 + 19 * l);
               if (j2 >= 0 && k2 >= 0 && j2 < 108 && k2 < 19) {
                  pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i1, j + 14 + 19 * l, 0, 204, 108, 19);
                  i2 = 16777088;
               } else {
                  pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i1, j + 14 + 19 * l, 0, 166, 108, 19);
               }

               pGuiGraphics.blit(ENCHANTING_TABLE_LOCATION, i1 + 1, j + 15 + 19 * l, 16 * l, 223, 16, 16);
               pGuiGraphics.drawWordWrap(this.font, formattedtext, j1, j + 16 + 19 * l, l1, i2);
               i2 = 8453920;
            }

            pGuiGraphics.drawString(this.font, s, j1 + 86 - this.font.width(s), j + 16 + 19 * l + 7, i2);
         }
      }

   }

   private void renderBook(GuiGraphics pGuiGraphics, int pX, int pY, float pPartialTick) {
      float f = Mth.lerp(pPartialTick, this.oOpen, this.open);
      float f1 = Mth.lerp(pPartialTick, this.oFlip, this.flip);
      Lighting.setupForEntityInInventory();
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((float)pX + 33.0F, (float)pY + 31.0F, 100.0F);
      float f2 = 40.0F;
      pGuiGraphics.pose().scale(-40.0F, 40.0F, 40.0F);
      pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
      pGuiGraphics.pose().translate((1.0F - f) * 0.2F, (1.0F - f) * 0.1F, (1.0F - f) * 0.25F);
      float f3 = -(1.0F - f) * 90.0F - 90.0F;
      pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(f3));
      pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
      float f4 = Mth.clamp(Mth.frac(f1 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
      float f5 = Mth.clamp(Mth.frac(f1 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
      this.bookModel.setupAnim(0.0F, f4, f5, f);
      VertexConsumer vertexconsumer = pGuiGraphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
      this.bookModel.renderToBuffer(pGuiGraphics.pose(), vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pGuiGraphics.flush();
      pGuiGraphics.pose().popPose();
      Lighting.setupFor3DItems();
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      pPartialTick = this.minecraft.getFrameTime();
      this.renderBackground(pGuiGraphics);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
      boolean flag = this.minecraft.player.getAbilities().instabuild;
      int i = this.menu.getGoldCount();

      for(int j = 0; j < 3; ++j) {
         int k = (this.menu).costs[j];
         Enchantment enchantment = Enchantment.byId((this.menu).enchantClue[j]);
         int l = (this.menu).levelClue[j];
         int i1 = j + 1;
         if (this.isHovering(60, 14 + 19 * j, 108, 17, (double)pMouseX, (double)pMouseY) && k > 0) {
            List<Component> list = Lists.newArrayList();
            list.add((Component.translatable("container.enchant.clue", enchantment == null ? "" : enchantment.getFullname(l))).withStyle(ChatFormatting.WHITE));
            if (enchantment == null) {
               list.add(Component.literal(""));
               list.add(Component.translatable("forge.container.enchant.limitedEnchantability").withStyle(ChatFormatting.RED));
            } else if (!flag) {
               list.add(CommonComponents.EMPTY);
               if (this.minecraft.player.experienceLevel < k) {
                  list.add(Component.translatable("container.enchant.level.requirement", (this.menu).costs[j]).withStyle(ChatFormatting.RED));
               } else {
                  MutableComponent mutablecomponent;
                  if (i1 == 1) {
                     mutablecomponent = Component.translatable("container.enchant.lapis.one");
                  } else {
                     mutablecomponent = Component.translatable("container.enchant.lapis.many", i1);
                  }

                  list.add(mutablecomponent.withStyle(i >= i1 ? ChatFormatting.GRAY : ChatFormatting.RED));
                  MutableComponent mutablecomponent1;
                  if (i1 == 1) {
                     mutablecomponent1 = Component.translatable("container.enchant.level.one");
                  } else {
                     mutablecomponent1 = Component.translatable("container.enchant.level.many", i1);
                  }

                  list.add(mutablecomponent1.withStyle(ChatFormatting.GRAY));
               }
            }

            pGuiGraphics.renderComponentTooltip(this.font, list, pMouseX, pMouseY);
            break;
         }
      }

   }

   public void tickBook() {
      ItemStack itemstack = this.menu.getSlot(0).getItem();
      if (!ItemStack.matches(itemstack, this.last)) {
         this.last = itemstack;

         do {
            this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
         } while(this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
      }

      ++this.time;
      this.oFlip = this.flip;
      this.oOpen = this.open;
      boolean flag = false;

      for(int i = 0; i < 3; ++i) {
         if ((this.menu).costs[i] != 0) {
            flag = true;
         }
      }

      if (flag) {
         this.open += 0.2F;
      } else {
         this.open -= 0.2F;
      }

      this.open = Mth.clamp(this.open, 0.0F, 1.0F);
      float f1 = (this.flipT - this.flip) * 0.4F;
      float f = 0.2F;
      f1 = Mth.clamp(f1, -0.2F, 0.2F);
      this.flipA += (f1 - this.flipA) * 0.9F;
      this.flip += this.flipA;
   }
}
