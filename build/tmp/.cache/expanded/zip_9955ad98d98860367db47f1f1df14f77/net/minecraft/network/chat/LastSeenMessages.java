package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {
   public static final Codec<LastSeenMessages> CODEC = MessageSignature.CODEC.listOf().xmap(LastSeenMessages::new, LastSeenMessages::entries);
   public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
   public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

   public void updateSignature(SignatureUpdater.Output pUpdaterOutput) throws SignatureException {
      pUpdaterOutput.update(Ints.toByteArray(this.entries.size()));

      for(MessageSignature messagesignature : this.entries) {
         pUpdaterOutput.update(messagesignature.bytes());
      }

   }

   public LastSeenMessages.Packed pack(MessageSignatureCache pSignatureCache) {
      return new LastSeenMessages.Packed(this.entries.stream().map((p_253457_) -> {
         return p_253457_.pack(pSignatureCache);
      }).toList());
   }

   public static record Packed(List<MessageSignature.Packed> entries) {
      public static final LastSeenMessages.Packed EMPTY = new LastSeenMessages.Packed(List.of());

      public Packed(FriendlyByteBuf pBuffer) {
         this(pBuffer.readCollection(FriendlyByteBuf.<List<MessageSignature.Packed>>limitValue(ArrayList::new, 20), MessageSignature.Packed::read));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeCollection(this.entries, MessageSignature.Packed::write);
      }

      public Optional<LastSeenMessages> unpack(MessageSignatureCache pSignatureCache) {
         List<MessageSignature> list = new ArrayList<>(this.entries.size());

         for(MessageSignature.Packed messagesignature$packed : this.entries) {
            Optional<MessageSignature> optional = messagesignature$packed.unpack(pSignatureCache);
            if (optional.isEmpty()) {
               return Optional.empty();
            }

            list.add(optional.get());
         }

         return Optional.of(new LastSeenMessages(list));
      }
   }

   public static record Update(int offset, BitSet acknowledged) {
      public Update(FriendlyByteBuf pBuffer) {
         this(pBuffer.readVarInt(), pBuffer.readFixedBitSet(20));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeVarInt(this.offset);
         pBuffer.writeFixedBitSet(this.acknowledged, 20);
      }
   }
}