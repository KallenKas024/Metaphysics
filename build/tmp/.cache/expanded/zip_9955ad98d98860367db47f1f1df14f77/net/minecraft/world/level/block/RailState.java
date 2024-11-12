package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailState {
   private final Level level;
   private final BlockPos pos;
   private final BaseRailBlock block;
   private BlockState state;
   private final boolean isStraight;
   private final List<BlockPos> connections = Lists.newArrayList();
   private final boolean canMakeSlopes;

   public RailState(Level pLevel, BlockPos pPos, BlockState pState) {
      this.level = pLevel;
      this.pos = pPos;
      this.state = pState;
      this.block = (BaseRailBlock)pState.getBlock();
      RailShape railshape = this.block.getRailDirection(state, pLevel, pPos, null);
      this.isStraight = !this.block.isFlexibleRail(state, pLevel, pPos);
      this.canMakeSlopes = this.block.canMakeSlopes(state, pLevel, pPos);
      this.updateConnections(railshape);
   }

   public List<BlockPos> getConnections() {
      return this.connections;
   }

   private void updateConnections(RailShape pShape) {
      this.connections.clear();
      switch (pShape) {
         case NORTH_SOUTH:
            this.connections.add(this.pos.north());
            this.connections.add(this.pos.south());
            break;
         case EAST_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.east());
            break;
         case ASCENDING_EAST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.east().above());
            break;
         case ASCENDING_WEST:
            this.connections.add(this.pos.west().above());
            this.connections.add(this.pos.east());
            break;
         case ASCENDING_NORTH:
            this.connections.add(this.pos.north().above());
            this.connections.add(this.pos.south());
            break;
         case ASCENDING_SOUTH:
            this.connections.add(this.pos.north());
            this.connections.add(this.pos.south().above());
            break;
         case SOUTH_EAST:
            this.connections.add(this.pos.east());
            this.connections.add(this.pos.south());
            break;
         case SOUTH_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.south());
            break;
         case NORTH_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.north());
            break;
         case NORTH_EAST:
            this.connections.add(this.pos.east());
            this.connections.add(this.pos.north());
      }

   }

   private void removeSoftConnections() {
      for(int i = 0; i < this.connections.size(); ++i) {
         RailState railstate = this.getRail(this.connections.get(i));
         if (railstate != null && railstate.connectsTo(this)) {
            this.connections.set(i, railstate.pos);
         } else {
            this.connections.remove(i--);
         }
      }

   }

   private boolean hasRail(BlockPos pPos) {
      return BaseRailBlock.isRail(this.level, pPos) || BaseRailBlock.isRail(this.level, pPos.above()) || BaseRailBlock.isRail(this.level, pPos.below());
   }

   @Nullable
   private RailState getRail(BlockPos pPos) {
      BlockState blockstate = this.level.getBlockState(pPos);
      if (BaseRailBlock.isRail(blockstate)) {
         return new RailState(this.level, pPos, blockstate);
      } else {
         BlockPos $$1 = pPos.above();
         blockstate = this.level.getBlockState($$1);
         if (BaseRailBlock.isRail(blockstate)) {
            return new RailState(this.level, $$1, blockstate);
         } else {
            $$1 = pPos.below();
            blockstate = this.level.getBlockState($$1);
            return BaseRailBlock.isRail(blockstate) ? new RailState(this.level, $$1, blockstate) : null;
         }
      }
   }

   private boolean connectsTo(RailState pState) {
      return this.hasConnection(pState.pos);
   }

   private boolean hasConnection(BlockPos pPos) {
      for(int i = 0; i < this.connections.size(); ++i) {
         BlockPos blockpos = this.connections.get(i);
         if (blockpos.getX() == pPos.getX() && blockpos.getZ() == pPos.getZ()) {
            return true;
         }
      }

      return false;
   }

   protected int countPotentialConnections() {
      int i = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (this.hasRail(this.pos.relative(direction))) {
            ++i;
         }
      }

      return i;
   }

   private boolean canConnectTo(RailState pState) {
      return this.connectsTo(pState) || this.connections.size() != 2;
   }

   private void connectTo(RailState pState) {
      this.connections.add(pState.pos);
      BlockPos blockpos = this.pos.north();
      BlockPos blockpos1 = this.pos.south();
      BlockPos blockpos2 = this.pos.west();
      BlockPos blockpos3 = this.pos.east();
      boolean flag = this.hasConnection(blockpos);
      boolean flag1 = this.hasConnection(blockpos1);
      boolean flag2 = this.hasConnection(blockpos2);
      boolean flag3 = this.hasConnection(blockpos3);
      RailShape railshape = null;
      if (flag || flag1) {
         railshape = RailShape.NORTH_SOUTH;
      }

      if (flag2 || flag3) {
         railshape = RailShape.EAST_WEST;
      }

      if (!this.isStraight) {
         if (flag1 && flag3 && !flag && !flag2) {
            railshape = RailShape.SOUTH_EAST;
         }

         if (flag1 && flag2 && !flag && !flag3) {
            railshape = RailShape.SOUTH_WEST;
         }

         if (flag && flag2 && !flag1 && !flag3) {
            railshape = RailShape.NORTH_WEST;
         }

         if (flag && flag3 && !flag1 && !flag2) {
            railshape = RailShape.NORTH_EAST;
         }
      }

      if (railshape == RailShape.NORTH_SOUTH && canMakeSlopes) {
         if (BaseRailBlock.isRail(this.level, blockpos.above())) {
            railshape = RailShape.ASCENDING_NORTH;
         }

         if (BaseRailBlock.isRail(this.level, blockpos1.above())) {
            railshape = RailShape.ASCENDING_SOUTH;
         }
      }

      if (railshape == RailShape.EAST_WEST && canMakeSlopes) {
         if (BaseRailBlock.isRail(this.level, blockpos3.above())) {
            railshape = RailShape.ASCENDING_EAST;
         }

         if (BaseRailBlock.isRail(this.level, blockpos2.above())) {
            railshape = RailShape.ASCENDING_WEST;
         }
      }

      if (railshape == null) {
         railshape = RailShape.NORTH_SOUTH;
      }

      if (!this.block.isValidRailShape(railshape)) { // Forge: allow rail block to decide if the new shape is valid
         this.connections.remove(pState.pos);
         return;
      }
      this.state = this.state.setValue(this.block.getShapeProperty(), railshape);
      this.level.setBlock(this.pos, this.state, 3);
   }

   private boolean hasNeighborRail(BlockPos pPos) {
      RailState railstate = this.getRail(pPos);
      if (railstate == null) {
         return false;
      } else {
         railstate.removeSoftConnections();
         return railstate.canConnectTo(this);
      }
   }

   public RailState place(boolean pPowered, boolean pAlwaysPlace, RailShape pShape) {
      BlockPos blockpos = this.pos.north();
      BlockPos blockpos1 = this.pos.south();
      BlockPos blockpos2 = this.pos.west();
      BlockPos blockpos3 = this.pos.east();
      boolean flag = this.hasNeighborRail(blockpos);
      boolean flag1 = this.hasNeighborRail(blockpos1);
      boolean flag2 = this.hasNeighborRail(blockpos2);
      boolean flag3 = this.hasNeighborRail(blockpos3);
      RailShape railshape = null;
      boolean flag4 = flag || flag1;
      boolean flag5 = flag2 || flag3;
      if (flag4 && !flag5) {
         railshape = RailShape.NORTH_SOUTH;
      }

      if (flag5 && !flag4) {
         railshape = RailShape.EAST_WEST;
      }

      boolean flag6 = flag1 && flag3;
      boolean flag7 = flag1 && flag2;
      boolean flag8 = flag && flag3;
      boolean flag9 = flag && flag2;
      if (!this.isStraight) {
         if (flag6 && !flag && !flag2) {
            railshape = RailShape.SOUTH_EAST;
         }

         if (flag7 && !flag && !flag3) {
            railshape = RailShape.SOUTH_WEST;
         }

         if (flag9 && !flag1 && !flag3) {
            railshape = RailShape.NORTH_WEST;
         }

         if (flag8 && !flag1 && !flag2) {
            railshape = RailShape.NORTH_EAST;
         }
      }

      if (railshape == null) {
         if (flag4 && flag5) {
            railshape = pShape;
         } else if (flag4) {
            railshape = RailShape.NORTH_SOUTH;
         } else if (flag5) {
            railshape = RailShape.EAST_WEST;
         }

         if (!this.isStraight) {
            if (pPowered) {
               if (flag6) {
                  railshape = RailShape.SOUTH_EAST;
               }

               if (flag7) {
                  railshape = RailShape.SOUTH_WEST;
               }

               if (flag8) {
                  railshape = RailShape.NORTH_EAST;
               }

               if (flag9) {
                  railshape = RailShape.NORTH_WEST;
               }
            } else {
               if (flag9) {
                  railshape = RailShape.NORTH_WEST;
               }

               if (flag8) {
                  railshape = RailShape.NORTH_EAST;
               }

               if (flag7) {
                  railshape = RailShape.SOUTH_WEST;
               }

               if (flag6) {
                  railshape = RailShape.SOUTH_EAST;
               }
            }
         }
      }

      if (railshape == RailShape.NORTH_SOUTH && canMakeSlopes) {
         if (BaseRailBlock.isRail(this.level, blockpos.above())) {
            railshape = RailShape.ASCENDING_NORTH;
         }

         if (BaseRailBlock.isRail(this.level, blockpos1.above())) {
            railshape = RailShape.ASCENDING_SOUTH;
         }
      }

      if (railshape == RailShape.EAST_WEST && canMakeSlopes) {
         if (BaseRailBlock.isRail(this.level, blockpos3.above())) {
            railshape = RailShape.ASCENDING_EAST;
         }

         if (BaseRailBlock.isRail(this.level, blockpos2.above())) {
            railshape = RailShape.ASCENDING_WEST;
         }
      }

      if (railshape == null || !this.block.isValidRailShape(railshape)) { // Forge: allow rail block to decide if the new shape is valid
         railshape = pShape;
      }

      this.updateConnections(railshape);
      this.state = this.state.setValue(this.block.getShapeProperty(), railshape);
      if (pAlwaysPlace || this.level.getBlockState(this.pos) != this.state) {
         this.level.setBlock(this.pos, this.state, 3);

         for(int i = 0; i < this.connections.size(); ++i) {
            RailState railstate = this.getRail(this.connections.get(i));
            if (railstate != null) {
               railstate.removeSoftConnections();
               if (railstate.canConnectTo(this)) {
                  railstate.connectTo(this);
               }
            }
         }
      }

      return this;
   }

   public BlockState getState() {
      return this.state;
   }
}
