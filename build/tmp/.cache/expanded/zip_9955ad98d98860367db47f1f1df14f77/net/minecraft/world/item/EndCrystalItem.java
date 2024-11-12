package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class EndCrystalItem extends Item {
   public EndCrystalItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (!blockstate.is(Blocks.OBSIDIAN) && !blockstate.is(Blocks.BEDROCK)) {
         return InteractionResult.FAIL;
      } else {
         BlockPos blockpos1 = blockpos.above();
         if (!level.isEmptyBlock(blockpos1)) {
            return InteractionResult.FAIL;
         } else {
            double d0 = (double)blockpos1.getX();
            double d1 = (double)blockpos1.getY();
            double d2 = (double)blockpos1.getZ();
            List<Entity> list = level.getEntities((Entity)null, new AABB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));
            if (!list.isEmpty()) {
               return InteractionResult.FAIL;
            } else {
               if (level instanceof ServerLevel) {
                  EndCrystal endcrystal = new EndCrystal(level, d0 + 0.5D, d1, d2 + 0.5D);
                  endcrystal.setShowBottom(false);
                  level.addFreshEntity(endcrystal);
                  level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, blockpos1);
                  EndDragonFight enddragonfight = ((ServerLevel)level).getDragonFight();
                  if (enddragonfight != null) {
                     enddragonfight.tryRespawn();
                  }
               }

               pContext.getItemInHand().shrink(1);
               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         }
      }
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
      return true;
   }
}