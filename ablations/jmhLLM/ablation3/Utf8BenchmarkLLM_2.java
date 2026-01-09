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
 * with System.arraycopy - Lines 116-117-118: setByteLength method with array
 * expansion
 *
 * This benchmark measures the performance characteristics of these specific
 * operations in isolation to identify potential optimization opportunities.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_2 {

  /**
   * State class containing all benchmark dependencies and test data. Scope.Thread
   * ensures each thread gets its own instance.
   */
  @State(Scope.Thread)
  public static class BenchmarkState {

    // Parameterized string sizes to test various scenarios
    @Param({ "10", "100", "1000", "10000" })
    public int stringSize;

    // Parameterized new length for setByteLength testing
    @Param({ "50", "500", "5000" })
    public int newLength;

    // Source Utf8 instance for copy constructor testing
    public Utf8 sourceUtf8;

    // Utf8 instance for setByteLength testing (needs expansion)
    public Utf8 targetUtf8Small;

    // Utf8 instance for setByteLength testing (no expansion needed)
    public Utf8 targetUtf8Large;

    /**
     * Setup method to initialize all test objects before benchmark execution.
     * Creates Utf8 instances with various sizes for different test scenarios.
     */
    @Setup(Level.Trial)
    public void setup() {
      // Create a string of the specified size
      StringBuilder sb = new StringBuilder(stringSize);
      for (int i = 0; i < stringSize; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      String testString = sb.toString();

      // Initialize source Utf8 for copy constructor benchmark
      sourceUtf8 = new Utf8(testString);

      // Initialize small Utf8 (will require array expansion in setByteLength)
      targetUtf8Small = new Utf8("small");

      // Initialize large Utf8 (already has sufficient capacity)
      targetUtf8Large = new Utf8(testString + testString);
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests the performance of
   * creating a new Utf8 instance by copying another Utf8 instance, which involves
   * byte array allocation and System.arraycopy operation.
   *
   * Critical path: - Line 64: new byte[other.length] - Line 65:
   * System.arraycopy(other.bytes, 0, this.bytes, 0, this.length)
   */
  @Benchmark
  public void benchmarkCopyConstructor(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for setByteLength method when array expansion is required (lines
   * 116-117-118). Tests the scenario where the current byte array is too small
   * and needs to be expanded, which involves allocating a new array and copying
   * existing data.
   *
   * Critical path (expansion needed): - Line 116: new byte[newLength] - Line 117:
   * System.arraycopy(bytes, 0, newBytes, 0, this.length) - Line 118: this.bytes =
   * newBytes
   */
  @Benchmark
  public void benchmarkSetByteLengthWithExpansion(BenchmarkState state, Blackhole blackhole) {
    // Reset to small state before each invocation
    Utf8 utf8 = new Utf8("small");
    utf8.setByteLength(state.newLength);
    blackhole.consume(utf8);
  }

  /**
   * Benchmark for setByteLength method when NO array expansion is required. Tests
   * the fast path where the existing byte array is already large enough, so only
   * the length field needs to be updated.
   *
   * This serves as a baseline to measure the overhead of the expansion path.
   */
  @Benchmark
  public void benchmarkSetByteLengthWithoutExpansion(BenchmarkState state, Blackhole blackhole) {
    // Use a Utf8 with sufficient capacity
    Utf8 utf8 = new Utf8(state.targetUtf8Large);
    // Set to a length smaller than current capacity
    utf8.setByteLength(state.stringSize / 2);
    blackhole.consume(utf8);
  }

  /**
   * Benchmark for measuring the baseline cost of System.arraycopy in isolation.
   * This helps understand the raw cost of memory copying operations used in both
   * the copy constructor and setByteLength.
   */
  @Benchmark
  public void benchmarkArrayCopyBaseline(BenchmarkState state, Blackhole blackhole) {
    byte[] source = state.sourceUtf8.getBytes();
    byte[] dest = new byte[source.length];
    System.arraycopy(source, 0, dest, 0, state.sourceUtf8.getByteLength());
    blackhole.consume(dest);
  }

  /**
   * Benchmark for measuring byte array allocation cost in isolation. This
   * establishes a baseline for the allocation overhead present in both the copy
   * constructor and setByteLength expansion path.
   */
  @Benchmark
  public void benchmarkArrayAllocationBaseline(BenchmarkState state, Blackhole blackhole) {
    byte[] newArray = new byte[state.stringSize];
    blackhole.consume(newArray);
  }
}
