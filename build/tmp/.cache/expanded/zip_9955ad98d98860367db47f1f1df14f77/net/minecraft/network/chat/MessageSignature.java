package net.minecraft.network.chat;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
   public static final Codec<MessageSignature> CODEC = ExtraCodecs.BASE64_STRING.xmap(MessageSignature::new, MessageSignature::bytes);
   public static final int BYTES = 256;

   public MessageSignature {
      Preconditions.checkState(bytes.length == 256, "Invalid message signature size");
   }

   public static MessageSignature read(FriendlyByteBuf pBuffer) {
      byte[] abyte = new byte[256];
      pBuffer.readBytes(abyte);
      return new MessageSignature(abyte);
   }

   public static void write(FriendlyByteBuf pBuffer, MessageSignature pSignature) {
      pBuffer.writeBytes(pSignature.bytes);
   }

   public boolean verify(SignatureValidator pValidator, SignatureUpdater pUpdater) {
      return pValidator.validate(pUpdater, this.bytes);
   }

   public ByteBuffer asByteBuffer() {
      return ByteBuffer.wrap(this.bytes);
   }

   public boolean equals(Object pOther) {
      if (this != pOther) {
         if (pOther instanceof MessageSignature) {
            MessageSignature messagesignature = (MessageSignature)pOther;
            if (Arrays.equals(this.bytes, messagesignature.bytes)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   public String toString() {
      return Base64.getEncoder().encodeToString(this.bytes);
   }

   public MessageSignature.Packed pack(MessageSignatureCache pSignatureCache) {
      int i = pSignatureCache.pack(this);
      return i != -1 ? new MessageSignature.Packed(i) : new MessageSignature.Packed(this);
   }

   public static record Packed(int id, @Nullable MessageSignature fullSignature) {
      public static final int FULL_SIGNATURE = -1;

      public Packed(MessageSignature pFullSignature) {
         this(-1, pFullSignature);
      }

      public Packed(int pId) {
         this(pId, (MessageSignature)null);
      }

      public static MessageSignature.Packed read(FriendlyByteBuf pBuffer) {
         int i = pBuffer.readVarInt() - 1;
         return i == -1 ? new MessageSignature.Packed(MessageSignature.read(pBuffer)) : new MessageSignature.Packed(i);
      }

      public static void write(FriendlyByteBuf pBuffer, MessageSignature.Packed pPacked) {
         pBuffer.writeVarInt(pPacked.id() + 1);
         if (pPacked.fullSignature() != null) {
            MessageSignature.write(pBuffer, pPacked.fullSignature());
         }

      }

      public Optional<MessageSignature> unpack(MessageSignatureCache pSignatureCache) {
         return this.fullSignature != null ? Optional.of(this.fullSignature) : Optional.ofNullable(pSignatureCache.unpack(this.id));
      }
   }
}