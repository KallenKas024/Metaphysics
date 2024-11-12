package net.minecraft.util;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
   public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> pEntityType, MobSpawnType pSpawnType, ServerLevel pLevel, BlockPos pPos, int pAttempts, int p_216409_, int pYOffset, SpawnUtil.Strategy pStrategy) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

      for(int i = 0; i < pAttempts; ++i) {
         int j = Mth.randomBetweenInclusive(pLevel.random, -p_216409_, p_216409_);
         int k = Mth.randomBetweenInclusive(pLevel.random, -p_216409_, p_216409_);
         blockpos$mutableblockpos.setWithOffset(pPos, j, pYOffset, k);
         if (pLevel.getWorldBorder().isWithinBounds(blockpos$mutableblockpos) && moveToPossibleSpawnPosition(pLevel, pYOffset, blockpos$mutableblockpos, pStrategy)) {
            T t = pEntityType.create(pLevel, (CompoundTag)null, (Consumer<T>)null, blockpos$mutableblockpos, pSpawnType, false, false);
            if (t != null) {
               if (net.minecraftforge.event.ForgeEventFactory.checkSpawnPosition(t, pLevel, pSpawnType)) {
                  pLevel.addFreshEntityWithPassengers(t);
                  return Optional.of(t);
               }

               t.discard();
            }
         }
      }

      return Optional.empty();
   }

   private static boolean moveToPossibleSpawnPosition(ServerLevel pLevel, int pYOffset, BlockPos.MutableBlockPos pPos, SpawnUtil.Strategy pStrategy) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = (new BlockPos.MutableBlockPos()).set(pPos);
      BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);

      for(int i = pYOffset; i >= -pYOffset; --i) {
         pPos.move(Direction.DOWN);
         blockpos$mutableblockpos.setWithOffset(pPos, Direction.UP);
         BlockState blockstate1 = pLevel.getBlockState(pPos);
         if (pStrategy.canSpawnOn(pLevel, pPos, blockstate1, blockpos$mutableblockpos, blockstate)) {
            pPos.move(Direction.UP);
            return true;
         }

         blockstate = blockstate1;
      }

      return false;
   }

   public interface Strategy {
      /** @deprecated */
      @Deprecated
      SpawnUtil.Strategy LEGACY_IRON_GOLEM = (p_289751_, p_289752_, p_289753_, p_289754_, p_289755_) -> {
         if (!p_289753_.is(Blocks.COBWEB) && !p_289753_.is(Blocks.CACTUS) && !p_289753_.is(Blocks.GLASS_PANE) && !(p_289753_.getBlock() instanceof StainedGlassPaneBlock) && !(p_289753_.getBlock() instanceof StainedGlassBlock) && !(p_289753_.getBlock() instanceof LeavesBlock) && !p_289753_.is(Blocks.CONDUIT) && !p_289753_.is(Blocks.ICE) && !p_289753_.is(Blocks.TNT) && !p_289753_.is(Blocks.GLOWSTONE) && !p_289753_.is(Blocks.BEACON) && !p_289753_.is(Blocks.SEA_LANTERN) && !p_289753_.is(Blocks.FROSTED_ICE) && !p_289753_.is(Blocks.TINTED_GLASS) && !p_289753_.is(Blocks.GLASS)) {
            return (p_289755_.isAir() || p_289755_.liquid()) && (p_289753_.isSolid() || p_289753_.is(Blocks.POWDER_SNOW));
         } else {
            return false;
         }
      };
      SpawnUtil.Strategy ON_TOP_OF_COLLIDER = (p_216416_, p_216417_, p_216418_, p_216419_, p_216420_) -> {
         return p_216420_.getCollisionShape(p_216416_, p_216419_).isEmpty() && Block.isFaceFull(p_216418_.getCollisionShape(p_216416_, p_216417_), Direction.UP);
      };

      boolean canSpawnOn(ServerLevel pLevel, BlockPos p_216429_, BlockState p_216430_, BlockPos p_216431_, BlockState p_216432_);
   }
}
