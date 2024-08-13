---
name: Bug report
about: Use this template to report any issues or bugs you encounter while using our SDK.
title: ''
labels: 'bug report'
assignees: ''

---
<!-- Please fill as many fields as possible, including screenshots or screen recordings, logs, code snippets, environment details, and any other information that could help in diagnosing and resolving the issue. -->

## Pre-Submission Checklist
1. I checked [previously opened issues](https://github.com/Adyen/adyen-android/issues?q=is%3Aissue) and confirmed that my bug hasn't been reported before: <!-- Yes/No -->
2. I reviewed the [integration guide](https://docs.adyen.com/online-payments/build-your-integration/?platform=Android) and [Github page](https://github.com/Adyen/adyen-android) to ensure that everything is implemented correctly: <!-- Yes/No -->
3. I verified that the question specifically relates to the Android SDK: <!-- Yes/No -->

## Description
<!-- Please provide a clear and concise description of the bug you're experiencing. Include details on what you expected to happen versus what actually occurred. -->

## Steps to Reproduce
1. I am able to consistently reproduce this issue: <!-- Yes/No -->
<!-- List all the steps needed to reproduce the issue. Include any specific settings, configurations, or conditions. -->
2. Steps to reproduce the issue:
    1. Step 1
    2. Step 2
    3. Step 3
<!-- If possible, provide screenshots or a screen recording that clearly illustrates the issue. -->
3. Screenshots or a screen recording:

## Logs and Crash Reports
<!--
Enable verbose logging and attach the logs here.
    - For SDK version `V5.*.*`:
        - Enable by calling `com.adyen.checkout.core.AdyenLogger.setLogLevel(Log.VERBOSE)` in your application class.
        - Filter your logs by `CO.` to see the checkout logs only.

    - For SDK version `V4.*.*`:
        - Enable by calling `com.adyen.checkout.core.log.Logger.setLogcatLevel(Log.VERBOSE)` in your application class.
        - Filter your logs by `CO.` to see the checkout logs only. 
-->
<details>
  <summary>Relevant logs</summary>

  ```
Insert your logs here.
  ```
</details>

<!-- If the issue results in a crash, please include the full stack trace. -->
<details>
  <summary>Crash report</summary>

  ```
Insert the full stack here.
  ```
</details>

## Code Snippets
<!-- Provide code snippets where the SDK is implemented in your project. -->
```
Insert your code here
```

## Integration Information
1. Server-side integration: <!-- Sessions/Advanced flow -->
2. Client-side integration: <!-- Drop-in/Components -->
3. SDK version:
4. Android version(s) where issue occurs:
5. Device model(s) where issue occurs:

## Additional Context
<!-- Please provide any other information that might be helpful in diagnosing the issue. This could include environment details, specific configurations, or related issues. -->
