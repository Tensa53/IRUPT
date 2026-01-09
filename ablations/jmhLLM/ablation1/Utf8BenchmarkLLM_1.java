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
 * This benchmark focuses on two specific operations: 1. Copy constructor
 * performance with System.arraycopy (lines 64-65) 2. setByteLength method with
 * byte array reallocation (lines 116-117-118)
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

    // Parameterized string lengths for different scenarios
    @Param({ "10", "100", "1000", "10000" })
    private int stringLength;

    // Source Utf8 instances for copy constructor benchmark
    private Utf8 sourceUtf8Small;
    private Utf8 sourceUtf8Medium;
    private Utf8 sourceUtf8Large;
    private Utf8 sourceUtf8VeryLarge;

    // Target Utf8 instances for setByteLength benchmark
    private Utf8 targetUtf8ForResize;
    private Utf8 targetUtf8WithCapacity;

    // Test strings for various scenarios
    private String testString;

    @Setup(Level.Trial)
    public void setup() {
      // Create test strings of various lengths
      testString = generateString(stringLength);
      String smallString = generateString(10);
      String mediumString = generateString(100);
      String largeString = generateString(1000);
      String veryLargeString = generateString(10000);

      // Initialize source Utf8 instances for copy constructor benchmarks
      sourceUtf8Small = new Utf8(smallString);
      sourceUtf8Medium = new Utf8(mediumString);
      sourceUtf8Large = new Utf8(largeString);
      sourceUtf8VeryLarge = new Utf8(veryLargeString);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reinitialize target instances before each invocation for setByteLength
      // benchmarks
      targetUtf8ForResize = new Utf8("initial");
      targetUtf8WithCapacity = new Utf8(generateString(stringLength / 2));
    }

    private String generateString(int length) {
      StringBuilder sb = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      return sb.toString();
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65) Tests the performance of
   * creating a new Utf8 instance from an existing one, including byte array
   * allocation and System.arraycopy operation.
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium-sized string (lines 64-65)
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorMedium(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor with large string (lines 64-65)
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for Utf8 copy constructor with very large string (lines 64-65)
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorVeryLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8VeryLarge);
  }

  /**
   * Benchmark for setByteLength method when resizing requires reallocation (lines
   * 116-117-118) This tests the scenario where the current byte array is too
   * small and needs to be expanded. Focuses on the byte array allocation and
   * System.arraycopy operations.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithReallocation(BenchmarkState state) {
    return state.targetUtf8ForResize.setByteLength(state.stringLength);
  }

  /**
   * Benchmark for setByteLength method when no reallocation is needed (lines
   * 116-117-118) This tests the scenario where the current byte array has
   * sufficient capacity, so only the length field is updated.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithoutReallocation(BenchmarkState state) {
    int newLength = Math.max(1, state.stringLength / 4);
    return state.targetUtf8WithCapacity.setByteLength(newLength);
  }

  /**
   * Benchmark for setByteLength method with incremental growth (lines
   * 116-117-118) This simulates a common usage pattern where the Utf8 instance is
   * gradually expanded.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthIncremental(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    // Incrementally grow the buffer
    for (int i = 10; i <= state.stringLength && i <= 100; i += 10) {
      utf8.setByteLength(i);
    }
    return utf8;
  }

  /**
   * Benchmark for combined operations: copy constructor followed by setByteLength
   * This tests a realistic scenario where a Utf8 instance is copied and then
   * modified.
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorAndSetByteLength(BenchmarkState state) {
    Utf8 copy = new Utf8(state.sourceUtf8Medium);
    return copy.setByteLength(state.stringLength);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_1.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
