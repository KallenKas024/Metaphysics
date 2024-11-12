package net.minecraft.client.resources;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPackSource extends BuiltInPackSource {
   private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(Component.translatable("resourcePack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
   private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(PackMetadataSection.TYPE, VERSION_METADATA_SECTION);
   private static final Component VANILLA_NAME = Component.translatable("resourcePack.vanilla.name");
   public static final String HIGH_CONTRAST_PACK = "high_contrast";
   private static final Map<String, Component> SPECIAL_PACK_NAMES = Map.of("programmer_art", Component.translatable("resourcePack.programmer_art.name"), "high_contrast", Component.translatable("resourcePack.high_contrast.name"));
   private static final ResourceLocation PACKS_DIR = new ResourceLocation("minecraft", "resourcepacks");
   @Nullable
   private final Path externalAssetDir;

   public ClientPackSource(Path pAssetIndex) {
      super(PackType.CLIENT_RESOURCES, createVanillaPackSource(pAssetIndex), PACKS_DIR);
      this.externalAssetDir = this.findExplodedAssetPacks(pAssetIndex);
   }

   @Nullable
   private Path findExplodedAssetPacks(Path pAssetIndex) {
      if (SharedConstants.IS_RUNNING_IN_IDE && pAssetIndex.getFileSystem() == FileSystems.getDefault()) {
         Path path = pAssetIndex.getParent().resolve("resourcepacks");
         if (Files.isDirectory(path)) {
            return path;
         }
      }

      return null;
   }

   public static VanillaPackResources createVanillaPackSource(Path pAssetIndex) {
      VanillaPackResourcesBuilder vanillapackresourcesbuilder = (new VanillaPackResourcesBuilder()).setMetadata(BUILT_IN_METADATA).exposeNamespace("minecraft", "realms");
      return vanillapackresourcesbuilder.applyDevelopmentConfig().pushJarResources().pushAssetPath(PackType.CLIENT_RESOURCES, pAssetIndex).build();
   }

   protected Component getPackTitle(String pId) {
      Component component = SPECIAL_PACK_NAMES.get(pId);
      return (Component)(component != null ? component : Component.literal(pId));
   }

   @Nullable
   protected Pack createVanillaPack(PackResources pResources) {
      return Pack.readMetaAndCreate("vanilla", VANILLA_NAME, true, (p_247953_) -> {
         return pResources;
      }, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
   }

   @Nullable
   protected Pack createBuiltinPack(String pId, Pack.ResourcesSupplier pResources, Component pTitle) {
      return Pack.readMetaAndCreate(pId, pTitle, false, pResources, PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
   }

   protected void populatePackList(BiConsumer<String, Function<String, Pack>> p_249851_) {
      super.populatePackList(p_249851_);
      if (this.externalAssetDir != null) {
         this.discoverPacksInPath(this.externalAssetDir, p_249851_);
      }

   }
}