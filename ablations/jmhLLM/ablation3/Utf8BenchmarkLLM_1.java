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
 * JMH Benchmark for Utf8 class focusing on: - Lines 64-65: Copy constructor
 * array allocation and System.arraycopy - Lines 116-118: setByteLength method
 * array reallocation and System.arraycopy
 *
 * This benchmark uses JMH 1.37 and follows best practices for
 * microbenchmarking.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = { "-XX:+UseG1GC", "-Xms2g", "-Xmx2g" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_1 {

  /**
   * State class containing all benchmark dependencies and test data. Uses Thread
   * scope to ensure each thread has its own state instance.
   */
  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data with various string sizes
    @Param({ "10", "100", "1000", "10000" })
    public int stringSize;

    // Source Utf8 objects for copy constructor benchmark
    public Utf8 sourceUtf8Small;
    public Utf8 sourceUtf8Medium;
    public Utf8 sourceUtf8Large;
    public Utf8 sourceUtf8VeryLarge;

    // Reusable Utf8 object for setByteLength benchmark
    public Utf8 reusableUtf8;

    // Target lengths for setByteLength benchmark
    @Param({ "50", "500", "5000" })
    public int targetLength;

    /**
     * Setup method to initialize all test objects. Called once per benchmark
     * iteration based on the State scope.
     */
    @Setup(Level.Trial)
    public void setup() {
      // Create source strings of various sizes
      String smallString = createString(10);
      String mediumString = createString(100);
      String largeString = createString(1000);
      String veryLargeString = createString(10000);

      // Initialize source Utf8 objects
      sourceUtf8Small = new Utf8(smallString);
      sourceUtf8Medium = new Utf8(mediumString);
      sourceUtf8Large = new Utf8(largeString);
      sourceUtf8VeryLarge = new Utf8(veryLargeString);

      // Initialize reusable Utf8 with parameterized size
      reusableUtf8 = new Utf8(createString(stringSize));
    }

    /**
     * Helper method to create a string of specified length. Uses repeating pattern
     * for consistency.
     */
    private String createString(int length) {
      StringBuilder sb = new StringBuilder(length);
      String pattern = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      for (int i = 0; i < length; i++) {
        sb.append(pattern.charAt(i % pattern.length()));
      }
      return sb.toString();
    }

    /**
     * Cleanup resources after each iteration if needed.
     */
    @TearDown(Level.Trial)
    public void tearDown() {
      // Clear references to allow garbage collection
      sourceUtf8Small = null;
      sourceUtf8Medium = null;
      sourceUtf8Large = null;
      sourceUtf8VeryLarge = null;
      reusableUtf8 = null;
    }
  }

  /**
   * Benchmark for Utf8 copy constructor with small strings (10 bytes). Tests
   * lines 64-65: array allocation and System.arraycopy.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkCopyConstructorSmall(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Small);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium strings (100 bytes). Tests
   * lines 64-65: array allocation and System.arraycopy.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkCopyConstructorMedium(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Medium);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor with large strings (1000 bytes). Tests
   * lines 64-65: array allocation and System.arraycopy.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkCopyConstructorLarge(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Large);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor with very large strings (10000 bytes).
   * Tests lines 64-65: array allocation and System.arraycopy.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkCopyConstructorVeryLarge(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8VeryLarge);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for setByteLength method when it triggers array reallocation. Tests
   * lines 116-118: conditional array reallocation and System.arraycopy. This
   * scenario tests when newLength > current bytes.length, forcing reallocation.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkSetByteLengthWithReallocation(BenchmarkState state, Blackhole blackhole) {
    // Create a fresh Utf8 with initial size
    Utf8 utf8 = new Utf8(state.reusableUtf8);

    // Set to a larger length to trigger reallocation (lines 116-118)
    // Ensure targetLength is larger than stringSize to trigger the if condition
    int newLength = state.stringSize + state.targetLength;
    Utf8 result = utf8.setByteLength(newLength);

    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength method without array reallocation. Tests line
   * 120-122: length update and string cache clearing without reallocation. This
   * scenario tests when newLength <= current bytes.length, avoiding reallocation.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkSetByteLengthWithoutReallocation(BenchmarkState state, Blackhole blackhole) {
    // Create a fresh Utf8 with initial size
    Utf8 utf8 = new Utf8(state.reusableUtf8);

    // Set to a smaller or equal length to avoid reallocation
    int newLength = Math.max(1, state.stringSize / 2);
    Utf8 result = utf8.setByteLength(newLength);

    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength method with incremental growth pattern. Tests
   * lines 116-118: repeated reallocations simulating typical usage. This
   * simulates a common pattern where Utf8 objects grow incrementally.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkSetByteLengthIncrementalGrowth(BenchmarkState state, Blackhole blackhole) {
    // Start with a small Utf8
    Utf8 utf8 = new Utf8(state.sourceUtf8Small);

    // Simulate incremental growth
    int step = state.targetLength / 5;
    for (int i = 1; i <= 5; i++) {
      utf8.setByteLength(10 + (step * i));
    }

    blackhole.consume(utf8);
  }

  /**
   * Combined benchmark testing both copy constructor and setByteLength. This
   * represents a realistic usage pattern where objects are copied and then
   * resized.
   *
   * @param state     The benchmark state
   * @param blackhole JMH blackhole to prevent dead code elimination
   */
  @Benchmark
  public void benchmarkCopyAndResize(BenchmarkState state, Blackhole blackhole) {
    // Copy constructor (lines 64-65)
    Utf8 copy = new Utf8(state.sourceUtf8Medium);

    // Resize (lines 116-118)
    copy.setByteLength(state.targetLength);

    blackhole.consume(copy);
  }
}
