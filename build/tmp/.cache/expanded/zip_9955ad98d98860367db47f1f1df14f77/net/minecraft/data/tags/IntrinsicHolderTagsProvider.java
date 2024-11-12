package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T> extends TagsProvider<T> {
   private final Function<T, ResourceKey<T>> keyExtractor;

   /**
    * @deprecated Forge: Use the {@linkplain #IntrinsicHolderTagsProvider(PackOutput, ResourceKey, CompletableFuture, Function, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
    */
   @Deprecated
   public IntrinsicHolderTagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, Function<T, ResourceKey<T>> pKeyExtractor) {
      this(pOutput, pRegistryKey, pLookupProvider, pKeyExtractor, "vanilla", null);
   }
   public IntrinsicHolderTagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, Function<T, ResourceKey<T>> pKeyExtractor, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pOutput, pRegistryKey, pLookupProvider, modId, existingFileHelper);
      this.keyExtractor = pKeyExtractor;
   }

   /**
    * @deprecated Forge: Use the {@linkplain #IntrinsicHolderTagsProvider(PackOutput, ResourceKey, CompletableFuture, CompletableFuture, Function, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
    */
   @Deprecated
   public IntrinsicHolderTagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<T>> pParentProvider, Function<T, ResourceKey<T>> pKeyExtractor) {
      this(pOutput, pRegistryKey, pLookupProvider, pParentProvider, pKeyExtractor, "vanilla", null);
   }
   public IntrinsicHolderTagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<T>> pParentProvider, Function<T, ResourceKey<T>> pKeyExtractor, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pOutput, pRegistryKey, pLookupProvider, pParentProvider, modId, existingFileHelper);
      this.keyExtractor = pKeyExtractor;
   }

   protected IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tag(TagKey<T> pTag) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(pTag);
      return new IntrinsicHolderTagsProvider.IntrinsicTagAppender<>(tagbuilder, this.keyExtractor, this.modId);
   }

   public static class IntrinsicTagAppender<T> extends TagsProvider.TagAppender<T> implements net.minecraftforge.common.extensions.IForgeIntrinsicHolderTagAppender<T> {
      private final Function<T, ResourceKey<T>> keyExtractor;

      IntrinsicTagAppender(TagBuilder pBuilder, Function<T, ResourceKey<T>> pKeyExtractor, String modId) {
         super(pBuilder, modId);
         this.keyExtractor = pKeyExtractor;
      }

      public IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTag(TagKey<T> pTag) {
         super.addTag(pTag);
         return this;
      }

      public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T pValue) {
         this.add(this.keyExtractor.apply(pValue));
         return this;
      }

      @SafeVarargs
      public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T... pValues) {
         Stream.<T>of(pValues).map(this.keyExtractor).forEach(this::add);
         return this;
      }

      @Override
      public final ResourceKey<T> getKey(T value) {
         return this.keyExtractor.apply(value);
      }
   }
}
