package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperimentsScreen extends Screen {
   private static final int MAIN_CONTENT_WIDTH = 310;
   private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
   private final Screen parent;
   private final PackRepository packRepository;
   private final Consumer<PackRepository> output;
   private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap<>();

   protected ExperimentsScreen(Screen pParent, PackRepository pPackRepository, Consumer<PackRepository> pOutput) {
      super(Component.translatable("experiments_screen.title"));
      this.parent = pParent;
      this.packRepository = pPackRepository;
      this.output = pOutput;

      for(Pack pack : pPackRepository.getAvailablePacks()) {
         if (pack.getPackSource() == PackSource.FEATURE) {
            this.packs.put(pack, pPackRepository.getSelectedPacks().contains(pack));
         }
      }

   }

   protected void init() {
      this.layout.addToHeader(new StringWidget(Component.translatable("selectWorld.experiments"), this.font));
      GridLayout.RowHelper gridlayout$rowhelper = this.layout.addToContents(new GridLayout()).createRowHelper(1);
      gridlayout$rowhelper.addChild((new MultiLineTextWidget(Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED), this.font)).setMaxWidth(310), gridlayout$rowhelper.newCellSettings().paddingBottom(15));
      SwitchGrid.Builder switchgrid$builder = SwitchGrid.builder(310).withInfoUnderneath(2, true).withRowSpacing(4);
      this.packs.forEach((p_270880_, p_270874_) -> {
         switchgrid$builder.addSwitch(getHumanReadableTitle(p_270880_), () -> {
            return this.packs.getBoolean(p_270880_);
         }, (p_270491_) -> {
            this.packs.put(p_270880_, p_270491_.booleanValue());
         }).withInfo(p_270880_.getDescription());
      });
      switchgrid$builder.build(gridlayout$rowhelper::addChild);
      GridLayout.RowHelper gridlayout$rowhelper1 = this.layout.addToFooter((new GridLayout()).columnSpacing(10)).createRowHelper(2);
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_DONE, (p_270336_) -> {
         this.onDone();
      }).build());
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_CANCEL, (p_274702_) -> {
         this.onClose();
      }).build());
      this.layout.visitWidgets((p_270313_) -> {
         AbstractWidget abstractwidget = this.addRenderableWidget(p_270313_);
      });
      this.repositionElements();
   }

   private static Component getHumanReadableTitle(Pack pPack) {
      String s = "dataPack." + pPack.getId() + ".name";
      return (Component)(I18n.exists(s) ? Component.translatable(s) : pPack.getTitle());
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   private void onDone() {
      List<Pack> list = new ArrayList<>(this.packRepository.getSelectedPacks());
      List<Pack> list1 = new ArrayList<>();
      this.packs.forEach((p_270540_, p_270780_) -> {
         list.remove(p_270540_);
         if (p_270780_) {
            list1.add(p_270540_);
         }

      });
      list.addAll(Lists.reverse(list1));
      this.packRepository.setSelected(list.stream().map(Pack::getId).toList());
      this.output.accept(this.packRepository);
   }

   protected void repositionElements() {
      this.layout.arrangeElements();
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
      pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
      int i = 32;
      pGuiGraphics.blit(BACKGROUND_LOCATION, 0, this.layout.getHeaderHeight(), 0.0F, 0.0F, this.width, this.height - this.layout.getHeaderHeight() - this.layout.getFooterHeight(), 32, 32);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}