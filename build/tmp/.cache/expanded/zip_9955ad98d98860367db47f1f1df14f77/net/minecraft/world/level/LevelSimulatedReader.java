package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

public interface LevelSimulatedReader {
   boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState);

   boolean isFluidAtPosition(BlockPos pPos, Predicate<FluidState> pPredicate);

   <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pPos, BlockEntityType<T> pType);

   BlockPos getHeightmapPos(Heightmap.Types pHeightmapType, BlockPos pPos);
}