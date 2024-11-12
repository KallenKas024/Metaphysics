package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
   public static final int MAX_HASH_LENGTH = 40;
   private final String url;
   private final String hash;
   private final boolean required;
   @Nullable
   private final Component prompt;

   public ClientboundResourcePackPacket(String pUrl, String pHash, boolean pRequired, @Nullable Component pPrompt) {
      if (pHash.length() > 40) {
         throw new IllegalArgumentException("Hash is too long (max 40, was " + pHash.length() + ")");
      } else {
         this.url = pUrl;
         this.hash = pHash;
         this.required = pRequired;
         this.prompt = pPrompt;
      }
   }

   public ClientboundResourcePackPacket(FriendlyByteBuf pBuffer) {
      this.url = pBuffer.readUtf();
      this.hash = pBuffer.readUtf(40);
      this.required = pBuffer.readBoolean();
      this.prompt = pBuffer.readNullable(FriendlyByteBuf::readComponent);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.url);
      pBuffer.writeUtf(this.hash);
      pBuffer.writeBoolean(this.required);
      pBuffer.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleResourcePack(this);
   }

   public String getUrl() {
      return this.url;
   }

   public String getHash() {
      return this.hash;
   }

   public boolean isRequired() {
      return this.required;
   }

   @Nullable
   public Component getPrompt() {
      return this.prompt;
   }
}