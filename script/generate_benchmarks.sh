CLASS_SOURCE_CODE_PATH=(
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/CacheDataRowAdapter.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/GridCacheDatabaseSharedManager.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/GridCacheOffheapManager.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/IgniteCacheDatabaseSharedManager.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/MemoryMetricsImpl.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/MemoryMetricsMXBeanImpl.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/MemoryMetricsSnapshot.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/MemoryPolicy.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/MetadataStorage.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/PersistenceMetricsImpl.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/PersistenceMetricsSnapshot.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/RootPage.java",
"src/main/java/org/apache/ignite/internal/processors/cache/persistence/RowStore.java"
)
BENCHMARK_CODE_PATH="src/main/java/org/apache/ignite/benchmark/LLM/"
CLASS_BENCHMARK_CODE_NAME=(
"CacheDataRowAdapterBenchmarkLLM.java",
"GridCacheDatabaseSharedManagerBenchmarkLLM.java",
"GridCacheOffheapManagerBenchmarkLLM.java",
"IgniteCacheDatabaseSharedManagerBenchmarkLLM.java",
"MemoryMetricsImplBenchmarkLLM.java",
"MemoryMetricsMXBeanImplBenchmarkLLM.java",
"MemoryMetricsSnapshotBenchmarkLLM.java",
"MemoryPolicyBenchmarkLLM.java",
"MetadataStorageBenchmarkLLM.java",
"PersistenceMetricsImplBenchmarkLLM.java",
"PersistenceMetricsSnapshotBenchmarkLLM.java",
"RootPageBenchmarkLLM.java",
"RowStoreBenchmarkLLM.java"
)
total=12

PROMPT_TEMPLATE="Create a micro-benchmark class with JMH 1.37 for the production class at %s and be sure to reach the highest possible coverage, for the statements and the branches of the class methods. Don't write micro-benchmark methods for the constructor method and for combined use of production code methods. When you need a state variable, be sure to use it through an inner state class and to pass an instance of the state class as a parameter to the micro-benchmark method. If it is feasible, use only one inner state class for the entire micro-benchmark class. Use legal imports and avoid to use mocks, don't use test class components in the micro-benchmarks. For the micro-benchmark methods, use a naming convention with 'bench' as a prefix and then use the name of the production code method. Don't verify that the class compiles. Write the obtained micro-benchmark class with name %s inside the %s folder"

for ((i = 0; i < total; i++)); do
  PROMPT=$(printf "$PROMPT_TEMPLATE" "${CLASS_SOURCE_CODE_PATH[$i]}" "${CLASS_BENCHMARK_CODE_NAME[$i]}" "$BENCHMARK_CODE_PATH")
  echo EXECUTING copilot -p "$PROMPT" --allow-all-tools
  copilot -p "$PROMPT" --allow-all-tools
done