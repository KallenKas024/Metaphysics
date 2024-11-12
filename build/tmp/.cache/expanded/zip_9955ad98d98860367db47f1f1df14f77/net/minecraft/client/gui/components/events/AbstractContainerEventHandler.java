package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerEventHandler implements ContainerEventHandler {
   @Nullable
   private GuiEventListener focused;
   private boolean isDragging;

   /**
    * {@return {@code true} if the GUI element is dragging, {@code false} otherwise}
    */
   public final boolean isDragging() {
      return this.isDragging;
   }

   /**
    * Sets if the GUI element is dragging or not.
    * @param pIsDragging the dragging state of the GUI element.
    */
   public final void setDragging(boolean pDragging) {
      this.isDragging = pDragging;
   }

   /**
    * Gets the focused GUI element.
    */
   @Nullable
   public GuiEventListener getFocused() {
      return this.focused;
   }

   /**
    * Sets the focus state of the GUI element.
    * @param pFocused the focused GUI element.
    */
   public void setFocused(@Nullable GuiEventListener pListener) {
      if (this.focused != null) {
         this.focused.setFocused(false);
      }

      if (pListener != null) {
         pListener.setFocused(true);
      }

      this.focused = pListener;
   }
}