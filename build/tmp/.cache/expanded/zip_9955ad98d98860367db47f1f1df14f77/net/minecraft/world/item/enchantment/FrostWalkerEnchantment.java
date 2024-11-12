package net.minecraft.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FrostWalkerEnchantment extends Enchantment {
   public FrostWalkerEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_FEET, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return pEnchantmentLevel * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 15;
   }

   /**
    * Checks if the enchantment should be considered a treasure enchantment. These enchantments can not be obtained
    * using the enchantment table. The mending enchantment is an example of a treasure enchantment.
    * @return Whether the enchantment is a treasure enchantment.
    */
   public boolean isTreasureOnly() {
      return true;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 2;
   }

   public static void onEntityMoved(LivingEntity pLiving, Level pLevel, BlockPos pPos, int pLevelConflicting) {
      if (pLiving.onGround()) {
         BlockState blockstate = Blocks.FROSTED_ICE.defaultBlockState();
         int i = Math.min(16, 2 + pLevelConflicting);
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-i, -1, -i), pPos.offset(i, -1, i))) {
            if (blockpos.closerToCenterThan(pLiving.position(), (double)i)) {
               blockpos$mutableblockpos.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
               BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);
               if (blockstate1.isAir()) {
                  BlockState blockstate2 = pLevel.getBlockState(blockpos);
                  if (blockstate2 == FrostedIceBlock.meltsInto() && blockstate.canSurvive(pLevel, blockpos) && pLevel.isUnobstructed(blockstate, blockpos, CollisionContext.empty()) && !net.minecraftforge.event.ForgeEventFactory.onBlockPlace(pLiving, net.minecraftforge.common.util.BlockSnapshot.create(pLevel.dimension(), pLevel, blockpos), net.minecraft.core.Direction.UP)) {
                     pLevel.setBlockAndUpdate(blockpos, blockstate);
                     pLevel.scheduleTick(blockpos, Blocks.FROSTED_ICE, Mth.nextInt(pLiving.getRandom(), 60, 120));
                  }
               }
            }
         }

      }
   }

   /**
    * Determines if the enchantment passed can be applied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.DEPTH_STRIDER;
   }
}
