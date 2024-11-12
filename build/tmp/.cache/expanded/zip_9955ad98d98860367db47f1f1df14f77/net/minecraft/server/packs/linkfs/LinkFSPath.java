package net.minecraft.server.packs.linkfs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

class LinkFSPath implements Path {
   private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new DummyFileAttributes() {
      public boolean isRegularFile() {
         return false;
      }

      public boolean isDirectory() {
         return true;
      }
   };
   private static final BasicFileAttributes FILE_ATTRIBUTES = new DummyFileAttributes() {
      public boolean isRegularFile() {
         return true;
      }

      public boolean isDirectory() {
         return false;
      }
   };
   private static final Comparator<LinkFSPath> PATH_COMPARATOR = Comparator.comparing(LinkFSPath::pathToString);
   private final String name;
   private final LinkFileSystem fileSystem;
   @Nullable
   private final LinkFSPath parent;
   @Nullable
   private List<String> pathToRoot;
   @Nullable
   private String pathString;
   private final PathContents pathContents;

   public LinkFSPath(LinkFileSystem pFileSystem, String pName, @Nullable LinkFSPath pParent, PathContents pPathContents) {
      this.fileSystem = pFileSystem;
      this.name = pName;
      this.parent = pParent;
      this.pathContents = pPathContents;
   }

   private LinkFSPath createRelativePath(@Nullable LinkFSPath pParent, String pName) {
      return new LinkFSPath(this.fileSystem, pName, pParent, PathContents.RELATIVE);
   }

   public LinkFileSystem getFileSystem() {
      return this.fileSystem;
   }

   public boolean isAbsolute() {
      return this.pathContents != PathContents.RELATIVE;
   }

   public File toFile() {
      PathContents pathcontents = this.pathContents;
      if (pathcontents instanceof PathContents.FileContents pathcontents$filecontents) {
         return pathcontents$filecontents.contents().toFile();
      } else {
         throw new UnsupportedOperationException("Path " + this.pathToString() + " does not represent file");
      }
   }

   @Nullable
   public LinkFSPath getRoot() {
      return this.isAbsolute() ? this.fileSystem.rootPath() : null;
   }

   public LinkFSPath getFileName() {
      return this.createRelativePath((LinkFSPath)null, this.name);
   }

   @Nullable
   public LinkFSPath getParent() {
      return this.parent;
   }

   public int getNameCount() {
      return this.pathToRoot().size();
   }

   private List<String> pathToRoot() {
      if (this.name.isEmpty()) {
         return List.of();
      } else {
         if (this.pathToRoot == null) {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            if (this.parent != null) {
               builder.addAll(this.parent.pathToRoot());
            }

            builder.add(this.name);
            this.pathToRoot = builder.build();
         }

         return this.pathToRoot;
      }
   }

   public LinkFSPath getName(int pIndex) {
      List<String> list = this.pathToRoot();
      if (pIndex >= 0 && pIndex < list.size()) {
         return this.createRelativePath((LinkFSPath)null, list.get(pIndex));
      } else {
         throw new IllegalArgumentException("Invalid index: " + pIndex);
      }
   }

