package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NbtPredicate {
   public static final NbtPredicate ANY = new NbtPredicate((CompoundTag)null);
   @Nullable
   private final CompoundTag tag;

   public NbtPredicate(@Nullable CompoundTag pTag) {
      this.tag = pTag;
   }

   public boolean matches(ItemStack pStack) {
      return this == ANY ? true : this.matches(pStack.getTag());
   }

   public boolean matches(Entity pEntity) {
      return this == ANY ? true : this.matches(getEntityTagToCompare(pEntity));
   }

   public boolean matches(@Nullable Tag pTag) {
      if (pTag == null) {
         return this == ANY;
      } else {
         return this.tag == null || NbtUtils.compareNbt(this.tag, pTag, true);
      }
   }

   public JsonElement serializeToJson() {
      return (JsonElement)(this != ANY && this.tag != null ? new JsonPrimitive(this.tag.toString()) : JsonNull.INSTANCE);
   }

   public static NbtPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         CompoundTag compoundtag;
         try {
            compoundtag = TagParser.parseTag(GsonHelper.convertToString(pJson, "nbt"));
         } catch (CommandSyntaxException commandsyntaxexception) {
            throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
         }

         return new NbtPredicate(compoundtag);
      } else {
         return ANY;
      }
   }

   public static CompoundTag getEntityTagToCompare(Entity pEntity) {
      CompoundTag compoundtag = pEntity.saveWithoutId(new CompoundTag());
      if (pEntity instanceof Player) {
         ItemStack itemstack = ((Player)pEntity).getInventory().getSelected();
         if (!itemstack.isEmpty()) {
            compoundtag.put("SelectedItem", itemstack.save(new CompoundTag()));
         }
      }

      return compoundtag;
   }
}