package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;

public class PaintingVariantTagsProvider extends TagsProvider<PaintingVariant> {
   /** @deprecated Forge: Use the {@linkplain #PaintingVariantTagsProvider(PackOutput, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant} */
   @Deprecated
   public PaintingVariantTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider) {
      super(pOutput, Registries.PAINTING_VARIANT, pProvider);
   }
   public PaintingVariantTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pOutput, Registries.PAINTING_VARIANT, pProvider, modId, existingFileHelper);
   }

   protected void addTags(HolderLookup.Provider pProvider) {
      this.tag(PaintingVariantTags.PLACEABLE).add(PaintingVariants.KEBAB, PaintingVariants.AZTEC, PaintingVariants.ALBAN, PaintingVariants.AZTEC2, PaintingVariants.BOMB, PaintingVariants.PLANT, PaintingVariants.WASTELAND, PaintingVariants.POOL, PaintingVariants.COURBET, PaintingVariants.SEA, PaintingVariants.SUNSET, PaintingVariants.CREEBET, PaintingVariants.WANDERER, PaintingVariants.GRAHAM, PaintingVariants.MATCH, PaintingVariants.BUST, PaintingVariants.STAGE, PaintingVariants.VOID, PaintingVariants.SKULL_AND_ROSES, PaintingVariants.WITHER, PaintingVariants.FIGHTERS, PaintingVariants.POINTER, PaintingVariants.PIGSCENE, PaintingVariants.BURNING_SKULL, PaintingVariants.SKELETON, PaintingVariants.DONKEY_KONG);
   }
}
