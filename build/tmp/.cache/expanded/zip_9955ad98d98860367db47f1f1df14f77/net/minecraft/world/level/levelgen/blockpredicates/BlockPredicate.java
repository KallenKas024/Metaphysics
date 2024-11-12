package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
   Codec<BlockPredicate> CODEC = BuiltInRegistries.BLOCK_PREDICATE_TYPE.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
   BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlocks(Blocks.AIR);
   BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(Blocks.AIR, Blocks.WATER);

   BlockPredicateType<?> type();

   static BlockPredicate allOf(List<BlockPredicate> pPredicates) {
      return new AllOfPredicate(pPredicates);
   }

   static BlockPredicate allOf(BlockPredicate... pPredicates) {
      return allOf(List.of(pPredicates));
   }

   static BlockPredicate allOf(BlockPredicate pPredicate1, BlockPredicate pPredicate2) {
      return allOf(List.of(pPredicate1, pPredicate2));
   }

   static BlockPredicate anyOf(List<BlockPredicate> pPredicates) {
      return new AnyOfPredicate(pPredicates);
   }

   static BlockPredicate anyOf(BlockPredicate... pPredicates) {
      return anyOf(List.of(pPredicates));
   }

   static BlockPredicate anyOf(BlockPredicate pPredicate1, BlockPredicate pPredicate2) {
      return anyOf(List.of(pPredicate1, pPredicate2));
   }

   static BlockPredicate matchesBlocks(Vec3i pOffset, List<Block> pBlocks) {
      return new MatchingBlocksPredicate(pOffset, HolderSet.direct(Block::builtInRegistryHolder, pBlocks));
   }

   static BlockPredicate matchesBlocks(List<Block> pBlocks) {
      return matchesBlocks(Vec3i.ZERO, pBlocks);
   }

   static BlockPredicate matchesBlocks(Vec3i pOffset, Block... pBlocks) {
      return matchesBlocks(pOffset, List.of(pBlocks));
   }

   static BlockPredicate matchesBlocks(Block... pBlocks) {
      return matchesBlocks(Vec3i.ZERO, pBlocks);
   }

   static BlockPredicate matchesTag(Vec3i pOffset, TagKey<Block> pTag) {
      return new MatchingBlockTagPredicate(pOffset, pTag);
   }

   static BlockPredicate matchesTag(TagKey<Block> pTag) {
      return matchesTag(Vec3i.ZERO, pTag);
   }

   static BlockPredicate matchesFluids(Vec3i pOffset, List<Fluid> pFluids) {
      return new MatchingFluidsPredicate(pOffset, HolderSet.direct(Fluid::builtInRegistryHolder, pFluids));
   }

   static BlockPredicate matchesFluids(Vec3i pOffset, Fluid... pFluids) {
      return matchesFluids(pOffset, List.of(pFluids));
   }

   static BlockPredicate matchesFluids(Fluid... pFluids) {
      return matchesFluids(Vec3i.ZERO, pFluids);
   }

   static BlockPredicate not(BlockPredicate pPredicate) {
      return new NotPredicate(pPredicate);
   }

   static BlockPredicate replaceable(Vec3i pOffset) {
      return new ReplaceablePredicate(pOffset);
   }

   static BlockPredicate replaceable() {
      return replaceable(Vec3i.ZERO);
   }

   static BlockPredicate wouldSurvive(BlockState pState, Vec3i pOffset) {
      return new WouldSurvivePredicate(pOffset, pState);
   }

   static BlockPredicate hasSturdyFace(Vec3i pOffset, Direction pDirection) {
      return new HasSturdyFacePredicate(pOffset, pDirection);
   }

   static BlockPredicate hasSturdyFace(Direction pDirection) {
      return hasSturdyFace(Vec3i.ZERO, pDirection);
   }

   static BlockPredicate solid(Vec3i pOffset) {
      return new SolidPredicate(pOffset);
   }

   static BlockPredicate solid() {
      return solid(Vec3i.ZERO);
   }

   static BlockPredicate noFluid() {
      return noFluid(Vec3i.ZERO);
   }

   static BlockPredicate noFluid(Vec3i pOffset) {
      return matchesFluids(pOffset, Fluids.EMPTY);
   }

   static BlockPredicate insideWorld(Vec3i pOffset) {
      return new InsideWorldBoundsPredicate(pOffset);
   }

   static BlockPredicate alwaysTrue() {
      return TrueBlockPredicate.INSTANCE;
   }
}