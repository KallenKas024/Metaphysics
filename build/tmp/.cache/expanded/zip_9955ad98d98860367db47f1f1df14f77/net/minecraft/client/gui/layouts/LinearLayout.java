package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LinearLayout extends AbstractLayout {
   private final LinearLayout.Orientation orientation;
   private final List<LinearLayout.ChildContainer> children = new ArrayList<>();
   private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

   public LinearLayout(int pWidth, int pHeight, LinearLayout.Orientation pOrientation) {
      this(0, 0, pWidth, pHeight, pOrientation);
   }

   public LinearLayout(int pX, int pY, int pWidth, int pHeight, LinearLayout.Orientation pOrientation) {
      super(pX, pY, pWidth, pHeight);
      this.orientation = pOrientation;
   }

   public void arrangeElements() {
      super.arrangeElements();
      if (!this.children.isEmpty()) {
         int i = 0;
         int j = this.orientation.getSecondaryLength(this);

         for(LinearLayout.ChildContainer linearlayout$childcontainer : this.children) {
            i += this.orientation.getPrimaryLength(linearlayout$childcontainer);
            j = Math.max(j, this.orientation.getSecondaryLength(linearlayout$childcontainer));
         }

         int k = this.orientation.getPrimaryLength(this) - i;
         int l = this.orientation.getPrimaryPosition(this);
         Iterator<LinearLayout.ChildContainer> iterator = this.children.iterator();
         LinearLayout.ChildContainer linearlayout$childcontainer1 = iterator.next();
         this.orientation.setPrimaryPosition(linearlayout$childcontainer1, l);
         l += this.orientation.getPrimaryLength(linearlayout$childcontainer1);
         LinearLayout.ChildContainer linearlayout$childcontainer2;
         if (this.children.size() >= 2) {
            for(Divisor divisor = new Divisor(k, this.children.size() - 1); divisor.hasNext(); l += this.orientation.getPrimaryLength(linearlayout$childcontainer2)) {
               l += divisor.nextInt();
               linearlayout$childcontainer2 = iterator.next();
               this.orientation.setPrimaryPosition(linearlayout$childcontainer2, l);
            }
         }

         int i1 = this.orientation.getSecondaryPosition(this);

         for(LinearLayout.ChildContainer linearlayout$childcontainer3 : this.children) {
            this.orientation.setSecondaryPosition(linearlayout$childcontainer3, i1, j);
         }

         switch (this.orientation) {
            case HORIZONTAL:
               this.height = j;
               break;
            case VERTICAL:
               this.width = j;
         }

      }
   }

   public void visitChildren(Consumer<LayoutElement> pConsumer) {
      this.children.forEach((p_265178_) -> {
         pConsumer.accept(p_265178_.child);
      });
   }

   public LayoutSettings newChildLayoutSettings() {
      return this.defaultChildLayoutSettings.copy();
   }

   public LayoutSettings defaultChildLayoutSetting() {
      return this.defaultChildLayoutSettings;
   }

   public <T extends LayoutElement> T addChild(T pChild) {
      return this.addChild(pChild, this.newChildLayoutSettings());
   }

   public <T extends LayoutElement> T addChild(T pChild, LayoutSettings pLayoutSettings) {
      this.children.add(new LinearLayout.ChildContainer(pChild, pLayoutSettings));
      return pChild;
   }

   @OnlyIn(Dist.CLIENT)
   static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
      protected ChildContainer(LayoutElement p_265706_, LayoutSettings p_265131_) {
         super(p_265706_, p_265131_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Orientation {
      HORIZONTAL,
      VERTICAL;

      int getPrimaryLength(LayoutElement pElement) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = pElement.getWidth();
               break;
            case VERTICAL:
               i = pElement.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getPrimaryLength(LinearLayout.ChildContainer pCotainer) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = pCotainer.getWidth();
               break;
            case VERTICAL:
               i = pCotainer.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getSecondaryLength(LayoutElement pElement) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = pElement.getHeight();
               break;
            case VERTICAL:
               i = pElement.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getSecondaryLength(LinearLayout.ChildContainer pContainer) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = pContainer.getHeight();
               break;
            case VERTICAL:
               i = pContainer.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      void setPrimaryPosition(LinearLayout.ChildContainer pContainer, int pPrimaryPosition) {
         switch (this) {
            case HORIZONTAL:
               pContainer.setX(pPrimaryPosition, pContainer.getWidth());
               break;
            case VERTICAL:
               pContainer.setY(pPrimaryPosition, pContainer.getHeight());
         }

      }

      void setSecondaryPosition(LinearLayout.ChildContainer pContainer, int pSecondaryPosition, int pSecondaryLength) {
         switch (this) {
            case HORIZONTAL:
               pContainer.setY(pSecondaryPosition, pSecondaryLength);
               break;
            case VERTICAL:
               pContainer.setX(pSecondaryPosition, pSecondaryLength);
         }

      }

      int getPrimaryPosition(LayoutElement pElement) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = pElement.getX();
               break;
            case VERTICAL:
               i = pElement.getY();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getSecondaryPosition(LayoutElement pElement) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = pElement.getY();
               break;
            case VERTICAL:
               i = pElement.getX();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }
   }
}