package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

class LinkFSProvider extends FileSystemProvider {
   public static final String SCHEME = "x-mc-link";

   public String getScheme() {
      return "x-mc-link";
   }

   public FileSystem newFileSystem(URI pUri, Map<String, ?> pEnviroment) {
      throw new UnsupportedOperationException();
   }

   public FileSystem getFileSystem(URI pUri) {
      throw new UnsupportedOperationException();
   }

   public Path getPath(URI pUri) {
      throw new UnsupportedOperationException();
   }

   public SeekableByteChannel newByteChannel(Path pPath, Set<? extends OpenOption> pOptions, FileAttribute<?>... pAttributes) throws IOException {
      if (!pOptions.contains(StandardOpenOption.CREATE_NEW) && !pOptions.contains(StandardOpenOption.CREATE) && !pOptions.contains(StandardOpenOption.APPEND) && !pOptions.contains(StandardOpenOption.WRITE)) {
         Path path = toLinkPath(pPath).toAbsolutePath().getTargetPath();
         if (path == null) {
            throw new NoSuchFileException(pPath.toString());
         } else {
            return Files.newByteChannel(path, pOptions, pAttributes);
         }
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public DirectoryStream<Path> newDirectoryStream(Path pDirectory, final DirectoryStream.Filter<? super Path> pFilter) throws IOException {
      final PathContents.DirectoryContents pathcontents$directorycontents = toLinkPath(pDirectory).toAbsolutePath().getDirectoryContents();
      if (pathcontents$directorycontents == null) {
         throw new NotDirectoryException(pDirectory.toString());
      } else {
         return new DirectoryStream<Path>() {
            public Iterator<Path> iterator() {
               return pathcontents$directorycontents.children().values().stream().filter((p_250987_) -> {
                  try {
                     return pFilter.accept(p_250987_);
                  } catch (IOException ioexception) {
                     throw new DirectoryIteratorException(ioexception);
                  }
               }).map((p_249891_) -> {
                  return (Path) p_249891_;
               }).iterator();
            }

            public void close() {
            }
         };
      }
   }

   public void createDirectory(Path pPath, FileAttribute<?>... pAttributes) {
      throw new ReadOnlyFileSystemException();
   }

   public void delete(Path pPath) {
      throw new ReadOnlyFileSystemException();
   }

   public void copy(Path pSource, Path pTarget, CopyOption... pOptions) {
      throw new ReadOnlyFileSystemException();
   }

   public void move(Path pSource, Path pTarget, CopyOption... pOptions) {
      throw new ReadOnlyFileSystemException();
   }

   public boolean isSameFile(Path pPath, Path pPath2) {
      return pPath instanceof LinkFSPath && pPath2 instanceof LinkFSPath && pPath.equals(pPath2);
   }

   public boolean isHidden(Path pPath) {
      return false;
   }

   public FileStore getFileStore(Path pPath) {
      return toLinkPath(pPath).getFileSystem().store();
   }

   public void checkAccess(Path pPath, AccessMode... pModes) throws IOException {
      if (pModes.length == 0 && !toLinkPath(pPath).exists()) {
         throw new NoSuchFileException(pPath.toString());
      } else {
         AccessMode[] aaccessmode = pModes;
         int i = pModes.length;
         int j = 0;

         while(j < i) {
            AccessMode accessmode = aaccessmode[j];
            switch (accessmode) {
               case READ:
                  if (!toLinkPath(pPath).exists()) {
                     throw new NoSuchFileException(pPath.toString());
                  }
               default:
                  ++j;
                  break;
               case EXECUTE:
               case WRITE:
                  throw new AccessDeniedException(accessmode.toString());
            }
         }

      }
   }

   @Nullable
   public <V extends FileAttributeView> V getFileAttributeView(Path pPath, Class<V> pType, LinkOption... pOptions) {
      LinkFSPath linkfspath = toLinkPath(pPath);
      return (V)(pType == BasicFileAttributeView.class ? linkfspath.getBasicAttributeView() : null);
   }

   public <A extends BasicFileAttributes> A readAttributes(Path pPath, Class<A> pType, LinkOption... pOptions) throws IOException {
      LinkFSPath linkfspath = toLinkPath(pPath).toAbsolutePath();
      if (pType == BasicFileAttributes.class) {
         return (A)linkfspath.getBasicAttributes();
      } else {
         throw new UnsupportedOperationException("Attributes of type " + pType.getName() + " not supported");
      }
   }

   public Map<String, Object> readAttributes(Path pPath, String pAttributes, LinkOption... pOptions) {
      throw new UnsupportedOperationException();
   }

   public void setAttribute(Path pPath, String pAttribute, Object pValue, LinkOption... pOptions) {
      throw new ReadOnlyFileSystemException();
   }

   private static LinkFSPath toLinkPath(@Nullable Path pPath) {
      if (pPath == null) {
         throw new NullPointerException();
      } else if (pPath instanceof LinkFSPath) {
         return (LinkFSPath)pPath;
      } else {
         throw new ProviderMismatchException();
      }
   }
}