package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.OptionalBox;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class InteractWithDoor {
   private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
   private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 3.0D;
   private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0D;

   public static BehaviorControl<LivingEntity> create() {
      MutableObject<Node> mutableobject = new MutableObject<>((Node)null);
      MutableInt mutableint = new MutableInt(0);
      return BehaviorBuilder.create((p_258474_) -> {
         return p_258474_.group(p_258474_.present(MemoryModuleType.PATH), p_258474_.registered(MemoryModuleType.DOORS_TO_CLOSE), p_258474_.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply(p_258474_, (p_258460_, p_258461_, p_258462_) -> {
            return (p_258469_, p_258470_, p_258471_) -> {
               Path path = p_258474_.get(p_258460_);
               Optional<Set<GlobalPos>> optional = p_258474_.tryGet(p_258461_);
               if (!path.notStarted() && !path.isDone()) {
                  if (Objects.equals(mutableobject.getValue(), path.getNextNode())) {
                     mutableint.setValue(20);
                  } else if (mutableint.decrementAndGet() > 0) {
                     return false;
                  }

                  mutableobject.setValue(path.getNextNode());
                  Node node = path.getPreviousNode();
                  Node node1 = path.getNextNode();
                  BlockPos blockpos = node.asBlockPos();
                  BlockState blockstate = p_258469_.getBlockState(blockpos);
                  if (blockstate.is(BlockTags.WOODEN_DOORS, (p_201959_) -> {
                     return p_201959_.getBlock() instanceof DoorBlock;
                  })) {
                     DoorBlock doorblock = (DoorBlock)blockstate.getBlock();
                     if (!doorblock.isOpen(blockstate)) {
                        doorblock.setOpen(p_258470_, p_258469_, blockstate, blockpos, true);
                     }

                     optional = rememberDoorToClose(p_258461_, optional, p_258469_, blockpos);
                  }

                  BlockPos blockpos1 = node1.asBlockPos();
                  BlockState blockstate1 = p_258469_.getBlockState(blockpos1);
                  if (blockstate1.is(BlockTags.WOODEN_DOORS, (p_201957_) -> {
                     return p_201957_.getBlock() instanceof DoorBlock;
                  })) {
                     DoorBlock doorblock1 = (DoorBlock)blockstate1.getBlock();
                     if (!doorblock1.isOpen(blockstate1)) {
                        doorblock1.setOpen(p_258470_, p_258469_, blockstate1, blockpos1, true);
                        optional = rememberDoorToClose(p_258461_, optional, p_258469_, blockpos1);
                     }
                  }

                  optional.ifPresent((p_258452_) -> {
                     closeDoorsThatIHaveOpenedOrPassedThrough(p_258469_, p_258470_, node, node1, p_258452_, p_258474_.tryGet(p_258462_));
                  });
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }

   public static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel pLevel, LivingEntity pEntity, @Nullable Node pPrevious, @Nullable Node pNext, Set<GlobalPos> pDoorPositions, Optional<List<LivingEntity>> pNearestLivingEntities) {
      Iterator<GlobalPos> iterator = pDoorPositions.iterator();

      while(iterator.hasNext()) {
         GlobalPos globalpos = iterator.next();
         BlockPos blockpos = globalpos.pos();
         if ((pPrevious == null || !pPrevious.asBlockPos().equals(blockpos)) && (pNext == null || !pNext.asBlockPos().equals(blockpos))) {
            if (isDoorTooFarAway(pLevel, pEntity, globalpos)) {
               iterator.remove();
            } else {
               BlockState blockstate = pLevel.getBlockState(blockpos);
               if (!blockstate.is(BlockTags.WOODEN_DOORS, (p_201952_) -> {
                  return p_201952_.getBlock() instanceof DoorBlock;
               })) {
                  iterator.remove();
               } else {
                  DoorBlock doorblock = (DoorBlock)blockstate.getBlock();
                  if (!doorblock.isOpen(blockstate)) {
                     iterator.remove();
                  } else if (areOtherMobsComingThroughDoor(pEntity, blockpos, pNearestLivingEntities)) {
                     iterator.remove();
                  } else {
                     doorblock.setOpen(pEntity, pLevel, blockstate, blockpos, false);
                     iterator.remove();
                  }
               }
            }
         }
      }

   }

   private static boolean areOtherMobsComingThroughDoor(LivingEntity pEntity, BlockPos pPos, Optional<List<LivingEntity>> pNearestLivingEntities) {
      return pNearestLivingEntities.isEmpty() ? false : pNearestLivingEntities.get().stream().filter((p_289329_) -> {
         return p_289329_.getType() == pEntity.getType();
      }).filter((p_289331_) -> {
         return pPos.closerToCenterThan(p_289331_.position(), 2.0D);
      }).anyMatch((p_258454_) -> {
         return isMobComingThroughDoor(p_258454_.getBrain(), pPos);
      });
   }

   private static boolean isMobComingThroughDoor(Brain<?> pBrain, BlockPos pPos) {
      if (!pBrain.hasMemoryValue(MemoryModuleType.PATH)) {
         return false;
      } else {
         Path path = pBrain.getMemory(MemoryModuleType.PATH).get();
         if (path.isDone()) {
            return false;
         } else {
            Node node = path.getPreviousNode();
            if (node == null) {
               return false;
            } else {
               Node node1 = path.getNextNode();
               return pPos.equals(node.asBlockPos()) || pPos.equals(node1.asBlockPos());
            }
         }
      }
   }

   private static boolean isDoorTooFarAway(ServerLevel pLevel, LivingEntity pEntity, GlobalPos pPos) {
      return pPos.dimension() != pLevel.dimension() || !pPos.pos().closerToCenterThan(pEntity.position(), 3.0D);
   }

   private static Optional<Set<GlobalPos>> rememberDoorToClose(MemoryAccessor<OptionalBox.Mu, Set<GlobalPos>> pDoorsToClose, Optional<Set<GlobalPos>> pDoorPositions, ServerLevel pLevel, BlockPos pPos) {
      GlobalPos globalpos = GlobalPos.of(pLevel.dimension(), pPos);
      return Optional.of(pDoorPositions.map((p_261437_) -> {
         p_261437_.add(globalpos);
         return p_261437_;
      }).orElseGet(() -> {
         Set<GlobalPos> set = Sets.newHashSet(globalpos);
         pDoorsToClose.set(set);
         return set;
      }));
   }
}