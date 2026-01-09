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
 * JMH Benchmark for Utf8 class focusing on lines 64-65, 116-117-118.
 *
 * Lines 64-65: Copy constructor - byte array allocation and System.arraycopy
 * Lines 116-117-118: setByteLength method - byte array reallocation and
 * System.arraycopy
 *
 * This benchmark measures the performance of: 1. Copy constructor: new
 * Utf8(Utf8) - tests byte array cloning performance 2. setByteLength method:
 * tests dynamic byte array resizing with data copy
 *
 * JMH Version: 1.37
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2, jvmArgsAppend = { "-XX:+UseG1GC", "-Xms2g", "-Xmx2g" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_3 {

  /**
   * State class containing all benchmark data and initialization. Using Thread
   * scope to ensure each thread has its own state.
   */
  @State(Scope.Thread)
  public static class BenchmarkState {

    // Test data with various sizes to simulate realistic scenarios
    @Param({ "10", "50", "100", "500", "1000" })
    public int byteLength;

    // Source Utf8 instance for copy constructor benchmark
    public Utf8 sourceUtf8;

    // Utf8 instance for setByteLength benchmark
    public Utf8 targetUtf8;

    // New length values for setByteLength benchmark (testing growth scenarios)
    public int newLengthSmaller;
    public int newLengthEqual;
    public int newLengthLarger;

    @Setup(Level.Trial)
    public void setup() {
      // Initialize source Utf8 with parameterized byte length
      byte[] sourceBytes = new byte[byteLength];
      for (int i = 0; i < byteLength; i++) {
        sourceBytes[i] = (byte) ('A' + (i % 26)); // Fill with ASCII characters
      }
      sourceUtf8 = new Utf8(sourceBytes);

      // Initialize target Utf8 for setByteLength tests
      byte[] targetBytes = new byte[byteLength];
      System.arraycopy(sourceBytes, 0, targetBytes, 0, byteLength);
      targetUtf8 = new Utf8(targetBytes);

      // Calculate new length scenarios
      newLengthSmaller = Math.max(1, byteLength / 2);
      newLengthEqual = byteLength;
      newLengthLarger = byteLength * 2;
    }

    @Setup(Level.Invocation)
    public void resetTargetUtf8() {
      // Reset targetUtf8 for setByteLength benchmarks to ensure consistent starting
      // state
      byte[] resetBytes = new byte[byteLength];
      for (int i = 0; i < byteLength; i++) {
        resetBytes[i] = (byte) ('A' + (i % 26));
      }
      targetUtf8 = new Utf8(resetBytes);
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests: new
   * byte[other.length] and System.arraycopy(other.bytes, 0, this.bytes, 0,
   * this.length)
   *
   * This benchmark measures the cost of creating a new Utf8 instance from an
   * existing one, which involves allocating a new byte array and copying the
   * data.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength method with smaller length (lines 116-117-118).
   * Tests scenario where newLength < current byte array length. No reallocation
   * occurs, only length field update.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSmaller(BenchmarkState state, Blackhole bh) {
    Utf8 result = state.targetUtf8.setByteLength(state.newLengthSmaller);
    bh.consume(state.targetUtf8.getBytes()); // Prevent dead code elimination
    return result;
  }

  /**
   * Benchmark for setByteLength method with equal length (lines 116-117-118).
   * Tests scenario where newLength == current byte array length. No reallocation
   * occurs, only length field update.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthEqual(BenchmarkState state, Blackhole bh) {
    Utf8 result = state.targetUtf8.setByteLength(state.newLengthEqual);
    bh.consume(state.targetUtf8.getBytes()); // Prevent dead code elimination
    return result;
  }

  /**
   * Benchmark for setByteLength method with larger length (lines 116-117-118).
   * Tests scenario where newLength > current byte array length. Triggers
   * reallocation: new byte[newLength] and System.arraycopy(bytes, 0, newBytes, 0,
   * this.length)
   *
   * This is the most expensive scenario as it involves: 1. Allocating a new
   * larger byte array 2. Copying existing data to the new array 3. Updating the
   * reference
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthLarger(BenchmarkState state, Blackhole bh) {
    Utf8 result = state.targetUtf8.setByteLength(state.newLengthLarger);
    bh.consume(state.targetUtf8.getBytes()); // Prevent dead code elimination
    return result;
  }

  /**
   * Baseline benchmark: measures the overhead of state access and method call.
   * Use this to understand the pure overhead vs actual operation cost.
   */
  @Benchmark
  public int benchmarkBaseline(BenchmarkState state) {
    return state.byteLength;
  }
}
