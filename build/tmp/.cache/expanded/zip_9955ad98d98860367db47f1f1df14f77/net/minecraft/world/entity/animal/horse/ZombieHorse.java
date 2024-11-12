package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ZombieHorse extends AbstractHorse {
   public ZombieHorse(EntityType<? extends ZombieHorse> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0D).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
   }

   protected void randomizeAttributes(RandomSource pRandom) {
      this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength(pRandom::nextDouble));
   }

   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ZOMBIE_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_HORSE_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ZOMBIE_HORSE_HURT;
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return EntityType.ZOMBIE_HORSE.create(pLevel);
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      return !this.isTamed() ? InteractionResult.PASS : super.mobInteract(pPlayer, pHand);
   }

   protected void addBehaviourGoals() {
   }
}