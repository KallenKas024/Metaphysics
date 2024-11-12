package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new Gson();
   /** Pattern that matches numeric variable placeholders in a resource string, such as "%d", "%3$d", "%.2f" */
   private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
   public static final String DEFAULT = "en_us";
   private static volatile Language instance = loadDefault();

   private static Language loadDefault() {
      ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
      BiConsumer<String, String> biconsumer = builder::put;
      parseTranslations(biconsumer, "/assets/minecraft/lang/en_us.json");
      final Map<String, String> map = new java.util.HashMap<>(builder.build());
      net.minecraftforge.server.LanguageHook.captureLanguageMap(map);
      return new Language() {
         public String getOrDefault(String p_128127_, String p_265421_) {
            return map.getOrDefault(p_128127_, p_265421_);
         }

         public boolean has(String p_128135_) {
            return map.containsKey(p_128135_);
         }

         public boolean isDefaultRightToLeft() {
            return false;
         }

         public FormattedCharSequence getVisualOrder(FormattedText p_128129_) {
            return (p_128132_) -> {
               return p_128129_.visit((p_177835_, p_177836_) -> {
                  return StringDecomposer.iterateFormatted(p_177836_, p_177835_, p_128132_) ? Optional.empty() : FormattedText.STOP_ITERATION;
               }, Style.EMPTY).isPresent();
            };
         }

         @Override
         public Map<String, String> getLanguageData() {
            return map;
         }
      };
   }

   private static void parseTranslations(BiConsumer<String, String> pOutput, String pLanguagePath) {
      try (InputStream inputstream = Language.class.getResourceAsStream(pLanguagePath)) {
         loadFromJson(inputstream, pOutput);
      } catch (JsonParseException | IOException ioexception) {
         LOGGER.error("Couldn't read strings from {}", pLanguagePath, ioexception);
      }

   }

   public static void loadFromJson(InputStream pStream, BiConsumer<String, String> pOutput) {
      JsonObject jsonobject = GSON.fromJson(new InputStreamReader(pStream, StandardCharsets.UTF_8), JsonObject.class);

      for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
         String s = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
         pOutput.accept(entry.getKey(), s);
      }

   }

   /**
    * Return the StringTranslate singleton instance
    */
   public static Language getInstance() {
      return instance;
   }

   public static void inject(Language pInstance) {
      instance = pInstance;
   }

   // FORGE START
   public Map<String, String> getLanguageData() { return ImmutableMap.of(); }

   public String getOrDefault(String pId) {
      return this.getOrDefault(pId, pId);
   }

   public abstract String getOrDefault(String pKey, String pDefaultValue);

   public abstract boolean has(String pId);

   public abstract boolean isDefaultRightToLeft();

   public abstract FormattedCharSequence getVisualOrder(FormattedText p_128116_);

   public List<FormattedCharSequence> getVisualOrder(List<FormattedText> pText) {
      return pText.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
   }
}
