#!/bin/bash

#
# Copyright (c) 2024 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by oscars on 14/6/2024.
#

input_file="build/api_changes.txt"
output_file="api_changes.md"

touch $output_file

#echo '' >> $output_file

should_append=false
did_change=false

while read line; do
  if [[ "$line" =~ "You can run :" ]]
  then
    should_append=false
    echo "\`\`\`" >> $output_file
  fi

  if $should_append
  then
    echo "$line" >> $output_file
  fi

  if [[ "$line" =~ "> API check failed for project" ]]
  then
    should_append=true
    did_change=true
    split=($line)
    split_size=${#split[@]}
    last_index=$(( split_size-1))
    title=${split[$last_index]}
    title=${title%?}
    echo "## $title" >> $output_file
    echo "\`\`\`" >> $output_file
  fi

done < $input_file

if $did_change
then
  echo "" >> $output_file
  echo "If these changes are intentional run \`./gradlew apiDump\` and commit the changes." >> $output_file
  echo "# 🚫 Public API Changes" | cat - $output_file > temp && mv temp $output_file
else
  echo "" >> $output_file
  echo "No changes detected!" >> $output_file
  echo "# ✅ Public API Changes" | cat - $output_file > temp && mv temp $output_file
fi
