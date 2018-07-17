#!/usr/bin/env bash

git clone https://github.com/Adyen/adyen-android.git
cd adyen-android
git checkout 2.x &&
cp example-app/local.gradle.example example-app/local.gradle &&
./gradlew :example-app:assembleDebug
