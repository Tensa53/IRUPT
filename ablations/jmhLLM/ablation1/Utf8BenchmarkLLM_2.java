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
 * constructor), 116-117-118 (setByteLength)
 *
 * This benchmark measures the performance of: 1. Utf8 copy constructor (lines
 * 64-65) - creates new byte array and copies data 2. setByteLength method
 * (lines 116-117-118) - resizes internal byte array when needed
 *
 * JMH Version: 1.37
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_2 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    // Parameters for different string sizes to test various scenarios
    @Param({ "10", "100", "1000", "10000" })
    private int stringSize;

    // Source Utf8 instance for copy constructor benchmark
    private Utf8 sourceUtf8;

    // Target Utf8 instance for setByteLength benchmark
    private Utf8 targetUtf8;

    // Pre-computed string data
    private String testString;

    @Setup(Level.Trial)
    public void setup() {
      // Create a test string of specified size
      StringBuilder sb = new StringBuilder(stringSize);
      for (int i = 0; i < stringSize; i++) {
        sb.append((char) ('A' + (i % 26)));
      }
      testString = sb.toString();

      // Initialize source Utf8 for copy constructor benchmarks
      sourceUtf8 = new Utf8(testString);

      // Initialize target Utf8 with initial capacity for setByteLength benchmarks
      targetUtf8 = new Utf8(testString);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reset targetUtf8 for setByteLength benchmarks to ensure consistent state
      targetUtf8 = new Utf8("initial");
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65)
   *
   * Tests the performance of creating a new Utf8 instance from an existing one.
   * This involves: - Creating a new byte array of the same length - Copying all
   * bytes using System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength with array expansion (lines 116-117-118)
   *
   * Tests the performance of setByteLength when the internal byte array needs to
   * be expanded. This involves: - Creating a new larger byte array - Copying
   * existing data using System.arraycopy - Setting the new length - Clearing the
   * cached string
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthExpand(BenchmarkState state) {
    // Force array expansion by requesting a size larger than current capacity
    int newLength = state.stringSize * 2;
    return state.targetUtf8.setByteLength(newLength);
  }

  /**
   * Benchmark for setByteLength without array expansion (lines 116-117-118)
   *
   * Tests the performance of setByteLength when the internal byte array is large
   * enough and doesn't need expansion. This only involves: - Setting the new
   * length - Clearing the cached string (No array creation or copying)
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoExpand(BenchmarkState state) {
    // Request a size smaller than or equal to current capacity
    int newLength = state.stringSize / 2;
    return state.targetUtf8.setByteLength(newLength);
  }

  /**
   * Benchmark for setByteLength with incremental growth (lines 116-117-118)
   *
   * Tests the performance of setByteLength with small incremental increases,
   * simulating a common usage pattern where data is read in chunks.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthIncremental(BenchmarkState state) {
    // Increment by 10% of current size
    int newLength = state.stringSize + (state.stringSize / 10);
    return state.targetUtf8.setByteLength(newLength);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_2.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
