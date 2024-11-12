package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlockStateArgument implements ArgumentType<BlockInput> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
   private final HolderLookup<Block> blocks;

   public BlockStateArgument(CommandBuildContext pBuildContext) {
      this.blocks = pBuildContext.holderLookup(Registries.BLOCK);
   }

   public static BlockStateArgument block(CommandBuildContext pBuildContext) {
      return new BlockStateArgument(pBuildContext);
   }

   public BlockInput parse(StringReader pReader) throws CommandSyntaxException {
      BlockStateParser.BlockResult blockstateparser$blockresult = BlockStateParser.parseForBlock(this.blocks, pReader, true);
      return new BlockInput(blockstateparser$blockresult.blockState(), blockstateparser$blockresult.properties().keySet(), blockstateparser$blockresult.nbt());
   }

   public static BlockInput getBlock(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, BlockInput.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return BlockStateParser.fillSuggestions(this.blocks, pBuilder, false, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}