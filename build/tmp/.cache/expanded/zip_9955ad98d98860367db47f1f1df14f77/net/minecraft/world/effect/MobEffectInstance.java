package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance>, net.minecraftforge.common.extensions.IForgeMobEffectInstance {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int INFINITE_DURATION = -1;
   private final MobEffect effect;
   private int duration;
   private int amplifier;
   private boolean ambient;
   private boolean visible;
   private boolean showIcon;
   /** A hidden effect which is not shown to the player. */
   @Nullable
   private MobEffectInstance hiddenEffect;
   private final Optional<MobEffectInstance.FactorData> factorData;

   public MobEffectInstance(MobEffect pEffect) {
      this(pEffect, 0, 0);
   }

   public MobEffectInstance(MobEffect pEffect, int pDuration) {
      this(pEffect, pDuration, 0);
   }

   public MobEffectInstance(MobEffect pEffect, int pDuration, int pAmplifier) {
      this(pEffect, pDuration, pAmplifier, false, true);
   }

   public MobEffectInstance(MobEffect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible) {
      this(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pVisible);
   }

   public MobEffectInstance(MobEffect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon) {
      this(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pShowIcon, (MobEffectInstance)null, pEffect.createFactorData());
   }

   public MobEffectInstance(MobEffect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon, @Nullable MobEffectInstance pHiddenEffect, Optional<MobEffectInstance.FactorData> pFactorData) {
      this.effect = pEffect;
      this.duration = pDuration;
      this.amplifier = pAmplifier;
      this.ambient = pAmbient;
      this.visible = pVisible;
      this.showIcon = pShowIcon;
      this.hiddenEffect = pHiddenEffect;
      this.factorData = pFactorData;
   }

   public MobEffectInstance(MobEffectInstance pOther) {
      this.effect = pOther.effect;
      this.factorData = this.effect.createFactorData();
      this.setDetailsFrom(pOther);
   }

   public Optional<MobEffectInstance.FactorData> getFactorData() {
      return this.factorData;
   }

   void setDetailsFrom(MobEffectInstance pEffectInstance) {
      this.duration = pEffectInstance.duration;
      this.amplifier = pEffectInstance.amplifier;
      this.ambient = pEffectInstance.ambient;
      this.visible = pEffectInstance.visible;
      this.showIcon = pEffectInstance.showIcon;
      this.curativeItems = pEffectInstance.curativeItems == null ? null : new java.util.ArrayList<net.minecraft.world.item.ItemStack>(pEffectInstance.curativeItems);
   }

   public boolean update(MobEffectInstance pOther) {
      if (this.effect != pOther.effect) {
         LOGGER.warn("This method should only be called for matching effects!");
      }

      int i = this.duration;
      boolean flag = false;
      if (pOther.amplifier > this.amplifier) {
         if (pOther.isShorterDurationThan(this)) {
            MobEffectInstance mobeffectinstance = this.hiddenEffect;
            this.hiddenEffect = new MobEffectInstance(this);
            this.hiddenEffect.hiddenEffect = mobeffectinstance;
         }

         this.amplifier = pOther.amplifier;
         this.duration = pOther.duration;
         flag = true;
      } else if (this.isShorterDurationThan(pOther)) {
         if (pOther.amplifier == this.amplifier) {
            this.duration = pOther.duration;
            flag = true;
         } else if (this.hiddenEffect == null) {
            this.hiddenEffect = new MobEffectInstance(pOther);
         } else {
            this.hiddenEffect.update(pOther);
         }
      }

      if (!pOther.ambient && this.ambient || flag) {
         this.ambient = pOther.ambient;
         flag = true;
      }

      if (pOther.visible != this.visible) {
         this.visible = pOther.visible;
         flag = true;
      }

      if (pOther.showIcon != this.showIcon) {
         this.showIcon = pOther.showIcon;
         flag = true;
      }

      return flag;
   }

   private boolean isShorterDurationThan(MobEffectInstance pOther) {
      return !this.isInfiniteDuration() && (this.duration < pOther.duration || pOther.isInfiniteDuration());
   }

   public boolean isInfiniteDuration() {
      return this.duration == -1;
   }

   public boolean endsWithin(int pDuration) {
      return !this.isInfiniteDuration() && this.duration <= pDuration;
   }

   public int mapDuration(Int2IntFunction pMapper) {
      return !this.isInfiniteDuration() && this.duration != 0 ? pMapper.applyAsInt(this.duration) : this.duration;
   }

   public MobEffect getEffect() {
      return this.effect == null ? null : net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getDelegateOrThrow(this.effect).get();
   }

   public int getDuration() {
      return this.duration;
   }

   public int getAmplifier() {
      return this.amplifier;
   }

   /**
    * Gets whether this potion effect originated from a beacon
    */
   public boolean isAmbient() {
      return this.ambient;
   }

   /**
    * Gets whether this potion effect will show ambient particles or not.
    */
   public boolean isVisible() {
      return this.visible;
   }

   public boolean showIcon() {
      return this.showIcon;
   }

   public boolean tick(LivingEntity pEntity, Runnable pOnExpirationRunnable) {
      if (this.hasRemainingDuration()) {
         int i = this.isInfiniteDuration() ? pEntity.tickCount : this.duration;
         if (this.effect.isDurationEffectTick(i, this.amplifier)) {
            this.applyEffect(pEntity);
         }

         this.tickDownDuration();
         if (this.duration == 0 && this.hiddenEffect != null) {
            this.setDetailsFrom(this.hiddenEffect);
            this.hiddenEffect = this.hiddenEffect.hiddenEffect;
            pOnExpirationRunnable.run();
         }
      }

      this.factorData.ifPresent((p_267917_) -> {
         p_267917_.tick(this);
      });
      return this.hasRemainingDuration();
   }

   private boolean hasRemainingDuration() {
      return this.isInfiniteDuration() || this.duration > 0;
   }

   private int tickDownDuration() {
      if (this.hiddenEffect != null) {
         this.hiddenEffect.tickDownDuration();
      }

      return this.duration = this.mapDuration((p_267916_) -> {
         return p_267916_ - 1;
      });
   }

   public void applyEffect(LivingEntity pEntity) {
      if (this.hasRemainingDuration()) {
         this.effect.applyEffectTick(pEntity, this.amplifier);
      }

   }

   public String getDescriptionId() {
      return this.effect.getDescriptionId();
   }

   public String toString() {
      String s;
      if (this.amplifier > 0) {
         s = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
      } else {
         s = this.getDescriptionId() + ", Duration: " + this.describeDuration();
      }

      if (!this.visible) {
         s = s + ", Particles: false";
      }

      if (!this.showIcon) {
         s = s + ", Show Icon: false";
      }

      return s;
   }

   private String describeDuration() {
      return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof MobEffectInstance)) {
         return false;
      } else {
         MobEffectInstance mobeffectinstance = (MobEffectInstance)pOther;
         return this.duration == mobeffectinstance.duration && this.amplifier == mobeffectinstance.amplifier && this.ambient == mobeffectinstance.ambient && this.effect.equals(mobeffectinstance.effect);
      }
   }

   public int hashCode() {
      int i = this.effect.hashCode();
      i = 31 * i + this.duration;
      i = 31 * i + this.amplifier;
      return 31 * i + (this.ambient ? 1 : 0);
   }

   /**
    * Write a custom potion effect to a potion item's NBT data.
    */
   public CompoundTag save(CompoundTag pNbt) {
      pNbt.putInt("Id", MobEffect.getId(this.getEffect()));
      net.minecraftforge.common.ForgeHooks.saveMobEffect(pNbt, "forge:id", this.getEffect());
      this.writeDetailsTo(pNbt);
      return pNbt;
   }

   private void writeDetailsTo(CompoundTag pNbt) {
      pNbt.putByte("Amplifier", (byte)this.getAmplifier());
      pNbt.putInt("Duration", this.getDuration());
      pNbt.putBoolean("Ambient", this.isAmbient());
      pNbt.putBoolean("ShowParticles", this.isVisible());
      pNbt.putBoolean("ShowIcon", this.showIcon());
      if (this.hiddenEffect != null) {
         CompoundTag compoundtag = new CompoundTag();
         this.hiddenEffect.save(compoundtag);
         pNbt.put("HiddenEffect", compoundtag);
      }
      writeCurativeItems(pNbt);

      this.factorData.ifPresent((p_216903_) -> {
         MobEffectInstance.FactorData.CODEC.encodeStart(NbtOps.INSTANCE, p_216903_).resultOrPartial(LOGGER::error).ifPresent((p_216906_) -> {
            pNbt.put("FactorCalculationData", p_216906_);
         });
      });
   }

   /**
    * Read a custom potion effect from a potion item's NBT data.
    */
   @Nullable
   public static MobEffectInstance load(CompoundTag pNbt) {
      int i = pNbt.getByte("Id") & 0xFF;
      MobEffect mobeffect = MobEffect.byId(i);
      mobeffect = net.minecraftforge.common.ForgeHooks.loadMobEffect(pNbt, "forge:id", mobeffect);
      return mobeffect == null ? null : loadSpecifiedEffect(mobeffect, pNbt);
   }

   private static MobEffectInstance loadSpecifiedEffect(MobEffect pEffect, CompoundTag pNbt) {
      int i = pNbt.getByte("Amplifier");
      int j = pNbt.getInt("Duration");
      boolean flag = pNbt.getBoolean("Ambient");
      boolean flag1 = true;
      if (pNbt.contains("ShowParticles", 1)) {
         flag1 = pNbt.getBoolean("ShowParticles");
      }

      boolean flag2 = flag1;
      if (pNbt.contains("ShowIcon", 1)) {
         flag2 = pNbt.getBoolean("ShowIcon");
      }

      MobEffectInstance mobeffectinstance = null;
      if (pNbt.contains("HiddenEffect", 10)) {
         mobeffectinstance = loadSpecifiedEffect(pEffect, pNbt.getCompound("HiddenEffect"));
      }

      Optional<MobEffectInstance.FactorData> optional;
      if (pNbt.contains("FactorCalculationData", 10)) {
         optional = MobEffectInstance.FactorData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pNbt.getCompound("FactorCalculationData"))).resultOrPartial(LOGGER::error);
      } else {
         optional = Optional.empty();
      }

      return readCurativeItems(new MobEffectInstance(pEffect, j, Math.max(0, i), flag, flag1, flag2, mobeffectinstance, optional), pNbt);
   }

   public int compareTo(MobEffectInstance pOther) {
      int i = 32147;
      return (this.getDuration() <= 32147 || pOther.getDuration() <= 32147) && (!this.isAmbient() || !pOther.isAmbient()) ? ComparisonChain.start().compare(this.isAmbient(), pOther.isAmbient()).compare(this.getDuration(), pOther.getDuration()).compare(this.getEffect().getSortOrder(this), pOther.getEffect().getSortOrder(this)).result() : ComparisonChain.start().compare(this.isAmbient(), pOther.isAmbient()).compare(this.getEffect().getSortOrder(this), pOther.getEffect().getSortOrder(this)).result();
   }

   //======================= FORGE START ===========================
   private java.util.List<net.minecraft.world.item.ItemStack> curativeItems;

   @Override
   public java.util.List<net.minecraft.world.item.ItemStack> getCurativeItems() {
      if (this.curativeItems == null) //Lazy load this so that we don't create a circular dep on Items.
         this.curativeItems = getEffect().getCurativeItems();
      return this.curativeItems;
   }
   @Override
   public void setCurativeItems(java.util.List<net.minecraft.world.item.ItemStack> curativeItems) {
      this.curativeItems = curativeItems;
   }
   private static MobEffectInstance readCurativeItems(MobEffectInstance effect, CompoundTag nbt) {
      if (nbt.contains("CurativeItems", net.minecraft.nbt.Tag.TAG_LIST)) {
         java.util.List<net.minecraft.world.item.ItemStack> items = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
         net.minecraft.nbt.ListTag list = nbt.getList("CurativeItems", net.minecraft.nbt.Tag.TAG_COMPOUND);
         for (int i = 0; i < list.size(); i++) {
            items.add(net.minecraft.world.item.ItemStack.of(list.getCompound(i)));
         }
         effect.setCurativeItems(items);
      }

      return effect;
   }

   public static class FactorData {
      public static final Codec<MobEffectInstance.FactorData> CODEC = RecordCodecBuilder.create((p_216933_) -> {
         return p_216933_.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("padding_duration").forGetter((p_216945_) -> {
            return p_216945_.paddingDuration;
         }), Codec.FLOAT.fieldOf("factor_start").orElse(0.0F).forGetter((p_216943_) -> {
            return p_216943_.factorStart;
         }), Codec.FLOAT.fieldOf("factor_target").orElse(1.0F).forGetter((p_216941_) -> {
            return p_216941_.factorTarget;
         }), Codec.FLOAT.fieldOf("factor_current").orElse(0.0F).forGetter((p_216939_) -> {
            return p_216939_.factorCurrent;
         }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_active").orElse(0).forGetter((p_267918_) -> {
            return p_267918_.ticksActive;
         }), Codec.FLOAT.fieldOf("factor_previous_frame").orElse(0.0F).forGetter((p_216935_) -> {
            return p_216935_.factorPreviousFrame;
         }), Codec.BOOL.fieldOf("had_effect_last_tick").orElse(false).forGetter((p_216929_) -> {
            return p_216929_.hadEffectLastTick;
         })).apply(p_216933_, MobEffectInstance.FactorData::new);
      });
      private final int paddingDuration;
      private float factorStart;
      private float factorTarget;
      private float factorCurrent;
      private int ticksActive;
      private float factorPreviousFrame;
      private boolean hadEffectLastTick;

      public FactorData(int p_216919_, float p_216920_, float p_216921_, float p_216922_, int p_216923_, float p_216924_, boolean p_216925_) {
         this.paddingDuration = p_216919_;
         this.factorStart = p_216920_;
         this.factorTarget = p_216921_;
         this.factorCurrent = p_216922_;
         this.ticksActive = p_216923_;
         this.factorPreviousFrame = p_216924_;
         this.hadEffectLastTick = p_216925_;
      }

      public FactorData(int pPaddingDuration) {
         this(pPaddingDuration, 0.0F, 1.0F, 0.0F, 0, 0.0F, false);
      }

      public void tick(MobEffectInstance pEffect) {
         this.factorPreviousFrame = this.factorCurrent;
         boolean flag = !pEffect.endsWithin(this.paddingDuration);
         ++this.ticksActive;
         if (this.hadEffectLastTick != flag) {
            this.hadEffectLastTick = flag;
            this.ticksActive = 0;
            this.factorStart = this.factorCurrent;
            this.factorTarget = flag ? 1.0F : 0.0F;
         }

         float f = Mth.clamp((float)this.ticksActive / (float)this.paddingDuration, 0.0F, 1.0F);
         this.factorCurrent = Mth.lerp(f, this.factorStart, this.factorTarget);
      }

      public float getFactor(LivingEntity pEntity, float pPartialTick) {
         if (pEntity.isRemoved()) {
            this.factorPreviousFrame = this.factorCurrent;
         }

         return Mth.lerp(pPartialTick, this.factorPreviousFrame, this.factorCurrent);
      }
   }
}
