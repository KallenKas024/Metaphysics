package net.minecraft.world.level.block.state.predicate;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStatePredicate implements Predicate<BlockState> {
   public static final Predicate<BlockState> ANY = (p_61299_) -> {
      return true;
   };
   private final StateDefinition<Block, BlockState> definition;
   private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

   private BlockStatePredicate(StateDefinition<Block, BlockState> pDefinition) {
      this.definition = pDefinition;
   }

   public static BlockStatePredicate forBlock(Block pBlock) {
      return new BlockStatePredicate(pBlock.getStateDefinition());
   }

   public boolean test(@Nullable BlockState pState) {
      if (pState != null && pState.getBlock().equals(this.definition.getOwner())) {
         if (this.properties.isEmpty()) {
            return true;
         } else {
            for(Map.Entry<Property<?>, Predicate<Object>> entry : this.properties.entrySet()) {
               if (!this.applies(pState, entry.getKey(), entry.getValue())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   protected <T extends Comparable<T>> boolean applies(BlockState pState, Property<T> pProperty, Predicate<Object> pValuePredicate) {
      T t = pState.getValue(pProperty);
      return pValuePredicate.test(t);
   }

   public <V extends Comparable<V>> BlockStatePredicate where(Property<V> pProperty, Predicate<Object> pValuePredicate) {
      if (!this.definition.getProperties().contains(pProperty)) {
         throw new IllegalArgumentException(this.definition + " cannot support property " + pProperty);
      } else {
         this.properties.put(pProperty, pValuePredicate);
         return this;
      }
   }
}