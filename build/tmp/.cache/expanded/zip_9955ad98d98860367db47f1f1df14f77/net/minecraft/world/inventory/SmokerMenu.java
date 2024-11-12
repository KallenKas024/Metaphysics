package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class SmokerMenu extends AbstractFurnaceMenu {
   public SmokerMenu(int pContainerId, Inventory pPlayerInventory) {
      super(MenuType.SMOKER, RecipeType.SMOKING, RecipeBookType.SMOKER, pContainerId, pPlayerInventory);
   }

   public SmokerMenu(int pContainerId, Inventory pPlayerInventory, Container pSmokerContainer, ContainerData pSmokerData) {
      super(MenuType.SMOKER, RecipeType.SMOKING, RecipeBookType.SMOKER, pContainerId, pPlayerInventory, pSmokerContainer, pSmokerData);
   }
}