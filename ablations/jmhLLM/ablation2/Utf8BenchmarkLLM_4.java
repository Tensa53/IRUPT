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
 * JMH Benchmark for Utf8 class focusing on: - Lines 64-65: Copy constructor
 * with System.arraycopy - Lines 116-117-118: setByteLength method with array
 * reallocation
 *
 * This benchmark tests the performance characteristics of these critical
 * operations under various data sizes.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2, jvmArgsAppend = { "-XX:+UseG1GC", "-Xms2g", "-Xmx2g" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_4 {

  /**
   * State class holding benchmark parameters and test data. Uses Thread scope to
   * ensure thread-safety in multi-threaded benchmark scenarios.
   */
  @State(Scope.Thread)
  public static class BenchmarkState {

    @Param({ "10", "100", "1000", "10000" })
    public int stringLength;

    @Param({ "50", "200", "2000", "20000" })
    public int newByteLength;

    public Utf8 sourceUtf8;
    public Utf8 targetUtf8ForSetByteLength;
    private String testString;

    @Setup
    public void setup() {
      StringBuilder sb = new StringBuilder(stringLength);
      for (int i = 0; i < stringLength; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      testString = sb.toString();

      sourceUtf8 = new Utf8(testString);

      targetUtf8ForSetByteLength = new Utf8(testString);
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65).
   *
   * Tests the performance of: - this.bytes = new byte[other.length]; (line 64) -
   * System.arraycopy(other.bytes, 0, this.bytes, 0, this.length); (line 65)
   *
   * This measures the cost of creating a new Utf8 instance by copying from an
   * existing one, which involves byte array allocation and native array copying.
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength method (lines 116-117-118).
   *
   * Tests the performance of array reallocation when new length exceeds capacity:
   * - byte[] newBytes = new byte[newLength]; (line 116) - System.arraycopy(bytes,
   * 0, newBytes, 0, this.length); (line 117) - this.bytes = newBytes; (line 118)
   *
   * This measures the cost of growing the internal byte array, which is a
   * critical operation during deserialization and data manipulation.
   *
   * Note: This benchmark forces reallocation by always requesting a length larger
   * than the current capacity.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLength(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8);
    return utf8.setByteLength(state.newByteLength);
  }

  /**
   * Benchmark for setByteLength with exact same length (edge case).
   *
   * This tests the performance when setByteLength is called but no array
   * reallocation is needed. This path skips lines 116-118 and only executes the
   * length update and string cache invalidation.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoReallocation(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8);
    return utf8.setByteLength(state.sourceUtf8.getByteLength());
  }

  /**
   * Benchmark combining both operations: copy constructor followed by
   * setByteLength.
   *
   * This represents a common pattern in Avro deserialization where a Utf8
   * instance is reused by copying and then resizing for new data.
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorThenSetByteLength(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8);
    return utf8.setByteLength(state.newByteLength);
  }
}
