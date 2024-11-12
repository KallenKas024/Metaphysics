package net.minecraft.util;

public class FrameTimer {
   public static final int LOGGING_LENGTH = 240;
   /** An array with the last 240 frames */
   private final long[] loggedTimes = new long[240];
   /** The last index used when 240 frames have been set */
   private int logStart;
   /** A counter */
   private int logLength;
   /** The next index to use in the array */
   private int logEnd;

   /**
    * Add a frame at the next index in the array frames
    */
   public void logFrameDuration(long pRunningTime) {
      this.loggedTimes[this.logEnd] = pRunningTime;
      ++this.logEnd;
      if (this.logEnd == 240) {
         this.logEnd = 0;
      }

      if (this.logLength < 240) {
         this.logStart = 0;
         ++this.logLength;
      } else {
         this.logStart = this.wrapIndex(this.logEnd + 1);
      }

   }

   public long getAverageDuration(int p_144733_) {
      int i = (this.logStart + p_144733_) % 240;
      int j = this.logStart;

      long k;
      for(k = 0L; j != i; ++j) {
         k += this.loggedTimes[j];
      }

      return k / (long)p_144733_;
   }

   public int scaleAverageDurationTo(int p_144735_, int pScale) {
      return this.scaleSampleTo(this.getAverageDuration(p_144735_), pScale, 60);
   }

   public int scaleSampleTo(long pValue, int pScale, int pDivisor) {
      double d0 = (double)pValue / (double)(1000000000L / (long)pDivisor);
      return (int)(d0 * (double)pScale);
   }

   /**
    * Return the last index used when 240 frames have been set
    */
   public int getLogStart() {
      return this.logStart;
   }

   /**
    * Return the index of the next frame in the array
    */
   public int getLogEnd() {
      return this.logEnd;
   }

   /**
    * Change 240 to 0
    */
   public int wrapIndex(int pRawIndex) {
      return pRawIndex % 240;
   }

   /**
    * Return the array of frames
    */
   public long[] getLog() {
      return this.loggedTimes;
   }
}