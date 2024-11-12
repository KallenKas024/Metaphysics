package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public interface ClientTooltipPositioner {
   Vector2ic positionTooltip(int pScreenWidth, int pScreenHeight, int pMouseX, int pMouseY, int pTooltipWidth, int pTooltipHeight);
}