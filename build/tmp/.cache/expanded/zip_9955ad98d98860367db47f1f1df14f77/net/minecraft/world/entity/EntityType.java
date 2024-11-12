package net.minecraft.world.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class EntityType<T extends Entity> implements FeatureElement, EntityTypeTest<Entity, T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String ENTITY_TAG = "EntityTag";
   private final Holder.Reference<EntityType<?>> builtInRegistryHolder = BuiltInRegistries.ENTITY_TYPE.createIntrusiveHolder(this);
   private static final float MAGIC_HORSE_WIDTH = 1.3964844F;
   private static final int DISPLAY_TRACKING_RANGE = 10;
   public static final EntityType<Allay> ALLAY = register("allay", EntityType.Builder.of(Allay::new, MobCategory.CREATURE).sized(0.35F, 0.6F).clientTrackingRange(8).updateInterval(2));
   public static final EntityType<AreaEffectCloud> AREA_EFFECT_CLOUD = register("area_effect_cloud", EntityType.Builder.<AreaEffectCloud>of(AreaEffectCloud::new, MobCategory.MISC).fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<ArmorStand> ARMOR_STAND = register("armor_stand", EntityType.Builder.<ArmorStand>of(ArmorStand::new, MobCategory.MISC).sized(0.5F, 1.975F).clientTrackingRange(10));
   public static final EntityType<Arrow> ARROW = register("arrow", EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));
   public static final EntityType<Axolotl> AXOLOTL = register("axolotl", EntityType.Builder.of(Axolotl::new, MobCategory.AXOLOTLS).sized(0.75F, 0.42F).clientTrackingRange(10));
   public static final EntityType<Bat> BAT = register("bat", EntityType.Builder.of(Bat::new, MobCategory.AMBIENT).sized(0.5F, 0.9F).clientTrackingRange(5));
   public static final EntityType<Bee> BEE = register("bee", EntityType.Builder.of(Bee::new, MobCategory.CREATURE).sized(0.7F, 0.6F).clientTrackingRange(8));
   public static final EntityType<Blaze> BLAZE = register("blaze", EntityType.Builder.of(Blaze::new, MobCategory.MONSTER).fireImmune().sized(0.6F, 1.8F).clientTrackingRange(8));
   public static final EntityType<Display.BlockDisplay> BLOCK_DISPLAY = register("block_display", EntityType.Builder.of(Display.BlockDisplay::new, MobCategory.MISC).sized(0.0F, 0.0F).clientTrackingRange(10).updateInterval(1));
   public static final EntityType<Boat> BOAT = register("boat", EntityType.Builder.<Boat>of(Boat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));
   public static final EntityType<Camel> CAMEL = register("camel", EntityType.Builder.of(Camel::new, MobCategory.CREATURE).sized(1.7F, 2.375F).clientTrackingRange(10));
   public static final EntityType<Cat> CAT = register("cat", EntityType.Builder.of(Cat::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(8));
   public static final EntityType<CaveSpider> CAVE_SPIDER = register("cave_spider", EntityType.Builder.of(CaveSpider::new, MobCategory.MONSTER).sized(0.7F, 0.5F).clientTrackingRange(8));
   public static final EntityType<ChestBoat> CHEST_BOAT = register("chest_boat", EntityType.Builder.<ChestBoat>of(ChestBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));
   public static final EntityType<MinecartChest> CHEST_MINECART = register("chest_minecart", EntityType.Builder.<MinecartChest>of(MinecartChest::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<Chicken> CHICKEN = register("chicken", EntityType.Builder.of(Chicken::new, MobCategory.CREATURE).sized(0.4F, 0.7F).clientTrackingRange(10));
   public static final EntityType<Cod> COD = register("cod", EntityType.Builder.of(Cod::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).clientTrackingRange(4));
   public static final EntityType<MinecartCommandBlock> COMMAND_BLOCK_MINECART = register("command_block_minecart", EntityType.Builder.<MinecartCommandBlock>of(MinecartCommandBlock::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<Cow> COW = register("cow", EntityType.Builder.of(Cow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).clientTrackingRange(10));
   public static final EntityType<Creeper> CREEPER = register("creeper", EntityType.Builder.of(Creeper::new, MobCategory.MONSTER).sized(0.6F, 1.7F).clientTrackingRange(8));
   public static final EntityType<Dolphin> DOLPHIN = register("dolphin", EntityType.Builder.of(Dolphin::new, MobCategory.WATER_CREATURE).sized(0.9F, 0.6F));
   public static final EntityType<Donkey> DONKEY = register("donkey", EntityType.Builder.of(Donkey::new, MobCategory.CREATURE).sized(1.3964844F, 1.5F).clientTrackingRange(10));
   public static final EntityType<DragonFireball> DRAGON_FIREBALL = register("dragon_fireball", EntityType.Builder.<DragonFireball>of(DragonFireball::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<Drowned> DROWNED = register("drowned", EntityType.Builder.of(Drowned::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<ThrownEgg> EGG = register("egg", EntityType.Builder.<ThrownEgg>of(ThrownEgg::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<ElderGuardian> ELDER_GUARDIAN = register("elder_guardian", EntityType.Builder.of(ElderGuardian::new, MobCategory.MONSTER).sized(1.9975F, 1.9975F).clientTrackingRange(10));
   public static final EntityType<EndCrystal> END_CRYSTAL = register("end_crystal", EntityType.Builder.<EndCrystal>of(EndCrystal::new, MobCategory.MISC).sized(2.0F, 2.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<EnderDragon> ENDER_DRAGON = register("ender_dragon", EntityType.Builder.of(EnderDragon::new, MobCategory.MONSTER).fireImmune().sized(16.0F, 8.0F).clientTrackingRange(10));
   public static final EntityType<ThrownEnderpearl> ENDER_PEARL = register("ender_pearl", EntityType.Builder.<ThrownEnderpearl>of(ThrownEnderpearl::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<EnderMan> ENDERMAN = register("enderman", EntityType.Builder.of(EnderMan::new, MobCategory.MONSTER).sized(0.6F, 2.9F).clientTrackingRange(8));
   public static final EntityType<Endermite> ENDERMITE = register("endermite", EntityType.Builder.of(Endermite::new, MobCategory.MONSTER).sized(0.4F, 0.3F).clientTrackingRange(8));
   public static final EntityType<Evoker> EVOKER = register("evoker", EntityType.Builder.of(Evoker::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<EvokerFangs> EVOKER_FANGS = register("evoker_fangs", EntityType.Builder.<EvokerFangs>of(EvokerFangs::new, MobCategory.MISC).sized(0.5F, 0.8F).clientTrackingRange(6).updateInterval(2));
   public static final EntityType<ThrownExperienceBottle> EXPERIENCE_BOTTLE = register("experience_bottle", EntityType.Builder.<ThrownExperienceBottle>of(ThrownExperienceBottle::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<ExperienceOrb> EXPERIENCE_ORB = register("experience_orb", EntityType.Builder.<ExperienceOrb>of(ExperienceOrb::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(20));
   public static final EntityType<EyeOfEnder> EYE_OF_ENDER = register("eye_of_ender", EntityType.Builder.<EyeOfEnder>of(EyeOfEnder::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(4));
   public static final EntityType<FallingBlockEntity> FALLING_BLOCK = register("falling_block", EntityType.Builder.of(FallingBlockEntity::new, MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20));
   public static final EntityType<FireworkRocketEntity> FIREWORK_ROCKET = register("firework_rocket", EntityType.Builder.<FireworkRocketEntity>of(FireworkRocketEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<Fox> FOX = register("fox", EntityType.Builder.of(Fox::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(8).immuneTo(Blocks.SWEET_BERRY_BUSH));
   public static final EntityType<Frog> FROG = register("frog", EntityType.Builder.of(Frog::new, MobCategory.CREATURE).sized(0.5F, 0.5F).clientTrackingRange(10));
   public static final EntityType<MinecartFurnace> FURNACE_MINECART = register("furnace_minecart", EntityType.Builder.<MinecartFurnace>of(MinecartFurnace::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<Ghast> GHAST = register("ghast", EntityType.Builder.of(Ghast::new, MobCategory.MONSTER).fireImmune().sized(4.0F, 4.0F).clientTrackingRange(10));
   public static final EntityType<Giant> GIANT = register("giant", EntityType.Builder.of(Giant::new, MobCategory.MONSTER).sized(3.6F, 12.0F).clientTrackingRange(10));
   public static final EntityType<GlowItemFrame> GLOW_ITEM_FRAME = register("glow_item_frame", EntityType.Builder.<GlowItemFrame>of(GlowItemFrame::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<GlowSquid> GLOW_SQUID = register("glow_squid", EntityType.Builder.of(GlowSquid::new, MobCategory.UNDERGROUND_WATER_CREATURE).sized(0.8F, 0.8F).clientTrackingRange(10));
   public static final EntityType<Goat> GOAT = register("goat", EntityType.Builder.of(Goat::new, MobCategory.CREATURE).sized(0.9F, 1.3F).clientTrackingRange(10));
   public static final EntityType<Guardian> GUARDIAN = register("guardian", EntityType.Builder.of(Guardian::new, MobCategory.MONSTER).sized(0.85F, 0.85F).clientTrackingRange(8));
   public static final EntityType<Hoglin> HOGLIN = register("hoglin", EntityType.Builder.of(Hoglin::new, MobCategory.MONSTER).sized(1.3964844F, 1.4F).clientTrackingRange(8));
   public static final EntityType<MinecartHopper> HOPPER_MINECART = register("hopper_minecart", EntityType.Builder.<MinecartHopper>of(MinecartHopper::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<Horse> HORSE = register("horse", EntityType.Builder.of(Horse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).clientTrackingRange(10));
   public static final EntityType<Husk> HUSK = register("husk", EntityType.Builder.of(Husk::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<Illusioner> ILLUSIONER = register("illusioner", EntityType.Builder.of(Illusioner::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<Interaction> INTERACTION = register("interaction", EntityType.Builder.of(Interaction::new, MobCategory.MISC).sized(0.0F, 0.0F).clientTrackingRange(10));
   public static final EntityType<IronGolem> IRON_GOLEM = register("iron_golem", EntityType.Builder.of(IronGolem::new, MobCategory.MISC).sized(1.4F, 2.7F).clientTrackingRange(10));
   public static final EntityType<ItemEntity> ITEM = register("item", EntityType.Builder.<ItemEntity>of(ItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(20));
   public static final EntityType<Display.ItemDisplay> ITEM_DISPLAY = register("item_display", EntityType.Builder.of(Display.ItemDisplay::new, MobCategory.MISC).sized(0.0F, 0.0F).clientTrackingRange(10).updateInterval(1));
   public static final EntityType<ItemFrame> ITEM_FRAME = register("item_frame", EntityType.Builder.<ItemFrame>of(ItemFrame::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<LargeFireball> FIREBALL = register("fireball", EntityType.Builder.<LargeFireball>of(LargeFireball::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<LeashFenceKnotEntity> LEASH_KNOT = register("leash_knot", EntityType.Builder.<LeashFenceKnotEntity>of(LeashFenceKnotEntity::new, MobCategory.MISC).noSave().sized(0.375F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<LightningBolt> LIGHTNING_BOLT = register("lightning_bolt", EntityType.Builder.of(LightningBolt::new, MobCategory.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<Llama> LLAMA = register("llama", EntityType.Builder.of(Llama::new, MobCategory.CREATURE).sized(0.9F, 1.87F).clientTrackingRange(10));
   public static final EntityType<LlamaSpit> LLAMA_SPIT = register("llama_spit", EntityType.Builder.<LlamaSpit>of(LlamaSpit::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<MagmaCube> MAGMA_CUBE = register("magma_cube", EntityType.Builder.of(MagmaCube::new, MobCategory.MONSTER).fireImmune().sized(2.04F, 2.04F).clientTrackingRange(8));
   public static final EntityType<Marker> MARKER = register("marker", EntityType.Builder.of(Marker::new, MobCategory.MISC).sized(0.0F, 0.0F).clientTrackingRange(0));
   public static final EntityType<Minecart> MINECART = register("minecart", EntityType.Builder.<Minecart>of(Minecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<MushroomCow> MOOSHROOM = register("mooshroom", EntityType.Builder.of(MushroomCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).clientTrackingRange(10));
   public static final EntityType<Mule> MULE = register("mule", EntityType.Builder.of(Mule::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).clientTrackingRange(8));
   public static final EntityType<Ocelot> OCELOT = register("ocelot", EntityType.Builder.of(Ocelot::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(10));
   public static final EntityType<Painting> PAINTING = register("painting", EntityType.Builder.<Painting>of(Painting::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
   public static final EntityType<Panda> PANDA = register("panda", EntityType.Builder.of(Panda::new, MobCategory.CREATURE).sized(1.3F, 1.25F).clientTrackingRange(10));
   public static final EntityType<Parrot> PARROT = register("parrot", EntityType.Builder.of(Parrot::new, MobCategory.CREATURE).sized(0.5F, 0.9F).clientTrackingRange(8));
   public static final EntityType<Phantom> PHANTOM = register("phantom", EntityType.Builder.of(Phantom::new, MobCategory.MONSTER).sized(0.9F, 0.5F).clientTrackingRange(8));
   public static final EntityType<Pig> PIG = register("pig", EntityType.Builder.of(Pig::new, MobCategory.CREATURE).sized(0.9F, 0.9F).clientTrackingRange(10));
   public static final EntityType<Piglin> PIGLIN = register("piglin", EntityType.Builder.of(Piglin::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<PiglinBrute> PIGLIN_BRUTE = register("piglin_brute", EntityType.Builder.of(PiglinBrute::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<Pillager> PILLAGER = register("pillager", EntityType.Builder.of(Pillager::new, MobCategory.MONSTER).canSpawnFarFromPlayer().sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<PolarBear> POLAR_BEAR = register("polar_bear", EntityType.Builder.of(PolarBear::new, MobCategory.CREATURE).immuneTo(Blocks.POWDER_SNOW).sized(1.4F, 1.4F).clientTrackingRange(10));
   public static final EntityType<ThrownPotion> POTION = register("potion", EntityType.Builder.<ThrownPotion>of(ThrownPotion::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<Pufferfish> PUFFERFISH = register("pufferfish", EntityType.Builder.of(Pufferfish::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.7F).clientTrackingRange(4));
   public static final EntityType<Rabbit> RABBIT = register("rabbit", EntityType.Builder.of(Rabbit::new, MobCategory.CREATURE).sized(0.4F, 0.5F).clientTrackingRange(8));
   public static final EntityType<Ravager> RAVAGER = register("ravager", EntityType.Builder.of(Ravager::new, MobCategory.MONSTER).sized(1.95F, 2.2F).clientTrackingRange(10));
   public static final EntityType<Salmon> SALMON = register("salmon", EntityType.Builder.of(Salmon::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.4F).clientTrackingRange(4));
   public static final EntityType<Sheep> SHEEP = register("sheep", EntityType.Builder.of(Sheep::new, MobCategory.CREATURE).sized(0.9F, 1.3F).clientTrackingRange(10));
   public static final EntityType<Shulker> SHULKER = register("shulker", EntityType.Builder.of(Shulker::new, MobCategory.MONSTER).fireImmune().canSpawnFarFromPlayer().sized(1.0F, 1.0F).clientTrackingRange(10));
   public static final EntityType<ShulkerBullet> SHULKER_BULLET = register("shulker_bullet", EntityType.Builder.<ShulkerBullet>of(ShulkerBullet::new, MobCategory.MISC).sized(0.3125F, 0.3125F).clientTrackingRange(8));
   public static final EntityType<Silverfish> SILVERFISH = register("silverfish", EntityType.Builder.of(Silverfish::new, MobCategory.MONSTER).sized(0.4F, 0.3F).clientTrackingRange(8));
   public static final EntityType<Skeleton> SKELETON = register("skeleton", EntityType.Builder.of(Skeleton::new, MobCategory.MONSTER).sized(0.6F, 1.99F).clientTrackingRange(8));
   public static final EntityType<SkeletonHorse> SKELETON_HORSE = register("skeleton_horse", EntityType.Builder.of(SkeletonHorse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).clientTrackingRange(10));
   public static final EntityType<Slime> SLIME = register("slime", EntityType.Builder.of(Slime::new, MobCategory.MONSTER).sized(2.04F, 2.04F).clientTrackingRange(10));
   public static final EntityType<SmallFireball> SMALL_FIREBALL = register("small_fireball", EntityType.Builder.<SmallFireball>of(SmallFireball::new, MobCategory.MISC).sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<Sniffer> SNIFFER = register("sniffer", EntityType.Builder.of(Sniffer::new, MobCategory.CREATURE).sized(1.9F, 1.75F).clientTrackingRange(10));
   public static final EntityType<SnowGolem> SNOW_GOLEM = register("snow_golem", EntityType.Builder.of(SnowGolem::new, MobCategory.MISC).immuneTo(Blocks.POWDER_SNOW).sized(0.7F, 1.9F).clientTrackingRange(8));
   public static final EntityType<Snowball> SNOWBALL = register("snowball", EntityType.Builder.<Snowball>of(Snowball::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<MinecartSpawner> SPAWNER_MINECART = register("spawner_minecart", EntityType.Builder.<MinecartSpawner>of(MinecartSpawner::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<SpectralArrow> SPECTRAL_ARROW = register("spectral_arrow", EntityType.Builder.<SpectralArrow>of(SpectralArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));
   public static final EntityType<Spider> SPIDER = register("spider", EntityType.Builder.of(Spider::new, MobCategory.MONSTER).sized(1.4F, 0.9F).clientTrackingRange(8));
   public static final EntityType<Squid> SQUID = register("squid", EntityType.Builder.of(Squid::new, MobCategory.WATER_CREATURE).sized(0.8F, 0.8F).clientTrackingRange(8));
   public static final EntityType<Stray> STRAY = register("stray", EntityType.Builder.of(Stray::new, MobCategory.MONSTER).sized(0.6F, 1.99F).immuneTo(Blocks.POWDER_SNOW).clientTrackingRange(8));
   public static final EntityType<Strider> STRIDER = register("strider", EntityType.Builder.of(Strider::new, MobCategory.CREATURE).fireImmune().sized(0.9F, 1.7F).clientTrackingRange(10));
   public static final EntityType<Tadpole> TADPOLE = register("tadpole", EntityType.Builder.of(Tadpole::new, MobCategory.CREATURE).sized(Tadpole.HITBOX_WIDTH, Tadpole.HITBOX_HEIGHT).clientTrackingRange(10));
   public static final EntityType<Display.TextDisplay> TEXT_DISPLAY = register("text_display", EntityType.Builder.of(Display.TextDisplay::new, MobCategory.MISC).sized(0.0F, 0.0F).clientTrackingRange(10).updateInterval(1));
   public static final EntityType<PrimedTnt> TNT = register("tnt", EntityType.Builder.<PrimedTnt>of(PrimedTnt::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10));
   public static final EntityType<MinecartTNT> TNT_MINECART = register("tnt_minecart", EntityType.Builder.<MinecartTNT>of(MinecartTNT::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
   public static final EntityType<TraderLlama> TRADER_LLAMA = register("trader_llama", EntityType.Builder.of(TraderLlama::new, MobCategory.CREATURE).sized(0.9F, 1.87F).clientTrackingRange(10));
   public static final EntityType<ThrownTrident> TRIDENT = register("trident", EntityType.Builder.<ThrownTrident>of(ThrownTrident::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));
   public static final EntityType<TropicalFish> TROPICAL_FISH = register("tropical_fish", EntityType.Builder.of(TropicalFish::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.4F).clientTrackingRange(4));
   public static final EntityType<Turtle> TURTLE = register("turtle", EntityType.Builder.of(Turtle::new, MobCategory.CREATURE).sized(1.2F, 0.4F).clientTrackingRange(10));
   public static final EntityType<Vex> VEX = register("vex", EntityType.Builder.of(Vex::new, MobCategory.MONSTER).fireImmune().sized(0.4F, 0.8F).clientTrackingRange(8));
   public static final EntityType<Villager> VILLAGER = register("villager", EntityType.Builder.<Villager>of(Villager::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10));
   public static final EntityType<Vindicator> VINDICATOR = register("vindicator", EntityType.Builder.of(Vindicator::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<WanderingTrader> WANDERING_TRADER = register("wandering_trader", EntityType.Builder.of(WanderingTrader::new, MobCategory.CREATURE).sized(0.6F, 1.95F).clientTrackingRange(10));
   public static final EntityType<Warden> WARDEN = register("warden", EntityType.Builder.of(Warden::new, MobCategory.MONSTER).sized(0.9F, 2.9F).clientTrackingRange(16).fireImmune());
   public static final EntityType<Witch> WITCH = register("witch", EntityType.Builder.of(Witch::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<WitherBoss> WITHER = register("wither", EntityType.Builder.of(WitherBoss::new, MobCategory.MONSTER).fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.9F, 3.5F).clientTrackingRange(10));
   public static final EntityType<WitherSkeleton> WITHER_SKELETON = register("wither_skeleton", EntityType.Builder.of(WitherSkeleton::new, MobCategory.MONSTER).fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.7F, 2.4F).clientTrackingRange(8));
   public static final EntityType<WitherSkull> WITHER_SKULL = register("wither_skull", EntityType.Builder.<WitherSkull>of(WitherSkull::new, MobCategory.MISC).sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10));
   public static final EntityType<Wolf> WOLF = register("wolf", EntityType.Builder.of(Wolf::new, MobCategory.CREATURE).sized(0.6F, 0.85F).clientTrackingRange(10));
   public static final EntityType<Zoglin> ZOGLIN = register("zoglin", EntityType.Builder.of(Zoglin::new, MobCategory.MONSTER).fireImmune().sized(1.3964844F, 1.4F).clientTrackingRange(8));
   public static final EntityType<Zombie> ZOMBIE = register("zombie", EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<ZombieHorse> ZOMBIE_HORSE = register("zombie_horse", EntityType.Builder.of(ZombieHorse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).clientTrackingRange(10));
   public static final EntityType<ZombieVillager> ZOMBIE_VILLAGER = register("zombie_villager", EntityType.Builder.of(ZombieVillager::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<ZombifiedPiglin> ZOMBIFIED_PIGLIN = register("zombified_piglin", EntityType.Builder.of(ZombifiedPiglin::new, MobCategory.MONSTER).fireImmune().sized(0.6F, 1.95F).clientTrackingRange(8));
   public static final EntityType<Player> PLAYER = register("player", EntityType.Builder.<Player>createNothing(MobCategory.MISC).noSave().noSummon().sized(0.6F, 1.8F).clientTrackingRange(32).updateInterval(2));
   public static final EntityType<FishingHook> FISHING_BOBBER = register("fishing_bobber", EntityType.Builder.<FishingHook>of(FishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5));
   private final EntityType.EntityFactory<T> factory;
   private final MobCategory category;
   private final ImmutableSet<Block> immuneTo;
   private final boolean serialize;
   private final boolean summon;
   private final boolean fireImmune;
   private final boolean canSpawnFarFromPlayer;
   private final int clientTrackingRange;
   private final int updateInterval;
   @Nullable
   private String descriptionId;
   @Nullable
   private Component description;
   @Nullable
   private ResourceLocation lootTable;
   private final EntityDimensions dimensions;
   private final FeatureFlagSet requiredFeatures;

   private final java.util.function.Predicate<EntityType<?>> velocityUpdateSupplier;
   private final java.util.function.ToIntFunction<EntityType<?>> trackingRangeSupplier;
   private final java.util.function.ToIntFunction<EntityType<?>> updateIntervalSupplier;
   private final java.util.function.BiFunction<net.minecraftforge.network.PlayMessages.SpawnEntity, Level, T> customClientFactory;

   private static <T extends Entity> EntityType<T> register(String pKey, EntityType.Builder<T> pBuilder) {
      return Registry.register(BuiltInRegistries.ENTITY_TYPE, pKey, pBuilder.build(pKey));
   }

   public static ResourceLocation getKey(EntityType<?> pEntityType) {
      return BuiltInRegistries.ENTITY_TYPE.getKey(pEntityType);
   }

   /**
    * Tries to get the entity type associated by the key.
    */
   public static Optional<EntityType<?>> byString(String pKey) {
      return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(pKey));
   }

   public EntityType(EntityType.EntityFactory<T> pFactory, MobCategory pCategory, boolean pSerialize, boolean pSummon, boolean pFireImmune, boolean pCanSpawnFarFromPlayer, ImmutableSet<Block> pImmuneTo, EntityDimensions pDimensions, int pClientTrackingRange, int pUpdateInterval, FeatureFlagSet pRequiredFeatures) {
       this(pFactory, pCategory, pSerialize, pSummon, pFireImmune, pCanSpawnFarFromPlayer, pImmuneTo, pDimensions, pClientTrackingRange, pUpdateInterval, pRequiredFeatures, EntityType::defaultVelocitySupplier, EntityType::defaultTrackingRangeSupplier, EntityType::defaultUpdateIntervalSupplier, null);
   }
   public EntityType(EntityType.EntityFactory<T> p_251402_, MobCategory p_251431_, boolean p_251439_, boolean p_251973_, boolean p_252007_, boolean p_250908_, ImmutableSet<Block> p_250201_, EntityDimensions p_251742_, int p_250479_, int p_249249_, FeatureFlagSet p_250427_, final java.util.function.Predicate<EntityType<?>> velocityUpdateSupplier, final java.util.function.ToIntFunction<EntityType<?>> trackingRangeSupplier, final java.util.function.ToIntFunction<EntityType<?>> updateIntervalSupplier, final java.util.function.BiFunction<net.minecraftforge.network.PlayMessages.SpawnEntity, Level, T> customClientFactory) {
      this.factory = p_251402_;
      this.category = p_251431_;
      this.canSpawnFarFromPlayer = p_250908_;
      this.serialize = p_251439_;
      this.summon = p_251973_;
      this.fireImmune = p_252007_;
      this.immuneTo = p_250201_;
      this.dimensions = p_251742_;
      this.clientTrackingRange = p_250479_;
      this.updateInterval = p_249249_;
      this.requiredFeatures = p_250427_;
      this.velocityUpdateSupplier = velocityUpdateSupplier;
      this.trackingRangeSupplier = trackingRangeSupplier;
      this.updateIntervalSupplier = updateIntervalSupplier;
      this.customClientFactory = customClientFactory;
   }

   @Nullable
   public T spawn(ServerLevel pServerLevel, @Nullable ItemStack pStack, @Nullable Player pPlayer, BlockPos pPos, MobSpawnType pSpawnType, boolean pShouldOffsetY, boolean pShouldOffsetYMore) {
      Consumer<T> consumer;
      CompoundTag compoundtag;
      if (pStack != null) {
         compoundtag = pStack.getTag();
         consumer = createDefaultStackConfig(pServerLevel, pStack, pPlayer);
      } else {
         consumer = (p_263563_) -> {
         };
         compoundtag = null;
      }

      return this.spawn(pServerLevel, compoundtag, consumer, pPos, pSpawnType, pShouldOffsetY, pShouldOffsetYMore);
   }

   public static <T extends Entity> Consumer<T> createDefaultStackConfig(ServerLevel pServerLevel, ItemStack pStack, @Nullable Player pPlayer) {
      return appendDefaultStackConfig((p_262561_) -> {
      }, pServerLevel, pStack, pPlayer);
   }

   public static <T extends Entity> Consumer<T> appendDefaultStackConfig(Consumer<T> pConsumer, ServerLevel pServerLevel, ItemStack pStack, @Nullable Player pPlayer) {
      return appendCustomEntityStackConfig(appendCustomNameConfig(pConsumer, pStack), pServerLevel, pStack, pPlayer);
   }

   public static <T extends Entity> Consumer<T> appendCustomNameConfig(Consumer<T> pConsumer, ItemStack pStack) {
      return pStack.hasCustomHoverName() ? pConsumer.andThen((p_262560_) -> {
         p_262560_.setCustomName(pStack.getHoverName());
      }) : pConsumer;
   }

   public static <T extends Entity> Consumer<T> appendCustomEntityStackConfig(Consumer<T> pConsumer, ServerLevel pLevel, ItemStack pStack, @Nullable Player pPlayer) {
      CompoundTag compoundtag = pStack.getTag();
      return compoundtag != null ? pConsumer.andThen((p_262558_) -> {
         updateCustomEntityTag(pLevel, pPlayer, p_262558_, compoundtag);
      }) : pConsumer;
   }

   @Nullable
   public T spawn(ServerLevel pLevel, BlockPos pPos, MobSpawnType pSpawnType) {
      return this.spawn(pLevel, (CompoundTag)null, (Consumer<T>)null, pPos, pSpawnType, false, false);
   }

   @Nullable
   public T spawn(ServerLevel pLevel, @Nullable CompoundTag pCompound, @Nullable Consumer<T> pConsumer, BlockPos pPos, MobSpawnType pSpawnType, boolean pShouldOffsetY, boolean pShouldOffsetYMore) {
      T t = this.create(pLevel, pCompound, pConsumer, pPos, pSpawnType, pShouldOffsetY, pShouldOffsetYMore);
      if (t != null) {
         pLevel.addFreshEntityWithPassengers(t);
      }

      return t;
   }

   @Nullable
   public T create(ServerLevel pLevel, @Nullable CompoundTag pNbt, @Nullable Consumer<T> pConsumer, BlockPos pPos, MobSpawnType pSpawnType, boolean pShouldOffsetY, boolean pShouldOffsetYMore) {
      T t = this.create(pLevel);
      if (t == null) {
         return (T)null;
      } else {
         double d0;
         if (pShouldOffsetY) {
            t.setPos((double)pPos.getX() + 0.5D, (double)(pPos.getY() + 1), (double)pPos.getZ() + 0.5D);
            d0 = getYOffset(pLevel, pPos, pShouldOffsetYMore, t.getBoundingBox());
         } else {
            d0 = 0.0D;
         }

         t.moveTo((double)pPos.getX() + 0.5D, (double)pPos.getY() + d0, (double)pPos.getZ() + 0.5D, Mth.wrapDegrees(pLevel.random.nextFloat() * 360.0F), 0.0F);
         if (t instanceof Mob) {
            Mob mob = (Mob)t;
            mob.yHeadRot = mob.getYRot();
            mob.yBodyRot = mob.getYRot();
            mob.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(mob.blockPosition()), pSpawnType, (SpawnGroupData)null, pNbt);
            mob.playAmbientSound();
         }

         if (pConsumer != null) {
            pConsumer.accept(t);
         }

         return t;
      }
   }

   protected static double getYOffset(LevelReader pLevel, BlockPos pPos, boolean pShouldOffsetYMore, AABB pBox) {
      AABB aabb = new AABB(pPos);
      if (pShouldOffsetYMore) {
         aabb = aabb.expandTowards(0.0D, -1.0D, 0.0D);
      }

      Iterable<VoxelShape> iterable = pLevel.getCollisions((Entity)null, aabb);
      return 1.0D + Shapes.collide(Direction.Axis.Y, pBox, iterable, pShouldOffsetYMore ? -2.0D : -1.0D);
   }

   public static void updateCustomEntityTag(Level pLevel, @Nullable Player pPlayer, @Nullable Entity pSpawnedEntity, @Nullable CompoundTag pItemNBT) {
      if (pItemNBT != null && pItemNBT.contains("EntityTag", 10)) {
         MinecraftServer minecraftserver = pLevel.getServer();
         if (minecraftserver != null && pSpawnedEntity != null) {
            if (pLevel.isClientSide || !pSpawnedEntity.onlyOpCanSetNbt() || pPlayer != null && minecraftserver.getPlayerList().isOp(pPlayer.getGameProfile())) {
               CompoundTag compoundtag = pSpawnedEntity.saveWithoutId(new CompoundTag());
               UUID uuid = pSpawnedEntity.getUUID();
               compoundtag.merge(pItemNBT.getCompound("EntityTag"));
               pSpawnedEntity.setUUID(uuid);
               pSpawnedEntity.load(compoundtag);
            }
         }
      }
   }

   public boolean canSerialize() {
      return this.serialize;
   }

   public boolean canSummon() {
      return this.summon;
   }

   public boolean fireImmune() {
      return this.fireImmune;
   }

   public boolean canSpawnFarFromPlayer() {
      return this.canSpawnFarFromPlayer;
   }

   public MobCategory getCategory() {
      return this.category;
   }

   public String getDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("entity", BuiltInRegistries.ENTITY_TYPE.getKey(this));
      }

      return this.descriptionId;
   }

   public Component getDescription() {
      if (this.description == null) {
         this.description = Component.translatable(this.getDescriptionId());
      }

      return this.description;
   }

   public String toString() {
      return this.getDescriptionId();
   }

   public String toShortString() {
      int i = this.getDescriptionId().lastIndexOf(46);
      return i == -1 ? this.getDescriptionId() : this.getDescriptionId().substring(i + 1);
   }

   public ResourceLocation getDefaultLootTable() {
      if (this.lootTable == null) {
         ResourceLocation resourcelocation = BuiltInRegistries.ENTITY_TYPE.getKey(this);
         this.lootTable = resourcelocation.withPrefix("entities/");
      }

      return this.lootTable;
   }

   public float getWidth() {
      return this.dimensions.width;
   }

   public float getHeight() {
      return this.dimensions.height;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   @Nullable
   public T create(Level pLevel) {
      return (T)(!this.isEnabled(pLevel.enabledFeatures()) ? null : this.factory.create(this, pLevel));
   }

   public static Optional<Entity> create(CompoundTag pTag, Level pLevel) {
      return Util.ifElse(by(pTag).map((p_185998_) -> {
         return p_185998_.create(pLevel);
      }), (p_185990_) -> {
         p_185990_.load(pTag);
      }, () -> {
         LOGGER.warn("Skipping Entity with id {}", (Object)pTag.getString("id"));
      });
   }

   public AABB getAABB(double pX, double pY, double pZ) {
      float f = this.getWidth() / 2.0F;
      return new AABB(pX - (double)f, pY, pZ - (double)f, pX + (double)f, pY + (double)this.getHeight(), pZ + (double)f);
   }

   public boolean isBlockDangerous(BlockState pState) {
      if (this.immuneTo.contains(pState.getBlock())) {
         return false;
      } else if (!this.fireImmune && WalkNodeEvaluator.isBurningBlock(pState)) {
         return true;
      } else {
         return pState.is(Blocks.WITHER_ROSE) || pState.is(Blocks.SWEET_BERRY_BUSH) || pState.is(Blocks.CACTUS) || pState.is(Blocks.POWDER_SNOW);
      }
   }

   public EntityDimensions getDimensions() {
      return this.dimensions;
   }

   public static Optional<EntityType<?>> by(CompoundTag pCompound) {
      return BuiltInRegistries.ENTITY_TYPE.getOptional(new ResourceLocation(pCompound.getString("id")));
   }

   @Nullable
   public static Entity loadEntityRecursive(CompoundTag pCompound, Level pLevel, Function<Entity, Entity> pEntityFunction) {
      return loadStaticEntity(pCompound, pLevel).map(pEntityFunction).map((p_185995_) -> {
         if (pCompound.contains("Passengers", 9)) {
            ListTag listtag = pCompound.getList("Passengers", 10);

            for(int i = 0; i < listtag.size(); ++i) {
               Entity entity = loadEntityRecursive(listtag.getCompound(i), pLevel, pEntityFunction);
               if (entity != null) {
                  entity.startRiding(p_185995_, true);
               }
            }
         }

         return p_185995_;
      }).orElse((Entity)null);
   }

   public static Stream<Entity> loadEntitiesRecursive(final List<? extends Tag> pTags, final Level pLevel) {
      final Spliterator<? extends Tag> spliterator = pTags.spliterator();
      return StreamSupport.stream(new Spliterator<Entity>() {
         public boolean tryAdvance(Consumer<? super Entity> p_147066_) {
            return spliterator.tryAdvance((p_147059_) -> {
               EntityType.loadEntityRecursive((CompoundTag)p_147059_, pLevel, (p_147062_) -> {
                  p_147066_.accept(p_147062_);
                  return p_147062_;
               });
            });
         }

         public Spliterator<Entity> trySplit() {
            return null;
         }

         public long estimateSize() {
            return (long)pTags.size();
         }

         public int characteristics() {
            return 1297;
         }
      }, false);
   }

   private static Optional<Entity> loadStaticEntity(CompoundTag pCompound, Level pLevel) {
      try {
         return create(pCompound, pLevel);
      } catch (RuntimeException runtimeexception) {
         LOGGER.warn("Exception loading entity: ", (Throwable)runtimeexception);
         return Optional.empty();
      }
   }

   public int clientTrackingRange() {
      return trackingRangeSupplier.applyAsInt(this);
   }
   private int defaultTrackingRangeSupplier() {
      return this.clientTrackingRange;
   }

   public int updateInterval() {
      return updateIntervalSupplier.applyAsInt(this);
   }
   private int defaultUpdateIntervalSupplier() {
      return this.updateInterval;
   }

   public boolean trackDeltas() {
      return velocityUpdateSupplier.test(this);
   }
   private boolean defaultVelocitySupplier() {
      return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != GLOW_ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
   }

   /**
    * Checks if this entity type is contained in the tag
    */
   public boolean is(TagKey<EntityType<?>> pTag) {
      return this.builtInRegistryHolder.is(pTag);
   }

   @Nullable
   public T tryCast(Entity pEntity) {
      return (T)(pEntity.getType() == this ? pEntity : null);
   }

   public Class<? extends Entity> getBaseClass() {
      return Entity.class;
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<EntityType<?>> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   public T customClientSpawn(net.minecraftforge.network.PlayMessages.SpawnEntity packet, Level world) {
       if (customClientFactory == null) return this.create(world);
       return customClientFactory.apply(packet, world);
   }
   public Stream<TagKey<EntityType<?>>> getTags() {return this.builtInRegistryHolder().tags();}

   public static class Builder<T extends Entity> {
      private final EntityType.EntityFactory<T> factory;
      private final MobCategory category;
      private ImmutableSet<Block> immuneTo = ImmutableSet.of();
      private boolean serialize = true;
      private boolean summon = true;
      private boolean fireImmune;
      private boolean canSpawnFarFromPlayer;
      private int clientTrackingRange = 5;
      private int updateInterval = 3;
      private EntityDimensions dimensions = EntityDimensions.scalable(0.6F, 1.8F);
      private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

      private java.util.function.Predicate<EntityType<?>> velocityUpdateSupplier = EntityType::defaultVelocitySupplier;
      private java.util.function.ToIntFunction<EntityType<?>> trackingRangeSupplier = EntityType::defaultTrackingRangeSupplier;
      private java.util.function.ToIntFunction<EntityType<?>> updateIntervalSupplier = EntityType::defaultUpdateIntervalSupplier;
      private java.util.function.BiFunction<net.minecraftforge.network.PlayMessages.SpawnEntity, Level, T> customClientFactory;

      private Builder(EntityType.EntityFactory<T> pFactory, MobCategory pCategory) {
         this.factory = pFactory;
         this.category = pCategory;
         this.canSpawnFarFromPlayer = pCategory == MobCategory.CREATURE || pCategory == MobCategory.MISC;
      }

      public static <T extends Entity> EntityType.Builder<T> of(EntityType.EntityFactory<T> pFactory, MobCategory pCategory) {
         return new EntityType.Builder<>(pFactory, pCategory);
      }

      public static <T extends Entity> EntityType.Builder<T> createNothing(MobCategory pCategory) {
         return new EntityType.Builder<>((p_20708_, p_20709_) -> {
            return (T)null;
         }, pCategory);
      }

      public EntityType.Builder<T> sized(float pWidth, float pHeight) {
         this.dimensions = EntityDimensions.scalable(pWidth, pHeight);
         return this;
      }

      public EntityType.Builder<T> noSummon() {
         this.summon = false;
         return this;
      }

      public EntityType.Builder<T> noSave() {
         this.serialize = false;
         return this;
      }

      public EntityType.Builder<T> fireImmune() {
         this.fireImmune = true;
         return this;
      }

      public EntityType.Builder<T> immuneTo(Block... pBlocks) {
         this.immuneTo = ImmutableSet.copyOf(pBlocks);
         return this;
      }

      public EntityType.Builder<T> canSpawnFarFromPlayer() {
         this.canSpawnFarFromPlayer = true;
         return this;
      }

      public EntityType.Builder<T> clientTrackingRange(int pClientTrackingRange) {
         this.clientTrackingRange = pClientTrackingRange;
         return this;
      }

      public EntityType.Builder<T> updateInterval(int pUpdateInterval) {
         this.updateInterval = pUpdateInterval;
         return this;
      }

      public EntityType.Builder<T> requiredFeatures(FeatureFlag... pRequiredFeatures) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset(pRequiredFeatures);
         return this;
      }

      public EntityType.Builder<T> setUpdateInterval(int interval) {
          this.updateIntervalSupplier = t->interval;
          return this;
      }

      public EntityType.Builder<T> setTrackingRange(int range) {
          this.trackingRangeSupplier = t->range;
          return this;
      }

      public EntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
          this.velocityUpdateSupplier = t->value;
          return this;
      }

      /**
       * By default, entities are spawned clientside via {@link EntityType#create(Level)}}.
       * If you need finer control over the spawning process, use this to get read access to the spawn packet.
       */
      public EntityType.Builder<T> setCustomClientFactory(java.util.function.BiFunction<net.minecraftforge.network.PlayMessages.SpawnEntity, Level, T> customClientFactory) {
          this.customClientFactory = customClientFactory;
          return this;
      }

      public EntityType<T> build(String pKey) {
         if (this.serialize) {
            Util.fetchChoiceType(References.ENTITY_TREE, pKey);
         }

         return new EntityType<>(this.factory, this.category, this.serialize, this.summon, this.fireImmune, this.canSpawnFarFromPlayer, this.immuneTo, this.dimensions, this.clientTrackingRange, this.updateInterval, this.requiredFeatures, velocityUpdateSupplier, trackingRangeSupplier, updateIntervalSupplier, customClientFactory);
      }
   }

   public interface EntityFactory<T extends Entity> {
      T create(EntityType<T> pEntityType, Level pLevel);
   }
}
