package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
   public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
   public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
   protected static final int HEIGHT = 9;
   protected static final VoxelShape BASE = Block.box(0.0D, 3.0D, 0.0D, 16.0D, 9.0D, 16.0D);
   private static final int LEG_WIDTH = 3;
   protected static final VoxelShape LEG_NORTH_WEST = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 3.0D, 3.0D);
   protected static final VoxelShape LEG_SOUTH_WEST = Block.box(0.0D, 0.0D, 13.0D, 3.0D, 3.0D, 16.0D);
   protected static final VoxelShape LEG_NORTH_EAST = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 3.0D, 3.0D);
   protected static final VoxelShape LEG_SOUTH_EAST = Block.box(13.0D, 0.0D, 13.0D, 16.0D, 3.0D, 16.0D);
   protected static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_NORTH_EAST);
   protected static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, LEG_SOUTH_WEST, LEG_SOUTH_EAST);
   protected static final VoxelShape WEST_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
   protected static final VoxelShape EAST_SHAPE = Shapes.or(BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
   private final DyeColor color;

   public BedBlock(DyeColor pColor, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
      this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, Boolean.valueOf(false)));
   }

   @Nullable
   public static Direction getBedOrientation(BlockGetter pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return blockstate.getBlock() instanceof BedBlock ? blockstate.getValue(FACING) : null;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.CONSUME;
      } else {
         if (pState.getValue(PART) != BedPart.HEAD) {
            pPos = pPos.relative(pState.getValue(FACING));
            pState = pLevel.getBlockState(pPos);
            if (!pState.is(this)) {
               return InteractionResult.CONSUME;
            }
         }

         if (!canSetSpawn(pLevel)) {
            pLevel.removeBlock(pPos, false);
            BlockPos blockpos = pPos.relative(pState.getValue(FACING).getOpposite());
            if (pLevel.getBlockState(blockpos).is(this)) {
               pLevel.removeBlock(blockpos, false);
            }

            Vec3 vec3 = pPos.getCenter();
            pLevel.explode((Entity)null, pLevel.damageSources().badRespawnPointExplosion(vec3), (ExplosionDamageCalculator)null, vec3, 5.0F, true, Level.ExplosionInteraction.BLOCK);
            return InteractionResult.SUCCESS;
         } else if (pState.getValue(OCCUPIED)) {
            if (!this.kickVillagerOutOfBed(pLevel, pPos)) {
               pPlayer.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }

            return InteractionResult.SUCCESS;
         } else {
            pPlayer.startSleepInBed(pPos).ifLeft((p_49477_) -> {
               if (p_49477_.getMessage() != null) {
                  pPlayer.displayClientMessage(p_49477_.getMessage(), true);
               }

            });
            return InteractionResult.SUCCESS;
         }
      }
   }

   public static boolean canSetSpawn(Level pLevel) {
      return pLevel.dimensionType().bedWorks();
   }

   private boolean kickVillagerOutOfBed(Level pLevel, BlockPos pPos) {
      List<Villager> list = pLevel.getEntitiesOfClass(Villager.class, new AABB(pPos), LivingEntity::isSleeping);
      if (list.isEmpty()) {
         return false;
      } else {
         list.get(0).stopSleeping();
         return true;
      }
   }

   public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
      super.fallOn(pLevel, pState, pPos, pEntity, pFallDistance * 0.5F);
   }

   /**
    * Called when an Entity lands on this Block.
    * This method is responsible for doing any modification on the motion of the entity that should result from the
    * landing.
    */
   public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
      if (pEntity.isSuppressingBounce()) {
         super.updateEntityAfterFallOn(pLevel, pEntity);
      } else {
         this.bounceUp(pEntity);
      }

   }

   private void bounceUp(Entity pEntity) {
      Vec3 vec3 = pEntity.getDeltaMovement();
      if (vec3.y < 0.0D) {
         double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
         pEntity.setDeltaMovement(vec3.x, -vec3.y * (double)0.66F * d0, vec3.z);
      }

   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == getNeighbourDirection(pState.getValue(PART), pState.getValue(FACING))) {
         return pFacingState.is(this) && pFacingState.getValue(PART) != pState.getValue(PART) ? pState.setValue(OCCUPIED, pFacingState.getValue(OCCUPIED)) : Blocks.AIR.defaultBlockState();
      } else {
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   /**
    * Given a bed part and the direction it's facing, find the direction to move to get the other bed part
    */
   private static Direction getNeighbourDirection(BedPart pPart, Direction pDirection) {
      return pPart == BedPart.FOOT ? pDirection : pDirection.getOpposite();
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide && pPlayer.isCreative()) {
         BedPart bedpart = pState.getValue(PART);
         if (bedpart == BedPart.FOOT) {
            BlockPos blockpos = pPos.relative(getNeighbourDirection(bedpart, pState.getValue(FACING)));
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (blockstate.is(this) && blockstate.getValue(PART) == BedPart.HEAD) {
               pLevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
               pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
            }
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getHorizontalDirection();
      BlockPos blockpos = pContext.getClickedPos();
      BlockPos blockpos1 = blockpos.relative(direction);
      Level level = pContext.getLevel();
      return level.getBlockState(blockpos1).canBeReplaced(pContext) && level.getWorldBorder().isWithinBounds(blockpos1) ? this.defaultBlockState().setValue(FACING, direction) : null;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Direction direction = getConnectedDirection(pState).getOpposite();
      switch (direction) {
         case NORTH:
            return NORTH_SHAPE;
         case SOUTH:
            return SOUTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         default:
            return EAST_SHAPE;
      }
   }

   public static Direction getConnectedDirection(BlockState pState) {
      Direction direction = pState.getValue(FACING);
      return pState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
   }

   public static DoubleBlockCombiner.BlockType getBlockType(BlockState pState) {
      BedPart bedpart = pState.getValue(PART);
      return bedpart == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
   }

   private static boolean isBunkBed(BlockGetter pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos.below()).getBlock() instanceof BedBlock;
   }

   public static Optional<Vec3> findStandUpPosition(EntityType<?> pEntityType, CollisionGetter pCollisionGetter, BlockPos pPos, Direction pDirection, float pYRot) {
      Direction direction = pDirection.getClockWise();
      Direction direction1 = direction.isFacingAngle(pYRot) ? direction.getOpposite() : direction;
      if (isBunkBed(pCollisionGetter, pPos)) {
         return findBunkBedStandUpPosition(pEntityType, pCollisionGetter, pPos, pDirection, direction1);
      } else {
         int[][] aint = bedStandUpOffsets(pDirection, direction1);
         Optional<Vec3> optional = findStandUpPositionAtOffset(pEntityType, pCollisionGetter, pPos, aint, true);
         return optional.isPresent() ? optional : findStandUpPositionAtOffset(pEntityType, pCollisionGetter, pPos, aint, false);
      }
   }

   private static Optional<Vec3> findBunkBedStandUpPosition(EntityType<?> pEntityType, CollisionGetter pCollisionGetter, BlockPos pPos, Direction pStateFacing, Direction pEntityFacing) {
      int[][] aint = bedSurroundStandUpOffsets(pStateFacing, pEntityFacing);
      Optional<Vec3> optional = findStandUpPositionAtOffset(pEntityType, pCollisionGetter, pPos, aint, true);
      if (optional.isPresent()) {
         return optional;
      } else {
         BlockPos blockpos = pPos.below();
         Optional<Vec3> optional1 = findStandUpPositionAtOffset(pEntityType, pCollisionGetter, blockpos, aint, true);
         if (optional1.isPresent()) {
            return optional1;
         } else {
            int[][] aint1 = bedAboveStandUpOffsets(pStateFacing);
            Optional<Vec3> optional2 = findStandUpPositionAtOffset(pEntityType, pCollisionGetter, pPos, aint1, true);
            if (optional2.isPresent()) {
               return optional2;
            } else {
               Optional<Vec3> optional3 = findStandUpPositionAtOffset(pEntityType, pCollisionGetter, pPos, aint, false);
               if (optional3.isPresent()) {
                  return optional3;
               } else {
                  Optional<Vec3> optional4 = findStandUpPositionAtOffset(pEntityType, pCollisionGetter, blockpos, aint, false);
                  return optional4.isPresent() ? optional4 : findStandUpPositionAtOffset(pEntityType, pCollisionGetter, pPos, aint1, false);
               }
            }
         }
      }
   }

   private static Optional<Vec3> findStandUpPositionAtOffset(EntityType<?> pEntityType, CollisionGetter pCollisionGetter, BlockPos pPos, int[][] pOffsets, boolean pSimulate) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int[] aint : pOffsets) {
         blockpos$mutableblockpos.set(pPos.getX() + aint[0], pPos.getY(), pPos.getZ() + aint[1]);
         Vec3 vec3 = DismountHelper.findSafeDismountLocation(pEntityType, pCollisionGetter, blockpos$mutableblockpos, pSimulate);
         if (vec3 != null) {
            return Optional.of(vec3);
         }
      }

      return Optional.empty();
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, PART, OCCUPIED);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BedBlockEntity(pPos, pState, this.color);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      if (!pLevel.isClientSide) {
         BlockPos blockpos = pPos.relative(pState.getValue(FACING));
         pLevel.setBlock(blockpos, pState.setValue(PART, BedPart.HEAD), 3);
         pLevel.blockUpdated(pPos, Blocks.AIR);
         pState.updateNeighbourShapes(pLevel, pPos, 3);
      }

   }

   public DyeColor getColor() {
      return this.color;
   }

   /**
    * Return a random long to be passed to {@link net.minecraft.client.resources.model.BakedModel#getQuads}, used for
    * random model rotations
    */
   public long getSeed(BlockState pState, BlockPos pPos) {
      BlockPos blockpos = pPos.relative(pState.getValue(FACING), pState.getValue(PART) == BedPart.HEAD ? 0 : 1);
      return Mth.getSeed(blockpos.getX(), pPos.getY(), blockpos.getZ());
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   private static int[][] bedStandUpOffsets(Direction pFirstDir, Direction pSecondDir) {
      return ArrayUtils.addAll((int[][])bedSurroundStandUpOffsets(pFirstDir, pSecondDir), (int[][])bedAboveStandUpOffsets(pFirstDir));
   }

   private static int[][] bedSurroundStandUpOffsets(Direction pFirstDir, Direction pSecondDir) {
      return new int[][]{{pSecondDir.getStepX(), pSecondDir.getStepZ()}, {pSecondDir.getStepX() - pFirstDir.getStepX(), pSecondDir.getStepZ() - pFirstDir.getStepZ()}, {pSecondDir.getStepX() - pFirstDir.getStepX() * 2, pSecondDir.getStepZ() - pFirstDir.getStepZ() * 2}, {-pFirstDir.getStepX() * 2, -pFirstDir.getStepZ() * 2}, {-pSecondDir.getStepX() - pFirstDir.getStepX() * 2, -pSecondDir.getStepZ() - pFirstDir.getStepZ() * 2}, {-pSecondDir.getStepX() - pFirstDir.getStepX(), -pSecondDir.getStepZ() - pFirstDir.getStepZ()}, {-pSecondDir.getStepX(), -pSecondDir.getStepZ()}, {-pSecondDir.getStepX() + pFirstDir.getStepX(), -pSecondDir.getStepZ() + pFirstDir.getStepZ()}, {pFirstDir.getStepX(), pFirstDir.getStepZ()}, {pSecondDir.getStepX() + pFirstDir.getStepX(), pSecondDir.getStepZ() + pFirstDir.getStepZ()}};
   }

   private static int[][] bedAboveStandUpOffsets(Direction pDir) {
      return new int[][]{{0, 0}, {-pDir.getStepX(), -pDir.getStepZ()}};
   }
}