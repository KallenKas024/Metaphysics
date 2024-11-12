package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements FeatureElement, ItemLike, net.minecraftforge.common.extensions.IForgeItem {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Map<Block, Item> BY_BLOCK = net.minecraftforge.registries.GameData.getBlockItemMap();
   protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
   protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
   public static final int MAX_STACK_SIZE = 64;
   public static final int EAT_DURATION = 32;
   public static final int MAX_BAR_WIDTH = 13;
   private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
   private final Rarity rarity;
   private final int maxStackSize;
   private final int maxDamage;
   private final boolean isFireResistant;
   @Nullable
   private final Item craftingRemainingItem;
   @Nullable
   private String descriptionId;
   @Nullable
   private final FoodProperties foodProperties;
   private final FeatureFlagSet requiredFeatures;

   public static int getId(Item pItem) {
      return pItem == null ? 0 : BuiltInRegistries.ITEM.getId(pItem);
   }

   public static Item byId(int pId) {
      return BuiltInRegistries.ITEM.byId(pId);
   }

   /** @deprecated */
   @Deprecated
   public static Item byBlock(Block pBlock) {
      return BY_BLOCK.getOrDefault(pBlock, Items.AIR);
   }

   public Item(Item.Properties pProperties) {
      this.rarity = pProperties.rarity;
      this.craftingRemainingItem = pProperties.craftingRemainingItem;
      this.maxDamage = pProperties.maxDamage;
      this.maxStackSize = pProperties.maxStackSize;
      this.foodProperties = pProperties.foodProperties;
      this.isFireResistant = pProperties.isFireResistant;
      this.requiredFeatures = pProperties.requiredFeatures;
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         String s = this.getClass().getSimpleName();
         if (!s.endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)s);
         }
      }
      this.canRepair = pProperties.canRepair;
      initClient();
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Item> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   /**
    * Called as the item is being used by an entity.
    */
   public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
   }

   /** @deprecated Forge: {@link net.minecraftforge.common.extensions.IForgeItem#onDestroyed(ItemEntity, DamageSource) Use damage source sensitive version} */
   @Deprecated
   public void onDestroyed(ItemEntity pItemEntity) {
   }

   public void verifyTagAfterLoad(CompoundTag pTag) {
   }

   public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      return true;
   }

   public Item asItem() {
      return this;
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      return InteractionResult.PASS;
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      return 1.0F;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
      if (itemstack.isEdible()) {
         if (pPlayer.canEat(itemstack.getFoodProperties(pPlayer).canAlwaysEat())) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(itemstack);
         } else {
            return InteractionResultHolder.fail(itemstack);
         }
      } else {
         return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
      }
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
      return this.isEdible() ? pLivingEntity.eat(pLevel, pStack) : pStack;
   }

   /**
    * Returns the maximum size of the stack for a specific item.
    */
   @Deprecated // Use ItemStack sensitive version.
   public final int getMaxStackSize() {
      return this.maxStackSize;
   }

   /**
    * Returns the maximum damage an item can take.
    */
   @Deprecated // Use ItemStack sensitive version.
   public final int getMaxDamage() {
      return this.maxDamage;
   }

   public boolean canBeDepleted() {
      return this.maxDamage > 0;
   }

   public boolean isBarVisible(ItemStack pStack) {
      return pStack.isDamaged();
   }

   public int getBarWidth(ItemStack pStack) {
      return Math.round(13.0F - (float)pStack.getDamageValue() * 13.0F / (float)this.getMaxDamage(pStack));
   }

   public int getBarColor(ItemStack pStack) {
      float stackMaxDamage = this.getMaxDamage(pStack);
      float f = Math.max(0.0F, (stackMaxDamage - (float)pStack.getDamageValue()) / stackMaxDamage);
      return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
   }

   public boolean overrideStackedOnOther(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer) {
      return false;
   }

   public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
      return false;
   }

   /**
    * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
    * the damage on the stack.
    */
   public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
      return false;
   }

   /**
    * Called when a {@link net.minecraft.world.level.block.Block} is destroyed using this Item. Return {@code true} to
    * trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pMiningEntity) {
      return false;
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      return false;
   }

   /**
    * Try interacting with given entity. Return {@code InteractionResult.PASS} if nothing should happen.
    */
   public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
      return InteractionResult.PASS;
   }

   public Component getDescription() {
      return Component.translatable(this.getDescriptionId());
   }

   public String toString() {
      return BuiltInRegistries.ITEM.getKey(this).getPath();
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
      }

      return this.descriptionId;
   }

   /**
    * Returns the unlocalized name of this item.
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return this.getDescriptionId();
   }

   /**
    * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
    */
   public boolean shouldOverrideMultiplayerNbt() {
      return true;
   }

   @Nullable
   @Deprecated // Use ItemStack sensitive version.
   public final Item getCraftingRemainingItem() {
      return this.craftingRemainingItem;
   }

   /**
    * True if this Item has a container item (a.k.a. crafting result)
    */
   @Deprecated // Use ItemStack sensitive version.
   public boolean hasCraftingRemainingItem() {
      return this.craftingRemainingItem != null;
   }

   /**
    * Called each tick as long the item is in a player's inventory. Used by maps to check if it's in a player's hand and
    * update its contents.
    */
   public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
   }

   /**
    * Called when item is crafted/smelted. Used only by maps so far.
    */
   public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
   }

   /**
    * Returns {@code true} if this is a complex item.
    */
   public boolean isComplex() {
      return false;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return pStack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      if (pStack.getItem().isEdible()) {
         return pStack.getFoodProperties(null).isFastFood() ? 16 : 32;
      } else {
         return 0;
      }
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
      return Optional.empty();
   }

   /**
    * Gets the title name of the book
    */
   public Component getName(ItemStack pStack) {
      return Component.translatable(this.getDescriptionId(pStack));
   }

   /**
    * Returns {@code true} if this item has an enchantment glint. By default, this returns
    * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
    * true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return pStack.isEnchanted();
   }

   /**
    * Return an item rarity from EnumRarity
    */
   public Rarity getRarity(ItemStack pStack) {
      if (!pStack.isEnchanted()) {
         return this.rarity;
      } else {
         switch (this.rarity) {
            case COMMON:
            case UNCOMMON:
               return Rarity.RARE;
            case RARE:
               return Rarity.EPIC;
            case EPIC:
            default:
               return this.rarity;
         }
      }
   }

   /**
    * Checks isDamagable and if it cannot be stacked
    */
   public boolean isEnchantable(ItemStack pStack) {
      return this.getMaxStackSize(pStack) == 1 && this.isDamageable(pStack);
   }

   protected static BlockHitResult getPlayerPOVHitResult(Level pLevel, Player pPlayer, ClipContext.Fluid pFluidMode) {
      float f = pPlayer.getXRot();
      float f1 = pPlayer.getYRot();
      Vec3 vec3 = pPlayer.getEyePosition();
      float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
      float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
      float f6 = f3 * f4;
      float f7 = f2 * f4;
      double d0 = pPlayer.getBlockReach();
      Vec3 vec31 = vec3.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
      return pLevel.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, pFluidMode, pPlayer));
   }

   /** Forge: Use ItemStack sensitive version. */
   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   @Deprecated
   public int getEnchantmentValue() {
      return 0;
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
      return false;
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   @Deprecated // Use ItemStack sensitive version.
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pSlot) {
      return ImmutableMultimap.of();
   }

   protected final boolean canRepair;

   @Override
   public boolean isRepairable(ItemStack stack) {
      return canRepair && isDamageable(stack);
   }

   /**
    * If this stack's item is a crossbow
    */
   public boolean useOnRelease(ItemStack pStack) {
      return pStack.getItem() == Items.CROSSBOW;
   }

   public ItemStack getDefaultInstance() {
      return new ItemStack(this);
   }

   public boolean isEdible() {
      return this.foodProperties != null;
   }

   // Use IForgeItem#getFoodProperties(ItemStack, LivingEntity) in favour of this.
   @Deprecated
   @Nullable
   public FoodProperties getFoodProperties() {
      return this.foodProperties;
   }

   public SoundEvent getDrinkingSound() {
      return SoundEvents.GENERIC_DRINK;
   }

   public SoundEvent getEatingSound() {
      return SoundEvents.GENERIC_EAT;
   }

   public boolean isFireResistant() {
      return this.isFireResistant;
   }

   public boolean canBeHurtBy(DamageSource pDamageSource) {
      return !this.isFireResistant || !pDamageSource.is(DamageTypeTags.IS_FIRE);
   }

   public boolean canFitInsideContainerItems() {
      return true;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   // FORGE START
   private Object renderProperties;

   /*
      DO NOT CALL, IT WILL DISAPPEAR IN THE FUTURE
      Call RenderProperties.get instead
    */
   public Object getRenderPropertiesInternal() {
      return renderProperties;
   }

   private void initClient() {
      // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
      if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData()) {
         initializeClient(properties -> {
            if (properties == this)
               throw new IllegalStateException("Don't extend IItemRenderProperties in your item, use an anonymous class instead.");
            this.renderProperties = properties;
         });
      }
   }

   public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
   }
   // END FORGE

   public static class Properties {
      int maxStackSize = 64;
      int maxDamage;
      @Nullable
      Item craftingRemainingItem;
      Rarity rarity = Rarity.COMMON;
      /** Sets food information to this item */
      @Nullable
      FoodProperties foodProperties;
      boolean isFireResistant;
      FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
      private boolean canRepair = true;

      public Item.Properties food(FoodProperties pFood) {
         this.foodProperties = pFood;
         return this;
      }

      public Item.Properties stacksTo(int pMaxStackSize) {
         if (this.maxDamage > 0) {
            throw new RuntimeException("Unable to have damage AND stack.");
         } else {
            this.maxStackSize = pMaxStackSize;
            return this;
         }
      }

      public Item.Properties defaultDurability(int pMaxDamage) {
         return this.maxDamage == 0 ? this.durability(pMaxDamage) : this;
      }

      public Item.Properties durability(int pMaxDamage) {
         this.maxDamage = pMaxDamage;
         this.maxStackSize = 1;
         return this;
      }

      public Item.Properties craftRemainder(Item pCraftingRemainingItem) {
         this.craftingRemainingItem = pCraftingRemainingItem;
         return this;
      }

      public Item.Properties rarity(Rarity pRarity) {
         this.rarity = pRarity;
         return this;
      }

      public Item.Properties fireResistant() {
         this.isFireResistant = true;
         return this;
      }

      public Item.Properties setNoRepair() {
         canRepair = false;
         return this;
      }

      public Item.Properties requiredFeatures(FeatureFlag... pRequiredFeatures) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset(pRequiredFeatures);
         return this;
      }
   }
}
