package net.minecraft.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;

public class VillagerData {
   public static final int MIN_VILLAGER_LEVEL = 1;
   public static final int MAX_VILLAGER_LEVEL = 5;
   private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
   public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create((p_258961_) -> {
      return p_258961_.group(BuiltInRegistries.VILLAGER_TYPE.byNameCodec().fieldOf("type").orElseGet(() -> {
         return VillagerType.PLAINS;
      }).forGetter((p_150024_) -> {
         return p_150024_.type;
      }), BuiltInRegistries.VILLAGER_PROFESSION.byNameCodec().fieldOf("profession").orElseGet(() -> {
         return VillagerProfession.NONE;
      }).forGetter((p_150022_) -> {
         return p_150022_.profession;
      }), Codec.INT.fieldOf("level").orElse(1).forGetter((p_150020_) -> {
         return p_150020_.level;
      })).apply(p_258961_, VillagerData::new);
   });
   private final VillagerType type;
   private final VillagerProfession profession;
   private final int level;

   public VillagerData(VillagerType p_35557_, VillagerProfession p_35558_, int p_35559_) {
      this.type = p_35557_;
      this.profession = p_35558_;
      this.level = Math.max(1, p_35559_);
   }

   public VillagerType getType() {
      return this.type;
   }

   public VillagerProfession getProfession() {
      return this.profession;
   }

   public int getLevel() {
      return this.level;
   }

   public VillagerData setType(VillagerType pType) {
      return new VillagerData(pType, this.profession, this.level);
   }

   public VillagerData setProfession(VillagerProfession pProfession) {
      return new VillagerData(this.type, pProfession, this.level);
   }

   public VillagerData setLevel(int pLevel) {
      return new VillagerData(this.type, this.profession, pLevel);
   }

   public static int getMinXpPerLevel(int pLevel) {
      return canLevelUp(pLevel) ? NEXT_LEVEL_XP_THRESHOLDS[pLevel - 1] : 0;
   }

   public static int getMaxXpPerLevel(int pLevel) {
      return canLevelUp(pLevel) ? NEXT_LEVEL_XP_THRESHOLDS[pLevel] : 0;
   }

   public static boolean canLevelUp(int pLevel) {
      return pLevel >= 1 && pLevel < 5;
   }
}