package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpawnEggItem extends Item {
   private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
   private final int backgroundColor;
   private final int highlightColor;
   private final EntityType<?> defaultType;

   /** @deprecated Forge: Use {@link net.minecraftforge.common.ForgeSpawnEggItem} instead for suppliers */
   @Deprecated
   public SpawnEggItem(EntityType<? extends Mob> pDefaultType, int pBackgroundColor, int pHighlightColor, Item.Properties pProperties) {
      super(pProperties);
      this.defaultType = pDefaultType;
      this.backgroundColor = pBackgroundColor;
      this.highlightColor = pHighlightColor;
      if (pDefaultType != null)
      BY_ID.put(pDefaultType, this);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      if (!(level instanceof ServerLevel)) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack itemstack = pContext.getItemInHand();
         BlockPos blockpos = pContext.getClickedPos();
         Direction direction = pContext.getClickedFace();
         BlockState blockstate = level.getBlockState(blockpos);
         if (blockstate.is(Blocks.SPAWNER)) {
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof SpawnerBlockEntity) {
               SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
               EntityType<?> entitytype1 = this.getType(itemstack.getTag());
               spawnerblockentity.setEntityId(entitytype1, level.getRandom());
               blockentity.setChanged();
               level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
               level.gameEvent(pContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockpos);
               itemstack.shrink(1);
               return InteractionResult.CONSUME;
            }
         }

         BlockPos blockpos1;
         if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
            blockpos1 = blockpos;
         } else {
            blockpos1 = blockpos.relative(direction);
         }

         EntityType<?> entitytype = this.getType(itemstack.getTag());
         if (entitytype.spawn((ServerLevel)level, itemstack, pContext.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
            itemstack.shrink(1);
            level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
         }

         return InteractionResult.CONSUME;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
      if (blockhitresult.getType() != HitResult.Type.BLOCK) {
         return InteractionResultHolder.pass(itemstack);
      } else if (!(pLevel instanceof ServerLevel)) {
         return InteractionResultHolder.success(itemstack);
      } else {
         BlockPos blockpos = blockhitresult.getBlockPos();
         if (!(pLevel.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemstack);
         } else if (pLevel.mayInteract(pPlayer, blockpos) && pPlayer.mayUseItemAt(blockpos, blockhitresult.getDirection(), itemstack)) {
            EntityType<?> entitytype = this.getType(itemstack.getTag());
            Entity entity = entitytype.spawn((ServerLevel)pLevel, itemstack, pPlayer, blockpos, MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) {
               return InteractionResultHolder.pass(itemstack);
            } else {
               if (!pPlayer.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }

               pPlayer.awardStat(Stats.ITEM_USED.get(this));
               pLevel.gameEvent(pPlayer, GameEvent.ENTITY_PLACE, entity.position());
               return InteractionResultHolder.consume(itemstack);
            }
         } else {
            return InteractionResultHolder.fail(itemstack);
         }
      }
   }

   public boolean spawnsEntity(@Nullable CompoundTag pNbt, EntityType<?> pType) {
      return Objects.equals(this.getType(pNbt), pType);
   }

   public int getColor(int pTintIndex) {
      return pTintIndex == 0 ? this.backgroundColor : this.highlightColor;
   }

   /** @deprecated Forge: call {@link net.minecraftforge.common.ForgeSpawnEggItem#fromEntityType(EntityType)} instead */
   @Deprecated
   @Nullable
   public static SpawnEggItem byId(@Nullable EntityType<?> pType) {
      return BY_ID.get(pType);
   }

   public static Iterable<SpawnEggItem> eggs() {
      return Iterables.unmodifiableIterable(BY_ID.values());
   }

   public EntityType<?> getType(@Nullable CompoundTag pNbt) {
      if (pNbt != null && pNbt.contains("EntityTag", 10)) {
         CompoundTag compoundtag = pNbt.getCompound("EntityTag");
         if (compoundtag.contains("id", 8)) {
            return EntityType.byString(compoundtag.getString("id")).orElse(this.getDefaultType());
         }
      }

      return this.getDefaultType();
   }

   public FeatureFlagSet requiredFeatures() {
      return this.getDefaultType().requiredFeatures();
   }

   public Optional<Mob> spawnOffspringFromSpawnEgg(Player pPlayer, Mob pMob, EntityType<? extends Mob> pEntityType, ServerLevel pServerLevel, Vec3 pPos, ItemStack pStack) {
      if (!this.spawnsEntity(pStack.getTag(), pEntityType)) {
         return Optional.empty();
      } else {
         Mob mob;
         if (pMob instanceof AgeableMob) {
            mob = ((AgeableMob)pMob).getBreedOffspring(pServerLevel, (AgeableMob)pMob);
         } else {
            mob = pEntityType.create(pServerLevel);
         }

         if (mob == null) {
            return Optional.empty();
         } else {
            mob.setBaby(true);
            if (!mob.isBaby()) {
               return Optional.empty();
            } else {
               mob.moveTo(pPos.x(), pPos.y(), pPos.z(), 0.0F, 0.0F);
               pServerLevel.addFreshEntityWithPassengers(mob);
               if (pStack.hasCustomHoverName()) {
                  mob.setCustomName(pStack.getHoverName());
               }

               if (!pPlayer.getAbilities().instabuild) {
                  pStack.shrink(1);
               }

               return Optional.of(mob);
            }
         }
      }
   }

   protected EntityType<?> getDefaultType() {
      return defaultType;
   }
}
