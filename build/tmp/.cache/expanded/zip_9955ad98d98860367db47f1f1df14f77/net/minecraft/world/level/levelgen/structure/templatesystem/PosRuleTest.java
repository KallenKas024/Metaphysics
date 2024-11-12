package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;

public abstract class PosRuleTest {
   public static final Codec<PosRuleTest> CODEC = BuiltInRegistries.POS_RULE_TEST.byNameCodec().dispatch("predicate_type", PosRuleTest::getType, PosRuleTestType::codec);

   public abstract boolean test(BlockPos pLocalPos, BlockPos pRelativePos, BlockPos pStructurePos, RandomSource pRandom);

   protected abstract PosRuleTestType<?> getType();
}