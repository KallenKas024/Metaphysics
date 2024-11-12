package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {
   public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
   public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
   public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
   public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
   public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
   public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
   public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
   public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
   public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
   public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
   public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
   public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
   public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
   public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
   public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
   public static final ResourceKey<TrimPattern> HOST = registryKey("host");

   public static void bootstrap(BootstapContext<TrimPattern> pContext) {
      register(pContext, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
      register(pContext, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
      register(pContext, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
      register(pContext, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
      register(pContext, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
      register(pContext, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
      register(pContext, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
      register(pContext, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
      register(pContext, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
      register(pContext, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
      register(pContext, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
      register(pContext, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
      register(pContext, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
      register(pContext, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
      register(pContext, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
      register(pContext, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
   }

   public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(RegistryAccess pRegistry, ItemStack pTemplate) {
      return pRegistry.registryOrThrow(Registries.TRIM_PATTERN).holders().filter((p_266833_) -> {
         return pTemplate.is(p_266833_.value().templateItem());
      }).findFirst();
   }

   private static void register(BootstapContext<TrimPattern> pContext, Item pTemplateItem, ResourceKey<TrimPattern> pTrimPatternKey) {
      TrimPattern trimpattern = new TrimPattern(pTrimPatternKey.location(), BuiltInRegistries.ITEM.wrapAsHolder(pTemplateItem), Component.translatable(Util.makeDescriptionId("trim_pattern", pTrimPatternKey.location())));
      pContext.register(pTrimPatternKey, trimpattern);
   }

   private static ResourceKey<TrimPattern> registryKey(String pKey) {
      return ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(pKey));
   }
}