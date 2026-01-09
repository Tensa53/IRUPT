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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for org.apache.avro.util.Utf8 class.
 *
 * This benchmark targets specific performance-critical lines: - Lines 64-65:
 * Utf8(Utf8 other) copy constructor - byte array allocation and
 * System.arraycopy - Lines 116-118: setByteLength() - conditional array
 * reallocation with System.arraycopy
 *
 * JMH Version: 1.37
 *
 * Run with: mvn clean install java -jar target/benchmarks.jar Utf8BenchmarkLLM
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = { "-XX:+UseG1GC", "-Xms2g", "-Xmx2g" })
public class Utf8BenchmarkLLM_2 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    @Param({ "8", "64", "256", "1024", "4096" })
    public int byteLength;

    public Utf8 sourceUtf8;
    public Utf8 targetUtf8ForResize;
    public byte[] testBytes;

    @Setup
    public void setup() {
      testBytes = new byte[byteLength];
      for (int i = 0; i < byteLength; i++) {
        testBytes[i] = (byte) ('A' + (i % 26));
      }

      sourceUtf8 = new Utf8(testBytes);

      byte[] smallBytes = new byte[byteLength / 4];
      for (int i = 0; i < smallBytes.length; i++) {
        smallBytes[i] = (byte) ('a' + (i % 26));
      }
      targetUtf8ForResize = new Utf8(smallBytes);
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65).
   *
   * Measures: - Line 64: new byte[other.length] allocation - Line 65:
   * System.arraycopy(other.bytes, 0, this.bytes, 0, this.length)
   *
   * This tests the performance of creating a defensive copy of a Utf8 object,
   * which is critical for immutability and thread-safety patterns.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength with array reallocation (lines 116-118).
   *
   * Measures the performance when newLength > bytes.length, triggering: - Line
   * 116: byte[] newBytes = new byte[newLength] allocation - Line 117:
   * System.arraycopy(bytes, 0, newBytes, 0, this.length) - Line 118: this.bytes =
   * newBytes assignment
   *
   * This path is critical for buffer reuse patterns where Utf8 instances need to
   * grow to accommodate larger data.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLength(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.targetUtf8ForResize);
    return utf8.setByteLength(state.byteLength);
  }

  /**
   * Baseline benchmark for setByteLength without reallocation.
   *
   * Tests the fast path where newLength <= bytes.length, which skips lines
   * 116-118 and only updates the length field.
   *
   * Use this as a baseline to measure the overhead of the reallocation path.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoReallocation(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.testBytes);
    return utf8.setByteLength(state.byteLength / 2);
  }

  /**
   * Benchmark for setByteLength with exact same length.
   *
   * Tests the minimal overhead path where newLength equals current bytes.length.
   * This clears the cached string but doesn't trigger reallocation.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSameSize(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.testBytes);
    return utf8.setByteLength(state.byteLength);
  }

  /**
   * Benchmark for setByteLength with incremental growth.
   *
   * Simulates a realistic pattern where a Utf8 buffer grows incrementally,
   * triggering lines 116-118 multiple times with different sizes.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthIncrementalGrowth(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    utf8.setByteLength(state.byteLength / 8);
    utf8.setByteLength(state.byteLength / 4);
    utf8.setByteLength(state.byteLength / 2);
    return utf8.setByteLength(state.byteLength);
  }

  /**
   * Benchmark for setByteLength with doubling growth pattern.
   *
   * Tests the common buffer-doubling strategy, measuring the performance of
   * repeated reallocations with exponentially increasing sizes.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthDoublingGrowth(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    int size = 8;
    while (size < state.byteLength) {
      utf8.setByteLength(size);
      size *= 2;
    }
    return utf8.setByteLength(state.byteLength);
  }

  /**
   * Benchmark for copy constructor followed by setByteLength.
   *
   * Measures the combined overhead of both operations (lines 64-65 and 116-118),
   * simulating a pattern where a Utf8 is copied and then resized.
   */
  @Benchmark
  public Utf8 benchmarkCopyThenResize(BenchmarkState state) {
    Utf8 copy = new Utf8(state.targetUtf8ForResize);
    return copy.setByteLength(state.byteLength);
  }

  /**
   * Benchmark for setByteLength with maximum reallocation overhead.
   *
   * Creates a minimal Utf8 and immediately grows it to the target size,
   * maximizing the cost of System.arraycopy on lines 116-118.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthMaxReallocation(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    return utf8.setByteLength(state.byteLength);
  }

  /**
   * Benchmark for repeated copy constructor calls.
   *
   * Tests the sustained performance of lines 64-65 under repeated allocation and
   * copying, measuring GC pressure and allocation overhead.
   */
  @Benchmark
  public Utf8 benchmarkRepeatedCopyConstructor(BenchmarkState state) {
    Utf8 result = state.sourceUtf8;
    for (int i = 0; i < 10; i++) {
      result = new Utf8(result);
    }
    return result;
  }
}
