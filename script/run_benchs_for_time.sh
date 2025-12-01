#!/bin/bash

# List of fully qualified benchmark names
# Easy way to extract names with command: java -jar <jar_name> -l
benchmarks=(
  "org.example.benchmarks.hndwrt.AmministratoreBenchmarkHndwrt.benchGetDepartment"
  "org.example.benchmarks.hndwrt.AmministratoreBenchmarkHndwrt.benchGetName"
  "org.example.benchmarks.hndwrt.AmministratoreBenchmarkHndwrt.benchGetSurname"
  "org.example.benchmarks.hndwrt.AmministratoreBenchmarkHndwrt.benchSetDepartment"
  "org.example.benchmarks.hndwrt.AmministratoreBenchmarkHndwrt.benchSetName"
  "org.example.benchmarks.hndwrt.AmministratoreBenchmarkHndwrt.benchSetSurname"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchGetSaldo"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchPrelievo_InsufficienteSaldo"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchPrelievo_SufficienteSaldo"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchSetId"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchSetSaldo"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchSetSaldo2"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchVersamento"
  "org.example.benchmarks.hndwrt.ContoBancarioBenchmarkHndwrt.benchgetId"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchGetCode"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchGetName"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchGetProfession"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchGetSurname"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchSetCode"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchSetName"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchSetProfession"
  "org.example.benchmarks.hndwrt.TecnicoBenchmarkHndwrt.benchSetSurname"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchGetAddress"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchGetContoBancario"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchGetName"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchGetSurname"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchGetTelephone"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchSetConto"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchSetName"
  "org.example.benchmarks.hndwrt.UtenteBenchmarkHndwrt.benchSetSurname"
)

# Path to JMH benchmarks
JAR_PATH="target/MavenProjectJ4-1.0-SNAPSHOT.jar"
# JMH arguments
COMMON_ARGS="-f 10 -i 3000 -wi 0 -bm ss -tu ms"
# Path to save json output
REPORT_PATH="time-reports"
# Normal user to change ownership of output files
NORMAL_USER="daniele"

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
  nice -n -20 java -Xms8G -Xmx8G -jar "$JAR_PATH" "$bm"  $COMMON_ARGS -rff "$REPORT_PATH/$class_name/$method_name-benchmark-results.json" -rf json
done

chown -R $NORMAL_USER:$NORMAL_USER $REPORT_PATH

echo ">>> All benchmarks executed"
echo ">>> Finished."
