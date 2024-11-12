package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.screens.recipebook.SmokingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmokerScreen extends AbstractFurnaceScreen<SmokerMenu> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/smoker.png");

   public SmokerScreen(SmokerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, new SmokingRecipeBookComponent(), pPlayerInventory, pTitle, TEXTURE);
   }
}