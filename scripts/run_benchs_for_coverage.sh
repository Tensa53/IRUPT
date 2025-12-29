#!/bin/sh

# variable to easy modify the parameters of the script
jacoco_agent_path="$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13-runtime.jar"
jacoco_exec_path_jmh="target/bench.exec"
packages_to_include_jacoco="org/apache/avro/data/*,org/apache/avro/util/*"
jar_path="target/avro-benchmarks.jar"
jmh_args="-f 1 -i 1 -wi 0 -bm ss -tu ms" # minimal jmh conf (1 fork-iteration, no warmup, single-shot, time milliseconds)
profiler_class_name="org.apache.avro.benchmarks.profilers.JaCoCoProfiler"

# execute jar file to run benchmarks with jacoco java agent and JaCoCoProfiler
java -javaagent:$jacoco_agent_path=destfile=$jacoco_exec_path_jmh,jmx=true -jar $jar_path $jmh_args -jvmArgs "-javaagent:$jacoco_agent_path=destfile=$jacoco_exec_path_jmh,jmx=true" -prof $profiler_class_name
