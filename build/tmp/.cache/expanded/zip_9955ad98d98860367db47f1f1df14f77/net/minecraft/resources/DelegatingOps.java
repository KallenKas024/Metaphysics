package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A {@link DynamicOps} that delegates all functionality to an internal delegate. Comments and parameters here are
 * copied from {@link DynamicOps} in DataFixerUpper.
 */
public abstract class DelegatingOps<T> implements DynamicOps<T> {
   protected final DynamicOps<T> delegate;

   protected DelegatingOps(DynamicOps<T> pDelegate) {
      this.delegate = pDelegate;
   }

   public T empty() {
      return this.delegate.empty();
   }

   public <U> U convertTo(DynamicOps<U> pOutOps, T pInput) {
      return this.delegate.convertTo(pOutOps, pInput);
   }

   public DataResult<Number> getNumberValue(T pInput) {
      return this.delegate.getNumberValue(pInput);
   }

   public T createNumeric(Number pI) {
      return this.delegate.createNumeric(pI);
   }

   public T createByte(byte pValue) {
      return this.delegate.createByte(pValue);
   }

   public T createShort(short pValue) {
      return this.delegate.createShort(pValue);
   }

   public T createInt(int pValue) {
      return this.delegate.createInt(pValue);
   }

   public T createLong(long pValue) {
      return this.delegate.createLong(pValue);
   }

   public T createFloat(float pValue) {
      return this.delegate.createFloat(pValue);
   }

   public T createDouble(double pValue) {
      return this.delegate.createDouble(pValue);
   }

   public DataResult<Boolean> getBooleanValue(T pInput) {
      return this.delegate.getBooleanValue(pInput);
   }

   public T createBoolean(boolean pValue) {
      return this.delegate.createBoolean(pValue);
   }

   public DataResult<String> getStringValue(T pInput) {
      return this.delegate.getStringValue(pInput);
   }

   public T createString(String pValue) {
      return this.delegate.createString(pValue);
   }

   public DataResult<T> mergeToList(T pList, T pValue) {
      return this.delegate.mergeToList(pList, pValue);
   }

   public DataResult<T> mergeToList(T pList, List<T> pValues) {
      return this.delegate.mergeToList(pList, pValues);
   }

   public DataResult<T> mergeToMap(T pMap, T pKey, T pValue) {
      return this.delegate.mergeToMap(pMap, pKey, pValue);
   }

   public DataResult<T> mergeToMap(T pMap, MapLike<T> pValues) {
      return this.delegate.mergeToMap(pMap, pValues);
   }

   public DataResult<Stream<Pair<T, T>>> getMapValues(T pInput) {
      return this.delegate.getMapValues(pInput);
   }

   public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T pInput) {
      return this.delegate.getMapEntries(pInput);
   }

   public T createMap(Stream<Pair<T, T>> pMap) {
      return this.delegate.createMap(pMap);
   }

   public DataResult<MapLike<T>> getMap(T pInput) {
      return this.delegate.getMap(pInput);
   }

   public DataResult<Stream<T>> getStream(T pInput) {
      return this.delegate.getStream(pInput);
   }

   public DataResult<Consumer<Consumer<T>>> getList(T pInput) {
      return this.delegate.getList(pInput);
   }

   public T createList(Stream<T> pInput) {
      return this.delegate.createList(pInput);
   }

   public DataResult<ByteBuffer> getByteBuffer(T pInput) {
      return this.delegate.getByteBuffer(pInput);
   }

   public T createByteList(ByteBuffer pInput) {
      return this.delegate.createByteList(pInput);
   }

   public DataResult<IntStream> getIntStream(T pInput) {
      return this.delegate.getIntStream(pInput);
   }

   public T createIntList(IntStream pInput) {
      return this.delegate.createIntList(pInput);
   }

   public DataResult<LongStream> getLongStream(T pInput) {
      return this.delegate.getLongStream(pInput);
   }

   public T createLongList(LongStream pInput) {
      return this.delegate.createLongList(pInput);
   }

   public T remove(T pInput, String pKey) {
      return this.delegate.remove(pInput, pKey);
   }

   public boolean compressMaps() {
      return this.delegate.compressMaps();
   }

   public ListBuilder<T> listBuilder() {
      return new ListBuilder.Builder<>(this);
   }

   public RecordBuilder<T> mapBuilder() {
      return new RecordBuilder.MapBuilder<>(this);
   }
}