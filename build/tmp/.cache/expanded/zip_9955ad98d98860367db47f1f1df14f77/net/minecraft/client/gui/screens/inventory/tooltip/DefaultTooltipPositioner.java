package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class DefaultTooltipPositioner implements ClientTooltipPositioner {
   public static final ClientTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

   private DefaultTooltipPositioner() {
   }

   public Vector2ic positionTooltip(int pScreenWidth, int pScreenHeight, int pMouseX, int pMouseY, int pTooltipWidth, int pTooltipHeight) {
      Vector2i vector2i = (new Vector2i(pMouseX, pMouseY)).add(12, -12);
      this.positionTooltip(pScreenWidth, pScreenHeight, vector2i, pTooltipWidth, pTooltipHeight);
      return vector2i;
   }

   private void positionTooltip(int pScreenWidth, int pScreenHeight, Vector2i pTooltipPos, int pTooltipWidth, int pTooltipHeight) {
      if (pTooltipPos.x + pTooltipWidth > pScreenWidth) {
         pTooltipPos.x = Math.max(pTooltipPos.x - 24 - pTooltipWidth, 4);
      }

      int i = pTooltipHeight + 3;
      if (pTooltipPos.y + i > pScreenHeight) {
         pTooltipPos.y = pScreenHeight - i;
      }

   }
}