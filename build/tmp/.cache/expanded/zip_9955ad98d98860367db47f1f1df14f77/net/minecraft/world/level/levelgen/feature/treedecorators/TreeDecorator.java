package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
   public static final Codec<TreeDecorator> CODEC = BuiltInRegistries.TREE_DECORATOR_TYPE.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

   protected abstract TreeDecoratorType<?> type();

   public abstract void place(TreeDecorator.Context pContext);

   public static final class Context {
      private final LevelSimulatedReader level;
      private final BiConsumer<BlockPos, BlockState> decorationSetter;
      private final RandomSource random;
      private final ObjectArrayList<BlockPos> logs;
      private final ObjectArrayList<BlockPos> leaves;
      private final ObjectArrayList<BlockPos> roots;

      public Context(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pDecorationSetter, RandomSource pRandom, Set<BlockPos> pLogs, Set<BlockPos> pLeaves, Set<BlockPos> pRoots) {
         this.level = pLevel;
         this.decorationSetter = pDecorationSetter;
         this.random = pRandom;
         this.roots = new ObjectArrayList<>(pRoots);
         this.logs = new ObjectArrayList<>(pLogs);
         this.leaves = new ObjectArrayList<>(pLeaves);
         this.logs.sort(Comparator.comparingInt(Vec3i::getY));
         this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
         this.roots.sort(Comparator.comparingInt(Vec3i::getY));
      }

      public void placeVine(BlockPos pPos, BooleanProperty pSideProperty) {
         this.setBlock(pPos, Blocks.VINE.defaultBlockState().setValue(pSideProperty, Boolean.valueOf(true)));
      }

      public void setBlock(BlockPos pPos, BlockState pState) {
         this.decorationSetter.accept(pPos, pState);
      }

      public boolean isAir(BlockPos pPos) {
         return this.level.isStateAtPosition(pPos, BlockBehaviour.BlockStateBase::isAir);
      }

      public LevelSimulatedReader level() {
         return this.level;
      }

      public RandomSource random() {
         return this.random;
      }

      public ObjectArrayList<BlockPos> logs() {
         return this.logs;
      }

      public ObjectArrayList<BlockPos> leaves() {
         return this.leaves;
      }

      public ObjectArrayList<BlockPos> roots() {
         return this.roots;
      }
   }
}