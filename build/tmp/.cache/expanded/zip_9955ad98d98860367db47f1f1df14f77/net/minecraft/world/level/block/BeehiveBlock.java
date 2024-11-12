package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeehiveBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;
   public static final int MAX_HONEY_LEVELS = 5;
   private static final int SHEARED_HONEYCOMB_COUNT = 3;

   public BeehiveBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HONEY_LEVEL, Integer.valueOf(0)).setValue(FACING, Direction.NORTH));
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return pBlockState.getValue(HONEY_LEVEL);
   }
   // Forge: Fixed MC-227255 Beehives and bee nests do not rotate/mirror correctly in structure blocks
   @Override public BlockState rotate(BlockState blockState, net.minecraft.world.level.block.Rotation rotation) { return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING))); }
   @Override public BlockState mirror(BlockState blockState, net.minecraft.world.level.block.Mirror mirror) { return blockState.rotate(mirror.getRotation(blockState.getValue(FACING))); }

   /**
    * Called after a player has successfully harvested this block. This method will only be called if the player has
    * used the correct tool and drops should be spawned.
    */
   public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pTe, ItemStack pStack) {
      super.playerDestroy(pLevel, pPlayer, pPos, pState, pTe, pStack);
      if (!pLevel.isClientSide && pTe instanceof BeehiveBlockEntity beehiveblockentity) {
         if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pStack) == 0) {
            beehiveblockentity.emptyAllLivingFromHive(pPlayer, pState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            pLevel.updateNeighbourForOutputSignal(pPos, this);
            this.angerNearbyBees(pLevel, pPos);
         }

         CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)pPlayer, pState, pStack, beehiveblockentity.getOccupantCount());
      }

   }

   private void angerNearbyBees(Level pLevel, BlockPos pPos) {
      List<Bee> list = pLevel.getEntitiesOfClass(Bee.class, (new AABB(pPos)).inflate(8.0D, 6.0D, 8.0D));
      if (!list.isEmpty()) {
         List<Player> list1 = pLevel.getEntitiesOfClass(Player.class, (new AABB(pPos)).inflate(8.0D, 6.0D, 8.0D));
         if (list1.isEmpty()) return; //Forge: Prevent Error when no players are around.
         int i = list1.size();

         for(Bee bee : list) {
            if (bee.getTarget() == null) {
               bee.setTarget(list1.get(pLevel.random.nextInt(i)));
            }
         }
      }

   }

   public static void dropHoneycomb(Level pLevel, BlockPos pPos) {
      popResource(pLevel, pPos, new ItemStack(Items.HONEYCOMB, 3));
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      int i = pState.getValue(HONEY_LEVEL);
      boolean flag = false;
      if (i >= 5) {
         Item item = itemstack.getItem();
         if (itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SHEARS_HARVEST)) {
            pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            dropHoneycomb(pLevel, pPos);
            itemstack.hurtAndBreak(1, pPlayer, (p_49571_) -> {
               p_49571_.broadcastBreakEvent(pHand);
            });
            flag = true;
            pLevel.gameEvent(pPlayer, GameEvent.SHEAR, pPos);
         } else if (itemstack.is(Items.GLASS_BOTTLE)) {
            itemstack.shrink(1);
            pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (itemstack.isEmpty()) {
               pPlayer.setItemInHand(pHand, new ItemStack(Items.HONEY_BOTTLE));
            } else if (!pPlayer.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
               pPlayer.drop(new ItemStack(Items.HONEY_BOTTLE), false);
            }

            flag = true;
            pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, pPos);
         }

         if (!pLevel.isClientSide() && flag) {
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
         }
      }

      if (flag) {
         if (!CampfireBlock.isSmokeyPos(pLevel, pPos)) {
            if (this.hiveContainsBees(pLevel, pPos)) {
               this.angerNearbyBees(pLevel, pPos);
            }

            this.releaseBeesAndResetHoneyLevel(pLevel, pState, pPos, pPlayer, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
         } else {
            this.resetHoneyLevel(pLevel, pState, pPos);
         }

         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
      }
   }

   private boolean hiveContainsBees(Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
         return !beehiveblockentity.isEmpty();
      } else {
         return false;
      }
   }

   public void releaseBeesAndResetHoneyLevel(Level pLevel, BlockState pState, BlockPos pPos, @Nullable Player pPlayer, BeehiveBlockEntity.BeeReleaseStatus pBeeReleaseStatus) {
      this.resetHoneyLevel(pLevel, pState, pPos);
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
         beehiveblockentity.emptyAllLivingFromHive(pPlayer, pState, pBeeReleaseStatus);
      }

   }

   public void resetHoneyLevel(Level pLevel, BlockState pState, BlockPos pPos) {
      pLevel.setBlock(pPos, pState.setValue(HONEY_LEVEL, Integer.valueOf(0)), 3);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(HONEY_LEVEL) >= 5) {
         for(int i = 0; i < pRandom.nextInt(1) + 1; ++i) {
            this.trySpawnDripParticles(pLevel, pPos, pState);
         }
      }

   }

   private void trySpawnDripParticles(Level pLevel, BlockPos pPos, BlockState pState) {
      if (pState.getFluidState().isEmpty() && !(pLevel.random.nextFloat() < 0.3F)) {
         VoxelShape voxelshape = pState.getCollisionShape(pLevel, pPos);
         double d0 = voxelshape.max(Direction.Axis.Y);
         if (d0 >= 1.0D && !pState.is(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.min(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(pLevel, pPos, voxelshape, (double)pPos.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos = pPos.below();
               BlockState blockstate = pLevel.getBlockState(blockpos);
               VoxelShape voxelshape1 = blockstate.getCollisionShape(pLevel, blockpos);
               double d2 = voxelshape1.max(Direction.Axis.Y);
               if ((d2 < 1.0D || !blockstate.isCollisionShapeFullBlock(pLevel, blockpos)) && blockstate.getFluidState().isEmpty()) {
                  this.spawnParticle(pLevel, pPos, voxelshape, (double)pPos.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void spawnParticle(Level pLevel, BlockPos pPos, VoxelShape pShape, double pY) {
      this.spawnFluidParticle(pLevel, (double)pPos.getX() + pShape.min(Direction.Axis.X), (double)pPos.getX() + pShape.max(Direction.Axis.X), (double)pPos.getZ() + pShape.min(Direction.Axis.Z), (double)pPos.getZ() + pShape.max(Direction.Axis.Z), pY);
   }

   private void spawnFluidParticle(Level pParticleData, double pX1, double pX2, double pZ1, double pZ2, double pY) {
      pParticleData.addParticle(ParticleTypes.DRIPPING_HONEY, Mth.lerp(pParticleData.random.nextDouble(), pX1, pX2), pY, Mth.lerp(pParticleData.random.nextDouble(), pZ1, pZ2), 0.0D, 0.0D, 0.0D);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HONEY_LEVEL, FACING);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BeehiveBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide && pPlayer.isCreative() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
            ItemStack itemstack = new ItemStack(this);
            int i = pState.getValue(HONEY_LEVEL);
            boolean flag = !beehiveblockentity.isEmpty();
            if (flag || i > 0) {
               if (flag) {
                  CompoundTag compoundtag = new CompoundTag();
                  compoundtag.put("Bees", beehiveblockentity.writeBees());
                  BlockItem.setBlockEntityData(itemstack, BlockEntityType.BEEHIVE, compoundtag);
               }

               CompoundTag compoundtag1 = new CompoundTag();
               compoundtag1.putInt("honey_level", i);
               itemstack.addTagElement("BlockStateTag", compoundtag1);
               ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), itemstack);
               itementity.setDefaultPickUpDelay();
               pLevel.addFreshEntity(itementity);
            }
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
      Entity entity = pParams.getOptionalParameter(LootContextParams.THIS_ENTITY);
      if (entity instanceof PrimedTnt || entity instanceof Creeper || entity instanceof WitherSkull || entity instanceof WitherBoss || entity instanceof MinecartTNT) {
         BlockEntity blockentity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         if (blockentity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
            beehiveblockentity.emptyAllLivingFromHive((Player)null, pState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
         }
      }

      return super.getDrops(pState, pParams);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pLevel.getBlockState(pFacingPos).getBlock() instanceof FireBlock) {
         BlockEntity blockentity = pLevel.getBlockEntity(pCurrentPos);
         if (blockentity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
            beehiveblockentity.emptyAllLivingFromHive((Player)null, pState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
         }
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }
}
