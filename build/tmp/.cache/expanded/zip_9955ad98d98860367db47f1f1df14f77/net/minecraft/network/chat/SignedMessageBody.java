package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
   public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec((p_253722_) -> {
      return p_253722_.group(Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content), ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp), Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt), LastSeenMessages.CODEC.optionalFieldOf("last_seen", LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)).apply(p_253722_, SignedMessageBody::new);
   });

   public static SignedMessageBody unsigned(String pContent) {
      return new SignedMessageBody(pContent, Instant.now(), 0L, LastSeenMessages.EMPTY);
   }

   public void updateSignature(SignatureUpdater.Output pOutput) throws SignatureException {
      pOutput.update(Longs.toByteArray(this.salt));
      pOutput.update(Longs.toByteArray(this.timeStamp.getEpochSecond()));
      byte[] abyte = this.content.getBytes(StandardCharsets.UTF_8);
      pOutput.update(Ints.toByteArray(abyte.length));
      pOutput.update(abyte);
      this.lastSeen.updateSignature(pOutput);
   }

   public SignedMessageBody.Packed pack(MessageSignatureCache pSignatureCache) {
      return new SignedMessageBody.Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack(pSignatureCache));
   }

   public static record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
      public Packed(FriendlyByteBuf pBuffer) {
         this(pBuffer.readUtf(256), pBuffer.readInstant(), pBuffer.readLong(), new LastSeenMessages.Packed(pBuffer));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeUtf(this.content, 256);
         pBuffer.writeInstant(this.timeStamp);
         pBuffer.writeLong(this.salt);
         this.lastSeen.write(pBuffer);
      }

      public Optional<SignedMessageBody> unpack(MessageSignatureCache pSignatureCache) {
         return this.lastSeen.unpack(pSignatureCache).map((p_249065_) -> {
            return new SignedMessageBody(this.content, this.timeStamp, this.salt, p_249065_);
         });
      }
   }
}