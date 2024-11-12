package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

public class Util {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int DEFAULT_MAX_THREADS = 255;
   private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
   private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
   private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
   private static final ExecutorService IO_POOL = makeIoExecutor();
   private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
   public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
   public static final Ticker TICKER = new Ticker() {
      public long read() {
         return Util.timeSource.getAsLong();
      }
   };
   public static final UUID NIL_UUID = new UUID(0L, 0L);
   public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter((p_201865_) -> {
      return p_201865_.getScheme().equalsIgnoreCase("jar");
   }).findFirst().orElseThrow(() -> {
      return new IllegalStateException("No jar file system provider found");
   });
   private static Consumer<String> thePauser = (p_201905_) -> {
   };

   public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
      return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
   }

   public static <T extends Comparable<T>> String getPropertyName(Property<T> pProperty, Object pValue) {
      return pProperty.getName((T)(pValue));
   }

   public static String makeDescriptionId(String pType, @Nullable ResourceLocation pId) {
      return pId == null ? pType + ".unregistered_sadface" : pType + "." + pId.getNamespace() + "." + pId.getPath().replace('/', '.');
   }

   public static long getMillis() {
      return getNanos() / 1000000L;
   }

   public static long getNanos() {
      return timeSource.getAsLong();
   }

   public static long getEpochMillis() {
      return Instant.now().toEpochMilli();
   }

   public static String getFilenameFormattedDateTime() {
      return FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
   }

   private static ExecutorService makeExecutor(String pServiceName) {
      int i = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
      ExecutorService executorservice;
      if (i <= 0) {
         executorservice = MoreExecutors.newDirectExecutorService();
      } else {
         executorservice = new ForkJoinPool(i, (p_201863_) -> {
            ForkJoinWorkerThread forkjoinworkerthread = new ForkJoinWorkerThread(p_201863_) {
               protected void onTermination(Throwable p_211561_) {
                  if (p_211561_ != null) {
                     Util.LOGGER.warn("{} died", this.getName(), p_211561_);
                  } else {
                     Util.LOGGER.debug("{} shutdown", (Object)this.getName());
                  }

                  super.onTermination(p_211561_);
               }
            };
            forkjoinworkerthread.setName("Worker-" + pServiceName + "-" + WORKER_COUNT.getAndIncrement());
            return forkjoinworkerthread;
         }, Util::onThreadException, true);
      }

      return executorservice;
   }

   private static int getMaxThreads() {
      String s = System.getProperty("max.bg.threads");
      if (s != null) {
         try {
            int i = Integer.parseInt(s);
            if (i >= 1 && i <= 255) {
               return i;
            }

            LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", s, 255);
         } catch (NumberFormatException numberformatexception) {
            LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", s, 255);
         }
      }

      return 255;
   }

   // We add these inner classes to compensate for Mojang's missing inner classes and shift the anonymous class index.
   // This allows us to obfuscate subsequent anonymous inner classes correctly.
   @SuppressWarnings("unused") private static java.util.function.LongSupplier INNER_CLASS_SHIFT1 = new java.util.function.LongSupplier() { public long getAsLong() { return 0; } };
   @SuppressWarnings("unused") private static java.util.function.LongSupplier INNER_CLASS_SHIFT2 = new java.util.function.LongSupplier() { public long getAsLong() { return 0; } };

   public static ExecutorService backgroundExecutor() {
      return BACKGROUND_EXECUTOR;
   }

   public static ExecutorService ioPool() {
      return IO_POOL;
   }

   public static void shutdownExecutors() {
      shutdownExecutor(BACKGROUND_EXECUTOR);
      shutdownExecutor(IO_POOL);
   }

   private static void shutdownExecutor(ExecutorService pService) {
      pService.shutdown();

      boolean flag;
      try {
         flag = pService.awaitTermination(3L, TimeUnit.SECONDS);
      } catch (InterruptedException interruptedexception) {
         flag = false;
      }

      if (!flag) {
         pService.shutdownNow();
      }

   }

   private static ExecutorService makeIoExecutor() {
      return Executors.newCachedThreadPool((p_201860_) -> {
         Thread thread = new Thread(p_201860_);
         thread.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
         thread.setUncaughtExceptionHandler(Util::onThreadException);
         return thread;
      });
   }

   public static void throwAsRuntime(Throwable pThrowable) {
      throw pThrowable instanceof RuntimeException ? (RuntimeException)pThrowable : new RuntimeException(pThrowable);
   }

   private static void onThreadException(Thread p_137496_, Throwable p_137497_) {
      pauseInIde(p_137497_);
      if (p_137497_ instanceof CompletionException) {
         p_137497_ = p_137497_.getCause();
      }

      if (p_137497_ instanceof ReportedException) {
         Bootstrap.realStdoutPrintln(((ReportedException)p_137497_).getReport().getFriendlyReport());
         System.exit(-1);
      }

      LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", p_137496_), p_137497_);
   }

   @Nullable
   public static Type<?> fetchChoiceType(DSL.TypeReference pType, String pChoiceName) {
      return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(pType, pChoiceName);
   }

   @Nullable
   private static Type<?> doFetchChoiceType(DSL.TypeReference pType, String pChoiceName) {
      Type<?> type = null;

      try {
         type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion())).getChoiceType(pType, pChoiceName);
      } catch (IllegalArgumentException illegalargumentexception) {
         LOGGER.debug("No data fixer registered for {}", (Object)pChoiceName);
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw illegalargumentexception;
         }
      }

      return type;
   }

   public static Runnable wrapThreadWithTaskName(String pName, Runnable pTask) {
      return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
         Thread thread = Thread.currentThread();
         String s = thread.getName();
         thread.setName(pName);

         try {
            pTask.run();
         } finally {
            thread.setName(s);
         }

      } : pTask;
   }

   public static <V> Supplier<V> wrapThreadWithTaskName(String pName, Supplier<V> pTask) {
      return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
         Thread thread = Thread.currentThread();
         String s = thread.getName();
         thread.setName(pName);

         Object object;
         try {
            object = pTask.get();
         } finally {
            thread.setName(s);
         }

         return (V)object;
      } : pTask;
   }

   public static Util.OS getPlatform() {
      String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (s.contains("win")) {
         return Util.OS.WINDOWS;
      } else if (s.contains("mac")) {
         return Util.OS.OSX;
      } else if (s.contains("solaris")) {
         return Util.OS.SOLARIS;
      } else if (s.contains("sunos")) {
         return Util.OS.SOLARIS;
      } else if (s.contains("linux")) {
         return Util.OS.LINUX;
      } else {
         return s.contains("unix") ? Util.OS.LINUX : Util.OS.UNKNOWN;
      }
   }

   public static Stream<String> getVmArguments() {
      RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
      return runtimemxbean.getInputArguments().stream().filter((p_201903_) -> {
         return p_201903_.startsWith("-X");
      });
   }

   public static <T> T lastOf(List<T> pList) {
      return pList.get(pList.size() - 1);
   }

   public static <T> T findNextInIterable(Iterable<T> pIterable, @Nullable T pElement) {
      Iterator<T> iterator = pIterable.iterator();
      T t = iterator.next();
      if (pElement != null) {
         T t1 = t;

         while(t1 != pElement) {
            if (iterator.hasNext()) {
               t1 = iterator.next();
            }
         }

         if (iterator.hasNext()) {
            return iterator.next();
         }
      }

      return t;
   }

   public static <T> T findPreviousInIterable(Iterable<T> pIterable, @Nullable T pCurrent) {
      Iterator<T> iterator = pIterable.iterator();

      T t;
      T t1;
      for(t = null; iterator.hasNext(); t = t1) {
         t1 = iterator.next();
         if (t1 == pCurrent) {
            if (t == null) {
               t = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : pCurrent);
            }
            break;
         }
      }

      return t;
   }

   public static <T> T make(Supplier<T> pSupplier) {
      return pSupplier.get();
   }

   public static <T> T make(T pObject, Consumer<T> pConsumer) {
      pConsumer.accept(pObject);
      return pObject;
   }

   public static <K> Hash.Strategy<K> identityStrategy() {
      return (Hash.Strategy<K>) Util.IdentityStrategy.INSTANCE;
   }

   /**
    * Takes a list of futures and returns a future of list that completes when all of them succeed or any of them error,
    */
   public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> pFutures) {
      if (pFutures.isEmpty()) {
         return CompletableFuture.completedFuture(List.of());
      } else if (pFutures.size() == 1) {
         return pFutures.get(0).thenApply(List::of);
      } else {
         CompletableFuture<Void> completablefuture = CompletableFuture.allOf(pFutures.toArray(new CompletableFuture[0]));
         return completablefuture.thenApply((p_203746_) -> {
            return pFutures.stream().map(CompletableFuture::join).toList();
         });
      }
   }

   public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> pCompletableFutures) {
      CompletableFuture<List<V>> completablefuture = new CompletableFuture<>();
      return fallibleSequence(pCompletableFutures, completablefuture::completeExceptionally).applyToEither(completablefuture, Function.identity());
   }

   public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> pCompletableFutures) {
      CompletableFuture<List<V>> completablefuture = new CompletableFuture<>();
      return fallibleSequence(pCompletableFutures, (p_274642_) -> {
         if (completablefuture.completeExceptionally(p_274642_)) {
            for(CompletableFuture<? extends V> completablefuture1 : pCompletableFutures) {
               completablefuture1.cancel(true);
            }
         }

      }).applyToEither(completablefuture, Function.identity());
   }

   private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> pCompletableFutures, Consumer<Throwable> pThrowableConsumer) {
      List<V> list = Lists.newArrayListWithCapacity(pCompletableFutures.size());
      CompletableFuture<?>[] completablefuture = new CompletableFuture[pCompletableFutures.size()];
      pCompletableFutures.forEach((p_214641_) -> {
         int i = list.size();
         list.add((V)null);
         completablefuture[i] = p_214641_.whenComplete((p_214650_, p_214651_) -> {
            if (p_214651_ != null) {
               pThrowableConsumer.accept(p_214651_);
            } else {
               list.set(i, p_214650_);
            }

         });
      });
      return CompletableFuture.allOf(completablefuture).thenApply((p_214626_) -> {
         return list;
      });
   }

   public static <T> Optional<T> ifElse(Optional<T> pOpt, Consumer<T> pConsumer, Runnable pOrElse) {
      if (pOpt.isPresent()) {
         pConsumer.accept(pOpt.get());
      } else {
         pOrElse.run();
      }

      return pOpt;
   }

   public static <T> Supplier<T> name(Supplier<T> pItem, Supplier<String> pNameSupplier) {
      return pItem;
   }

   public static Runnable name(Runnable pItem, Supplier<String> pNameSupplier) {
      return pItem;
   }

   public static void logAndPauseIfInIde(String pError) {
      LOGGER.error(pError);
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         doPause(pError);
      }

   }

   public static void logAndPauseIfInIde(String pMsg, Throwable pErr) {
      LOGGER.error(pMsg, pErr);
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         doPause(pMsg);
      }

   }

   public static <T extends Throwable> T pauseInIde(T pThrowable) {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         LOGGER.error("Trying to throw a fatal exception, pausing in IDE", pThrowable);
         doPause(pThrowable.getMessage());
      }

      return pThrowable;
   }

   public static void setPause(Consumer<String> pThePauser) {
      thePauser = pThePauser;
   }

   private static void doPause(String pMsg) {
      Instant instant = Instant.now();
      LOGGER.warn("Did you remember to set a breakpoint here?");
      boolean flag = Duration.between(instant, Instant.now()).toMillis() > 500L;
      if (!flag) {
         thePauser.accept(pMsg);
      }

   }

   public static String describeError(Throwable pThrowable) {
      if (pThrowable.getCause() != null) {
         return describeError(pThrowable.getCause());
      } else {
         return pThrowable.getMessage() != null ? pThrowable.getMessage() : pThrowable.toString();
      }
   }

   public static <T> T getRandom(T[] pSelections, RandomSource pRandom) {
      return pSelections[pRandom.nextInt(pSelections.length)];
   }

   public static int getRandom(int[] pSelections, RandomSource pRandom) {
      return pSelections[pRandom.nextInt(pSelections.length)];
   }

   public static <T> T getRandom(List<T> pSelections, RandomSource pRandom) {
      return pSelections.get(pRandom.nextInt(pSelections.size()));
   }

   public static <T> Optional<T> getRandomSafe(List<T> pSelections, RandomSource pRandom) {
      return pSelections.isEmpty() ? Optional.empty() : Optional.of(getRandom(pSelections, pRandom));
   }

   private static BooleanSupplier createRenamer(final Path pFilePath, final Path pNewName) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.move(pFilePath, pNewName);
               return true;
            } catch (IOException ioexception) {
               Util.LOGGER.error("Failed to rename", (Throwable)ioexception);
               return false;
            }
         }

         public String toString() {
            return "rename " + pFilePath + " to " + pNewName;
         }
      };
   }

   private static BooleanSupplier createDeleter(final Path pFilePath) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.deleteIfExists(pFilePath);
               return true;
            } catch (IOException ioexception) {
               Util.LOGGER.warn("Failed to delete", (Throwable)ioexception);
               return false;
            }
         }

         public String toString() {
            return "delete old " + pFilePath;
         }
      };
   }

   private static BooleanSupplier createFileDeletedCheck(final Path pFilePath) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return !Files.exists(pFilePath);
         }

         public String toString() {
            return "verify that " + pFilePath + " is deleted";
         }
      };
   }

   private static BooleanSupplier createFileCreatedCheck(final Path pFilePath) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return Files.isRegularFile(pFilePath);
         }

         public String toString() {
            return "verify that " + pFilePath + " is present";
         }
      };
   }

   private static boolean executeInSequence(BooleanSupplier... pSuppliers) {
      for(BooleanSupplier booleansupplier : pSuppliers) {
         if (!booleansupplier.getAsBoolean()) {
            LOGGER.warn("Failed to execute {}", (Object)booleansupplier);
            return false;
         }
      }

      return true;
   }

   private static boolean runWithRetries(int pMaxTries, String pActionName, BooleanSupplier... pSuppliers) {
      for(int i = 0; i < pMaxTries; ++i) {
         if (executeInSequence(pSuppliers)) {
            return true;
         }

         LOGGER.error("Failed to {}, retrying {}/{}", pActionName, i, pMaxTries);
      }

      LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)pActionName);
      return false;
   }

   public static void safeReplaceFile(File pCurrent, File pLatest, File pOldBackup) {
      safeReplaceFile(pCurrent.toPath(), pLatest.toPath(), pOldBackup.toPath());
   }

   public static void safeReplaceFile(Path pCurrent, Path pLatest, Path pOldBackup) {
      safeReplaceOrMoveFile(pCurrent, pLatest, pOldBackup, false);
   }

   public static void safeReplaceOrMoveFile(File pCurrent, File pLatest, File pOldBackup, boolean pMove) {
      safeReplaceOrMoveFile(pCurrent.toPath(), pLatest.toPath(), pOldBackup.toPath(), pMove);
   }

   public static void safeReplaceOrMoveFile(Path pCurrent, Path pLatest, Path pOldBackup, boolean pMove) {
      int i = 10;
      if (!Files.exists(pCurrent) || runWithRetries(10, "create backup " + pOldBackup, createDeleter(pOldBackup), createRenamer(pCurrent, pOldBackup), createFileCreatedCheck(pOldBackup))) {
         if (runWithRetries(10, "remove old " + pCurrent, createDeleter(pCurrent), createFileDeletedCheck(pCurrent))) {
            if (!runWithRetries(10, "replace " + pCurrent + " with " + pLatest, createRenamer(pLatest, pCurrent), createFileCreatedCheck(pCurrent)) && !pMove) {
               runWithRetries(10, "restore " + pCurrent + " from " + pOldBackup, createRenamer(pOldBackup, pCurrent), createFileCreatedCheck(pCurrent));
            }

         }
      }
   }

   public static int offsetByCodepoints(String pText, int pCursorPos, int pDirection) {
      int i = pText.length();
      if (pDirection >= 0) {
         for(int j = 0; pCursorPos < i && j < pDirection; ++j) {
            if (Character.isHighSurrogate(pText.charAt(pCursorPos++)) && pCursorPos < i && Character.isLowSurrogate(pText.charAt(pCursorPos))) {
               ++pCursorPos;
            }
         }
      } else {
         for(int k = pDirection; pCursorPos > 0 && k < 0; ++k) {
            --pCursorPos;
            if (Character.isLowSurrogate(pText.charAt(pCursorPos)) && pCursorPos > 0 && Character.isHighSurrogate(pText.charAt(pCursorPos - 1))) {
               --pCursorPos;
            }
         }
      }

      return pCursorPos;
   }

   public static Consumer<String> prefix(String pPrefix, Consumer<String> pExpectedSize) {
      return (p_214645_) -> {
         pExpectedSize.accept(pPrefix + p_214645_);
      };
   }

   public static DataResult<int[]> fixedSize(IntStream pStream, int pSize) {
      int[] aint = pStream.limit((long)(pSize + 1)).toArray();
      if (aint.length != pSize) {
         Supplier<String> supplier = () -> {
            return "Input is not a list of " + pSize + " ints";
         };
         return aint.length >= pSize ? DataResult.error(supplier, Arrays.copyOf(aint, pSize)) : DataResult.error(supplier);
      } else {
         return DataResult.success(aint);
      }
   }

   public static DataResult<long[]> fixedSize(LongStream pStream, int pExpectedSize) {
      long[] along = pStream.limit((long)(pExpectedSize + 1)).toArray();
      if (along.length != pExpectedSize) {
         Supplier<String> supplier = () -> {
            return "Input is not a list of " + pExpectedSize + " longs";
         };
         return along.length >= pExpectedSize ? DataResult.error(supplier, Arrays.copyOf(along, pExpectedSize)) : DataResult.error(supplier);
      } else {
         return DataResult.success(along);
      }
   }

   public static <T> DataResult<List<T>> fixedSize(List<T> pList, int pExpectedSize) {
      if (pList.size() != pExpectedSize) {
         Supplier<String> supplier = () -> {
            return "Input is not a list of " + pExpectedSize + " elements";
         };
         return pList.size() >= pExpectedSize ? DataResult.error(supplier, pList.subList(0, pExpectedSize)) : DataResult.error(supplier);
      } else {
         return DataResult.success(pList);
      }
   }

   public static void startTimerHackThread() {
      Thread thread = new Thread("Timer hack thread") {
         public void run() {
            while(true) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException interruptedexception) {
                  Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                  return;
               }
            }
         }
      };
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public static void copyBetweenDirs(Path pFromDirectory, Path pToDirectory, Path pFilePath) throws IOException {
      Path path = pFromDirectory.relativize(pFilePath);
      Path path1 = pToDirectory.resolve(path);
      Files.copy(pFilePath, path1);
   }

   public static String sanitizeName(String pFileName, CharPredicate pCharacterValidator) {
      return pFileName.toLowerCase(Locale.ROOT).chars().mapToObj((p_214666_) -> {
         return pCharacterValidator.test((char)p_214666_) ? Character.toString((char)p_214666_) : "_";
      }).collect(Collectors.joining());
   }

   public static <K, V> SingleKeyCache<K, V> singleKeyCache(Function<K, V> pComputeValue) {
      return new SingleKeyCache<>(pComputeValue);
   }

   public static <T, R> Function<T, R> memoize(final Function<T, R> pMemoFunction) {
      return new Function<T, R>() {
         private final Map<T, R> cache = new ConcurrentHashMap<>();

         public R apply(T p_214691_) {
            return this.cache.computeIfAbsent(p_214691_, pMemoFunction);
         }

         public String toString() {
            return "memoize/1[function=" + pMemoFunction + ", size=" + this.cache.size() + "]";
         }
      };
   }

   public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> pMemoBiFunction) {
      return new BiFunction<T, U, R>() {
         private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap<>();

         public R apply(T p_214700_, U p_214701_) {
            return this.cache.computeIfAbsent(Pair.of(p_214700_, p_214701_), (p_214698_) -> {
               return pMemoBiFunction.apply(p_214698_.getFirst(), p_214698_.getSecond());
            });
         }

         public String toString() {
            return "memoize/2[function=" + pMemoBiFunction + ", size=" + this.cache.size() + "]";
         }
      };
   }

   public static <T> List<T> toShuffledList(Stream<T> pStream, RandomSource pRandom) {
      ObjectArrayList<T> objectarraylist = pStream.collect(ObjectArrayList.toList());
      shuffle(objectarraylist, pRandom);
      return objectarraylist;
   }

   public static IntArrayList toShuffledList(IntStream pStream, RandomSource pRandom) {
      IntArrayList intarraylist = IntArrayList.wrap(pStream.toArray());
      int i = intarraylist.size();

      for(int j = i; j > 1; --j) {
         int k = pRandom.nextInt(j);
         intarraylist.set(j - 1, intarraylist.set(k, intarraylist.getInt(j - 1)));
      }

      return intarraylist;
   }

   public static <T> List<T> shuffledCopy(T[] pArray, RandomSource pRandom) {
      ObjectArrayList<T> objectarraylist = new ObjectArrayList<>(pArray);
      shuffle(objectarraylist, pRandom);
      return objectarraylist;
   }

   public static <T> List<T> shuffledCopy(ObjectArrayList<T> pList, RandomSource pRandom) {
      ObjectArrayList<T> objectarraylist = new ObjectArrayList<>(pList);
      shuffle(objectarraylist, pRandom);
      return objectarraylist;
   }

   public static <T> void shuffle(ObjectArrayList<T> pList, RandomSource pRandom) {
      int i = pList.size();

      for(int j = i; j > 1; --j) {
         int k = pRandom.nextInt(j);
         pList.set(j - 1, pList.set(k, pList.get(j - 1)));
      }

   }

   public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> pTask) {
      return blockUntilDone(pTask, CompletableFuture::isDone);
   }

   public static <T> T blockUntilDone(Function<Executor, T> pTask, Predicate<T> pDonePredicate) {
      BlockingQueue<Runnable> blockingqueue = new LinkedBlockingQueue<>();
      T t = pTask.apply(blockingqueue::add);

      while(!pDonePredicate.test(t)) {
         try {
            Runnable runnable = blockingqueue.poll(100L, TimeUnit.MILLISECONDS);
            if (runnable != null) {
               runnable.run();
            }
         } catch (InterruptedException interruptedexception) {
            LOGGER.warn("Interrupted wait");
            break;
         }
      }

      int i = blockingqueue.size();
      if (i > 0) {
         LOGGER.warn("Tasks left in queue: {}", (int)i);
      }

      return t;
   }

   public static <T> ToIntFunction<T> createIndexLookup(List<T> pList) {
      return createIndexLookup(pList, Object2IntOpenHashMap::new);
   }

   public static <T> ToIntFunction<T> createIndexLookup(List<T> pList, IntFunction<Object2IntMap<T>> pMapper) {
      Object2IntMap<T> object2intmap = pMapper.apply(pList.size());

      for(int i = 0; i < pList.size(); ++i) {
         object2intmap.put(pList.get(i), i);
      }

      return object2intmap;
   }

   public static <T, E extends Exception> T getOrThrow(DataResult<T> pDataResult, Function<String, E> pExceptionGetter) throws E {
      Optional<DataResult.PartialResult<T>> optional = pDataResult.error();
      if (optional.isPresent()) {
         throw pExceptionGetter.apply(optional.get().message());
      } else {
         return pDataResult.result().orElseThrow();
      }
   }

   public static boolean isWhitespace(int p_289004_) {
      return Character.isWhitespace(p_289004_) || Character.isSpaceChar(p_289004_);
   }

   public static boolean isBlank(@Nullable String pString) {
      return pString != null && pString.length() != 0 ? pString.chars().allMatch(Util::isWhitespace) : true;
   }

   static enum IdentityStrategy implements Hash.Strategy<Object> {
      INSTANCE;

      public int hashCode(Object pObject) {
         return System.identityHashCode(pObject);
      }

      public boolean equals(Object pThiz, Object pOther) {
         return pThiz == pOther;
      }
   }

   public static enum OS {
      LINUX("linux"),
      SOLARIS("solaris"),
      WINDOWS("windows") {
         protected String[] getOpenUrlArguments(URL p_137662_) {
            return new String[]{"rundll32", "url.dll,FileProtocolHandler", p_137662_.toString()};
         }
      },
      OSX("mac") {
         protected String[] getOpenUrlArguments(URL p_137667_) {
            return new String[]{"open", p_137667_.toString()};
         }
      },
      UNKNOWN("unknown");

      private final String telemetryName;

      OS(String pTelemetryName) {
         this.telemetryName = pTelemetryName;
      }

      public void openUrl(URL pUrl) {
         try {
            Process process = AccessController.doPrivileged((PrivilegedExceptionAction<Process>)(() -> {
               return Runtime.getRuntime().exec(this.getOpenUrlArguments(pUrl));
            }));
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
         } catch (IOException | PrivilegedActionException privilegedactionexception) {
            Util.LOGGER.error("Couldn't open url '{}'", pUrl, privilegedactionexception);
         }

      }

      public void openUri(URI pUri) {
         try {
            this.openUrl(pUri.toURL());
         } catch (MalformedURLException malformedurlexception) {
            Util.LOGGER.error("Couldn't open uri '{}'", pUri, malformedurlexception);
         }

      }

      public void openFile(File pFile) {
         try {
            this.openUrl(pFile.toURI().toURL());
         } catch (MalformedURLException malformedurlexception) {
            Util.LOGGER.error("Couldn't open file '{}'", pFile, malformedurlexception);
         }

      }

      protected String[] getOpenUrlArguments(URL pUrl) {
         String s = pUrl.toString();
         if ("file".equals(pUrl.getProtocol())) {
            s = s.replace("file:", "file://");
         }

         return new String[]{"xdg-open", s};
      }

      public void openUri(String pUri) {
         try {
            this.openUrl((new URI(pUri)).toURL());
         } catch (MalformedURLException | IllegalArgumentException | URISyntaxException urisyntaxexception) {
            Util.LOGGER.error("Couldn't open uri '{}'", pUri, urisyntaxexception);
         }

      }

      public String telemetryName() {
         return this.telemetryName;
      }
   }
}
