package net.minecraft;

public class ReportedException extends RuntimeException {
   private final CrashReport report;

   public ReportedException(CrashReport pReport) {
      this.report = pReport;
   }

   /**
    * Gets the CrashReport wrapped by this exception.
    */
   public CrashReport getReport() {
      return this.report;
   }

   public Throwable getCause() {
      return this.report.getException();
   }

   public String getMessage() {
      return this.report.getTitle();
   }
}