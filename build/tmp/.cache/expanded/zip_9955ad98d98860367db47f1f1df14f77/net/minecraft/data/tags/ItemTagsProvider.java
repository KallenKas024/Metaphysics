package net.minecraft.data.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
   /** A function that resolves block tag builders. */
   private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
   private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

   /**
    * @deprecated Forge: Use the {@linkplain #ItemTagsProvider(PackOutput, CompletableFuture, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
    */
   @Deprecated
   public ItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> pBlockTags) {
      this(pOutput, pLookupProvider, pBlockTags, "vanilla", null);
   }
   public ItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> pBlockTags, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pOutput, Registries.ITEM, pLookupProvider, (p_255790_) -> {
         return p_255790_.builtInRegistryHolder().key();
      }, modId, existingFileHelper);
      this.blockTags = pBlockTags;
   }

   /**
    * @deprecated Forge: Use the {@linkplain #ItemTagsProvider(PackOutput, CompletableFuture, CompletableFuture, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
    */
   @Deprecated
   public ItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<Item>> pParentProvider, CompletableFuture<TagsProvider.TagLookup<Block>> pBlockTags) {
      this(pOutput, pLookupProvider, pParentProvider, pBlockTags, "vanilla", null);
   }
   public ItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<Item>> pParentProvider, CompletableFuture<TagsProvider.TagLookup<Block>> pBlockTags, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pOutput, Registries.ITEM, pLookupProvider, pParentProvider, (p_274765_) -> {
         return p_274765_.builtInRegistryHolder().key();
      });
      this.blockTags = pBlockTags;
   }

   /**
    * Copies the entries from a block tag into an item tag.
    */
   protected void copy(TagKey<Block> pBlockTag, TagKey<Item> pItemTag) {
      this.tagsToCopy.put(pBlockTag, pItemTag);
   }

   protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
      return super.createContentsProvider().thenCombineAsync(this.blockTags, (p_274766_, p_274767_) -> {
         this.tagsToCopy.forEach((p_274763_, p_274764_) -> {
            TagBuilder tagbuilder = this.getOrCreateRawBuilder(p_274764_);
            Optional<TagBuilder> optional = p_274767_.apply(p_274763_);
            optional.orElseThrow(() -> {
               return new IllegalStateException("Missing block tag " + p_274764_.location());
            }).build().forEach(tagbuilder::add);
         });
         return p_274766_;
      });
   }
}
