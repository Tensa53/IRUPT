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
 * JMH Benchmark for Utf8 Generated to benchmark lines 64-65, 116-117-118
 *
 * Target Methods: - Utf8(Utf8 other) copy constructor (lines 64-65) -
 * setByteLength(int newLength) (lines 116-117-118)
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

    @Param({ "16", "64", "256", "1024", "4096" })
    private int byteArraySize;

    private Utf8 sourceUtf8;
    private Utf8 targetUtf8Small;
    private Utf8 targetUtf8Medium;
    private Utf8 targetUtf8Large;

    private int expansionSize;
    private int reductionSize;

    @Setup(Level.Trial)
    public void setup() {
      StringBuilder sb = new StringBuilder(byteArraySize);
      for (int i = 0; i < byteArraySize; i++) {
        sb.append((char) ('A' + (i % 26)));
      }
      String testString = sb.toString();

      sourceUtf8 = new Utf8(testString);

      expansionSize = byteArraySize * 2;
      reductionSize = Math.max(1, byteArraySize / 2);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
      StringBuilder sb = new StringBuilder(byteArraySize);
      for (int i = 0; i < byteArraySize; i++) {
        sb.append((char) ('A' + (i % 26)));
      }
      String testString = sb.toString();

      targetUtf8Small = new Utf8(testString.substring(0, Math.min(8, byteArraySize)));
      targetUtf8Medium = new Utf8(testString);
      targetUtf8Large = new Utf8(testString + testString);
    }
  }

  /**
   * Benchmark: Utf8(Utf8 other) copy constructor Lines 64-65: this.bytes = new
   * byte[other.length]; System.arraycopy(other.bytes, 0, this.bytes, 0,
   * this.length);
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructor(BenchmarkState state) {
    return new Utf8(state.sourceUtf8);
  }

  /**
   * Benchmark: setByteLength with array expansion Lines 116-117-118 (when
   * this.bytes.length < newLength): byte[] newBytes = new byte[newLength];
   * System.arraycopy(bytes, 0, newBytes, 0, this.length); this.bytes = newBytes;
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthExpansion(BenchmarkState state) {
    return state.targetUtf8Small.setByteLength(state.expansionSize);
  }

  /**
   * Benchmark: setByteLength with array reduction (no reallocation) Line 120:
   * this.length = newLength; Tests fast path where existing array capacity is
   * sufficient
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthReduction(BenchmarkState state) {
    return state.targetUtf8Large.setByteLength(state.reductionSize);
  }

  /**
   * Benchmark: setByteLength with same length Line 120-121: this.length =
   * newLength; this.string = null; Tests minimal overhead when length doesn't
   * change
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSameSize(BenchmarkState state) {
    return state.targetUtf8Medium.setByteLength(state.byteArraySize);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_3.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
