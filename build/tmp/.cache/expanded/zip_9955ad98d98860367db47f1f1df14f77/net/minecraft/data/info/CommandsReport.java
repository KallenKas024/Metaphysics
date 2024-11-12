package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class CommandsReport implements DataProvider {
   private final PackOutput output;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public CommandsReport(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
      this.output = pOutput;
      this.registries = pRegistries;
   }

   public CompletableFuture<?> run(CachedOutput pOutput) {
      Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
      return this.registries.thenCompose((p_256367_) -> {
         CommandDispatcher<CommandSourceStack> commanddispatcher = (new Commands(Commands.CommandSelection.ALL, Commands.createValidationContext(p_256367_))).getDispatcher();
         return DataProvider.saveStable(pOutput, ArgumentUtils.serializeNodeToJson(commanddispatcher, commanddispatcher.getRoot()), path);
      });
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public final String getName() {
      return "Command Syntax";
   }
}