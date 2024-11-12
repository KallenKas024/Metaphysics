package net.minecraft.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Rect2i {
   private int xPos;
   private int yPos;
   private int width;
   private int height;

   public Rect2i(int pXPos, int pYPos, int pWidth, int pHeight) {
      this.xPos = pXPos;
      this.yPos = pYPos;
      this.width = pWidth;
      this.height = pHeight;
   }

   public Rect2i intersect(Rect2i pOther) {
      int i = this.xPos;
      int j = this.yPos;
      int k = this.xPos + this.width;
      int l = this.yPos + this.height;
      int i1 = pOther.getX();
      int j1 = pOther.getY();
      int k1 = i1 + pOther.getWidth();
      int l1 = j1 + pOther.getHeight();
      this.xPos = Math.max(i, i1);
      this.yPos = Math.max(j, j1);
      this.width = Math.max(0, Math.min(k, k1) - this.xPos);
      this.height = Math.max(0, Math.min(l, l1) - this.yPos);
      return this;
   }

   public int getX() {
      return this.xPos;
   }

   public int getY() {
      return this.yPos;
   }

   public void setX(int pXPos) {
      this.xPos = pXPos;
   }

   public void setY(int pYPos) {
      this.yPos = pYPos;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void setWidth(int pWidth) {
      this.width = pWidth;
   }

   public void setHeight(int pHeight) {
      this.height = pHeight;
   }

   public void setPosition(int pXPos, int pYPos) {
      this.xPos = pXPos;
      this.yPos = pYPos;
   }

   public boolean contains(int pX, int pY) {
      return pX >= this.xPos && pX <= this.xPos + this.width && pY >= this.yPos && pY <= this.yPos + this.height;
   }
}