#!/bin/bash

PRODUCTION_CLASS_NAME="${1:-MyProductionClass}"
PACKAGE_PATH="${2:-com.example.myapp}"
LINES="${3:-10-11}"
END_LINE=""
METHOD_NAMES="${4:-all}"
STATE_SCOPE="Thread"
PROMPT_CHOICE="${5:-no_ablation}"

# Java Micro Benchmark (JMH) Generation Prompt Template - No Ablation: Include persona and include few shot
PROMPT_NO_ABLATION="Act as an expert Java performance engineer specializing in micro-benchmarking with JMH (Java Microbenchmark Harness).

Generate a comprehensive JMH benchmark class using JMH version 1.37 (latest stable release as of 2024).

## Target Information
- **Production Class**: $PRODUCTION_CLASS_NAME
- **Package Path**: $PACKAGE_PATH
- **Lines to Benchmark**: $LINES
- **Specific Methods**: $METHOD_NAMES

## Requirements

### 1. Dependencies and Imports
- Use ONLY JMH version 1.37 dependencies
- Use ONLY imports that are legal and available in the project scope
- DO NOT use any test dependencies (JUnit, Mockito, etc.)
- DO NOT use mocking frameworks or test utilities
- All dependencies must be production-grade or JMH-specific

### 2. Benchmark Scope
- Focus ONLY on the specified lines ($LINES)
- Create individual benchmarks for EACH method separately
- DO NOT write micro-benchmarks that combine multiple methods
- Each @Benchmark method should test ONE production method in isolation

