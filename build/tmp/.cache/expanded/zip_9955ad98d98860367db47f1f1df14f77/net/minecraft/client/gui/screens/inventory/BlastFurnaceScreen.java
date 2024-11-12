package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.screens.recipebook.BlastingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlastFurnaceScreen extends AbstractFurnaceScreen<BlastFurnaceMenu> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/blast_furnace.png");

   public BlastFurnaceScreen(BlastFurnaceMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, new BlastingRecipeBookComponent(), pPlayerInventory, pTitle, TEXTURE);
   }
}