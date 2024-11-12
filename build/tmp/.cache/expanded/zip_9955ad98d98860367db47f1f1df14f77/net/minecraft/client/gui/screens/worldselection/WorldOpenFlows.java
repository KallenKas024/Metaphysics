package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SymlinkWarningScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldOpenFlows {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final LevelStorageSource levelSource;

   public WorldOpenFlows(Minecraft pMinecraft, LevelStorageSource pLevelSource) {
      this.minecraft = pMinecraft;
      this.levelSource = pLevelSource;
   }

   public void loadLevel(Screen pLastScreen, String pLevelName) {
      this.doLoadLevel(pLastScreen, pLevelName, false, true);
   }

   public void createFreshLevel(String pLevelName, LevelSettings pLevelSettings, WorldOptions pWorldOptions, Function<RegistryAccess, WorldDimensions> pDimensionsGetter) {
      LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.createWorldAccess(pLevelName);
      if (levelstoragesource$levelstorageaccess != null) {
         PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource$levelstorageaccess);
         WorldDataConfiguration worlddataconfiguration = pLevelSettings.getDataConfiguration();

         try {
            WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(packrepository, worlddataconfiguration, false, false);
            WorldStem worldstem = this.loadWorldDataBlocking(worldloader$packconfig, (p_258145_) -> {
               WorldDimensions.Complete worlddimensions$complete = pDimensionsGetter.apply(p_258145_.datapackWorldgen()).bake(p_258145_.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
               return new WorldLoader.DataLoadOutput<>(new PrimaryLevelData(pLevelSettings, pWorldOptions, worlddimensions$complete.specialWorldProperty(), worlddimensions$complete.lifecycle()), worlddimensions$complete.dimensionsRegistryAccess());
            }, WorldStem::new);
            this.minecraft.doWorldLoad(pLevelName, levelstoragesource$levelstorageaccess, packrepository, worldstem, true);
         } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)exception);
            safeCloseAccess(levelstoragesource$levelstorageaccess, pLevelName);
         }

      }
   }

   @Nullable
   private LevelStorageSource.LevelStorageAccess createWorldAccess(String pLevelName) {
      try {
         return this.levelSource.validateAndCreateAccess(pLevelName);
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to read level {} data", pLevelName, ioexception);
         SystemToast.onWorldAccessFailure(this.minecraft, pLevelName);
         this.minecraft.setScreen((Screen)null);
         return null;
      } catch (ContentValidationException contentvalidationexception) {
         LOGGER.warn("{}", (Object)contentvalidationexception.getMessage());
         this.minecraft.setScreen(new SymlinkWarningScreen((Screen)null));
         return null;
      }
   }

   public void createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess pLevelStorage, ReloadableServerResources pResources, LayeredRegistryAccess<RegistryLayer> pRegistries, WorldData pWorldData) {
      PackRepository packrepository = ServerPacksSource.createPackRepository(pLevelStorage);
      CloseableResourceManager closeableresourcemanager = (new WorldLoader.PackConfig(packrepository, pWorldData.getDataConfiguration(), false, false)).createResourceManager().getSecond();
      this.minecraft.doWorldLoad(pLevelStorage.getLevelId(), pLevelStorage, packrepository, new WorldStem(closeableresourcemanager, pResources, pRegistries, pWorldData), true);
   }

   private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess pLevelStorage, boolean pSafeMode, PackRepository pPackRepository) throws Exception {
      WorldLoader.PackConfig worldloader$packconfig = this.getPackConfigFromLevelData(pLevelStorage, pSafeMode, pPackRepository);
      return this.loadWorldDataBlocking(worldloader$packconfig, (p_247851_) -> {
         DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, p_247851_.datapackWorldgen());
         Registry<LevelStem> registry = p_247851_.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
         Pair<WorldData, WorldDimensions.Complete> pair = pLevelStorage.getDataTag(dynamicops, p_247851_.dataConfiguration(), registry, p_247851_.datapackWorldgen().allRegistriesLifecycle());
         if (pair == null) {
            throw new IllegalStateException("Failed to load world");
         } else {
            return new WorldLoader.DataLoadOutput<>(pair.getFirst(), pair.getSecond().dimensionsRegistryAccess());
         }
      }, WorldStem::new);
   }

   public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess pLevelStorage) throws Exception {
      PackRepository packrepository = ServerPacksSource.createPackRepository(pLevelStorage);
      WorldLoader.PackConfig worldloader$packconfig = this.getPackConfigFromLevelData(pLevelStorage, false, packrepository);
      @OnlyIn(Dist.CLIENT)
      record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
      }
      return this.<Data, Pair<LevelSettings, WorldCreationContext>>loadWorldDataBlocking(worldloader$packconfig, (p_247857_) -> {
         DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, p_247857_.datapackWorldgen());
         Registry<LevelStem> registry = (new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable())).freeze();
         Pair<WorldData, WorldDimensions.Complete> pair = pLevelStorage.getDataTag(dynamicops, p_247857_.dataConfiguration(), registry, p_247857_.datapackWorldgen().allRegistriesLifecycle());
         if (pair == null) {
            throw new IllegalStateException("Failed to load world");
         } else {
            return new WorldLoader.DataLoadOutput<>(new Data(pair.getFirst().getLevelSettings(), pair.getFirst().worldGenOptions(), pair.getSecond().dimensions()), p_247857_.datapackDimensions());
         }
      }, (p_247840_, p_247841_, p_247842_, p_247843_) -> {
         p_247840_.close();
         return Pair.of(p_247843_.levelSettings, new WorldCreationContext(p_247843_.options, new WorldDimensions(p_247843_.existingDimensions), p_247842_, p_247841_, p_247843_.levelSettings.getDataConfiguration()));
      });
   }

   private WorldLoader.PackConfig getPackConfigFromLevelData(LevelStorageSource.LevelStorageAccess pLevelStorage, boolean pFunctionCompilationLevel, PackRepository pPackConfig) {
      WorldDataConfiguration worlddataconfiguration = pLevelStorage.getDataConfiguration();
      if (worlddataconfiguration == null) {
         throw new IllegalStateException("Failed to load data pack config");
      } else {
         return new WorldLoader.PackConfig(pPackConfig, worlddataconfiguration, pFunctionCompilationLevel, false);
      }
   }

   public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess pLevelStorage, boolean pSafeMode) throws Exception {
      PackRepository packrepository = ServerPacksSource.createPackRepository(pLevelStorage);
      return this.loadWorldStem(pLevelStorage, pSafeMode, packrepository);
   }

   private <D, R> R loadWorldDataBlocking(WorldLoader.PackConfig pPackConfig, WorldLoader.WorldDataSupplier<D> pWorldDataSupplier, WorldLoader.ResultFactory<D, R> pResultFactory) throws Exception {
      WorldLoader.InitConfig worldloader$initconfig = new WorldLoader.InitConfig(pPackConfig, Commands.CommandSelection.INTEGRATED, 2);
      CompletableFuture<R> completablefuture = WorldLoader.load(worldloader$initconfig, pWorldDataSupplier, pResultFactory, Util.backgroundExecutor(), this.minecraft);
      this.minecraft.managedBlock(completablefuture::isDone);
      return completablefuture.get();
   }

   private void doLoadLevel(Screen pLastScreen, String pLevelName, boolean pSafeMode, boolean pCheckAskForBackup) {
      // FORGE: Patch in overload to reduce further patching
      this.doLoadLevel(pLastScreen, pLevelName, pSafeMode, pCheckAskForBackup, false);
   }

   // FORGE: Patch in confirmExperimentalWarning which confirms the experimental warning when true
   private void doLoadLevel(Screen pLastScreen, String pLevelName, boolean pSafeMode, boolean pCheckAskForBackup, boolean confirmExperimentalWarning) {
      LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.createWorldAccess(pLevelName);
      if (levelstoragesource$levelstorageaccess != null) {
         PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource$levelstorageaccess);

         WorldStem worldstem;
         try {
            levelstoragesource$levelstorageaccess.readAdditionalLevelSaveData(); // Read extra (e.g. modded) data from the world before creating it
            worldstem = this.loadWorldStem(levelstoragesource$levelstorageaccess, pSafeMode, packrepository);
            if (confirmExperimentalWarning && worldstem.worldData() instanceof PrimaryLevelData pld) {
               pld.withConfirmedWarning(true);
            }
         } catch (Exception exception) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", (Throwable)exception);
            if (!pSafeMode) {
               this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> {
                  this.doLoadLevel(pLastScreen, pLevelName, true, pCheckAskForBackup);
               }));
            } else {
               this.minecraft.setScreen(new AlertScreen(() -> {
                  this.minecraft.setScreen((Screen)null);
               }, Component.translatable("datapackFailure.safeMode.failed.title"), Component.translatable("datapackFailure.safeMode.failed.description"), CommonComponents.GUI_TO_TITLE, true));
            }

            safeCloseAccess(levelstoragesource$levelstorageaccess, pLevelName);
            return;
         }

         WorldData worlddata = worldstem.worldData();
         boolean flag = worlddata.worldGenOptions().isOldCustomizedWorld();
         boolean flag1 = worlddata.worldGenSettingsLifecycle() != Lifecycle.stable();
         // Forge: Skip confirmation if it has been done already for this world
         boolean skipConfirmation = worlddata instanceof PrimaryLevelData pld && pld.hasConfirmedExperimentalWarning();
         if (skipConfirmation || !pCheckAskForBackup || !flag && !flag1) {
            this.minecraft.getDownloadedPackSource().loadBundledResourcePack(levelstoragesource$levelstorageaccess).thenApply((p_233177_) -> {
               return true;
            }).exceptionallyComposeAsync((p_233183_) -> {
               LOGGER.warn("Failed to load pack: ", p_233183_);
               return this.promptBundledPackLoadFailure();
            }, this.minecraft).thenAcceptAsync((p_233168_) -> {
               if (p_233168_) {
                  this.minecraft.doWorldLoad(pLevelName, levelstoragesource$levelstorageaccess, packrepository, worldstem, false);
               } else {
                  worldstem.close();
                  safeCloseAccess(levelstoragesource$levelstorageaccess, pLevelName);
                  this.minecraft.getDownloadedPackSource().clearServerPack().thenRunAsync(() -> {
                     this.minecraft.setScreen(pLastScreen);
                  }, this.minecraft);
               }

            }, this.minecraft).exceptionally((p_233175_) -> {
               this.minecraft.delayCrash(CrashReport.forThrowable(p_233175_, "Load world"));
               return null;
            });
         } else {
            if (flag) // Forge: For legacy world options, let vanilla handle it.
            this.askForBackup(pLastScreen, pLevelName, flag, () -> {
               this.doLoadLevel(pLastScreen, pLevelName, pSafeMode, false);
            });
            else net.minecraftforge.client.ForgeHooksClient.createWorldConfirmationScreen(() -> this.doLoadLevel(pLastScreen, pLevelName, pSafeMode, false, true));
            worldstem.close();
            safeCloseAccess(levelstoragesource$levelstorageaccess, pLevelName);
         }
      }
   }

   private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
      CompletableFuture<Boolean> completablefuture = new CompletableFuture<>();
      this.minecraft.setScreen(new ConfirmScreen(completablefuture::complete, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
      return completablefuture;
   }

   private static void safeCloseAccess(LevelStorageSource.LevelStorageAccess pLevelStorage, String pLevelName) {
      try {
         pLevelStorage.close();
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to unlock access to level {}", pLevelName, ioexception);
      }

   }

   private void askForBackup(Screen pLastScreen, String pLevelName, boolean pCustomized, Runnable pLoadLevel) {
      Component component;
      Component component1;
      if (pCustomized) {
         component = Component.translatable("selectWorld.backupQuestion.customized");
         component1 = Component.translatable("selectWorld.backupWarning.customized");
      } else {
         component = Component.translatable("selectWorld.backupQuestion.experimental");
         component1 = Component.translatable("selectWorld.backupWarning.experimental");
      }

      this.minecraft.setScreen(new BackupConfirmScreen(pLastScreen, (p_233172_, p_233173_) -> {
         if (p_233172_) {
            EditWorldScreen.makeBackupAndShowToast(this.levelSource, pLevelName);
         }

         pLoadLevel.run();
      }, component, component1, false));
   }

   public static void confirmWorldCreation(Minecraft pMinecraft, CreateWorldScreen pScreen, Lifecycle pLifecycle, Runnable pLoadWorld, boolean pSkipWarnings) {
      BooleanConsumer booleanconsumer = (p_233154_) -> {
         if (p_233154_) {
            pLoadWorld.run();
         } else {
            pMinecraft.setScreen(pScreen);
         }

      };
      if (!pSkipWarnings && pLifecycle != Lifecycle.stable()) {
         if (pLifecycle == Lifecycle.experimental()) {
            pMinecraft.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.warning.experimental.title"), Component.translatable("selectWorld.warning.experimental.question")));
         } else {
            pMinecraft.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.warning.deprecated.title"), Component.translatable("selectWorld.warning.deprecated.question")));
         }
      } else {
         pLoadWorld.run();
      }

   }
}
