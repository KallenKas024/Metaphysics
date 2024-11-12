package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public record ChatTypeDecoration(String translationKey, List<ChatTypeDecoration.Parameter> parameters, Style style) {
   public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create((p_239989_) -> {
      return p_239989_.group(Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey), ChatTypeDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters), Style.FORMATTING_CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(ChatTypeDecoration::style)).apply(p_239989_, ChatTypeDecoration::new);
   });

   public static ChatTypeDecoration withSender(String pTranslationKey) {
      return new ChatTypeDecoration(pTranslationKey, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
   }

   public static ChatTypeDecoration incomingDirectMessage(String pTranslationKey) {
      Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
      return new ChatTypeDecoration(pTranslationKey, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), style);
   }

   public static ChatTypeDecoration outgoingDirectMessage(String pTranslationKey) {
      Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
      return new ChatTypeDecoration(pTranslationKey, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.CONTENT), style);
   }

   public static ChatTypeDecoration teamMessage(String pTranslationKey) {
      return new ChatTypeDecoration(pTranslationKey, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
   }

   public Component decorate(Component pContent, ChatType.Bound pBoundChatType) {
      Object[] aobject = this.resolveParameters(pContent, pBoundChatType);
      return Component.translatable(this.translationKey, aobject).withStyle(this.style);
   }

   private Component[] resolveParameters(Component pContent, ChatType.Bound pBoundChatType) {
      Component[] acomponent = new Component[this.parameters.size()];

      for(int i = 0; i < acomponent.length; ++i) {
         ChatTypeDecoration.Parameter chattypedecoration$parameter = this.parameters.get(i);
         acomponent[i] = chattypedecoration$parameter.select(pContent, pBoundChatType);
      }

      return acomponent;
   }

   public static enum Parameter implements StringRepresentable {
      SENDER("sender", (p_241238_, p_241239_) -> {
         return p_241239_.name();
      }),
      TARGET("target", (p_241236_, p_241237_) -> {
         return p_241237_.targetName();
      }),
      CONTENT("content", (p_239974_, p_241427_) -> {
         return p_239974_;
      });

      public static final Codec<ChatTypeDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatTypeDecoration.Parameter::values);
      private final String name;
      private final ChatTypeDecoration.Parameter.Selector selector;

      private Parameter(String pName, ChatTypeDecoration.Parameter.Selector pSelector) {
         this.name = pName;
         this.selector = pSelector;
      }

      public Component select(Component pContent, ChatType.Bound pBoundChatType) {
         Component component = this.selector.select(pContent, pBoundChatType);
         return Objects.requireNonNullElse(component, CommonComponents.EMPTY);
      }

      public String getSerializedName() {
         return this.name;
      }

      public interface Selector {
         @Nullable
         Component select(Component pContent, ChatType.Bound pBoundChatType);
      }
   }
}