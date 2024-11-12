package net.minecraft.util;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.FileUtil;

public class DirectoryLock implements AutoCloseable {
   public static final String LOCK_FILE = "session.lock";
   private final FileChannel lockFile;
   private final FileLock lock;
   private static final ByteBuffer DUMMY;

   public static DirectoryLock create(Path pPath) throws IOException {
      Path path = pPath.resolve("session.lock");
      FileUtil.createDirectoriesSafe(pPath);
      FileChannel filechannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      try {
         filechannel.write(DUMMY.duplicate());
         filechannel.force(true);
         FileLock filelock = filechannel.tryLock();
         if (filelock == null) {
            throw DirectoryLock.LockException.alreadyLocked(path);
         } else {
            return new DirectoryLock(filechannel, filelock);
         }
      } catch (IOException ioexception1) {
         try {
            filechannel.close();
         } catch (IOException ioexception) {
            ioexception1.addSuppressed(ioexception);
         }

         throw ioexception1;
      }
   }

   private DirectoryLock(FileChannel pLockFile, FileLock pLock) {
      this.lockFile = pLockFile;
      this.lock = pLock;
   }

   public void close() throws IOException {
      try {
         if (this.lock.isValid()) {
            this.lock.release();
         }
      } finally {
         if (this.lockFile.isOpen()) {
            this.lockFile.close();
         }

      }

   }

   public boolean isValid() {
      return this.lock.isValid();
   }

   public static boolean isLocked(Path pPath) throws IOException {
      Path path = pPath.resolve("session.lock");

      try {
         boolean flag;
         try (
            FileChannel filechannel = FileChannel.open(path, StandardOpenOption.WRITE);
            FileLock filelock = filechannel.tryLock();
         ) {
            flag = filelock == null;
         }

         return flag;
      } catch (AccessDeniedException accessdeniedexception) {
         return true;
      } catch (NoSuchFileException nosuchfileexception) {
         return false;
      }
   }

   static {
      byte[] abyte = "\u2603".getBytes(Charsets.UTF_8);
      DUMMY = ByteBuffer.allocateDirect(abyte.length);
      DUMMY.put(abyte);
      DUMMY.flip();
   }

   public static class LockException extends IOException {
      private LockException(Path pPath, String pMessage) {
         super(pPath.toAbsolutePath() + ": " + pMessage);
      }

      public static DirectoryLock.LockException alreadyLocked(Path pPath) {
         return new DirectoryLock.LockException(pPath, "already locked (possibly by other Minecraft instance?)");
      }
   }
}