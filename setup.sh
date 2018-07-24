#!/usr/bin/env bash

# Validate
if [ -z $ANDROID_HOME ]; then
    echo Environment variable ANDROID_HOME is not defined. Make sure that the Android SDK is installed and the variable is exported.
    exit 1
fi

# Clone the repo
git clone https://github.com/Adyen/adyen-android.git
cd adyen-android
git checkout master

# Read the API key
if [ ! -f ./example-app/local.gradle ]; then
    read -p "Please enter your Checkout Demo Server API Key (x-demo-server-api-key):`echo $'\n> '`" apiKey
    apiKey=${apiKey:-<YOUR_DEMO_SERVER_API_KEY>}
    sed -e "s/<YOUR_DEMO_SERVER_API_KEY>/$apiKey/g" example-app/local.gradle.example > example-app/local.gradle
fi

# Build
./gradlew assembleDebug

# Install and launch
if [ ! -f $ANDROID_HOME/platform-tools/adb ]; then
    echo Could not find adb, checked: $ANDROID_HOME/platform-tools/adb
    exit 1
fi

$ANDROID_HOME/platform-tools/adb get-state 1 &> /dev/null
if [ $? == 0 ]; then
    ./gradlew installDebug
    $ANDROID_HOME/platform-tools/adb shell am start -n com.adyen.example/.MainActivity
else
    read -p "Is there a device connected? [y/n]`echo $'\n> '`" deviceConnected
    if [ $deviceConnected == y ]; then
        ./gradlew installDebug
        $ANDROID_HOME/platform-tools/adb shell am start -n com.adyen.example/.MainActivity
    fi
fi

# Launch Android Studio
case "$(uname -s)" in
    Darwin*)    open -a "Android Studio" build.gradle;;
    Linux*)     echo "Open $(pwd)/build.gradle with Android Studio";;
esac
