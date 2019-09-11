#!/usr/bin/env bash
RED=`tput setaf 1`
GREEN=`tput setaf 2`
YELLOW=`tput setaf 3`
NOCOLOR=`tput sgr0`

BASEDIR=$(dirname "$0")

payment_method_name=$1

payment_package_name=com.adyen.checkout.${payment_method_name//-/}

sh $BASEDIR/create_module.sh $payment_method_name-base ${payment_package_name}
sh $BASEDIR/create_module.sh $payment_method_name-ui ${payment_package_name}