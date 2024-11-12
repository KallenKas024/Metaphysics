package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class SelectorContents implements ComponentContents {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String pattern;
   @Nullable
   private final EntitySelector selector;
   protected final Optional<Component> separator;

   public SelectorContents(String pPattern, Optional<Component> pSeparator) {
      this.pattern = pPattern;
      this.separator = pSeparator;
      this.selector = parseSelector(pPattern);
   }

   @Nullable
   private static EntitySelector parseSelector(String pSelector) {
      EntitySelector entityselector = null;

      try {
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(new StringReader(pSelector));
         entityselector = entityselectorparser.parse();
      } catch (CommandSyntaxException commandsyntaxexception) {
         LOGGER.warn("Invalid selector component: {}: {}", pSelector, commandsyntaxexception.getMessage());
      }

      return entityselector;
   }

   public String getPattern() {
      return this.pattern;
   }

   @Nullable
   public EntitySelector getSelector() {
      return this.selector;
   }

   public Optional<Component> getSeparator() {
      return this.separator;
   }

   public MutableComponent resolve(@Nullable CommandSourceStack pNbtPathPattern, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      if (pNbtPathPattern != null && this.selector != null) {
         Optional<? extends Component> optional = ComponentUtils.updateForEntity(pNbtPathPattern, this.separator, pEntity, pRecursionDepth);
         return ComponentUtils.formatList(this.selector.findEntities(pNbtPathPattern), optional, Entity::getDisplayName);
      } else {
         return Component.empty();
      }
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pStyledContentConsumer, Style pStyle) {
      return pStyledContentConsumer.accept(pStyle, this.pattern);
   }

   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pContentConsumer) {
      return pContentConsumer.accept(this.pattern);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof SelectorContents) {
            SelectorContents selectorcontents = (SelectorContents)pOther;
            if (this.pattern.equals(selectorcontents.pattern) && this.separator.equals(selectorcontents.separator)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = this.pattern.hashCode();
      return 31 * i + this.separator.hashCode();
   }

   public String toString() {
      return "pattern{" + this.pattern + "}";
   }
}