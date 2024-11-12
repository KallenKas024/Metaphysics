package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class ReloadCommand {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void reloadPacks(Collection<String> pSelectedIds, CommandSourceStack pSource) {
      pSource.getServer().reloadResources(pSelectedIds).exceptionally((p_138234_) -> {
         LOGGER.warn("Failed to execute reload", p_138234_);
         pSource.sendFailure(Component.translatable("commands.reload.failure"));
         return null;
      });
   }

   /**
    * Gets a list of IDs for the selected packs as well as all packs not disabled by the world config.
    */
   private static Collection<String> discoverNewPacks(PackRepository pPackRepository, WorldData pWorldData, Collection<String> pSelectedIds) {
      pPackRepository.reload();
      Collection<String> collection = Lists.newArrayList(pSelectedIds);
      Collection<String> collection1 = pWorldData.getDataConfiguration().dataPacks().getDisabled();

      for(String s : pPackRepository.getAvailableIds()) {
         if (!collection1.contains(s) && !collection.contains(s)) {
            collection.add(s);
         }
      }

      return collection;
   }

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("reload").requires((p_138231_) -> {
         return p_138231_.hasPermission(2);
      }).executes((p_288528_) -> {
         CommandSourceStack commandsourcestack = p_288528_.getSource();
         MinecraftServer minecraftserver = commandsourcestack.getServer();
         PackRepository packrepository = minecraftserver.getPackRepository();
         WorldData worlddata = minecraftserver.getWorldData();
         Collection<String> collection = packrepository.getSelectedIds();
         Collection<String> collection1 = discoverNewPacks(packrepository, worlddata, collection);
         commandsourcestack.sendSuccess(() -> {
            return Component.translatable("commands.reload.success");
         }, true);
         reloadPacks(collection1, commandsourcestack);
         return 0;
      }));
   }
}