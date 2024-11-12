package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ParticleArgument implements ArgumentType<ParticleOptions> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle with options");
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType((p_103941_) -> {
      return Component.translatable("particle.notFound", p_103941_);
   });
   private final HolderLookup<ParticleType<?>> particles;

   public ParticleArgument(CommandBuildContext pBuildContext) {
      this.particles = pBuildContext.holderLookup(Registries.PARTICLE_TYPE);
   }

   public static ParticleArgument particle(CommandBuildContext pBuildContext) {
      return new ParticleArgument(pBuildContext);
   }

   public static ParticleOptions getParticle(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, ParticleOptions.class);
   }

   public ParticleOptions parse(StringReader pReader) throws CommandSyntaxException {
      return readParticle(pReader, this.particles);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static ParticleOptions readParticle(StringReader pReader, HolderLookup<ParticleType<?>> pParticleTypeLookup) throws CommandSyntaxException {
      ParticleType<?> particletype = readParticleType(pReader, pParticleTypeLookup);
      return readParticle(pReader, particletype);
   }

   private static ParticleType<?> readParticleType(StringReader pReader, HolderLookup<ParticleType<?>> pParticleTypeLookup) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.read(pReader);
      ResourceKey<ParticleType<?>> resourcekey = ResourceKey.create(Registries.PARTICLE_TYPE, resourcelocation);
      return pParticleTypeLookup.get(resourcekey).orElseThrow(() -> {
         return ERROR_UNKNOWN_PARTICLE.create(resourcelocation);
      }).value();
   }

   /**
    * Deserializes a particle once its type is known.
    */
   private static <T extends ParticleOptions> T readParticle(StringReader pReader, ParticleType<T> pType) throws CommandSyntaxException {
      return pType.getDeserializer().fromCommand(pType, pReader);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggestResource(this.particles.listElementIds().map(ResourceKey::location), pBuilder);
   }
}