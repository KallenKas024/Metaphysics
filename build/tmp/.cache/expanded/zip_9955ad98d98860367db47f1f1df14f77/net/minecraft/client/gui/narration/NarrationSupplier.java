package net.minecraft.client.gui.narration;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An interface for providing narration information.
 */
@OnlyIn(Dist.CLIENT)
public interface NarrationSupplier {
   /**
    * Updates the narration output with the current narration information.
    * @param pNarrationElementOutput the output to update with narration information.
    */
   void updateNarration(NarrationElementOutput pNarrationElementOutput);
}