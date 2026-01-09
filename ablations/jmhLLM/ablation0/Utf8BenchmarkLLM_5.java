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
 * JMH Benchmark for Utf8 class.
 *
 * Benchmarks the following critical code paths: - Lines 64-65: Copy constructor
 * with byte array allocation and System.arraycopy - Lines 116-117-118:
 * setByteLength method with conditional array reallocation
 *
 * This benchmark measures the performance characteristics of these operations
 * under various data size scenarios to identify potential bottlenecks.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_5 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    @Param({ "10", "100", "1000", "10000" })
    private int stringLength;

    private Utf8 sourceUtf8Small;
    private Utf8 sourceUtf8Medium;
    private Utf8 sourceUtf8Large;

    private Utf8 targetUtf8ForSetByteLength;
    private Utf8 targetUtf8ForSetByteLengthGrow;
    private Utf8 targetUtf8ForSetByteLengthShrink;

    private String testString;

    @Setup(Level.Trial)
    public void setup() {
      testString = generateString(stringLength);

      sourceUtf8Small = new Utf8(generateString(10));
      sourceUtf8Medium = new Utf8(generateString(100));
      sourceUtf8Large = new Utf8(generateString(1000));

      targetUtf8ForSetByteLength = new Utf8(testString);

      targetUtf8ForSetByteLengthGrow = new Utf8(generateString(10));

      targetUtf8ForSetByteLengthShrink = new Utf8(generateString(1000));
    }

    @Setup(Level.Invocation)
    public void resetTargets() {
      targetUtf8ForSetByteLength.set(testString);
      targetUtf8ForSetByteLengthGrow.set(generateString(10));
      targetUtf8ForSetByteLengthShrink.set(generateString(1000));
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
   * Benchmark for Utf8 copy constructor (lines 64-65).
   *
   * Tests the performance of: - this.bytes = new byte[other.length]; -
   * System.arraycopy(other.bytes, 0, this.bytes, 0, this.length);
   *
   * This measures the cost of array allocation and memory copy operations for
   * small UTF-8 strings (10 bytes).
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65).
   *
   * This measures the cost of array allocation and memory copy operations for
   * medium UTF-8 strings (100 bytes).
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorMedium(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65).
   *
   * This measures the cost of array allocation and memory copy operations for
   * large UTF-8 strings (1000 bytes).
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for setByteLength method (lines 116-117-118).
   *
   * Tests the performance when the new length equals current capacity (no
   * reallocation needed): - if (this.bytes.length < newLength) - FALSE branch
   *
   * This is the fast path where no array reallocation occurs.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthNoReallocation(BenchmarkState state) {
    return state.targetUtf8ForSetByteLength.setByteLength(state.stringLength);
  }

  /**
   * Benchmark for setByteLength method (lines 116-117-118).
   *
   * Tests the performance when growing the buffer (reallocation needed): - byte[]
   * newBytes = new byte[newLength]; - System.arraycopy(bytes, 0, newBytes, 0,
   * this.length); - this.bytes = newBytes;
   *
   * This is the slow path where array reallocation and copy occur.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithGrowth(BenchmarkState state) {
    return state.targetUtf8ForSetByteLengthGrow.setByteLength(state.stringLength);
  }

  /**
   * Benchmark for setByteLength method (lines 116-117-118).
   *
   * Tests the performance when shrinking the buffer (no reallocation): - if
   * (this.bytes.length < newLength) - FALSE branch
   *
   * This tests the fast path when reducing the logical length but keeping the
   * same underlying byte array.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithShrink(BenchmarkState state) {
    int newLength = Math.max(1, state.stringLength / 2);
    return state.targetUtf8ForSetByteLengthShrink.setByteLength(newLength);
  }

  /**
   * Main method to run the benchmark.
   *
   * Usage: java -jar target/avro-1.10.0-SNAPSHOT.jar Utf8BenchmarkLLM
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_5.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
