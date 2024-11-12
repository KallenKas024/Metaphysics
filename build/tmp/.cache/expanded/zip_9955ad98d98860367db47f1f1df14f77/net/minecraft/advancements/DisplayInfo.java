package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
   private final Component title;
   private final Component description;
   private final ItemStack icon;
   @Nullable
   private final ResourceLocation background;
   private final FrameType frame;
   private final boolean showToast;
   private final boolean announceChat;
   private final boolean hidden;
   private float x;
   private float y;

   public DisplayInfo(ItemStack pIcon, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceChat, boolean pHidden) {
      this.title = pTitle;
      this.description = pDescription;
      this.icon = pIcon;
      this.background = pBackground;
      this.frame = pFrame;
      this.showToast = pShowToast;
      this.announceChat = pAnnounceChat;
      this.hidden = pHidden;
   }

   public void setLocation(float pX, float pY) {
      this.x = pX;
      this.y = pY;
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getDescription() {
      return this.description;
   }

   public ItemStack getIcon() {
      return this.icon;
   }

   @Nullable
   public ResourceLocation getBackground() {
      return this.background;
   }

   public FrameType getFrame() {
      return this.frame;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public boolean shouldShowToast() {
      return this.showToast;
   }

   public boolean shouldAnnounceChat() {
      return this.announceChat;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public static DisplayInfo fromJson(JsonObject pJson) {
      Component component = Component.Serializer.fromJson(pJson.get("title"));
      Component component1 = Component.Serializer.fromJson(pJson.get("description"));
      if (component != null && component1 != null) {
         ItemStack itemstack = getIcon(GsonHelper.getAsJsonObject(pJson, "icon"));
         ResourceLocation resourcelocation = pJson.has("background") ? new ResourceLocation(GsonHelper.getAsString(pJson, "background")) : null;
         FrameType frametype = pJson.has("frame") ? FrameType.byName(GsonHelper.getAsString(pJson, "frame")) : FrameType.TASK;
         boolean flag = GsonHelper.getAsBoolean(pJson, "show_toast", true);
         boolean flag1 = GsonHelper.getAsBoolean(pJson, "announce_to_chat", true);
         boolean flag2 = GsonHelper.getAsBoolean(pJson, "hidden", false);
         return new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, flag1, flag2);
      } else {
         throw new JsonSyntaxException("Both title and description must be set");
      }
   }

   private static ItemStack getIcon(JsonObject pJson) {
      if (!pJson.has("item")) {
         throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
      } else {
         Item item = GsonHelper.getAsItem(pJson, "item");
         if (pJson.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            ItemStack itemstack = new ItemStack(item);
            if (pJson.has("nbt")) {
               try {
                  CompoundTag compoundtag = TagParser.parseTag(GsonHelper.convertToString(pJson.get("nbt"), "nbt"));
                  itemstack.setTag(compoundtag);
               } catch (CommandSyntaxException commandsyntaxexception) {
                  throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
               }
            }

            return itemstack;
         }
      }
   }

   public void serializeToNetwork(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.title);
      pBuffer.writeComponent(this.description);
      pBuffer.writeItem(this.icon);
      pBuffer.writeEnum(this.frame);
      int i = 0;
      if (this.background != null) {
         i |= 1;
      }

      if (this.showToast) {
         i |= 2;
      }

      if (this.hidden) {
         i |= 4;
      }

      pBuffer.writeInt(i);
      if (this.background != null) {
         pBuffer.writeResourceLocation(this.background);
      }

      pBuffer.writeFloat(this.x);
      pBuffer.writeFloat(this.y);
   }

   public static DisplayInfo fromNetwork(FriendlyByteBuf pBuffer) {
      Component component = pBuffer.readComponent();
      Component component1 = pBuffer.readComponent();
      ItemStack itemstack = pBuffer.readItem();
      FrameType frametype = pBuffer.readEnum(FrameType.class);
      int i = pBuffer.readInt();
      ResourceLocation resourcelocation = (i & 1) != 0 ? pBuffer.readResourceLocation() : null;
      boolean flag = (i & 2) != 0;
      boolean flag1 = (i & 4) != 0;
      DisplayInfo displayinfo = new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, false, flag1);
      displayinfo.setLocation(pBuffer.readFloat(), pBuffer.readFloat());
      return displayinfo;
   }

   public JsonElement serializeToJson() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("icon", this.serializeIcon());
      jsonobject.add("title", Component.Serializer.toJsonTree(this.title));
      jsonobject.add("description", Component.Serializer.toJsonTree(this.description));
      jsonobject.addProperty("frame", this.frame.getName());
      jsonobject.addProperty("show_toast", this.showToast);
      jsonobject.addProperty("announce_to_chat", this.announceChat);
      jsonobject.addProperty("hidden", this.hidden);
      if (this.background != null) {
         jsonobject.addProperty("background", this.background.toString());
      }

      return jsonobject;
   }

   private JsonObject serializeIcon() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.icon.getItem()).toString());
      if (this.icon.hasTag()) {
         jsonobject.addProperty("nbt", this.icon.getTag().toString());
      }

      return jsonobject;
   }
}