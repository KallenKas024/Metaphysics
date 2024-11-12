package net.minecraft.network.protocol;

import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;

public interface BundlerInfo {
   AttributeKey<BundlerInfo.Provider> BUNDLER_PROVIDER = AttributeKey.valueOf("bundler");
   int BUNDLE_SIZE_LIMIT = 4096;
   BundlerInfo EMPTY = new BundlerInfo() {
      public void unbundlePacket(Packet<?> p_265538_, Consumer<Packet<?>> p_265064_) {
         p_265064_.accept(p_265538_);
      }

      @Nullable
      public BundlerInfo.Bundler startPacketBundling(Packet<?> p_265749_) {
         return null;
      }
   };

   static <T extends PacketListener, P extends BundlePacket<T>> BundlerInfo createForPacket(final Class<P> pPacketClass, final Function<Iterable<Packet<T>>, P> pBundler, final BundleDelimiterPacket<T> pPacket) {
      return new BundlerInfo() {
         public void unbundlePacket(Packet<?> p_265337_, Consumer<Packet<?>> p_265615_) {
            if (p_265337_.getClass() == pPacketClass) {
               P p = (P)(p_265337_);
               p_265615_.accept(pPacket);
               p.subPackets().forEach(p_265615_);
               p_265615_.accept(pPacket);
            } else {
               p_265615_.accept(p_265337_);
            }

         }

         @Nullable
         public BundlerInfo.Bundler startPacketBundling(Packet<?> p_265097_) {
            return p_265097_ == pPacket ? new BundlerInfo.Bundler() {
               private final List<Packet<T>> bundlePackets = new ArrayList<>();

               @Nullable
               public Packet<?> addPacket(Packet<?> p_265205_) {
                  if (p_265205_ == pPacket) {
                     return pBundler.apply(this.bundlePackets);
                  } else if (this.bundlePackets.size() >= 4096) {
                     throw new IllegalStateException("Too many packets in a bundle");
                  } else {
                     this.bundlePackets.add((Packet<T>)p_265205_);
                     return null;
                  }
               }
            } : null;
         }
      };
   }

   void unbundlePacket(Packet<?> pPacket, Consumer<Packet<?>> pConsumer);

   @Nullable
   BundlerInfo.Bundler startPacketBundling(Packet<?> pPacket);

   public interface Bundler {
      @Nullable
      Packet<?> addPacket(Packet<?> pPacket);
   }

   public interface Provider {
      BundlerInfo getBundlerInfo(PacketFlow pFlow);
   }
}