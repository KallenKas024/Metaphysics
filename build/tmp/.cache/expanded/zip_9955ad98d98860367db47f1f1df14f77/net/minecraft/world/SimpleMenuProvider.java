package net.minecraft.world;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;

public final class SimpleMenuProvider implements MenuProvider {
   private final Component title;
   private final MenuConstructor menuConstructor;

   public SimpleMenuProvider(MenuConstructor pMenuConstructor, Component pTitle) {
      this.menuConstructor = pMenuConstructor;
      this.title = pTitle;
   }

   public Component getDisplayName() {
      return this.title;
   }

   public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
      return this.menuConstructor.createMenu(pContainerId, pPlayerInventory, pPlayer);
   }
}