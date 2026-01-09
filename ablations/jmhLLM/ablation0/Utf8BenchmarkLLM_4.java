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
 * JMH Benchmark for Utf8 class Generated to benchmark lines 64-65 (copy
 * constructor), 116-117-118 (setByteLength method)
 *
 * Benchmark focuses on: 1. Copy constructor performance with System.arraycopy
 * (lines 64-65) 2. setByteLength method performance with array reallocation
 * (lines 116-117-118)
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_4 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data for various string sizes to cover different scenarios
    @Param({ "10", "100", "1000", "10000" })
    private int stringSize;

    // Source Utf8 instance for copy constructor benchmark
    private Utf8 sourceUtf8;

    // Utf8 instance for setByteLength benchmark
    private Utf8 targetUtf8;

    // New length for setByteLength benchmark (smaller than current)
    private int smallerLength;

    // New length for setByteLength benchmark (larger than current, triggers
    // reallocation)
    private int largerLength;

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

      // Initialize target Utf8 for setByteLength benchmark
      targetUtf8 = new Utf8(testString);

      // Calculate test lengths for setByteLength
      smallerLength = Math.max(1, stringSize / 2);
      largerLength = stringSize * 2;
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65) Tests performance of: new
   * byte[other.length] and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength with smaller length (lines 116-118) Tests
   * performance when NO array reallocation is needed Only updates length field
   * and clears string cache
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSmaller(BenchmarkState state) {
    // Reset to original size first
    state.targetUtf8.setByteLength(state.stringSize);
    // Now set to smaller length (no reallocation)
    return state.targetUtf8.setByteLength(state.smallerLength);
  }

  /**
   * Benchmark for setByteLength with larger length (lines 116-118) Tests
   * performance when array reallocation IS needed Includes: new byte[newLength]
   * and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthLarger(BenchmarkState state) {
    // Reset to original size first
    state.targetUtf8.setByteLength(state.stringSize);
    // Now set to larger length (triggers reallocation on lines 116-118)
    return state.targetUtf8.setByteLength(state.largerLength);
  }

  /**
   * Benchmark for setByteLength with same length Tests performance when length
   * doesn't change but string cache is cleared
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSame(BenchmarkState state) {
    return state.targetUtf8.setByteLength(state.stringSize);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_4.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
