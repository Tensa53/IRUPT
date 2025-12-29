#!/usr/bin/bash
TEST_TOOL=$1

if [[ $TEST_TOOL = "junit" || $TEST_TOOL = "jmh" ]]; then
  DESTINATION_DIR="all-execs-$TEST_TOOL"
  mkdir -p $DESTINATION_DIR
  mapfile -t FILES < <(tree target/jacoco-$TEST_TOOL/ -fi -L 4 | grep "jacoco.exec")
  total=${#FILES[@]}
  for ((i = 0; i < total; i++)); do
      file=${FILES[i]}
      echo Copying exec file $((i+1)) of $total: $file
      cp "${FILES[i]}" $DESTINATION_DIR/jacoco-exec-$((i+1)).exec
  done

  java -jar jacococli.jar merge all-execs-$TEST_TOOL/*.exec --destfile all-execs-$TEST_TOOL/jacoco-merge.exec
  mvn jacoco:report -Djacoco.dataFile=all-execs-$TEST_TOOL/jacoco-merge.exec
  mv target/site/jacoco/ all-execs-$TEST_TOOL/jacoco-merged-report
else
  echo "Please provide a valid test tool name as an argument: junit or jmh"
fi
