package net.minecraft.server.packs.linkfs;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class LinkFileSystem extends FileSystem {
   private static final Set<String> VIEWS = Set.of("basic");
   public static final String PATH_SEPARATOR = "/";
   private static final Splitter PATH_SPLITTER = Splitter.on('/');
   private final FileStore store;
   private final FileSystemProvider provider = new LinkFSProvider();
   private final LinkFSPath root;

   LinkFileSystem(String pName, LinkFileSystem.DirectoryEntry pRoot) {
      this.store = new LinkFSFileStore(pName);
      this.root = buildPath(pRoot, this, "", (LinkFSPath)null);
   }

   private static LinkFSPath buildPath(LinkFileSystem.DirectoryEntry pDirectory, LinkFileSystem pFileSystem, String pName, @Nullable LinkFSPath pParent) {
      Object2ObjectOpenHashMap<String, LinkFSPath> object2objectopenhashmap = new Object2ObjectOpenHashMap<>();
      LinkFSPath linkfspath = new LinkFSPath(pFileSystem, pName, pParent, new PathContents.DirectoryContents(object2objectopenhashmap));
      pDirectory.files.forEach((p_249491_, p_250850_) -> {
         object2objectopenhashmap.put(p_249491_, new LinkFSPath(pFileSystem, p_249491_, linkfspath, new PathContents.FileContents(p_250850_)));
      });
      pDirectory.children.forEach((p_251592_, p_251728_) -> {
         object2objectopenhashmap.put(p_251592_, buildPath(p_251728_, pFileSystem, p_251592_, linkfspath));
      });
      object2objectopenhashmap.trim();
      return linkfspath;
   }

   public FileSystemProvider provider() {
      return this.provider;
   }

   public void close() {
   }

   public boolean isOpen() {
      return true;
   }

   public boolean isReadOnly() {
      return true;
   }

   public String getSeparator() {
      return "/";
   }

   public Iterable<Path> getRootDirectories() {
      return List.of(this.root);
   }

   public Iterable<FileStore> getFileStores() {
      return List.of(this.store);
   }

   public Set<String> supportedFileAttributeViews() {
      return VIEWS;
   }

   public Path getPath(String pFirst, String... pMore) {
      Stream<String> stream = Stream.of(pFirst);
      if (pMore.length > 0) {
         stream = Stream.concat(stream, Stream.of(pMore));
      }

      String s = stream.collect(Collectors.joining("/"));
      if (s.equals("/")) {
         return this.root;
      } else if (s.startsWith("/")) {
         LinkFSPath linkfspath1 = this.root;

         for(String s2 : PATH_SPLITTER.split(s.substring(1))) {
            if (s2.isEmpty()) {
               throw new IllegalArgumentException("Empty paths not allowed");
            }

            linkfspath1 = linkfspath1.resolveName(s2);
         }

         return linkfspath1;
      } else {
         LinkFSPath linkfspath = null;

         for(String s1 : PATH_SPLITTER.split(s)) {
            if (s1.isEmpty()) {
               throw new IllegalArgumentException("Empty paths not allowed");
            }

            linkfspath = new LinkFSPath(this, s1, linkfspath, PathContents.RELATIVE);
         }

         if (linkfspath == null) {
            throw new IllegalArgumentException("Empty paths not allowed");
         } else {
            return linkfspath;
         }
      }
   }

   public PathMatcher getPathMatcher(String pSyntaxAndPattern) {
      throw new UnsupportedOperationException();
   }

   public UserPrincipalLookupService getUserPrincipalLookupService() {
      throw new UnsupportedOperationException();
   }

   public WatchService newWatchService() {
      throw new UnsupportedOperationException();
   }

   public FileStore store() {
      return this.store;
   }

   public LinkFSPath rootPath() {
      return this.root;
   }

   public static LinkFileSystem.Builder builder() {
      return new LinkFileSystem.Builder();
   }

   public static class Builder {
      private final LinkFileSystem.DirectoryEntry root = new LinkFileSystem.DirectoryEntry();

      public LinkFileSystem.Builder put(List<String> pPathString, String pFileName, Path pFilePath) {
         LinkFileSystem.DirectoryEntry linkfilesystem$directoryentry = this.root;

         for(String s : pPathString) {
            linkfilesystem$directoryentry = linkfilesystem$directoryentry.children.computeIfAbsent(s, (p_249671_) -> {
               return new LinkFileSystem.DirectoryEntry();
            });
         }

         linkfilesystem$directoryentry.files.put(pFileName, pFilePath);
         return this;
      }

      public LinkFileSystem.Builder put(List<String> pPathString, Path pFilePath) {
         if (pPathString.isEmpty()) {
            throw new IllegalArgumentException("Path can't be empty");
         } else {
            int i = pPathString.size() - 1;
            return this.put(pPathString.subList(0, i), pPathString.get(i), pFilePath);
         }
      }

      public FileSystem build(String pName) {
         return new LinkFileSystem(pName, this.root);
      }
   }

   static record DirectoryEntry(Map<String, LinkFileSystem.DirectoryEntry> children, Map<String, Path> files) {
      public DirectoryEntry() {
         this(new HashMap<>(), new HashMap<>());
      }
   }
}