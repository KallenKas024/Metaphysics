package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CrashReportCategory {
   private final String title;
   private final List<CrashReportCategory.Entry> entries = Lists.newArrayList();
   private StackTraceElement[] stackTrace = new StackTraceElement[0];

   public CrashReportCategory(String pTitle) {
      this.title = pTitle;
   }

   public static String formatLocation(LevelHeightAccessor pLevelHeightAccess, double pX, double pY, double pZ) {
      return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", pX, pY, pZ, formatLocation(pLevelHeightAccess, BlockPos.containing(pX, pY, pZ)));
   }

   public static String formatLocation(LevelHeightAccessor pLevelHeightAccess, BlockPos pPos) {
      return formatLocation(pLevelHeightAccess, pPos.getX(), pPos.getY(), pPos.getZ());
   }

   public static String formatLocation(LevelHeightAccessor pLevelHeightAccess, int pX, int pY, int pZ) {
      StringBuilder stringbuilder = new StringBuilder();

      try {
         stringbuilder.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", pX, pY, pZ));
      } catch (Throwable throwable2) {
         stringbuilder.append("(Error finding world loc)");
      }

      stringbuilder.append(", ");

      try {
         int i = SectionPos.blockToSectionCoord(pX);
         int j = SectionPos.blockToSectionCoord(pY);
         int k = SectionPos.blockToSectionCoord(pZ);
         int l = pX & 15;
         int i1 = pY & 15;
         int j1 = pZ & 15;
         int k1 = SectionPos.sectionToBlockCoord(i);
         int l1 = pLevelHeightAccess.getMinBuildHeight();
         int i2 = SectionPos.sectionToBlockCoord(k);
         int j2 = SectionPos.sectionToBlockCoord(i + 1) - 1;
         int k2 = pLevelHeightAccess.getMaxBuildHeight() - 1;
         int l2 = SectionPos.sectionToBlockCoord(k + 1) - 1;
         stringbuilder.append(String.format(Locale.ROOT, "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", l, i1, j1, i, j, k, k1, l1, i2, j2, k2, l2));
      } catch (Throwable throwable1) {
         stringbuilder.append("(Error finding chunk loc)");
      }

      stringbuilder.append(", ");

      try {
         int i3 = pX >> 9;
         int j3 = pZ >> 9;
         int k3 = i3 << 5;
         int l3 = j3 << 5;
         int i4 = (i3 + 1 << 5) - 1;
         int j4 = (j3 + 1 << 5) - 1;
         int k4 = i3 << 9;
         int l4 = pLevelHeightAccess.getMinBuildHeight();
         int i5 = j3 << 9;
         int j5 = (i3 + 1 << 9) - 1;
         int k5 = pLevelHeightAccess.getMaxBuildHeight() - 1;
         int l5 = (j3 + 1 << 9) - 1;
         stringbuilder.append(String.format(Locale.ROOT, "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5));
      } catch (Throwable throwable) {
         stringbuilder.append("(Error finding world loc)");
      }

      return stringbuilder.toString();
   }

   /**
    * Adds a section to this crash report category, resolved by calling the given callable.
    * 
    * If the given callable throws an exception, a detail containing that exception will be created instead.
    */
   public CrashReportCategory setDetail(String pName, CrashReportDetail<String> pDetail) {
      try {
         this.setDetail(pName, pDetail.call());
      } catch (Throwable throwable) {
         this.setDetailError(pName, throwable);
      }

      return this;
   }

   /**
    * Adds a Crashreport section with the given name with the given value (converted {@code .toString()})
    */
   public CrashReportCategory setDetail(String pSectionName, Object pValue) {
      this.entries.add(new CrashReportCategory.Entry(pSectionName, pValue));
      return this;
   }

   /**
    * Adds a Crashreport section with the given name with the given Throwable
    */
   public void setDetailError(String pSectionName, Throwable pThrowable) {
      this.setDetail(pSectionName, pThrowable);
   }

   /**
    * Resets our stack trace according to the current trace, pruning the deepest 3 entries.  The parameter indicates how
    * many additional deepest entries to prune.  Returns the number of entries in the resulting pruned stack trace.
    */
   public int fillInStackTrace(int pSize) {
      StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
      if (astacktraceelement.length <= 0) {
         return 0;
      } else {
         int len = astacktraceelement.length - 3 - pSize;
         if (len <= 0) len = astacktraceelement.length;
         this.stackTrace = new StackTraceElement[len];
         System.arraycopy(astacktraceelement, astacktraceelement.length - len, this.stackTrace, 0, this.stackTrace.length);
         return this.stackTrace.length;
      }
   }

   /**
    * Do the deepest two elements of our saved stack trace match the given elements, in order from the deepest?
    */
   public boolean validateStackTrace(StackTraceElement pS1, StackTraceElement pS2) {
      if (this.stackTrace.length != 0 && pS1 != null) {
         StackTraceElement stacktraceelement = this.stackTrace[0];
         if (stacktraceelement.isNativeMethod() == pS1.isNativeMethod() && stacktraceelement.getClassName().equals(pS1.getClassName()) && stacktraceelement.getFileName().equals(pS1.getFileName()) && stacktraceelement.getMethodName().equals(pS1.getMethodName())) {
            if (pS2 != null != this.stackTrace.length > 1) {
               return false;
            } else if (pS2 != null && !this.stackTrace[1].equals(pS2)) {
               return false;
            } else {
               this.stackTrace[0] = pS1;
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   /**
    * Removes the given number entries from the bottom of the stack trace.
    */
   public void trimStacktrace(int pAmount) {
      StackTraceElement[] astacktraceelement = new StackTraceElement[this.stackTrace.length - pAmount];
      System.arraycopy(this.stackTrace, 0, astacktraceelement, 0, astacktraceelement.length);
      this.stackTrace = astacktraceelement;
   }

   public void getDetails(StringBuilder pBuilder) {
      pBuilder.append("-- ").append(this.title).append(" --\n");
      pBuilder.append("Details:");

      for(CrashReportCategory.Entry crashreportcategory$entry : this.entries) {
         pBuilder.append("\n\t");
         pBuilder.append(crashreportcategory$entry.getKey());
         pBuilder.append(": ");
         pBuilder.append(crashreportcategory$entry.getValue());
      }

      if (this.stackTrace != null && this.stackTrace.length > 0) {
         pBuilder.append("\nStacktrace:");
         pBuilder.append(net.minecraftforge.logging.CrashReportExtender.generateEnhancedStackTrace(this.stackTrace));
      }

   }

   public StackTraceElement[] getStacktrace() {
      return this.stackTrace;
   }

   public void applyStackTrace(Throwable t) {
      this.stackTrace = t.getStackTrace();
   }

   public static void populateBlockDetails(CrashReportCategory pCategory, LevelHeightAccessor pLevelHeightAccessor, BlockPos pPos, @Nullable BlockState pState) {
      if (pState != null) {
         pCategory.setDetail("Block", pState::toString);
      }

      pCategory.setDetail("Block location", () -> {
         return formatLocation(pLevelHeightAccessor, pPos);
      });
   }

   static class Entry {
      private final String key;
      private final String value;

      public Entry(String pKey, @Nullable Object pValue) {
         this.key = pKey;
         if (pValue == null) {
            this.value = "~~NULL~~";
         } else if (pValue instanceof Throwable) {
            Throwable throwable = (Throwable)pValue;
            this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
         } else {
            this.value = pValue.toString();
         }

      }

      public String getKey() {
         return this.key;
      }

      public String getValue() {
         return this.value;
      }
   }
}
