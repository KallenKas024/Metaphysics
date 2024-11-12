package net.minecraft.world.item.armortrim;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class ArmorTrim {
   public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create((p_267058_) -> {
      return p_267058_.group(TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)).apply(p_267058_, ArmorTrim::new);
   });
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String TAG_TRIM_ID = "Trim";
   private static final Component UPGRADE_TITLE = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.upgrade"))).withStyle(ChatFormatting.GRAY);
   private final Holder<TrimMaterial> material;
   private final Holder<TrimPattern> pattern;
   private final Function<ArmorMaterial, ResourceLocation> innerTexture;
   private final Function<ArmorMaterial, ResourceLocation> outerTexture;

   public ArmorTrim(Holder<TrimMaterial> p_267249_, Holder<TrimPattern> p_267212_) {
      this.material = p_267249_;
      this.pattern = p_267212_;
      this.innerTexture = Util.memoize((p_267934_) -> {
         ResourceLocation resourcelocation = p_267212_.value().assetId();
         String s = this.getColorPaletteSuffix(p_267934_);
         return resourcelocation.withPath((p_266737_) -> {
            return "trims/models/armor/" + p_266737_ + "_leggings_" + s;
         });
      });
      this.outerTexture = Util.memoize((p_267932_) -> {
         ResourceLocation resourcelocation = p_267212_.value().assetId();
         String s = this.getColorPaletteSuffix(p_267932_);
         return resourcelocation.withPath((p_266864_) -> {
            return "trims/models/armor/" + p_266864_ + "_" + s;
         });
      });
   }

   private String getColorPaletteSuffix(ArmorMaterial pArmorMaterial) {
      Map<ArmorMaterials, String> map = this.material.value().overrideArmorMaterials();
      return pArmorMaterial instanceof ArmorMaterials && map.containsKey(pArmorMaterial) ? map.get(pArmorMaterial) : this.material.value().assetName();
   }

   public boolean hasPatternAndMaterial(Holder<TrimPattern> pPattern, Holder<TrimMaterial> pMaterial) {
      return pPattern == this.pattern && pMaterial == this.material;
   }

   public Holder<TrimPattern> pattern() {
      return this.pattern;
   }

   public Holder<TrimMaterial> material() {
      return this.material;
   }

   public ResourceLocation innerTexture(ArmorMaterial pArmorMaterial) {
      return this.innerTexture.apply(pArmorMaterial);
   }

   public ResourceLocation outerTexture(ArmorMaterial pArmorMaterial) {
      return this.outerTexture.apply(pArmorMaterial);
   }

   public boolean equals(Object pOther) {
      if (!(pOther instanceof ArmorTrim armortrim)) {
         return false;
      } else {
         return armortrim.pattern == this.pattern && armortrim.material == this.material;
      }
   }

   public static boolean setTrim(RegistryAccess pRegistryAccess, ItemStack pArmor, ArmorTrim pTrim) {
      if (pArmor.is(ItemTags.TRIMMABLE_ARMOR)) {
         pArmor.getOrCreateTag().put("Trim", CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, pRegistryAccess), pTrim).result().orElseThrow());
         return true;
      } else {
         return false;
      }
   }

   public static Optional<ArmorTrim> getTrim(RegistryAccess pRegistryAccess, ItemStack pArmor) {
      if (pArmor.is(ItemTags.TRIMMABLE_ARMOR) && pArmor.getTag() != null && pArmor.getTag().contains("Trim")) {
         CompoundTag compoundtag = pArmor.getTagElement("Trim");
         ArmorTrim armortrim = CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, pRegistryAccess), compoundtag).resultOrPartial(LOGGER::error).orElse((ArmorTrim)null);
         return Optional.ofNullable(armortrim);
      } else {
         return Optional.empty();
      }
   }

   public static void appendUpgradeHoverText(ItemStack pArmor, RegistryAccess pRegistryAccess, List<Component> pTooltip) {
      Optional<ArmorTrim> optional = getTrim(pRegistryAccess, pArmor);
      if (optional.isPresent()) {
         ArmorTrim armortrim = optional.get();
         pTooltip.add(UPGRADE_TITLE);
         pTooltip.add(CommonComponents.space().append(armortrim.pattern().value().copyWithStyle(armortrim.material())));
         pTooltip.add(CommonComponents.space().append(armortrim.material().value().description()));
      }

   }
}