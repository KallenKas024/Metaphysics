package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiTypes {
   public static final ResourceKey<PoiType> ARMORER = createKey("armorer");
   public static final ResourceKey<PoiType> BUTCHER = createKey("butcher");
   public static final ResourceKey<PoiType> CARTOGRAPHER = createKey("cartographer");
   public static final ResourceKey<PoiType> CLERIC = createKey("cleric");
   public static final ResourceKey<PoiType> FARMER = createKey("farmer");
   public static final ResourceKey<PoiType> FISHERMAN = createKey("fisherman");
   public static final ResourceKey<PoiType> FLETCHER = createKey("fletcher");
   public static final ResourceKey<PoiType> LEATHERWORKER = createKey("leatherworker");
   public static final ResourceKey<PoiType> LIBRARIAN = createKey("librarian");
   public static final ResourceKey<PoiType> MASON = createKey("mason");
   public static final ResourceKey<PoiType> SHEPHERD = createKey("shepherd");
   public static final ResourceKey<PoiType> TOOLSMITH = createKey("toolsmith");
   public static final ResourceKey<PoiType> WEAPONSMITH = createKey("weaponsmith");
   public static final ResourceKey<PoiType> HOME = createKey("home");
   public static final ResourceKey<PoiType> MEETING = createKey("meeting");
   public static final ResourceKey<PoiType> BEEHIVE = createKey("beehive");
   public static final ResourceKey<PoiType> BEE_NEST = createKey("bee_nest");
   public static final ResourceKey<PoiType> NETHER_PORTAL = createKey("nether_portal");
   public static final ResourceKey<PoiType> LODESTONE = createKey("lodestone");
   public static final ResourceKey<PoiType> LIGHTNING_ROD = createKey("lightning_rod");
   private static final Set<BlockState> BEDS = ImmutableList.of(Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED).stream().flatMap((p_218097_) -> {
      return p_218097_.getStateDefinition().getPossibleStates().stream();
   }).filter((p_218095_) -> {
      return p_218095_.getValue(BedBlock.PART) == BedPart.HEAD;
   }).collect(ImmutableSet.toImmutableSet());
   private static final Set<BlockState> CAULDRONS = ImmutableList.of(Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON).stream().flatMap((p_218093_) -> {
      return p_218093_.getStateDefinition().getPossibleStates().stream();
   }).collect(ImmutableSet.toImmutableSet());
   // Forge: We patch these 2 fields to support modded entries
   private static final Map<BlockState, PoiType> TYPE_BY_STATE = net.minecraftforge.registries.GameData.getBlockStatePointOfInterestTypeMap();
   protected static final Set<BlockState> f_218067_ = TYPE_BY_STATE.keySet();

   private static Set<BlockState> getBlockStates(Block pBlock) {
      return ImmutableSet.copyOf(pBlock.getStateDefinition().getPossibleStates());
   }

   private static ResourceKey<PoiType> createKey(String pName) {
      return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, new ResourceLocation(pName));
   }

   private static PoiType register(Registry<PoiType> pKey, ResourceKey<PoiType> pValue, Set<BlockState> pMatchingStates, int pMaxTickets, int pValidRange) {
      PoiType poitype = new PoiType(pMatchingStates, pMaxTickets, pValidRange);
      Registry.register(pKey, pValue, poitype);
      registerBlockStates(pKey.getHolderOrThrow(pValue), pMatchingStates);
      return poitype;
   }

   private static void registerBlockStates(Holder<PoiType> pPoi, Set<BlockState> pStates) {
   }

   public static Optional<Holder<PoiType>> forState(BlockState pState) {
      return Optional.ofNullable(TYPE_BY_STATE.get(pState)).flatMap(net.minecraftforge.registries.ForgeRegistries.POI_TYPES::getHolder);
   }

   public static boolean hasPoi(BlockState pState) {
      return TYPE_BY_STATE.containsKey(pState);
   }

   public static PoiType bootstrap(Registry<PoiType> pRegistry) {
      register(pRegistry, ARMORER, getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
      register(pRegistry, BUTCHER, getBlockStates(Blocks.SMOKER), 1, 1);
      register(pRegistry, CARTOGRAPHER, getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
      register(pRegistry, CLERIC, getBlockStates(Blocks.BREWING_STAND), 1, 1);
      register(pRegistry, FARMER, getBlockStates(Blocks.COMPOSTER), 1, 1);
      register(pRegistry, FISHERMAN, getBlockStates(Blocks.BARREL), 1, 1);
      register(pRegistry, FLETCHER, getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
      register(pRegistry, LEATHERWORKER, CAULDRONS, 1, 1);
      register(pRegistry, LIBRARIAN, getBlockStates(Blocks.LECTERN), 1, 1);
      register(pRegistry, MASON, getBlockStates(Blocks.STONECUTTER), 1, 1);
      register(pRegistry, SHEPHERD, getBlockStates(Blocks.LOOM), 1, 1);
      register(pRegistry, TOOLSMITH, getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
      register(pRegistry, WEAPONSMITH, getBlockStates(Blocks.GRINDSTONE), 1, 1);
      register(pRegistry, HOME, BEDS, 1, 1);
      register(pRegistry, MEETING, getBlockStates(Blocks.BELL), 32, 6);
      register(pRegistry, BEEHIVE, getBlockStates(Blocks.BEEHIVE), 0, 1);
      register(pRegistry, BEE_NEST, getBlockStates(Blocks.BEE_NEST), 0, 1);
      register(pRegistry, NETHER_PORTAL, getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
      register(pRegistry, LODESTONE, getBlockStates(Blocks.LODESTONE), 0, 1);
      return register(pRegistry, LIGHTNING_ROD, getBlockStates(Blocks.LIGHTNING_ROD), 0, 1);
   }
}
