package net.minecraft.client.gui.components;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TabOrderedElement {
   /**
    * Returns the tab order group of the GUI component.
    * Tab order group determines the order in which the components are traversed when using keyboard navigation.
    * <p>
    * @return The tab order group of the GUI component.
    */
   default int getTabOrderGroup() {
      return 0;
   }
}