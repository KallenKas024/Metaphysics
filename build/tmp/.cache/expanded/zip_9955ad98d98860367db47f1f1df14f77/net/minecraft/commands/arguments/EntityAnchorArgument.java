package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityAnchorArgument implements ArgumentType<EntityAnchorArgument.Anchor> {
   private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
   private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType((p_90357_) -> {
      return Component.translatable("argument.anchor.invalid", p_90357_);
   });

   public static EntityAnchorArgument.Anchor getAnchor(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, EntityAnchorArgument.Anchor.class);
   }

   public static EntityAnchorArgument anchor() {
      return new EntityAnchorArgument();
   }

   public EntityAnchorArgument.Anchor parse(StringReader pReader) throws CommandSyntaxException {
      int i = pReader.getCursor();
      String s = pReader.readUnquotedString();
      EntityAnchorArgument.Anchor entityanchorargument$anchor = EntityAnchorArgument.Anchor.getByName(s);
      if (entityanchorargument$anchor == null) {
         pReader.setCursor(i);
         throw ERROR_INVALID.createWithContext(pReader, s);
      } else {
         return entityanchorargument$anchor;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggest(EntityAnchorArgument.Anchor.BY_NAME.keySet(), pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static enum Anchor {
      FEET("feet", (p_90389_, p_90390_) -> {
         return p_90389_;
      }),
      EYES("eyes", (p_90382_, p_90383_) -> {
         return new Vec3(p_90382_.x, p_90382_.y + (double)p_90383_.getEyeHeight(), p_90382_.z);
      });

      static final Map<String, EntityAnchorArgument.Anchor> BY_NAME = Util.make(Maps.newHashMap(), (p_90387_) -> {
         for(EntityAnchorArgument.Anchor entityanchorargument$anchor : values()) {
            p_90387_.put(entityanchorargument$anchor.name, entityanchorargument$anchor);
         }

      });
      private final String name;
      private final BiFunction<Vec3, Entity, Vec3> transform;

      private Anchor(String pName, BiFunction<Vec3, Entity, Vec3> pTransform) {
         this.name = pName;
         this.transform = pTransform;
      }

      @Nullable
      public static EntityAnchorArgument.Anchor getByName(String pName) {
         return BY_NAME.get(pName);
      }

      /**
       * Gets the coordinate based on the given entity's position.
       */
      public Vec3 apply(Entity pEntity) {
         return this.transform.apply(pEntity.position(), pEntity);
      }

      /**
       * Gets the coordinate based on the given command source's position. If the source is not an entity, no offsetting
       * occurs.
       */
      public Vec3 apply(CommandSourceStack pSource) {
         Entity entity = pSource.getEntity();
         return entity == null ? pSource.getPosition() : this.transform.apply(pSource.getPosition(), entity);
      }
   }
}