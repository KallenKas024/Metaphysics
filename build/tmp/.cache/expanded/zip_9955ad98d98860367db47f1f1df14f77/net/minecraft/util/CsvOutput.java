package net.minecraft.util;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;

public class CsvOutput {
   private static final String LINE_SEPARATOR = "\r\n";
   private static final String FIELD_SEPARATOR = ",";
   private final Writer output;
   private final int columnCount;

   CsvOutput(Writer pOutput, List<String> pFirstRow) throws IOException {
      this.output = pOutput;
      this.columnCount = pFirstRow.size();
      this.writeLine(pFirstRow.stream());
   }

   public static CsvOutput.Builder builder() {
      return new CsvOutput.Builder();
   }

   public void writeRow(Object... pData) throws IOException {
      if (pData.length != this.columnCount) {
         throw new IllegalArgumentException("Invalid number of columns, expected " + this.columnCount + ", but got " + pData.length);
      } else {
         this.writeLine(Stream.of(pData));
      }
   }

   private void writeLine(Stream<?> pData) throws IOException {
      this.output.write((String)pData.map(CsvOutput::getStringValue).collect(Collectors.joining(",")) + "\r\n");
   }

   private static String getStringValue(@Nullable Object p_13621_) {
      return StringEscapeUtils.escapeCsv(p_13621_ != null ? p_13621_.toString() : "[null]");
   }

   public static class Builder {
      private final List<String> headers = Lists.newArrayList();

      public CsvOutput.Builder addColumn(String pHeader) {
         this.headers.add(pHeader);
         return this;
      }

      public CsvOutput build(Writer pOutput) throws IOException {
         return new CsvOutput(pOutput, this.headers);
      }
   }
}