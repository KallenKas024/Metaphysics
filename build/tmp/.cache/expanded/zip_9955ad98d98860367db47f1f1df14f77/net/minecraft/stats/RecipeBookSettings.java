package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
   private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = new java.util.HashMap<>(ImmutableMap.of(RecipeBookType.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookType.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookType.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookType.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable")));
   private final Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

   private RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.TypeSettings> pStates) {
      this.states = pStates;
   }

   public RecipeBookSettings() {
      this(Util.make(Maps.newEnumMap(RecipeBookType.class), (p_12740_) -> {
         for(RecipeBookType recipebooktype : RecipeBookType.values()) {
            p_12740_.put(recipebooktype, new RecipeBookSettings.TypeSettings(false, false));
         }

      }));
   }

   public boolean isOpen(RecipeBookType pBookType) {
      return (this.states.get(pBookType)).open;
   }

   public void setOpen(RecipeBookType pBookType, boolean pOpen) {
      (this.states.get(pBookType)).open = pOpen;
   }

   public boolean isFiltering(RecipeBookType pBookType) {
      return (this.states.get(pBookType)).filtering;
   }

   public void setFiltering(RecipeBookType pBookType, boolean pFiltering) {
      (this.states.get(pBookType)).filtering = pFiltering;
   }

   public static RecipeBookSettings read(FriendlyByteBuf pBuffer) {
      Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);

      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         boolean flag = pBuffer.readBoolean();
         boolean flag1 = pBuffer.readBoolean();
         map.put(recipebooktype, new RecipeBookSettings.TypeSettings(flag, flag1));
      }

      return new RecipeBookSettings(map);
   }

   public void write(FriendlyByteBuf pBuffer) {
      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         RecipeBookSettings.TypeSettings recipebooksettings$typesettings = this.states.get(recipebooktype);
         if (recipebooksettings$typesettings == null) {
            pBuffer.writeBoolean(false);
            pBuffer.writeBoolean(false);
         } else {
            pBuffer.writeBoolean(recipebooksettings$typesettings.open);
            pBuffer.writeBoolean(recipebooksettings$typesettings.filtering);
         }
      }

   }

   public static RecipeBookSettings read(CompoundTag pTag) {
      Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);
      TAG_FIELDS.forEach((p_12750_, p_12751_) -> {
         boolean flag = pTag.getBoolean(p_12751_.getFirst());
         boolean flag1 = pTag.getBoolean(p_12751_.getSecond());
         map.put(p_12750_, new RecipeBookSettings.TypeSettings(flag, flag1));
      });
      return new RecipeBookSettings(map);
   }

   public void write(CompoundTag pTag) {
      TAG_FIELDS.forEach((p_12745_, p_12746_) -> {
         RecipeBookSettings.TypeSettings recipebooksettings$typesettings = this.states.get(p_12745_);
         pTag.putBoolean(p_12746_.getFirst(), recipebooksettings$typesettings.open);
         pTag.putBoolean(p_12746_.getSecond(), recipebooksettings$typesettings.filtering);
      });
   }

   public RecipeBookSettings copy() {
      Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);

      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         RecipeBookSettings.TypeSettings recipebooksettings$typesettings = this.states.get(recipebooktype);
         map.put(recipebooktype, recipebooksettings$typesettings.copy());
      }

      return new RecipeBookSettings(map);
   }

   public void replaceFrom(RecipeBookSettings pOther) {
      this.states.clear();

      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         RecipeBookSettings.TypeSettings recipebooksettings$typesettings = pOther.states.get(recipebooktype);
         this.states.put(recipebooktype, recipebooksettings$typesettings.copy());
      }

   }

   public boolean equals(Object pOther) {
      return this == pOther || pOther instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings)pOther).states);
   }

   public int hashCode() {
      return this.states.hashCode();
   }

   static final class TypeSettings {
      boolean open;
      boolean filtering;

      public TypeSettings(boolean pOpen, boolean pFiltering) {
         this.open = pOpen;
         this.filtering = pFiltering;
      }

      public RecipeBookSettings.TypeSettings copy() {
         return new RecipeBookSettings.TypeSettings(this.open, this.filtering);
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (!(pOther instanceof RecipeBookSettings.TypeSettings)) {
            return false;
         } else {
            RecipeBookSettings.TypeSettings recipebooksettings$typesettings = (RecipeBookSettings.TypeSettings)pOther;
            return this.open == recipebooksettings$typesettings.open && this.filtering == recipebooksettings$typesettings.filtering;
         }
      }

      public int hashCode() {
         int i = this.open ? 1 : 0;
         return 31 * i + (this.filtering ? 1 : 0);
      }

      public String toString() {
         return "[open=" + this.open + ", filtering=" + this.filtering + "]";
      }
   }
   //FORGE -- called automatically on Enum creation - used for serialization
   public static void addTagsForType(RecipeBookType type, String openTag, String filteringTag) {
      TAG_FIELDS.put(type, Pair.of(openTag, filteringTag));
   }
}
