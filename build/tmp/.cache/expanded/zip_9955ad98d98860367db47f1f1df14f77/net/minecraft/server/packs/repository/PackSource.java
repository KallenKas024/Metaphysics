package net.minecraft.server.packs.repository;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface PackSource {
   UnaryOperator<Component> NO_DECORATION = UnaryOperator.identity();
   PackSource DEFAULT = create(NO_DECORATION, true);
   PackSource BUILT_IN = create(decorateWithSource("pack.source.builtin"), true);
   PackSource FEATURE = create(decorateWithSource("pack.source.feature"), false);
   PackSource WORLD = create(decorateWithSource("pack.source.world"), true);
   PackSource SERVER = create(decorateWithSource("pack.source.server"), true);

   Component decorate(Component pName);

   boolean shouldAddAutomatically();

   static PackSource create(final UnaryOperator<Component> pDecorator, final boolean pShouldAddAutomatically) {
      return new PackSource() {
         public Component decorate(Component p_251609_) {
            return pDecorator.apply(p_251609_);
         }

         public boolean shouldAddAutomatically() {
            return pShouldAddAutomatically;
         }
      };
   }

   private static UnaryOperator<Component> decorateWithSource(String pTranslationKey) {
      Component component = Component.translatable(pTranslationKey);
      return (p_10539_) -> {
         return Component.translatable("pack.nameAndSource", p_10539_, component).withStyle(ChatFormatting.GRAY);
      };
   }
}