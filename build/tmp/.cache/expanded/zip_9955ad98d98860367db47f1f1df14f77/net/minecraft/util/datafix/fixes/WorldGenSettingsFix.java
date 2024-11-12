package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class WorldGenSettingsFix extends DataFix {
   private static final String VILLAGE = "minecraft:village";
   private static final String DESERT_PYRAMID = "minecraft:desert_pyramid";
   private static final String IGLOO = "minecraft:igloo";
   private static final String JUNGLE_TEMPLE = "minecraft:jungle_pyramid";
   private static final String SWAMP_HUT = "minecraft:swamp_hut";
   private static final String PILLAGER_OUTPOST = "minecraft:pillager_outpost";
   private static final String END_CITY = "minecraft:endcity";
   private static final String WOODLAND_MANSION = "minecraft:mansion";
   private static final String OCEAN_MONUMENT = "minecraft:monument";
   private static final ImmutableMap<String, WorldGenSettingsFix.StructureFeatureConfiguration> DEFAULTS = ImmutableMap.<String, WorldGenSettingsFix.StructureFeatureConfiguration>builder().put("minecraft:village", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 10387312)).put("minecraft:desert_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357617)).put("minecraft:igloo", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357618)).put("minecraft:jungle_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357619)).put("minecraft:swamp_hut", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357620)).put("minecraft:pillager_outpost", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 165745296)).put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 5, 10387313)).put("minecraft:endcity", new WorldGenSettingsFix.StructureFeatureConfiguration(20, 11, 10387313)).put("minecraft:mansion", new WorldGenSettingsFix.StructureFeatureConfiguration(80, 20, 10387319)).build();

   public WorldGenSettingsFix(Schema pOutputSchema) {
      super(pOutputSchema, true);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("WorldGenSettings building", this.getInputSchema().getType(References.WORLD_GEN_SETTINGS), (p_17184_) -> {
         return p_17184_.update(DSL.remainderFinder(), WorldGenSettingsFix::fix);
      });
   }

   private static <T> Dynamic<T> noise(long pSeed, DynamicLike<T> pDynamic, Dynamic<T> pSettings, Dynamic<T> pBiomeNoise) {
      return pDynamic.createMap(ImmutableMap.of(pDynamic.createString("type"), pDynamic.createString("minecraft:noise"), pDynamic.createString("biome_source"), pBiomeNoise, pDynamic.createString("seed"), pDynamic.createLong(pSeed), pDynamic.createString("settings"), pSettings));
   }

   private static <T> Dynamic<T> vanillaBiomeSource(Dynamic<T> pDynamic, long pSeed, boolean pLegacyBiomeInitLayer, boolean pLargeBiomes) {
      ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.<Dynamic<T>, Dynamic<T>>builder().put(pDynamic.createString("type"), pDynamic.createString("minecraft:vanilla_layered")).put(pDynamic.createString("seed"), pDynamic.createLong(pSeed)).put(pDynamic.createString("large_biomes"), pDynamic.createBoolean(pLargeBiomes));
      if (pLegacyBiomeInitLayer) {
         builder.put(pDynamic.createString("legacy_biome_init_layer"), pDynamic.createBoolean(pLegacyBiomeInitLayer));
      }

      return pDynamic.createMap(builder.build());
   }

   private static <T> Dynamic<T> fix(Dynamic<T> p_17186_) {
      DynamicOps<T> dynamicops = p_17186_.getOps();
      long i = p_17186_.get("RandomSeed").asLong(0L);
      Optional<String> optional = p_17186_.get("generatorName").asString().map((p_17227_) -> {
         return p_17227_.toLowerCase(Locale.ROOT);
      }).result();
      Optional<String> optional1 = p_17186_.get("legacy_custom_options").asString().result().map(Optional::of).orElseGet(() -> {
         return optional.equals(Optional.of("customized")) ? p_17186_.get("generatorOptions").asString().result() : Optional.empty();
      });
      boolean flag = false;
      Dynamic<T> dynamic;
      if (optional.equals(Optional.of("customized"))) {
         dynamic = defaultOverworld(p_17186_, i);
      } else if (!optional.isPresent()) {
         dynamic = defaultOverworld(p_17186_, i);
      } else {
         switch ((String)optional.get()) {
            case "flat":
               OptionalDynamic<T> optionaldynamic = p_17186_.get("generatorOptions");
               Map<Dynamic<T>, Dynamic<T>> map = fixFlatStructures(dynamicops, optionaldynamic);
               dynamic = p_17186_.createMap(ImmutableMap.of(p_17186_.createString("type"), p_17186_.createString("minecraft:flat"), p_17186_.createString("settings"), p_17186_.createMap(ImmutableMap.of(p_17186_.createString("structures"), p_17186_.createMap(map), p_17186_.createString("layers"), optionaldynamic.get("layers").result().orElseGet(() -> {
                  return p_17186_.createList(Stream.of(p_17186_.createMap(ImmutableMap.of(p_17186_.createString("height"), p_17186_.createInt(1), p_17186_.createString("block"), p_17186_.createString("minecraft:bedrock"))), p_17186_.createMap(ImmutableMap.of(p_17186_.createString("height"), p_17186_.createInt(2), p_17186_.createString("block"), p_17186_.createString("minecraft:dirt"))), p_17186_.createMap(ImmutableMap.of(p_17186_.createString("height"), p_17186_.createInt(1), p_17186_.createString("block"), p_17186_.createString("minecraft:grass_block")))));
               }), p_17186_.createString("biome"), p_17186_.createString(optionaldynamic.get("biome").asString("minecraft:plains"))))));
               break;
            case "debug_all_block_states":
               dynamic = p_17186_.createMap(ImmutableMap.of(p_17186_.createString("type"), p_17186_.createString("minecraft:debug")));
               break;
            case "buffet":
               OptionalDynamic<T> optionaldynamic1 = p_17186_.get("generatorOptions");
               OptionalDynamic<?> optionaldynamic2 = optionaldynamic1.get("chunk_generator");
               Optional<String> optional2 = optionaldynamic2.get("type").asString().result();
               Dynamic<T> dynamic1;
               if (Objects.equals(optional2, Optional.of("minecraft:caves"))) {
                  dynamic1 = p_17186_.createString("minecraft:caves");
                  flag = true;
               } else if (Objects.equals(optional2, Optional.of("minecraft:floating_islands"))) {
                  dynamic1 = p_17186_.createString("minecraft:floating_islands");
               } else {
                  dynamic1 = p_17186_.createString("minecraft:overworld");
               }

               Dynamic<T> dynamic2 = optionaldynamic1.get("biome_source").result().orElseGet(() -> {
                  return p_17186_.createMap(ImmutableMap.of(p_17186_.createString("type"), p_17186_.createString("minecraft:fixed")));
               });
               Dynamic<T> dynamic3;
               if (dynamic2.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
                  String s1 = dynamic2.get("options").get("biomes").asStream().findFirst().flatMap((p_17259_) -> {
                     return p_17259_.asString().result();
                  }).orElse("minecraft:ocean");
                  dynamic3 = dynamic2.remove("options").set("biome", p_17186_.createString(s1));
               } else {
                  dynamic3 = dynamic2;
               }

               dynamic = noise(i, p_17186_, dynamic1, dynamic3);
               break;
            default:
               boolean flag6 = optional.get().equals("default");
               boolean flag1 = optional.get().equals("default_1_1") || flag6 && p_17186_.get("generatorVersion").asInt(0) == 0;
               boolean flag2 = optional.get().equals("amplified");
               boolean flag3 = optional.get().equals("largebiomes");
               dynamic = noise(i, p_17186_, p_17186_.createString(flag2 ? "minecraft:amplified" : "minecraft:overworld"), vanillaBiomeSource(p_17186_, i, flag1, flag3));
         }
      }

      boolean flag4 = p_17186_.get("MapFeatures").asBoolean(true);
      boolean flag5 = p_17186_.get("BonusChest").asBoolean(false);
      ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
      builder.put(dynamicops.createString("seed"), dynamicops.createLong(i));
      builder.put(dynamicops.createString("generate_features"), dynamicops.createBoolean(flag4));
      builder.put(dynamicops.createString("bonus_chest"), dynamicops.createBoolean(flag5));
      builder.put(dynamicops.createString("dimensions"), vanillaLevels(p_17186_, i, dynamic, flag));
      optional1.ifPresent((p_17182_) -> {
         builder.put(dynamicops.createString("legacy_custom_options"), dynamicops.createString(p_17182_));
      });
      return new Dynamic<>(dynamicops, dynamicops.createMap(builder.build()));
   }

   protected static <T> Dynamic<T> defaultOverworld(Dynamic<T> pDynamic, long pSeed) {
      return noise(pSeed, pDynamic, pDynamic.createString("minecraft:overworld"), vanillaBiomeSource(pDynamic, pSeed, false, false));
   }

   protected static <T> T vanillaLevels(Dynamic<T> pDynamic, long pSeed, Dynamic<T> p_17193_, boolean pCaves) {
      DynamicOps<T> dynamicops = pDynamic.getOps();
      return dynamicops.createMap(ImmutableMap.of(dynamicops.createString("minecraft:overworld"), dynamicops.createMap(ImmutableMap.of(dynamicops.createString("type"), dynamicops.createString("minecraft:overworld" + (pCaves ? "_caves" : "")), dynamicops.createString("generator"), p_17193_.getValue())), dynamicops.createString("minecraft:the_nether"), dynamicops.createMap(ImmutableMap.of(dynamicops.createString("type"), dynamicops.createString("minecraft:the_nether"), dynamicops.createString("generator"), noise(pSeed, pDynamic, pDynamic.createString("minecraft:nether"), pDynamic.createMap(ImmutableMap.of(pDynamic.createString("type"), pDynamic.createString("minecraft:multi_noise"), pDynamic.createString("seed"), pDynamic.createLong(pSeed), pDynamic.createString("preset"), pDynamic.createString("minecraft:nether")))).getValue())), dynamicops.createString("minecraft:the_end"), dynamicops.createMap(ImmutableMap.of(dynamicops.createString("type"), dynamicops.createString("minecraft:the_end"), dynamicops.createString("generator"), noise(pSeed, pDynamic, pDynamic.createString("minecraft:end"), pDynamic.createMap(ImmutableMap.of(pDynamic.createString("type"), pDynamic.createString("minecraft:the_end"), pDynamic.createString("seed"), pDynamic.createLong(pSeed)))).getValue()))));
   }

   private static <T> Map<Dynamic<T>, Dynamic<T>> fixFlatStructures(DynamicOps<T> pOps, OptionalDynamic<T> p_17219_) {
      MutableInt mutableint = new MutableInt(32);
      MutableInt mutableint1 = new MutableInt(3);
      MutableInt mutableint2 = new MutableInt(128);
      MutableBoolean mutableboolean = new MutableBoolean(false);
      Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> map = Maps.newHashMap();
      if (!p_17219_.result().isPresent()) {
         mutableboolean.setTrue();
         map.put("minecraft:village", DEFAULTS.get("minecraft:village"));
      }

      p_17219_.get("structures").flatMap(Dynamic::getMapValues).result().ifPresent((p_17257_) -> {
         p_17257_.forEach((p_145823_, p_145824_) -> {
            p_145824_.getMapValues().result().ifPresent((p_145816_) -> {
               p_145816_.forEach((p_145807_, p_145808_) -> {
                  String s = p_145823_.asString("");
                  String s1 = p_145807_.asString("");
                  String s2 = p_145808_.asString("");
                  if ("stronghold".equals(s)) {
                     mutableboolean.setTrue();
                     switch (s1) {
                        case "distance":
                           mutableint.setValue(getInt(s2, mutableint.getValue(), 1));
                           return;
                        case "spread":
                           mutableint1.setValue(getInt(s2, mutableint1.getValue(), 1));
                           return;
                        case "count":
                           mutableint2.setValue(getInt(s2, mutableint2.getValue(), 1));
                           return;
                        default:
                     }
                  } else {
                     switch (s1) {
                        case "distance":
                           switch (s) {
                              case "village":
                                 setSpacing(map, "minecraft:village", s2, 9);
                                 return;
                              case "biome_1":
                                 setSpacing(map, "minecraft:desert_pyramid", s2, 9);
                                 setSpacing(map, "minecraft:igloo", s2, 9);
                                 setSpacing(map, "minecraft:jungle_pyramid", s2, 9);
                                 setSpacing(map, "minecraft:swamp_hut", s2, 9);
                                 setSpacing(map, "minecraft:pillager_outpost", s2, 9);
                                 return;
                              case "endcity":
                                 setSpacing(map, "minecraft:endcity", s2, 1);
                                 return;
                              case "mansion":
                                 setSpacing(map, "minecraft:mansion", s2, 1);
                                 return;
                              default:
                                 return;
                           }
                        case "separation":
                           if ("oceanmonument".equals(s)) {
                              WorldGenSettingsFix.StructureFeatureConfiguration worldgensettingsfix$structurefeatureconfiguration = map.getOrDefault("minecraft:monument", DEFAULTS.get("minecraft:monument"));
                              int i = getInt(s2, worldgensettingsfix$structurefeatureconfiguration.separation, 1);
                              map.put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(i, worldgensettingsfix$structurefeatureconfiguration.separation, worldgensettingsfix$structurefeatureconfiguration.salt));
                           }

                           return;
                        case "spacing":
                           if ("oceanmonument".equals(s)) {
                              setSpacing(map, "minecraft:monument", s2, 1);
                           }

                           return;
                        default:
                     }
                  }
               });
            });
         });
      });
      ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
      builder.put(p_17219_.createString("structures"), p_17219_.createMap(map.entrySet().stream().collect(Collectors.toMap((p_17225_) -> {
         return p_17219_.createString(p_17225_.getKey());
      }, (p_17222_) -> {
         return p_17222_.getValue().serialize(pOps);
      }))));
      if (mutableboolean.isTrue()) {
         builder.put(p_17219_.createString("stronghold"), p_17219_.createMap(ImmutableMap.of(p_17219_.createString("distance"), p_17219_.createInt(mutableint.getValue()), p_17219_.createString("spread"), p_17219_.createInt(mutableint1.getValue()), p_17219_.createString("count"), p_17219_.createInt(mutableint2.getValue()))));
      }

      return builder.build();
   }

   private static int getInt(String pString, int pDefaultValue) {
      return NumberUtils.toInt(pString, pDefaultValue);
   }

   private static int getInt(String pString, int pDefaultValue, int pMinValue) {
      return Math.max(pMinValue, getInt(pString, pDefaultValue));
   }

   private static void setSpacing(Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> p_17236_, String p_17237_, String pSpacing, int pMinSpacing) {
      WorldGenSettingsFix.StructureFeatureConfiguration worldgensettingsfix$structurefeatureconfiguration = p_17236_.getOrDefault(p_17237_, DEFAULTS.get(p_17237_));
      int i = getInt(pSpacing, worldgensettingsfix$structurefeatureconfiguration.spacing, pMinSpacing);
      p_17236_.put(p_17237_, new WorldGenSettingsFix.StructureFeatureConfiguration(i, worldgensettingsfix$structurefeatureconfiguration.separation, worldgensettingsfix$structurefeatureconfiguration.salt));
   }

   static final class StructureFeatureConfiguration {
      public static final Codec<WorldGenSettingsFix.StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create((p_17279_) -> {
         return p_17279_.group(Codec.INT.fieldOf("spacing").forGetter((p_145830_) -> {
            return p_145830_.spacing;
         }), Codec.INT.fieldOf("separation").forGetter((p_145828_) -> {
            return p_145828_.separation;
         }), Codec.INT.fieldOf("salt").forGetter((p_145826_) -> {
            return p_145826_.salt;
         })).apply(p_17279_, WorldGenSettingsFix.StructureFeatureConfiguration::new);
      });
      final int spacing;
      final int separation;
      final int salt;

      public StructureFeatureConfiguration(int p_17271_, int p_17272_, int p_17273_) {
         this.spacing = p_17271_;
         this.separation = p_17272_;
         this.salt = p_17273_;
      }

      public <T> Dynamic<T> serialize(DynamicOps<T> pOps) {
         return new Dynamic<>(pOps, CODEC.encodeStart(pOps, this).result().orElse(pOps.emptyMap()));
      }
   }
}