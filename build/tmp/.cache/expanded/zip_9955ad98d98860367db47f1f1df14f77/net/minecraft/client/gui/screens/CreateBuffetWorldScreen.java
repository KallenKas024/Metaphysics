package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
   private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome");
   private final Screen parent;
   private final Consumer<Holder<Biome>> applySettings;
   final Registry<Biome> biomes;
   private CreateBuffetWorldScreen.BiomeList list;
   Holder<Biome> biome;
   private Button doneButton;

   public CreateBuffetWorldScreen(Screen pParent, WorldCreationContext pContext, Consumer<Holder<Biome>> pApplySettings) {
      super(Component.translatable("createWorld.customize.buffet.title"));
      this.parent = pParent;
      this.applySettings = pApplySettings;
      this.biomes = pContext.worldgenLoadContext().registryOrThrow(Registries.BIOME);
      Holder<Biome> holder = this.biomes.getHolder(Biomes.PLAINS).or(() -> {
         return this.biomes.holders().findAny();
      }).orElseThrow();
      this.biome = pContext.selectedDimensions().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(holder);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   protected void init() {
      this.list = new CreateBuffetWorldScreen.BiomeList();
      this.addWidget(this.list);
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280788_) -> {
         this.applySettings.accept(this.biome);
         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280789_) -> {
         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.list.setSelected(this.list.children().stream().filter((p_232738_) -> {
         return Objects.equals(p_232738_.biome, this.biome);
      }).findFirst().orElse((CreateBuffetWorldScreen.BiomeList.Entry)null));
   }

   void updateButtonValidity() {
      this.doneButton.active = this.list.getSelected() != null;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderDirtBackground(pGuiGraphics);
      this.list.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      pGuiGraphics.drawCenteredString(this.font, BIOME_SELECT_INFO, this.width / 2, 28, 10526880);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   @OnlyIn(Dist.CLIENT)
   class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
      BiomeList() {
         super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
         Collator collator = Collator.getInstance(Locale.getDefault());
         CreateBuffetWorldScreen.this.biomes.holders().map((p_205389_) -> {
            return new CreateBuffetWorldScreen.BiomeList.Entry(p_205389_);
         }).sorted(Comparator.comparing((p_203142_) -> {
            return p_203142_.name.getString();
         }, collator)).forEach((p_203138_) -> {
            this.addEntry(p_203138_);
         });
      }

      public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry pEntry) {
         super.setSelected(pEntry);
         if (pEntry != null) {
            CreateBuffetWorldScreen.this.biome = pEntry.biome;
         }

         CreateBuffetWorldScreen.this.updateButtonValidity();
      }

      @OnlyIn(Dist.CLIENT)
      class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
         final Holder.Reference<Biome> biome;
         final Component name;

         public Entry(Holder.Reference<Biome> pBiome) {
            this.biome = pBiome;
            ResourceLocation resourcelocation = pBiome.key().location();
            String s = resourcelocation.toLanguageKey("biome");
            if (Language.getInstance().has(s)) {
               this.name = Component.translatable(s);
            } else {
               this.name = Component.literal(resourcelocation.toString());
            }

         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.name);
         }

         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            pGuiGraphics.drawString(CreateBuffetWorldScreen.this.font, this.name, pLeft + 5, pTop + 2, 16777215);
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
               BiomeList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}