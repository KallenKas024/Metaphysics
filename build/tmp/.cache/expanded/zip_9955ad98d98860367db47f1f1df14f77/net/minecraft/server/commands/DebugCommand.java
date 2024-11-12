package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.slf4j.Logger;

public class DebugCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.debug.notRunning"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.debug.alreadyRunning"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("debug").requires((p_180073_) -> {
         return p_180073_.hasPermission(3);
      }).then(Commands.literal("start").executes((p_180069_) -> {
         return start(p_180069_.getSource());
      })).then(Commands.literal("stop").executes((p_136918_) -> {
         return stop(p_136918_.getSource());
      })).then(Commands.literal("function").requires((p_180071_) -> {
         return p_180071_.hasPermission(3);
      }).then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).executes((p_136908_) -> {
         return traceFunction(p_136908_.getSource(), FunctionArgument.getFunctions(p_136908_, "name"));
      }))));
   }

   private static int start(CommandSourceStack pSource) throws CommandSyntaxException {
      MinecraftServer minecraftserver = pSource.getServer();
      if (minecraftserver.isTimeProfilerRunning()) {
         throw ERROR_ALREADY_RUNNING.create();
      } else {
         minecraftserver.startTimeProfiler();
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.debug.started");
         }, true);
         return 0;
      }
   }

   private static int stop(CommandSourceStack pSource) throws CommandSyntaxException {
      MinecraftServer minecraftserver = pSource.getServer();
      if (!minecraftserver.isTimeProfilerRunning()) {
         throw ERROR_NOT_RUNNING.create();
      } else {
         ProfileResults profileresults = minecraftserver.stopTimeProfiler();
         double d0 = (double)profileresults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
         double d1 = (double)profileresults.getTickDuration() / d0;
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d0), profileresults.getTickDuration(), String.format(Locale.ROOT, "%.2f", d1));
         }, true);
         return (int)d1;
      }
   }

   private static int traceFunction(CommandSourceStack pSource, Collection<CommandFunction> pFunctions) {
      int i = 0;
      MinecraftServer minecraftserver = pSource.getServer();
      String s = "debug-trace-" + Util.getFilenameFormattedDateTime() + ".txt";

      try {
         Path path = minecraftserver.getFile("debug").toPath();
         Files.createDirectories(path);

         try (Writer writer = Files.newBufferedWriter(path.resolve(s), StandardCharsets.UTF_8)) {
            PrintWriter printwriter = new PrintWriter(writer);

            for(CommandFunction commandfunction : pFunctions) {
               printwriter.println((Object)commandfunction.getId());
               DebugCommand.Tracer debugcommand$tracer = new DebugCommand.Tracer(printwriter);
               i += pSource.getServer().getFunctions().execute(commandfunction, pSource.withSource(debugcommand$tracer).withMaximumPermission(2), debugcommand$tracer);
            }
         }
      } catch (IOException | UncheckedIOException uncheckedioexception) {
         LOGGER.warn("Tracing failed", (Throwable)uncheckedioexception);
         pSource.sendFailure(Component.translatable("commands.debug.function.traceFailed"));
      }

      int j = i;
      if (pFunctions.size() == 1) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.debug.function.success.single", j, pFunctions.iterator().next().getId(), s);
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.debug.function.success.multiple", j, pFunctions.size(), s);
         }, true);
      }

      return i;
   }

   static class Tracer implements ServerFunctionManager.TraceCallbacks, CommandSource {
      public static final int INDENT_OFFSET = 1;
      private final PrintWriter output;
      private int lastIndent;
      private boolean waitingForResult;

      Tracer(PrintWriter pOutput) {
         this.output = pOutput;
      }

      private void indentAndSave(int pIndent) {
         this.printIndent(pIndent);
         this.lastIndent = pIndent;
      }

      private void printIndent(int pIndent) {
         for(int i = 0; i < pIndent + 1; ++i) {
            this.output.write("    ");
         }

      }

      private void newLine() {
         if (this.waitingForResult) {
            this.output.println();
            this.waitingForResult = false;
         }

      }

      public void onCommand(int pIndent, String pCommand) {
         this.newLine();
         this.indentAndSave(pIndent);
         this.output.print("[C] ");
         this.output.print(pCommand);
         this.waitingForResult = true;
      }

      public void onReturn(int pIndent, String pReturnValue, int pCommand) {
         if (this.waitingForResult) {
            this.output.print(" -> ");
            this.output.println(pCommand);
            this.waitingForResult = false;
         } else {
            this.indentAndSave(pIndent);
            this.output.print("[R = ");
            this.output.print(pCommand);
            this.output.print("] ");
            this.output.println(pReturnValue);
         }

      }

      public void onCall(int pIndent, ResourceLocation pCommand, int pSize) {
         this.newLine();
         this.indentAndSave(pIndent);
         this.output.print("[F] ");
         this.output.print((Object)pCommand);
         this.output.print(" size=");
         this.output.println(pSize);
      }

      public void onError(int pIndent, String pCommand) {
         this.newLine();
         this.indentAndSave(pIndent + 1);
         this.output.print("[E] ");
         this.output.print(pCommand);
      }

      public void sendSystemMessage(Component pComponent) {
         this.newLine();
         this.printIndent(this.lastIndent + 1);
         this.output.print("[M] ");
         this.output.println(pComponent.getString());
      }

      public boolean acceptsSuccess() {
         return true;
      }

      public boolean acceptsFailure() {
         return true;
      }

      public boolean shouldInformAdmins() {
         return false;
      }

      public boolean alwaysAccepts() {
         return true;
      }
   }
}