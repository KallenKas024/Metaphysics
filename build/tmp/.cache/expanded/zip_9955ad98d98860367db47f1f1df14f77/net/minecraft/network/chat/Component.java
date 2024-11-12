package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, FormattedText {
   /**
    * Gets the style of this component.
    */
   Style getStyle();

   ComponentContents getContents();

   default String getString() {
      return FormattedText.super.getString();
   }

   /**
    * Get the plain text of this FormattedText, without any styling or formatting codes, limited to {@code maxLength}
    * characters.
    */
   default String getString(int pMaxLength) {
      StringBuilder stringbuilder = new StringBuilder();
      this.visit((p_130673_) -> {
         int i = pMaxLength - stringbuilder.length();
         if (i <= 0) {
            return STOP_ITERATION;
         } else {
            stringbuilder.append(p_130673_.length() <= i ? p_130673_ : p_130673_.substring(0, i));
            return Optional.empty();
         }
      });
      return stringbuilder.toString();
   }

   /**
    * Gets the sibling components of this one.
    */
   List<Component> getSiblings();

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   default MutableComponent plainCopy() {
      return MutableComponent.create(this.getContents());
   }

   /**
    * Creates a copy of this component and also copies the style and siblings. Note that the siblings are copied
    * shallowly, meaning the siblings themselves are not copied.
    */
   default MutableComponent copy() {
      return new MutableComponent(this.getContents(), new ArrayList<>(this.getSiblings()), this.getStyle());
   }

   FormattedCharSequence getVisualOrderText();

   default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style pStyle) {
      Style style = this.getStyle().applyTo(pStyle);
      Optional<T> optional = this.getContents().visit(pAcceptor, style);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(Component component : this.getSiblings()) {
            Optional<T> optional1 = component.visit(pAcceptor, style);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor) {
      Optional<T> optional = this.getContents().visit(pAcceptor);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(Component component : this.getSiblings()) {
            Optional<T> optional1 = component.visit(pAcceptor);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default List<Component> toFlatList() {
      return this.toFlatList(Style.EMPTY);
   }

   default List<Component> toFlatList(Style pStyle) {
      List<Component> list = Lists.newArrayList();
      this.visit((p_178403_, p_178404_) -> {
         if (!p_178404_.isEmpty()) {
            list.add(literal(p_178404_).withStyle(p_178403_));
         }

         return Optional.empty();
      }, pStyle);
      return list;
   }

   default boolean contains(Component pOther) {
      if (this.equals(pOther)) {
         return true;
      } else {
         List<Component> list = this.toFlatList();
         List<Component> list1 = pOther.toFlatList(this.getStyle());
         return Collections.indexOfSubList(list, list1) != -1;
      }
   }

   static Component nullToEmpty(@Nullable String pText) {
      return (Component)(pText != null ? literal(pText) : CommonComponents.EMPTY);
   }

   static MutableComponent literal(String pText) {
      return MutableComponent.create(new LiteralContents(pText));
   }

   static MutableComponent translatable(String pKey) {
      return MutableComponent.create(new TranslatableContents(pKey, (String)null, TranslatableContents.NO_ARGS));
   }

   static MutableComponent translatable(String pKey, Object... pArgs) {
      return MutableComponent.create(new TranslatableContents(pKey, (String)null, pArgs));
   }

   static MutableComponent translatableWithFallback(String pKey, @Nullable String pFallback) {
      return MutableComponent.create(new TranslatableContents(pKey, pFallback, TranslatableContents.NO_ARGS));
   }

   static MutableComponent translatableWithFallback(String pKey, @Nullable String pFallback, Object... pArgs) {
      return MutableComponent.create(new TranslatableContents(pKey, pFallback, pArgs));
   }

   static MutableComponent empty() {
      return MutableComponent.create(ComponentContents.EMPTY);
   }

   static MutableComponent keybind(String pName) {
      return MutableComponent.create(new KeybindContents(pName));
   }

   static MutableComponent nbt(String pNbtPathPattern, boolean pInterpreting, Optional<Component> pSeparator, DataSource pDataSource) {
      return MutableComponent.create(new NbtContents(pNbtPathPattern, pInterpreting, pSeparator, pDataSource));
   }

   static MutableComponent score(String pName, String pObjective) {
      return MutableComponent.create(new ScoreContents(pName, pObjective));
   }

   static MutableComponent selector(String pPattern, Optional<Component> pSeparator) {
      return MutableComponent.create(new SelectorContents(pPattern, pSeparator));
   }

   public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
      private static final Gson GSON = Util.make(() -> {
         GsonBuilder gsonbuilder = new GsonBuilder();
         gsonbuilder.disableHtmlEscaping();
         gsonbuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
         gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         gsonbuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         return gsonbuilder.create();
      });
      private static final Field JSON_READER_POS = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
         }
      });
      private static final Field JSON_READER_LINESTART = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
         }
      });

      public MutableComponent deserialize(JsonElement pJson, Type pTypeOfT, JsonDeserializationContext pContext) throws JsonParseException {
         if (pJson.isJsonPrimitive()) {
            return Component.literal(pJson.getAsString());
         } else if (!pJson.isJsonObject()) {
            if (pJson.isJsonArray()) {
               JsonArray jsonarray1 = pJson.getAsJsonArray();
               MutableComponent mutablecomponent1 = null;

               for(JsonElement jsonelement : jsonarray1) {
                  MutableComponent mutablecomponent2 = this.deserialize(jsonelement, jsonelement.getClass(), pContext);
                  if (mutablecomponent1 == null) {
                     mutablecomponent1 = mutablecomponent2;
                  } else {
                     mutablecomponent1.append(mutablecomponent2);
                  }
               }

               return mutablecomponent1;
            } else {
               throw new JsonParseException("Don't know how to turn " + pJson + " into a Component");
            }
         } else {
            JsonObject jsonobject = pJson.getAsJsonObject();
            MutableComponent mutablecomponent;
            if (jsonobject.has("text")) {
               String s = GsonHelper.getAsString(jsonobject, "text");
               mutablecomponent = s.isEmpty() ? Component.empty() : Component.literal(s);
            } else if (jsonobject.has("translate")) {
               String s2 = GsonHelper.getAsString(jsonobject, "translate");
               String s1 = GsonHelper.getAsString(jsonobject, "fallback", (String)null);
               if (jsonobject.has("with")) {
                  JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "with");
                  Object[] aobject = new Object[jsonarray.size()];

                  for(int i = 0; i < aobject.length; ++i) {
                     aobject[i] = unwrapTextArgument(this.deserialize(jsonarray.get(i), pTypeOfT, pContext));
                  }

                  mutablecomponent = Component.translatableWithFallback(s2, s1, aobject);
               } else {
                  mutablecomponent = Component.translatableWithFallback(s2, s1);
               }
            } else if (jsonobject.has("score")) {
               JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "score");
               if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                  throw new JsonParseException("A score component needs a least a name and an objective");
               }

               mutablecomponent = Component.score(GsonHelper.getAsString(jsonobject1, "name"), GsonHelper.getAsString(jsonobject1, "objective"));
            } else if (jsonobject.has("selector")) {
               Optional<Component> optional = this.parseSeparator(pTypeOfT, pContext, jsonobject);
               mutablecomponent = Component.selector(GsonHelper.getAsString(jsonobject, "selector"), optional);
            } else if (jsonobject.has("keybind")) {
               mutablecomponent = Component.keybind(GsonHelper.getAsString(jsonobject, "keybind"));
            } else {
               if (!jsonobject.has("nbt")) {
                  throw new JsonParseException("Don't know how to turn " + pJson + " into a Component");
               }

               String s3 = GsonHelper.getAsString(jsonobject, "nbt");
               Optional<Component> optional1 = this.parseSeparator(pTypeOfT, pContext, jsonobject);
               boolean flag = GsonHelper.getAsBoolean(jsonobject, "interpret", false);
               DataSource datasource;
               if (jsonobject.has("block")) {
                  datasource = new BlockDataSource(GsonHelper.getAsString(jsonobject, "block"));
               } else if (jsonobject.has("entity")) {
                  datasource = new EntityDataSource(GsonHelper.getAsString(jsonobject, "entity"));
               } else {
                  if (!jsonobject.has("storage")) {
                     throw new JsonParseException("Don't know how to turn " + pJson + " into a Component");
                  }

                  datasource = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(jsonobject, "storage")));
               }

               mutablecomponent = Component.nbt(s3, flag, optional1, datasource);
            }

            if (jsonobject.has("extra")) {
               JsonArray jsonarray2 = GsonHelper.getAsJsonArray(jsonobject, "extra");
               if (jsonarray2.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int j = 0; j < jsonarray2.size(); ++j) {
                  mutablecomponent.append(this.deserialize(jsonarray2.get(j), pTypeOfT, pContext));
               }
            }

            mutablecomponent.setStyle(pContext.deserialize(pJson, Style.class));
            return mutablecomponent;
         }
      }

      private static Object unwrapTextArgument(Object pObject) {
         if (pObject instanceof Component component) {
            if (component.getStyle().isEmpty() && component.getSiblings().isEmpty()) {
               ComponentContents componentcontents = component.getContents();
               if (componentcontents instanceof LiteralContents) {
                  LiteralContents literalcontents = (LiteralContents)componentcontents;
                  return literalcontents.text();
               }
            }
         }

         return pObject;
      }

      private Optional<Component> parseSeparator(Type pType, JsonDeserializationContext pJsonContext, JsonObject pJsonObject) {
         return pJsonObject.has("separator") ? Optional.of(this.deserialize(pJsonObject.get("separator"), pType, pJsonContext)) : Optional.empty();
      }

      private void serializeStyle(Style pStyle, JsonObject pObject, JsonSerializationContext pCtx) {
         JsonElement jsonelement = pCtx.serialize(pStyle);
         if (jsonelement.isJsonObject()) {
            JsonObject jsonobject = (JsonObject)jsonelement;

            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
               pObject.add(entry.getKey(), entry.getValue());
            }
         }

      }

      public JsonElement serialize(Component pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         if (!pSrc.getStyle().isEmpty()) {
            this.serializeStyle(pSrc.getStyle(), jsonobject, pContext);
         }

         if (!pSrc.getSiblings().isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Component component : pSrc.getSiblings()) {
               jsonarray.add(this.serialize(component, Component.class, pContext));
            }

            jsonobject.add("extra", jsonarray);
         }

         ComponentContents componentcontents = pSrc.getContents();
         if (componentcontents == ComponentContents.EMPTY) {
            jsonobject.addProperty("text", "");
         } else if (componentcontents instanceof LiteralContents) {
            LiteralContents literalcontents = (LiteralContents)componentcontents;
            jsonobject.addProperty("text", literalcontents.text());
         } else if (componentcontents instanceof TranslatableContents) {
            TranslatableContents translatablecontents = (TranslatableContents)componentcontents;
            jsonobject.addProperty("translate", translatablecontents.getKey());
            String s = translatablecontents.getFallback();
            if (s != null) {
               jsonobject.addProperty("fallback", s);
            }

            if (translatablecontents.getArgs().length > 0) {
               JsonArray jsonarray1 = new JsonArray();

               for(Object object : translatablecontents.getArgs()) {
                  if (object instanceof Component) {
                     jsonarray1.add(this.serialize((Component)object, object.getClass(), pContext));
                  } else {
                     jsonarray1.add(new JsonPrimitive(String.valueOf(object)));
                  }
               }

               jsonobject.add("with", jsonarray1);
            }
         } else if (componentcontents instanceof ScoreContents) {
            ScoreContents scorecontents = (ScoreContents)componentcontents;
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("name", scorecontents.getName());
            jsonobject1.addProperty("objective", scorecontents.getObjective());
            jsonobject.add("score", jsonobject1);
         } else if (componentcontents instanceof SelectorContents) {
            SelectorContents selectorcontents = (SelectorContents)componentcontents;
            jsonobject.addProperty("selector", selectorcontents.getPattern());
            this.serializeSeparator(pContext, jsonobject, selectorcontents.getSeparator());
         } else if (componentcontents instanceof KeybindContents) {
            KeybindContents keybindcontents = (KeybindContents)componentcontents;
            jsonobject.addProperty("keybind", keybindcontents.getName());
         } else {
            if (!(componentcontents instanceof NbtContents)) {
               throw new IllegalArgumentException("Don't know how to serialize " + componentcontents + " as a Component");
            }

            NbtContents nbtcontents = (NbtContents)componentcontents;
            jsonobject.addProperty("nbt", nbtcontents.getNbtPath());
            jsonobject.addProperty("interpret", nbtcontents.isInterpreting());
            this.serializeSeparator(pContext, jsonobject, nbtcontents.getSeparator());
            DataSource datasource = nbtcontents.getDataSource();
            if (datasource instanceof BlockDataSource) {
               BlockDataSource blockdatasource = (BlockDataSource)datasource;
               jsonobject.addProperty("block", blockdatasource.posPattern());
            } else if (datasource instanceof EntityDataSource) {
               EntityDataSource entitydatasource = (EntityDataSource)datasource;
               jsonobject.addProperty("entity", entitydatasource.selectorPattern());
            } else {
               if (!(datasource instanceof StorageDataSource)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + componentcontents + " as a Component");
               }

               StorageDataSource storagedatasource = (StorageDataSource)datasource;
               jsonobject.addProperty("storage", storagedatasource.id().toString());
            }
         }

         return jsonobject;
      }

      private void serializeSeparator(JsonSerializationContext pContext, JsonObject pJson, Optional<Component> pSeparator) {
         pSeparator.ifPresent((p_178410_) -> {
            pJson.add("separator", this.serialize(p_178410_, p_178410_.getClass(), pContext));
         });
      }

      /**
       * Serializes a component into JSON.
       */
      public static String toJson(Component pComponent) {
         return GSON.toJson(pComponent);
      }

      public static String toStableJson(Component pComponent) {
         return GsonHelper.toStableString(toJsonTree(pComponent));
      }

      public static JsonElement toJsonTree(Component pComponent) {
         return GSON.toJsonTree(pComponent);
      }

      @Nullable
      public static MutableComponent fromJson(String pJson) {
         return GsonHelper.fromNullableJson(GSON, pJson, MutableComponent.class, false);
      }

      @Nullable
      public static MutableComponent fromJson(JsonElement pJson) {
         return GSON.fromJson(pJson, MutableComponent.class);
      }

      @Nullable
      public static MutableComponent fromJsonLenient(String pJson) {
         return GsonHelper.fromNullableJson(GSON, pJson, MutableComponent.class, true);
      }

      public static MutableComponent fromJson(com.mojang.brigadier.StringReader pReader) {
         try {
            JsonReader jsonreader = new JsonReader(new StringReader(pReader.getRemaining()));
            jsonreader.setLenient(false);
            MutableComponent mutablecomponent = GSON.getAdapter(MutableComponent.class).read(jsonreader);
            pReader.setCursor(pReader.getCursor() + getPos(jsonreader));
            return mutablecomponent;
         } catch (StackOverflowError | IOException ioexception) {
            throw new JsonParseException(ioexception);
         }
      }

      private static int getPos(JsonReader pReader) {
         try {
            return JSON_READER_POS.getInt(pReader) - JSON_READER_LINESTART.getInt(pReader) + 1;
         } catch (IllegalAccessException illegalaccessexception) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
         }
      }
   }
}