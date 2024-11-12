package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
   private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.center.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.nochange"));
   private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.small"));
   private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.big", (double)5.999997E7F));
   private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7D));
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.warning.time.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.warning.distance.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.damage.buffer.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.damage.amount.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("worldborder").requires((p_139268_) -> {
         return p_139268_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("distance", DoubleArgumentType.doubleArg((double)-5.999997E7F, (double)5.999997E7F)).executes((p_289305_) -> {
         return setSize(p_289305_.getSource(), p_289305_.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(p_289305_, "distance"), 0L);
      }).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_289306_) -> {
         return setSize(p_289306_.getSource(), p_289306_.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(p_289306_, "distance"), p_289306_.getSource().getLevel().getWorldBorder().getLerpRemainingTime() + (long)IntegerArgumentType.getInteger(p_289306_, "time") * 1000L);
      })))).then(Commands.literal("set").then(Commands.argument("distance", DoubleArgumentType.doubleArg((double)-5.999997E7F, (double)5.999997E7F)).executes((p_139286_) -> {
         return setSize(p_139286_.getSource(), DoubleArgumentType.getDouble(p_139286_, "distance"), 0L);
      }).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_139284_) -> {
         return setSize(p_139284_.getSource(), DoubleArgumentType.getDouble(p_139284_, "distance"), (long)IntegerArgumentType.getInteger(p_139284_, "time") * 1000L);
      })))).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes((p_139282_) -> {
         return setCenter(p_139282_.getSource(), Vec2Argument.getVec2(p_139282_, "pos"));
      }))).then(Commands.literal("damage").then(Commands.literal("amount").then(Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F)).executes((p_139280_) -> {
         return setDamageAmount(p_139280_.getSource(), FloatArgumentType.getFloat(p_139280_, "damagePerBlock"));
      }))).then(Commands.literal("buffer").then(Commands.argument("distance", FloatArgumentType.floatArg(0.0F)).executes((p_139278_) -> {
         return setDamageBuffer(p_139278_.getSource(), FloatArgumentType.getFloat(p_139278_, "distance"));
      })))).then(Commands.literal("get").executes((p_139276_) -> {
         return getSize(p_139276_.getSource());
      })).then(Commands.literal("warning").then(Commands.literal("distance").then(Commands.argument("distance", IntegerArgumentType.integer(0)).executes((p_139266_) -> {
         return setWarningDistance(p_139266_.getSource(), IntegerArgumentType.getInteger(p_139266_, "distance"));
      }))).then(Commands.literal("time").then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_139249_) -> {
         return setWarningTime(p_139249_.getSource(), IntegerArgumentType.getInteger(p_139249_, "time"));
      })))));
   }

   private static int setDamageBuffer(CommandSourceStack pSource, float pDistance) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getServer().overworld().getWorldBorder();
      if (worldborder.getDamageSafeZone() == (double)pDistance) {
         throw ERROR_SAME_DAMAGE_BUFFER.create();
      } else {
         worldborder.setDamageSafeZone((double)pDistance);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", pDistance));
         }, true);
         return (int)pDistance;
      }
   }

   private static int setDamageAmount(CommandSourceStack pSource, float pDamagePerBlock) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getServer().overworld().getWorldBorder();
      if (worldborder.getDamagePerBlock() == (double)pDamagePerBlock) {
         throw ERROR_SAME_DAMAGE_AMOUNT.create();
      } else {
         worldborder.setDamagePerBlock((double)pDamagePerBlock);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", pDamagePerBlock));
         }, true);
         return (int)pDamagePerBlock;
      }
   }

   private static int setWarningTime(CommandSourceStack pSource, int pTime) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getServer().overworld().getWorldBorder();
      if (worldborder.getWarningTime() == pTime) {
         throw ERROR_SAME_WARNING_TIME.create();
      } else {
         worldborder.setWarningTime(pTime);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.worldborder.warning.time.success", pTime);
         }, true);
         return pTime;
      }
   }

   private static int setWarningDistance(CommandSourceStack pSource, int pDistance) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getServer().overworld().getWorldBorder();
      if (worldborder.getWarningBlocks() == pDistance) {
         throw ERROR_SAME_WARNING_DISTANCE.create();
      } else {
         worldborder.setWarningBlocks(pDistance);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.worldborder.warning.distance.success", pDistance);
         }, true);
         return pDistance;
      }
   }

   private static int getSize(CommandSourceStack pSource) {
      double d0 = pSource.getServer().overworld().getWorldBorder().getSize();
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d0));
      }, false);
      return Mth.floor(d0 + 0.5D);
   }

   private static int setCenter(CommandSourceStack pSource, Vec2 pPos) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getServer().overworld().getWorldBorder();
      if (worldborder.getCenterX() == (double)pPos.x && worldborder.getCenterZ() == (double)pPos.y) {
         throw ERROR_SAME_CENTER.create();
      } else if (!((double)Math.abs(pPos.x) > 2.9999984E7D) && !((double)Math.abs(pPos.y) > 2.9999984E7D)) {
         worldborder.setCenter((double)pPos.x, (double)pPos.y);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", pPos.x), String.format(Locale.ROOT, "%.2f", pPos.y));
         }, true);
         return 0;
      } else {
         throw ERROR_TOO_FAR_OUT.create();
      }
   }

   private static int setSize(CommandSourceStack pSource, double pNewSize, long pTime) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getServer().overworld().getWorldBorder();
      double d0 = worldborder.getSize();
      if (d0 == pNewSize) {
         throw ERROR_SAME_SIZE.create();
      } else if (pNewSize < 1.0D) {
         throw ERROR_TOO_SMALL.create();
      } else if (pNewSize > (double)5.999997E7F) {
         throw ERROR_TOO_BIG.create();
      } else {
         if (pTime > 0L) {
            worldborder.lerpSizeBetween(d0, pNewSize, pTime);
            if (pNewSize > d0) {
               pSource.sendSuccess(() -> {
                  return Component.translatable("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", pNewSize), Long.toString(pTime / 1000L));
               }, true);
            } else {
               pSource.sendSuccess(() -> {
                  return Component.translatable("commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", pNewSize), Long.toString(pTime / 1000L));
               }, true);
            }
         } else {
            worldborder.setSize(pNewSize);
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", pNewSize));
            }, true);
         }

         return (int)(pNewSize - d0);
      }
   }
}