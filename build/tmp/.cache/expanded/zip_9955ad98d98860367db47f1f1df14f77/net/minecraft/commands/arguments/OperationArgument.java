package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.Score;

public class OperationArgument implements ArgumentType<OperationArgument.Operation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
   private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType(Component.translatable("arguments.operation.invalid"));
   private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType(Component.translatable("arguments.operation.div0"));

   public static OperationArgument operation() {
      return new OperationArgument();
   }

   public static OperationArgument.Operation getOperation(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, OperationArgument.Operation.class);
   }

   public OperationArgument.Operation parse(StringReader pReader) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw ERROR_INVALID_OPERATION.create();
      } else {
         int i = pReader.getCursor();

         while(pReader.canRead() && pReader.peek() != ' ') {
            pReader.skip();
         }

         return getOperation(pReader.getString().substring(i, pReader.getCursor()));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   /**
    * Makes an {@link net.minecraft.commands.arguments.OperationArgument.Operation} instance based on the given name.
    * This method handles all operations.
    */
   private static OperationArgument.Operation getOperation(String pName) throws CommandSyntaxException {
      return (pName.equals("><") ? (p_103279_, p_103280_) -> {
         int i = p_103279_.getScore();
         p_103279_.setScore(p_103280_.getScore());
         p_103280_.setScore(i);
      } : getSimpleOperation(pName));
   }

   /**
    * Makes an {@link net.minecraft.commands.arguments.OperationArgument.Operation} instance based on the given name.
    * This method actually returns {@link net.minecraft.commands.arguments.OperationArgument.SimpleOperation}, which is
    * used as a functional interface target with 2 ints. It handles all operations other than swap (><).
    */
   private static OperationArgument.SimpleOperation getSimpleOperation(String pName) throws CommandSyntaxException {
      switch (pName) {
         case "=":
            return (p_103298_, p_103299_) -> {
               return p_103299_;
            };
         case "+=":
            return (p_103295_, p_103296_) -> {
               return p_103295_ + p_103296_;
            };
         case "-=":
            return (p_103292_, p_103293_) -> {
               return p_103292_ - p_103293_;
            };
         case "*=":
            return (p_103289_, p_103290_) -> {
               return p_103289_ * p_103290_;
            };
         case "/=":
            return (p_264713_, p_264714_) -> {
               if (p_264714_ == 0) {
                  throw ERROR_DIVIDE_BY_ZERO.create();
               } else {
                  return Mth.floorDiv(p_264713_, p_264714_);
               }
            };
         case "%=":
            return (p_103271_, p_103272_) -> {
               if (p_103272_ == 0) {
                  throw ERROR_DIVIDE_BY_ZERO.create();
               } else {
                  return Mth.positiveModulo(p_103271_, p_103272_);
               }
            };
         case "<":
            return Math::min;
         case ">":
            return Math::max;
         default:
            throw ERROR_INVALID_OPERATION.create();
      }
   }

   @FunctionalInterface
   public interface Operation {
      void apply(Score pTargetScore, Score pSourceScore) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface SimpleOperation extends OperationArgument.Operation {
      int apply(int pTargetScore, int pSourceScore) throws CommandSyntaxException;

      default void apply(Score pTargetScore, Score pSourceScore) throws CommandSyntaxException {
         pTargetScore.setScore(this.apply(pTargetScore.getScore(), pSourceScore.getScore()));
      }
   }
}