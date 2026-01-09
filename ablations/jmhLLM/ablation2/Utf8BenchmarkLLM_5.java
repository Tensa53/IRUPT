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
 * with byte array allocation and System.arraycopy - Lines 116-117-118:
 * setByteLength method with conditional array expansion
 *
 * This benchmark measures the performance characteristics of these critical
 * operations in isolation to understand their impact on Avro
 * serialization/deserialization performance.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = { "-XX:+UseG1GC", "-Xms2g", "-Xmx2g" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_5 {

  /**
   * State class containing all test data and dependencies for benchmarks. Uses
   * Thread scope to ensure each thread gets its own instance.
   */
  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data with varying sizes to test different scenarios
    @Param({ "10", "100", "1000", "10000" })
    public int stringSize;

    // Source Utf8 objects for copy constructor testing
    public Utf8 sourceUtf8Small;
    public Utf8 sourceUtf8Medium;
    public Utf8 sourceUtf8Large;
    public Utf8 sourceUtf8Current;

    // Utf8 objects for setByteLength testing
    public Utf8 targetUtf8ForExpansion;
    public Utf8 targetUtf8NoExpansion;

    // Byte arrays for testing
    public byte[] testBytes;

    @Setup(Level.Trial)
    public void setupTrial() {
      // Create test strings of varying sizes
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < stringSize; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      String testString = sb.toString();

      // Initialize source Utf8 objects for copy constructor benchmarks
      sourceUtf8Small = new Utf8("Hello");
      sourceUtf8Medium = new Utf8(
          "The quick brown fox jumps over the lazy dog. This is a medium-sized string for testing.");
      sourceUtf8Large = new Utf8(testString);
      sourceUtf8Current = new Utf8(testString);

      // Initialize test bytes
      testBytes = testString.getBytes(java.nio.charset.StandardCharsets.UTF_8);

      // For setByteLength: create Utf8 with small initial capacity
      // This will force array expansion in the benchmark
      targetUtf8ForExpansion = new Utf8("small");

      // For setByteLength: create Utf8 with sufficient capacity
      // This will NOT trigger array expansion
      targetUtf8NoExpansion = new Utf8(testString);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reset state before each invocation to ensure consistent testing
      // For setByteLength benchmarks, we need fresh objects
      targetUtf8ForExpansion = new Utf8("small");
      targetUtf8NoExpansion = new Utf8(new String(testBytes, java.nio.charset.StandardCharsets.UTF_8));
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests: new
   * byte[other.length] allocation + System.arraycopy
   *
   * This measures the cost of creating a defensive copy of the internal byte
   * array, which is critical for immutability and thread-safety scenarios.
   */
  @Benchmark
  public void benchmarkUtf8CopyConstructor(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Current);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor with small strings (lines 64-65). Tests
   * the overhead of copy constructor with minimal data (5 bytes).
   */
  @Benchmark
  public void benchmarkUtf8CopyConstructorSmall(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Small);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium strings (lines 64-65). Tests
   * the overhead with realistic string sizes (~90 bytes).
   */
  @Benchmark
  public void benchmarkUtf8CopyConstructorMedium(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Medium);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for Utf8 copy constructor with large strings (lines 64-65). Tests
   * the impact of System.arraycopy with large arrays.
   */
  @Benchmark
  public void benchmarkUtf8CopyConstructorLarge(BenchmarkState state, Blackhole blackhole) {
    Utf8 copy = new Utf8(state.sourceUtf8Large);
    blackhole.consume(copy);
  }

  /**
   * Benchmark for setByteLength with array expansion (lines 116-117-118). Tests
   * the worst-case scenario where the internal byte array must be reallocated and
   * existing content copied to the new array.
   *
   * This path is taken when: this.bytes.length < newLength
   */
  @Benchmark
  public void benchmarkSetByteLengthWithExpansion(BenchmarkState state, Blackhole blackhole) {
    // This will trigger lines 116-118 because targetUtf8ForExpansion has small
    // capacity
    Utf8 result = state.targetUtf8ForExpansion.setByteLength(state.stringSize);
    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength without array expansion (line 120). Tests the
   * best-case scenario where the internal byte array is already large enough and
   * only the length field needs to be updated.
   *
   * This path is taken when: this.bytes.length >= newLength
   */
  @Benchmark
  public void benchmarkSetByteLengthNoExpansion(BenchmarkState state, Blackhole blackhole) {
    // This will NOT trigger expansion because targetUtf8NoExpansion has sufficient
    // capacity
    Utf8 result = state.targetUtf8NoExpansion.setByteLength(state.stringSize);
    blackhole.consume(result);
  }

  /**
   * Benchmark for setByteLength with progressive expansions. Tests multiple
   * sequential expansions to simulate real-world usage where a Utf8 object is
   * reused and grows over time.
   */
  @Benchmark
  public void benchmarkSetByteLengthProgressiveExpansion(BenchmarkState state, Blackhole blackhole) {
    Utf8 utf8 = new Utf8("start");

    // Simulate progressive growth with multiple expansions
    utf8.setByteLength(50);
    utf8.setByteLength(100);
    utf8.setByteLength(500);
    utf8.setByteLength(state.stringSize);

    blackhole.consume(utf8);
  }

  /**
   * Benchmark comparing the cost of copy constructor vs setByteLength expansion.
   * This helps understand the relative cost of these two allocation patterns.
   */
  @Benchmark
  public void benchmarkComparisonCopyVsExpansion(BenchmarkState state, Blackhole blackhole) {
    // Path 1: Copy constructor (lines 64-65)
    Utf8 copy = new Utf8(state.sourceUtf8Current);

    // Path 2: setByteLength with expansion (lines 116-117-118)
    Utf8 expanded = new Utf8("small");
    expanded.setByteLength(state.stringSize);

    blackhole.consume(copy);
    blackhole.consume(expanded);
  }
}
