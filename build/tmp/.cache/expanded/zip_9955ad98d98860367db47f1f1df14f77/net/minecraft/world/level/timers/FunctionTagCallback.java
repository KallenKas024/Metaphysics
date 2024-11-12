package net.minecraft.world.level.timers;

import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionTagCallback implements TimerCallback<MinecraftServer> {
   final ResourceLocation tagId;

   public FunctionTagCallback(ResourceLocation pTagId) {
      this.tagId = pTagId;
   }

   public void handle(MinecraftServer pObj, TimerQueue<MinecraftServer> pManager, long pGameTime) {
      ServerFunctionManager serverfunctionmanager = pObj.getFunctions();

      for(CommandFunction commandfunction : serverfunctionmanager.getTag(this.tagId)) {
         serverfunctionmanager.execute(commandfunction, serverfunctionmanager.getGameLoopSender());
      }

   }

   public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionTagCallback> {
      public Serializer() {
         super(new ResourceLocation("function_tag"), FunctionTagCallback.class);
      }

      public void serialize(CompoundTag p_82206_, FunctionTagCallback p_82207_) {
         p_82206_.putString("Name", p_82207_.tagId.toString());
      }

      public FunctionTagCallback deserialize(CompoundTag p_82204_) {
         ResourceLocation resourcelocation = new ResourceLocation(p_82204_.getString("Name"));
         return new FunctionTagCallback(resourcelocation);
      }
   }
}