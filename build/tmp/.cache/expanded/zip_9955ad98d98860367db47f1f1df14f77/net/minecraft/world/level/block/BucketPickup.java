package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface BucketPickup extends net.minecraftforge.common.extensions.IForgeBucketPickup {
   ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState);

   @Deprecated//Forge: Use state sensitive variant instead
   Optional<SoundEvent> getPickupSound();
}
