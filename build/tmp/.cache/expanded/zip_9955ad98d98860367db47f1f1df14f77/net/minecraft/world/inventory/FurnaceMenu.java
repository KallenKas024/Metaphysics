package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class FurnaceMenu extends AbstractFurnaceMenu {
   public FurnaceMenu(int pContainerId, Inventory pPlayerInventory) {
      super(MenuType.FURNACE, RecipeType.SMELTING, RecipeBookType.FURNACE, pContainerId, pPlayerInventory);
   }

   public FurnaceMenu(int pContainerId, Inventory pPlayerInventory, Container pFurnaceContainer, ContainerData pFurnaceData) {
      super(MenuType.FURNACE, RecipeType.SMELTING, RecipeBookType.FURNACE, pContainerId, pPlayerInventory, pFurnaceContainer, pFurnaceData);
   }
}