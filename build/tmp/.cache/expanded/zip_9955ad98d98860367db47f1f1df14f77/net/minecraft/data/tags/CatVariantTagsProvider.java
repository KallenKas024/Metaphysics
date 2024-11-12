package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.world.entity.animal.CatVariant;

public class CatVariantTagsProvider extends TagsProvider<CatVariant> {
   /** @deprecated Forge: Use the {@linkplain #CatVariantTagsProvider(PackOutput, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant} */
   @Deprecated
   public CatVariantTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider) {
      super(pOutput, Registries.CAT_VARIANT, pProvider);
   }
   public CatVariantTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pOutput, Registries.CAT_VARIANT, pProvider, modId, existingFileHelper);
   }

   protected void addTags(HolderLookup.Provider pProvider) {
      this.tag(CatVariantTags.DEFAULT_SPAWNS).add(CatVariant.TABBY, CatVariant.BLACK, CatVariant.RED, CatVariant.SIAMESE, CatVariant.BRITISH_SHORTHAIR, CatVariant.CALICO, CatVariant.PERSIAN, CatVariant.RAGDOLL, CatVariant.WHITE, CatVariant.JELLIE);
      this.tag(CatVariantTags.FULL_MOON_SPAWNS).addTag(CatVariantTags.DEFAULT_SPAWNS).add(CatVariant.ALL_BLACK);
   }
}
