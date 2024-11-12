package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface DispensibleContainerItem extends net.minecraftforge.common.extensions.IForgeDispensibleContainerItem {
   default void checkExtraContent(@Nullable Player pPlayer, Level pLevel, ItemStack pContainerStack, BlockPos pPos) {
   }

   @Deprecated //Forge: use the ItemStack sensitive version
   boolean emptyContents(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, @Nullable BlockHitResult pResult);
}
