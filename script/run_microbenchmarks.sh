#!/bin/bash

# List of fully qualified benchmark names
# Easy way to extract names with command: java -jar <jar_name> -l
benchmarks=(
  "benchmarks.UtenteBenchmark.benchGetName"
  "benchmarks.UtenteBenchmark.benchGetSurname"
  "benchmarks.UtenteBenchmark.benchGetTelephone"
  "benchmarks.UtenteBenchmark.benchGetAddress"
  "benchmarks.UtenteBenchmark.benchGetContoBancario"
  "benchmarks.UtenteBenchmark.benchSetName"
  "benchmarks.UtenteBenchmark.benchSetSurname"
  "benchmarks.UtenteBenchmark.benchSetConto"
  "benchmarks.TecnicoBenchmark.benchGetName"
  "benchmarks.TecnicoBenchmark.benchGetSurname"
  "benchmarks.TecnicoBenchmark.benchGetProfession"
  "benchmarks.TecnicoBenchmark.benchGetCode"
  "benchmarks.TecnicoBenchmark.benchSetName"
  "benchmarks.TecnicoBenchmark.benchSetSurname"
  "benchmarks.TecnicoBenchmark.benchSetProfession"
  "benchmarks.TecnicoBenchmark.benchSetCode"
  "benchmarks.ContoBancarioBenchmark.benchVersamento"
  "benchmarks.ContoBancarioBenchmark.benchPrelievo_SufficienteSaldo"
  "benchmarks.ContoBancarioBenchmark.benchPrelievo_InsufficienteSaldo"
  "benchmarks.ContoBancarioBenchmark.benchgetId"
  "benchmarks.ContoBancarioBenchmark.benchSetId"
  "benchmarks.ContoBancarioBenchmark.benchGetSaldo"
  "benchmarks.ContoBancarioBenchmark.benchSetSaldo"
  "benchmarks.ContoBancarioBenchmark.benchSetSaldo2"
  "benchmarks.AmministratoreBench.benchGetName"
  "benchmarks.AmministratoreBench.benchGetSurname"
  "benchmarks.AmministratoreBench.benchGetDepartment"
  "benchmarks.AmministratoreBench.benchSetName"
  "benchmarks.AmministratoreBench.benchSetSurname"
  "benchmarks.AmministratoreBench.benchSetDepartment"
)

# Path to JMH benchmarks
JAR_PATH="build/libs/GradleProjectJ4-1.0-SNAPSHOT-jmh.jar"
COMMON_ARGS="-f 10 -i 3000 -wi 0 -bm ss -tu ms"
REPORT_PATH="reports-time/jmh/"

# Random execution loop
remaining_benchmarks=("${benchmarks[@]}")
total=${#remaining_benchmarks[@]}

mkdir -p "$REPORT_PATH"

# Main loop
for ((i = 0; i < total; i++)); do
  idx=$((RANDOM % ${#remaining_benchmarks[@]}))
  bm="${remaining_benchmarks[$idx]}"
  unset 'remaining_benchmarks[idx]'
  # Re-index array to avoid holes
  remaining_benchmarks=("${remaining_benchmarks[@]}")

  echo "### Executing benchmark: $bm"

  #class_name=$(echo "$bm" | awk -F'.' '{split($(NF-2), a, "_"); print a[1]}')
  #method_name=$(echo "$bm" | awk -F'.' '{print $NF}')
  class_name=$(echo "$bm" | awk -F'.' '{$NF=""; sub(/\.$/, ""); print $0}' OFS='.')
  method_name=$(echo "$bm" | awk -F'.' '{print $NF}')

  #output_dir="$class_name/$method_name"
  mkdir -p "$REPORT_PATH/$class_name"

  echo "  -> Execution $i: running benchmark"
  nice -n -20 java -Xms8G -Xmx8G -jar "$JAR_PATH" "$bm"  $COMMON_ARGS -rff "$class_name/$method_name-benchmark-results.json" -rf json
done

echo ">>> All benchmarks executed"
echo ">>> Finished."
