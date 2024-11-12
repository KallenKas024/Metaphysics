package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
   static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
   static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
   static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
   private final Consumer<WorldTemplate> callback;
   RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
   int selectedTemplate = -1;
   private Button selectButton;
   private Button trailerButton;
   private Button publisherButton;
   @Nullable
   Component toolTip;
   @Nullable
   String currentLink;
   private final RealmsServer.WorldType worldType;
   int clicks;
   @Nullable
   private Component[] warning;
   private String warningURL;
   boolean displayWarning;
   private boolean hoverWarning;
   @Nullable
   List<TextRenderingUtils.Line> noTemplatesMessage;

   public RealmsSelectWorldTemplateScreen(Component pTitle, Consumer<WorldTemplate> pCallback, RealmsServer.WorldType pWorldType) {
      this(pTitle, pCallback, pWorldType, (WorldTemplatePaginatedList)null);
   }

   public RealmsSelectWorldTemplateScreen(Component pTitle, Consumer<WorldTemplate> pCallback, RealmsServer.WorldType pWorldType, @Nullable WorldTemplatePaginatedList pWorldTemplatePaginatedList) {
      super(pTitle);
      this.callback = pCallback;
      this.worldType = pWorldType;
      if (pWorldTemplatePaginatedList == null) {
         this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
         this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
      } else {
         this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(pWorldTemplatePaginatedList.templates));
         this.fetchTemplatesAsync(pWorldTemplatePaginatedList);
      }

   }

   public void setWarning(Component... pWarning) {
      this.warning = pWarning;
      this.displayWarning = true;
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
      if (this.hoverWarning && this.warningURL != null) {
         Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   public void init() {
      this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(this.worldTemplateObjectSelectionList.getTemplates());
      this.trailerButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.trailer"), (p_89701_) -> {
         this.onTrailer();
      }).bounds(this.width / 2 - 206, this.height - 32, 100, 20).build());
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.select"), (p_89696_) -> {
         this.selectTemplate();
      }).bounds(this.width / 2 - 100, this.height - 32, 100, 20).build());
      Component component = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
      Button button = Button.builder(component, (p_89691_) -> {
         this.onClose();
      }).bounds(this.width / 2 + 6, this.height - 32, 100, 20).build();
      this.addRenderableWidget(button);
      this.publisherButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.publisher"), (p_89679_) -> {
         this.onPublish();
      }).bounds(this.width / 2 + 112, this.height - 32, 100, 20).build());
      this.selectButton.active = false;
      this.trailerButton.visible = false;
      this.publisherButton.visible = false;
      this.addWidget(this.worldTemplateObjectSelectionList);
      this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
   }

   public Component getNarrationMessage() {
      List<Component> list = Lists.newArrayListWithCapacity(2);
      if (this.title != null) {
         list.add(this.title);
      }

      if (this.warning != null) {
         list.addAll(Arrays.asList(this.warning));
      }

      return CommonComponents.joinLines(list);
   }

   void updateButtonStates() {
      this.publisherButton.visible = this.shouldPublisherBeVisible();
      this.trailerButton.visible = this.shouldTrailerBeVisible();
      this.selectButton.active = this.shouldSelectButtonBeActive();
   }

   private boolean shouldSelectButtonBeActive() {
      return this.selectedTemplate != -1;
   }

   private boolean shouldPublisherBeVisible() {
      return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
   }

   private WorldTemplate getSelectedTemplate() {
      return this.worldTemplateObjectSelectionList.get(this.selectedTemplate);
   }

   private boolean shouldTrailerBeVisible() {
      return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public void onClose() {
      this.callback.accept((WorldTemplate)null);
   }

   void selectTemplate() {
      if (this.hasValidTemplate()) {
         this.callback.accept(this.getSelectedTemplate());
      }

   }

   private boolean hasValidTemplate() {
      return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
   }

   private void onTrailer() {
      if (this.hasValidTemplate()) {
         WorldTemplate worldtemplate = this.getSelectedTemplate();
         if (!"".equals(worldtemplate.trailer)) {
            Util.getPlatform().openUri(worldtemplate.trailer);
         }
      }

   }

   private void onPublish() {
      if (this.hasValidTemplate()) {
         WorldTemplate worldtemplate = this.getSelectedTemplate();
         if (!"".equals(worldtemplate.link)) {
            Util.getPlatform().openUri(worldtemplate.link);
         }
      }

   }

   private void fetchTemplatesAsync(final WorldTemplatePaginatedList pOutput) {
      (new Thread("realms-template-fetcher") {
         public void run() {
            WorldTemplatePaginatedList worldtemplatepaginatedlist = pOutput;

            RealmsClient realmsclient = RealmsClient.create();
            while (worldtemplatepaginatedlist != null) {
               Either<WorldTemplatePaginatedList, String> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldtemplatepaginatedlist, realmsclient);
               worldtemplatepaginatedlist = RealmsSelectWorldTemplateScreen.this.minecraft.submit(() -> {
               if (either.right().isPresent()) {
                  RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());
                  if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                     RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.get("mco.template.select.failure"));
                  }

                  return null;
               } else {
                  WorldTemplatePaginatedList worldtemplatepaginatedlist1 = either.left().get();

                  for(WorldTemplate worldtemplate : worldtemplatepaginatedlist1.templates) {
                     RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(worldtemplate);
                  }

                  if (worldtemplatepaginatedlist1.templates.isEmpty()) {
                     if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                        String s = I18n.get("mco.template.select.none", "%link");
                        TextRenderingUtils.LineSegment textrenderingutils$linesegment = TextRenderingUtils.LineSegment.link(I18n.get("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator");
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(s, textrenderingutils$linesegment);
                     }

                     return null;
                  } else {
                     return worldtemplatepaginatedlist1;
                  }
               }
            }).join();
            }

         }
      }).start();
   }

   Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList p_89656_, RealmsClient pRealmsClient) {
      try {
         return Either.left(pRealmsClient.fetchWorldTemplates(p_89656_.page + 1, p_89656_.size, this.worldType));
      } catch (RealmsServiceException realmsserviceexception) {
         return Either.right(realmsserviceexception.getMessage());
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
      this.toolTip = null;
      this.currentLink = null;
      this.hoverWarning = false;
      this.renderBackground(pGuiGraphics);
      this.worldTemplateObjectSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.noTemplatesMessage != null) {
         this.renderMultilineMessage(pGuiGraphics, pMouseX, pMouseY, this.noTemplatesMessage);
      }

      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 13, 16777215);
      if (this.displayWarning) {
         Component[] acomponent = this.warning;

         for(int i = 0; i < acomponent.length; ++i) {
            int j = this.font.width(acomponent[i]);
            int k = this.width / 2 - j / 2;
            int l = row(-1 + i);
            if (pMouseX >= k && pMouseX <= k + j && pMouseY >= l && pMouseY <= l + 9) {
               this.hoverWarning = true;
            }
         }

         for(int i1 = 0; i1 < acomponent.length; ++i1) {
            Component component = acomponent[i1];
            int j1 = 10526880;
            if (this.warningURL != null) {
               if (this.hoverWarning) {
                  j1 = 7107012;
                  component = component.copy().withStyle(ChatFormatting.STRIKETHROUGH);
               } else {
                  j1 = 3368635;
               }
            }

            pGuiGraphics.drawCenteredString(this.font, component, this.width / 2, row(-1 + i1), j1);
         }
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderMousehoverTooltip(pGuiGraphics, this.toolTip, pMouseX, pMouseY);
   }

   private void renderMultilineMessage(GuiGraphics pGuiGraphics, int pX, int pY, List<TextRenderingUtils.Line> pLines) {
      for(int i = 0; i < pLines.size(); ++i) {
         TextRenderingUtils.Line textrenderingutils$line = pLines.get(i);
         int j = row(4 + i);
         int k = textrenderingutils$line.segments.stream().mapToInt((p_280748_) -> {
            return this.font.width(p_280748_.renderedText());
         }).sum();
         int l = this.width / 2 - k / 2;

         for(TextRenderingUtils.LineSegment textrenderingutils$linesegment : textrenderingutils$line.segments) {
            int i1 = textrenderingutils$linesegment.isLink() ? 3368635 : 16777215;
            int j1 = pGuiGraphics.drawString(this.font, textrenderingutils$linesegment.renderedText(), l, j, i1);
            if (textrenderingutils$linesegment.isLink() && pX > l && pX < j1 && pY > j - 3 && pY < j + 8) {
               this.toolTip = Component.literal(textrenderingutils$linesegment.getLinkUrl());
               this.currentLink = textrenderingutils$linesegment.getLinkUrl();
            }

            l = j1;
         }
      }

   }

   protected void renderMousehoverTooltip(GuiGraphics pGuiGraphics, @Nullable Component pTooltip, int pMouseX, int pMouseY) {
      if (pTooltip != null) {
         int i = pMouseX + 12;
         int j = pMouseY - 12;
         int k = this.font.width(pTooltip);
         pGuiGraphics.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
         pGuiGraphics.drawString(this.font, pTooltip, i, j, 16777215);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
      final WorldTemplate template;

      public Entry(WorldTemplate pTemplate) {
         this.template = pTemplate;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.renderWorldTemplateItem(pGuiGraphics, this.template, pLeft, pTop, pMouseX, pMouseY);
      }

      private void renderWorldTemplateItem(GuiGraphics pGuiGraphics, WorldTemplate pTemplate, int pX, int pY, int pMouseX, int pMouseY) {
         int i = pX + 45 + 20;
         pGuiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, pTemplate.name, i, pY + 2, 16777215, false);
         pGuiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, pTemplate.author, i, pY + 15, 7105644, false);
         pGuiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, pTemplate.version, i + 227 - RealmsSelectWorldTemplateScreen.this.font.width(pTemplate.version), pY + 1, 7105644, false);
         if (!"".equals(pTemplate.link) || !"".equals(pTemplate.trailer) || !"".equals(pTemplate.recommendedPlayers)) {
            this.drawIcons(pGuiGraphics, i - 1, pY + 25, pMouseX, pMouseY, pTemplate.link, pTemplate.trailer, pTemplate.recommendedPlayers);
         }

         this.drawImage(pGuiGraphics, pX, pY + 1, pMouseX, pMouseY, pTemplate);
      }

      private void drawImage(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, WorldTemplate pTemplate) {
         pGuiGraphics.blit(RealmsTextureManager.worldTemplate(pTemplate.id, pTemplate.image), pX + 1, pY + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         pGuiGraphics.blit(RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION, pX, pY, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void drawIcons(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, String pLink, String pTrailer, String pRecommendedPlayers) {
         if (!"".equals(pRecommendedPlayers)) {
            pGuiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, pRecommendedPlayers, pX, pY + 4, 5000268, false);
         }

         int i = "".equals(pRecommendedPlayers) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(pRecommendedPlayers) + 2;
         boolean flag = false;
         boolean flag1 = false;
         boolean flag2 = "".equals(pLink);
         if (pMouseX >= pX + i && pMouseX <= pX + i + 32 && pMouseY >= pY && pMouseY <= pY + 15 && pMouseY < RealmsSelectWorldTemplateScreen.this.height - 15 && pMouseY > 32) {
            if (pMouseX <= pX + 15 + i && pMouseX > i) {
               if (flag2) {
                  flag1 = true;
               } else {
                  flag = true;
               }
            } else if (!flag2) {
               flag1 = true;
            }
         }

         if (!flag2) {
            float f = flag ? 15.0F : 0.0F;
            pGuiGraphics.blit(RealmsSelectWorldTemplateScreen.LINK_ICON, pX + i, pY, f, 0.0F, 15, 15, 30, 15);
         }

         if (!"".equals(pTrailer)) {
            int j = pX + i + (flag2 ? 0 : 17);
            float f1 = flag1 ? 15.0F : 0.0F;
            pGuiGraphics.blit(RealmsSelectWorldTemplateScreen.TRAILER_ICON, j, pY, f1, 0.0F, 15, 15, 30, 15);
         }

         if (flag) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.PUBLISHER_LINK_TOOLTIP;
            RealmsSelectWorldTemplateScreen.this.currentLink = pLink;
         } else if (flag1 && !"".equals(pTrailer)) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.TRAILER_LINK_TOOLTIP;
            RealmsSelectWorldTemplateScreen.this.currentLink = pTrailer;
         }

      }

      public Component getNarration() {
         Component component = CommonComponents.joinLines(Component.literal(this.template.name), Component.translatable("mco.template.select.narrate.authors", this.template.author), Component.literal(this.template.recommendedPlayers), Component.translatable("mco.template.select.narrate.version", this.template.version));
         return Component.translatable("narrator.select", component);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
      public WorldTemplateObjectSelectionList() {
         this(Collections.emptyList());
      }

      public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> pTemplates) {
         super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height, RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height - 40, 46);
         pTemplates.forEach(this::addEntry);
      }

      public void addEntry(WorldTemplate p_89805_) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(p_89805_));
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
         if (pButton == 0 && pMouseY >= (double)this.y0 && pMouseY <= (double)this.y1) {
            int i = this.width / 2 - 150;
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

            int j = (int)Math.floor(pMouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int k = j / this.itemHeight;
            if (pMouseX >= (double)i && pMouseX < (double)this.getScrollbarPosition() && k >= 0 && j >= 0 && k < this.getItemCount()) {
               this.selectItem(k);
               this.itemClicked(j, k, pMouseX, pMouseY, this.width, pButton);
               if (k >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                  return super.mouseClicked(pMouseX, pMouseY, pButton);
               }

               RealmsSelectWorldTemplateScreen.this.clicks += 7;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }

      public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry pSelected) {
         super.setSelected(pSelected);
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(pSelected);
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public void renderBackground(GuiGraphics pGuiGraphics) {
         RealmsSelectWorldTemplateScreen.this.renderBackground(pGuiGraphics);
      }

      public boolean isEmpty() {
         return this.getItemCount() == 0;
      }

      public WorldTemplate get(int pIndex) {
         return (this.children().get(pIndex)).template;
      }

      public List<WorldTemplate> getTemplates() {
         return this.children().stream().map((p_89814_) -> {
            return p_89814_.template;
         }).collect(Collectors.toList());
      }
   }
}