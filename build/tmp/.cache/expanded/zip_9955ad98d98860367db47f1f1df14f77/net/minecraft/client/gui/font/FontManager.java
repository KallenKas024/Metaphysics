package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontManager implements PreparableReloadListener, AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String FONTS_PATH = "fonts.json";
   public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
   private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private final FontSet missingFontSet;
   private final List<GlyphProvider> providersToClose = new ArrayList<>();
   private final Map<ResourceLocation, FontSet> fontSets = new HashMap<>();
   private final TextureManager textureManager;
   private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();

   public FontManager(TextureManager pTextureManager) {
      this.textureManager = pTextureManager;
      this.missingFontSet = Util.make(new FontSet(pTextureManager, MISSING_FONT), (p_95010_) -> {
         p_95010_.reload(Lists.newArrayList(new AllMissingGlyphProvider()));
      });
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      pPreparationsProfiler.startTick();
      pPreparationsProfiler.endTick();
      return this.prepare(pResourceManager, pBackgroundExecutor).thenCompose(pPreparationBarrier::wait).thenAcceptAsync((p_284609_) -> {
         this.apply(p_284609_, pReloadProfiler);
      }, pGameExecutor);
   }

   private CompletableFuture<FontManager.Preparation> prepare(ResourceManager pResourceManager, Executor pExecutor) {
      List<CompletableFuture<FontManager.UnresolvedBuilderBundle>> list = new ArrayList<>();

      for(Map.Entry<ResourceLocation, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(pResourceManager).entrySet()) {
         ResourceLocation resourcelocation = FONT_DEFINITIONS.fileToId(entry.getKey());
         list.add(CompletableFuture.supplyAsync(() -> {
            List<Pair<FontManager.BuilderId, GlyphProviderDefinition>> list1 = loadResourceStack(entry.getValue(), resourcelocation);
            FontManager.UnresolvedBuilderBundle fontmanager$unresolvedbuilderbundle = new FontManager.UnresolvedBuilderBundle(resourcelocation);

            for(Pair<FontManager.BuilderId, GlyphProviderDefinition> pair : list1) {
               FontManager.BuilderId fontmanager$builderid = pair.getFirst();
               pair.getSecond().unpack().ifLeft((p_286126_) -> {
                  CompletableFuture<Optional<GlyphProvider>> completablefuture = this.safeLoad(fontmanager$builderid, p_286126_, pResourceManager, pExecutor);
                  fontmanager$unresolvedbuilderbundle.add(fontmanager$builderid, completablefuture);
               }).ifRight((p_286129_) -> {
                  fontmanager$unresolvedbuilderbundle.add(fontmanager$builderid, p_286129_);
               });
            }

            return fontmanager$unresolvedbuilderbundle;
         }, pExecutor));
      }

      return Util.sequence(list).thenCompose((p_284592_) -> {
         List<CompletableFuture<Optional<GlyphProvider>>> list1 = p_284592_.stream().flatMap(FontManager.UnresolvedBuilderBundle::listBuilders).collect(Collectors.toCollection(ArrayList::new));
         GlyphProvider glyphprovider = new AllMissingGlyphProvider();
         list1.add(CompletableFuture.completedFuture(Optional.of(glyphprovider)));
         return Util.sequence(list1).thenCompose((p_284618_) -> {
            Map<ResourceLocation, List<GlyphProvider>> map = this.resolveProviders(p_284592_);
            CompletableFuture<?>[] completablefuture = map.values().stream().map((p_284585_) -> {
               return CompletableFuture.runAsync(() -> {
                  this.finalizeProviderLoading(p_284585_, glyphprovider);
               }, pExecutor);
            }).toArray((p_284587_) -> {
               return new CompletableFuture[p_284587_];
            });
            return CompletableFuture.allOf(completablefuture).thenApply((p_284595_) -> {
               List<GlyphProvider> list2 = p_284618_.stream().flatMap(Optional::stream).toList();
               return new FontManager.Preparation(map, list2);
            });
         });
      });
   }

   private CompletableFuture<Optional<GlyphProvider>> safeLoad(FontManager.BuilderId pBuilderId, GlyphProviderDefinition.Loader pLoader, ResourceManager pResourceManager, Executor pExecutor) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            return Optional.of(pLoader.load(pResourceManager));
         } catch (Exception exception) {
            LOGGER.warn("Failed to load builder {}, rejecting", pBuilderId, exception);
            return Optional.empty();
         }
      }, pExecutor);
   }

   private Map<ResourceLocation, List<GlyphProvider>> resolveProviders(List<FontManager.UnresolvedBuilderBundle> pUnresolvedBuilderBundles) {
      Map<ResourceLocation, List<GlyphProvider>> map = new HashMap<>();
      DependencySorter<ResourceLocation, FontManager.UnresolvedBuilderBundle> dependencysorter = new DependencySorter<>();
      pUnresolvedBuilderBundles.forEach((p_284626_) -> {
         dependencysorter.addEntry(p_284626_.fontId, p_284626_);
      });
      dependencysorter.orderByDependencies((p_284620_, p_284621_) -> {
         p_284621_.resolve(map::get).ifPresent((p_284590_) -> {
            map.put(p_284620_, p_284590_);
         });
      });
      return map;
   }

   private void finalizeProviderLoading(List<GlyphProvider> pProviders, GlyphProvider pMissingProvider) {
      pProviders.add(0, pMissingProvider);
      IntSet intset = new IntOpenHashSet();

      for(GlyphProvider glyphprovider : pProviders) {
         intset.addAll(glyphprovider.getSupportedGlyphs());
      }

      intset.forEach((p_284614_) -> {
         if (p_284614_ != 32) {
            for(GlyphProvider glyphprovider1 : Lists.reverse(pProviders)) {
               if (glyphprovider1.getGlyph(p_284614_) != null) {
                  break;
               }
            }

         }
      });
   }

   private void apply(FontManager.Preparation pPreperation, ProfilerFiller pProfiler) {
      pProfiler.startTick();
      pProfiler.push("closing");
      this.fontSets.values().forEach(FontSet::close);
      this.fontSets.clear();
      this.providersToClose.forEach(GlyphProvider::close);
      this.providersToClose.clear();
      pProfiler.popPush("reloading");
      pPreperation.providers().forEach((p_284627_, p_284628_) -> {
         FontSet fontset = new FontSet(this.textureManager, p_284627_);
         fontset.reload(Lists.reverse(p_284628_));
         this.fontSets.put(p_284627_, fontset);
      });
      this.providersToClose.addAll(pPreperation.allProviders);
      pProfiler.pop();
      pProfiler.endTick();
      if (!this.fontSets.containsKey(this.getActualId(Minecraft.DEFAULT_FONT))) {
         throw new IllegalStateException("Default font failed to load");
      }
   }

   private static List<Pair<FontManager.BuilderId, GlyphProviderDefinition>> loadResourceStack(List<Resource> pResources, ResourceLocation pFontId) {
      List<Pair<FontManager.BuilderId, GlyphProviderDefinition>> list = new ArrayList<>();

      for(Resource resource : pResources) {
         try (Reader reader = resource.openAsReader()) {
            JsonElement jsonelement = GSON.fromJson(reader, JsonElement.class);
            FontManager.FontDefinitionFile fontmanager$fontdefinitionfile = Util.getOrThrow(FontManager.FontDefinitionFile.CODEC.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new);
            List<GlyphProviderDefinition> list1 = fontmanager$fontdefinitionfile.providers;

            for(int i = list1.size() - 1; i >= 0; --i) {
               FontManager.BuilderId fontmanager$builderid = new FontManager.BuilderId(pFontId, resource.sourcePackId(), i);
               list.add(Pair.of(fontmanager$builderid, list1.get(i)));
            }
         } catch (Exception exception) {
            LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", pFontId, "fonts.json", resource.sourcePackId(), exception);
         }
      }

      return list;
   }

   public void setRenames(Map<ResourceLocation, ResourceLocation> pUnicodeForcedMap) {
      this.renames = pUnicodeForcedMap;
   }

   private ResourceLocation getActualId(ResourceLocation pId) {
      return this.renames.getOrDefault(pId, pId);
   }

   public Font createFont() {
      return new Font((p_284586_) -> {
         return this.fontSets.getOrDefault(this.getActualId(p_284586_), this.missingFontSet);
      }, false);
   }

   public Font createFontFilterFishy() {
      return new Font((p_284596_) -> {
         return this.fontSets.getOrDefault(this.getActualId(p_284596_), this.missingFontSet);
      }, true);
   }

   public void close() {
      this.fontSets.values().forEach(FontSet::close);
      this.providersToClose.forEach(GlyphProvider::close);
      this.missingFontSet.close();
   }

   @OnlyIn(Dist.CLIENT)
   static record BuilderId(ResourceLocation fontId, String pack, int index) {
      public String toString() {
         return "(" + this.fontId + ": builder #" + this.index + " from pack " + this.pack + ")";
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record BuilderResult(FontManager.BuilderId id, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result) {
      public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> pProviderResolver) {
         return this.result.map((p_285332_) -> {
            return p_285332_.join().map(List::of);
         }, (p_285367_) -> {
            List<GlyphProvider> list = pProviderResolver.apply(p_285367_);
            if (list == null) {
               FontManager.LOGGER.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", p_285367_, this.id);
               return Optional.empty();
            } else {
               return Optional.of(list);
            }
         });
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record FontDefinitionFile(List<GlyphProviderDefinition> providers) {
      public static final Codec<FontManager.FontDefinitionFile> CODEC = RecordCodecBuilder.create((p_286425_) -> {
         return p_286425_.group(GlyphProviderDefinition.CODEC.listOf().fieldOf("providers").forGetter(FontManager.FontDefinitionFile::providers)).apply(p_286425_, FontManager.FontDefinitionFile::new);
      });
   }

   @OnlyIn(Dist.CLIENT)
   static record Preparation(Map<ResourceLocation, List<GlyphProvider>> providers, List<GlyphProvider> allProviders) {
   }

   @OnlyIn(Dist.CLIENT)
   static record UnresolvedBuilderBundle(ResourceLocation fontId, List<FontManager.BuilderResult> builders, Set<ResourceLocation> dependencies) implements DependencySorter.Entry<ResourceLocation> {
      public UnresolvedBuilderBundle(ResourceLocation pFontId) {
         this(pFontId, new ArrayList<>(), new HashSet<>());
      }

      public void add(FontManager.BuilderId pBuilderId, GlyphProviderDefinition.Reference pReference) {
         this.builders.add(new FontManager.BuilderResult(pBuilderId, Either.right(pReference.id())));
         this.dependencies.add(pReference.id());
      }

      public void add(FontManager.BuilderId pId, CompletableFuture<Optional<GlyphProvider>> pProviderFuture) {
         this.builders.add(new FontManager.BuilderResult(pId, Either.left(pProviderFuture)));
      }

      private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
         return this.builders.stream().flatMap((p_285041_) -> {
            return p_285041_.result.left().stream();
         });
      }

      public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> pProviderResolver) {
         List<GlyphProvider> list = new ArrayList<>();

         for(FontManager.BuilderResult fontmanager$builderresult : this.builders) {
            Optional<List<GlyphProvider>> optional = fontmanager$builderresult.resolve(pProviderResolver);
            if (!optional.isPresent()) {
               return Optional.empty();
            }

            list.addAll(optional.get());
         }

         return Optional.of(list);
      }

      public void visitRequiredDependencies(Consumer<ResourceLocation> pVisitor) {
         this.dependencies.forEach(pVisitor);
      }

      public void visitOptionalDependencies(Consumer<ResourceLocation> pVisitor) {
      }
   }
}