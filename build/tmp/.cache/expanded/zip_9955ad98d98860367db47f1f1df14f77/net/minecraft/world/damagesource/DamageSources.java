package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class DamageSources {
   private final Registry<DamageType> damageTypes;
   private final DamageSource inFire;
   private final DamageSource lightningBolt;
   private final DamageSource onFire;
   private final DamageSource lava;
   private final DamageSource hotFloor;
   private final DamageSource inWall;
   private final DamageSource cramming;
   private final DamageSource drown;
   private final DamageSource starve;
   private final DamageSource cactus;
   private final DamageSource fall;
   private final DamageSource flyIntoWall;
   private final DamageSource fellOutOfWorld;
   private final DamageSource generic;
   private final DamageSource magic;
   private final DamageSource wither;
   private final DamageSource dragonBreath;
   private final DamageSource dryOut;
   private final DamageSource sweetBerryBush;
   private final DamageSource freeze;
   private final DamageSource stalagmite;
   private final DamageSource outsideBorder;
   private final DamageSource genericKill;

   public DamageSources(RegistryAccess pRegistry) {
      this.damageTypes = pRegistry.registryOrThrow(Registries.DAMAGE_TYPE);
      this.inFire = this.source(DamageTypes.IN_FIRE);
      this.lightningBolt = this.source(DamageTypes.LIGHTNING_BOLT);
      this.onFire = this.source(DamageTypes.ON_FIRE);
      this.lava = this.source(DamageTypes.LAVA);
      this.hotFloor = this.source(DamageTypes.HOT_FLOOR);
      this.inWall = this.source(DamageTypes.IN_WALL);
      this.cramming = this.source(DamageTypes.CRAMMING);
      this.drown = this.source(DamageTypes.DROWN);
      this.starve = this.source(DamageTypes.STARVE);
      this.cactus = this.source(DamageTypes.CACTUS);
      this.fall = this.source(DamageTypes.FALL);
      this.flyIntoWall = this.source(DamageTypes.FLY_INTO_WALL);
      this.fellOutOfWorld = this.source(DamageTypes.FELL_OUT_OF_WORLD);
      this.generic = this.source(DamageTypes.GENERIC);
      this.magic = this.source(DamageTypes.MAGIC);
      this.wither = this.source(DamageTypes.WITHER);
      this.dragonBreath = this.source(DamageTypes.DRAGON_BREATH);
      this.dryOut = this.source(DamageTypes.DRY_OUT);
      this.sweetBerryBush = this.source(DamageTypes.SWEET_BERRY_BUSH);
      this.freeze = this.source(DamageTypes.FREEZE);
      this.stalagmite = this.source(DamageTypes.STALAGMITE);
      this.outsideBorder = this.source(DamageTypes.OUTSIDE_BORDER);
      this.genericKill = this.source(DamageTypes.GENERIC_KILL);
   }

   private DamageSource source(ResourceKey<DamageType> pDamageTypeKey) {
      return new DamageSource(this.damageTypes.getHolderOrThrow(pDamageTypeKey));
   }

   private DamageSource source(ResourceKey<DamageType> pDamageTypeKey, @Nullable Entity pEntity) {
      return new DamageSource(this.damageTypes.getHolderOrThrow(pDamageTypeKey), pEntity);
   }

   private DamageSource source(ResourceKey<DamageType> pDamageTypeKey, @Nullable Entity pCausingEntity, @Nullable Entity pDirectEntity) {
      return new DamageSource(this.damageTypes.getHolderOrThrow(pDamageTypeKey), pCausingEntity, pDirectEntity);
   }

   public DamageSource inFire() {
      return this.inFire;
   }

   public DamageSource lightningBolt() {
      return this.lightningBolt;
   }

   public DamageSource onFire() {
      return this.onFire;
   }

   public DamageSource lava() {
      return this.lava;
   }

   public DamageSource hotFloor() {
      return this.hotFloor;
   }

   public DamageSource inWall() {
      return this.inWall;
   }

   public DamageSource cramming() {
      return this.cramming;
   }

   public DamageSource drown() {
      return this.drown;
   }

   public DamageSource starve() {
      return this.starve;
   }

   public DamageSource cactus() {
      return this.cactus;
   }

   public DamageSource fall() {
      return this.fall;
   }

   public DamageSource flyIntoWall() {
      return this.flyIntoWall;
   }

   public DamageSource fellOutOfWorld() {
      return this.fellOutOfWorld;
   }

   public DamageSource generic() {
      return this.generic;
   }

   public DamageSource magic() {
      return this.magic;
   }

   public DamageSource wither() {
      return this.wither;
   }

   public DamageSource dragonBreath() {
      return this.dragonBreath;
   }

   public DamageSource dryOut() {
      return this.dryOut;
   }

   public DamageSource sweetBerryBush() {
      return this.sweetBerryBush;
   }

   public DamageSource freeze() {
      return this.freeze;
   }

   public DamageSource stalagmite() {
      return this.stalagmite;
   }

   public DamageSource fallingBlock(Entity pEntity) {
      return this.source(DamageTypes.FALLING_BLOCK, pEntity);
   }

   public DamageSource anvil(Entity pEntity) {
      return this.source(DamageTypes.FALLING_ANVIL, pEntity);
   }

   public DamageSource fallingStalactite(Entity pEntity) {
      return this.source(DamageTypes.FALLING_STALACTITE, pEntity);
   }

   public DamageSource sting(LivingEntity pEntity) {
      return this.source(DamageTypes.STING, pEntity);
   }

   public DamageSource mobAttack(LivingEntity pMob) {
      return this.source(DamageTypes.MOB_ATTACK, pMob);
   }

   public DamageSource noAggroMobAttack(LivingEntity pMob) {
      return this.source(DamageTypes.MOB_ATTACK_NO_AGGRO, pMob);
   }

   public DamageSource playerAttack(Player pPlayer) {
      return this.source(DamageTypes.PLAYER_ATTACK, pPlayer);
   }

   public DamageSource arrow(AbstractArrow pArrow, @Nullable Entity pShooter) {
      return this.source(DamageTypes.ARROW, pArrow, pShooter);
   }

   public DamageSource trident(Entity pTrident, @Nullable Entity pThrower) {
      return this.source(DamageTypes.TRIDENT, pTrident, pThrower);
   }

   public DamageSource mobProjectile(Entity pProjectile, @Nullable LivingEntity pThrower) {
      return this.source(DamageTypes.MOB_PROJECTILE, pProjectile, pThrower);
   }

   public DamageSource fireworks(FireworkRocketEntity pFirework, @Nullable Entity pShooter) {
      return this.source(DamageTypes.FIREWORKS, pFirework, pShooter);
   }

   public DamageSource fireball(Fireball pFireball, @Nullable Entity pThrower) {
      return pThrower == null ? this.source(DamageTypes.UNATTRIBUTED_FIREBALL, pFireball) : this.source(DamageTypes.FIREBALL, pFireball, pThrower);
   }

   public DamageSource witherSkull(WitherSkull pWitherSkull, Entity pShooter) {
      return this.source(DamageTypes.WITHER_SKULL, pWitherSkull, pShooter);
   }

   public DamageSource thrown(Entity pCausingEntity, @Nullable Entity pDirectEntity) {
      return this.source(DamageTypes.THROWN, pCausingEntity, pDirectEntity);
   }

   public DamageSource indirectMagic(Entity pCausingEntity, @Nullable Entity pDirectEntity) {
      return this.source(DamageTypes.INDIRECT_MAGIC, pCausingEntity, pDirectEntity);
   }

   public DamageSource thorns(Entity pEntity) {
      return this.source(DamageTypes.THORNS, pEntity);
   }

   public DamageSource explosion(@Nullable Explosion pExplosion) {
      return pExplosion != null ? this.explosion(pExplosion.getDirectSourceEntity(), pExplosion.getIndirectSourceEntity()) : this.explosion((Entity)null, (Entity)null);
   }

   public DamageSource explosion(@Nullable Entity pCausingEntity, @Nullable Entity pDirectEntity) {
      return this.source(pDirectEntity != null && pCausingEntity != null ? DamageTypes.PLAYER_EXPLOSION : DamageTypes.EXPLOSION, pCausingEntity, pDirectEntity);
   }

   public DamageSource sonicBoom(Entity pEntity) {
      return this.source(DamageTypes.SONIC_BOOM, pEntity);
   }

   public DamageSource badRespawnPointExplosion(Vec3 pPosition) {
      return new DamageSource(this.damageTypes.getHolderOrThrow(DamageTypes.BAD_RESPAWN_POINT), pPosition);
   }

   public DamageSource outOfBorder() {
      return this.outsideBorder;
   }

   public DamageSource genericKill() {
      return this.genericKill;
   }
}