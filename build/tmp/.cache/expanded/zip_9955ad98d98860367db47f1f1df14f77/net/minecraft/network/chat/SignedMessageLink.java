package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
   public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create((p_253768_) -> {
      return p_253768_.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(SignedMessageLink::index), UUIDUtil.CODEC.fieldOf("sender").forGetter(SignedMessageLink::sender), UUIDUtil.CODEC.fieldOf("session_id").forGetter(SignedMessageLink::sessionId)).apply(p_253768_, SignedMessageLink::new);
   });

   public static SignedMessageLink unsigned(UUID pSender) {
      return root(pSender, Util.NIL_UUID);
   }

   public static SignedMessageLink root(UUID pSender, UUID pSessionId) {
      return new SignedMessageLink(0, pSender, pSessionId);
   }

   public void updateSignature(SignatureUpdater.Output pOutput) throws SignatureException {
      pOutput.update(UUIDUtil.uuidToByteArray(this.sender));
      pOutput.update(UUIDUtil.uuidToByteArray(this.sessionId));
      pOutput.update(Ints.toByteArray(this.index));
   }

   public boolean isDescendantOf(SignedMessageLink pOther) {
      return this.index > pOther.index() && this.sender.equals(pOther.sender()) && this.sessionId.equals(pOther.sessionId());
   }

   @Nullable
   public SignedMessageLink advance() {
      return this.index == Integer.MAX_VALUE ? null : new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
   }
}