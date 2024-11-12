package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AngleArgument implements ArgumentType<AngleArgument.SingleAngle> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.angle.incomplete"));
   public static final SimpleCommandExceptionType ERROR_INVALID_ANGLE = new SimpleCommandExceptionType(Component.translatable("argument.angle.invalid"));

   public static AngleArgument angle() {
      return new AngleArgument();
   }

   public static float getAngle(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, AngleArgument.SingleAngle.class).getAngle(pContext.getSource());
   }

   public AngleArgument.SingleAngle parse(StringReader pReader) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw ERROR_NOT_COMPLETE.createWithContext(pReader);
      } else {
         boolean flag = WorldCoordinate.isRelative(pReader);
         float f = pReader.canRead() && pReader.peek() != ' ' ? pReader.readFloat() : 0.0F;
         if (!Float.isNaN(f) && !Float.isInfinite(f)) {
            return new AngleArgument.SingleAngle(f, flag);
         } else {
            throw ERROR_INVALID_ANGLE.createWithContext(pReader);
         }
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static final class SingleAngle {
      private final float angle;
      private final boolean isRelative;

      SingleAngle(float pAngle, boolean pIsRelative) {
         this.angle = pAngle;
         this.isRelative = pIsRelative;
      }

      public float getAngle(CommandSourceStack pSource) {
         return Mth.wrapDegrees(this.isRelative ? this.angle + pSource.getRotation().y : this.angle);
      }
   }
}