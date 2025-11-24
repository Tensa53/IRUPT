#!/usr/bin/bash
TEST_TOOL=$1

if [[ $TEST_TOOL = "junit" || $TEST_TOOL = "jmh" ]]; then
  mapfile -t FILES < <(tree target/jacoco-$TEST_TOOL/ -fi -L 4 | grep "jacoco.exec")
  total=${#FILES[@]}

  # copy the directory with exec to preserve it before generating full reports
  cp -r target/jacoco-$TEST_TOOL/ target/jacoco-$TEST_TOOL-execs
  # copy the directory to copy only xml reports in it for data preparation
  cp -r target/jacoco-$TEST_TOOL/ target/jacoco-$TEST_TOOL-xml

  for ((i = 0; i < total; i++)); do
          file=${FILES[i]}
          JACOCO_EXEC_DIR_BASE=$(echo ${file} | awk '{ print substr($file, 1, length($file)-11) }')
          echo base dir: $JACOCO_EXEC_DIR_BASE
          echo Generating report for file $((i+1)) of $total: $file
          mvn jacoco:report -Djacoco.dataFile=$file
          mv target/site/jacoco/ target/site/jacoco-report
          mv target/site/jacoco-report/ $JACOCO_EXEC_DIR_BASE
  done

  mapfile -t REPORTS < <(tree target/jacoco-$TEST_TOOL/ -fi -L 4 | grep -E '(^|/)[^/]*jacoco-report/?$')
  mapfile -t FILES < <(tree target/jacoco-$TEST_TOOL-xml/ -fi -L 4 | grep "jacoco.exec")
  total=${#REPORTS[@]}

  for ((i = 0; i < total; i++)); do
          report=${REPORTS[i]}
          file=${FILES[i]}
          JACOCO_EXEC_XML_DIR_BASE=$(echo ${file} | awk '{ print substr($report, 1, length($report)-11) }')
          echo moving xml report for file $((i+1)) of $total: $file
          cp $report/jacoco.xml $JACOCO_EXEC_XML_DIR_BASE/
  done
else
  echo "Please provide a valid test tool name as an argument: junit or jmh"
fi