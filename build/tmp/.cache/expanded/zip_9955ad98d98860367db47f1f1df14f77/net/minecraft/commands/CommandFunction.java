package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
   private final CommandFunction.Entry[] entries;
   final ResourceLocation id;

   public CommandFunction(ResourceLocation pId, CommandFunction.Entry[] pEntries) {
      this.id = pId;
      this.entries = pEntries;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public CommandFunction.Entry[] getEntries() {
      return this.entries;
   }

   public static CommandFunction fromLines(ResourceLocation pId, CommandDispatcher<CommandSourceStack> pDispatcher, CommandSourceStack pSource, List<String> pLines) {
      List<CommandFunction.Entry> list = Lists.newArrayListWithCapacity(pLines.size());

      for(int i = 0; i < pLines.size(); ++i) {
         int j = i + 1;
         String s = pLines.get(i).trim();
         StringReader stringreader = new StringReader(s);
         if (stringreader.canRead() && stringreader.peek() != '#') {
            if (stringreader.peek() == '/') {
               stringreader.skip();
               if (stringreader.peek() == '/') {
                  throw new IllegalArgumentException("Unknown or invalid command '" + s + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
               }

               String s1 = stringreader.readUnquotedString();
               throw new IllegalArgumentException("Unknown or invalid command '" + s + "' on line " + j + " (did you mean '" + s1 + "'? Do not use a preceding forwards slash.)");
            }

            try {
               ParseResults<CommandSourceStack> parseresults = pDispatcher.parse(stringreader, pSource);
               if (parseresults.getReader().canRead()) {
                  throw Commands.getParseException(parseresults);
               }

               list.add(new CommandFunction.CommandEntry(parseresults));
            } catch (CommandSyntaxException commandsyntaxexception) {
               throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandsyntaxexception.getMessage());
            }
         }
      }

      return new CommandFunction(pId, list.toArray(new CommandFunction.Entry[0]));
   }

   public static class CacheableFunction {
      public static final CommandFunction.CacheableFunction NONE = new CommandFunction.CacheableFunction((ResourceLocation)null);
      @Nullable
      private final ResourceLocation id;
      private boolean resolved;
      private Optional<CommandFunction> function = Optional.empty();

      public CacheableFunction(@Nullable ResourceLocation pId) {
         this.id = pId;
      }

      public CacheableFunction(CommandFunction pFunction) {
         this.resolved = true;
         this.id = null;
         this.function = Optional.of(pFunction);
      }

      public Optional<CommandFunction> get(ServerFunctionManager pFunctionManager) {
         if (!this.resolved) {
            if (this.id != null) {
               this.function = pFunctionManager.get(this.id);
            }

            this.resolved = true;
         }

         return this.function;
      }

      @Nullable
      public ResourceLocation getId() {
         return this.function.map((p_78001_) -> {
            return p_78001_.id;
         }).orElse(this.id);
      }
   }

   public static class CommandEntry implements CommandFunction.Entry {
      private final ParseResults<CommandSourceStack> parse;

      public CommandEntry(ParseResults<CommandSourceStack> pParse) {
         this.parse = pParse;
      }

      public void execute(ServerFunctionManager pFunctionManager, CommandSourceStack pSource, Deque<ServerFunctionManager.QueuedCommand> pQueuedCommands, int pCommandLimit, int pDepth, @Nullable ServerFunctionManager.TraceCallbacks pTracer) throws CommandSyntaxException {
         if (pTracer != null) {
            String s = this.parse.getReader().getString();
            pTracer.onCommand(pDepth, s);
            int i = this.execute(pFunctionManager, pSource);
            pTracer.onReturn(pDepth, s, i);
         } else {
            this.execute(pFunctionManager, pSource);
         }

      }

      private int execute(ServerFunctionManager pFunctionManager, CommandSourceStack pSource) throws CommandSyntaxException {
         return pFunctionManager.getDispatcher().execute(Commands.mapSource(this.parse, (p_242934_) -> {
            return pSource;
         }));
      }

      public String toString() {
         return this.parse.getReader().getString();
      }
   }

   @FunctionalInterface
   public interface Entry {
      void execute(ServerFunctionManager pFunctionManager, CommandSourceStack pSource, Deque<ServerFunctionManager.QueuedCommand> pQueuedCommands, int pCommandLimit, int pDepth, @Nullable ServerFunctionManager.TraceCallbacks pTracer) throws CommandSyntaxException;
   }

   public static class FunctionEntry implements CommandFunction.Entry {
      private final CommandFunction.CacheableFunction function;

      public FunctionEntry(CommandFunction pFunction) {
         this.function = new CommandFunction.CacheableFunction(pFunction);
      }

      public void execute(ServerFunctionManager pFunctionManager, CommandSourceStack pSource, Deque<ServerFunctionManager.QueuedCommand> pQueuedCommands, int pCommandLimit, int pDepth, @Nullable ServerFunctionManager.TraceCallbacks pTracer) {
         Util.ifElse(this.function.get(pFunctionManager), (p_164900_) -> {
            CommandFunction.Entry[] acommandfunction$entry = p_164900_.getEntries();
            if (pTracer != null) {
               pTracer.onCall(pDepth, p_164900_.getId(), acommandfunction$entry.length);
            }

            int i = pCommandLimit - pQueuedCommands.size();
            int j = Math.min(acommandfunction$entry.length, i);

            for(int k = j - 1; k >= 0; --k) {
               pQueuedCommands.addFirst(new ServerFunctionManager.QueuedCommand(pSource, pDepth + 1, acommandfunction$entry[k]));
            }

         }, () -> {
            if (pTracer != null) {
               pTracer.onCall(pDepth, this.function.getId(), -1);
            }

         });
      }

      public String toString() {
         return "function " + this.function.getId();
      }
   }
}