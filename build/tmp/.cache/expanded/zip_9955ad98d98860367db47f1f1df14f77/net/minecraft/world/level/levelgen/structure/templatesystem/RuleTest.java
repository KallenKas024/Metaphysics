package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a (possibly randomly influenced) predicate of a given block state to be replaced during world generation.
 */
public abstract class RuleTest {
   public static final Codec<RuleTest> CODEC = BuiltInRegistries.RULE_TEST.byNameCodec().dispatch("predicate_type", RuleTest::getType, RuleTestType::codec);

   public abstract boolean test(BlockState pState, RandomSource pRandom);

   protected abstract RuleTestType<?> getType();
}