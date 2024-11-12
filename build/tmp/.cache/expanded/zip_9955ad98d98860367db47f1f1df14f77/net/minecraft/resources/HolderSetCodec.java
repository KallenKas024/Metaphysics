package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E> implements Codec<HolderSet<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<Holder<E>> elementCodec;
   private final Codec<List<Holder<E>>> homogenousListCodec;
   private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;
   private final Codec<net.minecraftforge.registries.holdersets.ICustomHolderSet<E>> forgeDispatchCodec;
   private final Codec<Either<net.minecraftforge.registries.holdersets.ICustomHolderSet<E>, Either<TagKey<E>, List<Holder<E>>>>> combinedCodec;

   private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> pHolderCodec, boolean pDisallowInline) {
      Codec<List<Holder<E>>> codec = ExtraCodecs.validate(pHolderCodec.listOf(), ExtraCodecs.ensureHomogenous(Holder::kind));
      return pDisallowInline ? codec : Codec.either(codec, pHolderCodec).xmap((p_206664_) -> {
         return p_206664_.map((p_206694_) -> {
            return p_206694_;
         }, List::of);
      }, (p_206684_) -> {
         return p_206684_.size() == 1 ? Either.right(p_206684_.get(0)) : Either.left(p_206684_);
      });
   }

   public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<Holder<E>> pHolderCodec, boolean pDisallowInline) {
      return new HolderSetCodec<>(pRegistryKey, pHolderCodec, pDisallowInline);
   }

   private HolderSetCodec(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<Holder<E>> pElementCodec, boolean pDisallowInline) {
      this.registryKey = pRegistryKey;
      this.elementCodec = pElementCodec;
      this.homogenousListCodec = homogenousList(pElementCodec, pDisallowInline);
      this.registryAwareCodec = Codec.either(TagKey.hashedCodec(pRegistryKey), this.homogenousListCodec);
      // FORGE: make registry-specific dispatch codec and make forge-or-vanilla either codec
      this.forgeDispatchCodec = ExtraCodecs.lazyInitializedCodec(() -> net.minecraftforge.registries.ForgeRegistries.HOLDER_SET_TYPES.get().getCodec())
          .dispatch(net.minecraftforge.registries.holdersets.ICustomHolderSet::type, type -> type.makeCodec(pRegistryKey, pElementCodec, pDisallowInline));
      this.combinedCodec = new ExtraCodecs.EitherCodec<>(this.forgeDispatchCodec, this.registryAwareCodec);
   }

   public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> pOps, T pInput) {
      if (pOps instanceof RegistryOps<T> registryops) {
         Optional<HolderGetter<E>> optional = registryops.getter(this.registryKey);
         if (optional.isPresent()) {
            HolderGetter<E> holdergetter = optional.get();
            // FORGE: use the wrapped codec to decode custom/tag/list instead of just tag/list
            return this.combinedCodec.decode(pOps, pInput).map((p_206682_) -> {
               return p_206682_.mapFirst((p_206679_) -> {
                  return p_206679_.map(java.util.function.Function.identity(), tagOrList -> tagOrList.map(holdergetter::getOrThrow, HolderSet::direct));
               });
            });
         }
      }

      return this.decodeWithoutRegistry(pOps, pInput);
   }

   public <T> DataResult<T> encode(HolderSet<E> pInput, DynamicOps<T> pOps, T pPrefix) {
      if (pOps instanceof RegistryOps<T> registryops) {
         Optional<HolderOwner<E>> optional = registryops.owner(this.registryKey);
         if (optional.isPresent()) {
            if (!pInput.canSerializeIn(optional.get())) {
               return DataResult.error(() -> {
                  return "HolderSet " + pInput + " is not valid in current registry set";
               });
            }

            // FORGE: use the dispatch codec to encode custom holdersets, otherwise fall back to vanilla tag/list
            if (pInput instanceof net.minecraftforge.registries.holdersets.ICustomHolderSet<E> customHolderSet)
                return this.forgeDispatchCodec.encode(customHolderSet, pOps, pPrefix);
            return this.registryAwareCodec.encode(pInput.unwrap().mapRight(List::copyOf), pOps, pPrefix);
         }
      }

      return this.encodeWithoutRegistry(pInput, pOps, pPrefix);
   }

   private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> pOps, T pInput) {
      return this.elementCodec.listOf().decode(pOps, pInput).flatMap((p_206666_) -> {
         List<Holder.Direct<E>> list = new ArrayList<>();

         for(Holder<E> holder : p_206666_.getFirst()) {
            if (!(holder instanceof Holder.Direct)) {
               return DataResult.error(() -> {
                  return "Can't decode element " + holder + " without registry";
               });
            }

            Holder.Direct<E> direct = (Holder.Direct)holder;
            list.add(direct);
         }

         return DataResult.success(new Pair<>(HolderSet.direct(list), p_206666_.getSecond()));
      });
   }

   private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> pInput, DynamicOps<T> pOps, T pPrefix) {
      return this.homogenousListCodec.encode(pInput.stream().toList(), pOps, pPrefix);
   }
}
