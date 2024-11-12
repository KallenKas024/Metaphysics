package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class Vec3Argument implements ArgumentType<Coordinates> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.pos3d.incomplete"));
   public static final SimpleCommandExceptionType ERROR_MIXED_TYPE = new SimpleCommandExceptionType(Component.translatable("argument.pos.mixed"));
   private final boolean centerCorrect;

   public Vec3Argument(boolean pCenterCorrect) {
      this.centerCorrect = pCenterCorrect;
   }

   public static Vec3Argument vec3() {
      return new Vec3Argument(true);
   }

   public static Vec3Argument vec3(boolean pCenterCorrect) {
      return new Vec3Argument(pCenterCorrect);
   }

   public static Vec3 getVec3(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, Coordinates.class).getPosition(pContext.getSource());
   }

   public static Coordinates getCoordinates(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, Coordinates.class);
   }

   public Coordinates parse(StringReader p_120843_) throws CommandSyntaxException {
      return (Coordinates)(p_120843_.canRead() && p_120843_.peek() == '^' ? LocalCoordinates.parse(p_120843_) : WorldCoordinates.parseDouble(p_120843_, this.centerCorrect));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
         return Suggestions.empty();
      } else {
         String s = pBuilder.getRemaining();
         Collection<SharedSuggestionProvider.TextCoordinates> collection;
         if (!s.isEmpty() && s.charAt(0) == '^') {
            collection = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
         } else {
            collection = ((SharedSuggestionProvider)pContext.getSource()).getAbsoluteCoordinates();
         }

         return SharedSuggestionProvider.suggestCoordinates(s, collection, pBuilder, Commands.createValidator(this::parse));
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}