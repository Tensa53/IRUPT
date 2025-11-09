#!/bin/sh

# variable to easy modify the parameters of the script
coverage_report_initial_dir="reports-coverage"
time_report_junit_dir="reports-time/junit/"
jacoco_initial_report_path="build/reports/jacoco/"
jacoco_agent_path="$HOME/.gradle/caches/modules-2/files-2.1/org.jacoco/org.jacoco.agent/0.8.13/850ba9544f357712728f89fe3e1fd51b265a0192/org.jacoco.agent-0.8.13-runtime.jar"
jacoco_exec_path_jmh="build/jacoco/bench.exec"
packages_to_exclude_jacoco="org/example/runners/**"
jar_path="build/libs/GradleProjectJ4-1.0-SNAPSHOT-jmh.jar"
jmh_args="-f 1 -i 1 -wi 0 -bm ss -tu ms" # minimal jmh conf (1 fork-iteration, no warmup, single-shot, time milliseconds)
profiler_class_name="benchmarks.profilers.JaCoCoProfiler"
uncovered_methods_class_name="org.example.runners.JaCoCoXMLUncoveredMethods"
jacoco_xml_report_path="reports-coverage/jmh/jacoco/jacoco.xml"
packages_to_exclude_cloc="benchmarks,runners"
source_code_path="src/main/java/org/example"

# create initial directory
mkdir -p $coverage_report_initial_dir

# commands to generate jacoco report and coverage matrix from junit tests
# create subfolders for report from junit
mkdir -p "$coverage_report_initial_dir/junit/"
mkdir -p $time_report_junit_dir

# gradle commands
./gradlew clean
./gradlew build

# copy jacoco report into the report directory
cp -r $jacoco_initial_report_path "$coverage_report_initial_dir/junit/"

# commands to generate jacoco report and coverage matrix from jmh benchmarks
# create subfolder for report from jmh
mkdir -p "$coverage_report_initial_dir/jmh/"

# custom gradle task to create a jar
./gradlew jmhJar

# execute jar file to run benchmarks
java -javaagent:$jacoco_agent_path=destfile=$jacoco_exec_path_jmh,jmx=true,excludes=packages_to_exclude_jacoco -jar $jar_path $jmh_args -jvmArgs "-javaagent:$jacoco_agent_path=destfile=$jacoco_exec_path_jmh,jmx=true,excludes=$packages_to_exclude_jacoco" -prof $profiler_class_name

# custom gradle task to easy execute jacoco report for jmh
./gradlew jacocoExternalReport

# custom java class to generate uncovered methods json
java -cp $jar_path $uncovered_methods_class_name $jacoco_xml_report_path "$coverage_report_initial_dir/jmh/uncovered.json"

# command line tool to generate total source code lines json
cloc $source_code_path --include-lang=Java --exclude-dir=$packages_to_exclude_cloc --json --out=$coverage_report_initial_dir/total-lines.json