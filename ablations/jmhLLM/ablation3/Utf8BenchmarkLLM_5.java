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

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Utf8 class focusing on: - Lines 64-65: Copy constructor
 * (array allocation and System.arraycopy) - Lines 116-117-118: setByteLength
 * method (array reallocation and System.arraycopy)
 *
 * This benchmark measures the performance of memory allocation and array
 * copying operations in the Utf8 class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_5 {

  /**
   * Benchmark state containing test data and objects. Uses Thread scope for
   * thread-safe benchmarking.
   */
  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data with various sizes to simulate real-world scenarios
    @Param({ "10", "100", "1000", "10000" })
    public int stringSize;

    public Utf8 sourceUtf8Small;
    public Utf8 sourceUtf8Medium;
    public Utf8 sourceUtf8Large;
    public Utf8 targetUtf8;

    @Setup(Level.Trial)
    public void setup() {
      // Initialize source Utf8 objects with different sizes
      sourceUtf8Small = new Utf8(createString(10));
      sourceUtf8Medium = new Utf8(createString(100));
      sourceUtf8Large = new Utf8(createString(1000));

      // Initialize target for setByteLength benchmarks
      targetUtf8 = new Utf8(createString(stringSize));
    }

    /**
     * Creates a string of specified length for testing.
     */
    private String createString(int length) {
      StringBuilder sb = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      return sb.toString();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reset target for each invocation to ensure consistent state
      targetUtf8 = new Utf8(createString(stringSize));
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests the performance of:
   * - new byte[] allocation (line 64) - System.arraycopy operation (line 65)
   *
   * This benchmark measures the cost of creating a new Utf8 instance by copying
   * an existing one, which involves allocating a new byte array and copying the
   * contents.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium-sized strings (lines 64-65).
   * Tests array allocation and copy performance with 100-byte strings.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorMedium(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor with large strings (lines 64-65). Tests
   * array allocation and copy performance with 1000-byte strings.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for setByteLength method when reallocation is NOT needed. Tests the
   * performance when the existing byte array is large enough, so only the length
   * field is updated (line 120) without triggering the reallocation code on lines
   * 116-118.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoReallocation(BenchmarkState state) {
    // Set to a smaller length that doesn't require reallocation
    return state.targetUtf8.setByteLength(state.stringSize / 2);
  }

  /**
   * Benchmark for setByteLength method when reallocation IS needed (lines
   * 116-117-118). Tests the performance of: - new byte[] allocation (line 116) -
   * System.arraycopy operation (line 117) - byte[] assignment (line 118)
   *
   * This benchmark measures the cost of growing the internal byte array when
   * setByteLength is called with a size larger than the current capacity.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithReallocation(BenchmarkState state) {
    // Set to a larger length that requires reallocation
    return state.targetUtf8.setByteLength(state.stringSize * 2);
  }

  /**
   * Benchmark for setByteLength with progressive reallocation. Tests the
   * performance of multiple reallocations in sequence, simulating scenarios where
   * the Utf8 object grows incrementally.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthProgressiveReallocation(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    // Trigger multiple reallocations by progressively increasing size
    utf8.setByteLength(state.stringSize / 4);
    utf8.setByteLength(state.stringSize / 2);
    utf8.setByteLength(state.stringSize);
    return utf8.setByteLength(state.stringSize * 2);
  }

  /**
   * Baseline benchmark: Create empty Utf8 object. Helps establish the baseline
   * cost of object creation without copy operations.
   */
  @Benchmark
  public Utf8 benchmarkUtf8EmptyConstructor() {
    return new Utf8();
  }

  /**
   * Combined benchmark: Copy constructor followed by setByteLength. Tests the
   * performance of both operations in sequence, which is a common pattern in
   * real-world usage.
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorThenSetByteLength(BenchmarkState state) {
    Utf8 copy = new Utf8(state.sourceUtf8Medium);
    return copy.setByteLength(200);
  }
}
