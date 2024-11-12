package net.minecraft.client.gui.layouts;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LayoutSettings {
   LayoutSettings padding(int pPadding);

   LayoutSettings padding(int pHorizontalPadding, int pVerticalPadding);

   LayoutSettings padding(int pPaddingLeft, int pPaddingTop, int pPaddingRight, int pPaddingBottom);

   LayoutSettings paddingLeft(int pPaddingLeft);

   LayoutSettings paddingTop(int pPaddingTop);

   LayoutSettings paddingRight(int pPaddingRight);

   LayoutSettings paddingBottom(int pPaddingBottom);

   LayoutSettings paddingHorizontal(int pHorizontalPadding);

   LayoutSettings paddingVertical(int pVerticalPadding);

   LayoutSettings align(float pXAlignment, float pYAlignment);

   LayoutSettings alignHorizontally(float pXAlignment);

   LayoutSettings alignVertically(float pYAlignment);

   default LayoutSettings alignHorizontallyLeft() {
      return this.alignHorizontally(0.0F);
   }

   default LayoutSettings alignHorizontallyCenter() {
      return this.alignHorizontally(0.5F);
   }

   default LayoutSettings alignHorizontallyRight() {
      return this.alignHorizontally(1.0F);
   }

   default LayoutSettings alignVerticallyTop() {
      return this.alignVertically(0.0F);
   }

   default LayoutSettings alignVerticallyMiddle() {
      return this.alignVertically(0.5F);
   }

   default LayoutSettings alignVerticallyBottom() {
      return this.alignVertically(1.0F);
   }

   LayoutSettings copy();

   LayoutSettings.LayoutSettingsImpl getExposed();

   static LayoutSettings defaults() {
      return new LayoutSettings.LayoutSettingsImpl();
   }

   @OnlyIn(Dist.CLIENT)
   public static class LayoutSettingsImpl implements LayoutSettings {
      public int paddingLeft;
      public int paddingTop;
      public int paddingRight;
      public int paddingBottom;
      public float xAlignment;
      public float yAlignment;

      public LayoutSettingsImpl() {
      }

      public LayoutSettingsImpl(LayoutSettings.LayoutSettingsImpl pOther) {
         this.paddingLeft = pOther.paddingLeft;
         this.paddingTop = pOther.paddingTop;
         this.paddingRight = pOther.paddingRight;
         this.paddingBottom = pOther.paddingBottom;
         this.xAlignment = pOther.xAlignment;
         this.yAlignment = pOther.yAlignment;
      }

      public LayoutSettings.LayoutSettingsImpl padding(int pPadding) {
         return this.padding(pPadding, pPadding);
      }

      public LayoutSettings.LayoutSettingsImpl padding(int pHorizontalPadding, int pVerticalPadding) {
         return this.paddingHorizontal(pHorizontalPadding).paddingVertical(pVerticalPadding);
      }

      public LayoutSettings.LayoutSettingsImpl padding(int pPaddingLeft, int pPaddingTop, int pPaddingRight, int pPaddingBottom) {
         return this.paddingLeft(pPaddingLeft).paddingRight(pPaddingRight).paddingTop(pPaddingTop).paddingBottom(pPaddingBottom);
      }

      public LayoutSettings.LayoutSettingsImpl paddingLeft(int pPaddingLeft) {
         this.paddingLeft = pPaddingLeft;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingTop(int pPaddingTop) {
         this.paddingTop = pPaddingTop;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingRight(int pPaddingRight) {
         this.paddingRight = pPaddingRight;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingBottom(int pPaddingBottom) {
         this.paddingBottom = pPaddingBottom;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingHorizontal(int pHorizontalPadding) {
         return this.paddingLeft(pHorizontalPadding).paddingRight(pHorizontalPadding);
      }

      public LayoutSettings.LayoutSettingsImpl paddingVertical(int pVerticalPadding) {
         return this.paddingTop(pVerticalPadding).paddingBottom(pVerticalPadding);
      }

      public LayoutSettings.LayoutSettingsImpl align(float pXAlignment, float pYAlignment) {
         this.xAlignment = pXAlignment;
         this.yAlignment = pYAlignment;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl alignHorizontally(float pXAlignment) {
         this.xAlignment = pXAlignment;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl alignVertically(float pYAlignment) {
         this.yAlignment = pYAlignment;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl copy() {
         return new LayoutSettings.LayoutSettingsImpl(this);
      }

      public LayoutSettings.LayoutSettingsImpl getExposed() {
         return this;
      }
   }
}