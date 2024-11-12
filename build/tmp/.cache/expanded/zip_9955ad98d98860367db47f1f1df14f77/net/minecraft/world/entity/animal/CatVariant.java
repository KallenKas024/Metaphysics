package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record CatVariant(ResourceLocation texture) {
   public static final ResourceKey<CatVariant> TABBY = createKey("tabby");
   public static final ResourceKey<CatVariant> BLACK = createKey("black");
   public static final ResourceKey<CatVariant> RED = createKey("red");
   public static final ResourceKey<CatVariant> SIAMESE = createKey("siamese");
   public static final ResourceKey<CatVariant> BRITISH_SHORTHAIR = createKey("british_shorthair");
   public static final ResourceKey<CatVariant> CALICO = createKey("calico");
   public static final ResourceKey<CatVariant> PERSIAN = createKey("persian");
   public static final ResourceKey<CatVariant> RAGDOLL = createKey("ragdoll");
   public static final ResourceKey<CatVariant> WHITE = createKey("white");
   public static final ResourceKey<CatVariant> JELLIE = createKey("jellie");
   public static final ResourceKey<CatVariant> ALL_BLACK = createKey("all_black");

   private static ResourceKey<CatVariant> createKey(String pName) {
      return ResourceKey.create(Registries.CAT_VARIANT, new ResourceLocation(pName));
   }

   public static CatVariant bootstrap(Registry<CatVariant> pRegistry) {
      register(pRegistry, TABBY, "textures/entity/cat/tabby.png");
      register(pRegistry, BLACK, "textures/entity/cat/black.png");
      register(pRegistry, RED, "textures/entity/cat/red.png");
      register(pRegistry, SIAMESE, "textures/entity/cat/siamese.png");
      register(pRegistry, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
      register(pRegistry, CALICO, "textures/entity/cat/calico.png");
      register(pRegistry, PERSIAN, "textures/entity/cat/persian.png");
      register(pRegistry, RAGDOLL, "textures/entity/cat/ragdoll.png");
      register(pRegistry, WHITE, "textures/entity/cat/white.png");
      register(pRegistry, JELLIE, "textures/entity/cat/jellie.png");
      return register(pRegistry, ALL_BLACK, "textures/entity/cat/all_black.png");
   }

   private static CatVariant register(Registry<CatVariant> pRegistry, ResourceKey<CatVariant> pKey, String pTexture) {
      return Registry.register(pRegistry, pKey, new CatVariant(new ResourceLocation(pTexture)));
   }
}