   public LinkFSPath subpath(int pStart, int pEnd) {
      List<String> list = this.pathToRoot();
      if (pStart >= 0 && pEnd <= list.size() && pStart < pEnd) {
         LinkFSPath linkfspath = null;

         for(int i = pStart; i < pEnd; ++i) {
            linkfspath = this.createRelativePath(linkfspath, list.get(i));
         }

         return linkfspath;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public boolean startsWith(Path pPath) {
      if (pPath.isAbsolute() != this.isAbsolute()) {
         return false;
      } else if (pPath instanceof LinkFSPath) {
         LinkFSPath linkfspath = (LinkFSPath)pPath;
         if (linkfspath.fileSystem != this.fileSystem) {
            return false;
         } else {
            List<String> list = this.pathToRoot();
            List<String> list1 = linkfspath.pathToRoot();
            int i = list1.size();
            if (i > list.size()) {
               return false;
            } else {
               for(int j = 0; j < i; ++j) {
                  if (!list1.get(j).equals(list.get(j))) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public boolean endsWith(Path pPath) {
      if (pPath.isAbsolute() && !this.isAbsolute()) {
         return false;
      } else if (pPath instanceof LinkFSPath) {
         LinkFSPath linkfspath = (LinkFSPath)pPath;
         if (linkfspath.fileSystem != this.fileSystem) {
            return false;
         } else {
            List<String> list = this.pathToRoot();
            List<String> list1 = linkfspath.pathToRoot();
            int i = list1.size();
            int j = list.size() - i;
            if (j < 0) {
               return false;
            } else {
               for(int k = i - 1; k >= 0; --k) {
                  if (!list1.get(k).equals(list.get(j + k))) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public LinkFSPath normalize() {
      return this;
   }

   public LinkFSPath resolve(Path pPath) {
      LinkFSPath linkfspath = this.toLinkPath(pPath);
      return pPath.isAbsolute() ? linkfspath : this.resolve(linkfspath.pathToRoot());
   }

   private LinkFSPath resolve(List<String> pNames) {
      LinkFSPath linkfspath = this;

      for(String s : pNames) {
         linkfspath = linkfspath.resolveName(s);
      }

      return linkfspath;
   }

   LinkFSPath resolveName(String pName) {
      if (isRelativeOrMissing(this.pathContents)) {
         return new LinkFSPath(this.fileSystem, pName, this, this.pathContents);
      } else {
         PathContents $$2 = this.pathContents;
         if ($$2 instanceof PathContents.DirectoryContents) {
            PathContents.DirectoryContents pathcontents$directorycontents = (PathContents.DirectoryContents)$$2;
            LinkFSPath linkfspath = pathcontents$directorycontents.children().get(pName);
            return linkfspath != null ? linkfspath : new LinkFSPath(this.fileSystem, pName, this, PathContents.MISSING);
         } else if (this.pathContents instanceof PathContents.FileContents) {
            return new LinkFSPath(this.fileSystem, pName, this, PathContents.MISSING);
         } else {
            throw new AssertionError("All content types should be already handled");
         }
      }
   }

   private static boolean isRelativeOrMissing(PathContents pPathContents) {
      return pPathContents == PathContents.MISSING || pPathContents == PathContents.RELATIVE;
   }

   public LinkFSPath relativize(Path pPath) {
      LinkFSPath linkfspath = this.toLinkPath(pPath);
      if (this.isAbsolute() != linkfspath.isAbsolute()) {
         throw new IllegalArgumentException("absolute mismatch");
      } else {
         List<String> list = this.pathToRoot();
         List<String> list1 = linkfspath.pathToRoot();
         if (list.size() >= list1.size()) {
            throw new IllegalArgumentException();
         } else {
            for(int i = 0; i < list.size(); ++i) {
               if (!list.get(i).equals(list1.get(i))) {
                  throw new IllegalArgumentException();
               }
            }

            return linkfspath.subpath(list.size(), list1.size());
         }
      }
   }

   public URI toUri() {
      try {
         return new URI("x-mc-link", this.fileSystem.store().name(), this.pathToString(), (String)null);
      } catch (URISyntaxException urisyntaxexception) {
         throw new AssertionError("Failed to create URI", urisyntaxexception);
      }
   }

   public LinkFSPath toAbsolutePath() {
      return this.isAbsolute() ? this : this.fileSystem.rootPath().resolve(this);
   }

   public LinkFSPath toRealPath(LinkOption... pOptions) {
      return this.toAbsolutePath();
   }

   public WatchKey register(WatchService pWatcher, WatchEvent.Kind<?>[] pEvents, WatchEvent.Modifier... pModifiers) {
      throw new UnsupportedOperationException();
   }

   public int compareTo(Path pOther) {
      LinkFSPath linkfspath = this.toLinkPath(pOther);
      return PATH_COMPARATOR.compare(this, linkfspath);
   }

   public boolean equals(Object pOther) {
      if (pOther == this) {
         return true;
      } else if (pOther instanceof LinkFSPath) {
         LinkFSPath linkfspath = (LinkFSPath)pOther;
         if (this.fileSystem != linkfspath.fileSystem) {
            return false;
         } else {
            boolean flag = this.hasRealContents();
            if (flag != linkfspath.hasRealContents()) {
               return false;
            } else if (flag) {
               return this.pathContents == linkfspath.pathContents;
            } else {
               return Objects.equals(this.parent, linkfspath.parent) && Objects.equals(this.name, linkfspath.name);
            }
         }
      } else {
         return false;
      }
   }

   private boolean hasRealContents() {
      return !isRelativeOrMissing(this.pathContents);
   }

   public int hashCode() {
      return this.hasRealContents() ? this.pathContents.hashCode() : this.name.hashCode();
   }

   public String toString() {
      return this.pathToString();
   }

   private String pathToString() {
      if (this.pathString == null) {
         StringBuilder stringbuilder = new StringBuilder();
         if (this.isAbsolute()) {
            stringbuilder.append("/");
         }

         Joiner.on("/").appendTo(stringbuilder, this.pathToRoot());
         this.pathString = stringbuilder.toString();
      }

      return this.pathString;
   }

   private LinkFSPath toLinkPath(@Nullable Path pPath) {
      if (pPath == null) {
         throw new NullPointerException();
      } else {
         if (pPath instanceof LinkFSPath) {
            LinkFSPath linkfspath = (LinkFSPath)pPath;
            if (linkfspath.fileSystem == this.fileSystem) {
               return linkfspath;
            }
         }

         throw new ProviderMismatchException();
      }
   }

   public boolean exists() {
      return this.hasRealContents();
   }

   @Nullable
   public Path getTargetPath() {
      PathContents pathcontents = this.pathContents;
      Path path;
      if (pathcontents instanceof PathContents.FileContents pathcontents$filecontents) {
         path = pathcontents$filecontents.contents();
      } else {
         path = null;
      }

      return path;
   }

   @Nullable
   public PathContents.DirectoryContents getDirectoryContents() {
      PathContents pathcontents = this.pathContents;
      PathContents.DirectoryContents pathcontents$directorycontents1;
      if (pathcontents instanceof PathContents.DirectoryContents pathcontents$directorycontents) {
         pathcontents$directorycontents1 = pathcontents$directorycontents;
      } else {
         pathcontents$directorycontents1 = null;
      }

      return pathcontents$directorycontents1;
   }

   public BasicFileAttributeView getBasicAttributeView() {
      return new BasicFileAttributeView() {
         public String name() {
            return "basic";
         }

         public BasicFileAttributes readAttributes() throws IOException {
            return LinkFSPath.this.getBasicAttributes();
         }

         public void setTimes(FileTime p_249505_, FileTime p_250498_, FileTime p_251700_) {
            throw new ReadOnlyFileSystemException();
         }
      };
   }

   public BasicFileAttributes getBasicAttributes() throws IOException {
      if (this.pathContents instanceof PathContents.DirectoryContents) {
         return DIRECTORY_ATTRIBUTES;
      } else if (this.pathContents instanceof PathContents.FileContents) {
         return FILE_ATTRIBUTES;
      } else {
         throw new NoSuchFileException(this.pathToString());
      }
   }
}