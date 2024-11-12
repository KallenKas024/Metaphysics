package net.minecraft.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TextureMapping {
   private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
   private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

   public TextureMapping put(TextureSlot pTextureSlot, ResourceLocation pTextureLocation) {
      this.slots.put(pTextureSlot, pTextureLocation);
      return this;
   }

   public TextureMapping putForced(TextureSlot pTextureSlot, ResourceLocation pTextureLocation) {
      this.slots.put(pTextureSlot, pTextureLocation);
      this.forcedSlots.add(pTextureSlot);
      return this;
   }

   public Stream<TextureSlot> getForced() {
      return this.forcedSlots.stream();
   }

   public TextureMapping copySlot(TextureSlot pSourceSlot, TextureSlot pTargetSlot) {
      this.slots.put(pTargetSlot, this.slots.get(pSourceSlot));
      return this;
   }

   public TextureMapping copyForced(TextureSlot pSourceSlot, TextureSlot pTargetSlot) {
      this.slots.put(pTargetSlot, this.slots.get(pSourceSlot));
      this.forcedSlots.add(pTargetSlot);
      return this;
   }

   public ResourceLocation get(TextureSlot pTextureSlot) {
      for(TextureSlot textureslot = pTextureSlot; textureslot != null; textureslot = textureslot.getParent()) {
         ResourceLocation resourcelocation = this.slots.get(textureslot);
         if (resourcelocation != null) {
            return resourcelocation;
         }
      }

      throw new IllegalStateException("Can't find texture for slot " + pTextureSlot);
   }

   public TextureMapping copyAndUpdate(TextureSlot pTextureSlot, ResourceLocation pTextureLocation) {
      TextureMapping texturemapping = new TextureMapping();
      texturemapping.slots.putAll(this.slots);
      texturemapping.forcedSlots.addAll(this.forcedSlots);
      texturemapping.put(pTextureSlot, pTextureLocation);
      return texturemapping;
   }

   public static TextureMapping cube(Block pBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pBlock);
      return cube(resourcelocation);
   }

   public static TextureMapping defaultTexture(Block pBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pBlock);
      return defaultTexture(resourcelocation);
   }

   public static TextureMapping defaultTexture(ResourceLocation pTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.TEXTURE, pTextureLocation);
   }

   public static TextureMapping cube(ResourceLocation pAllTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.ALL, pAllTextureLocation);
   }

   public static TextureMapping cross(Block pBlock) {
      return singleSlot(TextureSlot.CROSS, getBlockTexture(pBlock));
   }

   public static TextureMapping cross(ResourceLocation pCrossTextureLocation) {
      return singleSlot(TextureSlot.CROSS, pCrossTextureLocation);
   }

   public static TextureMapping plant(Block pPlantBlock) {
      return singleSlot(TextureSlot.PLANT, getBlockTexture(pPlantBlock));
   }

   public static TextureMapping plant(ResourceLocation pPlantTextureLocation) {
      return singleSlot(TextureSlot.PLANT, pPlantTextureLocation);
   }

   public static TextureMapping rail(Block pRailBlock) {
      return singleSlot(TextureSlot.RAIL, getBlockTexture(pRailBlock));
   }

   public static TextureMapping rail(ResourceLocation pRailTextureLocation) {
      return singleSlot(TextureSlot.RAIL, pRailTextureLocation);
   }

   public static TextureMapping wool(Block pWoolBlock) {
      return singleSlot(TextureSlot.WOOL, getBlockTexture(pWoolBlock));
   }

   public static TextureMapping flowerbed(Block pFlowerbedBlock) {
      return (new TextureMapping()).put(TextureSlot.FLOWERBED, getBlockTexture(pFlowerbedBlock)).put(TextureSlot.STEM, getBlockTexture(pFlowerbedBlock, "_stem"));
   }

   public static TextureMapping wool(ResourceLocation pWoolTextureLocation) {
      return singleSlot(TextureSlot.WOOL, pWoolTextureLocation);
   }

   public static TextureMapping stem(Block pStemBlock) {
      return singleSlot(TextureSlot.STEM, getBlockTexture(pStemBlock));
   }

   public static TextureMapping attachedStem(Block pUnattachedStemBlock, Block pAttachedStemBlock) {
      return (new TextureMapping()).put(TextureSlot.STEM, getBlockTexture(pUnattachedStemBlock)).put(TextureSlot.UPPER_STEM, getBlockTexture(pAttachedStemBlock));
   }

   public static TextureMapping pattern(Block pPatternBlock) {
      return singleSlot(TextureSlot.PATTERN, getBlockTexture(pPatternBlock));
   }

   public static TextureMapping fan(Block pFanBlock) {
      return singleSlot(TextureSlot.FAN, getBlockTexture(pFanBlock));
   }

   public static TextureMapping crop(ResourceLocation pCropTextureLocation) {
      return singleSlot(TextureSlot.CROP, pCropTextureLocation);
   }

   public static TextureMapping pane(Block pGlassBlock, Block pPaneBlock) {
      return (new TextureMapping()).put(TextureSlot.PANE, getBlockTexture(pGlassBlock)).put(TextureSlot.EDGE, getBlockTexture(pPaneBlock, "_top"));
   }

   public static TextureMapping singleSlot(TextureSlot pTextureSlot, ResourceLocation pTextureLocation) {
      return (new TextureMapping()).put(pTextureSlot, pTextureLocation);
   }

   public static TextureMapping column(Block pColumnBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pColumnBlock, "_side")).put(TextureSlot.END, getBlockTexture(pColumnBlock, "_top"));
   }

   public static TextureMapping cubeTop(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pBlock, "_side")).put(TextureSlot.TOP, getBlockTexture(pBlock, "_top"));
   }

   public static TextureMapping pottedAzalea(Block pAzaleaBlock) {
      return (new TextureMapping()).put(TextureSlot.PLANT, getBlockTexture(pAzaleaBlock, "_plant")).put(TextureSlot.SIDE, getBlockTexture(pAzaleaBlock, "_side")).put(TextureSlot.TOP, getBlockTexture(pAzaleaBlock, "_top"));
   }

   public static TextureMapping logColumn(Block pLogBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pLogBlock)).put(TextureSlot.END, getBlockTexture(pLogBlock, "_top")).put(TextureSlot.PARTICLE, getBlockTexture(pLogBlock));
   }

   public static TextureMapping column(ResourceLocation pSideTextureLocation, ResourceLocation pEndTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.SIDE, pSideTextureLocation).put(TextureSlot.END, pEndTextureLocation);
   }

   public static TextureMapping fence(Block pFenceBlock) {
      return (new TextureMapping()).put(TextureSlot.TEXTURE, getBlockTexture(pFenceBlock)).put(TextureSlot.SIDE, getBlockTexture(pFenceBlock, "_side")).put(TextureSlot.TOP, getBlockTexture(pFenceBlock, "_top"));
   }

   public static TextureMapping customParticle(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.TEXTURE, getBlockTexture(pBlock)).put(TextureSlot.PARTICLE, getBlockTexture(pBlock, "_particle"));
   }

   public static TextureMapping cubeBottomTop(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pBlock, "_side")).put(TextureSlot.TOP, getBlockTexture(pBlock, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(pBlock, "_bottom"));
   }

   public static TextureMapping cubeBottomTopWithWall(Block pBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pBlock);
      return (new TextureMapping()).put(TextureSlot.WALL, resourcelocation).put(TextureSlot.SIDE, resourcelocation).put(TextureSlot.TOP, getBlockTexture(pBlock, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(pBlock, "_bottom"));
   }

   public static TextureMapping columnWithWall(Block pColumnBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pColumnBlock);
      return (new TextureMapping()).put(TextureSlot.TEXTURE, resourcelocation).put(TextureSlot.WALL, resourcelocation).put(TextureSlot.SIDE, resourcelocation).put(TextureSlot.END, getBlockTexture(pColumnBlock, "_top"));
   }

   public static TextureMapping door(ResourceLocation pTopTextureLocation, ResourceLocation pBottomTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.TOP, pTopTextureLocation).put(TextureSlot.BOTTOM, pBottomTextureLocation);
   }

   public static TextureMapping door(Block pDoorBlock) {
      return (new TextureMapping()).put(TextureSlot.TOP, getBlockTexture(pDoorBlock, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(pDoorBlock, "_bottom"));
   }

   public static TextureMapping particle(Block pParticleBlock) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(pParticleBlock));
   }

   public static TextureMapping particle(ResourceLocation pTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, pTextureLocation);
   }

   public static TextureMapping fire0(Block pFireBlock) {
      return (new TextureMapping()).put(TextureSlot.FIRE, getBlockTexture(pFireBlock, "_0"));
   }

   public static TextureMapping fire1(Block pFireBlock) {
      return (new TextureMapping()).put(TextureSlot.FIRE, getBlockTexture(pFireBlock, "_1"));
   }

   public static TextureMapping lantern(Block pLanternBlock) {
      return (new TextureMapping()).put(TextureSlot.LANTERN, getBlockTexture(pLanternBlock));
   }

   public static TextureMapping torch(Block pTorchBlock) {
      return (new TextureMapping()).put(TextureSlot.TORCH, getBlockTexture(pTorchBlock));
   }

   public static TextureMapping torch(ResourceLocation pTorchTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.TORCH, pTorchTextureLocation);
   }

   public static TextureMapping particleFromItem(Item pParticleItem) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getItemTexture(pParticleItem));
   }

   public static TextureMapping commandBlock(Block pCommandBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pCommandBlock, "_side")).put(TextureSlot.FRONT, getBlockTexture(pCommandBlock, "_front")).put(TextureSlot.BACK, getBlockTexture(pCommandBlock, "_back"));
   }

   public static TextureMapping orientableCube(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pBlock, "_side")).put(TextureSlot.FRONT, getBlockTexture(pBlock, "_front")).put(TextureSlot.TOP, getBlockTexture(pBlock, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(pBlock, "_bottom"));
   }

   public static TextureMapping orientableCubeOnlyTop(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pBlock, "_side")).put(TextureSlot.FRONT, getBlockTexture(pBlock, "_front")).put(TextureSlot.TOP, getBlockTexture(pBlock, "_top"));
   }

   public static TextureMapping orientableCubeSameEnds(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(pBlock, "_side")).put(TextureSlot.FRONT, getBlockTexture(pBlock, "_front")).put(TextureSlot.END, getBlockTexture(pBlock, "_end"));
   }

   public static TextureMapping top(Block pBlock) {
      return (new TextureMapping()).put(TextureSlot.TOP, getBlockTexture(pBlock, "_top"));
   }

   public static TextureMapping craftingTable(Block pCraftingTableBlock, Block pCraftingTableMaterialBlock) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(pCraftingTableBlock, "_front")).put(TextureSlot.DOWN, getBlockTexture(pCraftingTableMaterialBlock)).put(TextureSlot.UP, getBlockTexture(pCraftingTableBlock, "_top")).put(TextureSlot.NORTH, getBlockTexture(pCraftingTableBlock, "_front")).put(TextureSlot.EAST, getBlockTexture(pCraftingTableBlock, "_side")).put(TextureSlot.SOUTH, getBlockTexture(pCraftingTableBlock, "_side")).put(TextureSlot.WEST, getBlockTexture(pCraftingTableBlock, "_front"));
   }

   public static TextureMapping fletchingTable(Block pFletchingTableBlock, Block pFletchingTableMaterialBlock) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(pFletchingTableBlock, "_front")).put(TextureSlot.DOWN, getBlockTexture(pFletchingTableMaterialBlock)).put(TextureSlot.UP, getBlockTexture(pFletchingTableBlock, "_top")).put(TextureSlot.NORTH, getBlockTexture(pFletchingTableBlock, "_front")).put(TextureSlot.SOUTH, getBlockTexture(pFletchingTableBlock, "_front")).put(TextureSlot.EAST, getBlockTexture(pFletchingTableBlock, "_side")).put(TextureSlot.WEST, getBlockTexture(pFletchingTableBlock, "_side"));
   }

   public static TextureMapping snifferEgg(String pCrackLevel) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_north")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_bottom")).put(TextureSlot.TOP, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_top")).put(TextureSlot.NORTH, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_north")).put(TextureSlot.SOUTH, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_south")).put(TextureSlot.EAST, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_east")).put(TextureSlot.WEST, getBlockTexture(Blocks.SNIFFER_EGG, pCrackLevel + "_west"));
   }

   public static TextureMapping campfire(Block pCampfireBlock) {
      return (new TextureMapping()).put(TextureSlot.LIT_LOG, getBlockTexture(pCampfireBlock, "_log_lit")).put(TextureSlot.FIRE, getBlockTexture(pCampfireBlock, "_fire"));
   }

   public static TextureMapping candleCake(Block pCandleCakeBlock, boolean pLit) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAKE, "_side")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAKE, "_bottom")).put(TextureSlot.TOP, getBlockTexture(Blocks.CAKE, "_top")).put(TextureSlot.SIDE, getBlockTexture(Blocks.CAKE, "_side")).put(TextureSlot.CANDLE, getBlockTexture(pCandleCakeBlock, pLit ? "_lit" : ""));
   }

   public static TextureMapping cauldron(ResourceLocation pCauldronContentTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAULDRON, "_side")).put(TextureSlot.SIDE, getBlockTexture(Blocks.CAULDRON, "_side")).put(TextureSlot.TOP, getBlockTexture(Blocks.CAULDRON, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAULDRON, "_bottom")).put(TextureSlot.INSIDE, getBlockTexture(Blocks.CAULDRON, "_inner")).put(TextureSlot.CONTENT, pCauldronContentTextureLocation);
   }

   public static TextureMapping sculkShrieker(boolean pCanSummon) {
      String s = pCanSummon ? "_can_summon" : "";
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom")).put(TextureSlot.SIDE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_side")).put(TextureSlot.TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, "_top")).put(TextureSlot.INNER_TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, s + "_inner_top")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"));
   }

   public static TextureMapping layer0(Item pLayerZeroItem) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, getItemTexture(pLayerZeroItem));
   }

   public static TextureMapping layer0(Block pLayerZeroBlock) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, getBlockTexture(pLayerZeroBlock));
   }

   public static TextureMapping layer0(ResourceLocation pLayerZeroTextureLocation) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, pLayerZeroTextureLocation);
   }

   public static TextureMapping layered(ResourceLocation pLayer0, ResourceLocation pLayer1) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, pLayer0).put(TextureSlot.LAYER1, pLayer1);
   }

   public static TextureMapping layered(ResourceLocation pLayer0, ResourceLocation pLayer1, ResourceLocation pLayer2) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, pLayer0).put(TextureSlot.LAYER1, pLayer1).put(TextureSlot.LAYER2, pLayer2);
   }

   public static ResourceLocation getBlockTexture(Block pBlock) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(pBlock);
      return resourcelocation.withPrefix("block/");
   }

   public static ResourceLocation getBlockTexture(Block pBlock, String pTextureSuffix) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(pBlock);
      return resourcelocation.withPath((p_248521_) -> {
         return "block/" + p_248521_ + pTextureSuffix;
      });
   }

   public static ResourceLocation getItemTexture(Item pItem) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(pItem);
      return resourcelocation.withPrefix("item/");
   }

   public static ResourceLocation getItemTexture(Item pItem, String pTextureSuffix) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(pItem);
      return resourcelocation.withPath((p_252192_) -> {
         return "item/" + p_252192_ + pTextureSuffix;
      });
   }
}