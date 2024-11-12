package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HarvestFarmland extends Behavior<Villager> {
   private static final int HARVEST_DURATION = 200;
   public static final float SPEED_MODIFIER = 0.5F;
   @Nullable
   private BlockPos aboveFarmlandPos;
   private long nextOkStartTime;
   private int timeWorkedSoFar;
   private final List<BlockPos> validFarmlandAroundVillager = Lists.newArrayList();

   public HarvestFarmland() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SECONDARY_JOB_SITE, MemoryStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, pOwner)) {
         return false;
      } else if (pOwner.getVillagerData().getProfession() != VillagerProfession.FARMER) {
         return false;
      } else {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = pOwner.blockPosition().mutable();
         this.validFarmlandAroundVillager.clear();

         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  blockpos$mutableblockpos.set(pOwner.getX() + (double)i, pOwner.getY() + (double)j, pOwner.getZ() + (double)k);
                  if (this.validPos(blockpos$mutableblockpos, pLevel)) {
                     this.validFarmlandAroundVillager.add(new BlockPos(blockpos$mutableblockpos));
                  }
               }
            }
         }

         this.aboveFarmlandPos = this.getValidFarmland(pLevel);
         return this.aboveFarmlandPos != null;
      }
   }

   @Nullable
   private BlockPos getValidFarmland(ServerLevel pServerLevel) {
      return this.validFarmlandAroundVillager.isEmpty() ? null : this.validFarmlandAroundVillager.get(pServerLevel.getRandom().nextInt(this.validFarmlandAroundVillager.size()));
   }

   private boolean validPos(BlockPos pPos, ServerLevel pServerLevel) {
      BlockState blockstate = pServerLevel.getBlockState(pPos);
      Block block = blockstate.getBlock();
      Block block1 = pServerLevel.getBlockState(pPos.below()).getBlock();
      return block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockstate) || blockstate.isAir() && block1 instanceof FarmBlock;
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      if (pGameTime > this.nextOkStartTime && this.aboveFarmlandPos != null) {
         pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
         pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
      }

   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      this.timeWorkedSoFar = 0;
      this.nextOkStartTime = pGameTime + 40L;
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerToCenterThan(pOwner.position(), 1.0D)) {
         if (this.aboveFarmlandPos != null && pGameTime > this.nextOkStartTime) {
            BlockState blockstate = pLevel.getBlockState(this.aboveFarmlandPos);
            Block block = blockstate.getBlock();
            Block block1 = pLevel.getBlockState(this.aboveFarmlandPos.below()).getBlock();
            if (block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockstate)) {
               pLevel.destroyBlock(this.aboveFarmlandPos, true, pOwner);
            }

            if (blockstate.isAir() && block1 instanceof FarmBlock && pOwner.hasFarmSeeds()) {
               SimpleContainer simplecontainer = pOwner.getInventory();

               for(int i = 0; i < simplecontainer.getContainerSize(); ++i) {
                  ItemStack itemstack = simplecontainer.getItem(i);
                  boolean flag = false;
                  if (!itemstack.isEmpty() && itemstack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
                     Item $$11 = itemstack.getItem();
                     if ($$11 instanceof BlockItem) {
                        BlockItem blockitem = (BlockItem)$$11;
                        BlockState blockstate1 = blockitem.getBlock().defaultBlockState();
                        pLevel.setBlockAndUpdate(this.aboveFarmlandPos, blockstate1);
                        pLevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(pOwner, blockstate1));
                        flag = true;
                     } else if (itemstack.getItem() instanceof net.minecraftforge.common.IPlantable) {
                        if (((net.minecraftforge.common.IPlantable)itemstack.getItem()).getPlantType(pLevel, aboveFarmlandPos) == net.minecraftforge.common.PlantType.CROP) {
                           pLevel.setBlock(aboveFarmlandPos, ((net.minecraftforge.common.IPlantable)itemstack.getItem()).getPlant(pLevel, aboveFarmlandPos), 3);
                           flag = true;
                        }
                     }
                  }

                  if (flag) {
                     pLevel.playSound((Player)null, (double)this.aboveFarmlandPos.getX(), (double)this.aboveFarmlandPos.getY(), (double)this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                     itemstack.shrink(1);
                     if (itemstack.isEmpty()) {
                        simplecontainer.setItem(i, ItemStack.EMPTY);
                     }
                     break;
                  }
               }
            }

            if (block instanceof CropBlock && !((CropBlock)block).isMaxAge(blockstate)) {
               this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
               this.aboveFarmlandPos = this.getValidFarmland(pLevel);
               if (this.aboveFarmlandPos != null) {
                  this.nextOkStartTime = pGameTime + 20L;
                  pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                  pOwner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
               }
            }
         }

         ++this.timeWorkedSoFar;
      }
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return this.timeWorkedSoFar < 200;
   }
}
