package net.minecraft.world.level;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
   default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pPos, BlockEntityType<T> pBlockEntityType) {
      return LevelReader.super.getBlockEntity(pPos, pBlockEntityType);
   }

   default List<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
      return EntityGetter.super.getEntityCollisions(pEntity, pCollisionBox);
   }

   default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
      return EntityGetter.super.isUnobstructed(pEntity, pShape);
   }

   default BlockPos getHeightmapPos(Heightmap.Types pHeightmapType, BlockPos pPos) {
      return LevelReader.super.getHeightmapPos(pHeightmapType, pPos);
   }
}