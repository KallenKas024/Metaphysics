package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
   private final List<ItemCombinerMenuSlotDefinition.SlotDefinition> slots;
   private final ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot;

   ItemCombinerMenuSlotDefinition(List<ItemCombinerMenuSlotDefinition.SlotDefinition> pSlots, ItemCombinerMenuSlotDefinition.SlotDefinition pResultSlot) {
      if (!pSlots.isEmpty() && !pResultSlot.equals(ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY)) {
         this.slots = pSlots;
         this.resultSlot = pResultSlot;
      } else {
         throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
      }
   }

   public static ItemCombinerMenuSlotDefinition.Builder create() {
      return new ItemCombinerMenuSlotDefinition.Builder();
   }

   public boolean hasSlot(int pSlot) {
      return this.slots.size() >= pSlot;
   }

   public ItemCombinerMenuSlotDefinition.SlotDefinition getSlot(int pSlot) {
      return this.slots.get(pSlot);
   }

   public ItemCombinerMenuSlotDefinition.SlotDefinition getResultSlot() {
      return this.resultSlot;
   }

   public List<ItemCombinerMenuSlotDefinition.SlotDefinition> getSlots() {
      return this.slots;
   }

   public int getNumOfInputSlots() {
      return this.slots.size();
   }

   public int getResultSlotIndex() {
      return this.getNumOfInputSlots();
   }

   public List<Integer> getInputSlotIndexes() {
      return this.slots.stream().map(ItemCombinerMenuSlotDefinition.SlotDefinition::slotIndex).collect(Collectors.toList());
   }

   public static class Builder {
      private final List<ItemCombinerMenuSlotDefinition.SlotDefinition> slots = new ArrayList<>();
      private ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot = ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY;

      public ItemCombinerMenuSlotDefinition.Builder withSlot(int pSlotIndex, int pX, int pY, Predicate<ItemStack> pMayPlace) {
         this.slots.add(new ItemCombinerMenuSlotDefinition.SlotDefinition(pSlotIndex, pX, pY, pMayPlace));
         return this;
      }

      public ItemCombinerMenuSlotDefinition.Builder withResultSlot(int pSlotIndex, int pX, int pY) {
         this.resultSlot = new ItemCombinerMenuSlotDefinition.SlotDefinition(pSlotIndex, pX, pY, (p_266825_) -> {
            return false;
         });
         return this;
      }

      public ItemCombinerMenuSlotDefinition build() {
         return new ItemCombinerMenuSlotDefinition(this.slots, this.resultSlot);
      }
   }

   public static record SlotDefinition(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {
      static final ItemCombinerMenuSlotDefinition.SlotDefinition EMPTY = new ItemCombinerMenuSlotDefinition.SlotDefinition(0, 0, 0, (p_267109_) -> {
         return true;
      });
   }
}