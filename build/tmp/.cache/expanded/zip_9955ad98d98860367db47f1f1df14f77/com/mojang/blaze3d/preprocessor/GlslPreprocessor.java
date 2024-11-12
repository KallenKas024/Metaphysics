package com.mojang.blaze3d.preprocessor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GlslPreprocessor {
   private static final String C_COMMENT = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
   private static final String LINE_COMMENT = "//[^\\v]*";
   private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
   private static final Pattern REGEX_VERSION = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
   private static final Pattern REGEX_ENDS_WITH_WHITESPACE = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

   public List<String> process(String pShaderData) {
      GlslPreprocessor.Context glslpreprocessor$context = new GlslPreprocessor.Context();
      List<String> list = this.processImports(pShaderData, glslpreprocessor$context, "");
      list.set(0, this.setVersion(list.get(0), glslpreprocessor$context.glslVersion));
      return list;
   }

   private List<String> processImports(String pShaderData, GlslPreprocessor.Context pContext, String pIncludeDirectory) {
      int i = pContext.sourceId;
      int j = 0;
      String s = "";
      List<String> list = Lists.newArrayList();
      Matcher matcher = REGEX_MOJ_IMPORT.matcher(pShaderData);

      while(matcher.find()) {
         if (!isDirectiveDisabled(pShaderData, matcher, j)) {
            String s1 = matcher.group(2);
            boolean flag = s1 != null;
            if (!flag) {
               s1 = matcher.group(3);
            }

            if (s1 != null) {
               String s2 = pShaderData.substring(j, matcher.start(1));
               String s3 = pIncludeDirectory + s1;
               String s4 = this.applyImport(flag, s3);
               if (!Strings.isNullOrEmpty(s4)) {
                  if (!StringUtil.endsWithNewLine(s4)) {
                     s4 = s4 + System.lineSeparator();
                  }

                  ++pContext.sourceId;
                  int k = pContext.sourceId;
                  List<String> list1 = this.processImports(s4, pContext, flag ? FileUtil.getFullResourcePath(s3) : "");
                  list1.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, k, this.processVersions(list1.get(0), pContext)));
                  if (!Util.isBlank(s2)) {
                     list.add(s2);
                  }

                  list.addAll(list1);
               } else {
                  String s6 = flag ? String.format(Locale.ROOT, "/*#moj_import \"%s\"*/", s1) : String.format(Locale.ROOT, "/*#moj_import <%s>*/", s1);
                  list.add(s + s2 + s6);
               }

               int l = StringUtil.lineCount(pShaderData.substring(0, matcher.end(1)));
               s = String.format(Locale.ROOT, "#line %d %d", l, i);
               j = matcher.end(1);
            }
         }
      }

      String s5 = pShaderData.substring(j);
      if (!Util.isBlank(s5)) {
         list.add(s + s5);
      }

      return list;
   }

   private String processVersions(String pVersionData, GlslPreprocessor.Context pContext) {
      Matcher matcher = REGEX_VERSION.matcher(pVersionData);
      if (matcher.find() && isDirectiveEnabled(pVersionData, matcher)) {
         pContext.glslVersion = Math.max(pContext.glslVersion, Integer.parseInt(matcher.group(2)));
         return pVersionData.substring(0, matcher.start(1)) + "/*" + pVersionData.substring(matcher.start(1), matcher.end(1)) + "*/" + pVersionData.substring(matcher.end(1));
      } else {
         return pVersionData;
      }
   }

   private String setVersion(String pVersionData, int pGlslVersion) {
      Matcher matcher = REGEX_VERSION.matcher(pVersionData);
      return matcher.find() && isDirectiveEnabled(pVersionData, matcher) ? pVersionData.substring(0, matcher.start(2)) + Math.max(pGlslVersion, Integer.parseInt(matcher.group(2))) + pVersionData.substring(matcher.end(2)) : pVersionData;
   }

   private static boolean isDirectiveEnabled(String pShaderData, Matcher pMatcher) {
      return !isDirectiveDisabled(pShaderData, pMatcher, 0);
   }

   private static boolean isDirectiveDisabled(String pShaderData, Matcher pMatcher, int pOffset) {
      int i = pMatcher.start() - pOffset;
      if (i == 0) {
         return false;
      } else {
         Matcher matcher = REGEX_ENDS_WITH_WHITESPACE.matcher(pShaderData.substring(pOffset, pMatcher.start()));
         if (!matcher.find()) {
            return true;
         } else {
            int j = matcher.end(1);
            return j == pMatcher.start();
         }
      }
   }

   @Nullable
   public abstract String applyImport(boolean pUseFullPath, String pDirectory);

   @OnlyIn(Dist.CLIENT)
   static final class Context {
      int glslVersion;
      int sourceId;
   }
}