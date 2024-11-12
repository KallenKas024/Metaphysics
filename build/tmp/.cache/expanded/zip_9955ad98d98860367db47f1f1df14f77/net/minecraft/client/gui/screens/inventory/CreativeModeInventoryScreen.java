package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
   /** The location of the creative inventory tabs texture */
   private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
   private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
   private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
   private static final int NUM_ROWS = 5;
   private static final int NUM_COLS = 9;
   private static final int TAB_WIDTH = 26;
   private static final int TAB_HEIGHT = 32;
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   static final SimpleContainer CONTAINER = new SimpleContainer(45);
   private static final Component TRASH_SLOT_TOOLTIP = Component.translatable("inventory.binSlot");
   private static final int TEXT_COLOR = 16777215;
   /** Currently selected creative inventory tab index. */
   private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
   /** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
   private float scrollOffs;
   /** True if the scrollbar is being dragged */
   private boolean scrolling;
   private EditBox searchBox;
   @Nullable
   private List<Slot> originalSlots;
   @Nullable
   private Slot destroyItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTextInput;
   private boolean hasClickedOutside;
   private final Set<TagKey<Item>> visibleTags = new HashSet<>();
   private final boolean displayOperatorCreativeTab;
   private final List<net.minecraftforge.client.gui.CreativeTabsScreenPage> pages = new java.util.ArrayList<>();
   private net.minecraftforge.client.gui.CreativeTabsScreenPage currentPage = new net.minecraftforge.client.gui.CreativeTabsScreenPage(new java.util.ArrayList<>());

   public CreativeModeInventoryScreen(Player pPlayer, FeatureFlagSet pEnabledFeatures, boolean pDisplayOperatorCreativeTab) {
      super(new CreativeModeInventoryScreen.ItemPickerMenu(pPlayer), pPlayer.getInventory(), CommonComponents.EMPTY);
      pPlayer.containerMenu = this.menu;
      this.imageHeight = 136;
      this.imageWidth = 195;
      this.displayOperatorCreativeTab = pDisplayOperatorCreativeTab;
      CreativeModeTabs.tryRebuildTabContents(pEnabledFeatures, this.hasPermissions(pPlayer), pPlayer.level().registryAccess());
   }

   private boolean hasPermissions(Player pPlayer) {
      return pPlayer.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
   }

   private void tryRefreshInvalidatedTabs(FeatureFlagSet pEnabledFeatures, boolean pHasPermissions, HolderLookup.Provider pHolders) {
      if (CreativeModeTabs.tryRebuildTabContents(pEnabledFeatures, pHasPermissions, pHolders)) {
         for(CreativeModeTab creativemodetab : CreativeModeTabs.allTabs()) {
            Collection<ItemStack> collection = creativemodetab.getDisplayItems();
            if (creativemodetab == selectedTab) {
               if (creativemodetab.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
                  this.selectTab(CreativeModeTabs.getDefaultTab());
               } else {
                  this.refreshCurrentTabContents(collection);
               }
            }
         }
      }

   }

   private void refreshCurrentTabContents(Collection<ItemStack> pItems) {
      int i = this.menu.getRowIndexForScroll(this.scrollOffs);
      (this.menu).items.clear();
      if (selectedTab.hasSearchBar()) {
         this.refreshSearchResults();
      } else {
         (this.menu).items.addAll(pItems);
      }

      this.scrollOffs = this.menu.getScrollForRowIndex(i);
      this.menu.scrollTo(this.scrollOffs);
   }

   public void containerTick() {
      super.containerTick();
      if (this.minecraft != null) {
         if (this.minecraft.player != null) {
            this.tryRefreshInvalidatedTabs(this.minecraft.player.connection.enabledFeatures(), this.hasPermissions(this.minecraft.player), this.minecraft.player.level().registryAccess());
         }

         if (!this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
         } else {
            this.searchBox.tick();
         }

      }
   }

   /**
    * Called when the mouse is clicked over a slot or outside the gui.
    */
   protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
      if (this.isCreativeSlot(pSlot)) {
         this.searchBox.moveCursorToEnd();
         this.searchBox.setHighlightPos(0);
      }

      boolean flag = pType == ClickType.QUICK_MOVE;
      pType = pSlotId == -999 && pType == ClickType.PICKUP ? ClickType.THROW : pType;
      if (pSlot == null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && pType != ClickType.QUICK_CRAFT) {
         if (!this.menu.getCarried().isEmpty() && this.hasClickedOutside) {
            if (pMouseButton == 0) {
               this.minecraft.player.drop(this.menu.getCarried(), true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
               this.menu.setCarried(ItemStack.EMPTY);
            }

            if (pMouseButton == 1) {
               ItemStack itemstack5 = this.menu.getCarried().split(1);
               this.minecraft.player.drop(itemstack5, true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack5);
            }
         }
      } else {
         if (pSlot != null && !pSlot.mayPickup(this.minecraft.player)) {
            return;
         }

         if (pSlot == this.destroyItemSlot && flag) {
            for(int j = 0; j < this.minecraft.player.inventoryMenu.getItems().size(); ++j) {
               this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, j);
            }
         } else if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            if (pSlot == this.destroyItemSlot) {
               this.menu.setCarried(ItemStack.EMPTY);
            } else if (pType == ClickType.THROW && pSlot != null && pSlot.hasItem()) {
               ItemStack itemstack = pSlot.remove(pMouseButton == 0 ? 1 : pSlot.getItem().getMaxStackSize());
               ItemStack itemstack1 = pSlot.getItem();
               this.minecraft.player.drop(itemstack, true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack);
               this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack1, ((CreativeModeInventoryScreen.SlotWrapper)pSlot).target.index);
            } else if (pType == ClickType.THROW && !this.menu.getCarried().isEmpty()) {
               this.minecraft.player.drop(this.menu.getCarried(), true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
               this.menu.setCarried(ItemStack.EMPTY);
            } else {
               this.minecraft.player.inventoryMenu.clicked(pSlot == null ? pSlotId : ((CreativeModeInventoryScreen.SlotWrapper)pSlot).target.index, pMouseButton, pType, this.minecraft.player);
               this.minecraft.player.inventoryMenu.broadcastChanges();
            }
         } else if (pType != ClickType.QUICK_CRAFT && pSlot.container == CONTAINER) {
            ItemStack itemstack4 = this.menu.getCarried();
            ItemStack itemstack7 = pSlot.getItem();
            if (pType == ClickType.SWAP) {
               if (!itemstack7.isEmpty()) {
                  this.minecraft.player.getInventory().setItem(pMouseButton, itemstack7.copyWithCount(itemstack7.getMaxStackSize()));
                  this.minecraft.player.inventoryMenu.broadcastChanges();
               }

               return;
            }

            if (pType == ClickType.CLONE) {
               if (this.menu.getCarried().isEmpty() && pSlot.hasItem()) {
                  ItemStack itemstack9 = pSlot.getItem();
                  this.menu.setCarried(itemstack9.copyWithCount(itemstack9.getMaxStackSize()));
               }

               return;
            }

            if (pType == ClickType.THROW) {
               if (!itemstack7.isEmpty()) {
                  ItemStack itemstack8 = itemstack7.copyWithCount(pMouseButton == 0 ? 1 : itemstack7.getMaxStackSize());
                  this.minecraft.player.drop(itemstack8, true);
                  this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack8);
               }

               return;
            }

            if (!itemstack4.isEmpty() && !itemstack7.isEmpty() && ItemStack.isSameItemSameTags(itemstack4, itemstack7)) {
               if (pMouseButton == 0) {
                  if (flag) {
                     itemstack4.setCount(itemstack4.getMaxStackSize());
                  } else if (itemstack4.getCount() < itemstack4.getMaxStackSize()) {
                     itemstack4.grow(1);
                  }
               } else {
                  itemstack4.shrink(1);
               }
            } else if (!itemstack7.isEmpty() && itemstack4.isEmpty()) {
               int l = flag ? itemstack7.getMaxStackSize() : itemstack7.getCount();
               this.menu.setCarried(itemstack7.copyWithCount(l));
            } else if (pMouseButton == 0) {
               this.menu.setCarried(ItemStack.EMPTY);
            } else if (!this.menu.getCarried().isEmpty()) {
               this.menu.getCarried().shrink(1);
            }
         } else if (this.menu != null) {
            ItemStack itemstack3 = pSlot == null ? ItemStack.EMPTY : this.menu.getSlot(pSlot.index).getItem();
            this.menu.clicked(pSlot == null ? pSlotId : pSlot.index, pMouseButton, pType, this.minecraft.player);
            if (AbstractContainerMenu.getQuickcraftHeader(pMouseButton) == 2) {
               for(int k = 0; k < 9; ++k) {
                  this.minecraft.gameMode.handleCreativeModeItemAdd(this.menu.getSlot(45 + k).getItem(), 36 + k);
               }
            } else if (pSlot != null) {
               ItemStack itemstack6 = this.menu.getSlot(pSlot.index).getItem();
               this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack6, pSlot.index - (this.menu).slots.size() + 9 + 36);
               int i = 45 + pMouseButton;
               if (pType == ClickType.SWAP) {
                  this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack3, i - (this.menu).slots.size() + 9 + 36);
               } else if (pType == ClickType.THROW && !itemstack3.isEmpty()) {
                  ItemStack itemstack2 = itemstack3.copyWithCount(pMouseButton == 0 ? 1 : itemstack3.getMaxStackSize());
                  this.minecraft.player.drop(itemstack2, true);
                  this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack2);
               }

               this.minecraft.player.inventoryMenu.broadcastChanges();
            }
         }
      }

   }

   private boolean isCreativeSlot(@Nullable Slot pSlot) {
      return pSlot != null && pSlot.container == CONTAINER;
   }

   protected void init() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         super.init();
         this.pages.clear();
         int tabIndex = 0;
         List<CreativeModeTab> currentPage = new java.util.ArrayList<>();
         for (CreativeModeTab sortedCreativeModeTab : net.minecraftforge.common.CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
            currentPage.add(sortedCreativeModeTab);
            tabIndex++;
            if (tabIndex == 10) {
               this.pages.add(new net.minecraftforge.client.gui.CreativeTabsScreenPage(currentPage));
               currentPage = new java.util.ArrayList<>();
               tabIndex = 0;
            }
         }
         if (tabIndex != 0) {
            this.pages.add(new net.minecraftforge.client.gui.CreativeTabsScreenPage(currentPage));
         }
         if (this.pages.isEmpty()) {
            this.currentPage = new net.minecraftforge.client.gui.CreativeTabsScreenPage(new java.util.ArrayList<>());
         } else {
            this.currentPage = this.pages.get(0);
         }
         if (this.pages.size() > 1) {
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("<"), b -> setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)))).pos(leftPos,  topPos - 50).size(20, 20).build());
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal(">"), b -> setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)))).pos(leftPos + imageWidth - 20, topPos - 50).size(20, 20).build());
         }
         this.currentPage = this.pages.stream().filter(page -> page.getVisibleTabs().contains(selectedTab)).findFirst().orElse(this.currentPage);
         if (!this.currentPage.getVisibleTabs().contains(selectedTab)) {
            selectedTab = this.currentPage.getVisibleTabs().get(0);
         }
         this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
         this.searchBox.setMaxLength(50);
         this.searchBox.setBordered(false);
         this.searchBox.setVisible(false);
         this.searchBox.setTextColor(16777215);
         this.addWidget(this.searchBox);
         CreativeModeTab creativemodetab = selectedTab;
         selectedTab = CreativeModeTabs.getDefaultTab();
         this.selectTab(creativemodetab);
         this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
         this.listener = new CreativeInventoryListener(this.minecraft);
         this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
         if (!selectedTab.shouldDisplay()) {
            this.selectTab(CreativeModeTabs.getDefaultTab());
         }
      } else {
         this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
      }

   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      int i = this.menu.getRowIndexForScroll(this.scrollOffs);
      String s = this.searchBox.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.searchBox.setValue(s);
      if (!this.searchBox.getValue().isEmpty()) {
         this.refreshSearchResults();
      }

      this.scrollOffs = this.menu.getScrollForRowIndex(i);
      this.menu.scrollTo(this.scrollOffs);
   }

   public void removed() {
      super.removed();
      if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
         this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
      }

   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char pCodePoint, int pModifiers) {
      if (this.ignoreTextInput) {
         return false;
      } else if (!selectedTab.hasSearchBar()) {
         return false;
      } else {
         String s = this.searchBox.getValue();
         if (this.searchBox.charTyped(pCodePoint, pModifiers)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
               this.refreshSearchResults();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      this.ignoreTextInput = false;
      if (!selectedTab.hasSearchBar()) {
         if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode)) {
            this.ignoreTextInput = true;
            this.selectTab(CreativeModeTabs.searchTab());
            return true;
         } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
         }
      } else {
         boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
         boolean flag1 = InputConstants.getKey(pKeyCode, pScanCode).getNumericKeyValue().isPresent();
         if (flag && flag1 && this.checkHotbarKeyPressed(pKeyCode, pScanCode)) {
            this.ignoreTextInput = true;
            return true;
         } else {
            String s = this.searchBox.getValue();
            if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
               if (!Objects.equals(s, this.searchBox.getValue())) {
                  this.refreshSearchResults();
               }

               return true;
            } else {
               return this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256 ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
         }
      }
   }

   /**
    * Called when a keyboard key is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the released key.
    * @param pScanCode the scan code of the released key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
      this.ignoreTextInput = false;
      return super.keyReleased(pKeyCode, pScanCode, pModifiers);
   }

   private void refreshSearchResults() {
      if (!selectedTab.hasSearchBar()) return;
      (this.menu).items.clear();
      this.visibleTags.clear();
      String s = this.searchBox.getValue();
      if (s.isEmpty()) {
         (this.menu).items.addAll(selectedTab.getDisplayItems());
      } else {
         SearchTree<ItemStack> searchtree;
         if (s.startsWith("#")) {
            s = s.substring(1);
            searchtree = this.minecraft.getSearchTree(net.minecraftforge.client.CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab));
            this.updateVisibleTags(s);
         } else {
            searchtree = this.minecraft.getSearchTree(net.minecraftforge.client.CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab));
         }

         (this.menu).items.addAll(searchtree.search(s.toLowerCase(Locale.ROOT)));
      }

      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
   }

   private void updateVisibleTags(String pSearch) {
      int i = pSearch.indexOf(58);
      Predicate<ResourceLocation> predicate;
      if (i == -1) {
         predicate = (p_98609_) -> {
            return p_98609_.getPath().contains(pSearch);
         };
      } else {
         String s = pSearch.substring(0, i).trim();
         String s1 = pSearch.substring(i + 1).trim();
         predicate = (p_98606_) -> {
            return p_98606_.getNamespace().contains(s) && p_98606_.getPath().contains(s1);
         };
      }

      BuiltInRegistries.ITEM.getTagNames().filter((p_205410_) -> {
         return predicate.test(p_205410_.location());
      }).forEach(this.visibleTags::add);
   }

   protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
      if (selectedTab.showTitle()) {
         com.mojang.blaze3d.systems.RenderSystem.disableBlend();
         pGuiGraphics.drawString(this.font, selectedTab.getDisplayName(), 8, 6, selectedTab.getLabelColor(), false);
      }

   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (pButton == 0) {
         double d0 = pMouseX - (double)this.leftPos;
         double d1 = pMouseY - (double)this.topPos;

         for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
            if (this.checkTabClicked(creativemodetab, d0, d1)) {
               return true;
            }
         }

         if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(pMouseX, pMouseY)) {
            this.scrolling = this.canScroll();
            return true;
         }
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton);
   }

   /**
    * Called when a mouse button is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was released.
    */
   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (pButton == 0) {
         double d0 = pMouseX - (double)this.leftPos;
         double d1 = pMouseY - (double)this.topPos;
         this.scrolling = false;

         for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
            if (this.checkTabClicked(creativemodetab, d0, d1)) {
               this.selectTab(creativemodetab);
               return true;
            }
         }
      }

      return super.mouseReleased(pMouseX, pMouseY, pButton);
   }

   /**
    * Returns (if you are not on the inventoryTab) and (the flag isn't set) and (you have more than 1 page of items).
    */
   private boolean canScroll() {
      return selectedTab.canScroll() && this.menu.canScroll();
   }

   /**
    * Sets the current creative tab, restructuring the GUI as needed.
    */
   private void selectTab(CreativeModeTab pTab) {
      CreativeModeTab creativemodetab = selectedTab;
      selectedTab = pTab;
      slotColor = pTab.getSlotColor();
      this.quickCraftSlots.clear();
      (this.menu).items.clear();
      this.clearDraggingState();
      if (selectedTab.getType() == CreativeModeTab.Type.HOTBAR) {
         HotbarManager hotbarmanager = this.minecraft.getHotbarManager();

         for(int i = 0; i < 9; ++i) {
            Hotbar hotbar = hotbarmanager.get(i);
            if (hotbar.isEmpty()) {
               for(int j = 0; j < 9; ++j) {
                  if (j == i) {
                     ItemStack itemstack = new ItemStack(Items.PAPER);
                     itemstack.getOrCreateTagElement("CustomCreativeLock");
                     Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
                     Component component1 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                     itemstack.setHoverName(Component.translatable("inventory.hotbarInfo", component1, component));
                     (this.menu).items.add(itemstack);
                  } else {
                     (this.menu).items.add(ItemStack.EMPTY);
                  }
               }
            } else {
               (this.menu).items.addAll(hotbar);
            }
         }
      } else if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
         (this.menu).items.addAll(selectedTab.getDisplayItems());
      }

      if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
         AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
         if (this.originalSlots == null) {
            this.originalSlots = ImmutableList.copyOf((this.menu).slots);
         }

         (this.menu).slots.clear();

         for(int k = 0; k < abstractcontainermenu.slots.size(); ++k) {
            int l;
            int i1;
            if (k >= 5 && k < 9) {
               int k1 = k - 5;
               int i2 = k1 / 2;
               int k2 = k1 % 2;
               l = 54 + i2 * 54;
               i1 = 6 + k2 * 27;
            } else if (k >= 0 && k < 5) {
               l = -2000;
               i1 = -2000;
            } else if (k == 45) {
               l = 35;
               i1 = 20;
            } else {
               int j1 = k - 9;
               int l1 = j1 % 9;
               int j2 = j1 / 9;
               l = 9 + l1 * 18;
               if (k >= 36) {
                  i1 = 112;
               } else {
                  i1 = 54 + j2 * 18;
               }
            }

            Slot slot = new CreativeModeInventoryScreen.SlotWrapper(abstractcontainermenu.slots.get(k), k, l, i1);
            (this.menu).slots.add(slot);
         }

         this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
         (this.menu).slots.add(this.destroyItemSlot);
      } else if (creativemodetab.getType() == CreativeModeTab.Type.INVENTORY) {
         (this.menu).slots.clear();
         (this.menu).slots.addAll(this.originalSlots);
         this.originalSlots = null;
      }

      if (selectedTab.hasSearchBar()) {
         this.searchBox.setVisible(true);
         this.searchBox.setCanLoseFocus(false);
         this.searchBox.setFocused(true);
         if (creativemodetab != pTab) {
            this.searchBox.setValue("");
         }
         this.searchBox.setWidth(selectedTab.getSearchBarWidth());
         this.searchBox.setX(this.leftPos + (82 /*default left*/ + 89 /*default width*/) - this.searchBox.getWidth());

         this.refreshSearchResults();
      } else {
         this.searchBox.setVisible(false);
         this.searchBox.setCanLoseFocus(true);
         this.searchBox.setFocused(false);
         this.searchBox.setValue("");
      }

      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
   }

   /**
    * Called when the mouse wheel is scrolled within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pDelta the scrolling delta.
    */
   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      if (!this.canScroll()) {
         return false;
      } else {
         this.scrollOffs = this.menu.subtractInputFromScroll(this.scrollOffs, pDelta);
         this.menu.scrollTo(this.scrollOffs);
         return true;
      }
   }

   protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
      boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
      this.hasClickedOutside = flag && !this.checkTabClicked(selectedTab, pMouseX, pMouseY);
      return this.hasClickedOutside;
   }

   protected boolean insideScrollbar(double pMouseX, double pMouseY) {
      int i = this.leftPos;
      int j = this.topPos;
      int k = i + 175;
      int l = j + 18;
      int i1 = k + 14;
      int j1 = l + 112;
      return pMouseX >= (double)k && pMouseY >= (double)l && pMouseX < (double)i1 && pMouseY < (double)j1;
   }

   /**
    * Called when the mouse is dragged within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that is being dragged.
    * @param pDragX the X distance of the drag.
    * @param pDragY the Y distance of the drag.
    */
   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (this.scrolling) {
         int i = this.topPos + 18;
         int j = i + 112;
         this.scrollOffs = ((float)pMouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.menu.scrollTo(this.scrollOffs);
         return true;
      } else {
         return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
      }
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

      for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
         if (this.checkTabHovering(pGuiGraphics, creativemodetab, pMouseX, pMouseY)) {
            break;
         }
      }

      if (this.destroyItemSlot != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, (double)pMouseX, (double)pMouseY)) {
         pGuiGraphics.renderTooltip(this.font, TRASH_SLOT_TOOLTIP, pMouseX, pMouseY);
      }

      if (this.pages.size() != 1) {
         Component page = Component.literal(String.format("%d / %d", this.pages.indexOf(this.currentPage) + 1, this.pages.size()));
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0F, 0F, 300F);
         pGuiGraphics.drawString(font, page.getVisualOrderText(), leftPos + (imageWidth / 2) - (font.width(page) / 2), topPos - 44, -1);
         pGuiGraphics.pose().popPose();
      }

      com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
   }

   public List<Component> getTooltipFromContainerItem(ItemStack pStack) {
      boolean flag = this.hoveredSlot != null && this.hoveredSlot instanceof CreativeModeInventoryScreen.CustomCreativeSlot;
      boolean flag1 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
      boolean flag2 = selectedTab.hasSearchBar();
      TooltipFlag.Default tooltipflag$default = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
      TooltipFlag tooltipflag = flag ? tooltipflag$default.asCreative() : tooltipflag$default;
      List<Component> list = pStack.getTooltipLines(this.minecraft.player, tooltipflag);
      if (flag1 && flag) {
         return list;
      } else {
         List<Component> list1 = Lists.newArrayList(list);
         if (flag2 && flag) {
            this.visibleTags.forEach((p_205407_) -> {
               if (pStack.is(p_205407_)) {
                  list1.add(1, Component.literal("#" + p_205407_.location()).withStyle(ChatFormatting.DARK_PURPLE));
               }

            });
         }

         int i = 1;

         for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
            if (!creativemodetab.hasSearchBar() && creativemodetab.contains(pStack)) {
               list1.add(i++, creativemodetab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
         }

         return list1;
      }
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
         if (creativemodetab != selectedTab) {
            this.renderTabButton(pGuiGraphics, creativemodetab);
         }
      }

      pGuiGraphics.blit(selectedTab.getBackgroundLocation(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      int j = this.leftPos + 175;
      int k = this.topPos + 18;
      int i = k + 112;
      if (selectedTab.canScroll()) {
         pGuiGraphics.blit(selectedTab.getTabsImage(), j, k + (int)((float)(i - k - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
      }

      if (currentPage.getVisibleTabs().contains(selectedTab)) //Forge: only display tab selection when the selected tab is on the current page
      this.renderTabButton(pGuiGraphics, selectedTab);
      if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
         InventoryScreen.renderEntityInInventoryFollowsMouse(pGuiGraphics, this.leftPos + 88, this.topPos + 45, 20, (float)(this.leftPos + 88 - pMouseX), (float)(this.topPos + 45 - 30 - pMouseY), this.minecraft.player);
      }

   }

   private int getTabX(CreativeModeTab pTab) {
      int i = currentPage.getColumn(pTab);
      int j = 27;
      int k = 27 * i;
      if (pTab.isAlignedRight()) {
         k = this.imageWidth - 27 * (7 - i) + 1;
      }

      return k;
   }

   private int getTabY(CreativeModeTab pTab) {
      int i = 0;
      if (currentPage.isTop(pTab)) {
         i -= 32;
      } else {
         i += this.imageHeight;
      }

      return i;
   }

   protected boolean checkTabClicked(CreativeModeTab pCreativeModeTab, double pRelativeMouseX, double pRelativeMouseY) {
      int i = this.getTabX(pCreativeModeTab);
      int j = this.getTabY(pCreativeModeTab);
      return pRelativeMouseX >= (double)i && pRelativeMouseX <= (double)(i + 26) && pRelativeMouseY >= (double)j && pRelativeMouseY <= (double)(j + 32);
   }

   protected boolean checkTabHovering(GuiGraphics pGuiGraphics, CreativeModeTab pCreativeModeTab, int pMouseX, int pMouseY) {
      int i = this.getTabX(pCreativeModeTab);
      int j = this.getTabY(pCreativeModeTab);
      if (this.isHovering(i + 3, j + 3, 21, 27, (double)pMouseX, (double)pMouseY)) {
         pGuiGraphics.renderTooltip(this.font, pCreativeModeTab.getDisplayName(), pMouseX, pMouseY);
         return true;
      } else {
         return false;
      }
   }

   protected void renderTabButton(GuiGraphics pGuiGraphics, CreativeModeTab pCreativeModeTab) {
      boolean flag = pCreativeModeTab == selectedTab;
      boolean flag1 = currentPage.isTop(pCreativeModeTab);
      int i = currentPage.getColumn(pCreativeModeTab);
      int j = i * 26;
      int k = 0;
      int l = this.leftPos + this.getTabX(pCreativeModeTab);
      int i1 = this.topPos;
      int j1 = 32;
      if (flag) {
         k += 32;
      }

      if (flag1) {
         i1 -= 28;
      } else {
         k += 64;
         i1 += this.imageHeight - 4;
      }

      com.mojang.blaze3d.systems.RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
      pGuiGraphics.blit(pCreativeModeTab.getTabsImage(), l, i1, j, k, 26, 32);
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
      l += 5;
      i1 += 8 + (flag1 ? 1 : -1);
      ItemStack itemstack = pCreativeModeTab.getIconItem();
      pGuiGraphics.renderItem(itemstack, l, i1);
      pGuiGraphics.renderItemDecorations(this.font, itemstack, l, i1);
      pGuiGraphics.pose().popPose();
   }

   public boolean isInventoryOpen() {
      return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
   }

   public static void handleHotbarLoadOrSave(Minecraft pClient, int pIndex, boolean pLoad, boolean pSave) {
      LocalPlayer localplayer = pClient.player;
      HotbarManager hotbarmanager = pClient.getHotbarManager();
      Hotbar hotbar = hotbarmanager.get(pIndex);
      if (pLoad) {
         for(int i = 0; i < Inventory.getSelectionSize(); ++i) {
            ItemStack itemstack = hotbar.get(i);
            ItemStack itemstack1 = itemstack.isItemEnabled(localplayer.level().enabledFeatures()) ? itemstack.copy() : ItemStack.EMPTY;
            localplayer.getInventory().setItem(i, itemstack1);
            pClient.gameMode.handleCreativeModeItemAdd(itemstack1, 36 + i);
         }

         localplayer.inventoryMenu.broadcastChanges();
      } else if (pSave) {
         for(int j = 0; j < Inventory.getSelectionSize(); ++j) {
            hotbar.set(j, localplayer.getInventory().getItem(j).copy());
         }

         Component component = pClient.options.keyHotbarSlots[pIndex].getTranslatedKeyMessage();
         Component component1 = pClient.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
         Component component2 = Component.translatable("inventory.hotbarSaved", component1, component);
         pClient.gui.setOverlayMessage(component2, false);
         pClient.getNarrator().sayNow(component2);
         hotbarmanager.save();
      }

   }

   public net.minecraftforge.client.gui.CreativeTabsScreenPage getCurrentPage() {
      return currentPage;
   }

   public void setCurrentPage(net.minecraftforge.client.gui.CreativeTabsScreenPage currentPage) {
      this.currentPage = currentPage;
   }

   @OnlyIn(Dist.CLIENT)
   static class CustomCreativeSlot extends Slot {
      public CustomCreativeSlot(Container pContainer, int pSlot, int pX, int pY) {
         super(pContainer, pSlot, pX, pY);
      }

      /**
       * Return whether this slot's stack can be taken from this slot.
       */
      public boolean mayPickup(Player pPlayer) {
         ItemStack itemstack = this.getItem();
         if (super.mayPickup(pPlayer) && !itemstack.isEmpty()) {
            return itemstack.isItemEnabled(pPlayer.level().enabledFeatures()) && itemstack.getTagElement("CustomCreativeLock") == null;
         } else {
            return itemstack.isEmpty();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ItemPickerMenu extends AbstractContainerMenu {
      /** The list of items in this container. */
      public final NonNullList<ItemStack> items = NonNullList.create();
      private final AbstractContainerMenu inventoryMenu;

      public ItemPickerMenu(Player pPlayer) {
         super((MenuType<?>)null, 0);
         this.inventoryMenu = pPlayer.inventoryMenu;
         Inventory inventory = pPlayer.getInventory();

         for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 9; ++j) {
               this.addSlot(new CreativeModeInventoryScreen.CustomCreativeSlot(CreativeModeInventoryScreen.CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
            }
         }

         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 9 + k * 18, 112));
         }

         this.scrollTo(0.0F);
      }

      /**
       * Determines whether supplied player can use this container
       */
      public boolean stillValid(Player pPlayer) {
         return true;
      }

      protected int calculateRowCount() {
         return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
      }

      protected int getRowIndexForScroll(float pScrollOffs) {
         return Math.max((int)((double)(pScrollOffs * (float)this.calculateRowCount()) + 0.5D), 0);
      }

      protected float getScrollForRowIndex(int pRowIndex) {
         return Mth.clamp((float)pRowIndex / (float)this.calculateRowCount(), 0.0F, 1.0F);
      }

      protected float subtractInputFromScroll(float pScrollOffs, double pInput) {
         return Mth.clamp(pScrollOffs - (float)(pInput / (double)this.calculateRowCount()), 0.0F, 1.0F);
      }

      /**
       * Updates the gui slot's ItemStacks based on scroll position.
       */
      public void scrollTo(float pPos) {
         int i = this.getRowIndexForScroll(pPos);

         for(int j = 0; j < 5; ++j) {
            for(int k = 0; k < 9; ++k) {
               int l = k + (j + i) * 9;
               if (l >= 0 && l < this.items.size()) {
                  CreativeModeInventoryScreen.CONTAINER.setItem(k + j * 9, this.items.get(l));
               } else {
                  CreativeModeInventoryScreen.CONTAINER.setItem(k + j * 9, ItemStack.EMPTY);
               }
            }
         }

      }

      public boolean canScroll() {
         return this.items.size() > 45;
      }

      /**
       * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
       * inventory and the other inventory(s).
       */
      public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
         if (pIndex >= this.slots.size() - 9 && pIndex < this.slots.size()) {
            Slot slot = this.slots.get(pIndex);
            if (slot != null && slot.hasItem()) {
               slot.setByPlayer(ItemStack.EMPTY);
            }
         }

         return ItemStack.EMPTY;
      }

      /**
       * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
       * is null for the initial slot that was double-clicked.
       */
      public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
         return pSlot.container != CreativeModeInventoryScreen.CONTAINER;
      }

      /**
       * Returns {@code true} if the player can "drag-spilt" items into this slot. Returns {@code true} by default.
       * Called to check if the slot can be added to a list of Slots to split the held ItemStack across.
       */
      public boolean canDragTo(Slot pSlot) {
         return pSlot.container != CreativeModeInventoryScreen.CONTAINER;
      }

      public ItemStack getCarried() {
         return this.inventoryMenu.getCarried();
      }

      public void setCarried(ItemStack pStack) {
         this.inventoryMenu.setCarried(pStack);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class SlotWrapper extends Slot {
      final Slot target;

      public SlotWrapper(Slot pSlot, int pIndex, int pX, int pY) {
         super(pSlot.container, pIndex, pX, pY);
         this.target = pSlot;
      }

      public void onTake(Player pPlayer, ItemStack pStack) {
         this.target.onTake(pPlayer, pStack);
      }

      /**
       * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
       */
      public boolean mayPlace(ItemStack pStack) {
         return this.target.mayPlace(pStack);
      }

      /**
       * Helper function to get the stack in the slot.
       */
      public ItemStack getItem() {
         return this.target.getItem();
      }

      /**
       * Returns if this slot contains a stack.
       */
      public boolean hasItem() {
         return this.target.hasItem();
      }

      public void setByPlayer(ItemStack pStack) {
         this.target.setByPlayer(pStack);
      }

      /**
       * Helper method to put a stack in the slot.
       */
      public void set(ItemStack pStack) {
         this.target.set(pStack);
      }

      /**
       * Called when the stack in a Slot changes
       */
      public void setChanged() {
         this.target.setChanged();
      }

      /**
       * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
       * case of armor slots)
       */
      public int getMaxStackSize() {
         return this.target.getMaxStackSize();
      }

      public int getMaxStackSize(ItemStack pStack) {
         return this.target.getMaxStackSize(pStack);
      }

      @Nullable
      public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
         return this.target.getNoItemIcon();
      }

      /**
       * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
       * stack.
       */
      public ItemStack remove(int pAmount) {
         return this.target.remove(pAmount);
      }

      /**
       * Actually only call when we want to render the white square effect over the slots. Return always True, except
       * for the armor slot of the Donkey/Mule (we can't interact with the Undead and Skeleton horses)
       */
      public boolean isActive() {
         return this.target.isActive();
      }

      /**
       * Return whether this slot's stack can be taken from this slot.
       */
      public boolean mayPickup(Player pPlayer) {
         return this.target.mayPickup(pPlayer);
      }

      @Override
      public int getSlotIndex() {
         return this.target.getSlotIndex();
      }

      @Override
      public boolean isSameInventory(Slot other) {
         return this.target.isSameInventory(other);
      }

      @Override
      public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
         this.target.setBackground(atlas, sprite);
         return this;
      }
   }
}
