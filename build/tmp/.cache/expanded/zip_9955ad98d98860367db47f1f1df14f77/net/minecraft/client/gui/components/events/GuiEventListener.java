package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Represents a listener for GUI events.
 * <p>
 * It extends the {@code TabOrderedElement} interface, providing tab order functionality for GUI components.
 */
@OnlyIn(Dist.CLIENT)
public interface GuiEventListener extends TabOrderedElement {
   long DOUBLE_CLICK_THRESHOLD_MS = 250L;

   /**
    * Called when the mouse is moved within the GUI element.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    */
   default void mouseMoved(double pMouseX, double pMouseY) {
   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   default boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      return false;
   }

   /**
    * Called when a mouse button is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was released.
    */
   default boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      return false;
   }

   /**
    * Called when the mouse is dragged within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that is being dragged.
    * @param pDragX the X distance of the drag.
    * @param pDragY the Y distance of the drag.
    */
   default boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      return false;
   }

   /**
    * Called when the mouse wheel is scrolled within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pDelta the scrolling delta.
    */
   default boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      return false;
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   default boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      return false;
   }

   /**
    * Called when a keyboard key is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the released key.
    * @param pScanCode the scan code of the released key.
    * @param pModifiers the keyboard modifiers.
    */
   default boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
      return false;
   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   default boolean charTyped(char pCodePoint, int pModifiers) {
      return false;
   }

   /**
    * Retrieves the next focus path based on the given focus navigation event.
    * <p>
    * @return the next focus path as a ComponentPath, or {@code null} if there is no next focus path.
    * @param pEvent the focus navigation event.
    */
   @Nullable
   default ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
      return null;
   }

   /**
    * Checks if the given mouse coordinates are over the GUI element.
    * <p>
    * @return {@code true} if the mouse is over the GUI element, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    */
   default boolean isMouseOver(double pMouseX, double pMouseY) {
      return false;
   }

   /**
    * Sets the focus state of the GUI element.
    * @param pFocused {@code true} to apply focus, {@code false} to remove focus
    */
   void setFocused(boolean pFocused);

   /**
    * {@return {@code true} if the GUI element is focused, {@code false} otherwise}
    */
   boolean isFocused();

   /**
    * {@return the current focus path as a ComponentPath, or {@code null}}
    */
   @Nullable
   default ComponentPath getCurrentFocusPath() {
      return this.isFocused() ? ComponentPath.leaf(this) : null;
   }

   /**
    * {@return the {@link ScreenRectangle} occupied by the GUI element}
    */
   default ScreenRectangle getRectangle() {
      return ScreenRectangle.empty();
   }
}