package net.minecraft.client.gui.screens.telemetry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleConsumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TelemetryEventWidget extends AbstractScrollWidget {
   private static final int HEADER_HORIZONTAL_PADDING = 32;
   private static final String TELEMETRY_REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
   private static final String TELEMETRY_OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
   private static final Component PROPERTY_TITLE = Component.translatable("telemetry_info.property_title").withStyle(ChatFormatting.UNDERLINE);
   private final Font font;
   private TelemetryEventWidget.Content content;
   @Nullable
   private DoubleConsumer onScrolledListener;

   public TelemetryEventWidget(int pX, int pY, int pWidth, int pHeight, Font pFont) {
      super(pX, pY, pWidth, pHeight, Component.empty());
      this.font = pFont;
      this.content = this.buildContent(Minecraft.getInstance().telemetryOptInExtra());
   }

   public void onOptInChanged(boolean pOptIn) {
      this.content = this.buildContent(pOptIn);
      this.setScrollAmount(this.scrollAmount());
   }

   private TelemetryEventWidget.Content buildContent(boolean pOptIn) {
      TelemetryEventWidget.ContentBuilder telemetryeventwidget$contentbuilder = new TelemetryEventWidget.ContentBuilder(this.containerWidth());
      List<TelemetryEventType> list = new ArrayList<>(TelemetryEventType.values());
      list.sort(Comparator.comparing(TelemetryEventType::isOptIn));
      if (!pOptIn) {
         list.removeIf(TelemetryEventType::isOptIn);
      }

      for(int i = 0; i < list.size(); ++i) {
         TelemetryEventType telemetryeventtype = list.get(i);
         this.addEventType(telemetryeventwidget$contentbuilder, telemetryeventtype);
         if (i < list.size() - 1) {
            telemetryeventwidget$contentbuilder.addSpacer(9);
         }
      }

      return telemetryeventwidget$contentbuilder.build();
   }

   public void setOnScrolledListener(@Nullable DoubleConsumer pOnScrolledListener) {
      this.onScrolledListener = pOnScrolledListener;
   }

   protected void setScrollAmount(double pScrollAmount) {
      super.setScrollAmount(pScrollAmount);
      if (this.onScrolledListener != null) {
         this.onScrolledListener.accept(this.scrollAmount());
      }

   }

   protected int getInnerHeight() {
      return this.content.container().getHeight();
   }

   protected double scrollRate() {
      return 9.0D;
   }

   protected void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = this.getY() + this.innerPadding();
      int j = this.getX() + this.innerPadding();
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((double)j, (double)i, 0.0D);
      this.content.container().visitWidgets((p_280896_) -> {
         p_280896_.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      });
      pGuiGraphics.pose().popPose();
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.content.narration());
   }

   private void addEventType(TelemetryEventWidget.ContentBuilder pContentBuilder, TelemetryEventType pEventType) {
      String s = pEventType.isOptIn() ? "telemetry.event.optional" : "telemetry.event.required";
      pContentBuilder.addHeader(this.font, Component.translatable(s, pEventType.title()));
      pContentBuilder.addHeader(this.font, pEventType.description().withStyle(ChatFormatting.GRAY));
      pContentBuilder.addSpacer(9 / 2);
      pContentBuilder.addLine(this.font, PROPERTY_TITLE, 2);
      this.addEventTypeProperties(pEventType, pContentBuilder);
   }

   private void addEventTypeProperties(TelemetryEventType pEventType, TelemetryEventWidget.ContentBuilder pContentBuilder) {
      for(TelemetryProperty<?> telemetryproperty : pEventType.properties()) {
         pContentBuilder.addLine(this.font, telemetryproperty.title());
      }

   }

   private int containerWidth() {
      return this.width - this.totalInnerPadding();
   }

   @OnlyIn(Dist.CLIENT)
   static record Content(GridLayout container, Component narration) {
   }

   @OnlyIn(Dist.CLIENT)
   static class ContentBuilder {
      private final int width;
      private final GridLayout grid;
      private final GridLayout.RowHelper helper;
      private final LayoutSettings alignHeader;
      private final MutableComponent narration = Component.empty();

      public ContentBuilder(int pWidth) {
         this.width = pWidth;
         this.grid = new GridLayout();
         this.grid.defaultCellSetting().alignHorizontallyLeft();
         this.helper = this.grid.createRowHelper(1);
         this.helper.addChild(SpacerElement.width(pWidth));
         this.alignHeader = this.helper.newCellSettings().alignHorizontallyCenter().paddingHorizontal(32);
      }

      public void addLine(Font pFont, Component pMessage) {
         this.addLine(pFont, pMessage, 0);
      }

      public void addLine(Font pFont, Component pMessage, int pPadding) {
         this.helper.addChild((new MultiLineTextWidget(pMessage, pFont)).setMaxWidth(this.width), this.helper.newCellSettings().paddingBottom(pPadding));
         this.narration.append(pMessage).append("\n");
      }

      public void addHeader(Font pFont, Component pMessage) {
         this.helper.addChild((new MultiLineTextWidget(pMessage, pFont)).setMaxWidth(this.width - 64).setCentered(true), this.alignHeader);
         this.narration.append(pMessage).append("\n");
      }

      public void addSpacer(int pHeight) {
         this.helper.addChild(SpacerElement.height(pHeight));
      }

      public TelemetryEventWidget.Content build() {
         this.grid.arrangeElements();
         return new TelemetryEventWidget.Content(this.grid, this.narration);
      }
   }
}