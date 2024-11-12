package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WorkAtComposter extends WorkAtPoi {
   private static final List<Item> COMPOSTABLE_ITEMS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);

   protected void useWorkstation(ServerLevel pLevel, Villager pVillager) {
      Optional<GlobalPos> optional = pVillager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      if (optional.isPresent()) {
         GlobalPos globalpos = optional.get();
         BlockState blockstate = pLevel.getBlockState(globalpos.pos());
         if (blockstate.is(Blocks.COMPOSTER)) {
            this.makeBread(pVillager);
            this.compostItems(pLevel, pVillager, globalpos, blockstate);
         }

      }
   }

   private void compostItems(ServerLevel pLevel, Villager pVillager, GlobalPos pGlobal, BlockState pState) {
      BlockPos blockpos = pGlobal.pos();
      if (pState.getValue(ComposterBlock.LEVEL) == 8) {
         pState = ComposterBlock.extractProduce(pVillager, pState, pLevel, blockpos);
      }

      int i = 20;
      int j = 10;
      int[] aint = new int[COMPOSTABLE_ITEMS.size()];
      SimpleContainer simplecontainer = pVillager.getInventory();
      int k = simplecontainer.getContainerSize();
      BlockState blockstate = pState;

      for(int l = k - 1; l >= 0 && i > 0; --l) {
         ItemStack itemstack = simplecontainer.getItem(l);
         int i1 = COMPOSTABLE_ITEMS.indexOf(itemstack.getItem());
         if (i1 != -1) {
            int j1 = itemstack.getCount();
            int k1 = aint[i1] + j1;
            aint[i1] = k1;
            int l1 = Math.min(Math.min(k1 - 10, i), j1);
            if (l1 > 0) {
               i -= l1;

               for(int i2 = 0; i2 < l1; ++i2) {
                  blockstate = ComposterBlock.insertItem(pVillager, blockstate, pLevel, itemstack, blockpos);
                  if (blockstate.getValue(ComposterBlock.LEVEL) == 7) {
                     this.spawnComposterFillEffects(pLevel, pState, blockpos, blockstate);
                     return;
                  }
               }
            }
         }
      }

      this.spawnComposterFillEffects(pLevel, pState, blockpos, blockstate);
   }

   private void spawnComposterFillEffects(ServerLevel pLevel, BlockState pPreState, BlockPos pPos, BlockState pPostState) {
      pLevel.levelEvent(1500, pPos, pPostState != pPreState ? 1 : 0);
   }

   private void makeBread(Villager pVillager) {
      SimpleContainer simplecontainer = pVillager.getInventory();
      if (simplecontainer.countItem(Items.BREAD) <= 36) {
         int i = simplecontainer.countItem(Items.WHEAT);
         int j = 3;
         int k = 3;
         int l = Math.min(3, i / 3);
         if (l != 0) {
            int i1 = l * 3;
            simplecontainer.removeItemType(Items.WHEAT, i1);
            ItemStack itemstack = simplecontainer.addItem(new ItemStack(Items.BREAD, l));
            if (!itemstack.isEmpty()) {
               pVillager.spawnAtLocation(itemstack, 0.5F);
            }

         }
      }
   }
}