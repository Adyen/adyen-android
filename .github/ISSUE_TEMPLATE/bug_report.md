---
name: Bug report
about: Use this template to report any issues or bugs you encounter while using our SDK.
title: ''
labels: 'bug report'
assignees: ''

---

## Description
Please provide a clear and concise description of the bug you're experiencing. Include details on what you expected to happen versus what actually occurred.

## Steps to Reproduce
1. List all the steps needed to reproduce the issue. Include any specific settings, configurations, or conditions.

2. If possible, provide screenshots or a screen recording that clearly illustrates the issue.

## Logs and Crash Reports
1. Include any relevant logs.
2. Enable verbose logging and attach the logs here.
    - For SDK version `V5.*.*`:
        - Enable by calling `com.adyen.checkout.core.AdyenLogger.setLogLevel(Log.VERBOSE)` in your application class.
        - Filter your logs by `CO.` to see the checkout logs only.

    - For SDK version `V4.*.*`:
        - Enable by calling `com.adyen.checkout.core.log.Logger.setLogcatLevel(Log.VERBOSE)` in your application class.
        - Filter your logs by `CO.` to see the checkout logs only.

3. If the issue results in a crash, please include the full stack trace.

## SDK-Related Code Segments
1. Provide relevant code snippets where the SDK is implemented.

2. If possible, highlight the portions of the code where the issue seems to occur.

## Integration Information
1. Specify the integration type you're using:
    - Drop-in
    - Components

2. Specify the integration flow you're using:
    - Sessions
    - Advanced

3. Specify the version of the SDK you're using.

4. Specify the Android version(s) affected.

5. Include the model of the device(s) where the issue occurs.

## Additional Context
Please provide any other information that might be helpful in diagnosing the issue. This could include environment details, specific configurations, or related issues.
