#!/bin/bash
MAX_RUN_NUMBER="10"
CURRENT_RUN="0"
TEST_COMMAND="./gradlew clean jacocoOverallTestReport -Dorg.gradle.parallel=false"
while [ $CURRENT_RUN -le $MAX_RUN_NUMBER ]
do
  eval $TEST_COMMAND
  if [ $? -eq 0 ]
then
  CURRENT_RUN=$((CURRENT_RUN+1))
else
  echo "Failed after running tests for $CURRENT_RUN times"
  exit 1
fi

done
echo "Tests ran succesfully completely for $MAX_RUN_NUMBER times"
exit 0
