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
 * Benchmarks the following critical operations: - Lines 64-65: Copy constructor
 * with System.arraycopy - Lines 116-117-118: setByteLength with array expansion
 * and System.arraycopy
 *
 * This benchmark measures the performance characteristics of these operations
 * with various string sizes and growth patterns.
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

    @Param({ "10", "100", "1000", "10000" })
    private int stringSize;

    @Param({ "50", "200", "2000" })
    private int newByteLength;

    private Utf8 sourceUtf8;
    private Utf8 targetUtf8ForSetByteLength;

    @Setup(Level.Trial)
    public void setup() {
      StringBuilder sb = new StringBuilder(stringSize);
      for (int i = 0; i < stringSize; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      String testString = sb.toString();

      sourceUtf8 = new Utf8(testString);
      targetUtf8ForSetByteLength = new Utf8(testString);
    }

    @Setup(Level.Invocation)
    public void resetForSetByteLength() {
      StringBuilder sb = new StringBuilder(stringSize);
      for (int i = 0; i < stringSize; i++) {
        sb.append((char) ('a' + (i % 26)));
      }
      targetUtf8ForSetByteLength = new Utf8(sb.toString());
    }
  }

  /**
   * Benchmark for Utf8 copy constructor (lines 64-65).
   *
   * This measures the performance of creating a new Utf8 instance from an
   * existing one, which involves allocating a new byte array and performing
   * System.arraycopy.
   *
   * Critical path: - new byte[other.length] allocation -
   * System.arraycopy(other.bytes, 0, this.bytes, 0, this.length)
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark for setByteLength method with array expansion (lines 116-117-118).
   *
   * This measures the performance of expanding the internal byte array when the
   * new length exceeds the current array capacity. This is the expensive path
   * that involves: - new byte[newLength] allocation - System.arraycopy(bytes, 0,
   * newBytes, 0, this.length)
   *
   * The benchmark uses newByteLength > stringSize to force array expansion.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithExpansion(BenchmarkState state) {
    if (state.newByteLength > state.stringSize) {
      return state.targetUtf8ForSetByteLength.setByteLength(state.newByteLength);
    }
    return state.targetUtf8ForSetByteLength;
  }

  /**
   * Benchmark for setByteLength method without array expansion.
   *
   * This measures the performance when the new length is within the current array
   * capacity, avoiding the expensive allocation and copy operations. This
   * represents the fast path through the method.
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthWithoutExpansion(BenchmarkState state) {
    int smallerLength = Math.max(1, state.stringSize / 2);
    return state.targetUtf8ForSetByteLength.setByteLength(smallerLength);
  }

  /**
   * Benchmark that measures the complete reuse pattern: setByteLength followed by
   * repeated modifications.
   *
   * This simulates the typical usage pattern where Utf8 instances are reused for
   * efficiency, requiring periodic array expansions.
   */
  @Benchmark
  public Utf8 benchmarkReusePattern(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8);
    if (state.newByteLength > state.stringSize) {
      utf8.setByteLength(state.newByteLength);
    }
    utf8.setByteLength(state.stringSize);
    return utf8;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_2.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
