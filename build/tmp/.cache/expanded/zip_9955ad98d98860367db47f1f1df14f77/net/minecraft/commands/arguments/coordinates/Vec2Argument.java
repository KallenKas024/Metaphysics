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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Vec2Argument implements ArgumentType<Coordinates> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "0.1 -0.5", "~1 ~-2");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.pos2d.incomplete"));
   private final boolean centerCorrect;

   public Vec2Argument(boolean pCenterCorrect) {
      this.centerCorrect = pCenterCorrect;
   }

   public static Vec2Argument vec2() {
      return new Vec2Argument(true);
   }

   public static Vec2Argument vec2(boolean pCenterCorrect) {
      return new Vec2Argument(pCenterCorrect);
   }

   public static Vec2 getVec2(CommandContext<CommandSourceStack> pContext, String pName) {
      Vec3 vec3 = pContext.getArgument(pName, Coordinates.class).getPosition(pContext.getSource());
      return new Vec2((float)vec3.x, (float)vec3.z);
   }

   public Coordinates parse(StringReader p_120824_) throws CommandSyntaxException {
      int i = p_120824_.getCursor();
      if (!p_120824_.canRead()) {
         throw ERROR_NOT_COMPLETE.createWithContext(p_120824_);
      } else {
         WorldCoordinate worldcoordinate = WorldCoordinate.parseDouble(p_120824_, this.centerCorrect);
         if (p_120824_.canRead() && p_120824_.peek() == ' ') {
            p_120824_.skip();
            WorldCoordinate worldcoordinate1 = WorldCoordinate.parseDouble(p_120824_, this.centerCorrect);
            return new WorldCoordinates(worldcoordinate, new WorldCoordinate(true, 0.0D), worldcoordinate1);
         } else {
            p_120824_.setCursor(i);
            throw ERROR_NOT_COMPLETE.createWithContext(p_120824_);
         }
      }
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

         return SharedSuggestionProvider.suggest2DCoordinates(s, collection, pBuilder, Commands.createValidator(this::parse));
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}