package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
   public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
   private static final int MAX_ARGUMENT_COUNT = 8;
   private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

   public ArgumentSignatures(FriendlyByteBuf pBuffer) {
      this(pBuffer.<ArgumentSignatures.Entry, List<ArgumentSignatures.Entry>>readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
   }

   @Nullable
   public MessageSignature get(String pKey) {
      for(ArgumentSignatures.Entry argumentsignatures$entry : this.entries) {
         if (argumentsignatures$entry.name.equals(pKey)) {
            return argumentsignatures$entry.signature;
         }
      }

      return null;
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.entries, (p_241214_, p_241215_) -> {
         p_241215_.write(p_241214_);
      });
   }

   public static ArgumentSignatures signCommand(SignableCommand<?> pCommand, ArgumentSignatures.Signer pSigner) {
      List<ArgumentSignatures.Entry> list = pCommand.arguments().stream().map((p_247962_) -> {
         MessageSignature messagesignature = pSigner.sign(p_247962_.value());
         return messagesignature != null ? new ArgumentSignatures.Entry(p_247962_.name(), messagesignature) : null;
      }).filter(Objects::nonNull).toList();
      return new ArgumentSignatures(list);
   }

   public static record Entry(String name, MessageSignature signature) {
      public Entry(FriendlyByteBuf pBuffer) {
         this(pBuffer.readUtf(16), MessageSignature.read(pBuffer));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeUtf(this.name, 16);
         MessageSignature.write(pBuffer, this.signature);
      }
   }

   @FunctionalInterface
   public interface Signer {
      @Nullable
      MessageSignature sign(String pArgumentText);
   }
}