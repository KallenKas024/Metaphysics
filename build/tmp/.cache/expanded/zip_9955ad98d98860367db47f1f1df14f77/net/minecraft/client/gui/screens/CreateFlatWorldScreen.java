package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateFlatWorldScreen extends Screen {
   private static final int SLOT_TEX_SIZE = 128;
   private static final int SLOT_BG_SIZE = 18;
   private static final int SLOT_STAT_HEIGHT = 20;
   private static final int SLOT_BG_X = 1;
   private static final int SLOT_BG_Y = 1;
   private static final int SLOT_FG_X = 2;
   private static final int SLOT_FG_Y = 2;
   protected final CreateWorldScreen parent;
   private final Consumer<FlatLevelGeneratorSettings> applySettings;
   FlatLevelGeneratorSettings generator;
   /** The text used to identify the material for a layer */
   private Component columnType;
   /** The text used to identify the height of a layer */
   private Component columnHeight;
   private CreateFlatWorldScreen.DetailsList list;
   /** The remove layer button */
   private Button deleteLayerButton;

   public CreateFlatWorldScreen(CreateWorldScreen pParent, Consumer<FlatLevelGeneratorSettings> pApplySettings, FlatLevelGeneratorSettings pGenerator) {
      super(Component.translatable("createWorld.customize.flat.title"));
      this.parent = pParent;
      this.applySettings = pApplySettings;
      this.generator = pGenerator;
   }

   public FlatLevelGeneratorSettings settings() {
      return this.generator;
   }

   public void setConfig(FlatLevelGeneratorSettings pGenerator) {
      this.generator = pGenerator;
   }

   protected void init() {
      this.columnType = Component.translatable("createWorld.customize.flat.tile");
      this.columnHeight = Component.translatable("createWorld.customize.flat.height");
      this.list = new CreateFlatWorldScreen.DetailsList();
      this.addWidget(this.list);
      this.deleteLayerButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.flat.removeLayer"), (p_95845_) -> {
         if (this.hasValidSelection()) {
            List<FlatLayerInfo> list = this.generator.getLayersInfo();
            int i = this.list.children().indexOf(this.list.getSelected());
            int j = list.size() - i - 1;
            list.remove(j);
            this.list.setSelected(list.isEmpty() ? null : this.list.children().get(Math.min(i, list.size() - 1)));
            this.generator.updateLayers();
            this.list.resetRows();
            this.updateButtonValidity();
         }
      }).bounds(this.width / 2 - 155, this.height - 52, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets"), (p_280790_) -> {
         this.minecraft.setScreen(new PresetFlatWorldScreen(this));
         this.generator.updateLayers();
         this.updateButtonValidity();
      }).bounds(this.width / 2 + 5, this.height - 52, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280791_) -> {
         this.applySettings.accept(this.generator);
         this.minecraft.setScreen(this.parent);
         this.generator.updateLayers();
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280792_) -> {
         this.minecraft.setScreen(this.parent);
         this.generator.updateLayers();
      }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.generator.updateLayers();
      this.updateButtonValidity();
   }

   /**
    * Would update whether the edit and remove buttons are enabled, but is currently disabled and always disables the
    * buttons (which are invisible anyway).
    */
   void updateButtonValidity() {
      this.deleteLayerButton.active = this.hasValidSelection();
   }

   /**
    * Returns whether there is a valid layer selection
    */
   private boolean hasValidSelection() {
      return this.list.getSelected() != null;
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
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
      this.list.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      int i = this.width / 2 - 92 - 16;
      pGuiGraphics.drawString(this.font, this.columnType, i, 32, 16777215);
      pGuiGraphics.drawString(this.font, this.columnHeight, i + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   @OnlyIn(Dist.CLIENT)
   class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
      static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");

      public DetailsList() {
         super(CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height, 43, CreateFlatWorldScreen.this.height - 60, 24);

         for(int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++i) {
            this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
         }

      }

      public void setSelected(@Nullable CreateFlatWorldScreen.DetailsList.Entry pEntry) {
         super.setSelected(pEntry);
         CreateFlatWorldScreen.this.updateButtonValidity();
      }

      protected int getScrollbarPosition() {
         return this.width - 70;
      }

      public void resetRows() {
         int i = this.children().indexOf(this.getSelected());
         this.clearEntries();

         for(int j = 0; j < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++j) {
            this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
         }

         List<CreateFlatWorldScreen.DetailsList.Entry> list = this.children();
         if (i >= 0 && i < list.size()) {
            this.setSelected(list.get(i));
         }

      }

      @OnlyIn(Dist.CLIENT)
      class Entry extends ObjectSelectionList.Entry<CreateFlatWorldScreen.DetailsList.Entry> {
         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            FlatLayerInfo flatlayerinfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - pIndex - 1);
            BlockState blockstate = flatlayerinfo.getBlockState();
            ItemStack itemstack = this.getDisplayItem(blockstate);
            this.blitSlot(pGuiGraphics, pLeft, pTop, itemstack);
            pGuiGraphics.drawString(CreateFlatWorldScreen.this.font, itemstack.getHoverName(), pLeft + 18 + 5, pTop + 3, 16777215, false);
            Component component;
            if (pIndex == 0) {
               component = Component.translatable("createWorld.customize.flat.layer.top", flatlayerinfo.getHeight());
            } else if (pIndex == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
               component = Component.translatable("createWorld.customize.flat.layer.bottom", flatlayerinfo.getHeight());
            } else {
               component = Component.translatable("createWorld.customize.flat.layer", flatlayerinfo.getHeight());
            }

            pGuiGraphics.drawString(CreateFlatWorldScreen.this.font, component, pLeft + 2 + 213 - CreateFlatWorldScreen.this.font.width(component), pTop + 3, 16777215, false);
         }

         private ItemStack getDisplayItem(BlockState pState) {
            Item item = pState.getBlock().asItem();
            if (item == Items.AIR) {
               if (pState.is(Blocks.WATER)) {
                  item = Items.WATER_BUCKET;
               } else if (pState.is(Blocks.LAVA)) {
                  item = Items.LAVA_BUCKET;
               }
            }

            return new ItemStack(item);
         }

         public Component getNarration() {
            FlatLayerInfo flatlayerinfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - DetailsList.this.children().indexOf(this) - 1);
            ItemStack itemstack = this.getDisplayItem(flatlayerinfo.getBlockState());
            return (Component)(!itemstack.isEmpty() ? Component.translatable("narrator.select", itemstack.getHoverName()) : CommonComponents.EMPTY);
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
               DetailsList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }

         private void blitSlot(GuiGraphics pGuiGraphics, int pX, int pY, ItemStack pStack) {
            this.blitSlotBg(pGuiGraphics, pX + 1, pY + 1);
            if (!pStack.isEmpty()) {
               pGuiGraphics.renderFakeItem(pStack, pX + 2, pY + 2);
            }

         }

         private void blitSlotBg(GuiGraphics pGuiGraphics, int pX, int pY) {
            pGuiGraphics.blit(CreateFlatWorldScreen.DetailsList.STATS_ICON_LOCATION, pX, pY, 0, 0.0F, 0.0F, 18, 18, 128, 128);
         }
      }
   }
}