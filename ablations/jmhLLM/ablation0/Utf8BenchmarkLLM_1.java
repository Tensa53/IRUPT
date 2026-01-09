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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Utf8 class
 *
 * Benchmarks the following methods and lines: - Lines 64-65: Utf8(Utf8 other)
 * copy constructor - array allocation and System.arraycopy - Lines 116-117-118:
 * setByteLength(int) - array reallocation with System.arraycopy
 *
 * This benchmark class focuses on performance-critical operations involving
 * byte array manipulation and memory allocation in the Utf8 class.
 *
 * JMH Version: 1.37 Generated: 2024-12-08
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_1 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data sizes - varying sizes to test different scenarios
    @Param({ "10", "100", "1000", "10000" })
    private int stringSize;

    // Source Utf8 instances for copy constructor benchmarks
    private Utf8 sourceUtf8Small;
    private Utf8 sourceUtf8Medium;
    private Utf8 sourceUtf8Large;
    private Utf8 sourceUtf8VeryLarge;

    // Target Utf8 instances for setByteLength benchmarks
    private Utf8 targetUtf8ForResize;
    private Utf8 targetUtf8WithInitialCapacity;

    // Strings for initialization
    private String testStringSmall;
    private String testStringMedium;
    private String testStringLarge;
    private String testStringVeryLarge;

    // New length values for setByteLength tests
    private int smallerLength;
    private int largerLength;

    @Setup(Level.Trial)
    public void setup() {
      // Initialize test strings with repeating patterns
      testStringSmall = buildTestString(10);
      testStringMedium = buildTestString(100);
      testStringLarge = buildTestString(1000);
      testStringVeryLarge = buildTestString(10000);

      // Initialize source Utf8 instances for copy constructor tests
      sourceUtf8Small = new Utf8(testStringSmall);
      sourceUtf8Medium = new Utf8(testStringMedium);
      sourceUtf8Large = new Utf8(testStringLarge);
      sourceUtf8VeryLarge = new Utf8(testStringVeryLarge);

      // Calculate lengths for setByteLength tests
      smallerLength = stringSize / 2;
      largerLength = stringSize * 2;
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reset target instances before each invocation for setByteLength tests
      // This ensures we're testing the reallocation path consistently
      targetUtf8ForResize = new Utf8(buildTestString(stringSize));
      targetUtf8WithInitialCapacity = new Utf8(buildTestString(stringSize / 2));
    }

    private String buildTestString(int size) {
      StringBuilder sb = new StringBuilder(size);
      for (int i = 0; i < size; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      return sb.toString();
    }

    public Utf8 getSourceUtf8() {
      switch (stringSize) {
      case 10:
        return sourceUtf8Small;
      case 100:
        return sourceUtf8Medium;
      case 1000:
        return sourceUtf8Large;
      case 10000:
        return sourceUtf8VeryLarge;
      default:
        return sourceUtf8Medium;
      }
    }
  }

  /**
   * Benchmark for Utf8(Utf8 other) copy constructor (Lines 64-65)
   *
   * This tests the performance of: - new byte[other.length] allocation (line 64)
   * - System.arraycopy(other.bytes, 0, this.bytes, 0, this.length) (line 65)
   *
   * The copy constructor creates a deep copy of the byte array, which is a
   * critical operation for immutability and thread safety scenarios.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.getSourceUtf8());
  }

  /**
   * Benchmark for setByteLength with expansion (Lines 116-117-118)
   *
   * This tests the reallocation path where newLength > current bytes.length: -
   * byte[] newBytes = new byte[newLength] (line 116) - System.arraycopy(bytes, 0,
   * newBytes, 0, this.length) (line 117) - this.bytes = newBytes (line 118)
   *
   * This is the expensive path that requires allocation and copying.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthExpansion(BenchmarkState state) {
    return state.targetUtf8WithInitialCapacity.setByteLength(state.largerLength);
  }

  /**
   * Benchmark for setByteLength without expansion (Line 120)
   *
   * This tests the fast path where newLength <= current bytes.length. No array
   * reallocation occurs, only the length field is updated. This provides a
   * baseline comparison against the reallocation path.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoExpansion(BenchmarkState state) {
    return state.targetUtf8ForResize.setByteLength(state.smallerLength);
  }

  /**
   * Benchmark for setByteLength with same length (Line 120-121)
   *
   * Tests the scenario where the length doesn't change but the method is called
   * to clear the cached string (line 121: this.string = null). This is common
   * when reusing Utf8 instances.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSameLength(BenchmarkState state) {
    return state.targetUtf8ForResize.setByteLength(state.stringSize);
  }

  /**
   * Combined benchmark: Copy constructor followed by setByteLength
   *
   * This tests a realistic usage pattern where a Utf8 is copied and then resized.
   * Helps understand the cumulative cost of these operations.
   */
  @Benchmark
  public Utf8 benchmarkCopyAndResize(BenchmarkState state) {
    Utf8 copy = new Utf8(state.getSourceUtf8());
    return copy.setByteLength(state.largerLength);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_1.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
