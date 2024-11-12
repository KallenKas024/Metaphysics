package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sheets {
   public static final ResourceLocation SHULKER_SHEET = new ResourceLocation("textures/atlas/shulker_boxes.png");
   public static final ResourceLocation BED_SHEET = new ResourceLocation("textures/atlas/beds.png");
   public static final ResourceLocation BANNER_SHEET = new ResourceLocation("textures/atlas/banner_patterns.png");
   public static final ResourceLocation SHIELD_SHEET = new ResourceLocation("textures/atlas/shield_patterns.png");
   public static final ResourceLocation SIGN_SHEET = new ResourceLocation("textures/atlas/signs.png");
   public static final ResourceLocation CHEST_SHEET = new ResourceLocation("textures/atlas/chest.png");
   public static final ResourceLocation ARMOR_TRIMS_SHEET = new ResourceLocation("textures/atlas/armor_trims.png");
   public static final ResourceLocation DECORATED_POT_SHEET = new ResourceLocation("textures/atlas/decorated_pot.png");
   private static final RenderType SHULKER_BOX_SHEET_TYPE = RenderType.entityCutoutNoCull(SHULKER_SHEET);
   private static final RenderType BED_SHEET_TYPE = RenderType.entitySolid(BED_SHEET);
   private static final RenderType BANNER_SHEET_TYPE = RenderType.entityNoOutline(BANNER_SHEET);
   private static final RenderType SHIELD_SHEET_TYPE = RenderType.entityNoOutline(SHIELD_SHEET);
   private static final RenderType SIGN_SHEET_TYPE = RenderType.entityCutoutNoCull(SIGN_SHEET);
   private static final RenderType CHEST_SHEET_TYPE = RenderType.entityCutout(CHEST_SHEET);
   private static final RenderType ARMOR_TRIMS_SHEET_TYPE = RenderType.armorCutoutNoCull(ARMOR_TRIMS_SHEET);
   private static final RenderType SOLID_BLOCK_SHEET = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
   private static final RenderType CUTOUT_BLOCK_SHEET = RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
   private static final RenderType TRANSLUCENT_ITEM_CULL_BLOCK_SHEET = RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
   private static final RenderType TRANSLUCENT_CULL_BLOCK_SHEET = RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
   public static final Material DEFAULT_SHULKER_TEXTURE_LOCATION = new Material(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker"));
   public static final List<Material> SHULKER_TEXTURE_LOCATION = Stream.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black").map((p_110784_) -> {
      return new Material(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker_" + p_110784_));
   }).collect(ImmutableList.toImmutableList());
   public static final Map<WoodType, Material> SIGN_MATERIALS = WoodType.values().collect(Collectors.toMap(Function.identity(), Sheets::createSignMaterial));
   public static final Map<WoodType, Material> HANGING_SIGN_MATERIALS = WoodType.values().collect(Collectors.toMap(Function.identity(), Sheets::createHangingSignMaterial));
   public static final Map<ResourceKey<BannerPattern>, Material> BANNER_MATERIALS = BuiltInRegistries.BANNER_PATTERN.registryKeySet().stream().collect(Collectors.toMap(Function.identity(), Sheets::createBannerMaterial));
   public static final Map<ResourceKey<BannerPattern>, Material> SHIELD_MATERIALS = BuiltInRegistries.BANNER_PATTERN.registryKeySet().stream().collect(Collectors.toMap(Function.identity(), Sheets::createShieldMaterial));
   public static final Map<ResourceKey<String>, Material> DECORATED_POT_MATERIALS = BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet().stream().collect(Collectors.toMap(Function.identity(), Sheets::createDecoratedPotMaterial));
   public static final Material[] BED_TEXTURES = Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map((p_110766_) -> {
      return new Material(BED_SHEET, new ResourceLocation("entity/bed/" + p_110766_.getName()));
   }).toArray((p_110764_) -> {
      return new Material[p_110764_];
   });
   public static final Material CHEST_TRAP_LOCATION = chestMaterial("trapped");
   public static final Material CHEST_TRAP_LOCATION_LEFT = chestMaterial("trapped_left");
   public static final Material CHEST_TRAP_LOCATION_RIGHT = chestMaterial("trapped_right");
   public static final Material CHEST_XMAS_LOCATION = chestMaterial("christmas");
   public static final Material CHEST_XMAS_LOCATION_LEFT = chestMaterial("christmas_left");
   public static final Material CHEST_XMAS_LOCATION_RIGHT = chestMaterial("christmas_right");
   public static final Material CHEST_LOCATION = chestMaterial("normal");
   public static final Material CHEST_LOCATION_LEFT = chestMaterial("normal_left");
   public static final Material CHEST_LOCATION_RIGHT = chestMaterial("normal_right");
   public static final Material ENDER_CHEST_LOCATION = chestMaterial("ender");

   public static RenderType bannerSheet() {
      return BANNER_SHEET_TYPE;
   }

   public static RenderType shieldSheet() {
      return SHIELD_SHEET_TYPE;
   }

   public static RenderType bedSheet() {
      return BED_SHEET_TYPE;
   }

   public static RenderType shulkerBoxSheet() {
      return SHULKER_BOX_SHEET_TYPE;
   }

   public static RenderType signSheet() {
      return SIGN_SHEET_TYPE;
   }

   public static RenderType hangingSignSheet() {
      return SIGN_SHEET_TYPE;
   }

   public static RenderType chestSheet() {
      return CHEST_SHEET_TYPE;
   }

   public static RenderType armorTrimsSheet() {
      return ARMOR_TRIMS_SHEET_TYPE;
   }

   public static RenderType solidBlockSheet() {
      return SOLID_BLOCK_SHEET;
   }

   public static RenderType cutoutBlockSheet() {
      return CUTOUT_BLOCK_SHEET;
   }

   public static RenderType translucentItemSheet() {
      return TRANSLUCENT_ITEM_CULL_BLOCK_SHEET;
   }

   public static RenderType translucentCullBlockSheet() {
      return TRANSLUCENT_CULL_BLOCK_SHEET;
   }

   public static void getAllMaterials(Consumer<Material> pMaterialConsumer) {
      pMaterialConsumer.accept(DEFAULT_SHULKER_TEXTURE_LOCATION);
      SHULKER_TEXTURE_LOCATION.forEach(pMaterialConsumer);
      BANNER_MATERIALS.values().forEach(pMaterialConsumer);
      SHIELD_MATERIALS.values().forEach(pMaterialConsumer);
      SIGN_MATERIALS.values().forEach(pMaterialConsumer);
      HANGING_SIGN_MATERIALS.values().forEach(pMaterialConsumer);

      for(Material material : BED_TEXTURES) {
         pMaterialConsumer.accept(material);
      }

      pMaterialConsumer.accept(CHEST_TRAP_LOCATION);
      pMaterialConsumer.accept(CHEST_TRAP_LOCATION_LEFT);
      pMaterialConsumer.accept(CHEST_TRAP_LOCATION_RIGHT);
      pMaterialConsumer.accept(CHEST_XMAS_LOCATION);
      pMaterialConsumer.accept(CHEST_XMAS_LOCATION_LEFT);
      pMaterialConsumer.accept(CHEST_XMAS_LOCATION_RIGHT);
      pMaterialConsumer.accept(CHEST_LOCATION);
      pMaterialConsumer.accept(CHEST_LOCATION_LEFT);
      pMaterialConsumer.accept(CHEST_LOCATION_RIGHT);
      pMaterialConsumer.accept(ENDER_CHEST_LOCATION);
   }

   private static Material createSignMaterial(WoodType p_173386_) {
      ResourceLocation location = new ResourceLocation(p_173386_.name());
      return new Material(SIGN_SHEET, new ResourceLocation(location.getNamespace(), "entity/signs/" + location.getPath()));
   }

   private static Material createHangingSignMaterial(WoodType p_251735_) {
      ResourceLocation location = new ResourceLocation(p_251735_.name());
      return new Material(SIGN_SHEET, new ResourceLocation(location.getNamespace(), "entity/signs/hanging/" + location.getPath()));
   }

   public static Material getSignMaterial(WoodType pWoodType) {
      return SIGN_MATERIALS.get(pWoodType);
   }

   public static Material getHangingSignMaterial(WoodType pWoodType) {
      return HANGING_SIGN_MATERIALS.get(pWoodType);
   }

   private static Material createBannerMaterial(ResourceKey<BannerPattern> p_234352_) {
      return new Material(BANNER_SHEET, BannerPattern.location(p_234352_, true));
   }

   public static Material getBannerMaterial(ResourceKey<BannerPattern> pBannerPattern) {
      return BANNER_MATERIALS.get(pBannerPattern);
   }

   private static Material createShieldMaterial(ResourceKey<BannerPattern> p_234354_) {
      return new Material(SHIELD_SHEET, BannerPattern.location(p_234354_, false));
   }

   public static Material getShieldMaterial(ResourceKey<BannerPattern> pBannerPattern) {
      return SHIELD_MATERIALS.get(pBannerPattern);
   }

   private static Material chestMaterial(String pChestName) {
      return new Material(CHEST_SHEET, new ResourceLocation("entity/chest/" + pChestName));
   }

   private static Material createDecoratedPotMaterial(ResourceKey<String> p_272805_) {
      return new Material(DECORATED_POT_SHEET, DecoratedPotPatterns.location(p_272805_));
   }

   @Nullable
   public static Material getDecoratedPotMaterial(@Nullable ResourceKey<String> pKey) {
      return pKey == null ? null : DECORATED_POT_MATERIALS.get(pKey);
   }

   public static Material chooseMaterial(BlockEntity pBlockEntity, ChestType pChestType, boolean pHoliday) {
      if (pBlockEntity instanceof EnderChestBlockEntity) {
         return ENDER_CHEST_LOCATION;
      } else if (pHoliday) {
         return chooseMaterial(pChestType, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT);
      } else {
         return pBlockEntity instanceof TrappedChestBlockEntity ? chooseMaterial(pChestType, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT) : chooseMaterial(pChestType, CHEST_LOCATION, CHEST_LOCATION_LEFT, CHEST_LOCATION_RIGHT);
      }
   }

   private static Material chooseMaterial(ChestType pChestType, Material pDoubleMaterial, Material pLeftMaterial, Material pRightMaterial) {
      switch (pChestType) {
         case LEFT:
            return pLeftMaterial;
         case RIGHT:
            return pRightMaterial;
         case SINGLE:
         default:
            return pDoubleMaterial;
      }
   }

   /**
    * Not threadsafe. Enqueue it in client setup.
    */
   public static void addWoodType(WoodType woodType) {
      SIGN_MATERIALS.put(woodType, createSignMaterial(woodType));
      HANGING_SIGN_MATERIALS.put(woodType, createHangingSignMaterial(woodType));
   }

   static {
      if (net.minecraftforge.fml.ModLoader.isLoadingStateValid() && !net.minecraftforge.fml.ModLoader.get().hasCompletedState("LOAD_REGISTRIES")) {
         com.mojang.logging.LogUtils.getLogger().error(
                 "net.minecraft.client.renderer.Sheets loaded too early, modded registry-based materials may not work correctly",
                 new IllegalStateException("net.minecraft.client.renderer.Sheets loaded too early")
         );
      }
   }
}
