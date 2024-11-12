package net.minecraft.world.level.storage;

public class DataVersion {
   private final int version;
   private final String series;
   public static String MAIN_SERIES = "main";

   public DataVersion(int pVersion) {
      this(pVersion, MAIN_SERIES);
   }

   public DataVersion(int pVersion, String pSeries) {
      this.version = pVersion;
      this.series = pSeries;
   }

   public boolean isSideSeries() {
      return !this.series.equals(MAIN_SERIES);
   }

   public String getSeries() {
      return this.series;
   }

   public int getVersion() {
      return this.version;
   }

   public boolean isCompatible(DataVersion pDataVersion) {
      return this.getSeries().equals(pDataVersion.getSeries());
   }
}