### 3. Naming Convention
- Class name MUST follow this pattern: $PRODUCTION_CLASS_NAMEBenchmarkLLM
- Benchmark methods should be named: \`benchmark<MethodName>\`
- State class should be named: \`BenchmarkState\`

### 4. State Management
- Use ONLY ONE @State inner class named \`BenchmarkState\`
- Instantiate ALL dependencies within this single state class
- Use @Setup method to initialize objects
- Use appropriate Scope (Thread, Benchmark, or Invocation) based on: $STATE_SCOPE

### 5. Configuration
- Include standard JMH annotations: @BenchmarkMode, @OutputTimeUnit, @Warmup, @Measurement, @Fork
- Use recommended defaults unless specified otherwise
- Include appropriate @Param annotations for parameterized benchmarks if needed

## Example Template

\`\`\`java
package {{PACKAGE_PATH}}. benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options. OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
* JMH Benchmark for $PRODUCTION_CLASS_NAME
* Generated to benchmark lines $LINES
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Thread)
public class $PRODUCTION_CLASS_NAMEBenchmarkLLM {

  @State(Scope.Thread)
  public static class BenchmarkState {

      // Declare all dependencies here
      private {{PRODUCTION_CLASS_NAME}} targetInstance;
      private Object dependency1;
      private Object dependency2;

      @Setup(Level.Trial)
      public void setup() {
          // Initialize all dependencies
          dependency1 = new Object();
          dependency2 = new Object();
          targetInstance = new {{PRODUCTION_CLASS_NAME}}(dependency1, dependency2);
      }
  }

  @Benchmark
  public void benchmarkMethodName(BenchmarkState state) {
      // Benchmark a single method from the target class
      state.targetInstance.methodName();
  }

  public static void main(String[] args) throws RunnerException {
      Options opt = new OptionsBuilder()
              .include({{PRODUCTION_CLASS_NAME}}BenchmarkLLM.class.getSimpleName())
              .build();
      new Runner(opt).run();
  }
}
\`\`\`

## Output Format
Provide:
1. Complete, runnable JMH benchmark class
2. Required Maven/Gradle dependencies snippet
3. Brief explanation of each benchmark method
4. Recommended JVM arguments for optimal benchmarking

Generate the benchmark class now following all the above requirements.
"

# Java Micro Benchmark (JMH) Generation Prompt Template - Ablation 1: Remove persona and keep few shot
PROMPT_ABLATION_ONE="Generate a comprehensive JMH benchmark class using JMH version 1.37 (latest stable release as of 2024).

## Target Information
- **Production Class**: $PRODUCTION_CLASS_NAME
- **Package Path**: $PACKAGE_PATH
- **Lines to Benchmark**: $START_LINE-$END_LINE
- **Specific Methods**: $METHOD_NAMES

## Requirements

### 1. Dependencies and Imports
- Use ONLY JMH version 1.37 dependencies
- Use ONLY imports that are legal and available in the project scope
- DO NOT use any test dependencies (JUnit, Mockito, etc.)
- DO NOT use mocking frameworks or test utilities
- All dependencies must be production-grade or JMH-specific

### 2. Benchmark Scope
- Focus ONLY on the specified lines ($START_LINE-$END_LINE)
- Create individual benchmarks for EACH method separately
- DO NOT write micro-benchmarks that combine multiple methods
- Each @Benchmark method should test ONE production method in isolation

### 3. Naming Convention
- Class name MUST follow this pattern: $PRODUCTION_CLASS_NAMEBenchmarkLLM
- Benchmark methods should be named: \`benchmark<MethodName>\`
- State class should be named: \`BenchmarkState\`

### 4. State Management
- Use ONLY ONE @State inner class named \`BenchmarkState\`
- Instantiate ALL dependencies within this single state class
- Use @Setup method to initialize objects
- Use appropriate Scope (Thread, Benchmark, or Invocation) based on: $STATE_SCOPE

### 5. Configuration
- Include standard JMH annotations: @BenchmarkMode, @OutputTimeUnit, @Warmup, @Measurement, @Fork
- Use recommended defaults unless specified otherwise
- Include appropriate @Param annotations for parameterized benchmarks if needed

## Example Template

\`\`\`java
package {{PACKAGE_PATH}}. benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options. OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
* JMH Benchmark for $PRODUCTION_CLASS_NAME
* Generated to benchmark lines $START_LINE-$END_LINE
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Thread)
public class $PRODUCTION_CLASS_NAMEBenchmarkLLM {

  @State(Scope.Thread)
  public static class BenchmarkState {

      // Declare all dependencies here
      private {{PRODUCTION_CLASS_NAME}} targetInstance;
      private Object dependency1;
      private Object dependency2;

      @Setup(Level.Trial)
      public void setup() {
          // Initialize all dependencies
          dependency1 = new Object();
          dependency2 = new Object();
          targetInstance = new {{PRODUCTION_CLASS_NAME}}(dependency1, dependency2);
      }
  }

  @Benchmark
  public void benchmarkMethodName(BenchmarkState state) {
      // Benchmark a single method from the target class
      state.targetInstance.methodName();
  }

  public static void main(String[] args) throws RunnerException {
      Options opt = new OptionsBuilder()
              .include({{PRODUCTION_CLASS_NAME}}BenchmarkLLM.class.getSimpleName())
              .build();
      new Runner(opt).run();
  }
}
\`\`\`

## Output Format
Provide:
1. Complete, runnable JMH benchmark class
2. Required Maven/Gradle dependencies snippet
3. Brief explanation of each benchmark method
4. Recommended JVM arguments for optimal benchmarking

Generate the benchmark class now following all the above requirements.
"

# Java Micro Benchmark (JMH) Generation Prompt Template - Ablation 2: Keep persona and remove few shot
PROMPT_ABLATION_TWO="Act as an expert Java performance engineer specializing in micro-benchmarking with JMH (Java Microbenchmark Harness).

Generate a comprehensive JMH benchmark class using JMH version 1.37 (latest stable release as of 2024).

## Target Information
- **Production Class**: $PRODUCTION_CLASS_NAME
- **Package Path**: $PACKAGE_PATH
- **Lines to Benchmark**: $START_LINE-$END_LINE
- **Specific Methods**: $METHOD_NAMES

## Requirements

### 1. Dependencies and Imports
- Use ONLY JMH version 1.37 dependencies
- Use ONLY imports that are legal and available in the project scope
- DO NOT use any test dependencies (JUnit, Mockito, etc.)
- DO NOT use mocking frameworks or test utilities
- All dependencies must be production-grade or JMH-specific

### 2. Benchmark Scope
- Focus ONLY on the specified lines ($START_LINE-$END_LINE)
- Create individual benchmarks for EACH method separately
- DO NOT write micro-benchmarks that combine multiple methods
- Each @Benchmark method should test ONE production method in isolation

### 3. Naming Convention
- Class name MUST follow this pattern: $PRODUCTION_CLASS_NAMEBenchmarkLLM
- Benchmark methods should be named: \`benchmark<MethodName>\`
- State class should be named: \`BenchmarkState\`

### 4. State Management
- Use ONLY ONE @State inner class named \`BenchmarkState\`
- Instantiate ALL dependencies within this single state class
- Use @Setup method to initialize objects
- Use appropriate Scope (Thread, Benchmark, or Invocation) based on: $STATE_SCOPE

### 5. Configuration
- Include standard JMH annotations: @BenchmarkMode, @OutputTimeUnit, @Warmup, @Measurement, @Fork
- Use recommended defaults unless specified otherwise
- Include appropriate @Param annotations for parameterized benchmarks if needed

## Output Format
Provide:
1. Complete, runnable JMH benchmark class
2. Required Maven/Gradle dependencies snippet
3. Brief explanation of each benchmark method
4. Recommended JVM arguments for optimal benchmarking

Generate the benchmark class now following all the above requirements.
"

# Java Micro Benchmark (JMH) Generation Prompt Template - Ablation 2: Remove persona and remove few shot
PROMPT_ABLATION_THREE="Generate a comprehensive JMH benchmark class using JMH version 1.37 (latest stable release as of 2024).

## Target Information
- **Production Class**: $PRODUCTION_CLASS_NAME
- **Package Path**: $PACKAGE_PATH
- **Lines to Benchmark**: $START_LINE-$END_LINE
- **Specific Methods**: $METHOD_NAMES

## Requirements

### 1. Dependencies and Imports
- Use ONLY JMH version 1.37 dependencies
- Use ONLY imports that are legal and available in the project scope
- DO NOT use any test dependencies (JUnit, Mockito, etc.)
- DO NOT use mocking frameworks or test utilities
- All dependencies must be production-grade or JMH-specific

### 2. Benchmark Scope
- Focus ONLY on the specified lines ($START_LINE-$END_LINE)
- Create individual benchmarks for EACH method separately
- DO NOT write micro-benchmarks that combine multiple methods
- Each @Benchmark method should test ONE production method in isolation

### 3. Naming Convention
- Class name MUST follow this pattern: $PRODUCTION_CLASS_NAMEBenchmarkLLM
- Benchmark methods should be named: \`benchmark<MethodName>\`
- State class should be named: \`BenchmarkState\`

### 4. State Management
- Use ONLY ONE @State inner class named \`BenchmarkState\`
- Instantiate ALL dependencies within this single state class
- Use @Setup method to initialize objects
- Use appropriate Scope (Thread, Benchmark, or Invocation) based on: $STATE_SCOPE

### 5. Configuration
- Include standard JMH annotations: @BenchmarkMode, @OutputTimeUnit, @Warmup, @Measurement, @Fork
- Use recommended defaults unless specified otherwise
- Include appropriate @Param annotations for parameterized benchmarks if needed

## Output Format
Provide:
1. Complete, runnable JMH benchmark class
2. Required Maven/Gradle dependencies snippet
3. Brief explanation of each benchmark method
4. Recommended JVM arguments for optimal benchmarking

Generate the benchmark class now following all the above requirements.
"

if [[ $PROMPT_CHOICE = "no_ablation" ]]; then
  echo Selected prompt with no ablation - Included persona and included few shot:
  echo
  PROMPT_TO_USE="$PROMPT_NO_ABLATION"
elif [[ $PROMPT_CHOICE = "ablation_one" ]]; then
  echo Selected prompt with one ablation - Removed persona and kept few shot:
  echo
  PROMPT_TO_USE="$PROMPT_ABLATION_ONE"
elif [[ $PROMPT_CHOICE = "ablation_two" ]]; then
  echo Selected prompt with one ablation - Kept persona and removed few shot:
  echo
  PROMPT_TO_USE="$PROMPT_ABLATION_TWO"
elif [[ $PROMPT_CHOICE = "ablation_three" ]]; then
  echo Selected prompt with two ablations - Removed persona and removed few shot:
  echo
  PROMPT_TO_USE="$PROMPT_ABLATION_THREE"
else
  echo "Please provide a valid prompt choice: no_ablation, ablation_one, ablation_two, ablation_three"
  exit 1
fi

echo "$PROMPT_TO_USE"
copilot -p "$PROMPT_TO_USE" --allow-all-paths --allow-all-tools
