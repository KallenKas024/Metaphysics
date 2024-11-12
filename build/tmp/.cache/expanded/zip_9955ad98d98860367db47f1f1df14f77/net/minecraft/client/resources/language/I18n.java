package net.minecraft.client.resources.language;

import java.util.IllegalFormatException;
import net.minecraft.locale.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class I18n {
   private static volatile Language language = Language.getInstance();

   private I18n() {
   }

   static void setLanguage(Language pLanguage) {
      language = pLanguage;
      net.minecraftforge.common.ForgeI18n.loadLanguageData(pLanguage.getLanguageData());
   }

   /**
    * Translates the given string and then formats it. Equivalent to {@code String.format(translate(key), parameters)}.
    */
   public static String get(String pTranslateKey, Object... pParameters) {
      String s = language.getOrDefault(pTranslateKey);

      try {
         return String.format(s, pParameters);
      } catch (IllegalFormatException illegalformatexception) {
         return "Format error: " + s;
      }
   }

   public static boolean exists(String pKey) {
      return language.has(pKey);
   }
}
