package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class BlastFurnaceMenu extends AbstractFurnaceMenu {
   public BlastFurnaceMenu(int pContainerId, Inventory pPlayerInventory) {
      super(MenuType.BLAST_FURNACE, RecipeType.BLASTING, RecipeBookType.BLAST_FURNACE, pContainerId, pPlayerInventory);
   }

   public BlastFurnaceMenu(int pContainerId, Inventory pPlayerInventory, Container pBlastFurnaceContainer, ContainerData pBlastFurnaceData) {
      super(MenuType.BLAST_FURNACE, RecipeType.BLASTING, RecipeBookType.BLAST_FURNACE, pContainerId, pPlayerInventory, pBlastFurnaceContainer, pBlastFurnaceData);
   }
}