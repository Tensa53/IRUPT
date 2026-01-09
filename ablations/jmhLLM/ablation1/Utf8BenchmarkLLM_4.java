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
 * constructor), 116-117-118 (setByteLength method)
 *
 * Benchmarks: 1. benchmarkUtf8CopyConstructor - Tests the copy constructor
 * Utf8(Utf8 other) which allocates a new byte array and copies content using
 * System.arraycopy (lines 64-65)
 *
 * 2. benchmarkSetByteLengthGrow - Tests setByteLength() when it needs to grow
 * the internal byte array, triggering allocation and System.arraycopy (lines
 * 116-117-118)
 *
 * 3. benchmarkSetByteLengthShrink - Tests setByteLength() when it reduces the
 * length without reallocating the byte array (line 120)
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
@State(Scope.Thread)
public class Utf8BenchmarkLLM_4 {

  @State(Scope.Thread)
  public static class BenchmarkState {

    @Param({ "10", "100", "1000", "10000" })
    private int stringSize;

    private Utf8 sourceUtf8Small;
    private Utf8 sourceUtf8Medium;
    private Utf8 sourceUtf8Large;

    private Utf8 targetUtf8ForGrow;
    private Utf8 targetUtf8ForShrink;

    private int smallSize;
    private int mediumSize;
    private int largeSize;

    @Setup(Level.Trial)
    public void setup() {
      smallSize = 10;
      mediumSize = 100;
      largeSize = stringSize;

      String smallString = generateString(smallSize);
      String mediumString = generateString(mediumSize);
      String largeString = generateString(largeSize);

      sourceUtf8Small = new Utf8(smallString);
      sourceUtf8Medium = new Utf8(mediumString);
      sourceUtf8Large = new Utf8(largeString);

      targetUtf8ForGrow = new Utf8(smallString);
      targetUtf8ForShrink = new Utf8(largeString);
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
   * Benchmark for Utf8 copy constructor with small strings (10 bytes) Tests lines
   * 64-65: byte array allocation and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorSmall(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Small);
  }

  /**
   * Benchmark for Utf8 copy constructor with medium strings (100 bytes) Tests
   * lines 64-65: byte array allocation and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorMedium(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Medium);
  }

  /**
   * Benchmark for Utf8 copy constructor with large strings (parameterized size)
   * Tests lines 64-65: byte array allocation and System.arraycopy
   */
  @Benchmark
  public Utf8 benchmarkUtf8CopyConstructorLarge(BenchmarkState state) {
    return new Utf8(state.sourceUtf8Large);
  }

  /**
   * Benchmark for setByteLength() when growing the byte array Tests lines
   * 116-117-118: new byte array allocation and System.arraycopy when newLength >
   * current bytes.length
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthGrow(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8Small);
    return utf8.setByteLength(state.largeSize);
  }

  /**
   * Benchmark for setByteLength() when shrinking (no reallocation) Tests line
   * 120: just updates length field without array reallocation
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthShrink(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8Large);
    return utf8.setByteLength(state.smallSize);
  }

  /**
   * Benchmark for setByteLength() when keeping same length (no reallocation)
   * Tests line 120: updates length field and clears cached string
   */
  @Benchmark
  public Utf8 benchmarkSetByteLengthSameSize(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8Medium);
    return utf8.setByteLength(state.mediumSize);
  }

  /**
   * Combined benchmark: copy constructor followed by setByteLength grow Tests the
   * interaction between lines 64-65 and 116-117-118
   */
  @Benchmark
  public Utf8 benchmarkCopyConstructorThenGrow(BenchmarkState state) {
    Utf8 utf8 = new Utf8(state.sourceUtf8Small);
    return utf8.setByteLength(state.largeSize);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Utf8BenchmarkLLM_4.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
