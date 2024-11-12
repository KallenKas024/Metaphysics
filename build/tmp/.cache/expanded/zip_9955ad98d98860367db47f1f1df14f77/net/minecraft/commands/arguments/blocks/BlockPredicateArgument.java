package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
   private final HolderLookup<Block> blocks;

   public BlockPredicateArgument(CommandBuildContext pContext) {
      this.blocks = pContext.holderLookup(Registries.BLOCK);
   }

   public static BlockPredicateArgument blockPredicate(CommandBuildContext pContext) {
      return new BlockPredicateArgument(pContext);
   }

   public BlockPredicateArgument.Result parse(StringReader pReader) throws CommandSyntaxException {
      return parse(this.blocks, pReader);
   }

   public static BlockPredicateArgument.Result parse(HolderLookup<Block> pLookup, StringReader pReader) throws CommandSyntaxException {
      return BlockStateParser.parseForTesting(pLookup, pReader, true).map((p_234630_) -> {
         return new BlockPredicateArgument.BlockPredicate(p_234630_.blockState(), p_234630_.properties().keySet(), p_234630_.nbt());
      }, (p_234632_) -> {
         return new BlockPredicateArgument.TagPredicate(p_234632_.tag(), p_234632_.vagueProperties(), p_234632_.nbt());
      });
   }

   public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      return pContext.getArgument(pName, BlockPredicateArgument.Result.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return BlockStateParser.fillSuggestions(this.blocks, pBuilder, true, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static class BlockPredicate implements BlockPredicateArgument.Result {
      private final BlockState state;
      private final Set<Property<?>> properties;
      @Nullable
      private final CompoundTag nbt;

      public BlockPredicate(BlockState pState, Set<Property<?>> pProperties, @Nullable CompoundTag pNbt) {
         this.state = pState;
         this.properties = pProperties;
         this.nbt = pNbt;
      }

      public boolean test(BlockInWorld pBlock) {
         BlockState blockstate = pBlock.getState();
         if (!blockstate.is(this.state.getBlock())) {
            return false;
         } else {
            for(Property<?> property : this.properties) {
               if (blockstate.getValue(property) != this.state.getValue(property)) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity blockentity = pBlock.getEntity();
               return blockentity != null && NbtUtils.compareNbt(this.nbt, blockentity.saveWithFullMetadata(), true);
            }
         }
      }

      public boolean requiresNbt() {
         return this.nbt != null;
      }
   }

   public interface Result extends Predicate<BlockInWorld> {
      boolean requiresNbt();
   }

   static class TagPredicate implements BlockPredicateArgument.Result {
      private final HolderSet<Block> tag;
      @Nullable
      private final CompoundTag nbt;
      private final Map<String, String> vagueProperties;

      TagPredicate(HolderSet<Block> pTag, Map<String, String> pVagueProperties, @Nullable CompoundTag pNbt) {
         this.tag = pTag;
         this.vagueProperties = pVagueProperties;
         this.nbt = pNbt;
      }

      public boolean test(BlockInWorld pBlock) {
         BlockState blockstate = pBlock.getState();
         if (!blockstate.is(this.tag)) {
            return false;
         } else {
            for(Map.Entry<String, String> entry : this.vagueProperties.entrySet()) {
               Property<?> property = blockstate.getBlock().getStateDefinition().getProperty(entry.getKey());
               if (property == null) {
                  return false;
               }

               Comparable<?> comparable = (Comparable)property.getValue(entry.getValue()).orElse(null);
               if (comparable == null) {
                  return false;
               }

               if (blockstate.getValue(property) != comparable) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity blockentity = pBlock.getEntity();
               return blockentity != null && NbtUtils.compareNbt(this.nbt, blockentity.saveWithFullMetadata(), true);
            }
         }
      }

      public boolean requiresNbt() {
         return this.nbt != null;
      }
   }
}