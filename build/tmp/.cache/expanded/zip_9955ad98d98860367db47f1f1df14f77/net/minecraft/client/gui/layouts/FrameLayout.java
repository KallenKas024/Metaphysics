package net.minecraft.client.gui.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrameLayout extends AbstractLayout {
   private final List<FrameLayout.ChildContainer> children = new ArrayList<>();
   private int minWidth;
   private int minHeight;
   private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5F, 0.5F);

   public FrameLayout() {
      this(0, 0, 0, 0);
   }

   public FrameLayout(int pWidth, int pHeight) {
      this(0, 0, pWidth, pHeight);
   }

   public FrameLayout(int pX, int pY, int pWidth, int pHeight) {
      super(pX, pY, pWidth, pHeight);
      this.setMinDimensions(pWidth, pHeight);
   }

   public FrameLayout setMinDimensions(int pMinWidth, int pMinHeight) {
      return this.setMinWidth(pMinWidth).setMinHeight(pMinHeight);
   }

   public FrameLayout setMinHeight(int pMinHeight) {
      this.minHeight = pMinHeight;
      return this;
   }

   public FrameLayout setMinWidth(int pMinWidth) {
      this.minWidth = pMinWidth;
      return this;
   }

   public LayoutSettings newChildLayoutSettings() {
      return this.defaultChildLayoutSettings.copy();
   }

   public LayoutSettings defaultChildLayoutSetting() {
      return this.defaultChildLayoutSettings;
   }

   public void arrangeElements() {
      super.arrangeElements();
      int i = this.minWidth;
      int j = this.minHeight;

      for(FrameLayout.ChildContainer framelayout$childcontainer : this.children) {
         i = Math.max(i, framelayout$childcontainer.getWidth());
         j = Math.max(j, framelayout$childcontainer.getHeight());
      }

      for(FrameLayout.ChildContainer framelayout$childcontainer1 : this.children) {
         framelayout$childcontainer1.setX(this.getX(), i);
         framelayout$childcontainer1.setY(this.getY(), j);
      }

      this.width = i;
      this.height = j;
   }

   public <T extends LayoutElement> T addChild(T pChild) {
      return this.addChild(pChild, this.newChildLayoutSettings());
   }

   public <T extends LayoutElement> T addChild(T pChild, LayoutSettings pLayoutSettings) {
      this.children.add(new FrameLayout.ChildContainer(pChild, pLayoutSettings));
      return pChild;
   }

   public void visitChildren(Consumer<LayoutElement> pConsumer) {
      this.children.forEach((p_265653_) -> {
         pConsumer.accept(p_265653_.child);
      });
   }

   public static void centerInRectangle(LayoutElement pChild, int pX, int pY, int pWidth, int pHeight) {
      alignInRectangle(pChild, pX, pY, pWidth, pHeight, 0.5F, 0.5F);
   }

   public static void centerInRectangle(LayoutElement pChild, ScreenRectangle pRectangle) {
      centerInRectangle(pChild, pRectangle.position().x(), pRectangle.position().y(), pRectangle.width(), pRectangle.height());
   }

   public static void alignInRectangle(LayoutElement pChild, ScreenRectangle pRectangle, float pDeltaX, float pDeltaY) {
      alignInRectangle(pChild, pRectangle.left(), pRectangle.top(), pRectangle.width(), pRectangle.height(), pDeltaX, pDeltaY);
   }

   public static void alignInRectangle(LayoutElement pChild, int pX, int pY, int pWidth, int pHeight, float pDeltaX, float pDeltaY) {
      alignInDimension(pX, pWidth, pChild.getWidth(), pChild::setX, pDeltaX);
      alignInDimension(pY, pHeight, pChild.getHeight(), pChild::setY, pDeltaY);
   }

   public static void alignInDimension(int pPosition, int pRectangleLength, int pChildLength, Consumer<Integer> pSetter, float pDelta) {
      int i = (int)Mth.lerp(pDelta, 0.0F, (float)(pRectangleLength - pChildLength));
      pSetter.accept(pPosition + i);
   }

   @OnlyIn(Dist.CLIENT)
   static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
      protected ChildContainer(LayoutElement p_265667_, LayoutSettings p_265430_) {
         super(p_265667_, p_265430_);
      }
   }
}