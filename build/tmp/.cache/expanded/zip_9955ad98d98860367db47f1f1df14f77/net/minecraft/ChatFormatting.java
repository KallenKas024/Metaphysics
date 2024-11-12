package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;

public enum ChatFormatting implements StringRepresentable {
   BLACK("BLACK", '0', 0, 0),
   DARK_BLUE("DARK_BLUE", '1', 1, 170),
   DARK_GREEN("DARK_GREEN", '2', 2, 43520),
   DARK_AQUA("DARK_AQUA", '3', 3, 43690),
   DARK_RED("DARK_RED", '4', 4, 11141120),
   DARK_PURPLE("DARK_PURPLE", '5', 5, 11141290),
   GOLD("GOLD", '6', 6, 16755200),
   GRAY("GRAY", '7', 7, 11184810),
   DARK_GRAY("DARK_GRAY", '8', 8, 5592405),
   BLUE("BLUE", '9', 9, 5592575),
   GREEN("GREEN", 'a', 10, 5635925),
   AQUA("AQUA", 'b', 11, 5636095),
   RED("RED", 'c', 12, 16733525),
   LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 16733695),
   YELLOW("YELLOW", 'e', 14, 16777045),
   WHITE("WHITE", 'f', 15, 16777215),
   OBFUSCATED("OBFUSCATED", 'k', true),
   BOLD("BOLD", 'l', true),
   STRIKETHROUGH("STRIKETHROUGH", 'm', true),
   UNDERLINE("UNDERLINE", 'n', true),
   ITALIC("ITALIC", 'o', true),
   RESET("RESET", 'r', -1, (Integer)null);

   public static final Codec<ChatFormatting> CODEC = StringRepresentable.fromEnum(ChatFormatting::values);
   public static final char PREFIX_CODE = '\u00a7';
   private static final Map<String, ChatFormatting> FORMATTING_BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_126660_) -> {
      return cleanName(p_126660_.name);
   }, (p_126652_) -> {
      return p_126652_;
   }));
   private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
   /** The name of this color/formatting */
   private final String name;
   private final char code;
   private final boolean isFormat;
   private final String toString;
   /** The numerical index that represents this color */
   private final int id;
   @Nullable
   private final Integer color;

   private static String cleanName(String pString) {
      return pString.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
   }

   private ChatFormatting(String pName, char pCode, int pId, @Nullable Integer pColor) {
      this(pName, pCode, false, pId, pColor);
   }

   private ChatFormatting(String pName, char pCode, boolean pIsFormat) {
      this(pName, pCode, pIsFormat, -1, (Integer)null);
   }

   private ChatFormatting(String pName, char pCode, boolean pIsFormat, int pId, @Nullable Integer pColor) {
      this.name = pName;
      this.code = pCode;
      this.isFormat = pIsFormat;
      this.id = pId;
      this.color = pColor;
      this.toString = "\u00a7" + pCode;
   }

   public char getChar() {
      return this.code;
   }

   /**
    * Returns the numerical color index that represents this formatting
    */
   public int getId() {
      return this.id;
   }

   /**
    * False if this is just changing the color or resetting true otherwise.
    */
   public boolean isFormat() {
      return this.isFormat;
   }

   /**
    * Checks if this is a color code.
    */
   public boolean isColor() {
      return !this.isFormat && this != RESET;
   }

   @Nullable
   public Integer getColor() {
      return this.color;
   }

   /**
    * Gets the friendly name of this value.
    */
   public String getName() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public String toString() {
      return this.toString;
   }

   /**
    * Returns a copy of the given string, with formatting codes stripped away.
    */
   @Nullable
   public static String stripFormatting(@Nullable String pText) {
      return pText == null ? null : STRIP_FORMATTING_PATTERN.matcher(pText).replaceAll("");
   }

   /**
    * Gets a value by its friendly name null if the given name does not map to a defined value.
    */
   @Nullable
   public static ChatFormatting getByName(@Nullable String pFriendlyName) {
      return pFriendlyName == null ? null : FORMATTING_BY_NAME.get(cleanName(pFriendlyName));
   }

   /**
    * Get a TextFormatting from its color index
    */
   @Nullable
   public static ChatFormatting getById(int pIndex) {
      if (pIndex < 0) {
         return RESET;
      } else {
         for(ChatFormatting chatformatting : values()) {
            if (chatformatting.getId() == pIndex) {
               return chatformatting;
            }
         }

         return null;
      }
   }

   @Nullable
   public static ChatFormatting getByCode(char pFormattingCode) {
      char c0 = Character.toString(pFormattingCode).toLowerCase(Locale.ROOT).charAt(0);

      for(ChatFormatting chatformatting : values()) {
         if (chatformatting.code == c0) {
            return chatformatting;
         }
      }

      return null;
   }

   /**
    * Gets all the valid values.
    */
   public static Collection<String> getNames(boolean pGetColor, boolean pGetFancyStyling) {
      List<String> list = Lists.newArrayList();

      for(ChatFormatting chatformatting : values()) {
         if ((!chatformatting.isColor() || pGetColor) && (!chatformatting.isFormat() || pGetFancyStyling)) {
            list.add(chatformatting.getName());
         }
      }

      return list;
   }

   public String getSerializedName() {
      return this.getName();
   }
}