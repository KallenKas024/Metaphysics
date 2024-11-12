package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ClientTextTooltip implements ClientTooltipComponent {
   private final FormattedCharSequence text;

   public ClientTextTooltip(FormattedCharSequence pText) {
      this.text = pText;
   }

   public int getWidth(Font pFont) {
      return pFont.width(this.text);
   }

   public int getHeight() {
      return 10;
   }

   public void renderText(Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
      pFont.drawInBatch(this.text, (float)pMouseX, (float)pMouseY, -1, true, pMatrix, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
   }
}