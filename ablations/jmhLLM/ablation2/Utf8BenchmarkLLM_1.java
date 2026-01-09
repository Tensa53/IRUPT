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
 * JMH Benchmark for Utf8 class performance analysis.
 *
 * Focuses on: - Lines 64-65: Copy constructor with array allocation and
 * System.arraycopy - Lines 116-118: setByteLength method with array allocation
 * and System.arraycopy
 *
 * Run with: java -jar target/avro-1.10.0-SNAPSHOT.jar Utf8Benchmark
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = { "-XX:+UseG1GC", "-Xms2g", "-Xmx2g" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_1 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    @Param({ "10", "100", "1000", "10000" })
    public int stringLength;

    public Utf8 sourceUtf8Small;
    public Utf8 sourceUtf8Medium;
    public Utf8 sourceUtf8Large;
    public Utf8 targetUtf8;
    public String testString;

    @Setup
    public void setup() {
      // Create test strings of various sizes
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < stringLength; i++) {
        sb.append((char) ('A' + (i % 26)));
      }
      testString = sb.toString();

      // Initialize source Utf8 objects for copy constructor benchmark
      sourceUtf8Small = new Utf8(createString(10));
      sourceUtf8Medium = new Utf8(createString(100));
      sourceUtf8Large = new Utf8(createString(1000));

      // Initialize target Utf8 for setByteLength benchmark
      targetUtf8 = new Utf8(testString);
    }

    private String createString(int length) {
      StringBuilder sb = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      return sb.toString();
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests: new
   * byte[other.length] allocation and System.arraycopy performance.
   *
   * This benchmark measures the overhead of creating a new Utf8 instance from an
   * existing one, which involves byte array allocation and copying.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor with small strings (10 bytes). Focuses on
   * lines 64-65 with minimal array size.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor with large strings (1000 bytes). Focuses
   * on lines 64-65 with large array size to measure System.arraycopy scaling.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for setByteLength method with array reallocation (lines 116-118).
   * Tests the scenario where newLength > current bytes.length, triggering: - new
   * byte[newLength] allocation - System.arraycopy(bytes, 0, newBytes, 0,
   * this.length) - bytes array replacement
   *
   * This is a critical path for Utf8 reuse patterns where the buffer needs to
   * grow to accommodate larger strings.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLength(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.testString);
    // Force reallocation by setting a larger length
    return utf8.setByteLength(state.stringLength * 2);
  }

  /**
   * Benchmark for setByteLength without reallocation. Tests the fast path where
   * newLength <= bytes.length (lines 120-122). This serves as a baseline to
   * compare against the reallocation path.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoReallocation(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.testString);
    // No reallocation, just update length
    return utf8.setByteLength(state.stringLength / 2);
  }

  /**
   * Benchmark for setByteLength with progressive growth. Simulates a realistic
   * scenario where Utf8 instances are reused and gradually grow, triggering
   * multiple reallocations.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthProgressiveGrowth(BenchmarkState state) {
    Utf8 utf8 = new Utf8();
    // Simulate progressive growth pattern
    utf8.setByteLength(state.stringLength / 4);
    utf8.setByteLength(state.stringLength / 2);
    utf8.setByteLength(state.stringLength);
    return utf8.setByteLength(state.stringLength * 2);
  }

  /**
   * Combined benchmark: Copy constructor followed by setByteLength. Measures the
   * interaction between both operations in a realistic usage pattern.
   */
  @Benchmark
  public Utf8 benchmarkCombinedCopyAndResize(BenchmarkState state) {
    Utf8 copy = new Utf8(state.sourceUtf8Medium);
    return copy.setByteLength(state.stringLength);
  }
}
