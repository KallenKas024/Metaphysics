package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class LootParams {
   private final ServerLevel level;
   private final Map<LootContextParam<?>, Object> params;
   private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops;
   private final float luck;

   public LootParams(ServerLevel pLevel, Map<LootContextParam<?>, Object> pParams, Map<ResourceLocation, LootParams.DynamicDrop> pDynamicDrops, float pLuck) {
      this.level = pLevel;
      this.params = pParams;
      this.dynamicDrops = pDynamicDrops;
      this.luck = pLuck;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public boolean hasParam(LootContextParam<?> pParam) {
      return this.params.containsKey(pParam);
   }

   public <T> T getParameter(LootContextParam<T> pParam) {
      T t = (T)this.params.get(pParam);
      if (t == null) {
         throw new NoSuchElementException(pParam.getName().toString());
      } else {
         return t;
      }
   }

   @Nullable
   public <T> T getOptionalParameter(LootContextParam<T> pParam) {
      return (T)this.params.get(pParam);
   }

   @Nullable
   public <T> T getParamOrNull(LootContextParam<T> pParam) {
      return (T)this.params.get(pParam);
   }

   public void addDynamicDrops(ResourceLocation pLocation, Consumer<ItemStack> pConsumer) {
      LootParams.DynamicDrop lootparams$dynamicdrop = this.dynamicDrops.get(pLocation);
      if (lootparams$dynamicdrop != null) {
         lootparams$dynamicdrop.add(pConsumer);
      }

   }

   public float getLuck() {
      return this.luck;
   }

   public static class Builder {
      private final ServerLevel level;
      private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
      private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops = Maps.newHashMap();
      private float luck;

      public Builder(ServerLevel pLevel) {
         this.level = pLevel;
      }

      public ServerLevel getLevel() {
         return this.level;
      }

      public <T> LootParams.Builder withParameter(LootContextParam<T> pParameter, T pValue) {
         this.params.put(pParameter, pValue);
         return this;
      }

      public <T> LootParams.Builder withOptionalParameter(LootContextParam<T> pParameter, @Nullable T pValue) {
         if (pValue == null) {
            this.params.remove(pParameter);
         } else {
            this.params.put(pParameter, pValue);
         }

         return this;
      }

      public <T> T getParameter(LootContextParam<T> pParameter) {
         T t = (T)this.params.get(pParameter);
         if (t == null) {
            throw new NoSuchElementException(pParameter.getName().toString());
         } else {
            return t;
         }
      }

      @Nullable
      public <T> T getOptionalParameter(LootContextParam<T> pParameter) {
         return (T)this.params.get(pParameter);
      }

      public LootParams.Builder withDynamicDrop(ResourceLocation pName, LootParams.DynamicDrop pDynamicDrop) {
         LootParams.DynamicDrop lootparams$dynamicdrop = this.dynamicDrops.put(pName, pDynamicDrop);
         if (lootparams$dynamicdrop != null) {
            throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
         } else {
            return this;
         }
      }

      public LootParams.Builder withLuck(float pLuck) {
         this.luck = pLuck;
         return this;
      }

      public LootParams create(LootContextParamSet pParams) {
         Set<LootContextParam<?>> set = Sets.difference(this.params.keySet(), pParams.getAllowed());
         if (false && !set.isEmpty()) { // Forge: Allow mods to pass custom loot parameters (not part of the vanilla loot table) to the loot context.
            throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
         } else {
            Set<LootContextParam<?>> set1 = Sets.difference(pParams.getRequired(), this.params.keySet());
            if (!set1.isEmpty()) {
               throw new IllegalArgumentException("Missing required parameters: " + set1);
            } else {
               return new LootParams(this.level, this.params, this.dynamicDrops, this.luck);
            }
         }
      }
   }

   @FunctionalInterface
   public interface DynamicDrop {
      void add(Consumer<ItemStack> pOutput);
   }
}
