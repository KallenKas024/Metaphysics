package net.minecraft.realms;

import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
   protected RealmsObjectSelectionList(int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
      super(Minecraft.getInstance(), pWidth, pHeight, pY0, pY1, pItemHeight);
   }

   public void setSelectedItem(int pIndex) {
      if (pIndex == -1) {
         this.setSelected((E)null);
      } else if (super.getItemCount() != 0) {
         this.setSelected(this.getEntry(pIndex));
      }

   }

   public void selectItem(int pIndex) {
      this.setSelectedItem(pIndex);
   }

   public void itemClicked(int pRelativeY, int pIndex, double pMouseX, double pMouseY, int pWidth, int pButton) {
   }

   public int getMaxPosition() {
      return 0;
   }

   public int getScrollbarPosition() {
      return this.getRowLeft() + this.getRowWidth();
   }

   public int getRowWidth() {
      return (int)((double)this.width * 0.6D);
   }

   public void replaceEntries(Collection<E> pEntries) {
      super.replaceEntries(pEntries);
   }

   public int getItemCount() {
      return super.getItemCount();
   }

   public int getRowTop(int pIndex) {
      return super.getRowTop(pIndex);
   }

   public int getRowLeft() {
      return super.getRowLeft();
   }

   public int addEntry(E pEntry) {
      return super.addEntry(pEntry);
   }

   public void clear() {
      this.clearEntries();
   }
}