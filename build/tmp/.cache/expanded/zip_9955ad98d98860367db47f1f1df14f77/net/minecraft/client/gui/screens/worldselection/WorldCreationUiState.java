package net.minecraft.client.gui.screens.worldselection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldCreationUiState {
   private static final Component DEFAULT_WORLD_NAME = Component.translatable("selectWorld.newWorld");
   private final List<Consumer<WorldCreationUiState>> listeners = new ArrayList<>();
   private String name = DEFAULT_WORLD_NAME.getString();
   private WorldCreationUiState.SelectedGameMode gameMode = WorldCreationUiState.SelectedGameMode.SURVIVAL;
   private Difficulty difficulty = Difficulty.NORMAL;
   @Nullable
   private Boolean allowCheats;
   private String seed;
   private boolean generateStructures;
   private boolean bonusChest;
   private final Path savesFolder;
   private String targetFolder;
   private WorldCreationContext settings;
   private WorldCreationUiState.WorldTypeEntry worldType;
   private final List<WorldCreationUiState.WorldTypeEntry> normalPresetList = new ArrayList<>();
   private final List<WorldCreationUiState.WorldTypeEntry> altPresetList = new ArrayList<>();
   private GameRules gameRules = new GameRules();

   public WorldCreationUiState(Path pSavesFolder, WorldCreationContext pSettings, Optional<ResourceKey<WorldPreset>> pPreset, OptionalLong pSeed) {
      this.savesFolder = pSavesFolder;
      this.settings = pSettings;
      this.worldType = new WorldCreationUiState.WorldTypeEntry(findPreset(pSettings, pPreset).orElse((Holder<WorldPreset>)null));
      this.updatePresetLists();
      this.seed = pSeed.isPresent() ? Long.toString(pSeed.getAsLong()) : "";
      this.generateStructures = pSettings.options().generateStructures();
      this.bonusChest = pSettings.options().generateBonusChest();
      this.targetFolder = this.findResultFolder(this.name);
   }

   public void addListener(Consumer<WorldCreationUiState> pListener) {
      this.listeners.add(pListener);
   }

   public void onChanged() {
      boolean flag = this.isBonusChest();
      if (flag != this.settings.options().generateBonusChest()) {
         this.settings = this.settings.withOptions((p_268360_) -> {
            return p_268360_.withBonusChest(flag);
         });
      }

      boolean flag1 = this.isGenerateStructures();
      if (flag1 != this.settings.options().generateStructures()) {
         this.settings = this.settings.withOptions((p_267945_) -> {
            return p_267945_.withStructures(flag1);
         });
      }

      for(Consumer<WorldCreationUiState> consumer : this.listeners) {
         consumer.accept(this);
      }

   }

   public void setName(String pName) {
      this.name = pName;
      this.targetFolder = this.findResultFolder(pName);
      this.onChanged();
   }

   private String findResultFolder(String pName) {
      String s = pName.trim();

      try {
         return FileUtil.findAvailableName(this.savesFolder, !s.isEmpty() ? s : DEFAULT_WORLD_NAME.getString(), "");
      } catch (Exception exception) {
         try {
            return FileUtil.findAvailableName(this.savesFolder, "World", "");
         } catch (IOException ioexception) {
            throw new RuntimeException("Could not create save folder", ioexception);
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public String getTargetFolder() {
      return this.targetFolder;
   }

   public void setGameMode(WorldCreationUiState.SelectedGameMode pGameMode) {
      this.gameMode = pGameMode;
      this.onChanged();
   }

   public WorldCreationUiState.SelectedGameMode getGameMode() {
      return this.isDebug() ? WorldCreationUiState.SelectedGameMode.DEBUG : this.gameMode;
   }

   public void setDifficulty(Difficulty pDifficulty) {
      this.difficulty = pDifficulty;
      this.onChanged();
   }

   public Difficulty getDifficulty() {
      return this.isHardcore() ? Difficulty.HARD : this.difficulty;
   }

   public boolean isHardcore() {
      return this.getGameMode() == WorldCreationUiState.SelectedGameMode.HARDCORE;
   }

   public void setAllowCheats(boolean pAllowCheats) {
      this.allowCheats = pAllowCheats;
      this.onChanged();
   }

   public boolean isAllowCheats() {
      if (this.isDebug()) {
         return true;
      } else if (this.isHardcore()) {
         return false;
      } else if (this.allowCheats == null) {
         return this.getGameMode() == WorldCreationUiState.SelectedGameMode.CREATIVE;
      } else {
         return this.allowCheats;
      }
   }

   public void setSeed(String pSeed) {
      this.seed = pSeed;
      this.settings = this.settings.withOptions((p_267957_) -> {
         return p_267957_.withSeed(WorldOptions.parseSeed(this.getSeed()));
      });
      this.onChanged();
   }

   public String getSeed() {
      return this.seed;
   }

   public void setGenerateStructures(boolean pGenerateStructures) {
      this.generateStructures = pGenerateStructures;
      this.onChanged();
   }

   public boolean isGenerateStructures() {
      return this.isDebug() ? false : this.generateStructures;
   }

   public void setBonusChest(boolean pBonusChest) {
      this.bonusChest = pBonusChest;
      this.onChanged();
   }

   public boolean isBonusChest() {
      return !this.isDebug() && !this.isHardcore() ? this.bonusChest : false;
   }

   public void setSettings(WorldCreationContext pSettings) {
      this.settings = pSettings;
      this.updatePresetLists();
      this.onChanged();
   }

   public WorldCreationContext getSettings() {
      return this.settings;
   }

   public void updateDimensions(WorldCreationContext.DimensionsUpdater pDimensionsUpdater) {
      this.settings = this.settings.withDimensions(pDimensionsUpdater);
      this.onChanged();
   }

   protected boolean tryUpdateDataConfiguration(WorldDataConfiguration pWorldDataConfiguration) {
      WorldDataConfiguration worlddataconfiguration = this.settings.dataConfiguration();
      if (worlddataconfiguration.dataPacks().getEnabled().equals(pWorldDataConfiguration.dataPacks().getEnabled()) && worlddataconfiguration.enabledFeatures().equals(pWorldDataConfiguration.enabledFeatures())) {
         this.settings = new WorldCreationContext(this.settings.options(), this.settings.datapackDimensions(), this.settings.selectedDimensions(), this.settings.worldgenRegistries(), this.settings.dataPackResources(), pWorldDataConfiguration);
         return true;
      } else {
         return false;
      }
   }

   public boolean isDebug() {
      return this.settings.selectedDimensions().isDebug();
   }

   public void setWorldType(WorldCreationUiState.WorldTypeEntry pWorldType) {
      this.worldType = pWorldType;
      Holder<WorldPreset> holder = pWorldType.preset();
      if (holder != null) {
         this.updateDimensions((p_268134_, p_268035_) -> {
            return holder.value().createWorldDimensions();
         });
      }

   }

   public WorldCreationUiState.WorldTypeEntry getWorldType() {
      return this.worldType;
   }

   @Nullable
   public PresetEditor getPresetEditor() {
      Holder<WorldPreset> holder = this.getWorldType().preset();
      return holder != null ? holder.unwrapKey().map(net.minecraftforge.client.PresetEditorManager::get).orElse(null) : null; // FORGE: redirect lookup to expanded map
   }

   public List<WorldCreationUiState.WorldTypeEntry> getNormalPresetList() {
      return this.normalPresetList;
   }

   public List<WorldCreationUiState.WorldTypeEntry> getAltPresetList() {
      return this.altPresetList;
   }

   private void updatePresetLists() {
      Registry<WorldPreset> registry = this.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET);
      this.normalPresetList.clear();
      this.normalPresetList.addAll(getNonEmptyList(registry, WorldPresetTags.NORMAL).orElseGet(() -> {
         return registry.holders().map(WorldCreationUiState.WorldTypeEntry::new).toList();
      }));
      this.altPresetList.clear();
      this.altPresetList.addAll(getNonEmptyList(registry, WorldPresetTags.EXTENDED).orElse(this.normalPresetList));
      Holder<WorldPreset> holder = this.worldType.preset();
      if (holder != null) {
         this.worldType = findPreset(this.getSettings(), holder.unwrapKey()).map(WorldCreationUiState.WorldTypeEntry::new).orElse(this.normalPresetList.get(0));
      }

   }

   private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext pContext, Optional<ResourceKey<WorldPreset>> pPreset) {
      return pPreset.flatMap((p_267974_) -> {
         return pContext.worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET).getHolder(p_267974_);
      });
   }

   private static Optional<List<WorldCreationUiState.WorldTypeEntry>> getNonEmptyList(Registry<WorldPreset> pRegistry, TagKey<WorldPreset> pKey) {
      return pRegistry.getTag(pKey).map((p_268149_) -> {
         return p_268149_.stream().map(WorldCreationUiState.WorldTypeEntry::new).toList();
      }).filter((p_268066_) -> {
         return !p_268066_.isEmpty();
      });
   }

   public void setGameRules(GameRules pGameRules) {
      this.gameRules = pGameRules;
      this.onChanged();
   }

   public GameRules getGameRules() {
      return this.gameRules;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum SelectedGameMode {
      SURVIVAL("survival", GameType.SURVIVAL),
      HARDCORE("hardcore", GameType.SURVIVAL),
      CREATIVE("creative", GameType.CREATIVE),
      DEBUG("spectator", GameType.SPECTATOR);

      public final GameType gameType;
      public final Component displayName;
      private final Component info;

      private SelectedGameMode(String pId, GameType pGameType) {
         this.gameType = pGameType;
         this.displayName = Component.translatable("selectWorld.gameMode." + pId);
         this.info = Component.translatable("selectWorld.gameMode." + pId + ".info");
      }

      public Component getInfo() {
         return this.info;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static record WorldTypeEntry(@Nullable Holder<WorldPreset> preset) {
      private static final Component CUSTOM_WORLD_DESCRIPTION = Component.translatable("generator.custom");

      public Component describePreset() {
         return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).map((p_268048_) -> {
            return (Component)Component.translatable(p_268048_.location().toLanguageKey("generator"));
         }).orElse(CUSTOM_WORLD_DESCRIPTION);
      }

      public boolean isAmplified() {
         return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).filter((p_268224_) -> {
            return p_268224_.equals(WorldPresets.AMPLIFIED);
         }).isPresent();
      }
   }
}
