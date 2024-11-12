package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
   private static final Component NO_RECURSIVE_TRACES = Component.translatable("commands.debug.function.noRecursion");
   private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
   private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
   final MinecraftServer server;
   @Nullable
   private ServerFunctionManager.ExecutionContext context;
   private List<CommandFunction> ticking = ImmutableList.of();
   private boolean postReload;
   private ServerFunctionLibrary library;

   public ServerFunctionManager(MinecraftServer pServer, ServerFunctionLibrary pLibrary) {
      this.server = pServer;
      this.library = pLibrary;
      this.postReload(pLibrary);
   }

   public int getCommandLimit() {
      return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
   }

   public CommandDispatcher<CommandSourceStack> getDispatcher() {
      return this.server.getCommands().getDispatcher();
   }

   public void tick() {
      if (this.postReload) {
         this.postReload = false;
         Collection<CommandFunction> collection = this.library.getTag(LOAD_FUNCTION_TAG);
         this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
      }

      this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
   }

   private void executeTagFunctions(Collection<CommandFunction> pFunctionObjects, ResourceLocation pIdentifier) {
      this.server.getProfiler().push(pIdentifier::toString);

      for(CommandFunction commandfunction : pFunctionObjects) {
         this.execute(commandfunction, this.getGameLoopSender());
      }

      this.server.getProfiler().pop();
   }

   public int execute(CommandFunction pFunctionObject, CommandSourceStack pSource) {
      return this.execute(pFunctionObject, pSource, (ServerFunctionManager.TraceCallbacks)null);
   }

   public int execute(CommandFunction pFunctionObject, CommandSourceStack pSource, @Nullable ServerFunctionManager.TraceCallbacks pTracer) {
      if (this.context != null) {
         if (pTracer != null) {
            this.context.reportError(NO_RECURSIVE_TRACES.getString());
            return 0;
         } else {
            this.context.delayFunctionCall(pFunctionObject, pSource);
            return 0;
         }
      } else {
         int i;
         try {
            this.context = new ServerFunctionManager.ExecutionContext(pTracer);
            i = this.context.runTopCommand(pFunctionObject, pSource);
         } finally {
            this.context = null;
         }

         return i;
      }
   }

   public void replaceLibrary(ServerFunctionLibrary pReloader) {
      this.library = pReloader;
      this.postReload(pReloader);
   }

   private void postReload(ServerFunctionLibrary pReloader) {
      this.ticking = ImmutableList.copyOf(pReloader.getTag(TICK_FUNCTION_TAG));
      this.postReload = true;
   }

   public CommandSourceStack getGameLoopSender() {
      return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
   }

   public Optional<CommandFunction> get(ResourceLocation pFunctionIdentifier) {
      return this.library.getFunction(pFunctionIdentifier);
   }

   public Collection<CommandFunction> getTag(ResourceLocation pFunctionTagIdentifier) {
      return this.library.getTag(pFunctionTagIdentifier);
   }

   public Iterable<ResourceLocation> getFunctionNames() {
      return this.library.getFunctions().keySet();
   }

   public Iterable<ResourceLocation> getTagNames() {
      return this.library.getAvailableTags();
   }

   class ExecutionContext {
      private int depth;
      @Nullable
      private final ServerFunctionManager.TraceCallbacks tracer;
      private final Deque<ServerFunctionManager.QueuedCommand> commandQueue = Queues.newArrayDeque();
      private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();
      boolean abortCurrentDepth = false;

      ExecutionContext(@Nullable ServerFunctionManager.TraceCallbacks pTracer) {
         this.tracer = pTracer;
      }

      void delayFunctionCall(CommandFunction pFunction, CommandSourceStack pSender) {
         int i = ServerFunctionManager.this.getCommandLimit();
         CommandSourceStack commandsourcestack = this.wrapSender(pSender);
         if (this.commandQueue.size() + this.nestedCalls.size() < i) {
            this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(commandsourcestack, this.depth, new CommandFunction.FunctionEntry(pFunction)));
         }

      }

      private CommandSourceStack wrapSender(CommandSourceStack pSender) {
         IntConsumer intconsumer = pSender.getReturnValueConsumer();
         return intconsumer instanceof ServerFunctionManager.ExecutionContext.AbortingReturnValueConsumer ? pSender : pSender.withReturnValueConsumer(new ServerFunctionManager.ExecutionContext.AbortingReturnValueConsumer(intconsumer));
      }

      int runTopCommand(CommandFunction pFunction, CommandSourceStack pSource) {
         int i = ServerFunctionManager.this.getCommandLimit();
         CommandSourceStack commandsourcestack = this.wrapSender(pSource);
         int j = 0;
         CommandFunction.Entry[] acommandfunction$entry = pFunction.getEntries();

         for(int k = acommandfunction$entry.length - 1; k >= 0; --k) {
            this.commandQueue.push(new ServerFunctionManager.QueuedCommand(commandsourcestack, 0, acommandfunction$entry[k]));
         }

         while(!this.commandQueue.isEmpty()) {
            try {
               ServerFunctionManager.QueuedCommand serverfunctionmanager$queuedcommand = this.commandQueue.removeFirst();
               ServerFunctionManager.this.server.getProfiler().push(serverfunctionmanager$queuedcommand::toString);
               this.depth = serverfunctionmanager$queuedcommand.depth;
               serverfunctionmanager$queuedcommand.execute(ServerFunctionManager.this, this.commandQueue, i, this.tracer);
               if (!this.abortCurrentDepth) {
                  if (!this.nestedCalls.isEmpty()) {
                     Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                  }
               } else {
                  while(!this.commandQueue.isEmpty() && (this.commandQueue.peek()).depth >= this.depth) {
                     this.commandQueue.removeFirst();
                  }

                  this.abortCurrentDepth = false;
               }

               this.nestedCalls.clear();
            } finally {
               ServerFunctionManager.this.server.getProfiler().pop();
            }

            ++j;
            if (j >= i) {
               return j;
            }
         }

         return j;
      }

      public void reportError(String pError) {
         if (this.tracer != null) {
            this.tracer.onError(this.depth, pError);
         }

      }

      class AbortingReturnValueConsumer implements IntConsumer {
         private final IntConsumer wrapped;

         AbortingReturnValueConsumer(IntConsumer pWrapped) {
            this.wrapped = pWrapped;
         }

         public void accept(int pValue) {
            this.wrapped.accept(pValue);
            ExecutionContext.this.abortCurrentDepth = true;
         }
      }
   }

   public static class QueuedCommand {
      private final CommandSourceStack sender;
      final int depth;
      private final CommandFunction.Entry entry;

      public QueuedCommand(CommandSourceStack pSender, int pDepth, CommandFunction.Entry pEntry) {
         this.sender = pSender;
         this.depth = pDepth;
         this.entry = pEntry;
      }

      public void execute(ServerFunctionManager pServerFunctionManager, Deque<ServerFunctionManager.QueuedCommand> pQueuedCommands, int pCommandLimit, @Nullable ServerFunctionManager.TraceCallbacks pTracer) {
         try {
            this.entry.execute(pServerFunctionManager, this.sender, pQueuedCommands, pCommandLimit, this.depth, pTracer);
         } catch (CommandSyntaxException commandsyntaxexception) {
            if (pTracer != null) {
               pTracer.onError(this.depth, commandsyntaxexception.getRawMessage().getString());
            }
         } catch (Exception exception) {
            if (pTracer != null) {
               pTracer.onError(this.depth, exception.getMessage());
            }
         }

      }

      public String toString() {
         return this.entry.toString();
      }
   }

   public interface TraceCallbacks {
      void onCommand(int pIndent, String pCommand);

      void onReturn(int pIndent, String pReturnValue, int pCommand);

      void onError(int pIndent, String pCommand);

      void onCall(int pIndent, ResourceLocation pCommand, int pSize);
   }
}