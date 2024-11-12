package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
   private final boolean reset;
   private final Map<ResourceLocation, Advancement.Builder> added;
   private final Set<ResourceLocation> removed;
   private final Map<ResourceLocation, AdvancementProgress> progress;

   public ClientboundUpdateAdvancementsPacket(boolean pReset, Collection<Advancement> pAdded, Set<ResourceLocation> pRemoved, Map<ResourceLocation, AdvancementProgress> pProgress) {
      this.reset = pReset;
      ImmutableMap.Builder<ResourceLocation, Advancement.Builder> builder = ImmutableMap.builder();

      for(Advancement advancement : pAdded) {
         builder.put(advancement.getId(), advancement.deconstruct());
      }

      this.added = builder.build();
      this.removed = ImmutableSet.copyOf(pRemoved);
      this.progress = ImmutableMap.copyOf(pProgress);
   }

   public ClientboundUpdateAdvancementsPacket(FriendlyByteBuf pBuffer) {
      this.reset = pBuffer.readBoolean();
      this.added = pBuffer.readMap(FriendlyByteBuf::readResourceLocation, Advancement.Builder::fromNetwork);
      this.removed = pBuffer.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
      this.progress = pBuffer.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBoolean(this.reset);
      pBuffer.writeMap(this.added, FriendlyByteBuf::writeResourceLocation, (p_179441_, p_179442_) -> {
         p_179442_.serializeToNetwork(p_179441_);
      });
      pBuffer.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
      pBuffer.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (p_179444_, p_179445_) -> {
         p_179445_.serializeToNetwork(p_179444_);
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleUpdateAdvancementsPacket(this);
   }

   public Map<ResourceLocation, Advancement.Builder> getAdded() {
      return this.added;
   }

   public Set<ResourceLocation> getRemoved() {
      return this.removed;
   }

   public Map<ResourceLocation, AdvancementProgress> getProgress() {
      return this.progress;
   }

   public boolean shouldReset() {
      return this.reset;
   }
}