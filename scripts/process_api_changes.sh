#!/bin/bash

#
# Copyright (c) 2024 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by oscars on 14/6/2024.
#

input_file="api_changes.txt"
output_file="api_changes.md"

touch "$output_file"

if [ -s $input_file ]
then
  echo "# 🚫 Public API Changes" >> "$output_file"
else
  echo "# ✅ Public API Changes" >> "$output_file"
  echo "No changes detected!" >> "$output_file"
  exit 0
fi

# Find all .api files to loop over later
api_files=($(find . -name '*.api'))
api_files_size=${#api_files[@]}

for ((i = 0 ; i < $api_files_size ; i+= 2 )); do
  git_diff=$(git diff --no-index --unified=0 "${api_files[i]}" "${api_files[i + 1]}")
  if [ -n "$git_diff" ]
  then
    # Add module name as title
    file_name=${api_files[i]}
    words=(${file_name//\// })
    words_size=${#words[@]}
    last_index=$(( words_size-1))
    title=${words[$last_index]}
    title=${title%????}
    echo "## $title" >> "$output_file"

    # Open code block
    echo "\`\`\`diff" >> "$output_file"

    diff_index=0
    # Dump git diff output
    while read -r line; do
      # Skip first 4 lines as they are verbose
      if (( $diff_index > 3 ))
      then
        echo "$line" >> "$output_file"
      fi
      ((diff_index++))
    done <<< "$git_diff"

    # Close code block
    echo "\`\`\`" >> "$output_file"
  fi
done

echo "If these changes are intentional run \`./gradlew apiDump\` and commit the changes." >> "$output_file"
