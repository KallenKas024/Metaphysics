package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
   private static final Logger LOGGER = LogUtils.getLogger();

   /**
    * Ensures that the given packet is handled on the main thread. If the current thread is not the main thread, this
    * method
    * throws {@link net.minecraft.server.RunningOnDifferentThreadException}, which is caught and ignored in the outer
    * call ({@link net.minecraft.network.Connection#channelRead0(io.netty.channel.ChannelHandlerContext,
    * net.minecraft.network.protocol.Packet)}). Additionally, it then re-schedules the packet to be handled on the main
    * thread,
    * which will then end up back here, but this time on the main thread.
    */
   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, ServerLevel pLevel) throws RunningOnDifferentThreadException {
      ensureRunningOnSameThread(pPacket, pProcessor, pLevel.getServer());
   }

   /**
    * Ensures that the given packet is handled on the main thread. If the current thread is not the main thread, this
    * method
    * throws {@link net.minecraft.server.RunningOnDifferentThreadException}, which is caught and ignored in the outer
    * call ({@link net.minecraft.network.Connection#channelRead0(io.netty.channel.ChannelHandlerContext,
    * net.minecraft.network.protocol.Packet)}). Additionally, it then re-schedules the packet to be handled on the main
    * thread,
    * which will then end up back here, but this time on the main thread.
    */
   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, BlockableEventLoop<?> pExecutor) throws RunningOnDifferentThreadException {
      if (!pExecutor.isSameThread()) {
         pExecutor.executeIfPossible(() -> {
            if (pProcessor.isAcceptingMessages()) {
               try {
                  pPacket.handle(pProcessor);
               } catch (Exception exception) {
                  if (pProcessor.shouldPropagateHandlingExceptions()) {
                     throw exception;
                  }

                  LOGGER.error("Failed to handle packet {}, suppressing error", pPacket, exception);
               }
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)pPacket);
            }

         });
         throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
      }
   }
}