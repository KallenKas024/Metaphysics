package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockColors {
   private static final int DEFAULT = -1;
   // FORGE: Use registry delegate as non-Vanilla block ids are not constant
   private final java.util.Map<net.minecraft.core.Holder.Reference<Block>, BlockColor> blockColors = new java.util.HashMap<>();
   private final Map<Block, Set<Property<?>>> coloringStates = Maps.newHashMap();

   public static BlockColors createDefault() {
      BlockColors blockcolors = new BlockColors();
      blockcolors.register((p_276233_, p_276234_, p_276235_, p_276236_) -> {
         return p_276234_ != null && p_276235_ != null ? BiomeColors.getAverageGrassColor(p_276234_, p_276233_.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? p_276235_.below() : p_276235_) : GrassColor.getDefaultColor();
      }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.register((p_276237_, p_276238_, p_276239_, p_276240_) -> {
         return p_276238_ != null && p_276239_ != null ? BiomeColors.getAverageGrassColor(p_276238_, p_276239_) : GrassColor.getDefaultColor();
      }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
      blockcolors.register((p_276241_, p_276242_, p_276243_, p_276244_) -> {
         if (p_276244_ != 0) {
            return p_276242_ != null && p_276243_ != null ? BiomeColors.getAverageGrassColor(p_276242_, p_276243_) : GrassColor.getDefaultColor();
         } else {
            return -1;
         }
      }, Blocks.PINK_PETALS);
      blockcolors.register((p_92636_, p_92637_, p_92638_, p_92639_) -> {
         return FoliageColor.getEvergreenColor();
      }, Blocks.SPRUCE_LEAVES);
      blockcolors.register((p_92631_, p_92632_, p_92633_, p_92634_) -> {
         return FoliageColor.getBirchColor();
      }, Blocks.BIRCH_LEAVES);
      blockcolors.register((p_92626_, p_92627_, p_92628_, p_92629_) -> {
         return p_92627_ != null && p_92628_ != null ? BiomeColors.getAverageFoliageColor(p_92627_, p_92628_) : FoliageColor.getDefaultColor();
      }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE, Blocks.MANGROVE_LEAVES);
      blockcolors.register((p_92621_, p_92622_, p_92623_, p_92624_) -> {
         return p_92622_ != null && p_92623_ != null ? BiomeColors.getAverageWaterColor(p_92622_, p_92623_) : -1;
      }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
      blockcolors.register((p_92616_, p_92617_, p_92618_, p_92619_) -> {
         return RedStoneWireBlock.getColorForPower(p_92616_.getValue(RedStoneWireBlock.POWER));
      }, Blocks.REDSTONE_WIRE);
      blockcolors.addColoringState(RedStoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
      blockcolors.register((p_92611_, p_92612_, p_92613_, p_92614_) -> {
         return p_92612_ != null && p_92613_ != null ? BiomeColors.getAverageGrassColor(p_92612_, p_92613_) : -1;
      }, Blocks.SUGAR_CANE);
      blockcolors.register((p_92606_, p_92607_, p_92608_, p_92609_) -> {
         return 14731036;
      }, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
      blockcolors.register((p_92601_, p_92602_, p_92603_, p_92604_) -> {
         int i = p_92601_.getValue(StemBlock.AGE);
         int j = i * 32;
         int k = 255 - i * 8;
         int l = i * 4;
         return j << 16 | k << 8 | l;
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.register((p_92596_, p_92597_, p_92598_, p_92599_) -> {
         return p_92597_ != null && p_92598_ != null ? 2129968 : 7455580;
      }, Blocks.LILY_PAD);
      net.minecraftforge.client.ForgeHooksClient.onBlockColorsInit(blockcolors);
      return blockcolors;
   }

   public int getColor(BlockState pState, Level pLevel, BlockPos pPos) {
      BlockColor blockcolor = this.blockColors.get(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(pState.getBlock()));
      if (blockcolor != null) {
         return blockcolor.getColor(pState, (BlockAndTintGetter)null, (BlockPos)null, 0);
      } else {
         MapColor mapcolor = pState.getMapColor(pLevel, pPos);
         return mapcolor != null ? mapcolor.col : -1;
      }
   }

   public int getColor(BlockState pState, @Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex) {
      BlockColor blockcolor = this.blockColors.get(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(pState.getBlock()));
      return blockcolor == null ? -1 : blockcolor.getColor(pState, pLevel, pPos, pTintIndex);
   }

   /** @deprecated Register via {@link net.minecraftforge.client.event.RegisterColorHandlersEvent.Block} */
   @Deprecated
   public void register(BlockColor pBlockColor, Block... pBlocks) {
      for(Block block : pBlocks) {
         this.blockColors.put(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(block), pBlockColor);
      }

   }

   private void addColoringStates(Set<Property<?>> pProperties, Block... pBlocks) {
      for(Block block : pBlocks) {
         this.coloringStates.put(block, pProperties);
      }

   }

   private void addColoringState(Property<?> pProperty, Block... pBlocks) {
      this.addColoringStates(ImmutableSet.of(pProperty), pBlocks);
   }

   public Set<Property<?>> getColoringProperties(Block pBlock) {
      return this.coloringStates.getOrDefault(pBlock, ImmutableSet.of());
   }
}
