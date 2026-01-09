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
 * JMH Benchmark for Utf8 class using JMH 1.37
 *
 * Benchmarks the following methods and lines: - Lines 64-65: Copy constructor
 * Utf8(Utf8 other) - array allocation and System.arraycopy - Lines 116-117-118:
 * setByteLength(int) method - array reallocation and System.arraycopy
 *
 * This benchmark measures the performance of critical byte array operations in
 * the Utf8 class, which is heavily used in Avro serialization/deserialization.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_5 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data with varying sizes to simulate real-world scenarios
    @Param({ "10", "100", "1000", "10000" })
    private int stringLength;

    // Source Utf8 instance for copy constructor benchmark
    private Utf8 sourceUtf8;

    // Target Utf8 instance for setByteLength benchmark
    private Utf8 targetUtf8;

    // Various new lengths for setByteLength benchmark
    @Param({ "50", "500", "5000" })
    private int newByteLength;

    @Setup(Level.Trial)
    public void setup() {
      // Create a string of the specified length
      StringBuilder sb = new StringBuilder(stringLength);
      for (int i = 0; i < stringLength; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      String testString = sb.toString();

      // Initialize source Utf8 for copy constructor benchmark (lines 64-65)
      sourceUtf8 = new Utf8(testString);

      // Initialize target Utf8 for setByteLength benchmark (lines 116-117-118)
      targetUtf8 = new Utf8(testString);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reset target for setByteLength to ensure consistent state
      StringBuilder sb = new StringBuilder(stringLength);
      for (int i = 0; i < stringLength; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      targetUtf8 = new Utf8(sb.toString());
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65)
   *
   * This benchmark measures the performance of: Line 64: byte[] allocation:
   * this.bytes = new byte[other.length]; Line 65: System.arraycopy(other.bytes,
   * 0, this.bytes, 0, this.length);
   *
   * The copy constructor creates a deep copy of another Utf8 instance, which
   * involves allocating a new byte array and copying the contents. This is
   * critical for immutability patterns and safe concurrent usage.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength method with array reallocation (lines
   * 116-117-118)
   *
   * This benchmark measures the performance of the array reallocation path: Line
   * 116: byte[] newBytes = new byte[newLength]; Line 117: System.arraycopy(bytes,
   * 0, newBytes, 0, this.length); Line 118: this.bytes = newBytes;
   *
   * This path is executed when the new length exceeds the current byte array
   * capacity, requiring allocation of a larger array and copying existing data.
   * This is common in incremental string building scenarios.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithReallocation(BenchmarkState state) {
    // Ensure we trigger reallocation by requesting a length larger than current
    // capacity
    int targetLength = state.stringLength + state.newByteLength;
    return state.targetUtf8.setByteLength(targetLength);
  }

  /**
   * Benchmark for setByteLength method without array reallocation
   *
   * This benchmark measures the performance when the byte array is already large
   * enough and no reallocation is needed. This represents the fast path of the
   * method where only the length field is updated.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithoutReallocation(BenchmarkState state) {
    // Set to a length within current capacity (no reallocation)
    int targetLength = Math.max(1, state.stringLength / 2);
    return state.targetUtf8.setByteLength(targetLength);
  }

  /**
   * Main method to run the benchmark
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_5.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
