package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tooltip implements NarrationSupplier {
   private static final int MAX_WIDTH = 170;
   private final Component message;
   @Nullable
   private List<FormattedCharSequence> cachedTooltip;
   @Nullable
   private final Component narration;

   private Tooltip(Component pMessage, @Nullable Component pNarration) {
      this.message = pMessage;
      this.narration = pNarration;
   }

   public static Tooltip create(Component pMessage, @Nullable Component pNarration) {
      return new Tooltip(pMessage, pNarration);
   }

   public static Tooltip create(Component pMessage) {
      return new Tooltip(pMessage, pMessage);
   }

   /**
    * Updates the narration output with the current narration information.
    * @param pNarrationElementOutput the output to update with narration information.
    */
   public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
      if (this.narration != null) {
         pNarrationElementOutput.add(NarratedElementType.HINT, this.narration);
      }

   }

   public List<FormattedCharSequence> toCharSequence(Minecraft pMinecraft) {
      if (this.cachedTooltip == null) {
         this.cachedTooltip = splitTooltip(pMinecraft, this.message);
      }

      return this.cachedTooltip;
   }

   public static List<FormattedCharSequence> splitTooltip(Minecraft pMinecraft, Component pMessage) {
      return pMinecraft.font.split(pMessage, 170);
   }
}