package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.util.Crypt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureValidator;

public record ProfilePublicKey(ProfilePublicKey.Data data) {
   public static final Component EXPIRED_PROFILE_PUBLIC_KEY = Component.translatable("multiplayer.disconnect.expired_public_key");
   private static final Component INVALID_SIGNATURE = Component.translatable("multiplayer.disconnect.invalid_public_key_signature.new");
   public static final Duration EXPIRY_GRACE_PERIOD = Duration.ofHours(8L);
   public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.xmap(ProfilePublicKey::new, ProfilePublicKey::data);

   public static ProfilePublicKey createValidated(SignatureValidator pSignatureValidator, UUID pProfileId, ProfilePublicKey.Data pProfilePublicKeyData, Duration pGracePeriod) throws ProfilePublicKey.ValidationException {
      if (pProfilePublicKeyData.hasExpired(pGracePeriod)) {
         throw new ProfilePublicKey.ValidationException(EXPIRED_PROFILE_PUBLIC_KEY);
      } else if (!pProfilePublicKeyData.validateSignature(pSignatureValidator, pProfileId)) {
         throw new ProfilePublicKey.ValidationException(INVALID_SIGNATURE);
      } else {
         return new ProfilePublicKey(pProfilePublicKeyData);
      }
   }

   public SignatureValidator createSignatureValidator() {
      return SignatureValidator.from(this.data.key, "SHA256withRSA");
   }

   public static record Data(Instant expiresAt, PublicKey key, byte[] keySignature) {
      private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
      public static final Codec<ProfilePublicKey.Data> CODEC = RecordCodecBuilder.create((p_219814_) -> {
         return p_219814_.group(ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey.Data::expiresAt), Crypt.PUBLIC_KEY_CODEC.fieldOf("key").forGetter(ProfilePublicKey.Data::key), ExtraCodecs.BASE64_STRING.fieldOf("signature_v2").forGetter(ProfilePublicKey.Data::keySignature)).apply(p_219814_, ProfilePublicKey.Data::new);
      });

      public Data(FriendlyByteBuf pBuffer) {
         this(pBuffer.readInstant(), pBuffer.readPublicKey(), pBuffer.readByteArray(4096));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeInstant(this.expiresAt);
         pBuffer.writePublicKey(this.key);
         pBuffer.writeByteArray(this.keySignature);
      }

      boolean validateSignature(SignatureValidator pSignatureValidator, UUID pProfileId) {
         return pSignatureValidator.validate(this.signedPayload(pProfileId), this.keySignature);
      }

      private byte[] signedPayload(UUID pProfileId) {
         byte[] abyte = this.key.getEncoded();
         byte[] abyte1 = new byte[24 + abyte.length];
         ByteBuffer bytebuffer = ByteBuffer.wrap(abyte1).order(ByteOrder.BIG_ENDIAN);
         bytebuffer.putLong(pProfileId.getMostSignificantBits()).putLong(pProfileId.getLeastSignificantBits()).putLong(this.expiresAt.toEpochMilli()).put(abyte);
         return abyte1;
      }

      public boolean hasExpired() {
         return this.expiresAt.isBefore(Instant.now());
      }

      public boolean hasExpired(Duration pGracePeriod) {
         return this.expiresAt.plus(pGracePeriod).isBefore(Instant.now());
      }

      public boolean equals(Object pOther) {
         if (!(pOther instanceof ProfilePublicKey.Data profilepublickey$data)) {
            return false;
         } else {
            return this.expiresAt.equals(profilepublickey$data.expiresAt) && this.key.equals(profilepublickey$data.key) && Arrays.equals(this.keySignature, profilepublickey$data.keySignature);
         }
      }
   }

   public static class ValidationException extends ThrowingComponent {
      public ValidationException(Component p_243378_) {
         super(p_243378_);
      }
   }
}