package net.minecraft.client.gui.navigation;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ScreenRectangle(ScreenPosition position, int width, int height) {
   private static final ScreenRectangle EMPTY = new ScreenRectangle(0, 0, 0, 0);

   public ScreenRectangle(int pX, int pY, int pWidth, int pHeight) {
      this(new ScreenPosition(pX, pY), pWidth, pHeight);
   }

   public static ScreenRectangle empty() {
      return EMPTY;
   }

   public static ScreenRectangle of(ScreenAxis pAxis, int pPrimaryPosition, int pSecondaryPosition, int pPrimaryLength, int pSecondaryLength) {
      ScreenRectangle screenrectangle;
      switch (pAxis) {
         case HORIZONTAL:
            screenrectangle = new ScreenRectangle(pPrimaryPosition, pSecondaryPosition, pPrimaryLength, pSecondaryLength);
            break;
         case VERTICAL:
            screenrectangle = new ScreenRectangle(pSecondaryPosition, pPrimaryPosition, pSecondaryLength, pPrimaryLength);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return screenrectangle;
   }

   public ScreenRectangle step(ScreenDirection pDirection) {
      return new ScreenRectangle(this.position.step(pDirection), this.width, this.height);
   }

   public int getLength(ScreenAxis pAxis) {
      int i;
      switch (pAxis) {
         case HORIZONTAL:
            i = this.width;
            break;
         case VERTICAL:
            i = this.height;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return i;
   }

   public int getBoundInDirection(ScreenDirection pDirection) {
      ScreenAxis screenaxis = pDirection.getAxis();
      return pDirection.isPositive() ? this.position.getCoordinate(screenaxis) + this.getLength(screenaxis) - 1 : this.position.getCoordinate(screenaxis);
   }

   public ScreenRectangle getBorder(ScreenDirection pDirection) {
      int i = this.getBoundInDirection(pDirection);
      ScreenAxis screenaxis = pDirection.getAxis().orthogonal();
      int j = this.getBoundInDirection(screenaxis.getNegative());
      int k = this.getLength(screenaxis);
      return of(pDirection.getAxis(), i, j, 1, k).step(pDirection);
   }

   public boolean overlaps(ScreenRectangle pRectangle) {
      return this.overlapsInAxis(pRectangle, ScreenAxis.HORIZONTAL) && this.overlapsInAxis(pRectangle, ScreenAxis.VERTICAL);
   }

   public boolean overlapsInAxis(ScreenRectangle pRectangle, ScreenAxis pAxis) {
      int i = this.getBoundInDirection(pAxis.getNegative());
      int j = pRectangle.getBoundInDirection(pAxis.getNegative());
      int k = this.getBoundInDirection(pAxis.getPositive());
      int l = pRectangle.getBoundInDirection(pAxis.getPositive());
      return Math.max(i, j) <= Math.min(k, l);
   }

   public int getCenterInAxis(ScreenAxis pAxis) {
      return (this.getBoundInDirection(pAxis.getPositive()) + this.getBoundInDirection(pAxis.getNegative())) / 2;
   }

   @Nullable
   public ScreenRectangle intersection(ScreenRectangle pRectangle) {
      int i = Math.max(this.left(), pRectangle.left());
      int j = Math.max(this.top(), pRectangle.top());
      int k = Math.min(this.right(), pRectangle.right());
      int l = Math.min(this.bottom(), pRectangle.bottom());
      return i < k && j < l ? new ScreenRectangle(i, j, k - i, l - j) : null;
   }

   public int top() {
      return this.position.y();
   }

   public int bottom() {
      return this.position.y() + this.height;
   }

   public int left() {
      return this.position.x();
   }

   public int right() {
      return this.position.x() + this.width;
   }
}