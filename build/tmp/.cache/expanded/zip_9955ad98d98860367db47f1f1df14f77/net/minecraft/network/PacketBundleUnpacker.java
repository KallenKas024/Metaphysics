package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {
   private final PacketFlow flow;

   public PacketBundleUnpacker(PacketFlow pFlow) {
      this.flow = pFlow;
   }

   protected void encode(ChannelHandlerContext pContext, Packet<?> pPacket, List<Object> p_265735_) throws Exception {
      BundlerInfo.Provider bundlerinfo$provider = pContext.channel().attr(BundlerInfo.BUNDLER_PROVIDER).get();
      if (bundlerinfo$provider == null) {
         throw new EncoderException("Bundler not configured: " + pPacket);
      } else {
         bundlerinfo$provider.getBundlerInfo(this.flow).unbundlePacket(pPacket, p_265735_::add);
      }
   }
}