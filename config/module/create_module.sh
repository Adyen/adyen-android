#!/usr/bin/env bash
RED=`tput setaf 1`
GREEN=`tput setaf 2`
YELLOW=`tput setaf 3`
NOCOLOR=`tput sgr0`

BASEDIR=$(dirname "$0")

module_name=$1
module_package_name=$2

SOURCE=./$BASEDIR/template
DEST=./$BASEDIR/../../${module_name}

cp -r ${SOURCE} ${DEST}

find $DEST -name '*.*' -exec sed -i -e "s/#module_name/$module_name/g" {} \;
find $DEST -name '*.*' -exec sed -i -e "s/#module_package_name/$module_package_name/g" {} \;
find $DEST -name '*-e' -exec rm {} +

module_package_name=${module_package_name//./\/}
mkdir -p $DEST/src/main/java/$module_package_name

echo ${GREEN}${module_name}${NOCOLOR} with package name of ${GREEN}${module_package_name}${NOCOLOR} created successfully

printf ", '${module_name}'" >> $DEST/../settings.gradle