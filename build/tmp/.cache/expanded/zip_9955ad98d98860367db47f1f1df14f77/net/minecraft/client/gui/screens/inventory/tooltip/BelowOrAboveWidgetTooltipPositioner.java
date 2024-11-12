package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class BelowOrAboveWidgetTooltipPositioner implements ClientTooltipPositioner {
   private final AbstractWidget widget;

   public BelowOrAboveWidgetTooltipPositioner(AbstractWidget pWidget) {
      this.widget = pWidget;
   }

   public Vector2ic positionTooltip(int pScreenWidth, int pScreenHeight, int pMouseX, int pMouseY, int pTooltipWidth, int pTooltipHeight) {
      Vector2i vector2i = new Vector2i();
      vector2i.x = this.widget.getX() + 3;
      vector2i.y = this.widget.getY() + this.widget.getHeight() + 3 + 1;
      if (vector2i.y + pTooltipHeight + 3 > pScreenHeight) {
         vector2i.y = this.widget.getY() - pTooltipHeight - 3 - 1;
      }

      if (vector2i.x + pTooltipWidth > pScreenWidth) {
         vector2i.x = Math.max(this.widget.getX() + this.widget.getWidth() - pTooltipWidth - 3, 4);
      }

      return vector2i;
   }
}