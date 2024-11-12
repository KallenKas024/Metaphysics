package net.minecraft.commands.arguments.item;

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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ItemArgument implements ArgumentType<ItemInput> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
   private final HolderLookup<Item> items;

   public ItemArgument(CommandBuildContext pContext) {
      this.items = pContext.holderLookup(Registries.ITEM);
   }

   public static ItemArgument item(CommandBuildContext pContext) {
      return new ItemArgument(pContext);
   }

   public ItemInput parse(StringReader pReader) throws CommandSyntaxException {
      ItemParser.ItemResult itemparser$itemresult = ItemParser.parseForItem(this.items, pReader);
      return new ItemInput(itemparser$itemresult.item(), itemparser$itemresult.nbt());
   }

   public static <S> ItemInput getItem(CommandContext<S> pContext, String pName) {
      return pContext.getArgument(pName, ItemInput.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return ItemParser.fillSuggestions(this.items, pBuilder, false);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}