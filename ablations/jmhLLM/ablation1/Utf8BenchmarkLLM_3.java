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
 * constructor), 116-117-118 (setByteLength array reallocation)
 *
 * Benchmarks focus on: 1. Copy constructor performance (lines 64-65): byte
 * array allocation and System.arraycopy 2. setByteLength performance (lines
 * 116-117-118): conditional array reallocation and System.arraycopy
 *
 * JMH Version: 1.37
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_3 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    // Different sizes to test various scenarios
    @Param({ "10", "100", "1000", "10000" })
    private int byteArraySize;

    // Source Utf8 instances for copy constructor benchmark
    private Utf8 sourceUtf8Small;
    private Utf8 sourceUtf8Medium;
    private Utf8 sourceUtf8Large;

    // Target Utf8 instances for setByteLength benchmark
    private Utf8 targetUtf8ForGrowth;
    private Utf8 targetUtf8ForShrink;
    private Utf8 targetUtf8NoRealloc;

    // Byte arrays for initialization
    private byte[] smallBytes;
    private byte[] mediumBytes;
    private byte[] largeBytes;

    @Setup(Level.Trial)
    public void setup() {
      // Initialize byte arrays with different sizes
      smallBytes = new byte[10];
      mediumBytes = new byte[100];
      largeBytes = new byte[byteArraySize];

      // Fill arrays with test data
      for (int i = 0; i < smallBytes.length; i++) {
        smallBytes[i] = (byte) ('A' + (i % 26));
      }
      for (int i = 0; i < mediumBytes.length; i++) {
        mediumBytes[i] = (byte) ('A' + (i % 26));
      }
      for (int i = 0; i < largeBytes.length; i++) {
        largeBytes[i] = (byte) ('A' + (i % 26));
      }

      // Initialize source Utf8 instances for copy constructor benchmarks
      sourceUtf8Small = new Utf8(smallBytes);
      sourceUtf8Medium = new Utf8(mediumBytes);
      sourceUtf8Large = new Utf8(largeBytes);

      // Initialize target Utf8 instances for setByteLength benchmarks
      targetUtf8ForGrowth = new Utf8(smallBytes);
      targetUtf8ForShrink = new Utf8(largeBytes);
      targetUtf8NoRealloc = new Utf8(mediumBytes);
    }

    @Setup(Level.Invocation)
    public void resetPerInvocation() {
      // Reset instances that get modified during benchmarks
      targetUtf8ForGrowth = new Utf8(smallBytes);
      targetUtf8ForShrink = new Utf8(largeBytes);
      targetUtf8NoRealloc = new Utf8(mediumBytes);
    }
  }

  /**
   * Benchmark for Utf8 copy constructor with small byte array (10 bytes) Tests
   * lines 64-65: new byte[] allocation and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium byte array (100 bytes) Tests
   * lines 64-65: new byte[] allocation and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorMedium(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor with large byte array (parameterized
   * size) Tests lines 64-65: new byte[] allocation and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for setByteLength when growing array (triggers reallocation) Tests
   * lines 116-117-118: new byte[] allocation, System.arraycopy, and array
   * assignment This scenario triggers the if condition (this.bytes.length <
   * newLength)
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthGrowArray(BenchmarkState state) {
    return state.targetUtf8ForGrowth.setByteLength(state.byteArraySize);
  }

  /**
   * Benchmark for setByteLength when shrinking array (no reallocation) Tests line
   * 120: only length assignment, no array reallocation This scenario skips the if
   * condition (this.bytes.length >= newLength)
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthShrinkArray(BenchmarkState state) {
    return state.targetUtf8ForShrink.setByteLength(10);
  }

  /**
   * Benchmark for setByteLength with same length (no reallocation) Tests line
   * 120-121: only length and string cache clearing This scenario also skips the
   * if condition
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoRealloc(BenchmarkState state) {
    return state.targetUtf8NoRealloc.setByteLength(100);
  }

  /**
   * Benchmark for setByteLength with incremental growth pattern Tests realistic
   * usage where byte length grows gradually Focuses on lines 116-117-118 with
   * various growth patterns
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthIncrementalGrowth(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    // Simulate incremental growth pattern
    utf8.setByteLength(10);
    utf8.setByteLength(50);
    utf8.setByteLength(100);
    return utf8;
  }

  /**
   * Main method to run benchmarks programmatically
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_3.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
