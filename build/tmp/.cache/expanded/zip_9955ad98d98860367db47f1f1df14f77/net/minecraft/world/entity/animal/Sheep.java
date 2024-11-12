package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class Sheep extends Animal implements Shearable, net.minecraftforge.common.IForgeShearable {
   private static final int EAT_ANIMATION_TICKS = 40;
   private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
   private static final Map<DyeColor, ItemLike> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), (p_29841_) -> {
      p_29841_.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
      p_29841_.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
      p_29841_.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
      p_29841_.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
      p_29841_.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
      p_29841_.put(DyeColor.LIME, Blocks.LIME_WOOL);
      p_29841_.put(DyeColor.PINK, Blocks.PINK_WOOL);
      p_29841_.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
      p_29841_.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
      p_29841_.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
      p_29841_.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
      p_29841_.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
      p_29841_.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
      p_29841_.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
      p_29841_.put(DyeColor.RED, Blocks.RED_WOOL);
      p_29841_.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
   });
   /** Map from EnumDyeColor to RGB values for passage to GlStateManager.color() */
   private static final Map<DyeColor, float[]> COLORARRAY_BY_COLOR = Maps.<DyeColor, float[]>newEnumMap(Arrays.stream(DyeColor.values()).collect(Collectors.toMap((p_29868_) -> {
      return p_29868_;
   }, Sheep::createSheepColor)));
   private int eatAnimationTick;
   private EatBlockGoal eatBlockGoal;

   private static float[] createSheepColor(DyeColor p_29866_) {
      if (p_29866_ == DyeColor.WHITE) {
         return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
      } else {
         float[] afloat = p_29866_.getTextureDiffuseColors();
         float f = 0.75F;
         return new float[]{afloat[0] * 0.75F, afloat[1] * 0.75F, afloat[2] * 0.75F};
      }
   }

   public static float[] getColorArray(DyeColor pDyeColor) {
      return COLORARRAY_BY_COLOR.get(pDyeColor);
   }

   public Sheep(EntityType<? extends Sheep> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void registerGoals() {
      this.eatBlockGoal = new EatBlockGoal(this);
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT), false));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
      this.goalSelector.addGoal(5, this.eatBlockGoal);
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
   }

   protected void customServerAiStep() {
      this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
      super.customServerAiStep();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.level().isClientSide) {
         this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
      }

      super.aiStep();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_WOOL_ID, (byte)0);
   }

   public ResourceLocation getDefaultLootTable() {
      if (this.isSheared()) {
         return this.getType().getDefaultLootTable();
      } else {
         ResourceLocation resourcelocation;
         switch (this.getColor()) {
            case WHITE:
               resourcelocation = BuiltInLootTables.SHEEP_WHITE;
               break;
            case ORANGE:
               resourcelocation = BuiltInLootTables.SHEEP_ORANGE;
               break;
            case MAGENTA:
               resourcelocation = BuiltInLootTables.SHEEP_MAGENTA;
               break;
            case LIGHT_BLUE:
               resourcelocation = BuiltInLootTables.SHEEP_LIGHT_BLUE;
               break;
            case YELLOW:
               resourcelocation = BuiltInLootTables.SHEEP_YELLOW;
               break;
            case LIME:
               resourcelocation = BuiltInLootTables.SHEEP_LIME;
               break;
            case PINK:
               resourcelocation = BuiltInLootTables.SHEEP_PINK;
               break;
            case GRAY:
               resourcelocation = BuiltInLootTables.SHEEP_GRAY;
               break;
            case LIGHT_GRAY:
               resourcelocation = BuiltInLootTables.SHEEP_LIGHT_GRAY;
               break;
            case CYAN:
               resourcelocation = BuiltInLootTables.SHEEP_CYAN;
               break;
            case PURPLE:
               resourcelocation = BuiltInLootTables.SHEEP_PURPLE;
               break;
            case BLUE:
               resourcelocation = BuiltInLootTables.SHEEP_BLUE;
               break;
            case BROWN:
               resourcelocation = BuiltInLootTables.SHEEP_BROWN;
               break;
            case GREEN:
               resourcelocation = BuiltInLootTables.SHEEP_GREEN;
               break;
            case RED:
               resourcelocation = BuiltInLootTables.SHEEP_RED;
               break;
            case BLACK:
               resourcelocation = BuiltInLootTables.SHEEP_BLACK;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return resourcelocation;
      }
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 10) {
         this.eatAnimationTick = 40;
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public float getHeadEatPositionScale(float pPartialTick) {
      if (this.eatAnimationTick <= 0) {
         return 0.0F;
      } else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
         return 1.0F;
      } else {
         return this.eatAnimationTick < 4 ? ((float)this.eatAnimationTick - pPartialTick) / 4.0F : -((float)(this.eatAnimationTick - 40) - pPartialTick) / 4.0F;
      }
   }

   public float getHeadEatAngleScale(float pPartialTick) {
      if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
         float f = ((float)(this.eatAnimationTick - 4) - pPartialTick) / 32.0F;
         return ((float)Math.PI / 5F) + 0.21991149F * Mth.sin(f * 28.7F);
      } else {
         return this.eatAnimationTick > 0 ? ((float)Math.PI / 5F) : this.getXRot() * ((float)Math.PI / 180F);
      }
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (false && itemstack.getItem() == Items.SHEARS) { //Forge: Moved to onSheared
         if (!this.level().isClientSide && this.readyForShearing()) {
            this.shear(SoundSource.PLAYERS);
            this.gameEvent(GameEvent.SHEAR, pPlayer);
            itemstack.hurtAndBreak(1, pPlayer, (p_29822_) -> {
               p_29822_.broadcastBreakEvent(pHand);
            });
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.CONSUME;
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   public void shear(SoundSource pCategory) {
      this.level().playSound((Player)null, this, SoundEvents.SHEEP_SHEAR, pCategory, 1.0F, 1.0F);
      this.setSheared(true);
      int i = 1 + this.random.nextInt(3);

      for(int j = 0; j < i; ++j) {
         ItemEntity itementity = this.spawnAtLocation(ITEM_BY_DYE.get(this.getColor()), 1);
         if (itementity != null) {
            itementity.setDeltaMovement(itementity.getDeltaMovement().add((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(this.random.nextFloat() * 0.05F), (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)));
         }
      }

   }

   public boolean readyForShearing() {
      return this.isAlive() && !this.isSheared() && !this.isBaby();
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("Sheared", this.isSheared());
      pCompound.putByte("Color", (byte)this.getColor().getId());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setSheared(pCompound.getBoolean("Sheared"));
      this.setColor(DyeColor.byId(pCompound.getByte("Color")));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SHEEP_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SHEEP_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SHEEP_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
   }

   /**
    * Gets the wool color of this sheep.
    */
   public DyeColor getColor() {
      return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 15);
   }

   /**
    * Sets the wool color of this sheep
    */
   public void setColor(DyeColor pDyeColor) {
      byte b0 = this.entityData.get(DATA_WOOL_ID);
      this.entityData.set(DATA_WOOL_ID, (byte)(b0 & 240 | pDyeColor.getId() & 15));
   }

   /**
    * Returns {@code true} if a sheep's wool has been sheared.
    */
   public boolean isSheared() {
      return (this.entityData.get(DATA_WOOL_ID) & 16) != 0;
   }

   /**
    * Makes a sheep sheared if set to {@code true}.
    */
   public void setSheared(boolean pSheared) {
      byte b0 = this.entityData.get(DATA_WOOL_ID);
      if (pSheared) {
         this.entityData.set(DATA_WOOL_ID, (byte)(b0 | 16));
      } else {
         this.entityData.set(DATA_WOOL_ID, (byte)(b0 & -17));
      }

   }

   public static DyeColor getRandomSheepColor(RandomSource pRandom) {
      int i = pRandom.nextInt(100);
      if (i < 5) {
         return DyeColor.BLACK;
      } else if (i < 10) {
         return DyeColor.GRAY;
      } else if (i < 15) {
         return DyeColor.LIGHT_GRAY;
      } else if (i < 18) {
         return DyeColor.BROWN;
      } else {
         return pRandom.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
      }
   }

   @Nullable
   public Sheep getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Sheep sheep = EntityType.SHEEP.create(pLevel);
      if (sheep != null) {
         sheep.setColor(this.getOffspringColor(this, (Sheep)pOtherParent));
      }

      return sheep;
   }

   /**
    * Applies the benefits of growing back wool and faster growing up to the acting entity. This function is used in the
    * {@code EatBlockGoal}.
    */
   public void ate() {
      super.ate();
      this.setSheared(false);
      if (this.isBaby()) {
         this.ageUp(60);
      }

   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      this.setColor(getRandomSheepColor(pLevel.getRandom()));
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   /**
    * Attempts to mix both parent sheep to come up with a mixed dye color.
    */
   private DyeColor getOffspringColor(Animal pFather, Animal pMother) {
      DyeColor dyecolor = ((Sheep)pFather).getColor();
      DyeColor dyecolor1 = ((Sheep)pMother).getColor();
      CraftingContainer craftingcontainer = makeContainer(dyecolor, dyecolor1);
      return this.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingcontainer, this.level()).map((p_289444_) -> {
         return p_289444_.assemble(craftingcontainer, this.level().registryAccess());
      }).map(ItemStack::getItem).filter(DyeItem.class::isInstance).map(DyeItem.class::cast).map(DyeItem::getDyeColor).orElseGet(() -> {
         return this.level().random.nextBoolean() ? dyecolor : dyecolor1;
      });
   }

   private static CraftingContainer makeContainer(DyeColor pFatherColor, DyeColor pMotherColor) {
      CraftingContainer craftingcontainer = new TransientCraftingContainer(new AbstractContainerMenu((MenuType)null, -1) {
         /**
          * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the
          * player inventory and the other inventory(s).
          */
         public ItemStack quickMoveStack(Player p_218264_, int p_218265_) {
            return ItemStack.EMPTY;
         }

         /**
          * Determines whether supplied player can use this container
          */
         public boolean stillValid(Player p_29888_) {
            return false;
         }
      }, 2, 1);
      craftingcontainer.setItem(0, new ItemStack(DyeItem.byColor(pFatherColor)));
      craftingcontainer.setItem(1, new ItemStack(DyeItem.byColor(pMotherColor)));
      return craftingcontainer;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 0.95F * pSize.height;
   }

   @Override
   public boolean isShearable(@org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos) {
      return readyForShearing();
   }

   @org.jetbrains.annotations.NotNull
   @Override
   public java.util.List<ItemStack> onSheared(@Nullable Player player, @org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos, int fortune) {
      world.playSound(null, this, SoundEvents.SHEEP_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
      this.gameEvent(GameEvent.SHEAR, player);
      if (!world.isClientSide) {
         this.setSheared(true);
         int i = 1 + this.random.nextInt(3);

         java.util.List<ItemStack> items = new java.util.ArrayList<>();
         for (int j = 0; j < i; ++j) {
            items.add(new ItemStack(ITEM_BY_DYE.get(this.getColor())));
         }
         return items;
      }
      return java.util.Collections.emptyList();
   }
}
