package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeaderAndFooterLayout implements Layout {
   private static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 36;
   private static final int DEFAULT_CONTENT_MARGIN_TOP = 30;
   private final FrameLayout headerFrame = new FrameLayout();
   private final FrameLayout footerFrame = new FrameLayout();
   private final FrameLayout contentsFrame = new FrameLayout();
   private final Screen screen;
   private int headerHeight;
   private int footerHeight;

   public HeaderAndFooterLayout(Screen pScreen) {
      this(pScreen, 36);
   }

   public HeaderAndFooterLayout(Screen pScreen, int pHeight) {
      this(pScreen, pHeight, pHeight);
   }

   public HeaderAndFooterLayout(Screen pScreen, int pHeaderHeight, int pFooterHeight) {
      this.screen = pScreen;
      this.headerHeight = pHeaderHeight;
      this.footerHeight = pFooterHeight;
      this.headerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
      this.footerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
      this.contentsFrame.defaultChildLayoutSetting().align(0.5F, 0.0F).paddingTop(30);
   }

   public void setX(int pX) {
   }

   public void setY(int pY) {
   }

   public int getX() {
      return 0;
   }

   public int getY() {
      return 0;
   }

   public int getWidth() {
      return this.screen.width;
   }

   public int getHeight() {
      return this.screen.height;
   }

   public int getFooterHeight() {
      return this.footerHeight;
   }

   public void setFooterHeight(int pFooterHeight) {
      this.footerHeight = pFooterHeight;
   }

   public void setHeaderHeight(int pHeaderHeight) {
      this.headerHeight = pHeaderHeight;
   }

   public int getHeaderHeight() {
      return this.headerHeight;
   }

   public void visitChildren(Consumer<LayoutElement> pConsumer) {
      this.headerFrame.visitChildren(pConsumer);
      this.contentsFrame.visitChildren(pConsumer);
      this.footerFrame.visitChildren(pConsumer);
   }

   public void arrangeElements() {
      int i = this.getHeaderHeight();
      int j = this.getFooterHeight();
      this.headerFrame.setMinWidth(this.screen.width);
      this.headerFrame.setMinHeight(i);
      this.headerFrame.setPosition(0, 0);
      this.headerFrame.arrangeElements();
      this.footerFrame.setMinWidth(this.screen.width);
      this.footerFrame.setMinHeight(j);
      this.footerFrame.arrangeElements();
      this.footerFrame.setY(this.screen.height - j);
      this.contentsFrame.setMinWidth(this.screen.width);
      this.contentsFrame.setMinHeight(this.screen.height - i - j);
      this.contentsFrame.setPosition(0, i);
      this.contentsFrame.arrangeElements();
   }

   public <T extends LayoutElement> T addToHeader(T pChild) {
      return this.headerFrame.addChild(pChild);
   }

   public <T extends LayoutElement> T addToHeader(T pChild, LayoutSettings pSettings) {
      return this.headerFrame.addChild(pChild, pSettings);
   }

   public <T extends LayoutElement> T addToFooter(T pChild) {
      return this.footerFrame.addChild(pChild);
   }

   public <T extends LayoutElement> T addToFooter(T pChild, LayoutSettings pSettings) {
      return this.footerFrame.addChild(pChild, pSettings);
   }

   public <T extends LayoutElement> T addToContents(T pChild) {
      return this.contentsFrame.addChild(pChild);
   }

   public <T extends LayoutElement> T addToContents(T pChild, LayoutSettings pSettings) {
      return this.contentsFrame.addChild(pChild, pSettings);
   }

   public LayoutSettings newHeaderLayoutSettings() {
      return this.headerFrame.newChildLayoutSettings();
   }

   public LayoutSettings newContentLayoutSettings() {
      return this.contentsFrame.newChildLayoutSettings();
   }

   public LayoutSettings newFooterLayoutSettings() {
      return this.footerFrame.newChildLayoutSettings();
   }
}