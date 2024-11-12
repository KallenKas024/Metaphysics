package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends Block implements EntityBlock, GameMasterBlock {
   public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

   public JigsawBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(ORIENTATION);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(ORIENTATION, pRotation.rotation().rotate(pState.getValue(ORIENTATION)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(ORIENTATION, pMirror.rotation().rotate(pState.getValue(ORIENTATION)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getClickedFace();
      Direction direction1;
      if (direction.getAxis() == Direction.Axis.Y) {
         direction1 = pContext.getHorizontalDirection().getOpposite();
      } else {
         direction1 = Direction.UP;
      }

      return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction1));
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new JigsawBlockEntity(pPos, pState);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof JigsawBlockEntity && pPlayer.canUseGameMasterBlocks()) {
         pPlayer.openJigsawBlock((JigsawBlockEntity)blockentity);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static boolean canAttach(StructureTemplate.StructureBlockInfo pInfo, StructureTemplate.StructureBlockInfo pInfo2) {
      Direction direction = getFrontFacing(pInfo.state());
      Direction direction1 = getFrontFacing(pInfo2.state());
      Direction direction2 = getTopFacing(pInfo.state());
      Direction direction3 = getTopFacing(pInfo2.state());
      JigsawBlockEntity.JointType jigsawblockentity$jointtype = JigsawBlockEntity.JointType.byName(pInfo.nbt().getString("joint")).orElseGet(() -> {
         return direction.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE;
      });
      boolean flag = jigsawblockentity$jointtype == JigsawBlockEntity.JointType.ROLLABLE;
      return direction == direction1.getOpposite() && (flag || direction2 == direction3) && pInfo.nbt().getString("target").equals(pInfo2.nbt().getString("name"));
   }

   /**
    * This represents the face that the puzzle piece is on. To connect: 2 jigsaws must have their puzzle piece face
    * facing each other.
    */
   public static Direction getFrontFacing(BlockState pState) {
      return pState.getValue(ORIENTATION).front();
   }

   /**
    * This represents the face that the line connector is on. To connect, if the OrientationType is ALIGNED, the two
    * lines must be in the same direction. (Their textures will form one straight line)
    */
   public static Direction getTopFacing(BlockState pState) {
      return pState.getValue(ORIENTATION).top();
   }
}