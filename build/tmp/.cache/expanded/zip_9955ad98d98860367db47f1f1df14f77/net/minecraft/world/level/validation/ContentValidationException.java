package net.minecraft.world.level.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ContentValidationException extends Exception {
   private final Path directory;
   private final List<ForbiddenSymlinkInfo> entries;

   public ContentValidationException(Path pDirectory, List<ForbiddenSymlinkInfo> pEntries) {
      this.directory = pDirectory;
      this.entries = pEntries;
   }

   public String getMessage() {
      return getMessage(this.directory, this.entries);
   }

   public static String getMessage(Path pDirectory, List<ForbiddenSymlinkInfo> pEntries) {
      return "Failed to validate '" + pDirectory + "'. Found forbidden symlinks: " + (String)pEntries.stream().map((p_289919_) -> {
         return p_289919_.link() + "->" + p_289919_.target();
      }).collect(Collectors.joining(", "));
   }
}