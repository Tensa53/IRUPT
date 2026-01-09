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

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Utf8 class focusing on lines 64-65 and 116-117-118.
 *
 * This benchmark tests: - Line 64: byte array allocation in copy constructor -
 * Line 65: System.arraycopy operation in copy constructor - Lines 116-118: byte
 * array reallocation and copy in setByteLength method
 *
 * JMH Version: 1.37
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_4 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    @Param({ "10", "100", "1000", "10000" })
    public int stringSize;

    @Param({ "50", "150", "1500", "15000" })
    public int newByteLength;

    public Utf8 sourceUtf8Small;
    public Utf8 sourceUtf8Medium;
    public Utf8 sourceUtf8Large;

    public Utf8 targetUtf8ForSetByteLength;

    @Setup(Level.Trial)
    public void setup() {
      // Initialize source Utf8 objects with different sizes for copy constructor
      // benchmarks
      sourceUtf8Small = new Utf8(generateString(10));
      sourceUtf8Medium = new Utf8(generateString(100));
      sourceUtf8Large = new Utf8(generateString(1000));

      // Initialize target for setByteLength benchmark
      targetUtf8ForSetByteLength = new Utf8(generateString(stringSize));
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      // Reinitialize target for setByteLength to ensure consistent state
      targetUtf8ForSetByteLength = new Utf8(generateString(stringSize));
    }

    private String generateString(int size) {
      StringBuilder sb = new StringBuilder(size);
      for (int i = 0; i < size; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      return sb.toString();
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65). Tests: new
   * byte[other.length] allocation and System.arraycopy operation.
   *
   * This benchmark focuses on the performance of: - Line 64: this.bytes = new
   * byte[other.length]; - Line 65: System.arraycopy(other.bytes, 0, this.bytes,
   * 0, this.length);
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium-sized strings (lines 64-65).
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorMedium(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor with large strings (lines 64-65).
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for setByteLength method when reallocation is needed (lines
   * 116-117-118). Tests the path where this.bytes.length < newLength, triggering:
   * - Line 116: byte[] newBytes = new byte[newLength]; - Line 117:
   * System.arraycopy(bytes, 0, newBytes, 0, this.length); - Line 118: this.bytes
   * = newBytes;
   *
   * This benchmark uses newByteLength > stringSize to force reallocation.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithReallocation(BenchmarkState state) {
    return state.targetUtf8ForSetByteLength.setByteLength(state.newByteLength);
  }

  /**
   * Benchmark for setByteLength method when NO reallocation is needed. Tests the
   * path where this.bytes.length >= newLength, avoiding lines 116-118.
   *
   * This provides a baseline comparison for the reallocation path.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithoutReallocation(BenchmarkState state) {
    // Use a smaller length that doesn't trigger reallocation
    int smallerLength = Math.max(1, state.stringSize / 2);
    return state.targetUtf8ForSetByteLength.setByteLength(smallerLength);
  }

  /**
   * Benchmark for setByteLength with same length (edge case). Tests when
   * newLength equals current length - no reallocation but string cache is
   * cleared.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSameLength(BenchmarkState state) {
    return state.targetUtf8ForSetByteLength.setByteLength(state.stringSize);
  }

  /**
   * Combined benchmark: Create via copy constructor then call setByteLength.
   * Tests the interaction between lines 64-65 and 116-117-118.
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorThenSetByteLength(BenchmarkState state) {
    Utf8 copy = new Utf8(state.sourceUtf8Medium);
    return copy.setByteLength(state.newByteLength);
  }
}
