package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PresetFlatWorldScreen extends Screen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int SLOT_TEX_SIZE = 128;
   private static final int SLOT_BG_SIZE = 18;
   private static final int SLOT_STAT_HEIGHT = 20;
   private static final int SLOT_BG_X = 1;
   private static final int SLOT_BG_Y = 1;
   private static final int SLOT_FG_X = 2;
   private static final int SLOT_FG_Y = 2;
   private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
   public static final Component UNKNOWN_PRESET = Component.translatable("flat_world_preset.unknown");
   /** The parent GUI */
   private final CreateFlatWorldScreen parent;
   private Component shareText;
   private Component listText;
   private PresetFlatWorldScreen.PresetsList list;
   private Button selectButton;
   EditBox export;
   FlatLevelGeneratorSettings settings;

   public PresetFlatWorldScreen(CreateFlatWorldScreen pParent) {
      super(Component.translatable("createWorld.customize.presets.title"));
      this.parent = pParent;
   }

   @Nullable
   private static FlatLayerInfo getLayerInfoFromString(HolderGetter<Block> pBlockGetter, String pLayerInfo, int pCurrentHeight) {
      List<String> list = Splitter.on('*').limit(2).splitToList(pLayerInfo);
      int i;
      String s;
      if (list.size() == 2) {
         s = list.get(1);

         try {
            i = Math.max(Integer.parseInt(list.get(0)), 0);
         } catch (NumberFormatException numberformatexception) {
            LOGGER.error("Error while parsing flat world string", (Throwable)numberformatexception);
            return null;
         }
      } else {
         s = list.get(0);
         i = 1;
      }

      int j = Math.min(pCurrentHeight + i, DimensionType.Y_SIZE);
      int k = j - pCurrentHeight;

      Optional<Holder.Reference<Block>> optional;
      try {
         optional = pBlockGetter.get(ResourceKey.create(Registries.BLOCK, new ResourceLocation(s)));
      } catch (Exception exception) {
         LOGGER.error("Error while parsing flat world string", (Throwable)exception);
         return null;
      }

      if (optional.isEmpty()) {
         LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)s);
         return null;
      } else {
         return new FlatLayerInfo(k, optional.get().value());
      }
   }

   private static List<FlatLayerInfo> getLayersInfoFromString(HolderGetter<Block> pBlockGetter, String pLayerInfo) {
      List<FlatLayerInfo> list = Lists.newArrayList();
      String[] astring = pLayerInfo.split(",");
      int i = 0;

      for(String s : astring) {
         FlatLayerInfo flatlayerinfo = getLayerInfoFromString(pBlockGetter, s, i);
         if (flatlayerinfo == null) {
            return Collections.emptyList();
         }

         list.add(flatlayerinfo);
         i += flatlayerinfo.getHeight();
      }

      return list;
   }

   public static FlatLevelGeneratorSettings fromString(HolderGetter<Block> pBlockGetter, HolderGetter<Biome> pBiomeGetter, HolderGetter<StructureSet> pStructureSetGetter, HolderGetter<PlacedFeature> pPlacedFeatureGetter, String pSettings, FlatLevelGeneratorSettings pLayerGenerationSettings) {
      Iterator<String> iterator = Splitter.on(';').split(pSettings).iterator();
      if (!iterator.hasNext()) {
         return FlatLevelGeneratorSettings.getDefault(pBiomeGetter, pStructureSetGetter, pPlacedFeatureGetter);
      } else {
         List<FlatLayerInfo> list = getLayersInfoFromString(pBlockGetter, iterator.next());
         if (list.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault(pBiomeGetter, pStructureSetGetter, pPlacedFeatureGetter);
         } else {
            Holder.Reference<Biome> reference = pBiomeGetter.getOrThrow(DEFAULT_BIOME);
            Holder<Biome> holder = reference;
            if (iterator.hasNext()) {
               String s = iterator.next();
               holder = Optional.ofNullable(ResourceLocation.tryParse(s)).map((p_258126_) -> {
                  return ResourceKey.create(Registries.BIOME, p_258126_);
               }).flatMap(pBiomeGetter::get).orElseGet(() -> {
                  LOGGER.warn("Invalid biome: {}", (Object)s);
                  return reference;
               });
            }

            return pLayerGenerationSettings.withBiomeAndLayers(list, pLayerGenerationSettings.structureOverrides(), holder);
         }
      }
   }

   static String save(FlatLevelGeneratorSettings pLevelGeneratorSettings) {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = 0; i < pLevelGeneratorSettings.getLayersInfo().size(); ++i) {
         if (i > 0) {
            stringbuilder.append(",");
         }

         stringbuilder.append(pLevelGeneratorSettings.getLayersInfo().get(i));
      }

      stringbuilder.append(";");
      stringbuilder.append(pLevelGeneratorSettings.getBiome().unwrapKey().map(ResourceKey::location).orElseThrow(() -> {
         return new IllegalStateException("Biome not registered");
      }));
      return stringbuilder.toString();
   }

   protected void init() {
      this.shareText = Component.translatable("createWorld.customize.presets.share");
      this.listText = Component.translatable("createWorld.customize.presets.list");
      this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
      this.export.setMaxLength(1230);
      WorldCreationContext worldcreationcontext = this.parent.parent.getUiState().getSettings();
      RegistryAccess registryaccess = worldcreationcontext.worldgenLoadContext();
      FeatureFlagSet featureflagset = worldcreationcontext.dataConfiguration().enabledFeatures();
      HolderGetter<Biome> holdergetter = registryaccess.lookupOrThrow(Registries.BIOME);
      HolderGetter<StructureSet> holdergetter1 = registryaccess.lookupOrThrow(Registries.STRUCTURE_SET);
      HolderGetter<PlacedFeature> holdergetter2 = registryaccess.lookupOrThrow(Registries.PLACED_FEATURE);
      HolderGetter<Block> holdergetter3 = registryaccess.lookupOrThrow(Registries.BLOCK).filterFeatures(featureflagset);
      this.export.setValue(save(this.parent.settings()));
      this.settings = this.parent.settings();
      this.addWidget(this.export);
      this.list = new PresetFlatWorldScreen.PresetsList(registryaccess, featureflagset);
      this.addWidget(this.list);
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets.select"), (p_280822_) -> {
         FlatLevelGeneratorSettings flatlevelgeneratorsettings = fromString(holdergetter3, holdergetter, holdergetter1, holdergetter2, this.export.getValue(), this.settings);
         this.parent.setConfig(flatlevelgeneratorsettings);
         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280823_) -> {
         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.updateButtonValidity(this.list.getSelected() != null);
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
      return this.list.mouseScrolled(pMouseX, pMouseY, pDelta);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.export.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.export.setValue(s);
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
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      pGuiGraphics.drawString(this.font, this.shareText, 50, 30, 10526880);
      pGuiGraphics.drawString(this.font, this.listText, 50, 70, 10526880);
      pGuiGraphics.pose().popPose();
      this.export.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void tick() {
      this.export.tick();
      super.tick();
   }

   public void updateButtonValidity(boolean pValid) {
      this.selectButton.active = pValid || this.export.getValue().length() > 1;
   }

   @OnlyIn(Dist.CLIENT)
   class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
      public PresetsList(RegistryAccess pRegistryAccess, FeatureFlagSet pFlags) {
         super(PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24);

         for(Holder<FlatLevelGeneratorPreset> holder : pRegistryAccess.registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
            Set<Block> set = holder.value().settings().getLayersInfo().stream().map((p_259579_) -> {
               return p_259579_.getBlockState().getBlock();
            }).filter((p_259421_) -> {
               return !p_259421_.isEnabled(pFlags);
            }).collect(Collectors.toSet());
            if (!set.isEmpty()) {
               PresetFlatWorldScreen.LOGGER.info("Discarding flat world preset {} since it contains experimental blocks {}", holder.unwrapKey().map((p_259357_) -> {
                  return p_259357_.location().toString();
               }).orElse("<unknown>"), set);
            } else {
               this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry(holder));
            }
         }

      }

      public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry pEntry) {
         super.setSelected(pEntry);
         PresetFlatWorldScreen.this.updateButtonValidity(pEntry != null);
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
         if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
         } else {
            if (CommonInputs.selected(pKeyCode) && this.getSelected() != null) {
               this.getSelected().select();
            }

            return false;
         }
      }

      @OnlyIn(Dist.CLIENT)
      public class Entry extends ObjectSelectionList.Entry<PresetFlatWorldScreen.PresetsList.Entry> {
         private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
         private final FlatLevelGeneratorPreset preset;
         private final Component name;

         public Entry(Holder<FlatLevelGeneratorPreset> pPresetHolder) {
            this.preset = pPresetHolder.value();
            this.name = pPresetHolder.unwrapKey().<Component>map((p_232760_) -> {
               return Component.translatable(p_232760_.location().toLanguageKey("flat_world_preset"));
            }).orElse(PresetFlatWorldScreen.UNKNOWN_PRESET);
         }

         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            this.blitSlot(pGuiGraphics, pLeft, pTop, this.preset.displayItem().value());
            pGuiGraphics.drawString(PresetFlatWorldScreen.this.font, this.name, pLeft + 18 + 5, pTop + 6, 16777215, false);
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
               this.select();
            }

            return false;
         }

         void select() {
            PresetsList.this.setSelected(this);
            PresetFlatWorldScreen.this.settings = this.preset.settings();
            PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(PresetFlatWorldScreen.this.settings));
            PresetFlatWorldScreen.this.export.moveCursorToStart();
         }

         private void blitSlot(GuiGraphics pGuiGraphics, int pX, int pY, Item pItem) {
            this.blitSlotBg(pGuiGraphics, pX + 1, pY + 1);
            pGuiGraphics.renderFakeItem(new ItemStack(pItem), pX + 2, pY + 2);
         }

         private void blitSlotBg(GuiGraphics pGuiGraphics, int pX, int pY) {
            pGuiGraphics.blit(STATS_ICON_LOCATION, pX, pY, 0, 0.0F, 0.0F, 18, 18, 128, 128);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.name);
         }
      }
   }
}