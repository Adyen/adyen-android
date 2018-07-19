#!/usr/bin/env bash

# Clone the repo
git clone https://github.com/Adyen/adyen-android.git
cd adyen-android
git checkout master &&

# Read the API key
read -p "Please enter your Checkout Demo Server API Key (x-demo-server-api-key):`echo $'\n> '`" apiKey
apiKey=${apiKey:-<YOUR_DEMO_SERVER_API_KEY>}
sed -e "s/<YOUR_DEMO_SERVER_API_KEY>/$apiKey/g" example-app/local.gradle.example > example-app/local.gradle

# Build, install, and launch the app
./gradlew assembleDebug installDebug && adb shell am start -n com.adyen.example/.MainActivity

case "$(uname -s)" in
    Darwin*)    open -a "Android Studio" build.gradle;;
    Linux*)     echo "Open $(pwd)/build.gradle with Android Studio";;
esac
