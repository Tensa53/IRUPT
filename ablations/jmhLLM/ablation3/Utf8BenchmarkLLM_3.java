/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.util.benchmark;

import org.apache.avro.util.Utf8;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Utf8 class focusing on lines 64-65 and 116-117-118.
 *
 * Benchmarked methods: - Lines 64-65: Copy constructor Utf8(Utf8 other) - byte
 * array allocation and System.arraycopy - Lines 116-117-118: setByteLength(int)
 * - byte array reallocation and System.arraycopy
 *
 * JMH Version: 1.37
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_3 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    // Source Utf8 objects for copy constructor benchmark
    private Utf8 smallUtf8;
    private Utf8 mediumUtf8;
    private Utf8 largeUtf8;

    // Reusable Utf8 objects for setByteLength benchmark
    private Utf8 utf8ForGrowth;
    private Utf8 utf8ForShrink;
    private Utf8 utf8ForSameSize;

    // Test data sizes
    @Param({ "10", "100", "1000", "10000" })
    public int dataSize;

    @Setup(Level.Trial)
    public void setup() {
      // Initialize source objects for copy constructor
      // Create Utf8 with different sizes by using byte arrays
      byte[] smallData = new byte[10];
      byte[] mediumData = new byte[100];
      byte[] largeData = new byte[1000];

      // Fill with sample data
      for (int i = 0; i < smallData.length; i++) {
        smallData[i] = (byte) ('a' + (i % 26));
      }
      for (int i = 0; i < mediumData.length; i++) {
        mediumData[i] = (byte) ('a' + (i % 26));
      }
      for (int i = 0; i < largeData.length; i++) {
        largeData[i] = (byte) ('a' + (i % 26));
      }

      smallUtf8 = new Utf8(smallData);
      mediumUtf8 = new Utf8(mediumData);
      largeUtf8 = new Utf8(largeData);

      // Initialize objects for setByteLength benchmark
      byte[] initialData = new byte[dataSize];
      for (int i = 0; i < initialData.length; i++) {
        initialData[i] = (byte) ('a' + (i % 26));
      }

      utf8ForGrowth = new Utf8(initialData);
      utf8ForShrink = new Utf8(initialData);
      utf8ForSameSize = new Utf8(initialData);
    }

    @Setup(Level.Invocation)
    public void resetState() {
      // Reset objects for setByteLength to ensure consistent starting conditions
      byte[] resetData = new byte[dataSize];
      for (int i = 0; i < resetData.length; i++) {
        resetData[i] = (byte) ('a' + (i % 26));
      }

      utf8ForGrowth = new Utf8(resetData);
      utf8ForShrink = new Utf8(resetData);
      utf8ForSameSize = new Utf8(resetData);
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests the performance of:
   * - new byte[other.length] allocation - System.arraycopy(other.bytes, 0,
   * this.bytes, 0, this.length)
   *
   * This benchmark measures the cost of creating a new Utf8 object by copying
   * from an existing one with small data (10 bytes).
   */
  @Benchmark
  public void benchmarkCopyConstructorSmall(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.smallUtf8);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests the performance of:
   * - new byte[other.length] allocation - System.arraycopy(other.bytes, 0,
   * this.bytes, 0, this.length)
   *
   * This benchmark measures the cost of creating a new Utf8 object by copying
   * from an existing one with medium data (100 bytes).
   */
  @Benchmark
  public void benchmarkCopyConstructorMedium(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.mediumUtf8);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests the performance of:
   * - new byte[other.length] allocation - System.arraycopy(other.bytes, 0,
   * this.bytes, 0, this.length)
   *
   * This benchmark measures the cost of creating a new Utf8 object by copying
   * from an existing one with large data (1000 bytes).
   */
  @Benchmark
  public void benchmarkCopyConstructorLarge(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.largeUtf8);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for setByteLength with growth scenario (lines 116-117-118). Tests
   * the performance when the new length is LARGER than current capacity: - byte[]
   * newBytes = new byte[newLength] allocation - System.arraycopy(bytes, 0,
   * newBytes, 0, this.length) - this.bytes = newBytes assignment
   *
   * This forces array reallocation and copy of existing data.
   */
  @Benchmark
  public void benchmarkSetByteLengthGrowth(BenchmarkState state, Blackhole blackhole) {
    // Grow to double the current size, forcing reallocation
    Utf8 result = state.utf8ForGrowth.setByteLength(state.dataSize * 2);
    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength with shrink scenario (lines 116-117-118). Tests
   * the performance when the new length is SMALLER than current capacity: - No
   * new array allocation (this.bytes.length >= newLength) - Only updates
   * this.length and this.string = null
   *
   * This is the fast path with no memory allocation.
   */
  @Benchmark
  public void benchmarkSetByteLengthShrink(BenchmarkState state, Blackhole blackhole) {
    // Shrink to half the current size, no reallocation needed
    Utf8 result = state.utf8ForShrink.setByteLength(state.dataSize / 2);
    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength with same size scenario (lines 116-117-118).
   * Tests the performance when the new length equals current length: - No new
   * array allocation (this.bytes.length >= newLength) - Only updates this.length
   * and this.string = null
   *
   * This represents the minimal overhead case (cache invalidation only).
   */
  @Benchmark
  public void benchmarkSetByteLengthSameSize(BenchmarkState state, Blackhole blackhole) {
    // Set to same size, minimal overhead
    Utf8 result = state.utf8ForSameSize.setByteLength(state.dataSize);
    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength with incremental growth (lines 116-117-118).
   * Tests the performance when growing by small increments: - Forces reallocation
   * multiple times - Measures cumulative cost of System.arraycopy
   *
   * This simulates real-world scenarios where Utf8 grows incrementally.
   */
  @Benchmark
  public void benchmarkSetByteLengthIncrementalGrowth(BenchmarkState state, Blackhole blackhole) {
    byte[] data = new byte[10];
    Utf8 utf8 = new Utf8(data);

    // Grow incrementally 10 times
    for (int i = 1; i <= 10; i++) {
      utf8.setByteLength(10 * i);
    }

    blackhole.consume(utf8);
  }
}
